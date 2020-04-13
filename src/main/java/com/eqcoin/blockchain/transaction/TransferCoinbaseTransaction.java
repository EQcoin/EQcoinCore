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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Vector;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

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
		transactionType = TransactionType.TRANSFERCOINBASE;
	}

	public TransferCoinbaseTransaction() {
		super();
	}

	public TransferCoinbaseTransaction(byte[] bytes)
			throws Exception {
		super(bytes);
	}

	public TransferCoinbaseTransaction(ByteArrayInputStream is) throws Exception {
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
		if(!isSanity()) {
			return false;
		}
		if(!nonce.equals(changeLog.getHeight().getNextID())) {
			return false;
		}
		Passport passport = changeLog.getFilter().getPassport(eqCoinFederalTxOut.getPassportId(), false);
		if(passport == null) {
			return false;
		}
		passport = null;
		passport = changeLog.getFilter().getPassport(eqCoinMinerTxOut.getPassportId(), false);
		if(passport == null) {
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
		return transactionType == TransactionType.TRANSFERCOINBASE;
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
		if(eqCoinFederalTxOut == null || !eqCoinFederalTxOut.isSanity() || eqCoinMinerTxOut == null || !eqCoinMinerTxOut.isSanity()) {
			return false;
		}
		if(!eqCoinFederalTxOut.getPassportId().equals(ID.ZERO)) {
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
		passport = changeLog.getFilter().getPassport(eqCoinFederalTxOut.getPassportId(), false);
		passport.deposit(eqCoinFederalTxOut.getValue());
		changeLog.getFilter().savePassport(passport);
		passport = null;
		passport = changeLog.getFilter().getPassport(eqCoinMinerTxOut.getPassportId(), false);
		passport.deposit(eqCoinMinerTxOut.getValue());
		changeLog.getFilter().savePassport(passport);
	}
	
	public String toInnerJson() {
		return
		"\"TransferCoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
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
		parseSoloAndTransactionType(is);
		parseNonce(is);
	}

	public void parseBody(ByteArrayInputStream is) throws Exception {
		eqCoinFederalTxOut = new TransferTxOut(is);
		eqCoinMinerTxOut = new TransferTxOut(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getHeaderBytes(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			serializeSoloAndTransactionTypeBytes(os);
			serializeNonce(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(eqCoinFederalTxOut.getBytes());
			os.write(eqCoinMinerTxOut.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
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
	protected boolean isEQCWitnessSanity() {
		return eqcWitness.isNull();
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
	
}
