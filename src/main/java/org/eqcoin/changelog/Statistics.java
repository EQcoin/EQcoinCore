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
//package org.eqcoin.changelog;
//
//import org.eqcoin.lock.LockMate;
//import org.eqcoin.passport.Passport;
//import org.eqcoin.seeds.EQCoinSeedRoot;
//import org.eqcoin.seeds.EQCoinSeeds;
//import org.eqcoin.transaction.Transaction;
//import org.eqcoin.transaction.TransferOPTransaction;
//import org.eqcoin.transaction.TransferTransaction;
//import org.eqcoin.transaction.ZionOPTransaction;
//import org.eqcoin.transaction.ZionTransaction;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//import org.eqcoin.util.Util;
//import org.eqcoin.util.Value;
//
///**
// * @author Xun Wang
// * @date Mar 19, 2020
// * @email 10509759@qq.com
// */
//@Deprecated
//public class Statistics {
//	private Value totalSupply;
//	private ID totalTransactionNumbers;
//	private ID totalPublickeyNumbers;
//	private ChangeLog changeLog;
//	
//	public Statistics(ChangeLog changeLog) {
//		totalTransactionNumbers = ID.ZERO;
//		totalPublickeyNumbers = ID.ZERO;
//		this.changeLog = changeLog;
//	}
//	
//	public boolean isValid(EQCoinSeeds eQcoinSeed) throws Exception {
//		// Check if total new lock numbers equal to total new passport numbers + total new updated Lock numbers
//		EQCoinSeedRoot preEQcoinSeedRoot = null;
//		ID totalNewLockNumbers = null;
//		ID totalNewPassportNumbers = null;
//		ID preTotalTransactionNumbers = null;
//		ID totalLockNumbers = null;
//		ID totalPassportNumbers = null;
//		if(changeLog.getHeight().equals(ID.ZERO)) {
//			totalNewLockNumbers = ID.TWO;
//			totalNewPassportNumbers = ID.TWO;
//			preTotalTransactionNumbers = ID.ZERO;
//			totalLockNumbers = ID.TWO;
//			totalPassportNumbers = ID.TWO;
//		}
//		else {
//			preEQcoinSeedRoot = Util.GS().getEQCoinSeedRoot(changeLog.getHeight().getPreviousID()); 
//			totalNewLockNumbers = changeLog.getTotalLockNumbers().subtract(preEQcoinSeedRoot.getTotalLockNumbers());
//			totalNewPassportNumbers = changeLog.getTotalPassportNumbers().subtract(preEQcoinSeedRoot.getTotalPassportNumbers());
//			preTotalTransactionNumbers = preEQcoinSeedRoot.getTotalTransactionNumbers();
//			totalLockNumbers = Util.GS().getTotalLockNumbers(changeLog.getFilter().getMode());
//			totalPassportNumbers = Util.GS().getTotalPassportNumbers(changeLog.getFilter().getMode());
//		}
//
//		// 20200530 here need do more job
////		if(!totalNewLockNumbers.equals(totalNewPassportNumbers.add(new ID(changeLog.getForbiddenLockList().size())))) {
////			Log.Error("TotalNewLockNumbers doesn't equal to totalNewPassportNumbers + totalNewUpdateLockNumbers. This is invalid.");
////			return false;
////		}
//		
//		// Check if total supply is valid
//		if(!totalSupply.equals(Util.cypherTotalSupply(changeLog))) {
//			Log.Error("TotalSupply is invalid expected: " + Util.cypherTotalSupply(changeLog) + " but actual: " + totalSupply);
//			return false;
//		}
//		
//		// Check if total transaction numbers is valid
//		if(!totalTransactionNumbers.equals(preTotalTransactionNumbers.add(new ID(eQcoinSeed.getNewTransactionList().size())))) {
//			Log.Error("TotalTransactionNumbers is invalid expected: " + totalTransactionNumbers + " but actual: " + preTotalTransactionNumbers.add(new ID(eQcoinSeed.getNewTransactionList().size())));
//			return false;
//		}
//		
//		// Check if total lock numbers is valid
//		if(!changeLog.getTotalLockNumbers().equals(totalLockNumbers)) {
//			Log.Error("Total lock numbers is invalid expected: " + changeLog.getTotalLockNumbers() + " but actual: " + totalLockNumbers);
//			return false;
//		}
//		
//		// Check if total passport numbers is valid
//		if(!changeLog.getTotalPassportNumbers().equals(totalPassportNumbers)) {
//			Log.Error("Total passport numbers is invalid expected: " + changeLog.getTotalPassportNumbers() + " but actual: " + totalPassportNumbers);
//			return false;
//		}
//		
//		return true;
//	}
//	
//	public void update(Passport passport) {
//		if(totalSupply == null) {
//			totalSupply = new Value(passport.getBalance());
//		}
//		else {
//			totalSupply = totalSupply.add(passport.getBalance());
//		}
//		totalTransactionNumbers = totalTransactionNumbers.add(passport.getNonce());
//	}
//	
//	/**
//	 * @return the totalSupply
//	 */
//	public Value getTotalSupply() {
//		return totalSupply;
//	}
//
//	/**
//	 * @return the totalTransactionNumbers
//	 */
//	public ID getTotalTransactionNumbers() {
//		return totalTransactionNumbers;
//	}
//
//	public void generateStatistics(EQCoinSeeds eQcoinSeed) throws Exception {
//		Passport passport;
//		for(long i=0; i<changeLog.getTotalPassportNumbers().longValue(); ++i) {
//			passport = changeLog.getFilter().getPassport(new ID(i), true);
//			Log.info("Hive height: " + changeLog.getHeight() + " Passport id: " + passport.getId() + " balance: " + passport.getBalance());
//			update(passport);
//		}
//	}
//	
//}
