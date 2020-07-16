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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Savepoint;
import java.util.Comparator;
import java.util.Objects;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.witness.Witness;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public class Transaction extends EQCSerializable implements Comparator<Transaction>, Comparable<Transaction> {
	/**
	 * Header
	 */
	protected TransactionType transactionType;
	/**
	 * Store relevant transaction's lock type and priority or VIP transaction's fee 
	 */
	protected ID status; 
	protected ID nonce;
	/**
	 * Body
	 */
	protected Witness witness;
	protected Operation operation;
	
	/**
	 * Transaction relevant helper variable
	 */
	protected ChangeLog changeLog;
	protected TransactionShape transactionShape;
	protected Value txFeeRate;
	
	// Flag bits
	private final int FLAG_BITS = 128;
	
	public enum TransactionType {
		ZEROZIONCOINBASE, ZIONCOINBASE, TRANSFERCOINBASE, ZION, ZIONOP, TRANSFER, TRANSFEROP, MODERATEOP;
		public static TransactionType get(int ordinal) {
			TransactionType transactionType = null;
			switch (ordinal) {
			case 0:
				transactionType = TransactionType.ZEROZIONCOINBASE;
				break;
			case 1:
				transactionType = TransactionType.ZIONCOINBASE;
				break;
			case 2:
				transactionType = TransactionType.TRANSFERCOINBASE;
				break;
			case 3:
				transactionType = TransactionType.ZION;
				break;
			case 4:
				transactionType = TransactionType.ZIONOP;
				break;
			case 5:
				transactionType = TransactionType.TRANSFER;
				break;
			case 6:
				transactionType = TransactionType.TRANSFEROP;
				break;
			case 7:
				transactionType = TransactionType.MODERATEOP;
				break;
			}
			if(transactionType == null) {
				throw new IllegalStateException("Invalid transaction type: " + ordinal);
			}
			return transactionType;
		}

		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public enum TransactionShape {
		SIGN, RPC, SEED;
		public static TransactionShape get(int ordinal) {
			TransactionShape transactionShape = null;
			switch (ordinal) {
			case 0:
				transactionShape = TransactionShape.SIGN;
				break;
			case 1:
				transactionShape = TransactionShape.RPC;
				break;
			case 2:
				transactionShape = TransactionShape.SEED;
				break;
			}
			if(transactionShape == null) {
				throw new IllegalStateException("Invalid transaction shape: " + ordinal);
			}
			return transactionShape;
		}

		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}

	public enum TRANSACTION_PRIORITY {
		VIP(4), ASAP(3), ASAP10(2), ASAP20(1), ASAP30(0);
		private TRANSACTION_PRIORITY(int priorityRate) {
			this.priorityRate = priorityRate;
		}

		private int priorityRate;

		public int getPriorityRate() {
			return priorityRate;
		}

		public static TRANSACTION_PRIORITY get(int priorityRate) {
			TRANSACTION_PRIORITY transaction_priority = null;
			switch (priorityRate) {
			case 0:
				transaction_priority = TRANSACTION_PRIORITY.ASAP30;
				break;
			case 1:
				transaction_priority = TRANSACTION_PRIORITY.ASAP20;
				break;
			case 2:
				transaction_priority = TRANSACTION_PRIORITY.ASAP10;
				break;
			case 3:
				transaction_priority = TRANSACTION_PRIORITY.ASAP;
				break;
			case 4:
				transaction_priority = TRANSACTION_PRIORITY.VIP;
				break;
			default:
				throw new IllegalStateException("Invalid TRANSACTION_PRIORITY rate: " + priorityRate);
			}
			return transaction_priority;
		}
		
	}

	public void init(ChangeLog changeLog) throws Exception {
		this.changeLog = changeLog;
		txFeeRate = changeLog.getTxFeeRate();
		witness.setTransaction(this);
	}
	
	protected void init() {
		transactionShape = TransactionShape.RPC;
	}
	
	public Transaction() {
		super();
	}
	
	public Transaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/**
	 * @return the witness
	 */
	public Witness getWitness() {
		return witness;
	}

	/**
	 * @param witness the witness to set
	 */
	public void setWitness(Witness witness) {
		this.witness = witness;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 * @throws NoSuchFieldException
	 */
	public void setNonce(ID nonce) {
		this.nonce = nonce;
	}

	@Override
	public int compareTo(Transaction o) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = this.getTransactionType().compareTo(o.getTransactionType())) == 0) {
			try {
				if ((nResult = this.getPriorityValue().compareTo(o.getPriorityValue())) == 0) {
					// 20200525 here need do more job for example compare witness
//					if ((nResult = this.txIn.getPassportId().compareTo(o.getTxIn().getPassportId())) == 0) {
//						nResult = this.nonce.compareTo(o.getNonce());
//					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		if (nResult != 0) {
//			if (nResult < 0) {
//				nResult = 1;
//			} else {
//				nResult = -1;
//			}
//		}
		return nResult;
	}

	@Override
	public int compare(Transaction o1, Transaction o2) {
		// TODO Auto-generated method stub
		int nResult = 0;
		if ((nResult = o1.getTransactionType().compareTo(o2.getTransactionType())) == 0) {
			try {
				if ((nResult = o1.getPriorityValue().compareTo(o2.getPriorityValue())) == 0) {
					// 20200525 here need do more job for example compare witness
//					if ((nResult = o1.txIn.getPassportId().compareTo(o2.getTxIn().getPassportId())) == 0) {
//						nResult = o1.nonce.compareTo(o2.getNonce());
//					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (nResult != 0) {
			if (nResult < 0) {
				nResult = 1;
			} else {
				nResult = -1;
			}
		}
		return nResult;
	}

	public Value getMaxTxFeeLimit() throws Exception {
		return getMaxBillingLength().multiply(txFeeRate).multiply(BigInteger.valueOf(TRANSACTION_PRIORITY.ASAP.getPriorityRate()));
	}

	public Value getMaxBillingLength() throws Exception {
		Value maxBillingLength = null;
		TransactionShape transactionShape = this.transactionShape;
		try {
			this.transactionShape = TransactionShape.SIGN;
			// Add transaction size but without signature size due to at this time doesn't know.
			maxBillingLength = new Value(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
			// Add MAX_TXFEE_LEN due to in txin maybe already include the txfee so here just subtract it's length which is zero by default
			maxBillingLength = maxBillingLength.add(Util.MAX_TXFEE_LEN.subtract(BigInteger.valueOf(status.getEQCBits().length)));
			// Add signature length
			// 20200509 here need do more job to find a better unique and abstract way to
			// calculate the signature length
			// For example if lock is P2SH lock then how to calculate it's signature length? If can implements one util to calculate the length according to lock type and lock?
			maxBillingLength = maxBillingLength.add(witness.getMaxBillingLength());
			// Add proof length
			Value proofLen = getProofLength();
			if(proofLen != null) {
				maxBillingLength = maxBillingLength.add(proofLen);
			}
		} finally {
			this.transactionShape = transactionShape;
		}
		return maxBillingLength;
	}

	public Value getTxFee() throws Exception {
		return calculateTxFee(getBillingLength());
	}
	
	public Value getTxFeeLimit() throws Exception {
		return calculateTxFee(getMaxBillingLength());
	}
	
	private Value calculateTxFee(Value len) throws Exception {
		Value txFee = null;
		TRANSACTION_PRIORITY priority = getPriority() ;
		if (priority == TRANSACTION_PRIORITY.VIP) {
			txFee = new Value(status.shiftRight(8).add(len.multiply(txFeeRate).multiply(BigInteger.ONE.add(BigInteger.valueOf(TRANSACTION_PRIORITY.ASAP.getPriorityRate())))));
		} else {
			txFee = len.multiply(txFeeRate).multiply(BigInteger.ONE.add(BigInteger.valueOf(priority.getPriorityRate())));
		}
		return txFee;
	}

	public TRANSACTION_PRIORITY getPriority() throws Exception {
		TRANSACTION_PRIORITY priority = null;
		byte flag = 0;
		byte[] value = null;
		value = status.toByteArray();
		flag = (byte) (value[value.length - 1] & 0x03);
		if (value.length > 1) {
			priority = TRANSACTION_PRIORITY.VIP;
		} else {
			priority = TRANSACTION_PRIORITY.get(flag);
		}
		return priority;
	}

	public Value getPriorityValue() throws Exception {
		Value rate = null;
		if (status.compareTo(BigInteger.valueOf(FLAG_BITS)) >= 0) {
			rate = new Value(status.shiftRight(8).add(BigInteger.valueOf(TRANSACTION_PRIORITY.VIP.getPriorityRate())));
		} else {
			rate = new Value(status.byteValue() & 0x03); 
		}
		return rate;
	}

	public boolean isBaseValid() throws Exception {
		if(!witness.isValid()) {
			Log.Error("Witness is invalid");
			return false;
		}
		return true;
	}
	
	protected Value getExpenditure() throws Exception {
		return getTxFee();
	}
	
	/**
	 * Check if all TxOut relevant fields is valid for example the TxOutList, Operation,
	 * HelixList etc...
	 * 
	 * @return
	 * @throws Exception
	 */
	protected boolean isDerivedValid() throws Exception {
		return false;
	}
	
	/**
	 * 0. 验证Transaction的完整性： 0.1 对于CoinBase transaction至少包括一个TxOut。 0.2 对于非CoinBase
	 * transaction至少包括一个TxOut&一个TxIn。 1. 验证TxIn余额是否足够？
	 * 之前高度的余额减去当前EQCBlock中之前的交易记录中已经花费的余额。 2. 验证TxIn address是否有效&和公钥是否一致？ 3.
	 * 验证TxIn‘s block header‘s hash+bin(TxIn+TxOut）的签名能否通过？ 4.
	 * 验证TxHash是否在之前的区块中不存在，也即此交易是唯一的交易。防止重放攻击。
	 * 验证Signature在之前的区块中&当前的EQCBlock中不存在。防止重放攻击。 5. 验证TxOut的数量是不是大于等于1&小于等于10。 6.
	 * 验证TxOut的地址是不是唯一的存在？也即每个TxOut地址只能出现一次。 7. 验证TxOut是否小于TxIn。 8.
	 * 验证是否TxFee大于零，验证是否所有的Txin&TxOut的Value大于零。 // 9. 验证TxFee是否足够。
	 * 
	 * @return
	 * @throws RocksDBException
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws Exception
	 */
	public boolean isValid() throws Exception {
		if (!isBaseValid()) {
			Log.Error("Transaction's isBaseValid verify failed");
			return false;
		}
		if (!isDerivedValid()) {
			Log.Error("Transaction's isDerivedValid verify failed");
			return false;
		}
		// Verify if Transaction's signature can pass
		if (!verifySignature()) {
			Log.Error("Transaction's signature verify failed");
			return false;
		}

		return true;
	}

	/**
	 * @return the transactionType
	 */
	public TransactionType getTransactionType() {
		return transactionType;
	}

	public static TransactionType parseTransactionType(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return parseTransactionType(is);
	}
	
	public static TransactionType parseTransactionType(ByteArrayInputStream is) throws Exception {
		TransactionType transactionType = null;
		try {
			is.mark(0);
			transactionType = TransactionType.get(EQCType.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return transactionType;
	}
	
	protected boolean isTransactionTypeSanity() {
		return false;
	}
	
	protected boolean isWitnessSanity() throws Exception {
		if(witness == null) {
			Log.Error("witness == null");
			return false;
		}
		if(!witness.isSanity()) {
			Log.Error("!witness.isSanity()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Transaction Parse(ByteArrayInputStream is) throws Exception {
		Transaction transaction = null;
		TransactionType transactionType = parseTransactionType(is);
		if (transactionType == TransactionType.TRANSFER) {
			transaction = new TransferTransaction();
		} else if (transactionType == TransactionType.TRANSFEROP) {
			transaction = new TransferOPTransaction();
		} else if (transactionType == TransactionType.ZION) {
			transaction = new ZionTransaction();
		} else if (transactionType == TransactionType.ZIONOP) {
			transaction = new ZionOPTransaction();
		} else if (transactionType == TransactionType.MODERATEOP) {
			transaction = new ModerateOPTransaction();
		} else if (transactionType == TransactionType.TRANSFERCOINBASE) {
			transaction = new TransferCoinbaseTransaction();
		} else if (transactionType == TransactionType.ZIONCOINBASE) {
			transaction = new ZionCoinbaseTransaction();
		} else if (transactionType == TransactionType.ZEROZIONCOINBASE) {
			transaction = new ZeroZionCoinbaseTransaction();
		} else {
			throw new IllegalStateException("Bad Transaction format: " + transactionType);
		}
		transaction.setTransactionShape(transactionShape).parse(is);
		return transaction;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public Transaction Parse(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		Transaction transaction = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		transaction = Parse(is);
		EQCType.assertNoRedundantData(is);
		return transaction;
	}

	public boolean verifySignature() throws Exception {
		return witness.verifySignature();
	}

	public byte[] getSignBytesHash() throws Exception {
		byte[] bytes = null;
		TransactionShape originalTransactionShape = transactionShape;
		try {
			transactionShape = TransactionShape.SIGN;
			bytes = MessageDigest.getInstance(Util.SHA3_512).digest(getBytes());
		} 
		finally {
			transactionShape = originalTransactionShape;
		}
		return bytes;
	}

//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((nonce == null) ? 0 : nonce.hashCode());
//		result = prime * result + ((publickey == null) ? 0 : publickey.hashCode());
//		result = prime * result + Arrays.hashCode(signature);
//		result = prime * result + ((transactionType == null) ? 0 : transactionType.hashCode());
//		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
//		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Transaction other = (Transaction) obj;
//		if (nonce == null) {
//			if (other.nonce != null)
//				return false;
//		} else if (!nonce.equals(other.nonce))
//			return false;
//		if (publickey == null) {
//			if (other.publickey != null)
//				return false;
//		} else if (!publickey.equals(other.publickey))
//			return false;
//		if (!Arrays.equals(signature, other.signature))
//			return false;
//		if (transactionType != other.transactionType)
//			return false;
//		if (txIn == null) {
//			if (other.txIn != null)
//				return false;
//		} else if (!txIn.equals(other.txIn))
//			return false;
//		if (txOutList == null) {
//			if (other.txOutList != null)
//				return false;
//		} else if (!txOutList.equals(other.txOutList))
//			return false;
//		return true;
//	}

	protected boolean isMeetPreCondition() throws Exception {
		boolean isSuccessful = false;
		isSuccessful = witness.isMeetPreCondition();
		if(isSuccessful && (transactionShape == TransactionShape.SEED)) {
			nonce = witness.getPassport().getNonce().getNextID();
		}
		return isSuccessful;
	}
	
	public boolean planting() throws Exception {
		boolean isSuccessful = false;
 		Savepoint savepoint = null;
		try {
   			if(isMeetPreCondition() && isSanity() && isValid()) {
				// Begin set save point
				savepoint = changeLog.getFilter().getConnection().setSavepoint();
				derivedPlanting();
				isSuccessful = true;
			}
		} catch (Exception e) {
			Log.Error("During planting error occur: " + e.getMessage() + " savepoint: "+ savepoint);
			if(savepoint != null) {
				changeLog.getFilter().getConnection().rollback(savepoint);
				Log.Error("Savepoint exists already rollbacked");
			}
			throw e;
		} finally {
			if(savepoint != null) {
				changeLog.getFilter().getConnection().releaseSavepoint(savepoint);
			}
			free();
		}
		return isSuccessful;
	}
	
	protected void derivedPlanting() throws Exception {
		witness.planting();
	}
	
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		parseDerivedBody(is);
		witness = new Witness().setTransaction(this).Parse(is);
	}
	
	protected void parseDerivedBody(ByteArrayInputStream is) throws Exception {
		
	}
	
	public Value getBillingValue() throws Exception {
		return null;
	}

	public Value getBillingLength() throws Exception {
		Value billingLength = null;
		// Add transaction length include signature length
		billingLength = new Value(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
		// Add proof length
		billingLength = billingLength.add(getProofLength());
		return billingLength;
	}
	
	protected Value getProofLength() throws Exception {
		return null;
	}
	
	protected boolean isStatusSanity() throws Exception {
		if(status == null) {
			Log.Error("status == null");
			return false;
		}
		if(!status.isSanity()) {
			Log.Error("!status.isSanity()");
			return false;
		}
		if(getPriority() == null) {
			Log.Error("getPriority() == null");
			return false;
		}
		return true;
	}
	
	public boolean isBaseSanity() throws Exception {
		if(transactionType == null) {
			Log.Error("transactionType == null");
			return false;
		}
		if(!isTransactionTypeSanity()) {
			return false;
		}
		if(!isStatusSanity()) {
			return false;
		}
		if(nonce == null) {
			Log.Error("nonce == null");
			return false;
		}
		if(!nonce.isSanity()) {
			Log.Error("!nonce.isSanity()");
			return false;
		}
		if(!isWitnessSanity()) {
			return false;
		}
		return true;
	}
	
	protected boolean isDerivedSanity() throws Exception {
		return false;
	}
	
	public boolean isSanity() throws Exception {
		return isBaseSanity() && isDerivedSanity();
	}
	
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// Parse Transaction type
		transactionType = TransactionType.get(EQCType.parseID(is).intValue());
		// Parse Status
		status = EQCType.parseID(is);
		getType();
		// Parse nonce
		if(transactionShape != TransactionShape.SEED) {
			nonce = EQCType.parseID(is);
		}
	}
	
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
		// Serialization status
		os.write(status.getEQCBits());
		// Serialization nonce
		if(transactionShape != TransactionShape.SEED) {
			os.write(EQCType.bigIntegerToEQCBits(nonce));
		}
		return os;
	}
	
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization derived body bytes
		os.write(getDerivedBodyBytes());
		if (transactionShape != TransactionShape.SIGN) {
			// Serialization Witness
			os.write(witness.getBytes());
		}
		return os;
	}
	
	protected byte[] getDerivedBodyBytes() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}
	
	public String toInnerJson() {
		return null;
	}
	
	public String statusInnerJson() {
		String status = "";
			try {
				status = 
						"\"Status\":" + 
						"\n{" +
						"\"Lock type\":" + getType() + ",\n" +
						"\"Priority\":" + getPriority() + ",\n" +
						"\"TxFeeLimit\":" + "\"" +  Long.toString(getTxFeeLimit().longValue()) + "\"" + "\n" +
						"}";
			} catch (Exception e) {
				Log.Error(e.getMessage());
			}
		return status;
	}
	
	public boolean compare(Transaction transaction) {
		return false;
	}
	
	protected void free() {
		changeLog = null;
//		witness.free();
	}

	/**
	 * @return the changeLog
	 */
	public ChangeLog getChangeLog() {
		return changeLog;
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		if(!(this instanceof TransferOPTransaction || this instanceof ZionOPTransaction || this instanceof ModerateOPTransaction)) {
			throw new IllegalStateException("Only OP Transaction support getOperation but current transaction is: " + transactionType);
		}
		return operation;
	}
	
	public void setOperation(Operation operation) {
		if(!(this instanceof TransferOPTransaction || this instanceof ZionOPTransaction || this instanceof ModerateOPTransaction)) {
			throw new IllegalStateException("Only OP Transaction support getOperation but current transaction is: " + transactionType);
		}
		this.operation = operation;
	}

	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(Value txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	// Maybe here need do more job to find a better way to generate a shorter signature
	public byte[] getProof() {
		return witness.getProof();
	}
	
	public void setPriority(TRANSACTION_PRIORITY priority, LockType lockType, Value txFee) throws Exception {
		Objects.requireNonNull(priority);
		if(priority == TRANSACTION_PRIORITY.VIP) {
			Objects.requireNonNull(txFee);
			status = new ID(txFee.shiftLeft(8).add(BigInteger.valueOf(type.ordinal()).shiftLeft(2)));
		}
		else {
			status = new ID(BigInteger.valueOf(priority.getPriorityRate()).add(BigInteger.valueOf(type.ordinal()).shiftLeft(2)));
		}
	}

	/**
	 * @param transactionShape the transactionShape to set
	 */
	public Transaction setTransactionShape(TransactionShape transactionShape) {
		this.transactionShape = transactionShape;
		return this;
	}

	public LockType getLockType() {
		byte flag = 0;
		byte[] value = null;
		value = status.toByteArray();
		flag = (byte) (value[value.length - 1] >>> 2);
		return LockType.get(flag);
	}

	/**
	 * @return the status
	 */
	public ID getStatus() {
		return status;
	}

}
