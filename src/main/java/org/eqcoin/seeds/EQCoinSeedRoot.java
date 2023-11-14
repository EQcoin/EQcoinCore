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
//package org.eqcoin.seeds;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.util.Arrays;
//
//import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.noneDSA;
//import org.eqcoin.changelog.ChangeLog;
//import org.eqcoin.serialization.EQCSerializable;
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.transaction.Transaction;
//import org.eqcoin.transaction.TransferCoinbaseTransaction;
//import org.eqcoin.transaction.Transaction.TransactionShape;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//import org.eqcoin.util.Util;
//import org.eqcoin.util.Value;
//
///**
// * @author Xun Wang
// * @date July 30, 2019
// * @email 10509759@qq.com
// */
//@Deprecated
//public class EQCoinSeedRoot extends EQCSerializable {
//	private Value totalSupply;
//	/**
//	 * Calculate this according to newHelixList ARRAY's length which equal to the
//	 * new Locks in ZionTransaction and UpdateLockOP
//	 */
//	private ID totalLockNumbers;
//	/**
//	 * Calculate this according to ZionTransaction.
//	 */
//	private ID totalPassportNumbers;
//	/**
//	 * Calculate this according to newTransactionList ARRAY's length
//	 */
//	private ID totalTransactionNumbers;
//	private Transaction coinbaseTransaction;
//	
//	private ChangeLog changeLog;
//	
//	
//	public EQCoinSeedRoot(byte[] bytes) throws Exception {
//		super(bytes);
//	}
//	
//	public EQCoinSeedRoot(ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//
//	public EQCoinSeedRoot() {
//		super();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#parseBody(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parse(ByteArrayInputStream is) throws Exception {
//		totalSupply = EQCType.parseValue(is);
//		totalLockNumbers = EQCType.parseID(is);
//		totalPassportNumbers = EQCType.parseID(is);
//		totalTransactionNumbers = EQCType.parseID(is);
//		if(totalSupply.compareTo(Util.MAX_EQC) <= 0) {
//			coinbaseTransaction = new Transaction().Parse(is);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#getBodyBytes()
//	 */
//	@Override
//	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
//		os.write(totalSupply.getEQCBits());
//		os.write(totalLockNumbers.getEQCBits());
//		os.write(totalPassportNumbers.getEQCBits());
//		os.write(totalTransactionNumbers.getEQCBits());
//		if(totalSupply.compareTo(Util.MAX_EQC) <= 0) {
//			os.write(coinbaseTransaction.getBytes());
//		}
//		return os;
//	}
//	
//	/**
//	 * @return the totalTransactionNumbers
//	 */
//	public ID getTotalTransactionNumbers() {
//		return totalTransactionNumbers;
//	}
//
//	/**
//	 * @param totalTransactionNumbers the totalTransactionNumbers to set
//	 */
//	public void setTotalTransactionNumbers(ID totalTransactionNumbers) {
//		this.totalTransactionNumbers = totalTransactionNumbers;
//	}
//
//	/**
//	 * @return the totalPassportNumbers
//	 */
//	public ID getTotalPassportNumbers() {
//		return totalPassportNumbers;
//	}
//
//	/**
//	 * @param totalPassportNumbers the totalPassportNumbers to set
//	 */
//	public void setTotalPassportNumbers(ID totalPassportNumbers) {
//		this.totalPassportNumbers = totalPassportNumbers;
//	}
//
//	/**
//	 * @return the coinbaseTransaction
//	 */
//	public Transaction getCoinbaseTransaction() {
//		return coinbaseTransaction;
//	}
//
//	/**
//	 * @param coinbaseTransaction the coinbaseTransaction to set
//	 */
//	public void setCoinbaseTransaction(Transaction coinbaseTransaction) {
//		this.coinbaseTransaction = coinbaseTransaction;
//	}
//
//	public String toInnerJson() {
//		return "\"EQcoinSeedRoot\":" + "{\n"
//				+ "\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\"" + ",\n"
//				+ "\"TotalSupply\":" + "\"" + totalSupply.longValue() + "\"" + ",\n" 
//				+ "\"TotalLockNumbers\":" + "\"" + totalLockNumbers.longValue() + "\"" + ",\n" 
//				+ "\"TotalPassportNumbers\":" + "\"" + totalPassportNumbers.longValue() + "\"" + ",\n" 
//				+ coinbaseTransaction.toInnerJson()
//				+ "\n" + "}";
//	}
//	
//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isSanity()
//	 */
//	@Override
//	public boolean isSanity() throws Exception {
//		// Here need check if total supply <= MaxSupply should have coinbase otherwise will haven't coinbase
//		if(totalSupply == null) {
//			Log.Error("totalSupply == null");
//			return false;
//		}
//		if(!totalSupply.isSanity()) {
//			Log.Error("!totalSupply.isSanity()");
//			return false;
//		}
//		if(totalLockNumbers == null) {
//			Log.Error("totalLockNumbers == null");
//			return false;
//		}
//		if(!totalLockNumbers.isSanity()) {
//			Log.Error("!totalLockNumbers.isSanity()");
//			return false;
//		}
//		if(totalPassportNumbers == null) {
//			Log.Error("totalPassportNumbers == null");
//			return false;
//		}
//		if(!totalPassportNumbers.isSanity()) {
//			Log.Error("!totalPassportNumbers.isSanity()");
//			return false;
//		}
//		if(totalTransactionNumbers == null) {
//			Log.Error("totalTransactionNumbers == null");
//			return false;
//		}
//		if(!totalTransactionNumbers.isSanity()) {
//			Log.Error("!totalTransactionNumbers.isSanity()");
//			return false;
//		}
//		if(totalSupply.compareTo(Util.MAX_EQC) <= 0) {
//			if(coinbaseTransaction == null) {
//				Log.Error("coinbaseTransaction == null");
//				return false;
//			}
//			if(!coinbaseTransaction.isSanity()) {
//				Log.Error("!coinbaseTransaction.isSanity()");
//				return false;
//			}
//		}
//		else if(coinbaseTransaction != null) {
//			Log.Error("coinbaseTransaction != null");
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
//	 */
//	@Override
//	public boolean isValid() throws Exception {
//		if(!super.isValid()) {
//			return false;
//		}
////		if(!totalSupply.equals(changeLog.getStatistics().getTotalSupply())) {
////			Log.Error("TotalSupply is invalid expected: " + changeLog.getStatistics().getTotalSupply() + " but actual: " + totalSupply);
////			return false;
////		}
////		if(!totalLockNumbers.equals(changeLog.getTotalLockMateNumbers())) {
////			Log.Error("TotalLockNumbers is invalid expected: " + changeLog.getTotalLockMateNumbers() + " but actual: " + totalLockNumbers);
////			return false;
////		}
////		if(!totalPassportNumbers.equals(changeLog.getTotalPassportNumbers())) {
////			Log.Error("TotalLockNumbers is invalid expected: " + changeLog.getTotalPassportNumbers() + " but actual: " + totalPassportNumbers);
////			return false;
////		}
//		return true;
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
//	 * @param totalLockNumbers the totalLockNumbers to set
//	 */
//	public void setTotalLockNumbers(ID totalLockNumbers) {
//		this.totalLockNumbers = totalLockNumbers;
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
//	 * @param totalSupply the totalSupply to set
//	 */
//	public void setTotalSupply(Value totalSupply) {
//		this.totalSupply = totalSupply;
//	}
//
//	/**
//	 * @param changeLog the changeLog to set
//	 */
//	public void setChangeLog(ChangeLog changeLog) {
//		this.changeLog = changeLog;
//	}
//
//}
