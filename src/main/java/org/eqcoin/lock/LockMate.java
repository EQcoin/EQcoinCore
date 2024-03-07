/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.lock;

import java.io.ByteArrayOutputStream;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.publickey.Publickey;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class LockMate extends EQCObject {
	
	private ID id;
	private boolean isIDUpdated;
	private Lock lock;
	private boolean isLockUpdated;
	private byte status;
	private boolean isStatusUpdated;
	private Publickey publickey;
	private boolean isPublickeyUpdated;
	private EQCHive eqcHive;
	
	public enum STATUS {
		MASTER((byte)0), SUB((byte)1), LIVELY((byte)253), FORBIDDEN((byte)2);
		
		private STATUS(byte status) {
			this.status = status;
		}

		private byte status;

		public byte getStatus() {
			return status;
		}

		public static STATUS get(int statusValue) {
			STATUS status = null;
			switch (statusValue) {
			case 1:
				status = MASTER;
				break;
			case 2:
				status = SUB;
				break;
			case 4:
				status = LIVELY;
				break;
			case 6:
				status = FORBIDDEN;
				break;
			default:
				throw new IllegalStateException("Invalid STATUS value: " + statusValue);
			}
			return status;
		}
		
	}
	
	public LockMate() {}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBodyBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(lock.getBytes());
		os.write(publickey.getBytes());
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(id == null) {
			Log.Error("id == null");
			return false;
		}
		if(!id.isSanity()) {
			Log.Error("!id.isSanity()");
			return false;
		}
		if(lock == null) {
			Log.Error("eqcLock == null");
			return false;
		}
		if(!lock.isSanity()) {
			Log.Error("!eqcLock.isSanity()");
			return false;
		}
		if((publickey != null) && !publickey.isSanity()) {
			Log.Error("!eqcPublickey.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
		isIDUpdated = true;
	}

	/**
	 * @return the lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @param lock the lock to set
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
		isLockUpdated = true;
	}

	/**
	 * @return the publickey
	 */
	public Publickey getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(Publickey publickey) {
		this.publickey = publickey;
		isPublickeyUpdated = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}

	public String toInnerJson() {
		return "\"LockMate\":" + "{\n" + 
				"\"ID\":" + "\"" + id + "\"" + ",\n" +
				lock.toInnerJson() + ",\n" +
				((publickey!=null)?publickey.toInnerJson():null) +
				"\n}";
	}

	public void setMaster() {
		status &= STATUS.MASTER.getStatus();
//		isStatusUpdated = true;
	}
	
	public void setSub() {
		status |= STATUS.SUB.getStatus();
		isStatusUpdated = true;
	}
	
	public void setLively() {
		status &= STATUS.LIVELY.getStatus();
		isStatusUpdated = true;
	}
	
	public void setForbidden() {
		status |= STATUS.FORBIDDEN.getStatus();
		isStatusUpdated = true;
	}
	
	public boolean isMaster() {
		return (status & Util.BIT_0) == 0;
	}
	
	public boolean isSub() {
		return (status & Util.BIT_0) == 1;
	}
	
	public boolean isLively() {
		return (status & Util.BIT_1) == 0;
	}
	
	public boolean isForbidden() {
		return (status & Util.BIT_1) == 1;
	}

	/**
	 * @return the status
	 */
	public byte getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(byte status) {
		this.status = status;
		isStatusUpdated = true;
	}

	/**
	 * @return the isIDUpdated
	 */
	public boolean isIDUpdated() {
		return isIDUpdated;
	}

	/**
	 * @return the isLockUpdated
	 */
	public boolean isLockUpdated() {
		return isLockUpdated;
	}

	/**
	 * @return the isStatusUpdated
	 */
	public boolean isStatusUpdated() {
		return isStatusUpdated;
	}

	/**
	 * @return the isPublickeyUpdated
	 */
	public boolean isPublickeyUpdated() {
		return isPublickeyUpdated;
	}
	
	public void sync() {
		isLockUpdated = true;
		isStatusUpdated = true;
		isPublickeyUpdated = true;
	}
	
	public boolean isSync() {
		return isLockUpdated && isStatusUpdated && isPublickeyUpdated;
	}
	
	public void planting() throws Exception {
		eqcHive.getGlobalState().saveLockMate(this);
	}
	
	/**
	 * Forbidden relevant lock mate:
	 * Set it's status to forbidden and clear it's publickey.
	 * @throws Exception
	 */
	public LockMate forbidden() throws Exception {
		if(!publickey.isNULL()) {
			publickey.setPublickey(null);
		}
		setForbidden();
		return this;
	}

	public EQCHive getEQCHive() {
		return eqcHive;
	}

	public LockMate setEQCHive(EQCHive eqcHive) {
		this.eqcHive = eqcHive;
		return this;
	}
	
}
