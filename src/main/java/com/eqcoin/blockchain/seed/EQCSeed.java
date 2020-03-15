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
package com.eqcoin.blockchain.seed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Vector;
import org.apache.velocity.runtime.directive.Parse;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.passport.Passport.PassportType;
import com.eqcoin.blockchain.transaction.CompressedPublickey;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TxOut;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.serialization.EQCType.ARRAY;
import com.eqcoin.util.ID;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCSeed implements EQCTypable, EQCInheritable {
	protected EQCSeedHeader subchainHeader;
	protected Vector<Transaction> newTransactionList;
	protected long newTransactionListLength;
	// The following is Transactions' Segregated Witness members it's hash will be
	// recorded in the Root's accountsMerkelTreeRoot together with Transaction.
	protected Vector<EQCSegWit> newEQCSegWitList;
	protected boolean isSegwit;
	
	public enum SubchainType {
		EQCOIN, INVALID;
		public static SubchainType get(int ordinal) {
			SubchainType subchainType = null;
			switch (ordinal) {
			case 0:
				subchainType = SubchainType.EQCOIN;
				break;
			default:
				subchainType = SubchainType.INVALID;
				break;
			}
			return subchainType;
		}
		public boolean isSanity() {
			if((this.ordinal() < EQCOIN.ordinal()) || (this.ordinal() >= INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public void init() {
		newTransactionList = new Vector<>();
		newEQCSegWitList = new Vector<>();
	}
	
	public EQCSeed() {
		init();
	}
	
	public EQCSeed(byte[] bytes, boolean isSegwit) throws Exception {
		EQCType.assertNotNull(bytes);
		init();
		this.isSegwit = isSegwit;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseHeader(is);
		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBytes()
	 */
	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getHeaderBytes());
		os.write(getBodyBytes());
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(subchainHeader == null || newEQCSegWitList == null || newEQCSegWitList == null) {
			return false;
		}
		if(!(newEQCSegWitList.isEmpty() && newEQCSegWitList.isEmpty() && newTransactionListLength == 0)) {
			return false;
		}
		if(newTransactionList.size() != newEQCSegWitList.size()) {
			return false;
		}
		long newTransactionListLength = 0;
		for(Transaction transaction:newTransactionList) {
			newTransactionListLength += transaction.getBin(TransactionShape.SEED).length;
		}
		if(this.newTransactionListLength != newTransactionListLength) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		subchainHeader = new EQCSeedHeader(is);
	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse NewTransactionList
		ARRAY transactions = null;
		Transaction transaction = null;
		transactions = EQCType.parseARRAY(is);
		if (!transactions.isNULL()) {
			newTransactionListLength = transactions.size;
			ByteArrayInputStream is1 = new ByteArrayInputStream(transactions.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				transaction = Transaction.parseTransaction(EQCType.parseBIN(is1), TransactionShape.SEED);
				newTransactionList.add(transaction);
			}
			EQCType.assertEqual(transactions.size, newTransactionList.size());
		}
		if (!isSegwit) {
			// Parse NewEQCSegWitList
			ARRAY segWitList = null;
			segWitList = EQCType.parseARRAY(is);
			if (!segWitList.isNULL()) {
				ByteArrayInputStream is1 = new ByteArrayInputStream(segWitList.elements);
				while (!EQCType.isInputStreamEnd(is1)) {
					newEQCSegWitList.add(new EQCSegWit(is1));
				}
				EQCType.assertEqual(segWitList.size, newEQCSegWitList.size());
			}
		}
		else {
			// Just skip the data stream to keep data stream's format is valid
			EQCType.parseARRAY(is);
		}
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(subchainHeader.getBytes());
		return os.toByteArray();
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getNewTransactionListARRAY());
		if(!isSegwit) {
			os.write(getNewEQCSegWitListARRAY());
		}
		return os.toByteArray();
	}

	/**
	 * @return the EQCSubchainHeader
	 */
	public EQCSeedHeader getSubchainHeader() {
		return subchainHeader;
	}

	/**
	 * @param EQCSeedHeader the EQCSubchainHeader to set
	 */
	public void setSubchainHeader(EQCSeedHeader subchainHeader) {
		this.subchainHeader = subchainHeader;
	}

	/**
	 * @return the newTransactionListLength
	 */
	public long getNewTransactionListLength() {
		return newTransactionListLength;
	}

	/**
	 * @return the newTransactionList
	 */
	public Vector<Transaction> getNewTransactionList() {
		return newTransactionList;
	}

	/**
	 * @return the newEQCSegWitList
	 */
	public Vector<EQCSegWit> getNewEQCSegWitList() {
		return newEQCSegWitList;
	}
	
//	public ID getSN(PassportsMerkleTree changeLog)
//			throws ClassNotFoundException, SQLException, Exception {
//		ID sn = null;
//		if (newTransactionList.size() == 0) {
//			if (changeLog.getHeight().equals(ID.ZERO)) {
//				initSN = ID.ONE;
//			} else {
//				EQcoinSubchainPassport eQcoinSubchainAccount = (EQcoinSubchainPassport) Util.DB()
//						.getPassport(Asset.EQCOIN, changeLog.getHeight().getPreviousID());
//				initSN = eQcoinSubchainAccount.getTotalTransactionNumbers()
//						.add(changeLog.getTotalCoinbaseTransactionNumbers()).getNextID();
//			}
//			sn = initSN;
//		} else {
//			sn = initSN.add(ID.valueOf(newTransactionList.size()));
//		}
//		return sn;
//	}
	
	public void addTransaction(Transaction transaction, ChangeLog changeLog) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBin(TransactionShape.SEED).length;
			// Add Signature
			newEQCSegWitList.add(transaction.getEqcSegWit());
//			// Add Transactions
//			Util.DB().saveTransaction(transaction, changeLog.getHeight(), index, getSN(changeLog), changeLog.getFilter().getMode());
	}
	
	public boolean isTransactionExists(Transaction transaction) {
		return newTransactionList.contains(transaction);
	}
	
	public byte[] getRoot() throws Exception {
		return null;
	}
	
	public byte[] getNewEQCSegWitListMerkelTreeRoot() throws Exception {
		Vector<byte[]> eqcSegWitList = new Vector<byte[]>();
		for (EQCSegWit eqcSegWit : newEQCSegWitList) {
			eqcSegWitList.add(eqcSegWit.getBytes());
		}
		return Util.getMerkleTreeRoot(eqcSegWitList, true);
	}
	
	public byte[] getNewTransactionListMerkelTreeRoot() throws Exception {
		Vector<byte[]> transactions = new Vector<byte[]>();
		for (Transaction transaction : newTransactionList) {
			transactions.add(transaction.getBytes(TransactionShape.SEED));
		}
		return Util.getMerkleTreeRoot(transactions, true);
	}
	
	public static EQcoinSeed parse(byte[] bytes, boolean isSegwit) throws Exception {
		EQcoinSeed eQcoinSeed = null;
		eQcoinSeed = new EQcoinSeed(bytes, isSegwit);
		return eQcoinSeed;
	}
	
//	public void buildTransactionsForVerify() throws ClassNotFoundException, SQLException {
//		// Only have CoinBase Transaction just return
//		if (transactions.getNewTransactionList().size() == 1) {
//			Transaction transaction = transactions.getNewTransactionList().get(0);
//			// Set Address for every Transaction
//			// Set TxOut Address
//			for (TxOut txOut : transaction.getTxOutList()) {
//				txOut.getPassport().setReadableAddress(Util.getAddress(txOut.getPassport().getId(), this));
//			}
//			return;
//		}
//
//		// Set Signature for every Transaction
//		// Bug fix change to verify if every Transaction's signature is equal to
//		// Signatures
//		for (int i = 1; i < signatures.getSignatureList().size(); ++i) {
//			transactions.getNewTransactionList().get(i).setSignature(signatures.getSignatureList().get(i));
//		}
//
//		for (int i = 1; i < transactions.getNewTransactionList().size(); ++i) {
//			Transaction transaction = transactions.getNewTransactionList().get(i);
//			// Set PublicKey for every Transaction
//			// Bug fix before add in Transactions every transaction should have signature &
//			// PublicKey.
//			transaction.setCompressedPublickey(Util.getPublicKey(transaction.getTxIn().getPassport().getId(), this));
//			// Set Address for every Transaction
//			// Set TxIn Address
//			transaction.getTxIn().getPassport()
//					.setReadableAddress(Util.getAddress(transaction.getTxIn().getPassport().getId(), this));
//			// Set TxOut Address
//			for (TxOut txOut : transaction.getTxOutList()) {
//				txOut.getPassport().setReadableAddress(Util.getAddress(txOut.getPassport().getId(), this));
//			}
//		}
//	}
	
	private byte[] getNewTransactionListARRAY() throws Exception {
		if (newTransactionList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> transactions = new Vector<byte[]>();
			for (Transaction transaction : newTransactionList) {
				transactions.add(transaction.getBin(TransactionShape.SEED));
			}
			return EQCType.bytesArrayToARRAY(transactions);
		}
	}
	
	private byte[] getNewEQCSegWitListARRAY() throws Exception {
		if (newEQCSegWitList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> eqcSegWitList = new Vector<byte[]>();
			for (EQCSegWit eqcSegWit : newEQCSegWitList) {
				eqcSegWitList.add(eqcSegWit.getBin());
			}
			return EQCType.bytesArrayToARRAY(eqcSegWitList);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}
	
	public String toInnerJson() {
		return
				"\"EQCSubchain\":{\n" + subchainHeader.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n},\n" +
						"\"NewSignatureList\":" + 
						"\n{\n" +
						"\"Size\":\"" + newEQCSegWitList.size() + "\",\n" +
						"\"List\":" + 
							_getNewSignatureList() + "\n}\n" +
				 "\n}\n}";
	}
	
	protected String _getNewTransactionList() {
		String tx = null;
		if (newTransactionList != null && newTransactionList.size() > 0) {
			tx = "\n[\n";
			if (newTransactionList.size() > 1) {
				for (int i = 0; i < newTransactionList.size() - 1; ++i) {
					tx += newTransactionList.get(i) + ",\n";
				}
			}
			tx += newTransactionList.get(newTransactionList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	protected String _getNewSignatureList() {
		String tx = null;
		if (newEQCSegWitList != null && newEQCSegWitList.size() > 0) {
			tx = "\n[\n";
			if (newEQCSegWitList.size() > 1) {
				for (int i = 0; i < newEQCSegWitList.size() - 1; ++i) {
					tx += newEQCSegWitList.get(i).toInnerJson() + ",\n";
				}
			}
			tx += newEQCSegWitList.get(newEQCSegWitList.size() - 1).toInnerJson();
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	public void plantingTransaction(Vector<Transaction> transactionList, ChangeLog changeLog) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		
	}
	
	public boolean saveTransactions() throws Exception {
		return false;
	}
	
}
