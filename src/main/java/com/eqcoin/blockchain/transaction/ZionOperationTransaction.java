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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.blockchain.transaction.operation.UpdateLockOperation;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 11, 2020
 * @email 10509759@qq.com
 */
public class ZionOperationTransaction extends ZionTransaction {
	private Operation operation;

	public ZionOperationTransaction() {
		super();
		transactionType = TransactionType.ZIONOPERATION;
	}

	public ZionOperationTransaction(byte[] bytes, TransactionShape transactionShape)
			throws Exception {
		super(bytes, transactionShape);
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
		size += operation.getBin(LockShape.AI).length;

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
		size += operation.getBin(LockShape.AI).length;
		return super.getBillingSize();
	}

	@Override
	public boolean isValid()
			throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if(!operation.isMeetPreconditions(this)) {
			Log.Error("Operation " + operation + " doesn't meet preconditions.");
			return false;
		}
		if(!operation.isValid(changeLog)) {
			Log.Error("Operation " + operation + " isn't valid.");
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
		ZionOperationTransaction other = (ZionOperationTransaction) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		return true;
	}
	
	public String toInnerJson() {
		return
		"\"ZionOperationTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n"
				+ operation.toInnerJson() + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"EQCSegWit\":" + eqcSegWit.toInnerJson() + ",\n" + "\"Publickey\":" 
				+ ((compressedPublickey.getCompressedPublickey() == null) ? null : "\"" + Util.getHexString(compressedPublickey.getCompressedPublickey()) + "\"")+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.TRANSFEROPERATION;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isDerivedSanity(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public boolean isDerivedSanity(TransactionShape transactionShape) {
		if(operation == null) {
			return false;
		}
		if(transactionShape == TransactionShape.RPC) {
			if(!operation.isSanity(LockShape.READABLE)) {
				return false;
			}
		}
		else {
			if(!operation.isSanity(LockShape.ID)) {
				return false;
			}
		}
		
		if(operation.getOP() != OP.LOCK && operation.getOP() != OP.CHECKPOINT && operation.getOP() != OP.TXFEERATE) {
			return false;
		}

		if(operation.getOP() == OP.LOCK) {
			if(!super.isDerivedSanity(transactionShape)) {
				return false;
			}
		}
		else {
			if(txOutList.size() !=0) {
				return false;
			}
		}
		
		return true;
	}
	
	public void planting() throws Exception {
		super.planting();
		if(!operation.execute(this)) {
			throw new IllegalStateException("During execute operation error occur: " + operation);
		}
	}
	
	public byte[] getBodyBytes(TransactionShape transactionShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Operation
			if(transactionShape == TransactionShape.RPC) {
				os.write(operation.getBytes(LockShape.READABLE));
			}
			else {
				os.write(operation.getBytes(LockShape.ID));
			}
			// Serialization Super body
			os.write(super.getBodyBytes(transactionShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public void parseBody(ByteArrayInputStream is, TransactionShape transactionShape) throws Exception {
		// Parse Operation
		if(transactionShape == TransactionShape.RPC) {
			operation = Operation.parseOperation(is, LockShape.READABLE);
		}
		else {
			operation = Operation.parseOperation(is, LockShape.ID);
		}
		// Parse Super body
		super.parseBody(is, transactionShape);
	}
	
}
