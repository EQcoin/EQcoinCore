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
package org.eqcoin.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;

import javax.naming.InitialContext;
import javax.print.attribute.Size2DSyntax;

import org.eqcoin.avro.O;
import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Statistics;
import org.eqcoin.crypto.MerkleTree;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.lock.LockMate;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.seed.EQCSeed;
import org.eqcoin.seed.EQcoinSeed;
import org.eqcoin.seed.EQcoinSeedRoot;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.service.MinerService;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.TransferCoinbaseTransaction;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.TxIn;
import org.eqcoin.transaction.ZionTxOut;
import org.eqcoin.transaction.operation.ChangeLock;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCHive extends EQCSerializable {
	private EQCHiveRoot eqcHiveRoot;
	private EQcoinSeed eQcoinSeed;
	private ChangeLog changeLog;

	// The min size of the EQCHeader's is 142 bytes. 
	// Here need check the max size also need do more job
	private final int size = 142;

	public EQCHive(byte[] bytes) throws Exception {
		super(bytes);
	}

	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse EqcHeader
		eqcHiveRoot = new EQCHiveRoot(is);
		// Parse EQcoinSeed
		eQcoinSeed.parse(is);
	}
	
	public EQCHive() {
		super();
	}

	protected void init() {
		eqcHiveRoot = new EQCHiveRoot();
		eQcoinSeed = new EQcoinSeed();
	}

	public EQCHive(byte[] preProof, ID currentHeight, ChangeLog changeLog) throws Exception {
		super();
		this.changeLog = changeLog;
		eQcoinSeed.setChangeLog(changeLog);
		// Create EQC block header
		eqcHiveRoot.setPreProof(preProof);
		eqcHiveRoot.setHeight(currentHeight);
		eqcHiveRoot.setTarget(Util.cypherTarget(changeLog));
		eqcHiveRoot.setTimestamp(new ID(System.currentTimeMillis()));
		eqcHiveRoot.setNonce(ID.ZERO);
	}

	/**
	 * @return the eqcHiveRoot
	 */
	public EQCHiveRoot getEQCHiveRoot() {
		return eqcHiveRoot;
	}

	/**
	 * @param eqcHiveRoot the eqcHiveRoot to set
	 */
	public void setEQCHiveRoot(EQCHiveRoot eqcHiveRoot) {
		this.eqcHiveRoot = eqcHiveRoot;
	}

	public ID getHeight() {
		return eqcHiveRoot.getHeight();
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
		return

		"\"EQCHive\":{\n" + eqcHiveRoot.toInnerJson() + ",\n" +
		eQcoinSeed.toInnerJson() + "\n" +
		 "}";
	}

	public void plantingEQcoinSeed() throws Exception {
		/**
		 * Heal Protocol If height equal to a specific height then update the EQcoin Federal's
		 * Lock to the new Lock the more information you can reference to
		 * https://github.com/eqzip/eqcoin
		 */
		
		// Retrieve all transactions from transaction pool
		Vector<Transaction> pendingTransactionList = new Vector<Transaction>();
		pendingTransactionList.addAll(Util.DB().getTransactionListInPool());
		Log.info("Current have " + pendingTransactionList.size() + " pending Transactions.");
				
		/**
		 * Begin handle EQcoinSeed
		 */
		eQcoinSeed.plantingTransaction(pendingTransactionList);
		
		// Set EQCHeader's Root's hash
		eqcHiveRoot.setEQCoinSeedProof(eQcoinSeed.getProof());
		
	}

	/**
	 * Deprecated use public boolean isValid(AccountsMerkleTree changeLog)
	 * instead of
	 * 
	 * @param eqcBlock
	 * @param changeLog
	 * @return
	 * @throws Exception
	 */
//	@Deprecated
	// Keep this only for reference&double check after used it then removed it
//	public static boolean verify(EQCHive eqcBlock, AccountsMerkleTree changeLog) throws Exception {
//		// Check if EQCHeader is valid
//		BigInteger target = Util.targetBytesToBigInteger(Util.cypherTarget(eqcBlock.getHeight()));
//		if (new BigInteger(1,
//				Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(eqcBlock.getEqcHeader().getBytes(), Util.HUNDRED_THOUSAND))
//						.compareTo(target) > 0) {
//			Log.Error("EQCHeader is invalid");
//			return false;
//		}
//
//		// Check if Transactions size is less than 1 MB
////		if (eqcBlock.getTransactions().getSize() > Util.ONE_MB) {
////			Log.Error("Transactions size  is invalid, size: " + eqcBlock.getTransactions().getSize());
////			return false;
////		}
//
//		// Check if AddressList is correct the first Address' Serial Number is equal to
//		// previous block's last Address' Serial Number + 1
//		// Every Address should be unique in current AddressList and doesn't exists in
//		// the history AddressList in H2
//		// Every Address's Serial Number should equal to previous Address'
//		// getNextSerialNumber
//
//		// Check if PublicKeyList is correct the first PublicKey's Serial Number is
//		// equal to previous block's last PublicKey's getNextSerialNumber
//		// Every PublicKey should be unique in current PublicKeyList and doesn't exists
//		// in the history PublicKeyList in H2
//		// Every PublicKey's Serial Number should equal to previous PublicKey's
//		// getNextSerialNumber
//
//		// Get Transaction list and Signature list
//		Vector<Transaction> transactinList = eqcBlock.getTransactions().getNewTransactionList();
//		Vector<byte[]> signatureList = eqcBlock.getSignatures().getSignatureList();
//
//		// In addition to the CoinBase transaction, the following checks are made for
//		// all other transactions.
//		// Check if every Transaction's PublicKey is exists
//		if (!eqcBlock.isEveryPublicKeyExists()) {
//			Log.Error("Every Transaction's PublicKey should exists");
//			return false;
//		}
//		// Check if every Transaction's Address is exists
//		if (!eqcBlock.isEveryAddressExists()) {
//			Log.Error("Every Transaction's Address should exists");
//			return false;
//		}
//
//		// Fill in every Transaction's PublicKey, Signature, relevant Address for verify
//		// Bad methods need change to every Transaction use itself's prepareVerify
////		eqcBlock.buildTransactionsForVerify();
//
//		// Check if only have CoinBase Transaction
//		if (signatureList == null) {
//			if (transactinList.size() != 1) {
//				Log.Error(
//						"Only have CoinBase Transaction but the number of Transaction isn't equal to 1, current size: "
//								+ transactinList.size());
//				return false;
//			}
//		} else {
//			// Check if every Transaction has it's Signature
//			if ((transactinList.size() - 1) != signatureList.size()) {
//				Log.Error("Transaction's number: " + (transactinList.size() - 1)
//						+ " doesn't equal to Signature's number: " + signatureList.size());
//				return false;
//			}
//		}
//
//		// Check if CoinBase is correct - CoinBase's Address&Value is valid
//		if (!transactinList.get(0).isCoinBase()) {
//			Log.Error("The No.0 Transaction isn't CoinBase");
//			return false;
//		}
//		// Check if CoinBase's TxOut Address is valid
//		if (!transactinList.get(0).isTxOutAddressValid()) {
//			Log.Error("The CoinBase's TxOut's Address is invalid: "
//					+ transactinList.get(0).getTxOutList().get(0).toString());
//			return false;
//		}
//		// Check if CoinBase's value is valid
//		long totalTxFee = 0;
//		for (int i = 1; i < transactinList.size(); ++i) {
//			totalTxFee += transactinList.get(i).getTxFee();
//		}
//		long coinBaseValue = 0;
//		if (eqcBlock.getHeight().compareTo(Util.getMaxCoinbaseHeight(eqcBlock.getHeight())) < 0) {
//			coinBaseValue = Util.COINBASE_REWARD + totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to COINBASE_REWARD + totalTxFee: " + (Util.COINBASE_REWARD + totalTxFee));
//				return false;
//			}
//		} else {
//			coinBaseValue = totalTxFee;
//			if (transactinList.get(0).getTxOutValues() != coinBaseValue) {
//				Log.Error("CoinBase's value: " + transactinList.get(0).getTxOutValues()
//						+ " doesn't equal to totalTxFee: " + totalTxFee);
//				return false;
//			}
//		}
//
//		// Check if only have one CoinBase
//		for (int i = 1; i < transactinList.size(); ++i) {
//			if (transactinList.get(i).isCoinBase()) {
//				Log.Error("Every EQCBlock should has only one CoinBase but No. " + i + " is also CoinBase.");
//				return false;
//			}
//		}
//
//		// Check if Signature is unique in current Signatures and doesn't exists in the
//		// history Signature table in H2
//		for (int i = 0; i < signatureList.size(); ++i) {
//			for (int j = i + 1; j < signatureList.size(); ++j) {
//				if (Arrays.equals(signatureList.get(i), signatureList.get(j))) {
//					Log.Error("Signature doesn't unique in current  Signature list");
//					return false;
//				}
//			}
//		}
//
//		for (byte[] signature : signatureList) {
//			if (EQCBlockChainH2.getInstance().isSignatureExists(signature)) {
//				Log.Error("Signature doesn't unique in H2's history Signature list");
//				return false;
//			}
//		}
//
//		// Check if every Transaction is valid
//		for (Transaction transaction : eqcBlock.getTransactions().getNewTransactionList()) {
//			if (transaction.isCoinBase()) {
//
//			} else {
//				if (!transaction.isValid(changeLog, AddressShape.READABLE)) {
//					Log.Error("Every Transaction should valid");
//					return false;
//				}
//			}
//		}
//
//		return true;
//	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqcHiveRoot.getBytes());
			os.write(eQcoinSeed.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	public int getSize() {
		return getBytes().length;
	}

	public byte[] getProof() throws NoSuchAlgorithmException {
		return eqcHiveRoot.getProof();
	}

	@Override
	public boolean isSanity() throws Exception {
		if (eqcHiveRoot == null) {
			Log.Error("eqcHiveRoot == null");
			return false;
		}
		if(!eqcHiveRoot.isSanity()) {
			Log.Error("!eqcHiveRoot.isSanity()");
			return false;
		}
		if(eQcoinSeed == null) {
			Log.Error("eQcoinSeed == null");
			return false;
		}
		if(!eQcoinSeed.isSanity()) {
			Log.Error("!eQcoinSeed.isSanity()");
			return false;
		}
		if(eQcoinSeed == null ) {
			Log.Error("eQcoinSeed == null");
			return false;
		}
		if (!eQcoinSeed.isSanity()) {
			Log.Error("!eQcoinSeed.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * Auditing the EQCHive
	 * <p>
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean isValid() {
		try {

			/**
			 * Heal Protocol If height equal to a specific height then update the EQcoin Federal's
			 * Lock to the new Lock the more information you can reference to
			 * https://github.com/eqzip/eqcoin
			 */
			
			// Check if Target is valid
			if (!eqcHiveRoot.isDifficultyValid(changeLog)) {
				Log.Error("EQCHeader difficulty is invalid.");
				return false;
			}

			// Check if EQcoinSeed is valid
			if (!eQcoinSeed.isValid()) {
				Log.Error("EQcoinSeed is invalid.");
				return false;
			}

//			// Verify Statistics
//			changeLog.getStatistics().generateStatistics(eQcoinSeed);
//			if (!changeLog.getStatistics().isValid(eQcoinSeed)) {
//				Log.Error("Statistics data is invalid.");
//				return false;
//			}
//
//			// Build AccountsMerkleTree and generate Root and Statistics
//			changeLog.buildProofBase();
//			changeLog.generateProofRoot();

			// Verify Root
//		// Check total supply
//		if (statistics.totalSupply != Util.cypherTotalSupply(eqcHeader.getHeight())) {
//			Log.Error("Total supply is invalid doesn't equal cypherTotalSupply.");
//			return false;
//		}
//		if(statistics.totalSupply != root.getTotalSupply()){
//			Log.Error("Total supply is invalid doesn't equal root.");
//			return false;
//		}

//			EQCHive previousBlock = EQCBlockChainRocksDB.getInstance()
//					.getEQCBlock(eqcHeader.getHeight().getPreviousID(), true);
			// Check total Account numbers
//		if (!previousBlock.getRoot().getTotalAccountNumbers()
//				.add(BigInteger.valueOf(transactions.getNewAccountList().size()))
//				.equals(changeLog.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal changeLog.");
//			return false;
//		}
//		if(!root.getTotalAccountNumbers().equals(changeLog.getTotalAccountNumbers())) {
//			Log.Error("Total Account numbers is invalid doesn't equal root.");
//			return false;
//		}

//		// Check total Transaction numbers
//		if (!previousBlock.getRoot().getTotalTransactionNumbers()
//				.add(BigInteger.valueOf(transactions.getNewTransactionList().size()))
//				.equals(statistics.totalTransactionNumbers)) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal transactions.getNewTransactionList.");
//			return false;
//		}
//		if(!statistics.totalTransactionNumbers.equals(root.getTotalTransactionNumbers())) {
//			Log.Error("Total Transaction numbers is invalid doesn't equal root.getTotalTransactionNumbers.");
//			return false;
//		}
			// Check AccountsMerkelTreeRoot
//			if (!Arrays.equals(eqcRoot.getAccountsMerkelTreeRoot(), changeLog.getPassportMerkleTreeRoot())) {
//				Log.Error("EQCPassportStateRoot is invalid!");
//				return false;
//			}
			// Check TransactionsMerkelTreeRoot
//			if (!Arrays.equals(eqcRoot.getSubchainsMerkelTreeRoot(), eQcoinSeed.getRoot())) {
//				Log.Error("EQcoinSeedStateRoot is invalid!");
//				return false;
//			}
			// Verify EQCHeader
			if (!eqcHiveRoot.setChangeLog(changeLog).setEQcoinSeed(eQcoinSeed).isValid()) {
				Log.Error("EQCHeader is invalid!");
				return false;
			}

			// Merge shouldn't be done at here
//		// Merge AccountsMerkleTree relevant Account's status
//		if(!changeLog.merge()) {
//			Log.Error("Merge AccountsMerkleTree relevant Account's status error occur");
//			return false;
//		}
		} catch (Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}

	public O getO() {
		return new O(ByteBuffer.wrap(this.getBytes()));
	}

	/**
	 * @return the EQcoinSeed
	 */
	public EQcoinSeed getEQcoinSeed() {
		return eQcoinSeed;
	}

	/**
	 * @param eQcoinSeed the eQcoinSeed to set
	 */
	public void setEQcoinSeed(EQcoinSeed eQcoinSeed) {
		this.eQcoinSeed = eQcoinSeed;
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
		eQcoinSeed.setChangeLog(changeLog);
		changeLog.setCoinbaseTransaction(eQcoinSeed.getEQcoinSeedRoot().getCoinbaseTransaction());
	}
	
}
