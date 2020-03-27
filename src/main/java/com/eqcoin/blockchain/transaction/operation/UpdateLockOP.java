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
package com.eqcoin.blockchain.transaction.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class UpdateLockOP extends Operation {
	private Lock lock;
	
	public UpdateLockOP() {
		op = OP.LOCK;
	}

	public UpdateLockOP(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public void execute() throws Exception {
		Passport passport = transaction.getChangeLog().getFilter().getPassport(transaction.getTxIn().getLock().getId(), false);
		Lock lock = new Lock();
		lock.setId(transaction.getChangeLog().getNextLockId());
		lock.setPassportId(passport.getId());
		lock.setReadableLock(lock.getReadableLock());
		passport.setLockID(lock.getId());
		transaction.getChangeLog().getFilter().saveLock(lock);
		transaction.getChangeLog().getFilter().savePassport(passport);
		transaction.getChangeLog().increaseTotalNewUpdatedLockNumbers();
	}

	/**
	 * @return the Lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @param Lock the Lock to set
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	@Override
	public boolean isSanity() {
		if(op != OP.LOCK || lock == null) {
			return false;
		}
		if(!lock.isSanity(LockShape.AI)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateLockOperation\":" + 
		"\n{" +
			lock.toInnerJson() + "\n" +
		"}";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// Parse Lock
		// here need do more job to support AI
		lock = new Lock(EQCType.bytesToASCIISting(EQCType.parseBIN(is)));
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Lock
			os.write(lock.getBin());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
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
		UpdateLockOP other = (UpdateLockOP) obj;
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
		return lock.isSanity(LockShape.AI) && (transaction.getChangeLog().getFilter().getLock(lock.getReadableLock(), true) == null);
	}
	
}
