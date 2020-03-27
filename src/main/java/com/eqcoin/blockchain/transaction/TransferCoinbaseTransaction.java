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
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
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
public class TransferCoinbaseTransaction extends TransferTransaction {
	public final static int REWARD_NUMBERS = 2;
	
	public TransferCoinbaseTransaction() {
		super();
		transactionType = TransactionType.TRANSFERCOINBASE;
	}

	public TransferCoinbaseTransaction(byte[] bytes)
			throws Exception {
		super(bytes);
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
	public boolean isDerivedSanity() {
		if (txOutList.size() != REWARD_NUMBERS) {
			Log.Error("Total TxOut numbers is invalid");
			return false;
		}
		for (TxOut txOut : txOutList) {
			if (!txOut.isSanity(LockShape.ID)) {
				Log.Error("TxOut's sanity test failed");
				return false;
			}
		}
		if (txOutList.get(0).getLock().getId().equals(txOutList.get(1).getLock().getId())) {
			Log.Error("Miner's Lock shouldn't equal to EQcoin Federal's lock");
			return false;
		}

		return true;
	}

//	@Override
//	public void preparePlanting()
//			throws Exception {
//		Lock lock = null;
//		if(transactionShape == TransactionShape.RPC) {
//			// Update TxOut's Lock's isNew status if need
//			for(TxOut txOut:txOutList) {
//				lock = changeLog.getFilter().getLock(txOut.getLock().getReadableLock(), true);
//				if (lock == null) {
//					txOut.setNew(true);
//				} else {
//					// For security issue need retrieve and fill in every Lock' ID according to
//					// it's readableLock
//					txOut.getLock().setId(lock.getPassportId());
//				}
//			}
//		}
//		else {
//			// Update TxOut's Address' isNew status if need
//			for (TxOut txOut : txOutList) {
//				if(txOut.getLock().getId().compareTo(changeLog.getPreviousTotalPassportNumbers()) > 0) {
//					txOut.setNew(true);
//					txOut.setLock(eQcoinSeed.getNextLock());
//					if(!txOut.getLock().getId().equals(changeLog.getNextPassportId())) {
//						throw new IllegalStateException("CoinbaseTransaction's ID is invalid.");
//					}
//				}
//			}
//		}
//	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		Passport passport = null;
		// Update CoinbaseTransaction's relevant Passport
		for(TxOut txOut:txOutList) {
			passport = changeLog.getFilter().getPassport(txOut.getLock().getId(), false);
			passport.deposit(new ID(txOut.getValue()));
			changeLog.getFilter().savePassport(passport);
		}
	}
	
//	public boolean prepareGaining() throws Exception {
//		// Update TxOut's Address' isNew status if need
//		for (TxOut txOut : txOutList) {
//			if(txOut.getLock().getId().compareTo(changeLog.getPreviousTotalPassportNumbers()) > 0) {
//				txOut.setNew(true);
//				txOut.setLock(eQcoinSeed.getNextLock());
//				if(!txOut.getLock().getId().equals(changeLog.getNextPassportId())) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}
//	
//	protected void derivedGaining() throws Exception {
//		Passport passport = null;
//		Lock lock = null;
//		// Update CoinbaseTransaction's relevant Passport
//		for(TxOut txOut:txOutList) {
//			if (txOut.isNew()) {
//				lock = changeLog.getFilter().getLock(txOut.getLock().getReadableLock(), true);
//				if(lock.getId().equals(ID.ONE)) {
//					EQcoinRootPassport  eqcFederalPassport = new EQcoinRootPassport();
//					eqcFederalPassport.setId(changeLog.getNextPassportId());
//					eqcFederalPassport.setLockID(lock.getId());
//					eqcFederalPassport.setTxFeeRate((byte) Util.TXFEE_RATE);
//					eqcFederalPassport.setCheckPointHeight(ID.ZERO);
//					eqcFederalPassport.setCheckPointHash(Util.MAGIC_HASH);
//					lock.setPassportId(eqcFederalPassport.getId());
//					changeLog.getFilter().saveLock(lock);
//					passport = eqcFederalPassport;
//				}
//				else {
//					passport = new AssetPassport();
//					passport.setId(changeLog.getNextPassportId());
//					passport.setLockID(lock.getId());
//					lock.setPassportId(passport.getId());
//					changeLog.getFilter().saveLock(lock);
//				}
//			} else {
//				passport = changeLog.getFilter().getPassport(txOut.getLock().getId(), true);
//			}
//			passport.deposit(new ID(txOut.getValue()));
//			changeLog.getFilter().savePassport(passport);
//		}
//	}

	public String toInnerJson() {
		return

		"\"CoinbaseTransaction\":" + "\n{\n" + TxIn.coinBase() + ",\n"
		+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
		+ "\"List\":" + "\n" + getTxOutString() + "\n,"
		+ "\"Nonce\":" + "\"" + nonce + "\"" + 
		"\n}";
	}

	@Override
	public int getBillingSize() {
		return 0;
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
		byte txOutValidCount = 0;
		while (txOutValidCount++ < REWARD_NUMBERS && !EQCType.isInputStreamEnd(is)) {
			txOutList.add(new TxOut(is, LockShape.ID));
		}
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
		if(transaction instanceof TransferCoinbaseTransaction) {
			return false;
		}
		TransferCoinbaseTransaction coinbaseTransaction = (TransferCoinbaseTransaction) transaction;
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
