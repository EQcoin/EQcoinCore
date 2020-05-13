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
package org.eqcoin.transaction.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.passport.Passport;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.Value;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class ChangeLock extends Operation {
	private Lock lock;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		op = OP.LOCK;
	}

	public ChangeLock() throws Exception {
		super();
	}
	
	public ChangeLock(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public void planting() throws Exception {
		Passport passport = transaction.getChangeLog().getFilter().getPassport(transaction.getTxIn().getPassportId(), true);
		LockMate lockMate = new LockMate();
		lockMate.setId(transaction.getChangeLog().getNextLockId());
		lockMate.setLock(lock);
		passport.setLockID(lockMate.getId());
		lockMate.setChangeLog(transaction.getChangeLog()).planting();
		passport.setChangeLog(transaction.getChangeLog()).planting();
		// Due to from here doesn't need forbidden lock any more so just remove it's publickey to release space
		// If light client want verify the forbidden lock proof can recovery it from relevant transaction's signature
		transaction.getTxInLockMate().getPublickey().setPublickey(null);
		transaction.getTxInLockMate().setChangeLog(transaction.getChangeLog()).planting();
		// At the last add the forbidden lock in case exist any exception
		transaction.getChangeLog().getForbiddenLockList().add(transaction.getTxInLockMate());
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
	}

	@Override
	public boolean isSanity() {
		if(op == null) {
			Log.Error("op == null");
			return false;
		}
		if(op != OP.LOCK) {
			Log.Error("op != OP.LOCK");
			return false;
		}
		if(lock == null) {
			Log.Error("lock == null");
			return false;
		}
		if(!lock.isSanity()) {
			Log.Error("!lock.isSanity()");
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"ChangeLock\":" + "{\n" +
			super.toInnerJson()  + ",\n"  + 
			lock.toInnerJson() + "\n" +
		"}\n";
	}

	@Override
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		// Parse Lock
		lock = new Lock().Parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Lock
		os.write(lock.getBytes());
		return os;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lock == null) ? 0 : lock.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ChangeLock other = (ChangeLock) obj;
		if (lock == null) {
			if (other.lock != null) {
				return false;
			}
		} else if (!lock.equals(other.lock)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.operation.Operation#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public boolean isValid() throws Exception {
		if(transaction.getChangeLog().getFilter().isLockExists(lock, true) != null) {
			Log.Error("Lock already exists: " + lock);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.operation.Operation#getProofLength()
	 */
	@Override
	public Value getProofLength() {
		return lock.getProofLength();
	}
	
}
