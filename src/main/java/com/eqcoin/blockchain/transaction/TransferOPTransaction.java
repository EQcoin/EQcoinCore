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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.blockchain.transaction.operation.UpdateLockOP;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
/**
 * @author Xun Wang
 * @date Mar 25, 2019
 * @email 10509759@qq.com
 */
public class TransferOPTransaction extends TransferTransaction {
	protected final static int MIN_TXOUT = 0;
	private Operation operation;

	public TransferOPTransaction() {
		super();
		transactionType = TransactionType.TRANSFEROP;
	}

	public TransferOPTransaction(byte[] bytes)
			throws Exception {
		super(bytes);
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getMaxBillingSize()
	 */
	@Override
	public int getMaxBillingLength() {
		int size = 0;

		// TransferTransaction size
		size += super.getMaxBillingLength();

		// Operations size
		size += operation.getBin().length;

		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.Transaction#getBillingSize()
	 */
	@Override
	public int getBillingSize() throws Exception {
		int size = 0;
		// TransferTransaction size
		size += super.getBillingSize();

		// Operations size
		size += operation.getBin().length;
		return super.getBillingSize();
	}

	@Override
	public boolean isValid()
			throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if(!operation.isMeetPreconditions()) {
			Log.Error("Operation " + operation + " doesn't meet preconditions.");
			return false;
		}
		if(!operation.isValid()) {
			Log.Error("Operation " + operation + " doesn't valid.");
			return false;
		}
		if(!super.isValid()) {
			return false;
		}
		return true;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see com.eqzip.eqcoin.blockchain.Transaction#isTxOutNumberValid()
//	 */
//	@Override
//	public boolean isTxOutNumberValid() {
//		return (txOutList.size() >= MIN_TXOUT) && (txOutList.size() <= MAX_TXOUT);
//	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
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
		TransferOPTransaction other = (TransferOPTransaction) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		return true;
	}
	
	public String toInnerJson() {
		return
		"\"TransferOperationTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n"
				+ operation.toInnerJson() + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"EQCSegWit\":" + eqcWitness.toInnerJson()
				+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.TRANSFEROP;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isDerivedSanity(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public boolean isDerivedSanity() {
		if(!super.isDerivedSanity()) {
			return false;
		}
		if(operation == null) {
			return false;
		}
		if (!(operation instanceof UpdateLockOP)) {
			return false;
		}
		if (!operation.isSanity()) {
			return false;
		}
		return true;
	}

//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.transaction.Transaction#preparePlanting()
//	 */
//	@Override
//	public void preparePlanting() throws Exception {
////		if(transactionShape == TransactionShape.RPC) {
////			super.preparePlanting();
////		}
////		else {
////			UpdateLockOP updateLockOperation = (UpdateLockOP) operation;
////			Lock lock = null;
////			lock = eQcoinSeed.getNextLock();
////			Objects.requireNonNull(lock);
////			updateLockOperation.setLock(lock);
////		}
//	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		super.derivedPlanting();
		operation.execute();
	}

	protected byte[] getDerivedBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(operation.getBytes());
			// Serialization Super body
			os.write(super.getBodyBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse Operation
		operation = Operation.parseOperation(is);
		// Parse Super body
		super.parseBody(is);
	}
	
	public void parseHeader(ByteArrayInputStream is)
			throws Exception {
		parseSoloAndTransactionType(is);
		parseNonce(is);
		parseTxIn(is);
	}

	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			serializeSoloAndTransactionTypeBytes(os);
			serializeNonce(os);
			serializeTxInBytes(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
}
