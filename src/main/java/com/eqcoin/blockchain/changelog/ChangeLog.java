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
package com.eqcoin.blockchain.changelog;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.seed.EQcoinSeedRoot;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.ZionOPTransaction;
import com.eqcoin.blockchain.transaction.ZionTransaction;
import com.eqcoin.crypto.MerkleTree;
import com.eqcoin.persistence.EQCBlockChain;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * ChangeLog store the changed Lock&Passport's new state during accounting or
 * verify new block and provide the unified access to Lock&Passport. If the Lock
 * or Passport has changed in the new block just retrieve it from ChangeLog
 * otherwise from the relevant global state database or snapshot. ChangeLog also
 * provide the service to generate PassportMerkleTreeRoot and LockMerkleTreeRoot.
 * 
 * The height of PassportsMerkleTree is previous EQCBlock's height when build
 * new block or verify new block. If create from the early height need fill all
 * the relevant Passport's Snapshot(In current height) from H2 in Filter.
 * 
 * @author Xun Wang
 * @date Mar 11, 2019
 * @email 10509759@qq.com
 */
public class ChangeLog {
	/**
	 * Current EQCHive's height which is the base for the new EQCHive
	 */
	private ID height;
	
	private Vector<byte[]> livelyLockBaseList;
	private byte[] livelyLockProofRoot;
	private ID totalLockNumbers;
	private ID previousTotalLockNumbers;
	
	private Vector<byte[]> passportBaseList;
	private byte[] passportProofRoot;
	private ID totalPassportNumbers;
	private ID previousTotalPassportNumbers;
	
	private ID totalNewPassportNumbers;
	private Vector<EQCLockMate> forbiddenLockList;
	private Vector<byte[]> forbiddenLockBaseList;
	private byte[] forbiddenLockProofRoot;
	
	private Filter filter;
	private EQcoinSeedRoot preEQcoinSeedRoot;
	private Transaction coinbaseTransaction;
	private Statistics statistics;
	private Value txFeeRate;

	public ChangeLog(ID height, Filter filter) throws Exception {
		super();
		EQCHive previousEQCHive = null;
		
		statistics = new Statistics(this);
		totalNewPassportNumbers = ID.ZERO;
		filter.setChangeLog(this);
		this.height = height;
		// When recoverySingularityStatus the No.0 EQCHive doesn't exist so here need special operation
//		if (height.equals(ID.ZERO)) {
//			try {
//				eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE, height);
//			}
//			catch (Exception e) {
//				Log.info("Height is zero and Account No.1 doesn't exists: " + e.getMessage());
//			}
//		} else {
//			totalAccountNumbers = Util.DB().getTotalAccountNumbers(height.getPreviousID());
//		}
		
		if(height.equals(ID.ZERO)) {
			previousTotalLockNumbers = ID.ZERO;
			previousTotalPassportNumbers = ID.ZERO;
			txFeeRate = new Value(Util.DEFAULT_TXFEE_RATE);
		}
		else {
			preEQcoinSeedRoot = Util.DB().getEQcoinSeedRoot(height.getPreviousID());
			// Here exists one bug prevous total supply also need retrieve from previous EQCHive
			previousTotalLockNumbers = preEQcoinSeedRoot.getTotalLockNumbers();
			previousTotalPassportNumbers = preEQcoinSeedRoot.getTotalPassportNumbers();
			EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) filter.getPassport(ID.ZERO, false);
			txFeeRate = new Value(eQcoinRootPassport.getTxFeeRate());
		}
		
		totalLockNumbers = previousTotalLockNumbers;
		totalPassportNumbers = previousTotalPassportNumbers;
		this.filter = filter;
		passportBaseList = new Vector<>();
		livelyLockBaseList = new Vector<>();
		forbiddenLockList = new Vector<>();
		forbiddenLockBaseList = new Vector<>();
		forbiddenLockProofRoot = EQCType.NULL_ARRAY;
	}
	
	/**
	 * Check if Passport exists according to Lock's AddressAI.
	 * <p>
	 * When check TxIn Account doesn't need searching in filter just set isFiltering to false
	 * Check if TxIn Account exists in EQC blockchain's Accounts table
	 * and it's create height less than current AccountsMerkleTree's
	 * height.
	 * 
	 * When check TxOut Account need searching in filter just set isFiltering to true
	 * Check if TxOut Address exists in Filter table or EQC blockchain's Accounts table
	 * and it's create height less than current AccountsMerkleTree's
	 * height.
	 * 
	 * @param lock
	 * @param isFiltering When need searching in Filter table just set it to true
	 * @return true if Account exists
	 * @throws Exception 
	 */
//	public synchronized boolean isPassportExists(Lock lock, boolean isFiltering) throws Exception {
//		boolean isExists = false;
//		if(isFiltering && filter.isAccountExists(lock)) {
//			isExists = true;
//		}
//		else {
//			Passport passport = Util.DB().getPassport(lock.getAddressAI(), Mode.GLOBAL);
////			if(passport != null && passport.getCreateHeight().compareTo(height) < 0 && passport.getLockCreateHeight().compareTo(height) < 0 && passport.getId().compareTo(previousTotalPassportNumbers) <= 0) {
////				isExists = true;
////			}
//			if(passport != null && passport.getId().compareTo(previousTotalPassportNumbers) <= 0) {
//				isExists = true;
//			}
//		}
//		return  isExists;
//	}
	
//	public synchronized Passport getPassport(ID id, boolean isFiltering) throws Exception {
////		EQCType.assertNotBigger(id, previousTotalAccountNumbers); // here need do more job to determine if need this check
//		return filter.getPassport(id, isFiltering);
//	}
	
//	public synchronized Passport getPassport(Lock key, boolean isFiltering) throws Exception {
//		return filter.getPassport(key, isFiltering);
//	}
	
	/**
	 * Save current Passport in Filter
	 * @param passport
	 * @return true if save successful
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
//	public synchronized void savePassport(Passport account) throws ClassNotFoundException, SQLException, Exception {
////		Log.info(account.toString());
//		filter.savePassport(account);
//	}
	
	/**
	 * @return the totalPassportNumber
	 */
	public synchronized ID getTotalPassportNumbers() {
		return totalPassportNumbers;
	}
	
	/**
	 * Get current EQCHive's height
	 * 
	 * @return the height
	 */
	public synchronized ID getHeight() {
 		return height;
	}

	/**
	 * Set current EQCHive's height
	 * 
	 * @param height the height to set
	 */
	public synchronized void setHeight(ID height) {
		this.height = height;
	}

	public void buildProofBase() throws Exception {
 		buildPassportAndLivelyLockProofBase();
		buildForbiddenLockProofBase();
	}
	
	public void buildPassportAndLivelyLockProofBase() throws Exception {
		Log.info("Begin buildPassportAndLockProofBase");
		Passport passport = null;
		MerkleTree passportMerkleTree = null;
		Vector<byte[]> passportList = new Vector<>();
		EQCLockMate lock = null;
		MerkleTree livelyLockMerkleTree = null;
		Vector<byte[]> livelyLockList = new Vector<>();
		
		for (long i = 0; i < totalPassportNumbers.longValue(); ++i) {
			// Build passport proof base
			passport = filter.getPassport(new ID(i), true);
			passportList.add(passport.getBytes());
			if((i%Util.KILOBYTE) == 0) {
				passportMerkleTree = new MerkleTree(passportList, true);
				passportMerkleTree.generateRoot();
				passportBaseList.add(passportMerkleTree.getRoot());
				passportMerkleTree = null;
				passportList = new Vector<>();
			}
			// Build lively lock proof base
 			lock = filter.getLock(new ID(passport.getLockID()), true);
			livelyLockList.add(lock.getBytes());
			if((i%Util.KILOBYTE) == 0) {
				livelyLockMerkleTree = new MerkleTree(livelyLockList, true);
				livelyLockMerkleTree.generateRoot();
				livelyLockBaseList.add(livelyLockMerkleTree.getRoot());
				livelyLockMerkleTree = null;
				livelyLockList = new Vector<>();
			}
		}

		passportMerkleTree = new MerkleTree(passportList, true);
		passportMerkleTree.generateRoot();
		passportBaseList.add(passportMerkleTree.getRoot());
		
		livelyLockMerkleTree = new MerkleTree(livelyLockList, true);
		livelyLockMerkleTree.generateRoot();
		livelyLockBaseList.add(livelyLockMerkleTree.getRoot());
		
	}
	
	public void generateLivelyLockAndPassportProofRoot() throws NoSuchAlgorithmException {
		MerkleTree merkleTree = new MerkleTree(livelyLockBaseList, false);
		merkleTree.generateRoot();
		livelyLockProofRoot = merkleTree.getRoot();
		merkleTree = new MerkleTree(passportBaseList, false);
		merkleTree.generateRoot();
		passportProofRoot = merkleTree.getRoot();
	}
	
	public void generateProofRoot() throws NoSuchAlgorithmException {
		generateLivelyLockAndPassportProofRoot();
		generateForbiddenLockProofRoot();
	}
	
	public byte[] getPassportProofRoot() {
		return passportProofRoot;
	}

	public byte[] getLockProofRoot() {
		return livelyLockProofRoot;
	}
	
	public void buildForbiddenLockProofBase() throws Exception {
		Log.info("Begin buildForbiddenLockProofBase");
		EQCLockMate lock = null;
		MerkleTree merkleTree = null;
		Vector<byte[]> forbiddenLockBytesList = new Vector<>();
		
		for (int i = 0; i <forbiddenLockList.size(); ++i) {
			lock = forbiddenLockList.get(i);
			forbiddenLockBytesList.add(lock.getBytes());
			if((i%Util.KILOBYTE) == 0) {
				merkleTree = new MerkleTree(forbiddenLockBytesList, true);
				merkleTree.generateRoot();
				forbiddenLockBaseList.add(merkleTree.getRoot());
				merkleTree = null;
				forbiddenLockBytesList = new Vector<>();
			}
		}
		merkleTree = new MerkleTree(forbiddenLockBytesList, true);
		merkleTree.generateRoot();
		if(merkleTree.getRoot() != null) {
			// Exists forbidden lock just generate it's root
			forbiddenLockBaseList.add(merkleTree.getRoot());
		}
	}
	
	public void generateForbiddenLockProofRoot() throws NoSuchAlgorithmException {
		MerkleTree merkleTree = new MerkleTree(forbiddenLockBaseList, false);
		merkleTree.generateRoot();
		forbiddenLockProofRoot = merkleTree.getRoot();
	}
	
	public byte[] getForbiddenLockProofRoot() {
		return forbiddenLockProofRoot;
	}
	
	public void merge() throws Exception {
		filter.merge();
	}
	
	public void clear() throws ClassNotFoundException, SQLException, Exception {
		filter.clear();
	}
	
//	public ID getPassportID(Passport address) throws ClassNotFoundException, SQLException, Exception {
//		return filter.getPassportID(address);
//	}

	public byte[] getEQCHeaderHash(ID height) throws Exception {
		return Util.DB().getEQCHiveRootProof(height);
	}
	
	public byte[] getEQCHeaderBuddyHash(ID height) throws Exception {
		byte[] hash = null;
		EQCType.assertNotBigger(height, this.height);
		if(height.compareTo(this.height) < 0) {
			hash = Util.DB().getEQCHiveRootProof(height);
		}
		else {
			hash = Util.DB().getEQCHiveRootProof(height.getPreviousID());
		}
		return hash;
	}
	
	public byte[] getEQCHive(ID height, boolean isSegwit) throws Exception {
		return Util.DB().getEQCHive(height);
	}
	
	public void takeSnapshot() throws Exception {
		filter.takeSnapshot();
	}

	/**
	 * @return the filter
	 */
	public Filter getFilter() {
		return filter;
	}
	
	public void updateGlobalState() throws Exception {
		// Save the snapshot of current tail height's changed Accounts from Account Table to Snapshot Table
		takeSnapshot();
		merge();
		clear();
	}
	
	public ID getTotalCoinbaseTransactionNumbers() {
		if (height.compareTo(Util.getMaxCoinbaseHeight(height)) < 0) {
			return height.getNextID();
		}
		else {
			return Util.getMaxCoinbaseHeight(height);
		}
	}

	/**
	 * @return the previousTotalPassportNumbers
	 */
	public ID getPreviousTotalPassportNumbers() {
		return previousTotalPassportNumbers;
	}

	/**
	 * @return the totalLockNumbers
	 */
	public ID getTotalLockNumbers() {
		return totalLockNumbers;
	}

	/**
	 * @return the previousTotalLockNumbers
	 */
	public ID getPreviousTotalLockNumbers() {
		return previousTotalLockNumbers;
	}
	
	public synchronized ID getNextLockId() {
		ID nextLockId = totalLockNumbers;
		totalLockNumbers = totalLockNumbers.getNextID();
		return nextLockId;
	}
	
	public synchronized ID getNextPassportId() {
		ID nextPassportId = totalPassportNumbers;
		totalPassportNumbers = totalPassportNumbers.getNextID();
		return nextPassportId;
	}

	/**
	 * @return the statistics
	 */
	public Statistics getStatistics() {
		return statistics;
	}

	/**
	 * @return the forbiddenLockList
	 */
	public Vector<EQCLockMate> getForbiddenLockList() {
		return forbiddenLockList;
	}

	/**
	 * @param coinbaseTransaction the coinbaseTransaction to set
	 */
	public void setCoinbaseTransaction(Transaction coinbaseTransaction) {
		this.coinbaseTransaction = coinbaseTransaction;
	}

	/**
	 * @return the coinbaseTransaction
	 */
	public Transaction getCoinbaseTransaction() {
		return coinbaseTransaction;
	}

	/**
	 * @return the txFeeRate
	 */
	public Value getTxFeeRate() {
		return txFeeRate;
	}
	
	public EQCBlockChain getDB() throws Exception {
		return Util.DB();
	}
	
}
