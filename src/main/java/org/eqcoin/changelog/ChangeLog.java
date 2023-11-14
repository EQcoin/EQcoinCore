/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
//package org.eqcoin.changelog;
//
//import java.sql.SQLException;
//import java.sql.Savepoint;
//
//import org.eqcoin.hive.EQCHive;
//import org.eqcoin.passport.EQcoinRootPassport;
//import org.eqcoin.persistence.globalstate.GlobalState.Statistics;
//import org.eqcoin.seeds.EQCoinSeedRoot;
//import org.eqcoin.seeds.EQCoinSeeds;
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.transaction.Transaction;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//import org.eqcoin.util.Util;
//import org.eqcoin.util.Value;
//
///**
// * ChangeLog store the changed Lock&Passport's new state during accounting or
// * verify new block and provide the unified access to Lock&Passport. If the Lock
// * or Passport has changed in the new block just retrieve it from ChangeLog
// * otherwise from the relevant global state database or snapshot. ChangeLog also
// * provide the service to generate PassportMerkleTreeRoot and LockMerkleTreeRoot.
// * 
// * The height of PassportsMerkleTree is previous EQCBlock's height when build
// * new block or verify new block. If create from the early height need fill all
// * the relevant Passport's Snapshot(In current height) from H2 in Filter.
// * 
// * @author Xun Wang
// * @date Mar 11, 2019
// * @email 10509759@qq.com
// */
//@Deprecated
//public class ChangeLog {
//	/**
//	 * Current EQCHive's height which is the base for the new EQCHive
//	 */
//	private ID height;
//	private Value totalSupply;
//	private ID previousTotalLockNumbers;
//	private ID totalLockNumbers;
//	private ID previousTotalPassportNumbers;
//	private ID totalPassportNumbers;
//	private Filter filter;
//	private EQCoinSeedRoot preEQcoinSeedRoot;
//	private EQCHive currentEQCHive;
//	private Value txFeeRate;
//
//	public ChangeLog(ID height, Filter filter) throws Exception {
//		super();
//		
////		totalNewPassportNumbers = ID.ZERO;
//		filter.setChangeLog(this);
//		this.height = height;
//		// When recoverySingularityStatus the No.0 EQCHive doesn't exist so here need special operation
////		if (height.equals(ID.ZERO)) {
////			try {
////				eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB().getAccount(ID.ONE, height);
////			}
////			catch (Exception e) {
////				Log.info("Height is zero and Account No.1 doesn't exists: " + e.getMessage());
////			}
////		} else {
////			totalAccountNumbers = Util.DB().getTotalAccountNumbers(height.getPreviousID());
////		}
//		
//		if(height.equals(ID.ZERO)) {
//			previousTotalLockNumbers = ID.ZERO;
//			previousTotalPassportNumbers = ID.ZERO;
//			txFeeRate = new Value(Util.DEFAULT_TXFEE_RATE);
//		}
//		else {
//			preEQcoinSeedRoot = Util.GS().getEQCoinSeedRoot(height.getPreviousID());
//			// Here exists one bug prevous total supply also need retrieve from previous EQCHive
//			previousTotalLockNumbers = preEQcoinSeedRoot.getTotalLockNumbers();
//			previousTotalPassportNumbers = preEQcoinSeedRoot.getTotalPassportNumbers();
//			EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) filter.getPassport(ID.ZERO, false);
//			txFeeRate = new Value(eQcoinRootPassport.getTxFeeRate());
//		}
//		
//		totalLockNumbers = previousTotalLockNumbers;
//		totalPassportNumbers = previousTotalPassportNumbers;
//		totalSupply = Util.cypherTotalSupply(this);
//		this.filter = filter;
//	}
//	
//	/**
//	 * Check if Passport exists according to Lock's AddressAI.
//	 * <p>
//	 * When check TxIn Account doesn't need searching in filter just set isFiltering to false
//	 * Check if TxIn Account exists in EQC blockchain's Accounts table
//	 * and it's create height less than current AccountsMerkleTree's
//	 * height.
//	 * 
//	 * When check TxOut Account need searching in filter just set isFiltering to true
//	 * Check if TxOut Address exists in Filter table or EQC blockchain's Accounts table
//	 * and it's create height less than current AccountsMerkleTree's
//	 * height.
//	 * 
//	 * @param lock
//	 * @param isFiltering When need searching in Filter table just set it to true
//	 * @return true if Account exists
//	 * @throws Exception 
//	 */
////	public synchronized boolean isPassportExists(Lock lock, boolean isFiltering) throws Exception {
////		boolean isExists = false;
////		if(isFiltering && filter.isAccountExists(lock)) {
////			isExists = true;
////		}
////		else {
////			Passport passport = Util.DB().getPassport(lock.getAddressAI(), Mode.GLOBAL);
//////			if(passport != null && passport.getCreateHeight().compareTo(height) < 0 && passport.getLockCreateHeight().compareTo(height) < 0 && passport.getId().compareTo(previousTotalPassportNumbers) <= 0) {
//////				isExists = true;
//////			}
////			if(passport != null && passport.getId().compareTo(previousTotalPassportNumbers) <= 0) {
////				isExists = true;
////			}
////		}
////		return  isExists;
////	}
//	
////	public synchronized Passport getPassport(ID id, boolean isFiltering) throws Exception {
//////		EQCType.assertNotBigger(id, previousTotalAccountNumbers); // here need do more job to determine if need this check
////		return filter.getPassport(id, isFiltering);
////	}
//	
////	public synchronized Passport getPassport(Lock key, boolean isFiltering) throws Exception {
////		return filter.getPassport(key, isFiltering);
////	}
//	
//	/**
//	 * Save current Passport in Filter
//	 * @param passport
//	 * @return true if save successful
//	 * @throws Exception 
//	 * @throws SQLException 
//	 * @throws ClassNotFoundException 
//	 */
////	public synchronized void savePassport(Passport account) throws ClassNotFoundException, SQLException, Exception {
//////		Log.info(account.toString());
////		filter.savePassport(account);
////	}
//	
//	/**
//	 * @return the totalPassportNumber
//	 */
//	public synchronized ID getTotalPassportNumbers() {
//		return totalPassportNumbers;
//	}
//	
//	/**
//	 * Get current EQCHive's height
//	 * 
//	 * @return the height
//	 */
//	public synchronized ID getHeight() {
// 		return height;
//	}
//
//	/**
//	 * Set current EQCHive's height
//	 * 
//	 * @param height the height to set
//	 */
//	public synchronized void setHeight(ID height) {
//		this.height = height;
//	}
//
//	public void merge() throws Exception {
//		filter.merge();
//	}
//	
//	public void clear() throws ClassNotFoundException, SQLException, Exception {
//		filter.clear();
//	}
//	
//	@Deprecated
//	public byte[] getEQCHeaderHash(ID height) throws Exception {
//		return Util.GS().getEQCHiveRootProof(height);
//	}
//	
//	@Deprecated
//	public byte[] getEQCHeaderBuddyHash(ID height) throws Exception {
//		byte[] hash = null;
//		EQCType.assertNotBigger(height, this.height);
//		if(height.compareTo(this.height) < 0) {
//			hash = Util.GS().getEQCHiveRootProof(height);
//		}
//		else {
//			hash = Util.GS().getEQCHiveRootProof(height.getPreviousID());
//		}
//		return hash;
//	}
//	
//	@Deprecated
//	public byte[] getEQCHive(ID height, boolean isSegwit) throws Exception {
//		return Util.GS().getEQCHive(height);
//	}
//	
//	public void takeSnapshot() throws Exception {
//		filter.takeSnapshot();
//	}
//
//	/**
//	 * @return the filter
//	 */
//	public Filter getFilter() {
//		return filter;
//	}
//	
//	public void updateGlobalState(EQCHive eqcHive, Savepoint savepoint) throws Exception {
//		try {
//			Util.GS().saveEQCHive(eqcHive);
//			takeSnapshot();
//			merge();
//			clear();
//			Util.GS().saveEQCHiveTailHeight(eqcHive.getHeight());
//			Util.MC().deleteTransactionsInPool(eqcHive);
//			if(savepoint != null) {
//				Log.info("Begin commit at EQCHive No." + eqcHive.getHeight());
//				Util.GS().getConnection().commit();
//				Log.info("Commit successful at EQCHive No." + eqcHive.getHeight());
//			}
//		} catch (Exception e) {
//			Log.Error("During update global state error occur: " + e + " savepoint: " + savepoint);
//			if (savepoint != null) {
//				Log.info("Begin rollback at EQCHive No." + eqcHive.getHeight());
//				Util.GS().getConnection().rollback(savepoint);
//				Log.info("Rollback successful at EQCHive No." + eqcHive.getHeight());
//			}
//			throw e;
//		}
//		finally {
//			if(savepoint != null) {
//				Util.GS().getConnection().releaseSavepoint(savepoint);
//			}
//		}
//	}
//	
//	/**
//	 * @return the previousTotalPassportNumbers
//	 */
//	public ID getPreviousTotalPassportNumbers() {
//		return previousTotalPassportNumbers;
//	}
//
//	/**
//	 * @return the totalLockNumbers
//	 */
//	public ID getTotalLockNumbers() {
//		return totalLockNumbers;
//	}
//
//	/**
//	 * @return the previousTotalLockNumbers
//	 */
//	public ID getPreviousTotalLockNumbers() {
//		return previousTotalLockNumbers;
//	}
//	
//	public synchronized ID getNextLockId() {
//		ID nextLockId = totalLockNumbers;
//		totalLockNumbers = totalLockNumbers.getNextID();
//		return nextLockId;
//	}
//	
//	public synchronized ID getNextPassportId() {
//		ID nextPassportId = totalPassportNumbers;
//		totalPassportNumbers = totalPassportNumbers.getNextID();
//		return nextPassportId;
//	}
//
//	/**
//	 * @return the txFeeRate
//	 */
//	public Value getTxFeeRate() {
//		return txFeeRate;
//	}
//	
//	/**
//	 * @return the currentEQCHive
//	 */
//	public EQCHive getCurrentEQCHive() {
//		return currentEQCHive;
//	}
//
//	/**
//	 * @param currentEQCHive the currentEQCHive to set
//	 */
//	public void setCurrentEQCHive(EQCHive currentEQCHive) {
//		this.currentEQCHive = currentEQCHive;
//	}
//
//	public boolean isStatisticsValid() throws Exception {
//		// Check if total new lock numbers equal to total new passport numbers + total new updated Lock numbers
//		EQCoinSeedRoot preEQcoinSeedRoot = null;
//		ID totalNewLockNumbers = null;
//		ID totalNewPassportNumbers = null;
//		ID preTotalTransactionNumbers = null;
//		ID totalLockNumbers = null;
//		ID totalPassportNumbers = null;
//		if(height.equals(ID.ZERO)) {
//			totalNewLockNumbers = ID.TWO;
//			totalNewPassportNumbers = ID.TWO;
//			preTotalTransactionNumbers = ID.ZERO;
//			totalLockNumbers = ID.TWO;
//			totalPassportNumbers = ID.TWO;
//		}
//		else {
//			preEQcoinSeedRoot = Util.GS().getEQCoinSeedRoot(height.getPreviousID()); 
//			totalNewLockNumbers = this.totalLockNumbers.subtract(preEQcoinSeedRoot.getTotalLockNumbers());
//			totalNewPassportNumbers = this.totalPassportNumbers.subtract(preEQcoinSeedRoot.getTotalPassportNumbers());
//			preTotalTransactionNumbers = preEQcoinSeedRoot.getTotalTransactionNumbers();
//			totalLockNumbers = Util.GS().getTotalLockNumbers(filter.getMode());
//			totalPassportNumbers = Util.GS().getTotalPassportNumbers(filter.getMode());
//		}
//
//		// 20200530 here need do more job
////		if(!totalNewLockNumbers.equals(totalNewPassportNumbers.add(new ID(changeLog.getForbiddenLockList().size())))) {
////			Log.Error("TotalNewLockNumbers doesn't equal to totalNewPassportNumbers + totalNewUpdateLockNumbers. This is invalid.");
////			return false;
////		}
//		
//		Statistics statistics = Util.GS().getStatistics(filter.getMode());
//		
//		// Check if total supply is valid
//		if(!totalSupply.equals(statistics.getTotalSupply())) {
//			Log.Error("TotalSupply is invalid expected: " + totalSupply + " but actual: " + statistics.getTotalSupply());
//			return false;
//		}
//		
//		// Check if total transaction numbers is valid
//		if(!statistics.getTotalTransactionNumbers().equals(preTotalTransactionNumbers.add(new ID(currentEQCHive.getEQCoinSeeds().getNewTransactionList().size())))) {
//			Log.Error("TotalTransactionNumbers is invalid expected: " + statistics.getTotalTransactionNumbers() + " but actual: " + preTotalTransactionNumbers.add(new ID(currentEQCHive.getEQCoinSeeds().getNewTransactionList().size())));
//			return false;
//		}
//		
//		// Check if total lock numbers is valid
//		if(!this.totalLockNumbers.equals(totalLockNumbers)) {
//			Log.Error("Total lock numbers is invalid expected: " + this.totalLockNumbers + " but actual: " + totalLockNumbers);
//			return false;
//		}
//		
//		// Check if total passport numbers is valid
//		if(!this.totalPassportNumbers.equals(totalPassportNumbers)) {
//			Log.Error("Total passport numbers is invalid expected: " + this.totalPassportNumbers + " but actual: " + totalPassportNumbers);
//			return false;
//		}
//		
//		return true;
//	}
//
//	/**
//	 * @return the totalSupply
//	 */
//	public Value getTotalSupply() {
//		return totalSupply;
//	}
//	
//}
