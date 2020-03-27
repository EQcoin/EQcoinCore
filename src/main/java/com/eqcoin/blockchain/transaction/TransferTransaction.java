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

	public TransferTransaction(byte[] bytes) throws Exception {
		super(bytes);
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
	
	public void parseHeader(ByteArrayInputStream is)
			throws Exception {
		parseNonce(is);
		parseTxIn(is);
	}

	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization nonce
			serializeNonce(os);
			// Serialization TxIn
			serializeTxInBytes(os);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public String toInnerJson() {
		return

		"\"TransferTransaction\":" + "\n{\n" + txIn.toInnerJson() + ",\n" + "\"TxOutList\":" + "\n{\n" + "\"Size\":"
				+ "\"" + txOutList.size() + "\"" + ",\n" + "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n" + "\"EQCWitness\":"
				+ eqcWitness.toInnerJson() 
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
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization body
			os.write(getBodyBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	protected void derivedTxOutPlanting() throws Exception {
		Passport passport = null;
		// Update current Transaction's TxOut Account
		for (TxOut txOut : txOutList) {
			passport = changeLog.getFilter().getPassport(txOut.getLock().getId(), true);
			passport.deposit(new ID(txOut.getValue()));
			changeLog.getFilter().savePassport(passport);
		}
	}

	@Override
	protected void derivedPlanting() throws Exception {
		super.derivedPlanting();
		derivedTxOutPlanting();
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#parseSoloAndTransactionType(java.io.ByteArrayInputStream)
	 */
	@Override
	protected void parseSoloAndTransactionType(ByteArrayInputStream is) throws Exception {
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#getSoloAndTransactionTypeBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	protected void serializeSoloAndTransactionTypeBytes(ByteArrayOutputStream os) throws Exception {
	}

	protected void parseDerivedBody(ByteArrayInputStream is)
			throws Exception {
		// Parse TxOut
		byte[] txOuts = EQCType.parseBIN(is);
		ByteArrayInputStream iStream = new ByteArrayInputStream(txOuts);
		while (!EQCType.isInputStreamEnd(iStream)) {
			TxOut txOut = new TxOut(iStream, LockShape.ID);
			// Add TxOut
			txOutList.add(txOut);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#getDerivedBodyBytes()
	 */
	@Override
	protected byte[] getDerivedBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization TxOut
			os.write(EQCType.bytesToBIN(getTxOutListBytes()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	private byte[] getTxOutListBytes() throws Exception {
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

	/**
	 * Check if all TxOut relevant fields is valid for example the TxOutList, Operation,
	 * HelixList etc...
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isDerivedValid() throws Exception {
		if(txInLock.getPublickey() == null) {
			Log.Error("TxIn's Lock relevant publikey is null.");
			return false;
		}
		
		// Check if All LockID is valid
		if(!isAllTxOutLockIDValid()) {
			Log.Error("isAllTxOutLockIDValid failed.");
			return false;
		}

		// Check if TxFeeLimit is valid
		// Here maybe exists one bug pay attention to the total txout values need less than txin value
		// Here need avoid the result of txin value - total txout values is negative
		if (!isTxFeeLimitValid()) {
			Log.Error("isTxFeeLimitValid failed.");
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
		return txOut.getLock().getId().equals(txIn.getLock().getId());
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
			if (txOut2.getLock().getId().equals(txOut.getLock().getId())) {
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
				if (txOutList.get(i).getLock().getId().equals(txOutList.get(j).getLock().getId())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int getBillingSize() throws Exception {
		int size = 0;

		// Transaction's ID format's size which storage in the EQC Blockchain
		size += getBin().length;
//		Log.info("ID size: " + size);

		// Transaction's AddressList size which storage the new Address
		for (TxOut txOut : txOutList) {
			if (txOut.isNew()) {
				size += txOut.getBin(LockShape.AI).length;
//				Log.info("New TxOut: " + txOut.getBin(AddressShape.AI).length);
			}
		}

		// Transaction's Signature size
		size += eqcWitness.getBin().length;
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

	public boolean isAllTxOutLockIDValid() {
		for (TxOut txOut : txOutList) {
			if (txOut.getLock().getId().compareTo(changeLog.getPreviousTotalPassportNumbers().getPreviousID()) > 0) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isDerivedSanity() {
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
