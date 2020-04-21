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
import java.util.Arrays;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.ZionOPTransaction;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Aug 19, 2019
 * @email 10509759@qq.com
 */
public class ChangeCheckPointOP extends Operation {
	private byte[] checkPointHash;
	private ID checkPointHeight;
	
	public ChangeCheckPointOP() throws Exception {
		super();
		op = OP.CHECKPOINT;
	}
	
	public ChangeCheckPointOP(Transaction transaction) throws Exception {
		super(transaction);
		op = OP.CHECKPOINT;
	}

	public ChangeCheckPointOP(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#execute()
	 */
	@Override
	public void planting() throws Exception {
		EQcoinRootPassport eQcoinSeedPassport = (EQcoinRootPassport) transaction.getChangeLog().getFilter().getPassport(ID.ONE, true);
		eQcoinSeedPassport.setCheckPointHash(checkPointHash);
		eQcoinSeedPassport.setCheckPointHeight(checkPointHeight);
		transaction.getChangeLog().getFilter().savePassport(eQcoinSeedPassport);
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isMeetPreconditions()
	 */
	@Override
	public boolean isMeetPreconditions() throws Exception {
		if(!transaction.getTxIn().getPassportId().equals(ID.ONE)) {
			return false;
		}
		if(!(transaction instanceof ZionOPTransaction)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSanity() {
		if(op != OP.CHECKPOINT) {
			return false;
		}
		if(checkPointHash == null || checkPointHash.length != 64) {
			return false;
		}
		if(!checkPointHeight.isSanity()) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateCheckPointOP\":" + 
		"\n{" +
		"\"CheckPointHash\":" + "\"" + Util.dumpBytes(checkPointHash, 16) + "\","  + 
		"\"CheckPointHeight\":" + "\"" + checkPointHeight + "\""  + 
		"\n}\n";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		checkPointHash = EQCType.parseBIN(is);
		checkPointHeight = EQCType.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCType.bytesToBIN(checkPointHash));
		os.write(checkPointHeight.getEQCBits());
		return os;
	}

	/**
	 * @return the checkPointHash
	 */
	public byte[] getCheckPointHash() {
		return checkPointHash;
	}

	/**
	 * @param checkPointHash the checkPointHash to set
	 */
	public void setCheckPointHash(byte[] checkPointHash) {
		this.checkPointHash = checkPointHash;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.operation.Operation#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public boolean isValid() throws Exception {
		if(checkPointHeight.compareTo(transaction.getChangeLog().getHeight()) >=0) {
			return false;
		}
		if (!Arrays.equals(checkPointHash, transaction.getChangeLog().getEQCHeaderHash(checkPointHeight))) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(checkPointHash);
		result = prime * result + ((checkPointHeight == null) ? 0 : checkPointHeight.hashCode());
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
		ChangeCheckPointOP other = (ChangeCheckPointOP) obj;
		if (!Arrays.equals(checkPointHash, other.checkPointHash)) {
			return false;
		}
		if (checkPointHeight == null) {
			if (other.checkPointHeight != null) {
				return false;
			}
		} else if (!checkPointHeight.equals(other.checkPointHeight)) {
			return false;
		}
		return true;
	}
	
}
