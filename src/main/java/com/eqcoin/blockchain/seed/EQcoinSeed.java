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
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.ChangeLog.Statistics;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.CoinbaseTransaction;
import com.eqcoin.blockchain.transaction.CompressedPublickey;
import com.eqcoin.blockchain.transaction.TransferOperationTransaction;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TxOut;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.serialization.EQCType.ARRAY;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;

/**
 * @author Xun Wang
 * @date July 31, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSeed extends EQCSeed {
	private Vector<Lock> newHelixList;
	private Vector<CompressedPublickey> newCompressedPublickeyList;
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#init()
	 */
	@Override
	public void init() {
		super.init();
		newHelixList = new Vector<>();
		newCompressedPublickeyList = new Vector<>();
		subchainHeader = new EQcoinSeedHeader();
	}

	public EQcoinSeed() {
		super();
	}

	public EQcoinSeed(byte[] bytes, boolean isSegwit) throws Exception {
		super(bytes, isSegwit);
	}

	public void addTransaction(Transaction transaction) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBytes(TransactionShape.SEED).length;
			// Add EQCSegWit
			newEQCSegWitList.add(transaction.getEqcSegWit());
			// Add new Publickey if need
			if (transaction.getCompressedPublickey().isNew()) {
				newCompressedPublickeyList.add(transaction.getCompressedPublickey());
			}
			// Add new Helix if need
			if(transaction instanceof TransferTransaction) {
				TransferTransaction transferTransaction = (TransferTransaction) transaction;
				for (TxOut txOut : transferTransaction.getTxOutList()) {
					if (txOut.isNew()) {
						newHelixList.add(txOut.getLock());
					}
				}
			}
	}
	
	public void addCoinbaseTransaction(CoinbaseTransaction coinbaseTransaction, ChangeLog changeLog) throws ClassNotFoundException, SQLException, Exception {
		// Add Coinbase Transaction
		((EQcoinSeedHeader) subchainHeader).setCoinbaseTransaction(coinbaseTransaction);
		// Add new Address
		for (TxOut txOut : coinbaseTransaction.getTxOutList()) {
			if (txOut.isNew()) {
				newHelixList.add(txOut.getLock());
			}
		}
	}
	
	public EQcoinSeedHeader getEQcoinSubchainHeader() {
		return (EQcoinSeedHeader) subchainHeader;
	}
	
	public ID getNewPassportID(ChangeLog changeLog) {
		ID initID = null;
		if (newHelixList.size() == 0) {
			initID = new ID(changeLog.getPreviousTotalAccountNumbers()
					.add(BigInteger.valueOf(Util.INIT_ADDRESS_SERIAL_NUMBER)));
		} else {
			initID = newHelixList.lastElement().getId().getNextID();
		}
		return initID;
	}
	
	public Lock getPassport(ID id) {
		Lock key = null;
		for(Lock key2:newHelixList) {
			if(key2.getId().equals(id)) {
				key = key2;
				break;
			}
		}
		return key;
	}
	
	public CompressedPublickey getCompressedPublickey(ID id) {
		CompressedPublickey compressedPublickey = null;
		for(CompressedPublickey compressedPublickey2:newCompressedPublickeyList) {
			if(compressedPublickey2.getId().equals(id)) {
				compressedPublickey = compressedPublickey2;
				break;
			}
		}
		return compressedPublickey;
	}

	/**
	 * @return the newHelixList
	 */
	public Vector<Lock> getNewHelixList() {
		return newHelixList;
	}

	/**
	 * @param newHelixList the newHelixList to set
	 */
	public void setNewHelixList(Vector<Lock> newHelixList) {
		this.newHelixList = newHelixList;
	}

	/**
	 * @return the newCompressedPublickeyList
	 */
	public Vector<CompressedPublickey> getNewCompressedPublickeyList() {
		return newCompressedPublickeyList;
	}

//	public boolean isEveryAddressExists() throws ClassNotFoundException, SQLException {
//		for (Transaction transaction : transactions.getNewTransactionList()) {
//			// Check if TxIn Address exists
//			if (!transaction.isCoinBase()) {
//				if (Util.getAddress(transaction.getTxIn().getPassport().getId(), this) == null) {
//					return false;
//				}
//			}
//
//			// Check if All TxOut Address exists
//			for (TxOut txOut : transaction.getTxOutList()) {
//				if (Util.getAddress(txOut.getPassport().getId(), this) == null) {
//					return false;
//				}
//			}
//		}
//		return true;
//	}
//
//	public boolean isEveryPublicKeyExists() {
//		if (transactions.getNewTransactionList().size() == 1) {
//			return true;
//		}
//		for (int i = 1; i < transactions.getSize(); ++i) {
//			return false;
//		}
//		return true;
//	}
	
	@Override
	public boolean isValid(ChangeLog changeLog) {
		try {
			EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, changeLog.getHeight().getPreviousID());
			ID sn = eQcoinSubchainAccount.getTotalTransactionNumbers().getNextID();
			// Check if Transactions' size < 1 MB
			if (newTransactionListLength > Util.ONE_MB) {
				Log.Error("EQcoinSubchain's length is invalid");
				return false;
			}

			// Check if NewPassportList is valid
			if (!isNewPassportListValid(changeLog)) {
				Log.Error("EQcoinSubchain's NewPassportList is invalid");
				return false;              
			}

			// Check if NewCompressedPublickeyList is valid
			if (!isNewCompressedPublickeyListValid(changeLog)) {
				Log.Error("EQcoinSubchain's NewCompressedPublickeyList is invalid");
				return false;
			}

			// Check if Signatures' size and Transaction size is equal
			if (newTransactionList.size() != newEQCSegWitList.size()) {
				Log.Error("EQcoinSubchain's newTransactionList's size doesn't equal to newEQCSegWitList's size");
				return false;
			}
			
			// Verify CoinBaseTransaction
			// Here need check if need exist CoinbaseTransaction
			CoinbaseTransaction coinbaseTransaction = getEQcoinSubchainHeader().getCoinbaseTransaction();
			coinbaseTransaction.heal(changeLog, this);
			if (!coinbaseTransaction.isValid()) {
				Log.info("CoinBaseTransaction is invalid: " + coinbaseTransaction);
				return false;
			}
			else {
				coinbaseTransaction.planting(changeLog);
//				Util.DB().saveTransaction(coinbaseTransaction, changeLog.getHeight(), ID.valueOf(Integer.MAX_VALUE), sn, changeLog.getFilter().getMode());
//				sn = sn.getNextID();
			}

			long totalTxFee = 0;
			Transaction transaction = null;
			for (int i = 0; i < newTransactionList.size(); ++i) {
				transaction = newTransactionList.get(i);
				// Fill in Signature
				transaction.setEqcSegWit(newEQCSegWitList.get(i));
				
				// Check if TxIn exists in previous block
				if(transaction.getTxIn().getPassportId().compareTo(changeLog.getPreviousTotalAccountNumbers()) > 0) {
					Log.Error("Transaction Account doesn't exist in previous block have to exit");
					return false;
				}
				
				if(!transaction.isSanity(TransactionShape.RPC)) {
					return false;
				}
				
				try {
					transaction.heal(this);
//					if(transaction.getCompressedPublickey().isNew()) {
//						transaction.getCompressedPublickey().setID(transaction.getTxIn().getPassport().getId());
//						transaction.getCompressedPublickey().setCompressedPublickey(getCompressedPublickey(transaction.getTxIn().getPassport().getId()).getCompressedPublickey());
//					}
				} catch (IllegalStateException e) {
					Log.Error(e.getMessage());
					return false;
				}

				// Check if the Transaction's type is correct
				if (!(transaction instanceof TransferTransaction || transaction instanceof TransferOperationTransaction)) {
					Log.Error("TransactionType is invalid have to exit");
					return false;
				}

				if (!transaction.isValid()) {
					Log.info("Transaction is invalid: " + transaction);
					return false;
				} else {
					// Update AccountsMerkleTree relevant Account's status
					transaction.planting();
//					Util.DB().saveTransaction(transaction, changeLog.getHeight(), ID.valueOf(i), sn, changeLog.getFilter().getMode());
//					sn = sn.getNextID();
					totalTxFee += transaction.getTxFee();
				}
			}
			
			// Add 

			// Update EQcoinSubchainAccount
			eQcoinSubchainAccount = (EQcoinSeedPassport) changeLog.getFilter().getPassport(ID.ONE, true);
			// Verify TxFee
			if(subchainHeader.getTotalTxFee().longValue() != totalTxFee) {
				Log.Error("Total TxFee is invalid.");
				return false;
			}
			else {
				eQcoinSubchainAccount.deposit(subchainHeader.getTotalTxFee());
			}
			// Update EQcoin Subchain's Header
			eQcoinSubchainAccount.setTotalSupply(ID.valueOf(Util.cypherTotalSupply(changeLog.getHeight())));
			eQcoinSubchainAccount.setTotalPassportNumbers(eQcoinSubchainAccount.getTotalPassportNumbers()
					.add(ID.valueOf(newHelixList.size())));
			eQcoinSubchainAccount.setTotalTransactionNumbers(eQcoinSubchainAccount
					.getTotalTransactionNumbers().add(ID.valueOf(newTransactionList.size())));
			// Save EQcoin Subchain's Header
			changeLog.getFilter().savePassport(eQcoinSubchainAccount);
			
			// Add audit layer at the following positions
			
		} catch (Exception e) {
			Log.Error("EQCHive is invalid: " + e.getMessage());
			return false;
		}

		return true;
	}
	
	public boolean isNewPassportListValid(ChangeLog changeLog) throws Exception {
//		if(newHelixList.size() == 0) {
//			// Here exists one bug need check if current Transactions contain any new Passport
//			return true;
//		}
		// Here need do more job to make sure the new lokc's order is correct and is unique
		if (!newHelixList.isEmpty() && !newHelixList.get(0).getId().getPreviousID()
				.equals(changeLog.getPreviousTotalAccountNumbers())) {
			return false;
		}
		// Get the new Passport's ID list from Transactions
		Vector<ID> newPassports = new Vector<>();
		for (Transaction transaction : newTransactionList) {
			// 2020-03-11 Here need do more job
//			for (TxOut txOut : transaction.getTxOutList()) {
//				if (txOut.getLock().getId().compareTo(changeLog.getPreviousTotalAccountNumbers()) > 0) {
//					if (!newPassports.contains(txOut.getLock().getId())) {
//						newPassports.add(txOut.getLock().getId());
//					}
//				}
//			}
		}
		if (newHelixList.size() != newPassports.size()) {
			return false;
		}
		for (int i = 0; i < newHelixList.size(); ++i) {
			if (!newHelixList.get(i).getId().equals(newPassports.get(i))) {
				return false;
			}
		}
		for (int i = 0; i < newHelixList.size(); ++i) {
			// Check if Address already exists and if exists duplicate Address in newHelixList
			if (changeLog.getFilter().getLock(newHelixList.get(i).getReadableLock(), true) != null) {
				return false;
			} else {
				// Check if ID is valid
				if (i < (newHelixList.size() - 1)) {
					if (!newHelixList.get(i).getId().getNextID().equals(newHelixList.get(i + 1))) {
						return false;
					}
				}
//				// Save new Account in Filter
//				Account account = new AssetAccount();
//				account.setCreateHeight(changeLog.getHeight());
//				account.setVersion(ID.ZERO);
//				account.setVersionUpdateHeight(changeLog.getHeight());
//				account.setPassport(newHelixList.get(i));
//				account.setLockCreateHeight(changeLog.getHeight());
//				Asset asset = new CoinAsset();
//				asset.setVersion(ID.ZERO);
//				asset.setVersionUpdateHeight(changeLog.getHeight());
//				asset.setAssetID(Asset.EQCOIN);
//				asset.setCreateHeight(changeLog.getHeight());
//				asset.deposit(ID.ZERO);
//				asset.setBalanceUpdateHeight(changeLog.getHeight());
//				asset.setNonce(ID.ZERO);
//				asset.setNonceUpdateHeight(changeLog.getHeight());
//				account.setAsset(asset);
//				account.setUpdateHeight(changeLog.getHeight());
//				changeLog.saveAccount(account);
//					Log.info("Original Account Numbers: " + changeLog.getTotalAccountNumbers());
//					changeLog.increaseTotalAccountNumbers();
//					Log.info("New Account Numbers: " + changeLog.getTotalAccountNumbers());
			}
		}
		return true;
	}
	
	public boolean isNewCompressedPublickeyListValid(ChangeLog changeLog) throws Exception {
		// Get the new Publickey's relevant readableLock list from Transactions
		Vector<String> relevantReadableLockList = new Vector<>();
		for (Transaction transaction : newTransactionList) {
			Passport account = changeLog.getFilter().getPassport(transaction.getTxIn().getPassportId(), true);
			Lock lock = changeLog.getFilter().getLock(account.getLockID(), true);
			if (lock.getPublickey() == null) {
				if (!relevantReadableLockList.contains(lock.getReadableLock())) {
					relevantReadableLockList.add(lock.getReadableLock());
				}
			}
		}
		if (newCompressedPublickeyList.size() != relevantReadableLockList.size()) {
			Log.Error("Publickey and relevant Lock's size doesn't equal");
			return false;
		}
		Lock key = null;
		for (int i = 0; i < newCompressedPublickeyList.size(); ++i) {
			// Check if it is unique
			for (int j = i + 1; j < newCompressedPublickeyList.size(); ++j) {
				if (newCompressedPublickeyList.get(i).equals(newCompressedPublickeyList.get(j))) {
					Log.Error("Publickey doesn't unique");
					return false;
				}
			}
			if (!LockTool.verifyAddressPublickey(relevantReadableLockList.get(i),
					newCompressedPublickeyList.get(i).getCompressedPublickey())) {
				return false;
			}
		}
		return true;
	}
	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		subchainHeader = new EQcoinSeedHeader(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		// Parse NewPassportList
		ARRAY passports = null;
		passports = EQCType.parseARRAY(is);
		Lock key = null;
		if (!passports.isNULL()) {
			ByteArrayInputStream is1 = new ByteArrayInputStream(passports.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				key = new Lock(is1);
				newHelixList.add(key);
			}
			EQCType.assertEqual(passports.size, newHelixList.size());
		}
		// Parse NewCompressedPublickeyList
		ARRAY compressedPublickeys = null;
		compressedPublickeys = EQCType.parseARRAY(is);
		CompressedPublickey compressedPublickey = null;
		if (!compressedPublickeys.isNULL()) {
			ByteArrayInputStream is1 = new ByteArrayInputStream(compressedPublickeys.elements);
			while (!EQCType.isInputStreamEnd(is1)) {
				compressedPublickey = new CompressedPublickey(is1);
				newCompressedPublickeyList.add(compressedPublickey);
			}
			EQCType.assertEqual(compressedPublickeys.size, newCompressedPublickeyList.size());
		}
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(super.getBodyBytes());
		os.write(getNewPassportListARRAY());
		os.write(getNewCompressedPublickeyListARRAY());
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(subchainHeader == null || newEQCSegWitList == null || newEQCSegWitList == null || newHelixList == null || newCompressedPublickeyList == null) {
			return false;
		}
		if(!subchainHeader.isSanity()) {
			return false;
		}
		if(!(newEQCSegWitList.isEmpty() && newEQCSegWitList.isEmpty() && newTransactionListLength == 0 && newCompressedPublickeyList.isEmpty())) {
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
	
	private byte[] getNewPassportListARRAY() {
		if (newHelixList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> passports = new Vector<byte[]>();
			for (Lock key : newHelixList) {
				passports.add(key.getBytes());
			}
			return EQCType.bytesArrayToARRAY(passports);
		}
	}
	
	private byte[] getNewCompressedPublickeyListARRAY() {
		if (newCompressedPublickeyList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		} else {
			Vector<byte[]> compressedPublickeys = new Vector<byte[]>();
			for (CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
				compressedPublickeys.add(compressedPublickey.getBin());
			}
			return EQCType.bytesArrayToARRAY(compressedPublickeys);
		}
	}

	public String toInnerJson() {
		return
				"\"EQcoinSubchain\":{\n" + subchainHeader.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n},\n" +
				"\"NewSignatureList\":" + 
						"\n{\n" +
						"\"Size\":\"" + newEQCSegWitList.size() + "\",\n" +
						"\"List\":" + 
							_getNewSignatureList() + "\n},\n" +
				"\"NewPassportList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newHelixList.size() + "\",\n" +
				"\"List\":" + 
				_getNewPassportList() + "\n},\n" +
				"\"NewCompressedPublickeyList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newCompressedPublickeyList.size() + "\",\n" +
				"\"List\":" + 
				_getNewCompressedPublickeyList() + "\n}\n" +		
				 "\n}\n}";
	}
	
	private String _getNewPassportList() {
		String tx = null;
		if (newHelixList != null && newHelixList.size() > 0) {
			tx = "\n[\n";
			if (newHelixList.size() > 1) {
				for (int i = 0; i < newHelixList.size() - 1; ++i) {
					tx += newHelixList.get(i) + ",\n";
				}
			}
			tx += newHelixList.get(newHelixList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}
	
	private String _getNewCompressedPublickeyList() {
		String tx = null;
		if (newCompressedPublickeyList != null && newCompressedPublickeyList.size() > 0) {
			tx = "\n[\n";
			if (newCompressedPublickeyList.size() > 1) {
				for (int i = 0; i < newCompressedPublickeyList.size() - 1; ++i) {
					tx += newCompressedPublickeyList.get(i) + ",\n";
				}
			}
			tx += newCompressedPublickeyList.get(newCompressedPublickeyList.size() - 1);
			tx += "\n]";
		} else {
			tx = "[]";
		}
		return tx;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchain#accountingTransaction(java.util.Vector)
	 */
	@Override
	public void plantingTransaction(Vector<Transaction> transactionList, ChangeLog changeLog) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		if (changeLog.getHeight().compareTo(Util.getMaxCoinbaseHeight(changeLog.getHeight())) < 0) {
			// Create CoinBase Transaction
			Lock lock = new Lock();
			lock.setReadableLock(Util.SINGULARITY_B);
			CoinbaseTransaction coinbaseTransaction = Util.generateCoinBaseTransaction(lock, changeLog);
			// Check if CoinBase isValid and update CoinBase's Account
			coinbaseTransaction.preparePlanting(changeLog);
			if (!coinbaseTransaction.isValid()) {
				throw new IllegalStateException("CoinbaseTransaction is invalid: " + coinbaseTransaction);
			} else {
				coinbaseTransaction.planting(changeLog);
			}
			// Add CoinBase into EQcoinSeedHeader
			addCoinbaseTransaction(coinbaseTransaction, changeLog);
		}
		
		// Handle every pending Transaction
		for (Transaction transaction : transactionList) {
			// If Transaction's TxIn is null or TxIn doesn't exists in Accounts just
			// continue
			if (transaction.getTxIn() == null
					|| transaction.getTxIn().getPassportId().compareTo(changeLog.getPreviousTotalPassportNumbers()) > 0) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("TxIn is null or TxIn doesn't exist in Accounts this is invalid just discard it: "
						+ transaction.toString());
				continue;
			}

			// If Transaction already in the EQcoinSubchain just continue
			if (isTransactionExists(transaction)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction already exists this is invalid just discard it: " + transaction.toString());
				continue;
			}
			
			// Check if Transaction isSanity
			if(!transaction.isSanity(TransactionShape.RPC)) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction isn't sanity just discard it: " + transaction.toString());
				continue;
			}
			
			// Init Transaction
			transaction.init(changeLog);
			
			// Prepare Transaction
			transaction.preparePlanting();

			// Check if Transaction is valid
			if (!transaction.isValid()) {
				EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				Log.Error("Transaction is invalid: " + transaction);
				continue;
			}

			// Add Transaction into EQcoinSubchain
			if ((getNewTransactionListLength()
					+ transaction.getBillingSize()) <= Util.MAX_BLOCK_SIZE) {
				Log.info("Add new Transaction which TxFee is: " + transaction.getTxFee());
				addTransaction(transaction, changeLog);
				// Update Transaction
				transaction.planting();
				// Update the TxFee
				getEQcoinSubchainHeader().depositTxFee(transaction.getTxFee());
			} else {
				Log.info("Exceed EQcoinSubchain's MAX_BLOCK_SIZE just stop accounting transaction");
				break;
			}
		}
		
//				// Update EQcoinSubchain's Header
//				EQcoinSubchainHeader preEQcoinSubchainHeader = changeLog.getEQCBlock(changeLog.getHeight().getPreviousID(), true).getEQcoinSubchain().getEQcoinSubchainHeader();
//				getEQcoinSubchainHeader().setTotalPassportNumbers(preEQcoinSubchainHeader.getTotalPassportNumbers().add(ID.valueOf(newHelixList.size())));
//				getEQcoinSubchainHeader().setTotalTransactionNumbers(preEQcoinSubchainHeader.getTotalTransactionNumbers().add(ID.valueOf(newTransactionList.size())));
				
				// Update EQcoin AssetSubchainAccount's Header
				EQcoinSeedPassport eQcoinSubchainPassport = (EQcoinSeedPassport) changeLog.getFilter().getPassport(ID.ONE, true);
				eQcoinSubchainPassport.setTotalSupply(new ID(Util.cypherTotalSupply(changeLog.getHeight())));
				eQcoinSubchainPassport.setTotalPassportNumbers(eQcoinSubchainPassport.getTotalPassportNumbers()
						.add(BigInteger.valueOf(newHelixList.size())));
				eQcoinSubchainPassport.setTotalTransactionNumbers(eQcoinSubchainPassport
						.getTotalTransactionNumbers().add(BigInteger.valueOf(newTransactionList.size())));
				eQcoinSubchainPassport.deposit(getEQcoinSubchainHeader().getTotalTxFee());
				// Here need update the total lock number
				// Save EQcoin Subchain's Header
				changeLog.getFilter().savePassport(eQcoinSubchainPassport);
				
				if (!getEQcoinSubchainHeader().getTotalPassportNumbers()
						.equals(eQcoinSubchainPassport.getTotalPassportNumbers())) {
					throw new IllegalStateException("TotalAccountNumbers is invalid");
				}
				
				if (!getEQcoinSubchainHeader().getTotalTransactionNumbers()
						.equals(eQcoinSubchainPassport.getTotalTransactionNumbers())) {
					throw new IllegalStateException("TotalTransactionNumbers is invalid");
				}

	}
	
	public boolean saveTransactions() throws Exception {
		return true;
	}
	
	public byte[] getRoot() throws Exception {
		Vector<byte[]> vector = new Vector<>();
		vector.add(subchainHeader.getBytes());
		vector.add(getNewTransactionListMerkelTreeRoot());
		vector.add(getNewEQCSegWitListMerkelTreeRoot());
		vector.add(getNewHelixListMerkelTreeRoot());
		vector.add(getNewCompressedPublickeyListMerkelTreeRoot());
		return Util.getMerkleTreeRoot(vector, true);
	}
	
	public byte[] getNewHelixListMerkelTreeRoot() throws Exception {
		Vector<byte[]> helixList = new Vector<byte[]>();
		for (Lock lock : newHelixList) {
			helixList.add(lock.getBytes(LockShape.AI));
		}
		return Util.getMerkleTreeRoot(helixList, true);
	}
	
	public byte[] getNewCompressedPublickeyListMerkelTreeRoot() throws Exception {
		Vector<byte[]> compressedPublickeyList = new Vector<byte[]>();
		for (CompressedPublickey compressedPublickey : newCompressedPublickeyList) {
			compressedPublickeyList.add(compressedPublickey.getBytes());
		}
		return Util.getMerkleTreeRoot(compressedPublickeyList, true);
	}
	
}
