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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.LockMate;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.AssetPassport;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.txout.ZionTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 30, 2020
 * @email 10509759@qq.com
 */
public class ZeroZionCoinbaseTransaction extends Transaction {
	private ZionTxOut eqCoinFederalTxOut;
	private ZionTxOut eqCoinMinerTxOut;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.ZionTransaction#init()
	 */
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
		transactionType = TransactionType.ZEROZIONCOINBASE;
	}

	public ZeroZionCoinbaseTransaction() {
		super();
	}

	public ZeroZionCoinbaseTransaction(ByteArrayInputStream is)
			throws Exception {
		super(is);
	}

	@Override
	public boolean isValid() throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		ID lockId = null;
		if(!nonce.equals(ID.ONE)) {
			Log.Error("ZeroZionCoinbaseTransaction's nonce should be the one");
			return false;
		}
		lockId = eqcHive.getGlobalState().isLockMateExists(eqCoinFederalTxOut.getLock());
		if(lockId != null) {
			Log.Error("EQcoinFederal Coinbase Passport relevant Lock shouldn't exists.");
			return false;
		}
		lockId = eqcHive.getGlobalState().isLockMateExists(eqCoinMinerTxOut.getLock());
		if(lockId != null) {
			Log.Error("Miner Coinbase Passport relevant Lock shouldn't exists.");
			return false;
		}
		Value minerCoinbaseReward = Util.getCurrentMinerCoinbaseReward(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()));
		if(!eqCoinFederalTxOut.getValue().equals(Util.getCurrentCoinbaseReward(eqcHive.getGlobalState()).subtract(minerCoinbaseReward))) {
			Log.Error("EQcoinFederal's coinbase reward is invalid");
			return false;
		}
		if(!eqCoinMinerTxOut.getValue().equals(minerCoinbaseReward)) {
			Log.Error("EQcoinMiner's coinbase reward is invalid");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		if(transactionType == null) {
			Log.Error("transactionType == null");
			return false;
		}
		if(transactionType != TransactionType.ZEROZIONCOINBASE) {
			Log.Error("transactionType != TransactionType.ZEROZIONCOINBASE");
			return false;
		}
		return true;
	}
	
	@Override
	protected boolean isStatusSanity() {
		if(priority != null) {
			Log.Error("priority != null");
			return false;
		}
		if(lockType != null) {
			Log.Error("lockType != null");
			return false;
		}
		return true;
	}

	@Override
	public boolean isDerivedSanity() throws Exception {
		if(eqCoinFederalTxOut == null) {
			Log.Error("eqCoinFederalTxOut == null");
			return false;
		}
		if(!(eqCoinFederalTxOut instanceof ZionTxOut)) {
			Log.Error("!(eqCoinFederalTxOut instanceof ZionTxOut)");
			return false;
		}
		if(!eqCoinFederalTxOut.isSanity()) {
			Log.Error("!eqCoinFederalTxOut.isSanity()");
			return false;
		}
		if(!(eqCoinFederalTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)) {
			Log.Error("!(eqCoinFederalTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)");
			return false;
		}
		if(eqCoinMinerTxOut == null) {
			Log.Error("eqCoinMinerTxOut == null");
			return false;
		}
		if(!(eqCoinMinerTxOut instanceof ZionTxOut)) {
			Log.Error("!(eqCoinMinerTxOut instanceof ZionTxOut)");
			return false;
		}
		if(!eqCoinMinerTxOut.isSanity()) {
			Log.Error("!eqCoinMinerTxOut.isSanity()");
			return false;
		}
		if(!(eqCoinMinerTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)) {
			Log.Error("!(eqCoinMinerTxOut.getValue().compareTo(Util.MIN_BALANCE) >= 0)");
			return false;
		}
		if(eqCoinFederalTxOut.getLock().equals(eqCoinMinerTxOut.getLock())) {
			Log.Error("eqCoinFederalTxOut.getEQCLock().equals(eqCoinMinerTxOut.getEQCLock())");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		Passport passport = null;
		LockMate lockMate = null;
		// Planting CoinbaseTransaction's relevant Passport
		// Planting EQcoin Federal's Passport
		lockMate = new LockMate();
		lockMate.setLock(eqCoinFederalTxOut.getLock());
		lockMate.setId(ID.ZERO);
		lockMate.setEQCHive(eqcHive).planting();
//		EQcoinRootPassport eQcoinRootPassport = new EQcoinRootPassport();
//		eQcoinRootPassport.setId(ID.ZERO);
//		eQcoinRootPassport.setLockID(lockMate.getId());
//		eQcoinRootPassport.setProtocolVersion(Util.PROTOCOL_VERSION);
//		eQcoinRootPassport.setBlockInterval(Util.BASIC_EQCHIVE_INTERVAL.divide(Util.TARGET_EQCHIVE_INTERVAL).byteValue());
//		eQcoinRootPassport.setTxFeeRate((byte) Util.DEFAULT_POWER_PRICE);
//		eQcoinRootPassport.setCheckPointHeight(ID.ZERO);
//		eQcoinRootPassport.setCheckPointHash(Util.MAGIC_HASH);
//		eQcoinRootPassport.deposit(eqCoinFederalTxOut.getValue());
//		eQcoinRootPassport.getStorage().addStateVariable(new UpdateHeight());
//		eQcoinRootPassport.setEQCHive(eqcHive).planting();
		// Planting Miner's Passport
		lockMate = null;
		lockMate = new LockMate();
		lockMate.setLock(eqCoinMinerTxOut.getLock());
		lockMate.setId(eqcHive.getGlobalState().getLastLockMateId().getNextID());
		lockMate.setEQCHive(eqcHive).planting();
		passport = null;
		passport = new AssetPassport();
		passport.setId(eqcHive.getGlobalState().getLastPassportId().getNextID());
		passport.setLockID(lockMate.getId());
		passport.deposit(eqCoinMinerTxOut.getValue());
		passport.setEQCHive(eqcHive).planting();
	}
	
	public String toInnerJson() {
		return
		"\"ZeroZionCoinbaseTransaction\":"
		+ "\"EQcoinFederalTxOut\":" + "\n" + eqCoinFederalTxOut.toInnerJson() + "\n,"
		+ "\"EQcoinMinerTxOut\":" + "\n" + eqCoinMinerTxOut.toInnerJson() + "\n,"
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseHeader(java.io.ByteArrayInputStream, com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// Parse Transaction type
		transactionType = TransactionType.get(EQCCastle.parseID(is).intValue());
		// Parse nonce
		nonce = EQCCastle.parseID(is);
	}

	public void parseBody(ByteArrayInputStream is) throws Exception {
		eqCoinFederalTxOut = new ZionTxOut(is);
		eqCoinMinerTxOut = new ZionTxOut(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getHeaderBytes(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
		os.write(transactionType.getEQCBits());
		// Serialization nonce
		os.write(EQCCastle.bigIntegerToEQCBits(nonce));
		return os;
	}
	
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization TxOut
		os.write(eqCoinFederalTxOut.getBytes());
		os.write(eqCoinMinerTxOut.getBytes());
		return os;
	}

	/**
	 * @return the eqCoinFederalTxOut
	 */
	public ZionTxOut getEqCoinFederalTxOut() {
		return eqCoinFederalTxOut;
	}

	/**
	 * @param eqCoinFederalTxOut the eqCoinFederalTxOut to set
	 */
	public void setEqCoinFederalTxOut(ZionTxOut eqCoinFederalTxOut) {
		this.eqCoinFederalTxOut = eqCoinFederalTxOut;
	}

	/**
	 * @return the eqCoinMinerTxOut
	 */
	public ZionTxOut getEqCoinMinerTxOut() {
		return eqCoinMinerTxOut;
	}

	/**
	 * @param eqCoinMinerTxOut the eqCoinMinerTxOut to set
	 */
	public void setEqCoinMinerTxOut(ZionTxOut eqCoinMinerTxOut) {
		this.eqCoinMinerTxOut = eqCoinMinerTxOut;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isEQCWitnessSanity()
	 */
	@Override
	protected boolean isWitnessSanity() {
		if(witness != null) {
			Log.Error("eqcWitness != null");
			return false;
		}
		return true;
	}

	@Override
	public void init(EQCHive eqcHive) throws Exception {
		this.eqcHive = eqcHive;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#initPlanting()
	 */
	@Override
	protected boolean isMeetPreCondition() throws Exception {
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ZeroZionCoinbaseTransaction [eqCoinFederalTxOut=" + eqCoinFederalTxOut + ", eqCoinMinerTxOut="
				+ eqCoinMinerTxOut + ", transactionType=" + transactionType + ", nonce=" + nonce
				+ statusInnerJson() + ", eqcWitness=" + witness + ", operationList=" + operation + "]";
	}
	
}
