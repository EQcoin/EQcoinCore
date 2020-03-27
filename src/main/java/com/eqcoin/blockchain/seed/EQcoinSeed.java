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
package com.eqcoin.blockchain.seed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Statistics;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.TransferCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.EQCPublickey;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TxOut;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;

/**
 * @author Xun Wang
 * @date July 31, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSeed extends EQCSeed {
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#init()
	 */
	@Override
	public void init() {
		super.init();
		eqcSeedRoot = new EQcoinSeedRoot();
	}

	public EQcoinSeed() {
		super();
	}

	public EQcoinSeed(byte[] bytes) throws Exception {
		super(bytes);
	}

	public void addTransaction(Transaction transaction) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBytes().length;
	}
	
	public void addCoinbaseTransaction(TransferCoinbaseTransaction coinbaseTransaction, ChangeLog changeLog) throws ClassNotFoundException, SQLException, Exception {
		// Add Coinbase Transaction
		((EQcoinSeedRoot) eqcSeedRoot).setCoinbaseTransaction(coinbaseTransaction);
	}
	
	public EQcoinSeedRoot getEQcoinSeedRoot() {
		return (EQcoinSeedRoot) eqcSeedRoot;
	}
	
	@Override
	public boolean isValid() {
		try {
			
			// Verify CoinBaseTransaction
			// Here need check if need exist CoinbaseTransaction
			TransferCoinbaseTransaction coinbaseTransaction = getEQcoinSeedRoot().getCoinbaseTransaction();
			if(!coinbaseTransaction.isSanity()) {
				Log.Error("CoinBaseTransaction isn't sanity: " + coinbaseTransaction);
				return false;
			}
			coinbaseTransaction.init(changeLog);
			if (coinbaseTransaction.isValid()) {
				coinbaseTransaction.planting();
			} else {
				Log.Error("CoinBaseTransaction is invalid: " + coinbaseTransaction);
				return false;
			}

			Transaction transaction = null;
			for (int i = 0; i < newTransactionList.size(); ++i) {
				transaction = newTransactionList.get(i);
				transaction.init(changeLog);
				// Check the priority
				if(i < (newTransactionList.size() -1)) {
					// Here need do more job to determine which methods is better
					if(transaction.compareTo(newTransactionList.get(i+1)) <= 0) {
						Log.Error("The Transaction's priority is wrong.");
						return false;
					}
				}
				
				if(!transaction.isSanity()) {
					Log.Error("Transaction isn't sanity: " + transaction);
					return false;
				}

				if (transaction.isValid()) {
					newTransactionListLength += transaction.getBytes().length;
					transaction.planting();
				} else {
					Log.info("Transaction is invalid: " + transaction);
					return false;
				}
			}
			
			// Check if Transactions' size < 1 MB
			if (newTransactionListLength > Util.ONE_MB) {
				Log.Error("EQcoinSubchain's length is invalid");
				return false;
			}
			
			// Verify new EQcoinSeed finished just check if relevant Statistics data is valid
			changeLog.getStatistics().generateStatistics(this);
			if (!changeLog.getStatistics().isValid(this)) {
				Log.Error("Statistics is invalid!");
				return false;
			}
			
			// Check if EQcoinSeedRoot is valid
		
			
		} catch (Exception e) {
			Log.Error("EQcoinSeed is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		eqcSeedRoot = new EQcoinSeedRoot(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(eqcSeedRoot == null) {
			return false;
		}
		if(!eqcSeedRoot.isSanity()) {
			return false;
		}
//		if(!(newEQCSegWitList.isEmpty() && newEQCSegWitList.isEmpty() && newTransactionListLength == 0 && newCompressedPublickeyList.isEmpty())) {
//			return false;
//		}
//		if(newTransactionList.size() != newEQCSegWitList.size()) {
//			return false;
//		}
		long newTransactionListLength = 0;
		for(Transaction transaction:newTransactionList) {
			newTransactionListLength += transaction.getBin().length;
		}
		if(this.newTransactionListLength != newTransactionListLength) {
			return false;
		}
		return true;
	}
	
	
	public String toInnerJson() {
		return
				"\"EQcoinSeed\":{\n" + eqcSeedRoot.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n}\n}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#accountingTransaction(java.util.Vector)
	 */
	@Override
	public void plantingTransaction(Vector<Transaction> transactionList, ChangeLog changeLog) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if (changeLog.getHeight().compareTo(Util.getMaxCoinbaseHeight(changeLog.getHeight())) < 0) {
			// Create CoinBase Transaction
			Lock lock = new Lock();
			lock.setId(ID.ONE);
			TransferCoinbaseTransaction coinbaseTransaction = Util.generateTransferCoinbaseTransaction(lock, changeLog);
			if(!coinbaseTransaction.isSanity()) {
				Log.Error("CoinbaseTransaction isn't sanity have to terminate planting Transaction: " + coinbaseTransaction.toString());
				throw new IllegalStateException("CoinbaseTransaction isn't sanity have to terminate planting Transaction");
			}
			coinbaseTransaction.init(changeLog);
			if (!coinbaseTransaction.isValid()) {
				throw new IllegalStateException("CoinbaseTransaction is invalid: " + coinbaseTransaction);
			} else {
				coinbaseTransaction.planting();
			}
			// Add CoinBase into EQcoinSeedHeader
			addCoinbaseTransaction(coinbaseTransaction, changeLog);
		}
		
		// Handle every pending Transaction
		for (Transaction transaction : transactionList) {
			try {
				// Check if Transaction isSanity
				if(!transaction.isSanity()) {
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction isn't sanity just discard it: " + transaction.toString());
					continue;
				}
				
				// Init Transaction
				transaction.init(changeLog);
				
				// Check if Transaction is valid
				if (!transaction.isValid()) {
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
					Log.Error("Transaction is invalid: " + transaction);
					continue;
				}
			}
			catch (Exception e) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("During Transacton planting exception occur:" + e + " just discard it: "
						+ transaction.toString());
				continue;
			}
			
			// Add Transaction into EQcoinSeed
			if ((getNewTransactionListLength()
					+ transaction.getBytes().length) <= Util.MAX_EQCHIVE_SIZE) {
				Log.info("Add new Transaction which TxFee is: " + transaction.getTxFee());
				addTransaction(transaction);
				// Planting Transaction
				transaction.planting();
			} else {
				Log.info("Exceed EQcoinSubchain's MAX_BLOCK_SIZE just stop accounting transaction");
				break;
			}
		}
		
		// Planting new EQcoinSeed finished just check if relevant Statistics data is valid
		changeLog.getStatistics().generateStatistics(this);
		if (!changeLog.getStatistics().isValid(this)) {
			Log.Error("Statistics is invalid!");
			throw new IllegalStateException("Statistics is invalid!");
		}

		// Generate relevant root
		changeLog.buildLockMerkleTreeBase();
		changeLog.generateLockMerkleTreeRoot();
		changeLog.buildPassportMerkleTreeBase();
		changeLog.generatePassportMerkleTreeRoot();
		
		// Update EQcoinSeedRoot
		EQcoinSeedRoot eQcoinSeedRoot = (EQcoinSeedRoot) eqcSeedRoot;
		eQcoinSeedRoot.setTotalTransactionNumbers(changeLog.getStatistics().getTotalTransactionNumbers());
		eQcoinSeedRoot.setTotalSupply(changeLog.getStatistics().getTotalSupply());
		eQcoinSeedRoot.setTotalLockNumbers(changeLog.getTotalLockNumbers());
		eQcoinSeedRoot.setTotalPassportNumbers(changeLog.getTotalPassportNumbers());
		eQcoinSeedRoot.setTotalPublickeyNumbers(changeLog.getTotalPublickeyNumbers());
		eQcoinSeedRoot.setLockMerkelTreeRoot(changeLog.getLockMerkleTreeRoot());
		eQcoinSeedRoot.setPassportMerkelTreeRoot(changeLog.getPassportMerkleTreeRoot());
		
	}
	
	public boolean saveTransactions() throws Exception {
		return true;
	}
	
	public byte[] getHash() throws Exception {
		Vector<byte[]> vector = new Vector<>();
		vector.add(eqcSeedRoot.getBytes());
		vector.add(getBytes());
		return Util.getMerkleTreeRoot(vector, true);
	}

}