/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.util.Log;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class TransferTransaction extends Transaction {
	protected final static int MIN_TXOUT = 1;
	protected Vector<TransferTxOut> txOutList;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#init()
	 */
	@Override
	protected void init() {
		super.init();
		txOutList = new Vector<>();
		transactionType = TransactionType.TRANSFER;
	}

	public TransferTransaction() {
		super();
	}
	
	public TransferTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.TRANSFER;
	}
	
	public String toInnerJson() {
		return

		"\"TransferTransaction\":" + "\n{\n" + statusInnerJson() + ",\n" + "\"TxOutList\":" + "\n{\n" + "\"Size\":"
				+ "\"" + txOutList.size() + "\"" + ",\n" + "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ witness.toInnerJson() 
				+ "\n" + "}";
	}

	protected void derivedTxOutPlanting() throws Exception {
		Passport passport = null;
		// Update current Transaction's TxOut Account
		for (TransferTxOut transferTxOut : txOutList) {
			passport = eqcHive.getGlobalState().getPassport(transferTxOut.getPassportId());
			passport.deposit(transferTxOut.getValue());
			passport.setEQCHive(eqcHive).planting();
		}
	}

	@Override
	protected void derivedPlanting() throws Exception {
		super.derivedPlanting();
		derivedTxOutPlanting();
	}
	
	protected void parseDerivedBody(ByteArrayInputStream is)
			throws Exception {
		// Parse TxOut
		txOutList = EQCCastle.parseArray(is, new TransferTxOut());
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.Transaction#getDerivedBodyBytes()
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

	/**
	 * Check if all TxOut relevant fields is valid for example the TxOutList, Operation,
	 * HelixList etc...
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isDerivedValid() throws Exception {
		// Check if All LockID is valid
		if(!isAllTxOutLockIDValid()) {
			Log.Error("isAllTxOutLockIDValid failed.");
			return false;
		}
		return true;
	}
	
	/**
	 * @return the txOutList
	 */
	public Vector<TransferTxOut> getTxOutList() {
		return txOutList;
	}

	public void addTxOut(TransferTxOut txOut) {
		if (!isTxOutPassportExists(txOut)) {
			txOutList.add(txOut);
		} else {
			Log.Error(txOut + " already exists in txOutList just ignore it.");
		}
	}

	public Value getTxOutValues() {
		Value totalTxOut = null;
		for (TransferTxOut txOut : txOutList) {
			if(totalTxOut == null) {
				totalTxOut = new Value(txOut.getValue());
			}
			else {
				totalTxOut = totalTxOut.add(txOut.getValue());
			}
		}
		return totalTxOut;
	}

	public boolean isTxOutPassportIncludeTxInPassport(Passport passport) {
		for (TransferTxOut txOut : txOutList) {
			if (txOut.getPassportId().equals(passport.getId())) {
				return true;
			}
		}
		return false;
	}

	public Value getBillingValue() throws Exception {
		Log.info("TxFee: " + getTxFee() + " TxOutValues: " + getTxOutValues() + " TxFeeLimit: " + getTxFeeLimit());
		return getTxFee().add(getTxOutValues());
	}

	public boolean isTxOutPassportExists(TransferTxOut transferTxOut) {
		boolean boolIsExists = false;
		for (TransferTxOut transferTxOut2 : txOutList) {
			if (transferTxOut2.getPassportId().equals(transferTxOut.getPassportId())) {
				boolIsExists = true;
//				Log.info("TxOutAddressExists" + " a: " + txOut2.getAddress().getAddress() + " b: " + txOut.getAddress().getAddress());
				break;
			}
		}
		return boolIsExists;
	}
	
	public boolean isTxOutLockUnique() {
		for (int i = 0; i < txOutList.size(); ++i) {
			for (int j = i + 1; j < txOutList.size(); ++j) {
				if (txOutList.get(i).getPassportId().equals(txOutList.get(j).getPassportId())) {
					return false;
				}
			}
		}
		return true;
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
		for (TransferTxOut transferTxOut : txOutList) {
			if (transferTxOut.getPassportId().compareTo(eqcHive.getPreRoot().getTotalPassportNumbers()) >= 0) {
				Log.Error("isAllTxOutLockIDValid failed: " + transferTxOut);
				return false;
			}
		}
		return true;
	}
	
	public boolean isDerivedSanity() throws Exception {
		// Check if the TxOutList is sanity
		if(txOutList == null) {
			Log.Error("txOutList == null");
			return false;
		}
		if (!(txOutList.size() >= MIN_TXOUT)) {
			Log.Error("!(txOutList.size() >= MIN_TXOUT), txOutList.size: " + txOutList.size());
			return false;
		}
		for (TransferTxOut txOut : txOutList) {
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
		// Check if TxOut's Address doesn't include TxIn
		if (isTxOutPassportIncludeTxInPassport(witness.getPassport())) {
			Log.info("Txout's Address include TxIn this is invalid");
			return false;
		}
		if(!getTxFee().isSanity()) {
			Log.Error("!getTxFee().isSanity()");
			return false;
		}
		return true;
	}

	
	
}
