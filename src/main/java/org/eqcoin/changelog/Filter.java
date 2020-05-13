/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.changelog;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.Publickey;
import org.eqcoin.passport.Passport;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 16, 2019
 * @email 10509759@qq.com
 */
public class Filter {
	private ChangeLog changeLog;
	private Mode mode;
	private Connection connection;

	public enum Mode {
		GLOBAL, MINING, VALID
	}

	public Filter(Mode mode) throws ClassNotFoundException, SQLException, Exception {
		if(mode == Mode.GLOBAL) {
			throw new IllegalStateException("Filter's mode only support MINING and VALID");
		}
		this.mode = mode;
		connection = Util.DB().getConnection();
		// In case before close the app crashed or abnormal interruption so here just
		// clear the table
		clear();
	}

//	public Account getAccount(ID id, boolean isLoadInFilter) throws Exception {
//		// Here maybe exists some bugs need do more test
//		// Test find during verify block due to before Transaction.update the total
//		// account numbers will not increase so the new account's id will exceed the
//		// total account numbers
////		EQCType.assertNotBigger(id, changeLog.getTotalAccountNumbers());
//		return getAccount(id, isLoadInFilter);
//	}

//	/**
//	 * Search the Account in Filter table
//	 * For security issue only support search address via AddressAI
//	 * <p>
//	 * 
//	 * @param key
//	 * @return
//	 * @throws Exception 
//	 * @throws SQLException 
//	 * @throws ClassNotFoundException 
//	 */
//	public boolean isAccountExists(Lock key) throws ClassNotFoundException, SQLException, Exception {
//		boolean isSucc = false;
//		// For security issue only support search address via AddressAI
//		if (Util.DB().getPassport(key.getAddressAI(), mode) != null) {
//			isSucc = true;
//		}
//		return isSucc;
//	}

	public void savePassport(Passport passport) throws ClassNotFoundException, SQLException, Exception {
		Util.DB().savePassport(passport, mode);
	}

	public Passport getPassport(ID id, boolean isFiltering) throws Exception {
		Passport passport = null;
		// Check if Account already loading in filter
		if(isFiltering) {
			passport = Util.DB().getPassport(id, mode);
		}
		if (passport == null)  {
			// The first time loading account need loading the previous block's snapshot but
			// doesn't include No.0 EQCHive
			if (changeLog.getHeight().compareTo(ID.ZERO) > 0) {
				ID tailHeight = Util.DB().getEQCHiveTailHeight();
				if (changeLog.getHeight().isNextID(tailHeight)) {
					passport = Util.DB().getPassport(id, Mode.GLOBAL);
					// here if need check the lock create height?
//					if(!(account != null && account.getCreateHeight().compareTo(changeLog.getHeight()) < 0 && account.getLockCreateHeight().compareTo(changeLog.getHeight()) < 0 && account.getId().compareTo(changeLog.getPreviousTotalAccountNumbers()) <= 0)) {
//						Log.Error("Account exists but doesn't valid" + account);
//						account = null;
//					}
				} else if (changeLog.getHeight().compareTo(tailHeight) <= 0) {
					// Load relevant Account from snapshot
					passport = EQCHiveH2.getInstance().getPassportSnapshot(new ID(id),
							changeLog.getHeight());
				} else {
					throw new IllegalStateException("Wrong height " + changeLog.getHeight() + " tail height "
							+ Util.DB().getEQCHiveTailHeight());
				}
			} else {
				passport = EQCHiveH2.getInstance().getPassportSnapshot(new ID(id), ID.ZERO);
			}
		}
		return passport;
	}
	
	public Passport getPassportFromLockId(ID lockId) throws Exception {
		Passport passport = null;
		passport = Util.DB().getPassportFromLockId(lockId, mode);
		return passport;
	}
	
	public void saveLock(LockMate lock) throws ClassNotFoundException, SQLException, Exception {
		Util.DB().saveLock(lock, mode);
	}

	public LockMate getLock(ID id, boolean isFiltering) throws Exception {
		LockMate lock = null;
		// Check if Lock already loading in filter
		if(isFiltering) {
			lock = Util.DB().getLock(id, mode);
		}
		if (lock == null)  {
				ID tailHeight = Util.DB().getEQCHiveTailHeight();
				if (changeLog.getHeight().isNextID(tailHeight)) {
					lock = Util.DB().getLock(id, Mode.GLOBAL);
				} else if (changeLog.getHeight().compareTo(tailHeight) <= 0) {
					if(id.compareTo(changeLog.getPreviousTotalLockNumbers()) <= 0) {
						lock =  EQCHiveH2.getInstance().getLockSnapshot(id,
								changeLog.getHeight());
						if(lock == null) {
							lock = Util.DB().getLock(id, Mode.GLOBAL);
						}
					}
				} else {
					throw new IllegalStateException("Wrong height " + changeLog.getHeight() + " tail height "
							+ Util.DB().getEQCHiveTailHeight());
				}
		}
		return lock;
	}
	
	public ID isLockExists(Lock eqcLock, boolean isFiltering) throws Exception {
		ID lockId = null;
		// Check if Lock already loading in filter
		if(isFiltering) {
			lockId = Util.DB().isLockExists(eqcLock, mode);
		}
		if (lockId == null)  {
				ID tailHeight = Util.DB().getEQCHiveTailHeight();
				if (changeLog.getHeight().isNextID(tailHeight)) {
					lockId = Util.DB().isLockExists(eqcLock, Mode.GLOBAL);
				} else if (changeLog.getHeight().compareTo(tailHeight) <= 0) {
					lockId = Util.DB().isLockExists(eqcLock, Mode.GLOBAL);
					if(lockId != null && lockId.compareTo(changeLog.getPreviousTotalLockNumbers()) >= 0) {
						lockId = null;
					}
				} else {
					throw new IllegalStateException("Wrong height " + changeLog.getHeight() + " tail height "
							+ Util.DB().getEQCHiveTailHeight());
				}
		}
		return lockId;
	}
	
	public void merge() throws Exception {
		Util.DB().mergeLock(mode);
		Util.DB().mergePassport(mode);
	}

	public void takeSnapshot() throws Exception {
		Util.DB().takeLockSnapshot(mode, changeLog);
		Util.DB().takePassportSnapshot(mode, changeLog);
	}

	public void clear() throws ClassNotFoundException, SQLException, Exception {
		Util.DB().clearLock(mode);
		Util.DB().clearPassport(mode);
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * @return the mode
	 */
	public Mode getMode() {
		return mode;
	}
	
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		return connection;
	}

}
