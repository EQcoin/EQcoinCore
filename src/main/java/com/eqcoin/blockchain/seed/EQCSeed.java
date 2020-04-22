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
import java.util.Iterator;
import java.util.Vector;
import org.apache.velocity.runtime.directive.Parse;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport.PassportType;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.blockchain.transaction.ZionTransaction;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCSeed extends EQCSerializable {
	protected EQCSeedRoot eqcSeedRoot;
	protected Vector<Transaction> newTransactionList;
	// This is the new Transaction list's total size which should less than MAX_EQCHIVE_SIZE.
	protected int newTransactionListLength;
	protected ChangeLog changeLog;
	
	protected void init() {
		newTransactionList = new Vector<>();
	}
	
	public EQCSeed() {
		init();
	}
	
	public EQCSeed(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public EQCSeed(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(eqcSeedRoot == null || newTransactionList == null) {
			return false;
		}
		// Here need do more job to determine if current EQcoinSeed have transaction
//		if(!(newEQCSegWitList.isEmpty() && newEQCSegWitList.isEmpty() && newTransactionListLength == 0)) {
//			return false;
//		}
		long newTransactionListLength = 0;
		for(Transaction transaction:newTransactionList) {
			newTransactionListLength += transaction.getBytes().length;
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
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(eqcSeedRoot.getBytes());
		return os;
	}
	
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse NewTransactionList
		newTransactionList = EQCType.parseArray(is, new Transaction());
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCType.eqcSerializableListToArray(newTransactionList));
		return os;
	}

	/**
	 * @return the EQCSeedHeader
	 */
	public EQCSeedRoot getEQCSeedHeader() {
		return eqcSeedRoot;
	}

	/**
	 * @param EQCSeedRoot the EQCSeedHeader to set
	 */
	public void setEQCSeedHeader(EQCSeedRoot eqcSeedHeader) {
		this.eqcSeedRoot = eqcSeedHeader;
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
	
	public void addTransaction(Transaction transaction) throws ClassNotFoundException, SQLException, Exception {
			// Add Transaction
			newTransactionList.add(transaction);
			newTransactionListLength += transaction.getBytes().length;
	}
	
	public byte[] getProof() throws Exception {
		return null;
	}
	
	public byte[] getNewTransactionListProofRoot() throws Exception {
		if(newTransactionList.isEmpty()) {
			return EQCType.NULL_ARRAY;
		}
		else {
			Vector<byte[]> transactions = new Vector<byte[]>();
			for (Transaction transaction : newTransactionList) {
				transactions.add(transaction.getBytes());
			}
			return Util.getMerkleTreeRoot(transactions, true);
		}
	}
	
	public static EQcoinSeed parse(byte[] bytes) throws Exception {
		EQcoinSeed eQcoinSeed = null;
		eQcoinSeed = new EQcoinSeed(bytes);
		return eQcoinSeed;
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
				"\"EQCSubchain\":{\n" + eqcSeedRoot.toInnerJson() + ",\n" +
				"\"NewTransactionList\":" + 
				"\n{\n" +
				"\"Size\":\"" + newTransactionList.size() + "\",\n" +
				"\"List\":" + 
					_getNewTransactionList() + "\n}\n}";
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
	
	public void plantingTransaction(Vector<Transaction> transactionList) throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		
	}
	
	public boolean saveTransactions() throws Exception {
		return false;
	}
	
	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
		eqcSeedRoot.setChangeLog(changeLog);
	}
	
}
