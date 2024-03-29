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
package org.eqcoin.seeds;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.lock.LockTool;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.TransferCoinbaseTransaction;
import org.eqcoin.transaction.ZeroZionCoinbaseTransaction;
import org.eqcoin.transaction.ZionCoinbaseTransaction;
import org.eqcoin.transaction.txout.ZionTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date July 31, 2019
 * @email 10509759@qq.com
 */
public class EQCSeeds extends EQCObject {
	private Vector<Transaction> newTransactionList;
	// This is the new Transaction list's total size which should less than MAX_EQCHIVE_SIZE.
	private int newTransactionListLength;
	private EQCHive eqcHive;
	
	public EQCSeeds() {
		super();
	}

	public EQCSeeds(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public EQCSeeds(final ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	protected String _getNewTransactionList() {
		String tx = null;
		if (newTransactionList != null && newTransactionList.size() > 0) {
			tx = "\n[\n";
			if (newTransactionList.size() > 1) {
				for (int i = 0; i < newTransactionList.size() - 1; ++i) {
					tx += newTransactionList.get(i) + ",\n";
				}
			}
			tx += newTransactionList.get(newTransactionList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}

	@Deprecated
	public void addCoinbaseTransaction(final Transaction coinbaseTransaction) throws ClassNotFoundException, SQLException, Exception {
		// Add Coinbase Transaction
	}
	
	public void addTransaction(final Transaction transaction) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			// Here need change to get seed bytes
			newTransactionListLength += transaction.getBytes().length;
	}
	
	/**
	 * @return the newTransactionList
	 */
	public Vector<Transaction> getNewTransactionList() {
		return newTransactionList;
	}
	
	public byte[] getProof() throws Exception {
		if(newTransactionList.isEmpty()) {
			return EQCCastle.NULL_ARRAY;
		}
		else {
			final Vector<byte[]> transactions = new Vector<>();
			for (final Transaction transaction : newTransactionList) {
				transactions.add(transaction.getBytes());
			}
			return Util.getMerkleTreeRoot(transactions, true);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#init()
	 */
	@Override
	public void init() {
		newTransactionList = new Vector<>();
	}
	
	public boolean isValid() {
		try {
			// Verify CoinBaseTransaction
			// Here need check if need exist CoinbaseTransaction
//			TransferCoinbaseTransaction coinbaseTransaction = (TransferCoinbaseTransaction) getEQCoinSeedRoot().getCoinbaseTransaction();
//			if(!coinbaseTransaction.isSanity()) {
//				Log.Error("CoinBaseTransaction isn't sanity: " + coinbaseTransaction);
//				return false;
//			}
//			coinbaseTransaction.init(changeLog);
//			if (!coinbaseTransaction.planting()) {
//				Log.Error("CoinBaseTransaction is invalid: " + coinbaseTransaction);
//				return false;
//			}

			Transaction transaction = null;
			for (int i = 0; i < newTransactionList.size(); ++i) {
				transaction = newTransactionList.get(i);
				if(i == 0) {
					if(eqcHive.getRoot().getHeight().equals(ID.ZERO)) {
						if(!(transaction instanceof ZeroZionCoinbaseTransaction)) {
						Log.Error("Current height is zero the coinbase transaction should be ZeroZionCoinbaseTransaction, but actual it's: " + transaction);
						return false;
						}
					} 
					else {
						if(!(transaction instanceof ZionCoinbaseTransaction) || !(transaction instanceof TransferCoinbaseTransaction)) {
							Log.Error("The coinbase transaction should be ZionCoinbaseTransaction or TransferCoinbaseTransaction, but actual it's: " + transaction);
							return false;
							}
					}
				}
				else {
					if(transaction instanceof ZeroZionCoinbaseTransaction || transaction instanceof ZionCoinbaseTransaction || transaction instanceof TransferCoinbaseTransaction) {
						Log.Error("Only No.1 transaction can be coinbase transaction, but actual it's: " + transaction);
						return false;
					}
				}
				transaction.init(eqcHive);
				// Check the priority
				if(i < (newTransactionList.size() -1)) {
					// Here need do more job to determine which methods is better
					if(transaction.compareTo(newTransactionList.get(i+1)) <= 0) {
						Log.Error("The Transaction's priority is wrong.");
						return false;
					}
				}
				
				if (transaction.planting()) {
					newTransactionListLength += transaction.getBytes().length;
				} else {
					Log.info("Transaction is invalid: " + transaction);
					return false;
				}
			}
			
			// Check if Transactions' size < 1 MB 
			// 20200616 here need change to current EQCHive size
			if (newTransactionListLength > Util.ONE_MB) {
				Log.Error("EQcoinSubchain's length is invalid");
				return false;
			}
			
//			// Verify new EQcoinSeeds finished just audit if relevant Statistics data is valid
//			Statistics statistics = eqcHive.getGlobalState().getStatistics();
//			if (!isStatisticsValid(statistics)) {
//				Log.Error("Statistics is invalid!");
//				return false;
//			}
			
			// Check if EQcoinSeedRoot is valid include verify relevant root according to
			// ChangeLog and verify new EQcoinSeed finished just audit if relevant
			// Statistics data is valid
//			if(!eqCoinSeedRoot.isValid()) {
//				Log.Error("EQcoinSeedRoot is invalid!");
//				return false;
//			}
			
		} catch (final Exception e) {
			Log.Error("EQcoinSeed is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}
	
	public void planting(final Vector<Transaction> transactionList) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		// Here need sort all transactions from transaction pool
		
		Transaction coinbaseTransaction = null;
 		if(eqcHive.getRoot().getHeight().equals(ID.ZERO)) {
			final ZeroZionCoinbaseTransaction zeroZionCoinbaseTransaction = new ZeroZionCoinbaseTransaction();

			ZionTxOut txOut = new ZionTxOut();
 			txOut.setLock(LockTool.readableLockToEQCLock(Util.SINGULARITY_A));
			final Value minerCoinbaseReward = Util.getCurrentMinerCoinbaseReward(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()));
			txOut.setValue(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()).subtract(minerCoinbaseReward));
			zeroZionCoinbaseTransaction.setEqCoinFederalTxOut(txOut);

			txOut = new ZionTxOut();
			txOut.setLock(LockTool.readableLockToEQCLock(Util.SINGULARITY_B));
			txOut.setValue(minerCoinbaseReward);
			zeroZionCoinbaseTransaction.setEqCoinMinerTxOut(txOut);
			zeroZionCoinbaseTransaction.setNonce(eqcHive.getRoot().getHeight().getNextID());
			coinbaseTransaction = zeroZionCoinbaseTransaction;
			coinbaseTransaction.init(eqcHive);
			if (!coinbaseTransaction.planting()) {
				throw new IllegalStateException("ZeroZionCoinbaseTransaction is invalid: " + coinbaseTransaction);
			} 
			// Add CoinBase into EQcoinSeedHeader
			addCoinbaseTransaction(coinbaseTransaction);
		}
		else {
//			EQCHiveRoot preEQCHiveRoot = Util.GS().getEQCoinSeedRoot(changeLog.getHeight().getPreviousID());
			final EQCHiveRoot preEQCHiveRoot =  eqcHive.getGlobalState().getEQCHiveRoot(eqcHive.getRoot().getHeight().getPreviousID());
			if(preEQCHiveRoot.getTotalSupply().compareTo(Util.MAX_EQC) < 0) {
				// Create CoinBase Transaction
				coinbaseTransaction = Util.generateTransferCoinbaseTransaction(ID.ONE, eqcHive.getGlobalState());
				coinbaseTransaction.init(eqcHive);
				if (!coinbaseTransaction.planting()) {
					throw new IllegalStateException("CoinbaseTransaction is invalid: " + coinbaseTransaction);
				}
				// Add CoinBase into EQcoinSeedHeader
				addCoinbaseTransaction(coinbaseTransaction);
			}
		}
		
		// Handle every pending Transaction
		for (final Transaction transaction : transactionList) {
			// Add Transaction into EQcoinSeed
			if ((newTransactionListLength
					+ transaction.getBytes().length) <= Util.MAX_EQCHIVE_SIZE) { // Here need change to retrieve from EQCoinPassport
				Log.info("Add new Transaction with nonce: " + transaction.getNonce());
				
				try {
					// Init Transaction
					transaction.init(eqcHive);
					
					// Check if Transaction is sanity and valid then planting
					if (!transaction.planting()) {
						Util.MC().deleteTransactionInPool(transaction);
						Log.Error("Transaction is invalid planting failed: " + transaction);
						continue;
					}
					
				}
				catch (final Exception e) {
					Util.MC().deleteTransactionInPool(transaction);
					Log.Error("During add new transacton exception occur:" + e + " just discard it: "
							+ transaction.toString());
					continue;
				}
				
				addTransaction(transaction);
	
			} else {
				Log.info("Exceed EQcoinSubchain's MAX_BLOCK_SIZE just stop accounting transaction");
				break;
			}
		}
		Log.info("EQCoinSeeds planting successful");
	}
	
	public boolean saveTransactions() throws Exception {
		return true;
	}
	
	public void setEqcHive(final EQCHive eqcHive) {
		this.eqcHive = eqcHive;
	}

	@Override
	public String toInnerJson() {
		return
				"\"EQcoinSeeds\":{\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n}\n}";
	}
	
}