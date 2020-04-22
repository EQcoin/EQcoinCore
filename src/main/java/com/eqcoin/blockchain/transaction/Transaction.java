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
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TRANSACTION_PRIORITY;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.blockchain.transaction.operation.ChangeLockOP;
import com.eqcoin.crypto.EQCECCPublicKey;
import com.eqcoin.crypto.RecoverySECP256R1Publickey;
import com.eqcoin.crypto.RecoverySECP521R1Publickey;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.persistence.EQCBlockChain;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

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
	 * Body
	 */
	protected TxIn txIn;
	protected ID nonce;
	protected EQCWitness eqcWitness;
	protected Operation operation;
	
	/**
	 * Transaction relevant helper variable
	 */
	protected ChangeLog changeLog;
	protected Passport txInPassport;
	protected EQCLockMate txInLockMate;
	protected TransactionShape transactionShape;
	protected Value txFeeRate;
	protected LockType lockType;
	
	// Flag bits
	private final int FLAG_BITS = 256;
	
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
			return transactionShape;
		}

		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}

	public enum TRANSACTION_PRIORITY {
		VIP(5), ASAP(4), ASAP10(3), ASAP20(2), ASAP30(1);
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
			case 1:
				transaction_priority = TRANSACTION_PRIORITY.ASAP30;
				break;
			case 2:
				transaction_priority = TRANSACTION_PRIORITY.ASAP20;
				break;
			case 3:
				transaction_priority = TRANSACTION_PRIORITY.ASAP10;
				break;
			case 4:
				transaction_priority = TRANSACTION_PRIORITY.ASAP;
				break;
			case 5:
				transaction_priority = TRANSACTION_PRIORITY.VIP;
				break;
			default:
				throw new IllegalStateException("Invalid TRANSACTION_PRIORITY: " + transaction_priority);
			}
			return transaction_priority;
		}
		
	}

	public void init(ChangeLog changeLog) throws Exception {
		this.changeLog = changeLog;
		txFeeRate = changeLog.getTxFeeRate();
	}
	
	protected void init() {
		transactionShape = TransactionShape.SEED;
	}
	
	public Transaction() {
		super();
	}
	
	public Transaction(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public Transaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/**
	 * @return the txIn
	 */
	public TxIn getTxIn() {
		return txIn;
	}

	/**
	 * @param txIn the txIn to set
	 */
	public void setTxIn(TxIn txIn) {
		this.txIn = txIn;
	}

	/**
	 * @return the eqcWitness
	 */
	public EQCWitness getEqcWitness() {
		return eqcWitness;
	}

	/**
	 * @param eqcWitness the eqcWitness to set
	 */
	public void setEqcWitness(EQCWitness eqcWitness) {
		this.eqcWitness = eqcWitness;
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
					if ((nResult = this.txIn.getPassportId().compareTo(o.getTxIn().getPassportId())) == 0) {
						nResult = this.nonce.compareTo(o.getNonce());
					}
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
					if ((nResult = o1.txIn.getPassportId().compareTo(o2.getTxIn().getPassportId())) == 0) {
						nResult = o1.nonce.compareTo(o2.getNonce());
					}
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
		Value maxBillingLength = Value.ZERO;
		transactionShape = TransactionShape.SIGN;
		// Add transaction size but without signature size due to at this time doesn't know.
		maxBillingLength = maxBillingLength.add(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
		// Add MAX_TXFEE_LEN due to in txin maybe already include the txfee so here just subtract it's length which is zero by default
		maxBillingLength = maxBillingLength.add(Util.MAX_TXFEE_LEN.subtract(BigInteger.valueOf(txIn.getValue().getEQCBits().length)));
		// Add max signature length
		if(lockType == lockType.T1) {
			maxBillingLength = maxBillingLength.add(Util.P256_MAX_SIGNATURE_LEN);
		}
		else if(lockType == LockType.T2) {
			maxBillingLength = maxBillingLength.add(Util.P521_MAX_SIGNATURE_LEN);
		}
		else {
			throw new IllegalStateException("Wrong lock type.");
		}
		// Add proof length
		maxBillingLength = maxBillingLength.add(getProofLength());
		transactionShape = TransactionShape.SEED;
		return maxBillingLength;
	}

	public Value getTxFee() throws Exception {
		Value txFee = null;
		TRANSACTION_PRIORITY priority = getPriority() ;
		if (priority == TRANSACTION_PRIORITY.VIP) {
			txFee = txIn.getValue().subtract(BigInteger.valueOf(FLAG_BITS));
		} else {
			txFee = getBillingLength().multiply(txFeeRate).multiply(BigInteger.valueOf(priority.getPriorityRate()));
		}
		return txFee;
	}
	
	public Value getTxFeeLimit() throws Exception {
		return getMaxBillingLength().multiply(txFeeRate).multiply(BigInteger.valueOf(getPriority().getPriorityRate()));
	}

	public TRANSACTION_PRIORITY getPriority() throws Exception {
		int rate = 1;
		if (txIn.getValue().compareTo(getMaxTxFeeLimit()) > 0) {
			rate = TRANSACTION_PRIORITY.VIP.getPriorityRate();
		} else {
			rate = (txIn.value.intValue()>=4)?(txIn.value.intValue()-4):txIn.value.intValue();
		}
		return TRANSACTION_PRIORITY.get(rate);
	}

	public Value getPriorityValue() throws Exception {
		Value rate = null;
		if (txIn.getValue().compareTo(getMaxTxFeeLimit()) > 0) {
			rate = txIn.value.subtract(getMaxTxFeeLimit()).add(BigInteger.valueOf(TRANSACTION_PRIORITY.VIP.getPriorityRate()));
		} else {
			rate = new Value(txIn.getValue().intValue()); 
		}
		return rate;
	}

	public boolean isBaseValid() throws Exception {
		// Check if TxIn's passport id is less than previous EQCHive's total passport numbers
		if(txIn.getPassportId().compareTo(changeLog.getPreviousTotalPassportNumbers()) >= 0) {
			return false;
		}
		
		// Check if Nonce is correct
		if (!nonce.isNextID(txInPassport.getNonce())) {
			Log.Error("Nonce doesn't correct, current: " + nonce + " expect: " + txInPassport.getNonce().getNextID());
			return false;
		}

		// Try to recovery publickey from signature
		if (txInLockMate.getEqcPublickey().isNULL()) {
			byte[] publickey = null;
			if(lockType == LockType.T1) {
				publickey = RecoverySECP256R1Publickey.getInstance().recoveryPublickey(this);
			}
			else if(lockType == LockType.T2) {
				publickey = RecoverySECP521R1Publickey.getInstance().recoveryPublickey(this);
			}
			if(publickey == null) {
				Log.Error("TxIn id: " + txIn.getPassportId() + " relevant lock's publickey recovery failed");
				return false;
			}
			else {
				Log.info("TxIn id: " + txIn.getPassportId() + " relevant lock's publickey doesn't exists and recovery successful: " + Util.bytesToHexString(publickey));
			}
			txInLockMate.getEqcPublickey().setNew();
			txInLockMate.getEqcPublickey().setPublickey(publickey);
		}
		
		// Here exists one bug need do more job
		// Check balance from current Passport
		if (txIn.getValue().add(Util.MIN_EQC).compareTo(txInPassport.getBalance()) > 0) {
			Log.Error("Balance isn't enough");
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
		is.mark(0);
		transactionType = TransactionType.get(EQCType.parseID(is).intValue());
		is.reset();
		if (transactionType == null) {
			throw new NullPointerException("Bad transaction format");
		}
		return transactionType;
	}
	
	protected boolean isTransactionTypeSanity() {
		return false;
	}
	
	protected boolean isEQCWitnessSanity() {
		return eqcWitness != null && eqcWitness.isSanity();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		Transaction transaction = null;
		TransactionType transactionType = parseTransactionType(is);
		if (transactionType == TransactionType.TRANSFER) {
			transaction = new TransferTransaction(is);
		} else if (transactionType == TransactionType.TRANSFEROP) {
			transaction = new TransferOPTransaction(is);
		} else if (transactionType == TransactionType.ZION) {
			transaction = new ZionTransaction(is);
		} else if (transactionType == TransactionType.ZIONOP) {
			transaction = new ZionOPTransaction(is);
		} else if (transactionType == TransactionType.MODERATEOP) {
			transaction = new ModerateOPTransaction(is);
		} else if (transactionType == TransactionType.TRANSFERCOINBASE) {
			transaction = new TransferCoinbaseTransaction(is);
		} else if (transactionType == TransactionType.ZIONCOINBASE) {
			transaction = new ZionCoinbaseTransaction(is);
		} else if (transactionType == TransactionType.ZEROZIONCOINBASE) {
			transaction = new ZeroZionCoinbaseTransaction(is);
		} else {
			throw new IllegalStateException("Bad Transaction format: " + transactionType);
		}
		return (T) transaction;
	}
	
	public boolean verifySignature() throws ClassNotFoundException, SQLException, Exception {
		boolean isTransactionValid = false;
		Signature signature = null;
		// Verify Signature
		try {
			signature = Signature.getInstance("NONEwithECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (lockType == LockType.T1) {
				eccType = ECCTYPE.P256;
			} else if (lockType == LockType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(eccType);
			// Create EQPublicKey according to compressed Publickey
			eqcPublicKey.setECPoint(txInLockMate.getEqcPublickey().getPublickey());
			signature.initVerify(eqcPublicKey);
			transactionShape = TransactionShape.SIGN;
//			Log.info("\nPublickey: " + Util.dumpBytesLittleEndianHex(txInLockMate.getEqcPublickey().getPublickey()));
//			Log.info("\nMessageLen: " + getBytes().length + "\nMessageBytes: " + Util.dumpBytesLittleEndianHex(getBytes()));
//			Log.info("\nMessage Hash: " + Util.dumpBytesLittleEndianHex(MessageDigest.getInstance(Util.SHA3_512).digest(getBytes())));
			signature.update(MessageDigest.getInstance(Util.SHA3_512).digest(getBytes()));
			isTransactionValid = signature.verify(eqcWitness.getDERSignature());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		finally {
			transactionShape = TransactionShape.SEED;
		}
		return isTransactionValid;
	}

	public void sign(Signature ecdsa) throws ClassNotFoundException, SQLException, Exception {
		ecdsa.update(getSignBytesHash());
		if(lockType == LockType.T1) {
			eqcWitness = new T1Witness();
		} else if(lockType == LockType.T2) {
			eqcWitness = new T2Witness();
		}
		eqcWitness.setDERSignature(ecdsa.sign());
	}
	
	public byte[] getSignBytesHash() throws Exception {
		byte[] bytes = null;
		try {
			transactionShape = TransactionShape.SIGN;
			bytes = MessageDigest.getInstance(Util.SHA3_512).digest(getBytes());
		} 
		finally {
			transactionShape = TransactionShape.SEED;
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

	protected void initPlanting() throws Exception {
		txInPassport = changeLog.getFilter().getPassport(txIn.getPassportId(), true);
		txInLockMate = changeLog.getFilter().getLock(txInPassport.getLockID(), true);
		lockType = txInLockMate.getLock().getLockType();
		if(txInPassport == null || txInLockMate == null) {
			throw new IllegalStateException("TxIn's relevant Lock and Passport shouldn't null.");
		}
	}
	
	public boolean planting() throws Exception {
		boolean isSanityAndValid = false;
		try {
			// Begin Transaction
			changeLog.getFilter().getConnection().setAutoCommit(false);
			initPlanting();
			if(isSanity() && isValid()) {
 				isSanityAndValid = true;
				derivedPlanting();
				changeLog.getFilter().getConnection().commit();
			}
		} catch (Exception e) {
			Log.Error("During planting error occur: " + e.getMessage());
			changeLog.getFilter().getConnection().rollback();
			throw e;
		} finally {
			// End Transaction
			changeLog.getFilter().getConnection().setAutoCommit(true);
			free();
		}
		return isSanityAndValid;
	}
	
	protected void derivedPlanting() throws Exception {
		// Update publickey if need
		if(txInLockMate.getEqcPublickey().isNew()) {
			changeLog.getFilter().saveLock(txInLockMate);
		}
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		txInPassport.increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		txInPassport.withdraw(getBillingValue());
		// Here exists one bug
		txInPassport.setUpdateHeight(changeLog.getHeight());
		changeLog.getFilter().savePassport(txInPassport);
		// Deposit TxFee
		Value txFee = new Value(getTxFee());
		Value minerTxFee = txFee.multiply(Value.valueOf(5)).divide(Value.valueOf(100));
		Value eqCoinFederalTxFee = txFee.subtract(minerTxFee);
		Passport eqCoinFederal = changeLog.getFilter().getPassport(ID.ZERO, true);
		Passport minerPassport = null;
		if(changeLog.getCoinbaseTransaction() instanceof TransferCoinbaseTransaction) {
			TransferCoinbaseTransaction transferCoinbaseTransaction = (TransferCoinbaseTransaction) changeLog.getCoinbaseTransaction();
			minerPassport = changeLog.getFilter().getPassport(transferCoinbaseTransaction.getEqCoinMinerTxOut().getPassportId(), true);
		}
		else {
			ZionCoinbaseTransaction zionCoinbaseTransaction = (ZionCoinbaseTransaction) changeLog.getCoinbaseTransaction();
			ID minerLockId = changeLog.getFilter().isLockExists(zionCoinbaseTransaction.getEqCoinMinerTxOut().getLock(), true);
			EQCLockMate minerLock = changeLog.getFilter().getLock(minerLockId, true);
			minerPassport = changeLog.getFilter().getPassport(minerLock.getPassportId(), true);
		}
		eqCoinFederal.deposit(eqCoinFederalTxFee);
		eqCoinFederal.setUpdateHeight(changeLog.getHeight());
		minerPassport.deposit(minerTxFee);
		changeLog.getFilter().savePassport(eqCoinFederal);
		changeLog.getFilter().savePassport(minerPassport);
	}
	
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		parseDerivedBody(is);
		if(lockType == LockType.T1) {
			eqcWitness = new T1Witness(is);
		}
		else if(lockType == LockType.T2) {
			eqcWitness = new T2Witness(is);
		}
	}
	
	protected void parseDerivedBody(ByteArrayInputStream is) throws Exception {
		
	}
	
	public Value getBillingValue() throws Exception {
		return Value.ZERO;
	}

	public Value getBillingLength() throws Exception {
		Value billingLength = Value.ZERO;
		// Add transaction length include signature length
		billingLength = billingLength.add(BigInteger.valueOf(getBytes().length)).add(Util.MAX_TXFEE_LEN);
		// Add proof length
		billingLength = billingLength.add(getProofLength());
		return billingLength;
	}
	
	protected Value getProofLength() throws Exception {
		return Value.ZERO;
	}
	
	protected boolean isTxInSanity() throws Exception {
		return txIn != null && txIn.isSanity() && getPriority() != null;
	}
	
	public boolean isBaseSanity() throws Exception {
		return (transactionType != null && isTransactionTypeSanity() && isTxInSanity() && nonce != null && nonce.isSanity() && isEQCWitnessSanity());
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
		// Parse TxIn
		txIn = new TxIn(is);
		if (txIn.getValue().compareTo(BigInteger.valueOf(FLAG_BITS)) >= 0) {
			byte[] value = txIn.getValue().toByteArray();
			byte flag = value[value.length - 1];
			if (flag == 0) {
				lockType = LockType.T1;
			} else if (flag == 1) {
				lockType = LockType.T2;
			} else {
				throw new IllegalStateException("Invalid lock type: " + flag);
			}
		} else {
			if (txIn.getValue().compareTo(BigInteger.valueOf(8)) > 0) {
				throw new IllegalStateException("Invalid TxIn value: " + txIn);
			} else if (txIn.getValue().compareTo(BigInteger.valueOf(4)) >= 0) {
				lockType = LockType.T2;
			} else {
				lockType = LockType.T1;
			}
		}
		// Parse nonce
		nonce = EQCType.parseID(is);
	}
	
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
		// Serialization TxIn
		os.write(txIn.getBytes());
		// Serialization nonce
		os.write(EQCType.bigIntegerToEQCBits(nonce));
		return os;
	}
	
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization derived body bytes
		os.write(getDerivedBodyBytes());
		if (transactionShape == TransactionShape.SEED) {
			// Serialization Witness
			os.write(eqcWitness.getBytes());
		}
		return os;
	}
	
	protected byte[] getDerivedBodyBytes() throws Exception {
		return null;
	}

	public Nest getNest() {
		Nest nest = new Nest();
		nest.setId(txIn.getPassportId());
		return nest;
	}
	
	public O getO() throws Exception {
		return new O(ByteBuffer.wrap(getBytes()));
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
	
	public boolean compare(Transaction transaction) {
		return false;
	}
	
	protected void free() {
		txInPassport = null;
		txInLockMate = null;
		changeLog = null;
	}

	/**
	 * @return the changeLog
	 */
	public ChangeLog getChangeLog() {
		return changeLog;
	}

	/**
	 * @return the txInPassport
	 */
	public Passport getTxInPassport() {
		return txInPassport;
	}

	/**
	 * @return the txInLockMate
	 */
	public EQCLockMate getTxInLockMate() {
		return txInLockMate;
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

//	protected boolean plantingOperation() throws Exception {
//		for(Operation operation:operation) {
//			operation.planting();
//		}
//		return true;
//	}
	
	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(Value txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	/**
	 * @param lockType the lockType to set
	 */
	public void setLockType(LockType lockType) {
		this.lockType = lockType;
	}

	// Here need do more job to find a better way
	public byte[] getProof() {
		byte[] bytes = new byte[4];
		if (eqcWitness instanceof T1Witness) {
			bytes[0] = eqcWitness.getEqcSignature()[0];
			bytes[1] = eqcWitness.getEqcSignature()[9];
			bytes[2] = eqcWitness.getEqcSignature()[20];
			bytes[3] = eqcWitness.getEqcSignature()[31];
		} 
		else if(eqcWitness instanceof T2Witness) {
			bytes[0] = eqcWitness.getEqcSignature()[0];
			bytes[1] = eqcWitness.getEqcSignature()[21];
			bytes[2] = eqcWitness.getEqcSignature()[42];
			bytes[3] = eqcWitness.getEqcSignature()[63];
		}
		else {
			throw new IllegalStateException("Invalid EQCWitness type: " + eqcWitness);
		}
		return bytes;
	}
	
	public void setPriority(TRANSACTION_PRIORITY priority, Value txFee) throws Exception {
		Objects.requireNonNull(priority);
		if(priority == TRANSACTION_PRIORITY.VIP) {
			Value maxTxFeeLimit = getMaxTxFeeLimit();
			if(txFee.compareTo(getMaxTxFeeLimit()) <= 0) {
				throw new IllegalStateException("When priority is VIP the txfee shouldn't less than max txfee limit");
			}
			txFee = txFee.subtract(maxTxFeeLimit);
			if(txFee.compareTo(BigInteger.valueOf(32)) < 0) {
				throw new IllegalStateException("When priority is VIP the txfee shouldn't less than 32");
			}
			if(lockType == LockType.T2) {
				txFee = txFee.add(BigInteger.ONE);
			}
			txIn.setValue(txFee);
		}
		else {
			Value priorityValue = new Value(priority.getPriorityRate());
			if(lockType == LockType.T2) {
				priorityValue = priorityValue.add(BigInteger.valueOf(4));
			}
			txIn.setValue(priorityValue);
		}
	}

	/**
	 * @param transactionShape the transactionShape to set
	 */
	public void setTransactionShape(TransactionShape transactionShape) {
		this.transactionShape = transactionShape;
	}

	/**
	 * @return the lockType
	 */
	public LockType getLockType() {
		return lockType;
	}

	/**
	 * @param txInLockMate the txInLockMate to set
	 */
	public void setTxInLockMate(EQCLockMate eqcLockMate) {
		this.txInLockMate = eqcLockMate;
	}
	
}
