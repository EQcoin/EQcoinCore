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
package org.eqcoin.seed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import org.eqcoin.changelog.Statistics;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.Publickey;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.T2Lock;
import org.eqcoin.passport.AssetPassport;
import org.eqcoin.passport.EQcoinRootPassport;
import org.eqcoin.passport.Passport;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.TransferCoinbaseTransaction;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.Value;
import org.eqcoin.transaction.ZeroZionCoinbaseTransaction;
import org.eqcoin.transaction.ZionCoinbaseTransaction;
import org.eqcoin.transaction.ZionTxOut;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

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
	
	public EQcoinSeed(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public void addTransaction(Transaction transaction) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			// Here need change to get seed bytes
			newTransactionListLength += transaction.getBytes().length;
	}
	
	public void addCoinbaseTransaction(Transaction coinbaseTransaction) throws ClassNotFoundException, SQLException, Exception {
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
			TransferCoinbaseTransaction coinbaseTransaction = (TransferCoinbaseTransaction) getEQcoinSeedRoot().getCoinbaseTransaction();
			if(!coinbaseTransaction.isSanity()) {
				Log.Error("CoinBaseTransaction isn't sanity: " + coinbaseTransaction);
				return false;
			}
			coinbaseTransaction.init(changeLog);
			if (!coinbaseTransaction.planting()) {
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
				
				if (transaction.planting()) {
					newTransactionListLength += transaction.getBytes().length;
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
			
			// Deposit txFee
			if(changeLog.getTxFee().compareTo(Value.ZERO) > 0) {
				Log.info("Current EQCHive's txFee is: " + changeLog.getTxFee());
				Value minerTxFee = changeLog.getTxFee().multiply(Value.valueOf(5)).divide(Value.valueOf(100));
				Value eqCoinFederalTxFee = changeLog.getTxFee().subtract(minerTxFee);
				Passport eqCoinFederalPassport = changeLog.getFilter().getPassport(ID.ZERO, true);
				Passport minerPassport = null;
				if(changeLog.getCoinbaseTransaction() instanceof TransferCoinbaseTransaction) {
					TransferCoinbaseTransaction transferCoinbaseTransaction = (TransferCoinbaseTransaction) changeLog.getCoinbaseTransaction();
					minerPassport = changeLog.getFilter().getPassport(transferCoinbaseTransaction.getEqCoinMinerTxOut().getPassportId(), true);
				}
				else {
					ZionCoinbaseTransaction zionCoinbaseTransaction = (ZionCoinbaseTransaction) changeLog.getCoinbaseTransaction();
					// Here may need do more job
					ID minerLockId = changeLog.getFilter().isLockExists(zionCoinbaseTransaction.getEqCoinMinerTxOut().getLock(), true);
					minerPassport = changeLog.getFilter().getPassportFromLockId(minerLockId);
				}
				eqCoinFederalPassport.deposit(eqCoinFederalTxFee);
				minerPassport.deposit(minerTxFee);
				eqCoinFederalPassport.setChangeLog(changeLog).planting();
				minerPassport.setChangeLog(changeLog).planting();
			}
			
			// Verify new EQcoinSeed finished just audit if relevant Statistics data is valid
			changeLog.getStatistics().generateStatistics(this);
			if (!changeLog.getStatistics().isValid(this)) {
				Log.Error("Statistics is invalid!");
				return false;
			}
			
			// Check if EQcoinSeedRoot is valid include verify relevant root according to ChangeLog
			if(!eqcSeedRoot.isValid()) {
				Log.Error("EQcoinSeedRoot is invalid!");
				return false;
			}
			
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

	public String toInnerJson() {
		return
				"\"EQcoinSeed\":{\n" + eqcSeedRoot.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n}\n}";
	}
	
	public void plantingTransaction(Vector<Transaction> transactionList) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		// Here need sort all transactions from transaction pool
		
		Transaction coinbaseTransaction = null;
		if(changeLog.getHeight().equals(ID.ZERO)) {
			ZeroZionCoinbaseTransaction zeroZionCoinbaseTransaction = new ZeroZionCoinbaseTransaction();

			ZionTxOut txOut = new ZionTxOut();
			txOut.setLock(LockTool.readableLockToEQCLock(Util.SINGULARITY_A));
			Value minerCoinbaseReward = Util.getCurrentMinerCoinbaseReward(Util.getCurrentCoinbaseReward(changeLog));
			txOut.setValue(Util.getCurrentCoinbaseReward(changeLog).subtract(minerCoinbaseReward));
			zeroZionCoinbaseTransaction.setEqCoinFederalTxOut(txOut);

			txOut = new ZionTxOut();
			txOut.setLock(LockTool.readableLockToEQCLock(Util.SINGULARITY_B));
			txOut.setValue(minerCoinbaseReward);
			zeroZionCoinbaseTransaction.setEqCoinMinerTxOut(txOut);
			zeroZionCoinbaseTransaction.setNonce(changeLog.getHeight().getNextID());
			coinbaseTransaction = zeroZionCoinbaseTransaction;
			coinbaseTransaction.init(changeLog);
			if (!coinbaseTransaction.planting()) {
				throw new IllegalStateException("ZeroZionCoinbaseTransaction is invalid: " + coinbaseTransaction);
			} 
			// Add CoinBase into EQcoinSeedHeader
			addCoinbaseTransaction(coinbaseTransaction);
			// Here need do more job to analysis if exist a better way
			changeLog.setCoinbaseTransaction(coinbaseTransaction);
		}
		else {
			EQcoinSeedRoot eQcoinSeedRoot = Util.DB().getEQcoinSeedRoot(changeLog.getHeight().getPreviousID());
			if(eQcoinSeedRoot.getTotalSupply().compareTo(Util.MAX_EQC) <= 0) {
				// Create CoinBase Transaction
				coinbaseTransaction = Util.generateTransferCoinbaseTransaction(ID.ONE, changeLog);
				coinbaseTransaction.init(changeLog);
				if (!coinbaseTransaction.planting()) {
					throw new IllegalStateException("CoinbaseTransaction is invalid: " + coinbaseTransaction);
				}
				// Add CoinBase into EQcoinSeedHeader
				addCoinbaseTransaction(coinbaseTransaction);
				// Here need do more job to analysis if exist a better way
				changeLog.setCoinbaseTransaction(coinbaseTransaction);
			}
		}
		
		// Handle every pending Transaction
		for (Transaction transaction : transactionList) {
			// Add Transaction into EQcoinSeed
			if ((getNewTransactionListLength()
					+ transaction.getBytes().length) <= Util.MAX_EQCHIVE_SIZE) {
				Log.info("Add new Transaction which Passport ID: " + transaction.getTxIn().getPassportId() + " Nonce: " + transaction.getNonce());
				
				try {
					// Init Transaction
					transaction.init(changeLog);
					
					// Check if Transaction is sanity and valid then planting
					if (!transaction.planting()) {
						EQCHiveH2.getInstance().deleteTransactionInPool(transaction);
						Log.Error("Transaction is invalid planting failed: " + transaction);
						continue;
					}
					
				}
				catch (Exception e) {
					EQCHiveH2.getInstance().deleteTransactionInPool(transaction);
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
		
		// Deposit txFee
		if(changeLog.getTxFee().compareTo(Value.ZERO) > 0) {
			Log.info("Current EQCHive's txFee is: " + changeLog.getTxFee());
			Value minerTxFee = changeLog.getTxFee().multiply(Value.valueOf(5)).divide(Value.valueOf(100));
			Value eqCoinFederalTxFee = changeLog.getTxFee().subtract(minerTxFee);
			Passport eqCoinFederalPassport = changeLog.getFilter().getPassport(ID.ZERO, true);
			Passport minerPassport = null;
			if(changeLog.getCoinbaseTransaction() instanceof TransferCoinbaseTransaction) {
				TransferCoinbaseTransaction transferCoinbaseTransaction = (TransferCoinbaseTransaction) changeLog.getCoinbaseTransaction();
				minerPassport = changeLog.getFilter().getPassport(transferCoinbaseTransaction.getEqCoinMinerTxOut().getPassportId(), true);
			}
			else {
				ZionCoinbaseTransaction zionCoinbaseTransaction = (ZionCoinbaseTransaction) changeLog.getCoinbaseTransaction();
				// Here may need do more job
				ID minerLockId = changeLog.getFilter().isLockExists(zionCoinbaseTransaction.getEqCoinMinerTxOut().getLock(), true);
				minerPassport = changeLog.getFilter().getPassportFromLockId(minerLockId);
			}
			eqCoinFederalPassport.deposit(eqCoinFederalTxFee);
			minerPassport.deposit(minerTxFee);
			eqCoinFederalPassport.setChangeLog(changeLog).planting();
			minerPassport.setChangeLog(changeLog).planting();
		}
		
		// Planting new EQcoinSeed finished just check if relevant Statistics data is valid
		changeLog.getStatistics().generateStatistics(this);
		if (!changeLog.getStatistics().isValid(this)) {
			Log.Error("Statistics is invalid!");
			throw new IllegalStateException("Statistics is invalid!");
		}

		// Generate relevant root
		changeLog.buildProofBase();
		changeLog.generateProofRoot();
		
		// Update EQcoinSeedRoot
		EQcoinSeedRoot eQcoinSeedRoot = (EQcoinSeedRoot) eqcSeedRoot;
		eQcoinSeedRoot.setTotalTransactionNumbers(changeLog.getStatistics().getTotalTransactionNumbers());
		eQcoinSeedRoot.setTotalSupply(changeLog.getStatistics().getTotalSupply());
		eQcoinSeedRoot.setTotalLockNumbers(changeLog.getTotalLockNumbers());
		eQcoinSeedRoot.setTotalPassportNumbers(changeLog.getTotalPassportNumbers());
		eQcoinSeedRoot.setPassportProofRoot(changeLog.getPassportProofRoot());
		eQcoinSeedRoot.setForbiddenLockProofRoot(changeLog.getForbiddenLockProofRoot());
		
	}
	
	public boolean saveTransactions() throws Exception {
		return true;
	}
	
	public byte[] getProof() throws Exception {
		Vector<byte[]> vector = new Vector<>();
		vector.add(eqcSeedRoot.getBytes());
		vector.add(getNewTransactionListProofRoot());
		return Util.getMerkleTreeRoot(vector, true);
	}

}