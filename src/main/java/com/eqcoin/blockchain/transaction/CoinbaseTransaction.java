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
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
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
public class CoinbaseTransaction extends TransferTransaction {
	public final static int REWARD_NUMBERS = 2;
	
	public CoinbaseTransaction() {
		super();
		transactionType = TransactionType.COINBASE;
	}

	public CoinbaseTransaction(byte[] bytes, TransactionShape transactionShape)
			throws Exception {
		super(bytes, transactionShape);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBytes(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBytes(TransactionShape transactionShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Header
			os.write(getHeaderBytes(transactionShape));
			// Serialization Body
			os.write(getBodyBytes(transactionShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.Transaction#getBin(com.eqzip.eqcoin.util.Util.
	 * AddressShape)
	 */
	@Override
	public byte[] getBin(TransactionShape transactionShape) throws Exception {
		return EQCType.bytesToBIN(getBytes(transactionShape));
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
		if(!isSanity(TransactionShape.SEED)) {
			return false;
		}
		if(!nonce.equals(changeLog.getHeight().getNextID())) {
			return false;
		}
		if (changeLog.getHeight().compareTo(Util.getMaxCoinbaseHeight(changeLog.getHeight())) < 0) {
			if(!txOutList.get(0).isNew()) {
				Lock lock = changeLog.getFilter().getLock(txOutList.get(0).getLock().getReadableLock(), true);
				if(!lock.getPassportId().equals(ID.ONE)) {
					Log.Error("EQC Federation Coinbase Reward Passport's ID should equal to 1");
					return false;
				}
			}
			if(!txOutList.get(1).isNew()) {
				Lock lock = changeLog.getFilter().getLock(txOutList.get(1).getLock().getReadableLock(), true);
				if(lock.getPassportId().equals(ID.ONE)) {
					Log.Error("Miner Coinbase Reward Passport's ID shouldn't equal to 1");
					return false;
				}
			}
			if(txOutList.get(0).getValue() != Util.EQC_FEDERATION_COINBASE_REWARD) {
				return false;
			}
			if(txOutList.get(1).getValue() != Util.MINER_COINBASE_REWARD) {
				return false;
			}
		} else {
			throw new IllegalStateException("After MaxCoinbaseHeight haven't any CoinBase reward");
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		return transactionType == TransactionType.COINBASE;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTxInSanity()
	 */
	@Override
	protected boolean isTxInSanity() {
		return txIn == null;
	}

	@Override
	public boolean isDerivedSanity(TransactionShape transactionShape) {
		if (txOutList.size() != REWARD_NUMBERS) {
			Log.Error("Total TxOut numbers is invalid");
			return false;
		}
		for(TxOut txOut : txOutList) {
			if(!txOut.isSanity(LockShape.READABLE)) {
				Log.Error("TxOut's sanity test failed");
				return false;
			}
		}
		if(txOutList.get(0).getLock().getReadableLock().equals(txOutList.get(1).getLock().getReadableLock())) {
			return false;
		}
		return true;
	}

	@Override
	public void preparePlanting(ChangeLog changeLog)
			throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		Lock lock = null;
		
		// Update TxOut's Lock's isNew status if need
		for(TxOut txOut:txOutList) {
			lock = changeLog.getFilter().getLock(txOut.getLock().getReadableLock(), true);
			if (lock == null) {
				txOut.setNew(true);
			} else {
				// For security issue need retrieve and fill in every Lock' ID according to
				// it's readableLock
				txOut.setPassportId(lock.getPassportId());
			}
		}
	}
	
	@Override
	public void heal(ChangeLog changeLog, EQCSeed eqcSubchain) throws Exception {
//		EQcoinSubchain eQcoinSubchain = (EQcoinSubchain) eqcSubchain;
//		Passport account = null;
//		// Update TxOut's Address' isNew status if need
//		for (TxOut txOut : txOutList) {
//			account = changeLog.getFilter().getPassport(txOut.getPassportId(), true);
//			if (account == null) {
//				// Maybe here need reflacter
//				txOut.getLock().setReadableLock(eQcoinSubchain.getPassport(txOut.getLock().getId()).getReadableLock());
//				txOut.setNew(true);
//			} else {
//				// For security issue need retrieve and fill in every Address' AddressAI according to it's ID
//				txOut.getLock().setReadableLock(account.getLock().getReadableLock());
//			}
//		}
	}

	public void planting(ChangeLog changeLog) throws Exception {
		Passport passport = null;
		Lock lock = null;

		// Update CoinbaseTransaction's relevant Passport
		for(TxOut txOut:txOutList) {
			if (txOut.isNew()) {
				lock = txOut.getLock();
				lock.setId(changeLog.getNextLockId());
				if(lock.getId().equals(ID.ONE)) {
					EQcoinSeedPassport  eqcFederalPassport = new EQcoinSeedPassport();
					eqcFederalPassport.setId(changeLog.getNextPassportId());
					eqcFederalPassport.setLockID(lock.getId());
					eqcFederalPassport.setTxFeeRate((byte) Util.TXFEE_RATE);
					eqcFederalPassport.setCheckPointHeight(ID.ZERO);
					eqcFederalPassport.setCheckPointHash(Util.MAGIC_HASH);
					lock.setPassportId(eqcFederalPassport.getId());
					changeLog.getFilter().saveLock(lock);
					passport = eqcFederalPassport;
				}
				else {
					passport = new AssetPassport();
					passport.setId(changeLog.getNextPassportId());
					passport.setLockID(lock.getId());
					lock.setPassportId(passport.getId());
					changeLog.getFilter().saveLock(lock);
				}
			} else {
				passport = changeLog.getFilter().getPassport(txOut.getPassportId(), true);
			}
			passport.deposit(new ID(txOut.getValue()));
			changeLog.getFilter().savePassport(passport);
		}
		
	}
	
	public String toInnerJson() {
		return

		"\"CoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
		+ "\"List\":" + "\n" + getTxOutString() + "\n,"
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	/**
	 * CoinbaseTransaction doesn't need this.
	 * @see com.eqzip.eqcoin.blockchain.transaction.Transaction#getBillingSize()
	 */
	@Override
	public int getBillingSize() {
		int size = 0;

//		// Transaction's ID format's size which storage in the EQC Blockchain
//		size += getBin(LockShape.ID).length;
//		Log.info("ID size: " + size);
//
//		// Transaction's AddressList size which storage the new Address
//		for (TxOut txOut : txOutList) {
//			if (txOut.isNew()) {
//				size += txOut.getLock().getBin(LockShape.AI).length;
//				Log.info("New TxOut: " + txOut.getLock().getBin(LockShape.AI).length);
//			}
//		}
//		
//		Log.info("Total size: " + size);
		return size;
	}

	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
	}

	public void parseBody(ByteArrayInputStream is, TransactionShape transactionShape) throws Exception {
		nonce = EQCType.parseID(is);
		byte txOutValidCount = 0;
		while (txOutValidCount++ < REWARD_NUMBERS && !EQCType.isInputStreamEnd(is)) {
			txOutList.add(new TxOut(is, LockShape.ID));
		}
	}
	
	public byte[] getBodyBytes(TransactionShape transactionShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxOut
			for (TxOut txOut : txOutList) {
				os.write(txOut.getBytes(LockShape.ID));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.transaction.Transaction#compare(com.eqchains.blockchain.transaction.Transaction)
	 */
	@Override
	public boolean compare(Transaction transaction) {
		if(transaction instanceof CoinbaseTransaction) {
			return false;
		}
		CoinbaseTransaction coinbaseTransaction = (CoinbaseTransaction) transaction;
		for(int i=0; i<REWARD_NUMBERS; ++i) {
			if(!txOutList.get(i).compare(coinbaseTransaction.getTxOutList().get(i))) {
				return false;
			}
		}
		if(!nonce.equals(transaction.getNonce())) {
			return false;
		}
		return true;
	}
	
}
