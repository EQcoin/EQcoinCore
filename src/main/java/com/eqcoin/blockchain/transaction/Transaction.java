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
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQCWitness;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TXFEE_RATE;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.crypto.EQCPublicKey;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCLockShapeTypable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public abstract class Transaction extends EQCSerializable implements Comparator<Transaction>, Comparable<Transaction> {
	/**
	 * Header
	 */
	protected ID solo;
	protected TransactionType transactionType;
	/**
	 * Body
	 */
	protected ID nonce;
	protected TxIn txIn;
	protected EQCWitness eqcWitness;
	public final static ID SOLO = ID.ZERO;
	protected EQcoinSeed eQcoinSeed;
	protected ChangeLog changeLog;
	protected Passport txInPassport;
	protected Lock txInLock;
	protected TransactionShape transactionShape;
	
	public enum TransactionType {
		ZION, ZIONCOINBASE, TRANSFER, TRANSFERCOINBASE, ZIONOP, TRANSFEROP, MODERATEOP;
		public static TransactionType get(int ordinal) {
			TransactionType transactionType = null;
			switch (ordinal) {
			case 0:
				transactionType = TransactionType.ZION;
				break;
			case 1:
				transactionType = TransactionType.ZIONCOINBASE;
				break;
			case 2:
				transactionType = TransactionType.TRANSFER;
				break;
			case 3:
				transactionType = TransactionType.TRANSFERCOINBASE;
				break;
			case 4:
				transactionType = TransactionType.ZIONOP;
				break;
			case 5:
				transactionType = TransactionType.TRANSFEROP;
				break;
			case 6:
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
		SIGN, SEED;
		public static TransactionShape get(int ordinal) {
			TransactionShape transactionShape = null;
			switch (ordinal) {
			case 0:
				transactionShape = TransactionShape.SIGN;
				break;
			case 1:
				transactionShape = TransactionShape.SEED;
				break;
			}
			return transactionShape;
		}

		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}

	public enum TXFEE_RATE {
		POSTPONEVIP(9), POSTPONE0(8), POSTPONE20(4), POSTPONE40(2), POSTPONE60(1);
		private TXFEE_RATE(int rate) {
			this.rate = rate;
		}

		private int rate;

		public int getRate() {
			return rate;
		}

		public static TXFEE_RATE get(int rate) {
			TXFEE_RATE txfee_rate = null;
			switch (rate) {
			case 1:
				txfee_rate = TXFEE_RATE.POSTPONE60;
				break;
			case 2:
				txfee_rate = TXFEE_RATE.POSTPONE40;
				break;
			case 4:
				txfee_rate = TXFEE_RATE.POSTPONE20;
				break;
			case 8:
				txfee_rate = TXFEE_RATE.POSTPONE0;
				break;
			case 9:
				txfee_rate = TXFEE_RATE.POSTPONEVIP;
				break;
			}
			return txfee_rate;
		}
		
	}

	public void init(ChangeLog changeLog) throws Exception {
		this.changeLog = changeLog;
		txInPassport = changeLog.getFilter().getPassport(txIn.getLock().getId(), false);
		txInLock = changeLog.getFilter().getLock(txInPassport.getLockID(), true);
		if(txInPassport == null || txInLock == null) {
			throw new IllegalStateException("TxIn's relevant Lock and Passport shouldn't null.");
		}
	}
	
	protected void init() {
		solo = SOLO;
		eqcWitness = new EQCWitness();
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
			if ((nResult = this.getQosRate().compareTo(o.getQosRate())) == 0) {
				if ((nResult = this.txIn.getLock().getId().compareTo(o.getTxIn().getLock().getId())) == 0) {
					nResult = this.nonce.compareTo(o.getNonce());
				}
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
			if ((nResult = o1.getQosRate().compareTo(o2.getQosRate())) == 0) {
				if ((nResult = o1.txIn.getLock().getId().compareTo(o2.getTxIn().getLock().getId())) == 0) {
					nResult = o1.nonce.compareTo(o2.getNonce());
				}
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

	public long getMaxTxFeeLimit() {
		return (getMaxBillingLength() * TXFEE_RATE.POSTPONE0.getRate() * Util.TXFEE_RATE);
	}

	public long getDefaultTxFeeLimit() {
		return (getMaxBillingLength() * Util.TXFEE_RATE);
	}

	public int getMaxBillingLength() {
		return 0;
	}

	public long getTxFee() throws Exception {
		long txFee = 0;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			txFee = getTxFeeLimit();
		} else {
			txFee = getBillingSize() * getQos().getRate() * Util.TXFEE_RATE;
		}
		return txFee;
	}

	public long cypherTxFeeLimit(TXFEE_RATE txfee_rate) {
		return (getMaxBillingLength() * txfee_rate.getRate() * Util.TXFEE_RATE);
	}

	public TXFEE_RATE getQos() {
		int rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingLength() * Util.TXFEE_RATE));
		}
		return TXFEE_RATE.get(rate);
	}

	public ID getQosRate() {
		long rate = 1;
		if (getTxFeeLimit() > getMaxTxFeeLimit()) {
			rate = TXFEE_RATE.POSTPONEVIP.getRate() + getTxFeeLimit() - getMaxTxFeeLimit();
		} else {
			rate = (int) (getTxFeeLimit() / (getMaxBillingLength() * Util.TXFEE_RATE));
		}
		return new ID(rate);
	}

	public boolean isBaseValid() {
		// Check if Nonce is correct
		if (!nonce.isNextID(txInPassport.getNonce())) {
			Log.Error("Nonce doesn't correct, current: " + nonce + " expect: " + txInPassport.getNonce().getNextID());
			return false;
		}

		if (txInLock.getPublickey() == null) {
			// Here need do more job
//			if (compressedPublickey.isNULL()) {
//				return false;
//			}
//			// Verify Publickey
//			if (!LockTool.verifyLockAndPublickey(txInLock.getReadableLock(),
//					compressedPublickey.getPublickey())) {
//				Log.Error("Verify Publickey failed");
//				return false;
//			}
		}
		
		// Check balance from current Passport
		if (txIn.getValue() + Util.MIN_EQC > txInPassport.getBalance().longValue()) {
			Log.Error("Balance isn't enough");
			return false;
		}
		
		return true;
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
	 * 验证TxOut的地址是不是唯一的存在？也即每个TxOut地址只能出现一次。 7. 验证TxOut是否小于TxIn？ 8.
	 * 验证是否TxFee大于零，验证是否所有的Txin&TxOut的Value大于零。 // 9. 验证TxFee是否足够？
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

	public static TransactionType parseTransactionType(byte[] bytes) {
		TransactionType transactionType = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		byte[] data = null;
		ID solo = null;
		try {
			if ((data = EQCType.parseEQCBits(is)) != null) {
				solo = EQCType.eqcBitsToID(data);
			}
			if (solo.equals(SOLO)) {
				if ((data = EQCType.parseEQCBits(is)) != null) {
					transactionType = TransactionType.get(EQCType.eqcBitsToInt(data));
				}
			} else {
				transactionType = TransactionType.TRANSFER;
			}
		} catch (NoSuchFieldException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		if(transactionType == null) {
			throw new NullPointerException("Bad transaction format.");
		}
		return transactionType;
	}
	
	protected boolean isTransactionTypeSanity() {
		return false;
	}

	public static Transaction parseTransaction(byte[] bytes) {
		if(bytes == null) {
			return null;
		}
		Transaction transaction = null;
		TransactionType transactionType = parseTransactionType(bytes);

		try {
			if (transactionType == TransactionType.TRANSFERCOINBASE) {
				transaction = new TransferCoinbaseTransaction(bytes);
			} else if (transactionType == TransactionType.ZION) {
				transaction = new ZionTransaction(bytes);
			} else if (transactionType == TransactionType.ZIONOP) {
				transaction = null;
			} else if (transactionType == TransactionType.TRANSFER) {
				transaction = new TransferTransaction(bytes);
			} else if (transactionType == TransactionType.TRANSFEROP) {
				transaction = new TransferOPTransaction(bytes);
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return transaction;
	}
	
	public boolean verifySignature() throws ClassNotFoundException, SQLException, Exception {
		boolean isTransactionValid = false;
		Signature signature = null;
		// Verify Signature
		try {
			signature = Signature.getInstance("NONEwithECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (txInLock.getAddressType() == LockType.T1) {
				eccType = ECCTYPE.P256;
			} else if (txInLock.getAddressType() == LockType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCPublicKey eqcPublicKey = new EQCPublicKey(eccType);
			// Create EQPublicKey according to compressed Publickey
			eqcPublicKey.setECPoint(txInLock.getPublickey());
			signature.initVerify(eqcPublicKey);
			signature.update(MessageDigest.getInstance(Util.SHA3_512).digest(getBytes()));
			isTransactionValid = signature.verify(eqcWitness.getSignature());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}

	public byte[] sign(Signature ecdsa) throws ClassNotFoundException, SQLException, Exception {
		try {
			ecdsa.update(MessageDigest.getInstance(Util.SHA3_512).digest(getBytes()));
			eqcWitness.setSignature(ecdsa.sign());
		} catch (SignatureException | IOException e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return eqcWitness.getSignature();
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

//	public void preparePlanting() throws Exception {
//		eqcWitness = eQcoinSeed.getNextEQCSegWit();
//		if (eqcWitness == null) {
//			throw new IllegalStateException("Transaction relevant EQCSegWit shouldn't null.");
//		}
//		if (txInLock.getPublickey() == null) {
//			compressedPublickey = eQcoinSeed.getNextCompressedPublickey();
//			compressedPublickey.setNew(true);
//			if (compressedPublickey == null) {
//				throw new IllegalStateException("Transaction relevant CompressedPublickey shouldn't null.");
//			}
//		}
//	}
	
	public void planting() throws Exception {
		try {
			changeLog.getFilter().getConnection().setAutoCommit(false);
			derivedPlanting();
			changeLog.getFilter().getConnection().commit();
		} catch (Exception e) {
			Log.Error(e.getMessage());
			changeLog.getFilter().getConnection().rollback();
			throw e;
		} finally {
			free();
			changeLog.getFilter().getConnection().setAutoCommit(true);
		}
	}
	
	protected void derivedPlanting() throws Exception {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		txInPassport.increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		txInPassport.withdraw(new ID(getBillingValue()));
		txInPassport.setUpdateHeight(changeLog.getHeight());
		changeLog.getFilter().savePassport(txInPassport);
		// Deposit TxFee
		Passport eqCoinFederal = changeLog.getFilter().getPassport(ID.ONE, true);
		eqCoinFederal.deposit(new ID(getTxFee()));
		eqCoinFederal.setUpdateHeight(changeLog.getHeight());
		changeLog.getFilter().savePassport(eqCoinFederal);
	}
	
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		parseDerivedBody(is);
		eqcWitness = new EQCWitness(is);
	}
	
	protected void parseDerivedBody(ByteArrayInputStream is) throws Exception {
		
	}
	
	public long getTxFeeLimit() {
		return 0;
	}

	public boolean isTxFeeLimitValid() {
//		Log.info("getTxFeeLimit(): " + getTxFeeLimit());
//		Log.info("getMaxTxFeeLimit(): " + getMaxTxFeeLimit());
//		Log.info("getDefaultTxFeeLimit(): " + getDefaultTxFeeLimit());
		boolean boolIsValid = true;
		if (getTxFeeLimit() < getDefaultTxFeeLimit()) {
			boolIsValid = false;
		}
//		else if ((getTxFeeLimit() <= getMaxTxFeeLimit()) && (getTxFeeLimit() % getDefaultTxFeeLimit()) != 0) {
//			boolIsValid = false;
//		}
		return boolIsValid;
	}

	public long getBillingValue() throws Exception {
		return 0;
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
	}

	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
	}

	public int getBillingSize() throws Exception {
		return 0;
	}
	
	protected boolean isTxInSanity() {
		return txIn != null && txIn.isSanity(LockShape.ID);
	}
	
	public boolean isBaseSanity() {
		if (solo == null || !solo.equals(SOLO) || transactionType == null || !isTransactionTypeSanity() || isTxInSanity() || nonce == null
				|| !nonce.isSanity() || eqcWitness == null || !eqcWitness.isSanity()) {
			return false;
		}
		return true;
	}
	
	protected boolean isDerivedSanity() {
		return false;
	}
	
	public boolean isSanity() {
		return isBaseSanity() && isDerivedSanity();
	}
	
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		parseSoloAndTransactionType(is);
		parseNonce(is);
		parseTxIn(is);
	}
	
	protected void parseNonce(ByteArrayInputStream is) throws Exception {
		// Parse nonce
		nonce = new ID(EQCType.parseEQCBits(is));
	}
	
	protected void parseSoloAndTransactionType(ByteArrayInputStream is) throws Exception {
		// Parse Solo
		solo = EQCType.parseID(is);
		// Parse Transaction type
		transactionType = TransactionType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
	}
	
	protected void parseTxIn(ByteArrayInputStream is) throws Exception {
		// Parse TxIn
		txIn = new TxIn(is, LockShape.ID);
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
	
	protected void serializeNonce(ByteArrayOutputStream os) throws Exception {
		// Serialization nonce
		os.write(EQCType.bigIntegerToEQCBits(nonce));
	}
	
	protected void serializeSoloAndTransactionTypeBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Solo
		os.write(solo.getEQCBits());
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
	}
	
	protected void serializeTxInBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization TxIn
		os.write(txIn.getBytes(LockShape.ID));
	}
	
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization derived body bytes
			os.write(getDerivedBodyBytes());
			if (transactionShape == TransactionShape.SEED) {
				// Serialization Witness
				os.write(eqcWitness.getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	protected byte[] getDerivedBodyBytes() throws Exception {
		return null;
	}

//	public static Transaction parseRPC(byte[] bytes) throws NoSuchFieldException, IllegalStateException, IOException {
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		return parseRPC(is);
//	}
//	
//	public static Transaction parseRPC(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
//		// Parse Transaction
//		Transaction transaction = Transaction.parseTransaction(EQCType.parseBIN(is));
//		// Parse EQCWitness
//		transaction.getEqcSegWit().setSignature(EQCType.parseBIN(is));
//		// Parse Compressed PublicKey
//		transaction.getPublickey().setPublickey(EQCType.parseBIN(is));
//	
//		return transaction;
//	}

	public Nest getNest() {
		Nest nest = new Nest();
		nest.setId(txIn.getLock().getId());
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
		txInLock = null;
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
	 * @return the txInLock
	 */
	public Lock getTxInLock() {
		return txInLock;
	}

	/**
	 * @return the transactionShape
	 */
	public TransactionShape getTransactionShape() {
		return transactionShape;
	}

	/**
	 * @param transactionShape the transactionShape to set
	 */
	public void setTransactionShape(TransactionShape transactionShape) {
		this.transactionShape = transactionShape;
	}
	
}
