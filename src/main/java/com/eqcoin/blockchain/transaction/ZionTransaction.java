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
import java.util.Vector;

import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Mar 7, 2020
 * @email 10509759@qq.com
 */
public class ZionTransaction extends TransferTransaction {
	
	public ZionTransaction(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public ZionTransaction() {
		super();
		transactionType = TransactionType.ZION;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.ZION;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isDerivedValid()
	 */
	@Override
	public boolean isDerivedValid() throws Exception {
		for(TxOut txOut:txOutList) {
			Lock lock = changeLog.getFilter().getLock(txOut.getLock().getReadableLock(), true);
			if(lock != null) {
				Log.Error("The Lock already exists this is invalid.");
				return false;
			}
		}
		// Check if TxFeeLimit is valid
		// Here maybe exists one bug pay attention to the total txout values need less than txin value
		// Here need avoid the result of txin value - total txout values is negative
		if (!isTxFeeLimitValid()) {
			Log.Error("isTxFeeLimitValid failed");
			return false;
		}
		return true;
	}

	@Override
	public boolean isDerivedSanity() {
		// Check if the TxOutList is sanity
		if(txOutList == null) {
			return false;
		}
		if (!(txOutList.size() >= MIN_TXOUT)) {
			return false;
		}
		for (TxOut txOut : txOutList) {
				if (!txOut.isSanity(LockShape.READABLE)) {
					return false;
				}
		}
		// Check if the TxOut's Passport is unique
		if (!isTxOutPassportUnique()) {
			Log.Error("TxOut Passport isn't unique");
			return false;
		}
		return true;
	}

	public boolean isTxOutPassportUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getLock().getReadableLock().equals(txOutList.get(j).getLock().getReadableLock())) {
					return false;
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedTxOutPlanting()
	 */
	@Override
	protected void derivedTxOutPlanting() throws Exception {
		Lock lock = null;
		Passport passport = null;
		// Update current Transaction's TxOut Passport
		for (TxOut txOut : txOutList) {
			lock = txOut.getLock();
			lock.setId(changeLog.getNextLockId());
			passport = new AssetPassport();
			passport.setId(changeLog.getNextPassportId());
			passport.setLockID(lock.getId());
			passport.deposit(new ID(txOut.getValue()));
			passport.setUpdateHeight(changeLog.getHeight());
			lock.setPassportId(passport.getId());
			changeLog.getFilter().saveLock(lock);
			changeLog.getFilter().savePassport(passport);
		}
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		parseSoloAndTransactionType(is);
		parseNonce(is);
		parseTxIn(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getHeaderBytes()
	 */
	@Override
	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			serializeSoloAndTransactionTypeBytes(os);
			serializeNonce(os);
			serializeTxInBytes(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
}
