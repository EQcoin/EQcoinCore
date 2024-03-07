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
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 21, 2019
 * @email 10509759@qq.com
 */
public class TransferCoinbaseTransaction extends Transaction {
	private TransferTxOut eqCoinFederalTxOut;
	private TransferTxOut eqCoinMinerTxOut;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#init()
	 */
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		super.init();
//		transactionType = TransactionType.TRANSFERCOINBASE;
	}

	public TransferCoinbaseTransaction() {
		super();
	}

	public TransferCoinbaseTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public boolean isValid() throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if(eqCoinMinerTxOut.getPassportId().compareTo(eqcHive.getPreRoot().getTotalPassportNumbers()) >= 0) {
			Log.Error("No confirmed passport id can't use: " + eqCoinFederalTxOut.getPassportId());
			return false;
		}
		if(!nonce.equals(eqcHive.getRoot().getHeight().getNextID())) {
			Log.Error("!nonce.equals(changeLog.getHeight().getNextID())");
			return false;
		}
		Passport passport = eqcHive.getGlobalState().getPassport(eqCoinFederalTxOut.getPassportId());
		if(passport == null) {
			Log.Error("EQcoinFederal's passport == null");
			return false;
		}
		passport = null;
		passport = eqcHive.getGlobalState().getPassport(eqCoinMinerTxOut.getPassportId());
		if(passport == null) {
			Log.Error("EQcoinMiner's passport == null");
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
//		if(transactionType != TransactionType.TRANSFERCOINBASE) {
//			Log.Error("transactionType != TransactionType.TRANSFERCOINBASE");
//			return false;
//		}
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
		if(!eqCoinFederalTxOut.isSanity()) {
			Log.Error("!eqCoinFederalTxOut.isSanity()");
			return false;
		}
		if(eqCoinMinerTxOut == null) {
			Log.Error("eqCoinMinerTxOut == null");
			return false;
		}
		if(!eqCoinMinerTxOut.isSanity()) {
			Log.Error("!eqCoinMinerTxOut.isSanity()");
			return false;
		}
		if(!eqCoinFederalTxOut.getPassportId().equals(ID.ZERO)) {
			Log.Error("!eqCoinFederalTxOut.getPassportId().equals(ID.ZERO)");
			return false;
		}
		if (eqCoinFederalTxOut.getPassportId().equals(eqCoinMinerTxOut.getPassportId())) {
			Log.Error("Miner's Lock shouldn't equal to EQcoin Federal's lock");
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
		passport = eqcHive.getGlobalState().getPassport(eqCoinFederalTxOut.getPassportId());
		passport.deposit(eqCoinFederalTxOut.getValue());
		passport.setEQCHive(eqcHive).planting();
		passport = null;
		passport = eqcHive.getGlobalState().getPassport(eqCoinMinerTxOut.getPassportId());
		passport.deposit(eqCoinMinerTxOut.getValue());
		passport.setEQCHive(eqcHive).planting();
	}
	
	public String toInnerJson() {
		return
		"\"TransferCoinbaseTransaction\":" + "\n{\n"
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
//		transactionType = TransactionType.get(EQCCastle.parseID(is).intValue());
		// Parse nonce
		nonce = EQCCastle.parseID(is);
	}

	public void parseBody(ByteArrayInputStream is) throws Exception {
		eqCoinFederalTxOut = new TransferTxOut(is);
		eqCoinMinerTxOut = new TransferTxOut(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getHeaderBytes(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Transaction type
//		os.write(transactionType.getEQCBits());
		// Serialization nonce
		os.write(EQCCastle.bigIntegerToEQCBits(nonce));
		return os;
	}
	
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(eqCoinFederalTxOut.getBytes());
		os.write(eqCoinMinerTxOut.getBytes());
		return os;
	}

	/**
	 * @return the eqCoinFederalTxOut
	 */
	public TransferTxOut getEqCoinFederalTxOut() {
		return eqCoinFederalTxOut;
	}

	/**
	 * @param eqCoinFederalTxOut the eqCoinFederalTxOut to set
	 */
	public void setEqCoinFederalTxOut(TransferTxOut eqCoinFederalTxOut) {
		this.eqCoinFederalTxOut = eqCoinFederalTxOut;
	}

	/**
	 * @return the eqCoinMinerTxOut
	 */
	public TransferTxOut getEqCoinMinerTxOut() {
		return eqCoinMinerTxOut;
	}

	/**
	 * @param eqCoinMinerTxOut the eqCoinMinerTxOut to set
	 */
	public void setEqCoinMinerTxOut(TransferTxOut eqCoinMinerTxOut) {
		this.eqCoinMinerTxOut = eqCoinMinerTxOut;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isEQCWitnessSanity()
	 */
	@Override
	protected boolean isWitnessSanity() {
		return witness == null;
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
	
}
