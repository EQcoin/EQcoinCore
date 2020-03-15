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

import java.awt.Window.Type;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TXFEE_RATE;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.TRANSACTION_OP;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransferTransaction extends Transaction {
	protected final static int MIN_TXOUT = 1;
	/**
	 * In the TxOut list will only contain the relevant Passport's ID and it's value
	 */
	protected Vector<TxOut> txOutList;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#init()
	 */
	@Override
	protected void init() {
		super.init();
		txOutList = new Vector<>();
	}

	public TransferTransaction(byte[] bytes, TransactionShape transactionShape) throws Exception {
		super(bytes, transactionShape);
	}

	public TransferTransaction() {
		transactionType = TransactionType.TRANSFER;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.TRANSFER;
	}
	
	public void parseHeader(ByteArrayInputStream is, TransactionShape transactionShape)
			throws Exception {
		// Parse nonce
		nonce = new ID(EQCType.parseEQCBits(is));
		// Parse TxIn
		txIn = new TxIn(is, LockShape.ID);
	}

	public byte[] getHeaderBytes(TransactionShape transactionShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			os.write(EQCType.bigIntegerToEQCBits(nonce));
			// Serialization TxIn
			os.write(txIn.getBytes(LockShape.ID));
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
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((eqcSegWit == null) ? 0 : eqcSegWit.hashCode());
		result = prime * result + ((txIn == null) ? 0 : txIn.hashCode());
		result = prime * result + ((txOutList == null) ? 0 : txOutList.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransferTransaction other = (TransferTransaction) obj;
		if (eqcSegWit == null) {
			if (other.eqcSegWit != null)
				return false;
		} else if (!eqcSegWit.equals(other.eqcSegWit))
			return false;
		if (txIn == null) {
			if (other.txIn != null)
				return false;
		} else if (!txIn.equals(other.txIn))
			return false;
		if (txOutList == null) {
			if (other.txOutList != null)
				return false;
		} else {
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> originalTxOutList = new Vector<TxOut>();
			for (TxOut txOut : txOutList) {
				originalTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(originalTxOutList);
			// Temporarily save a copy of TxOut in order not to change the order in which
			// the user enters TxOut.
			Vector<TxOut> targetTxOutList = new Vector<TxOut>();
			for (TxOut txOut : other.txOutList) {
				targetTxOutList.add(txOut);
			}
			// Sort the temporarily saved TxOut in alphabetical dictionary order.
			Collections.sort(targetTxOutList);
			// Compare temporarily saved TxOut collections sorted alphabetically in
			// alphabetical order.
			if (!originalTxOutList.equals(targetTxOutList))
				return false;
		}
		return true;
	}

	public String toInnerJson() {
		return

		"\"TransferTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n" + "\"TxOutList\":" + "\n{\n" + "\"Size\":"
				+ "\"" + txOutList.size() + "\"" + ",\n" + "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n" + "\"EQCSegWit\":"
				+ eqcSegWit.toInnerJson() + ",\n" + "\"Publickey\":"
				+ ((compressedPublickey == null || compressedPublickey.getCompressedPublickey() == null) ? null
						: "\"" + Util.getHexString(compressedPublickey.getCompressedPublickey()) + "\"")
				+ "\n" + "}";
	}

	@Override
	public int getMaxBillingLength() {
		int length = 0;

		/**
		 * Transaction.getBytes(AddressShape.ID)'s length
		 */
		// Nonce
		length += EQCType.bigIntegerToEQCBits(nonce).length;
		// TxIn Serial Number length
		length += Util.BASIC_SERIAL_NUMBER_LEN;
		// TxIn value's length
		length += Util.BASIC_VALUE_NUMBER_LEN;
		// TxOut length
		for (TxOut txOut : txOutList) {
			length += Util.BASIC_SERIAL_NUMBER_LEN;
			length += EQCType.longToEQCBits(txOut.getValue()).length;
		}
		// For Binxx's overhead length
		length += EQCType.getEQCTypeOverhead(length);

		/**
		 * Transaction's relevant Passport length
		 */
		for (TxOut txOut : txOutList) {
			length += txOut.getLock().getBillingSize();
		}

		/**
		 * Transaction's compressed Publickey length
		 */
		length += compressedPublickey.getBillingSize();

		/**
		 * Transaction's Signature length
		 */
		if (txIn.getLock().getAddressType() == LockType.T1) {
			length += Util.P256_BASIC_SIGNATURE_LEN;
		} else if (txIn.getLock().getAddressType() == LockType.T2) {
			length += Util.P521_BASIC_SIGNATURE_LEN;
		}
//		Log.info("Total length: " + length);
		return length;
	}

//	@Override
//	public int getBillingSize() {
//		int size = 0;
//
//		// Transaction's Serial Number format's size which storage in the EQC Blockchain
//		size += getBin().length;
//		Log.info("ID size: " + size);
//
//		// Transaction's AddressList size which storage the new Address
//		for (TxOut txOut : txOutList) {
//			if (txOut.isNew()) {
//				size += txOut.getAddress().getBin(AddressShape.AI).length;
//				Log.info("New TxOut: " + txOut.getAddress().getBin(AddressShape.AI).length);
//			}
//		}
//
//		// Transaction's PublickeyList size
//		if (publickey.isNew()) {
//			size += publickey.getBin().length;
//			Log.info("New Publickey: " + publickey.getBin().length);
//		}
//
//		// Transaction's Signature size
//		size += EQCType.bytesToBIN(signature).length;
//		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
//
//		Log.info("Total size: " + size);
//		return size;
//	}

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
			// Serialization body
			os.write(getBodyBytes(transactionShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	
	public void preparePlanting(ChangeLog changeLog) throws Exception {
		Passport txInPassport = changeLog.getFilter().getPassport(txIn.getPassportId(), true);
		Lock lock = changeLog.getFilter().getLock(txInPassport.getLockID(), true);

		// Update Publickey's isNew status if need
		if (lock.getPublickey() == null) {
			compressedPublickey.setNew(true);
		}
		
	}

	public void heal(ChangeLog changeLog, EQCSeed eqcSubchain) throws Exception {
		EQcoinSeed eQcoinSubchain = (EQcoinSeed) eqcSubchain;
		Passport txInPassport = changeLog.getFilter().getPassport(txIn.getPassportId(), true);
		Lock txInLock = changeLog.getFilter().getLock(txInPassport.getId(), true);
		// Update Publickey's isNew status if need
		if (txInLock.getPublickey() == null) {
			compressedPublickey.setNew(true);
//			compressedPublickey.setId(changeLog.getNextLockId());
			compressedPublickey.setCompressedPublickey(
					eQcoinSubchain.getCompressedPublickey(txIn.getPassportId()).getCompressedPublickey());
		}

		// Update TxOut's Address' isNew status if need
//		Passport account = null;
//		for (TxOut txOut : txOutList) {
//			account = changeLog.getFilter().getPassport(txOut.getPassportId(), true);
//			if (account == null) {
//				txOut.getKey().setReadableLock(
//						eQcoinSubchain.getPassport(txOut.getKey().getId()).getReadableLock());
//				txOut.setNew(true);
//			} else {
//				// For security issue need retrieve and fill in every Address' AddressAI
//				// according to it's ID
//				txOut.getKey().setReadableLock(account.getKey().getReadableLock());
//			}
//		}
	}

	public void planting(TransactionShape transactionShape) throws Exception {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		Passport passport = changeLog.getFilter().getPassport(txIn.getPassportId(), true);
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		passport.increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		passport.withdraw(new ID(getBillingValue()));
		changeLog.getFilter().savePassport(passport);
		// Update current Transaction's TxIn Publickey if need
		if (compressedPublickey.isNew()) {
			Lock lock = changeLog.getFilter().getLock(passport.getId(), true);
			lock.setPublickey(compressedPublickey.getCompressedPublickey());
			changeLog.getFilter().saveLock(lock);
		}

		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				Lock lock = new Lock();
				lock.setId(changeLog.getNextLockId());
				lock.setReadableLock(txOut.getLock().getReadableLock());
				passport = new AssetPassport();
				passport.setId(changeLog.getNextPassportId());
				lock.setPassportId(passport.getId());
				changeLog.getFilter().saveLock(lock);
			} else {
				passport = changeLog.getFilter().getPassport(txOut.getPassportId(), true);
			}
			passport.deposit(new ID(txOut.getValue()));
			changeLog.getFilter().savePassport(passport);
		}
		
		free();
	}

	public void parseBody(ByteArrayInputStream is, TransactionShape transactionShape)
			throws Exception {
		// Parse TxOut
		while (!EQCType.isInputStreamEnd(is)) {
			TxOut txOut = new TxOut(is, LockShape.ID);
			// Add TxOut
			txOutList.add(txOut);
		}
	}

	@Override
	public byte[] getBodyBytes(TransactionShape transactionShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization TxOut
			for (TxOut txOut : txOutList) {
				if(transactionShape == TransactionShape.RPC) {
					os.write(txOut.getBytes(LockShape.READABLE));
				}
				else {
					os.write(txOut.getBytes(LockShape.ID));
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * Check if all TxOut relevant fields is valid for example the TxOutList, Operation,
	 * HelixList etc...
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isDerivedValid() throws Exception {
		// Check if TxOut is valid
		for(TxOut txOut:txOutList) {
			if(txOut.getPassportId().compareTo(changeLog.getPreviousTotalPassportNumbers()) > 0) {
				Log.Error("isTxOutValid failed");
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
	
	/**
	 * @return the txOutList
	 */
	public Vector<TxOut> getTxOutList() {
		return txOutList;
	}

	/**
	 * @param txOutList the txOutList to set
	 */
	public void setTxOutList(Vector<TxOut> txOutList) {
		this.txOutList = txOutList;
	}

	public void addTxOut(TxOut txOut) {
		if (!isTxOutPassportExists(txOut)) {
			if (!isTxOutPassportEqualsTxInPassport(txOut)) {
				txOutList.add(txOut);
			} else {
				Log.Error(txOut + " equal to TxIn Address: " + txIn + " just ignore it.");
			}
		} else {
			Log.Error(txOut + " already exists in txOutList just ignore it.");
		}
	}

	public int getTxOutNumber() {
		return txOutList.size();
	}

	public long getTxOutValues() {
		long totalTxOut = 0;
		for (TxOut txOut : txOutList) {
			totalTxOut += txOut.getValue();
		}
		return totalTxOut;
	}

	public boolean isTxOutValueLessThanTxInValue() {
		return getTxOutValues() < txIn.getValue();
	}

	public boolean isTxOutPassportIncludeTxInPassport() {
		for (TxOut txOut : txOutList) {
			if (isTxOutPassportEqualsTxInPassport(txOut)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTxOutPassportEqualsTxInPassport(TxOut txOut) {
		return txOut.getLock().getPassportId().equals(txIn.getLock().getPassportId());
	}

	public long getTxFeeLimit() {
		return txIn.getValue() - getTxOutValues();
	}
	
	public long getBillingValue() throws Exception {
		Log.info("TxFee: " + getTxFee() + " TxOutValues: " + getTxOutValues() + " TxFeeLimit: " + getTxFeeLimit());
		return getTxFee() + getTxOutValues();
	}

	public void setTxFeeLimit(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}

	public void cypherTxInValue(TXFEE_RATE txfee_rate) {
		txIn.setValue(getTxOutValues() + cypherTxFeeLimit(txfee_rate));
	}
	
	public boolean isTxOutPassportExists(TxOut txOut) {
		boolean boolIsExists = false;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getLock().getPassportId().equals(txOut.getLock().getPassportId())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}
	
	public TxOut getTxOut(ID id) {
		TxOut txOut = null;
		for (TxOut txOut2 : txOutList) {
			if (txOut2.getLock().getId().equals(id)) {
				txOut = txOut2;
				break;
			}
		}
		return txOut;
	}

	public boolean isTxOutPassportUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getPassportId().equals(txOutList.get(j).getPassportId())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int getBillingSize() throws Exception {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin(TransactionShape.SEED).length;
//		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getBin(LockShape.AI).length;
//				Log.info("New TxOut: " + txOut.getBin(AddressShape.AI).length);
			}
		}

		// Transaction's PublickeyList size
		if (compressedPublickey.isNew()) {
			size += compressedPublickey.getBin().length;
//			Log.info("New Publickey: " + publickey.getBin().length);
		}

		// Transaction's Signature size
		size += eqcSegWit.getBin().length;
//		Log.info("Signature size: " + EQCType.bytesToBIN(signature).length);
//		Log.info("Total size: " + size);
		return size;
	}
	
	protected String getTxOutString() {
		String tx = "[\n";
		if (txOutList.size() > 0) {
			for (int i = 0; i < txOutList.size() - 1; ++i) {
				tx += txOutList.get(i) + ",\n";
			}
			tx += txOutList.get(txOutList.size() - 1);
		} else {
			tx += null;
		}
		tx += "\n]";
		return tx;
	}

	public boolean isAllAddressIDValid(ChangeLog changeLog) {
		if (txIn.getLock().getId().compareTo(changeLog.getTotalPassportNumbers()) > 0) {
			return false;
		}
		for (TxOut txOut : txOutList) {
			if (txOut.getLock().getId().compareTo(changeLog.getTotalPassportNumbers()) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isDerivedSanity(TransactionShape transactionShape) {
		// Check if the TxOutList is sanity
		if(txOutList == null) {
			return false;
		}
		if (!(txOutList.size() >= MIN_TXOUT)) {
			return false;
		}
		for (TxOut txOut : txOutList) {
			if (!txOut.isSanity(LockShape.ID)) {
				return false;
			}
		}
		// Check if the TxOut's Passport is unique
		if (!isTxOutPassportUnique()) {
			Log.Error("TxOut Passport isn't unique");
			return false;
		}
		// Check if TxOut's Address doesn't include TxIn
		if (isTxOutPassportIncludeTxInPassport()) {
			Log.info("Txout's Address include TxIn this is invalid");
			return false;
		}
		return true;
	}
	
}
