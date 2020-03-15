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
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferOperationTransaction;
import com.eqcoin.blockchain.transaction.ZionOperationTransaction;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util.LockTool;

/**
 * @author Xun Wang
 * @date Jun 22, 2019
 * @email 10509759@qq.com
 */
public class UpdateTxFeeRateOperation extends Operation {
	private byte txFeeRate;
	
	public UpdateTxFeeRateOperation() {
		super(OP.TXFEERATE);
	}
	
	public UpdateTxFeeRateOperation(ByteArrayInputStream is, LockShape lockShape) throws NoSuchFieldException, IllegalArgumentException, IOException {
		super(OP.TXFEERATE);
		parseHeader(is, lockShape);
		parseBody(is, lockShape);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBytes(com.eqzip
	 * .eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBytes(LockShape lockShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Header
			os.write(getHeaderBytes(lockShape));
			// Serialization Body
			os.write(getBodyBytes(lockShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#getBin(com.eqzip.
	 * eqcoin.blockchain.Address.AddressShape)
	 */
	@Override
	public byte[] getBin(LockShape lockShape) {
		return EQCType.bytesToBIN(getBytes(lockShape));
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.OperationTransaction.Operation#execute()
	 */
	@Override
	public boolean execute(Transaction transaction) throws Exception {
		EQcoinSeedPassport eQcoinSeedPassport = (EQcoinSeedPassport) transaction.getChangeLog().getFilter().getPassport(ID.ONE, true);
		eQcoinSeedPassport.setTxFeeRate(txFeeRate);
		transaction.getChangeLog().getFilter().savePassport(eQcoinSeedPassport);
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isMeetPreconditions()
	 */
	@Override
	public boolean isMeetPreconditions(Transaction transaction) throws Exception {
		if(!(transaction instanceof ZionOperationTransaction)) {
			return false;
		}
		if(!transaction.getTxIn().getPassportId().equals(ID.TWO)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.transaction.operation.Operation#isSanity(com.eqzip.eqcoin.blockchain.transaction.Address.AddressShape[])
	 */
	@Override
	public boolean isSanity(LockShape lockShape) {
		if(op != OP.TXFEERATE) {
			return false;
		}
		if(txFeeRate < 1 || txFeeRate > 10) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toInnerJson() {
		return 
		"\"UpdateTxFeeRateOperation\":" + 
		"\n{" +
		"\"TxFeeRate\":" + "\"" + txFeeRate + "\""  + 
		"\n}\n";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is, LockShape lockShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// Parse TxFeeRate
		txFeeRate = EQCType.parseBIN(is)[0];
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
	 */
	@Override
	public byte[] getBodyBytes(LockShape lockShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization TxFeeRate
			os.write(EQCType.bytesToBIN(new byte[] {txFeeRate}));
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
		result = prime * result + txFeeRate;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdateTxFeeRateOperation other = (UpdateTxFeeRateOperation) obj;
		if (txFeeRate != other.txFeeRate)
			return false;
		return true;
	}

	/**
	 * @return the txFeeRate
	 */
	public byte getTxFeeRate() {
		return txFeeRate;
	}

	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(byte txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.operation.Operation#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public boolean isValid(ChangeLog changeLog) throws Exception {
		return true;
	}
	
}
