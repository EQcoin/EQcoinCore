/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Savepoint;
import java.util.Comparator;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.witness.Witness;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.transaction.txout.ZionTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public class Transaction extends EQCObject implements Comparator<Transaction>, Comparable<Transaction> {
	@Deprecated
	public enum TRANSACTION_PRIORITY {
		VIP(0), ASAP(1);
		public static TRANSACTION_PRIORITY get(final int priorityRate) {
			TRANSACTION_PRIORITY transaction_priority = null;
			switch (priorityRate) {
			case 0:
				transaction_priority = TRANSACTION_PRIORITY.VIP;
				break;
			case 1:
				transaction_priority = TRANSACTION_PRIORITY.ASAP;
				break;
			default:
				throw new IllegalStateException("Invalid TRANSACTION_PRIORITY rate: " + priorityRate);
			}
			return transaction_priority;
		}

		private int priorityRate;

		private TRANSACTION_PRIORITY(final int priorityRate) {
			this.priorityRate = priorityRate;
		}

		public int getPriorityRate() {
			return priorityRate;
		}

	}
	public enum TransactionShape {
		SIGN, RPC;
		public static TransactionShape get(final int ordinal) {
			TransactionShape transactionShape = null;
			switch (ordinal) {
			case 0:
				transactionShape = TransactionShape.SIGN;
				break;
			case 1:
				transactionShape = TransactionShape.RPC;
				break;
			}
			if(transactionShape == null) {
				throw new IllegalStateException("Invalid transaction shape: " + ordinal);
			}
			return transactionShape;
		}

		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	public enum TransactionType {
		// Here need do more job in the new way doesn't need ZEROZIONCOINBASE,
		// ZIONCOINBASE, TRANSFERCOINBASE, ZION, ZIONOP, TRANSFER, TRANSFEROP,
		// MODERATEOP. ZEROZIONCOINBASE can use Transaction2 with none transfer and have
		// zion flag represent
		ZEROZIONCOINBASE, ZIONCOINBASE, TRANSFERCOINBASE, ZION, ZIONOP, TRANSFER, TRANSFEROP, MODERATEOP;
		public static TransactionType get(final int ordinal) {
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
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	public static TransactionType parseTransactionType(final byte[] bytes) throws Exception {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return parseTransactionType(is);
	}
	public static TransactionType parseTransactionType(final ByteArrayInputStream is) throws Exception {
		TransactionType transactionType = null;
		try {
			is.mark(0);
			transactionType = TransactionType.get(EQCCastle.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return transactionType;
	}
	/**
	 * Header
	 */
	protected ID status;
	protected ID passportID;
	protected ID nonce;

	@Deprecated
	protected TRANSACTION_PRIORITY priority;

	/**
	 * Body maybe include:
	 * [ZionTxOut]
	 * [TransferTxOut]
	 * [OperationTxOut]
	 */
	protected Value vipTxFee;

	protected Vector<ZionTxOut> zionTxOutList;

	protected  int maxZionTxOutNumbers = 255;

	protected Vector<TransferTxOut> transferTxOutQuantumList;

	protected  int maxTransferTxOutQuantumNumbers = 7;

	protected Vector<TransferTxOut> transferTxOutList;

	protected  int maxTransferTxOutNumbers = 37;

	// Here need change to operation vector
	protected Operation operation;
	protected Vector<Operation> operationTxOutList;

	protected Witness witness;
	protected LockType lockType;



	/**
	 * Transaction relevant helper variable
	 */
	protected EQCHive eqcHive;

	protected Value txFeeRate;

	protected TransactionShape transactionShape;

	// Flag bits
	private final byte FLAG_BITS = (byte) 128;

	public Transaction() {
		super();
	}

	public Transaction(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	private Value calculateTxFee(final Value len) throws Exception {
		Value txFee = null;
		if (priority == TRANSACTION_PRIORITY.VIP) {
			txFee = vipTxFee.add(len.multiply(getTxFeeRate()));
		} else {
			txFee = len.multiply(getTxFeeRate());
		}
		return txFee;
	}

	public boolean compare(final Transaction transaction) {
		return false;
	}

	@Override
	public int compare(final Transaction o1, final Transaction o2) {
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
			} catch (final Exception e) {
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

	@Override
	public int compareTo(final Transaction o) {
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
			} catch (final Exception e) {
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

	protected void derivedPlanting() throws Exception {
		witness.planting();
	}

	protected void free() {
		eqcHive = null;
		//		witness.free();
	}

	public Value getBillingLength() throws Exception {
		Value billingLength = null;
		// Add transaction length include signature length
		billingLength = new Value(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
		// Add proof length
		billingLength = billingLength.add(getGlobalStateLength());
		return billingLength;
	}

	public Value getBillingValue() throws Exception {
		return null;
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
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

	public EQCHive getEQCHive() {
		return eqcHive;
	}

	//	public TRANSACTION_PRIORITY getPriority() throws Exception {
	//		TRANSACTION_PRIORITY priority = null;
	//		byte flag = 0;
	//		byte[] value = null;
	//		value = status.toByteArray();
	//		flag = (byte) (value[value.length - 1] & 0x03);
	//		if (value.length > 1) {
	//			priority = TRANSACTION_PRIORITY.VIP;
	//		} else {
	//			priority = TRANSACTION_PRIORITY.get(flag);
	//		}
	//		return priority;
	//	}

	protected Value getExpenditure() throws Exception {
		return getTxFee();
	}

	protected Value getGlobalStateLength() throws Exception {
		return null;
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
		// Serialization status
		byte status = (byte) lockType.ordinal();
		if(priority == TRANSACTION_PRIORITY.ASAP) {
			status &= FLAG_BITS;
		}
		os.write(status);
		if(priority == TRANSACTION_PRIORITY.VIP) {
			os.write(txFeeRate.getEQCBits());
		}
		// Serialization nonce
		os.write(EQCCastle.bigIntegerToEQCBits(nonce));
		return os;
	}

	public LockType getLockType() {
		return lockType;
	}

	public Value getMaxBillingLength() throws Exception {
		Value maxBillingLength = null;
		final TransactionShape transactionShape = this.transactionShape;
		try {
			this.transactionShape = TransactionShape.SIGN;
			// Add transaction size but without signature size due to at this time doesn't know.
			maxBillingLength = new Value(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
			//			// Add MAX_TXFEE_LEN due to in txin maybe already include the txfee so here just subtract it's length which is zero by default
			//			maxBillingLength = maxBillingLength.add(Util.MAX_TXFEE_LEN.subtract(BigInteger.valueOf(status.getEQCBits().length)));
			// Add signature length
			// 20200509 here need do more job to find a better unique and abstract way to
			// calculate the signature length
			// For example if lock is P2SH lock then how to calculate it's signature length? If can implements one util to calculate the length according to lock type and lock?
			maxBillingLength = maxBillingLength.add(witness.getMaxBillingLength());
			// Add proof length
			final Value proofLen = getGlobalStateLength();
			if(proofLen != null) {
				maxBillingLength = maxBillingLength.add(proofLen);
			}
		} finally {
			this.transactionShape = transactionShape;
		}
		return maxBillingLength;
	}

	public Value getMaxTxFeeLimit() throws Exception {
		return getMaxBillingLength().multiply(eqcHive.getTxFeeRate()).multiply(BigInteger.valueOf(TRANSACTION_PRIORITY.ASAP.getPriorityRate()));
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
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

	public TRANSACTION_PRIORITY getPriority() {
		return priority;
	}

	public ID getPriorityValue() throws Exception {
		ID value = ID.ZERO;
		if(priority == TRANSACTION_PRIORITY.VIP) {
			value = new ID(vipTxFee);
		}
		return value;
	}

	// Maybe here need do more job to find a better way to generate a shorter signature
	public byte[] getProof() {
		return witness.getProof();
	}

	public byte[] getSignBytesHash() throws Exception {
		byte[] bytes = null;
		final TransactionShape originalTransactionShape = transactionShape;
		try {
			transactionShape = TransactionShape.SIGN;
			bytes = MessageDigest.getInstance(Util.SHA3_512).digest(getBytes());
		}
		finally {
			transactionShape = originalTransactionShape;
		}
		return bytes;
	}

	public Value getTxFee() throws Exception {
		return calculateTxFee(getBillingLength());
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

	public Value getTxFeeLimit() throws Exception {
		return calculateTxFee(getMaxBillingLength());
	}

	public Value getTxFeeRate() throws Exception {
		Value txFeeRate = null;
		if(this.txFeeRate != null) {
			txFeeRate = this.txFeeRate;
		}
		else if(eqcHive != null){
			txFeeRate = eqcHive.getTxFeeRate();
		}
		else {
			throw new IllegalStateException("Doesn't init txfee rate");
		}
		return txFeeRate;
	}

	/**
	 * @return the witness
	 */
	public Witness getWitness() {
		return witness;
	}

	@Override
	protected void init() {
		transactionShape = TransactionShape.RPC;
	}

	public void init(final EQCHive eqcHive) throws Exception {
		this.eqcHive = eqcHive;
		witness.setTransaction(this);
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

	public boolean isBaseValid() throws Exception {
		if(!witness.isValid()) {
			Log.Error("Witness is invalid");
			return false;
		}
		return true;
	}

	protected boolean isDerivedSanity() throws Exception {
		return false;
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

	protected boolean isMeetPreCondition() throws Exception {
		return witness.isMeetPreCondition();
	}

	@Override
	public boolean isSanity() throws Exception {
		return isBaseSanity() && isDerivedSanity();
	}

	protected boolean isStatusSanity() throws Exception {
		if(priority == null) {
			Log.Error("priority == null");
			return false;
		}
		if(lockType == null) {
			Log.Error("lockType == null");
			return false;
		}
		if(priority == TRANSACTION_PRIORITY.VIP) {
			if(vipTxFee == null) {
				Log.Error("vipTxFee == null");
				return false;
			}
			if(!vipTxFee.isSanity()) {
				Log.Error("!vipTxFee.isSanity()");
				return false;
			}
		}
		return true;
	}

	protected boolean isTransactionTypeSanity() {
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
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public Transaction Parse(final byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		Transaction transaction = null;
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		transaction = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		return transaction;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Transaction Parse(final ByteArrayInputStream is) throws Exception {
		Transaction transaction = null;
		final TransactionType transactionType = parseTransactionType(is);
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

	@Override
	public void parseBody(final ByteArrayInputStream is)
			throws Exception {
		parseDerivedBody(is);
		witness = new Witness().setTransaction(this).Parse(is);
	}

	protected void parseDerivedBody(final ByteArrayInputStream is) throws Exception {

	}

	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		// Parse Transaction type
		transactionType = TransactionType.get(EQCCastle.parseID(is).intValue());
		// Parse Status
		final byte status = EQCCastle.parseNBytes(is, 1)[0];
		priority = TRANSACTION_PRIORITY.get(status >>> 7);
		lockType = LockType.get(status & FLAG_BITS);
		if(priority == TRANSACTION_PRIORITY.VIP) {
			vipTxFee = EQCCastle.parseValue(is);
		}
		// Parse nonce
		nonce = EQCCastle.parseID(is);
	}

	public boolean planting() throws Exception {
		boolean isSuccessful = false;
		Savepoint savepoint = null;
		try {
			if(isMeetPreCondition() && isSanity() && isValid()) {
				// Begin set save point
				savepoint = eqcHive.getGlobalState().setSavepoint();
				derivedPlanting();
				isSuccessful = true;
			}
		} catch (final Exception e) {
			Log.Error("During planting error occur: " + e.getMessage() + " savepoint: "+ savepoint);
			if(savepoint != null) {
				eqcHive.getGlobalState().rollback(savepoint);
				Log.info("Savepoint exists and the transaction successful rollbacked");
			}
			throw e;
		} finally {
			if(savepoint != null) {
				eqcHive.getGlobalState().releaseSavepoint(savepoint);
			}
			free();
		}
		return isSuccessful;
	}

	public void setLockType(final LockType lockType) {
		this.lockType = lockType;
	}

	/**
	 * @param nonce the nonce to set
	 * @throws NoSuchFieldException
	 */
	public void setNonce(final ID nonce) {
		this.nonce = nonce;
	}

	public void setOperation(final Operation operation) {
		if(!(this instanceof TransferOPTransaction || this instanceof ZionOPTransaction || this instanceof ModerateOPTransaction)) {
			throw new IllegalStateException("Only OP Transaction support getOperation but current transaction is: " + transactionType);
		}
		this.operation = operation;
	}

	//	public void setPriority(TRANSACTION_PRIORITY priority, LockType lockType, Value txFee) throws Exception {
	//		Objects.requireNonNull(priority);
	//		if(priority == TRANSACTION_PRIORITY.VIP) {
	//			Objects.requireNonNull(txFee);
	//			status = new ID(txFee.shiftLeft(8).add(BigInteger.valueOf(lockType.ordinal()).shiftLeft(2)));
	//		}
	//		else {
	//			status = new ID(BigInteger.valueOf(priority.getPriorityRate()).add(BigInteger.valueOf(lockType.ordinal()).shiftLeft(2)));
	//		}
	//	}

	public void setPriority(final TRANSACTION_PRIORITY priority) {
		this.priority = priority;
	}

	//	public LockType getLockType() {
	//		byte flag = 0;
	//		byte[] value = null;
	//		value = status.toByteArray();
	//		flag = (byte) (value[value.length - 1] >>> 2);
	//		return LockType.get(flag);
	//	}
	//
	//	/**
	//	 * @return the status
	//	 */
	//	public ID getStatus() {
	//		return status;
	//	}

	/**
	 * @param transactionShape the transactionShape to set
	 */
	public Transaction setTransactionShape(final TransactionShape transactionShape) {
		this.transactionShape = transactionShape;
		return this;
	}

	public void setTxFeeRate(final Value txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	/**
	 * @param witness the witness to set
	 */
	public void setWitness(final Witness witness) {
		this.witness = witness;
	}

	public String statusInnerJson() {
		String status = "";
		try {
			status =
					"\"Status\":" +
							"\n{" +
							"\"Priority\":" + priority + ",\n" +
							"\"Lock type\":" + lockType + ",\n" +
							"\"TxFeeLimit\":" + "\"" +  Long.toString(getTxFeeLimit().longValue()) + "\"" + "\n" +
							"}";
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}
		return status;
	}

	@Override
	public String toInnerJson() {
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

	public boolean verifySignature() throws Exception {
		return witness.verifySignature();
	}

}
