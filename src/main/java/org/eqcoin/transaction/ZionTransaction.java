/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection 
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
import java.util.Vector;

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
	
	public ZionTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		if(transactionType == null) {
			Log.Error("transactionType == null");
			return false;
		}
		if(transactionType != TransactionType.ZION) {
			Log.Error("transactionType != TransactionType.ZION");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isDerivedValid()
	 */
	@Override
	public boolean isDerivedValid() throws Exception {
		ID lockId = null;
		for(ZionTxOut txOut:txOutList) {
			lockId = eqcHive.getGlobalState().isLockMateExists(txOut.getLock());
			if(lockId != null) {
				Log.Error("The Lock already exists this is invalid.");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isDerivedSanity() throws Exception {
		// Check if the TxOutList is sanity
		if(txOutList == null) {
			Log.Error("txOutList == null");
			return false;
		}
		if (!(txOutList.size() >= MIN_TXOUT)) {
			Log.Error("!(txOutList.size() >= MIN_TXOUT), txOutList.size:" + txOutList.size());
			return false;
		}
		for (ZionTxOut txOut : txOutList) {
				if (!txOut.isSanity()) {
					Log.Error("!txOut.isSanity(): " + txOut);
					return false;
				}
		}
		// Check if the TxOut's Passport is unique
		if (!isTxOutLockUnique()) {
			Log.Error("TxOut Passport isn't unique");
			return false;
		}
		return true;
	}

	public boolean isTxOutLockUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getLock().equals(txOutList.get(j).getLock())) {
					Log.Error(txOutList.get(i).getLock() + " equals " + txOutList.get(j).getLock());
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
		LockMate lockMate = null;
		Passport passport = null;
		// Update current Transaction's TxOut Passport
		for (ZionTxOut txOut : txOutList) {
			lockMate = new LockMate();
			lockMate.setLock(txOut.getLock());
			lockMate.setId(eqcHive.getGlobalState().getLastLockMateId().getNextID());
			lockMate.setEQCHive(eqcHive).planting();
			passport = new AssetPassport();
			passport.setId(eqcHive.getGlobalState().getLastPassportId().getNextID());
			passport.setLockID(lockMate.getId());
			passport.deposit(txOut.getValue());
			passport.setEQCHive(eqcHive).planting();
		}
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#parseDerivedBody(java.io.ByteArrayInputStream)
	 */
	@Override
	protected void parseDerivedBody(ByteArrayInputStream is) throws Exception {
		// Parse TxOut
		txOutList = EQCCastle.parseArray(is, new ZionTxOut());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#getProofLength()
	 */
	@Override
	protected Value getGlobalStateLength() throws Exception {
		Value proofLength = null;
		for(ZionTxOut aiTxOut:txOutList) {
			if(proofLength == null) {
				proofLength = new Value(Util.ASSET_PASSPORT_PROOF_SPACE_COST);
				proofLength = proofLength.add(aiTxOut.getLock().getGlobalStateLength());
			}
			else {
				proofLength = proofLength.add(Util.ASSET_PASSPORT_PROOF_SPACE_COST);
				proofLength = proofLength.add(aiTxOut.getLock().getGlobalStateLength());
			}
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
			os.write(EQCCastle.eqcSerializableListToArray(txOutList));
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
		Value totalTxOut = null;
		for (ZionTxOut txOut : txOutList) {
			if(totalTxOut == null) {
				totalTxOut = new Value(txOut.getValue());
			}
			else {
				totalTxOut = totalTxOut.add(txOut.getValue());
			}
		}
		return totalTxOut;
	}
	
	
}
