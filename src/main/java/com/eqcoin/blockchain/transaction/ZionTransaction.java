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

import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 7, 2020
 * @email 10509759@qq.com
 */
public class ZionTransaction extends TransferTransaction {
	protected Vector<ZionTxOut> txOutList;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#init()
	 */
	@Override
	protected void init() {
		super.init();
		transactionType = TransactionType.ZION;
		txOutList = new Vector<>();
	}

	public ZionTransaction() {
		super();
	}
	
	public ZionTransaction(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public ZionTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
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
		ID lockId = null;
		for(ZionTxOut txOut:txOutList) {
			lockId = changeLog.getFilter().isLockExists(txOut.getLock(), true);
			if(lockId != null) {
				Log.Error("The Lock already exists this is invalid.");
				return false;
			}
		}
//		// Check if TxFeeLimit is valid
//		// Here maybe exists one bug pay attention to the total txout values need less than txin value
//		// Here need avoid the result of txin value - total txout values is negative
//		if (!isTxFeeLimitValid()) {
//			Log.Error("isTxFeeLimitValid failed");
//			return false;
//		}
		return true;
	}

	@Override
	public boolean isDerivedSanity() throws Exception {
		// Check if the TxOutList is sanity
		if(txOutList == null) {
			return false;
		}
		if (!(txOutList.size() >= MIN_TXOUT)) {
			return false;
		}
		for (ZionTxOut txOut : txOutList) {
				if (!txOut.isSanity()) {
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
				if (txOutList.get(i).getLock().equals(txOutList.get(j).getLock())) {
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
		EQCLockMate lock = null;
		Passport passport = null;
		// Update current Transaction's TxOut Passport
		for (ZionTxOut txOut : txOutList) {
			lock = new EQCLockMate();
			lock.setLock(txOut.getLock());
			lock.setId(changeLog.getNextLockId());
			passport = new AssetPassport();
			passport.setId(changeLog.getNextPassportId());
			passport.setLockID(lock.getId());
			passport.deposit(txOut.getValue());
			passport.setUpdateHeight(changeLog.getHeight());
			lock.setPassportId(passport.getId());
			changeLog.getFilter().saveLock(lock);
			changeLog.getFilter().savePassport(passport);
		}
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseDerivedBody(java.io.ByteArrayInputStream)
	 */
	@Override
	protected void parseDerivedBody(ByteArrayInputStream is) throws Exception {
		// Parse TxOut
		txOutList = EQCType.parseArray(is, new ZionTxOut());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#getProofLength()
	 */
	@Override
	protected Value getProofLength() throws Exception {
		Value proofLength = Value.ZERO;
		for(ZionTxOut aiTxOut:txOutList) {
			proofLength = proofLength.add(Util.ASSET_PASSPORT_PROOF_SPACE_COST);
			proofLength = proofLength.add(aiTxOut.getLock().getProofLength());
		}
		return proofLength;
	}

	public void addTxOut(ZionTxOut txOut) {
		if (!isTxOutPassportExists(txOut)) {
			txOutList.add(txOut);
		} else {
			Log.Error(txOut + " already exists in txOutList just ignore it.");
		}
	}

	public boolean isTxOutPassportExists(ZionTxOut txOut) {
		boolean boolIsExists = false;
		for (ZionTxOut txOut2 : txOutList) {
			if (txOut2.getLock().equals(txOut.getLock())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getDerivedBodyBytes()
	 */
	@Override
	protected byte[] getDerivedBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization TxOut
			os.write(EQCType.eqcSerializableListToArray(txOutList));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#getTxOutValues()
	 */
	@Override
	public Value getTxOutValues() {
		Value totalTxOut = Value.ZERO;
		for (ZionTxOut txOut : txOutList) {
			totalTxOut = totalTxOut.add(txOut.getValue());
		}
		return totalTxOut;
	}
	
	
}
