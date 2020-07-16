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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.lock.LockMate;
import org.eqcoin.passport.AssetPassport;
import org.eqcoin.passport.EQcoinRootPassport;
import org.eqcoin.passport.Passport;
import org.eqcoin.passport.storage.UpdateHeight;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction.TransactionType;
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
			Log.Error("!nonce.equals(changeLog.getHeight().getNextID())");
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
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTxInSanity()
	 */
	@Override
	protected boolean isStatusSanity() {
		if(status != null) {
			Log.Error("status != null");
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
		lockMate.setId(changeLog.getNextLockId());
		lockMate.setChangeLog(changeLog).planting();
		EQcoinRootPassport eqcFederalPassport = new EQcoinRootPassport();
		eqcFederalPassport.setId(changeLog.getNextPassportId());
		eqcFederalPassport.setLockID(lockMate.getId());
		eqcFederalPassport.setProtocolVersion(Util.PROTOCOL_VERSION);
		eqcFederalPassport.setBlockInterval(Util.BASIC_EQCHIVE_INTERVAL.divide(Util.TARGET_EQCHIVE_INTERVAL).byteValue());
		eqcFederalPassport.setTxFeeRate((byte) Util.DEFAULT_TXFEE_RATE);
		eqcFederalPassport.setCheckPointHeight(ID.ZERO);
		eqcFederalPassport.setCheckPointHash(Util.MAGIC_HASH);
		eqcFederalPassport.deposit(eqCoinFederalTxOut.getValue());
		eqcFederalPassport.getStorage().addStateVariable(new UpdateHeight());
		eqcFederalPassport.setChangeLog(changeLog).planting();
		// Planting Miner's Passport
		lockMate = null;
		lockMate = new LockMate();
		lockMate.setLock(eqCoinMinerTxOut.getLock());
		lockMate.setId(changeLog.getNextLockId());
		lockMate.setChangeLog(changeLog).planting();
		passport = null;
		passport = new AssetPassport();
		passport.setId(changeLog.getNextPassportId());
		passport.setLockID(lockMate.getId());
		passport.deposit(eqCoinMinerTxOut.getValue());
		passport.setChangeLog(changeLog).planting();
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
	protected boolean isWitnessSanity() {
		if(witness != null) {
			Log.Error("eqcWitness != null");
			return false;
		}
		return true;
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
