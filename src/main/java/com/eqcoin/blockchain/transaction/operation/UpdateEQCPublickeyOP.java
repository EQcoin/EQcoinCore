/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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

import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.lock.T1Publickey;
import com.eqcoin.blockchain.lock.T2Publickey;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Mar 23, 2020
 * @email 10509759@qq.com
 */
@Deprecated
public class UpdateEQCPublickeyOP extends Operation {
	private EQCPublickey eqcPublickey;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		op = OP.PUBLICKEY;
		eqcPublickey = new EQCPublickey();
	}

	public UpdateEQCPublickeyOP() throws Exception {
		super();
	}

	public UpdateEQCPublickeyOP(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public void planting() throws Exception {
		transaction.getTxInLockMate().setEqcPublickey(eqcPublickey);
		transaction.getChangeLog().getFilter().saveLock(transaction.getTxInLockMate());
	}
	
	/**
	 * @return the eQCPublickey
	 */
	public EQCPublickey getEQCPublickey() {
		return eqcPublickey;
	}

	/**
	 * @param eQCPublickey the eQCPublickey to set
	 */
	public void setEQCPublickey(EQCPublickey eQCPublickey) {
		this.eqcPublickey = eQCPublickey;
	}

	@Override
	public boolean isSanity() {
		return (op == OP.PUBLICKEY && eqcPublickey != null && eqcPublickey.isSanity());
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateEQCPublickeyOP\":" + 
		"\n{" +
			eqcPublickey.toInnerJson() + "\n" +
		"}";
	}

	@Override
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		// Parse EQCPublickey
		if(transaction.getTxInLockMate().getLock().getLockType() == LockType.T1) {
			eqcPublickey = new T1Publickey(is);
		}
		else if(transaction.getTxInLockMate().getLock().getLockType() == LockType.T2) {
			eqcPublickey = new T2Publickey(is);
		}
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Lock
			os.write(eqcPublickey.getBytes());
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
		result = prime * result + ((eqcPublickey == null) ? 0 : eqcPublickey.hashCode());
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
		UpdateEQCPublickeyOP other = (UpdateEQCPublickeyOP) obj;
		if (eqcPublickey == null) {
			if (other.eqcPublickey != null) {
				return false;
			}
		} else if (!eqcPublickey.equals(other.eqcPublickey)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() throws Exception {
		if(!transaction.getTxInLockMate().getEqcPublickey().isNULL()) {
			Log.Error("Lock relevant publickey shouldn't exists.");
			return false;
		}
		if(!LockTool.verifyEQCLockAndPublickey(transaction.getTxInLockMate().getLock(), eqcPublickey.getPublickey())) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.operation.Operation#getProofLength()
	 */
	@Override
	public Value getProofLength() {
		return new Value(eqcPublickey.getPublickey().length);
	}
	
}
