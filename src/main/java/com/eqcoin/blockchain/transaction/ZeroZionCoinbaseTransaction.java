/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

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

	public ZeroZionCoinbaseTransaction(byte[] bytes)
			throws Exception {
		super(bytes);
	}
	
	public ZeroZionCoinbaseTransaction(ByteArrayInputStream is)
			throws Exception {
		super(is);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#isValid(com.eqzip.eqcoin.blockchain.
	 * AccountsMerkleTree)
	 */
	@Override
	public boolean isValid() throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		ID lockId = null;
		if(!nonce.equals(changeLog.getHeight().getNextID())) {
			return false;
		}
		lockId = changeLog.getFilter().isLockExists(eqCoinFederalTxOut.getLock(), true);
		if(lockId != null) {
			Log.Error("EQcoinFederal Coinbase Passport relevant Lock shouldn't exists.");
			return false;
		}
		lockId = changeLog.getFilter().isLockExists(eqCoinMinerTxOut.getLock(), true);
		if(lockId != null) {
			Log.Error("Miner Coinbase Passport relevant Lock shouldn't exists.");
			return false;
		}
		Value minerCoinbaseReward = Util.getCurrentMinerCoinbaseReward(Util.getCurrentCoinbaseReward(changeLog));
		if(!eqCoinFederalTxOut.getValue().equals(Util.getCurrentCoinbaseReward(changeLog).subtract(minerCoinbaseReward))) {
			return false;
		}
		if(!eqCoinMinerTxOut.getValue().equals(minerCoinbaseReward)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		return (transactionType!= null && transactionType == TransactionType.ZEROZIONCOINBASE);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTxInSanity()
	 */
	@Override
	protected boolean isTxInSanity() {
		return txIn == null;
	}

	@Override
	public boolean isDerivedSanity() throws Exception {
		return (eqCoinFederalTxOut != null && eqCoinFederalTxOut instanceof ZionTxOut && eqCoinFederalTxOut.isSanity()
				&& eqCoinFederalTxOut.getValue().compareTo(Util.MIN_EQC) >= 0 && eqCoinMinerTxOut != null
				&& eqCoinMinerTxOut instanceof ZionTxOut && eqCoinMinerTxOut.isSanity()
				&& eqCoinMinerTxOut.getValue().compareTo(Util.MIN_EQC) >= 0 && !eqCoinFederalTxOut.getLock().equals(eqCoinMinerTxOut.getLock()));
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		Passport passport = null;
		EQCLockMate lock = null;
		// Planting CoinbaseTransaction's relevant Passport
		// Planting EQcoin Federal's Passport
		lock = new EQCLockMate();
		lock.setLock(eqCoinFederalTxOut.getLock());
		lock.setId(changeLog.getNextLockId());
		EQcoinRootPassport eqcFederalPassport = new EQcoinRootPassport();
		eqcFederalPassport.setId(changeLog.getNextPassportId());
		eqcFederalPassport.setLockID(lock.getId());
		eqcFederalPassport.setBlockInterval(Util.BASIC_BLOCK_INTERVAL.divide(Util.TARGET_BLOCK_INTERVAL).byteValue());
		eqcFederalPassport.setTxFeeRate((byte) Util.DEFAULT_TXFEE_RATE);
		eqcFederalPassport.setCheckPointHeight(ID.ZERO);
		eqcFederalPassport.setCheckPointHash(Util.MAGIC_HASH);
		lock.setPassportId(eqcFederalPassport.getId());
		changeLog.getFilter().saveLock(lock);
		eqcFederalPassport.deposit(eqCoinFederalTxOut.getValue());
		changeLog.getFilter().savePassport(eqcFederalPassport);
		// Planting Miner's Passport
		lock = null;
		lock = new EQCLockMate();
		lock.setLock(eqCoinMinerTxOut.getLock());
		lock.setId(changeLog.getNextLockId());
		passport = null;
		passport = new AssetPassport();
		passport.setId(changeLog.getNextPassportId());
		passport.setLockID(lock.getId());
		lock.setPassportId(passport.getId());
		changeLog.getFilter().saveLock(lock);
		passport.deposit(eqCoinMinerTxOut.getValue());
		changeLog.getFilter().savePassport(passport);
	}
	
	public String toInnerJson() {
		return
		"\"ZeroZionCoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"EQcoinFederalTxOut\":" + "\n" + eqCoinFederalTxOut.toInnerJson() + "\n,"
		+ "\"EQcoinMinerTxOut\":" + "\n" + eqCoinMinerTxOut.toInnerJson() + "\n,"
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	@Override
	public Value getBillingLength() {
		return Value.ZERO;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseHeader(java.io.ByteArrayInputStream, com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// Parse Transaction type
		transactionType = TransactionType.get(EQCType.parseID(is).intValue());
		// Parse nonce
		nonce = EQCType.parseID(is);
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
		os.write(EQCType.bigIntegerToEQCBits(nonce));
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
	protected boolean isEQCWitnessSanity() {
		return eqcWitness == null;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#init(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public void init(ChangeLog changeLog) throws Exception {
		this.changeLog = changeLog;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#initPlanting()
	 */
	@Override
	protected void initPlanting() throws Exception {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ZeroZionCoinbaseTransaction [eqCoinFederalTxOut=" + eqCoinFederalTxOut + ", eqCoinMinerTxOut="
				+ eqCoinMinerTxOut + ", transactionType=" + transactionType + ", nonce=" + nonce
				+ ", txIn=" + txIn + ", eqcWitness=" + eqcWitness + ", operationList=" + operation + ", changeLog="
				+ changeLog + ", txInPassport=" + txInPassport + ", txInLock=" + txInLockMate + ", transactionShape="
				+ transactionShape + ", txFeeRate=" + txFeeRate + ", lockType=" + lockType + "]";
	}
	
}
