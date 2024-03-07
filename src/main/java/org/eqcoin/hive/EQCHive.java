/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Vector;

import org.eqcoin.avro.O;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.GlobalState.Plantable;
import org.eqcoin.persistence.globalstate.GlobalState.Statistics;
import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.seeds.EQCSeeds;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCStateObject;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Oct 1, 2018
 * @email 10509759@qq.com
 */
public class EQCHive extends EQCStateObject implements Plantable, Gateway {
	private EQCHiveRoot root;
	private EQCSeeds eqCoinSeeds;
	private GlobalState globalState;
	private EQCHiveRoot preRoot;
	private Value txFeeRate;

	// The min size of the EQCHeader's is 142 bytes.
	// Here need check the max size also need do more job
	private final int size = 142;

	public EQCHive() {
		super();
	}

	public EQCHive(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public EQCHive(final byte[] preProof, final ID currentHeight, final GlobalState globalState) throws Exception {
		super();
		this.globalState = globalState;
		if(currentHeight.equals(ID.ZERO)) {
			txFeeRate = new Value(Util.DEFAULT_POWER_PRICE);
		}
		else {
			//			final EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
			txFeeRate = new Value(eQcoinRootPassport.getTxFeeRate());
		}
		eqCoinSeeds.setEqcHive(this);
		// Create EQC block header
		root.setPreProof(preProof);
		root.setHeight(currentHeight);
		root.setTarget(Util.cypherTarget(globalState));
		root.setTotalSupply(Util.cypherTotalSupply(this));
		root.setTimestamp(new ID(System.currentTimeMillis()));
		root.setNonce(ID.ZERO);
	}

	public <T> EQCHive(final T type) throws Exception {
		super();
		parse(type);
	}

	@Override
	public byte[] getBin() {
		return EQCCastle.bytesToBIN(getBytes());
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(root.getBytes());
			os.write(eqCoinSeeds.getBytes());
		} catch (final Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * @return the EQCoinSeed
	 */
	public EQCSeeds getEQCoinSeeds() {
		return eqCoinSeeds;
	}

	public GlobalState getGlobalState() {
		return globalState;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.StateObject#getKey()
	 */
	@Override
	public byte[] getKey() throws Exception {
		return root.getHeight().getEQCBits();
	}

	public O getO() {
		return new O(ByteBuffer.wrap(this.getBytes()));
	}

	public EQCHiveRoot getPreRoot() {
		return preRoot;
	}

	public byte[] getProof() throws Exception {
		return root.getProof();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.rpc.gateway.Gateway#getProtocol(java.lang.Class)
	 */
	@Override
	public <T> T getProtocol(final Class<T> type) throws Exception {
		return Gateway.getProtocol(type, getBytes());
	}

	/**
	 * @return the eqcHiveRoot
	 */
	public EQCHiveRoot getRoot() {
		return root;
	}

	public int getSize() {
		return getBytes().length;
	}

	public Value getTxFeeRate() {
		return txFeeRate;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.StateObject#getValue()
	 */
	@Override
	public byte[] getValue() throws Exception {
		return getBytes();
	}

	@Override
	protected void init() {
		root = new EQCHiveRoot();
		eqCoinSeeds = new EQCSeeds();
	}

	@Override
	public boolean isSanity() throws Exception {
		if (root == null) {
			Log.Error("eqcHiveRoot == null");
			return false;
		}
		if(!root.isSanity()) {
			Log.Error("!eqcHiveRoot.isSanity()");
			return false;
		}
		if(eqCoinSeeds == null) {
			Log.Error("eQcoinSeed == null");
			return false;
		}
		if(!eqCoinSeeds.isSanity()) {
			Log.Error("!eQcoinSeed.isSanity()");
			return false;
		}
		if(eqCoinSeeds == null ) {
			Log.Error("eQcoinSeed == null");
			return false;
		}
		if (!eqCoinSeeds.isSanity()) {
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


			// Check if Target is valid
			if (!root.isDifficultyValid(globalState)) {
				Log.Error("EQCHeader difficulty is invalid");
				return false;
			}

			// Check if EQCHiveRoot is valid
			if(!root.isValid()) {
				Log.Error("EQCHiveRoot is invalid");
				return false;
			}

			// Check if EQcoinSeeds is valid
			if (!eqCoinSeeds.isValid()) {
				Log.Error("EQcoinSeeds is invalid");
				return false;
			}

			// Check Statistics
			final Statistics statistics = globalState.getStatistics();
			if (!root.isStatisticsValid(statistics, true)) {
				Log.Error("Statistics is invalid!");
				return false;
			}

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
			//			if (!eqcHiveRoot.setChangeLog(changeLog).setEQCoinSeed(eqCoinSeed).isValid()) {
			// Here maybe need do more job
			if (!root.isValid()) {
				Log.Error("EQCHeader is invalid!");
				return false;
			}

			// Merge shouldn't be done at here
			//		// Merge AccountsMerkleTree relevant Account's status
			//		if(!changeLog.merge()) {
			//			Log.Error("Merge AccountsMerkleTree relevant Account's status error occur");
			//			return false;
			//		}
		} catch (final Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}

	@Override
	public void parse(final ByteArrayInputStream is) throws Exception {
		Objects.requireNonNull(is);
		// Parse EqcHeader
		root = new EQCHiveRoot(is);
		// Parse EQcoinSeed
		eqCoinSeeds.parse(is);
		EQCCastle.assertNoRedundantData(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.rpc.gateway.Gateway#parse(java.lang.Object)
	 */
	@Override
	public <T> void parse(final T type) throws Exception {
		Objects.requireNonNull(type);
		final ByteArrayInputStream is = new ByteArrayInputStream(Gateway.parseProtocol(type));
		parse(is);
		EQCCastle.assertNoRedundantData(is);
	}

	@Override
	public void planting() throws Exception {

		// Retrieve all transactions from transaction pool
		final Vector<Transaction> pendingTransactionList = Util.MC().getTransactionListInPool();
		Log.info("Current have " + pendingTransactionList.size() + " pending Transactions.");

		/**
		 * Begin planting EQcoinSeeds
		 */
		eqCoinSeeds.planting(pendingTransactionList);

		// Planting new EQcoinSeeds finished just check if relevant Global State's Statistics data is valid
		final Statistics statistics = globalState.getStatistics();
		if (!root.isStatisticsValid(statistics, false)) {
			Log.Error("Statistics is invalid");
			throw new IllegalStateException("Statistics is invalid");
		}

		// Update EQCHiveRoot
		root.setTotalTransactionNumbers(statistics.getTotalTransactionNumbers());
		root.setTotalSupply(statistics.getTotalSupply());
		root.setTotalLockMateNumbers(statistics.getTotalLockMateNumbers());
		root.setTotalPassportNumbers(statistics.getTotalPassportNumbers());
		root.setEQCoinSeedsProof(eqCoinSeeds.getProof());
	}

	/**
	 * @param eqCoinSeed the eqCoinSeed to set
	 */
	public void setEQCoinSeeds(final EQCSeeds eqCoinSeed) {
		this.eqCoinSeeds = eqCoinSeed;
	}

	public void setGlobalState(final GlobalState globalState) throws Exception {
		this.globalState = globalState;
		preRoot = globalState.getEQCHiveRoot(root.getHeight().getPreviousID());
	}

	/**
	 * @param eqcHiveRoot the eqcHiveRoot to set
	 */
	public void setRoot(final EQCHiveRoot eqcHiveRoot) {
		this.root = eqcHiveRoot;
	}

	public void setTxFeeRate(final Value txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	@Override
	public String toInnerJson() {
		return

		"\"EQCHive\":{\n" + root.toInnerJson() + ",\n" + eqCoinSeeds.toInnerJson() + "\n" + "}";
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

}
