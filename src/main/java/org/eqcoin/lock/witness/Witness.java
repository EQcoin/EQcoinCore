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
package org.eqcoin.lock.witness;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Signature;
import java.sql.SQLException;

import org.eqcoin.crypto.RecoverySECP256R1Publickey;
import org.eqcoin.crypto.RecoverySECP521R1Publickey;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.publickey.Publickey;
import org.eqcoin.lock.publickey.T1Publickey;
import org.eqcoin.lock.publickey.T2Publickey;
import org.eqcoin.serialization.EQCInheritable;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.passport.Passport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TransactionType;
import org.eqcoin.transaction.TransferCoinbaseTransaction;
import org.eqcoin.transaction.ZionCoinbaseTransaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * Witness contains the transaction relevant witness parts for example signature.
 * 
 * @author Xun Wang
 * @date Mar 5, 2020
 * @email 10509759@qq.com
 */
public class Witness extends EQCObject {
	
	protected byte[] witness;
	protected Transaction transaction;
	protected Passport passport;
	
	public Witness() {
	}
	
	public Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Witness Parse(ByteArrayInputStream is) throws Exception {
		Witness witness = null;
		if (transaction.getLockType() ==LockType.T1) {
			witness = new T1Witness(is);
		} else if (transaction.getLockType() == LockType.T2) {
			witness = new T2Witness(is);
		} else {
			throw new IllegalStateException("Invalid lock type: " + transaction.getLockType());
		}
		return witness;
	}

	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws IOException {
		os.write(witness);
		return os;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return 
		
				"{\n" +
				toInnerJson() +
				"\n}";
		
	}
	
	public String toInnerJson() {
		return "";
	}

	public boolean isNull() {
		return true;
	}
	
	/**
	 * @return the witness
	 */
	public byte[] getWitness() {
		return witness;
	}

	/**
	 * @param witness the witness to set
	 * @throws Exception 
	 */
	public void setWitness(byte[] witness) throws Exception {
		this.witness = witness;
	}

	public byte[] getProof() {
		return null;
	}
	
	public BigInteger getMaxBillingLength() {
		return BigInteger.ZERO;
	}
	
	public Witness setTransaction(Transaction transaction) {
		this.transaction = transaction;
		return this;
	}
	
	public boolean verifySignature() throws Exception {
		return false;
	}

	public boolean isMeetPreCondition() throws Exception {
		return false;
	}
	
	public void planting() throws Exception {
		// Update current Transaction's relevant Account's AccountsMerkleTree's data
		// Update current Transaction's TxIn Account's relevant Asset's Nonce&Balance
		// Update current Transaction's TxIn Account's relevant Asset's Nonce
		passport.increaseNonce();
		// Update current Transaction's TxIn Account's relevant Asset's Balance
		passport.withdraw(transaction.getBillingValue());
		passport.setEQCHive(transaction.getEQCHive()).planting();

		// Deposit TxFee
		Value txFee = transaction.getTxFee();
		Value minerTxFee = txFee.multiply(Value.valueOf(5)).divide(Value.valueOf(100));
		Value eqCoinFederalTxFee = txFee.subtract(minerTxFee);
		Passport eqCoinFederalPassport = transaction.getEQCHive().getGlobalState().getPassport(ID.ZERO);
		Passport minerPassport = null;
		Transaction coinbaseTransaction = transaction.getEQCHive().getEQCoinSeeds().getNewTransactionList().get(0);
		if (coinbaseTransaction instanceof TransferCoinbaseTransaction) {
			TransferCoinbaseTransaction transferCoinbaseTransaction = (TransferCoinbaseTransaction) coinbaseTransaction;
			minerPassport = transaction.getEQCHive().getGlobalState()
					.getPassport(transferCoinbaseTransaction.getEqCoinMinerTxOut().getPassportId());
		} else {
			ZionCoinbaseTransaction zionCoinbaseTransaction = (ZionCoinbaseTransaction) coinbaseTransaction;
			// Here may need do more job
			ID minerLockId = transaction.getEQCHive().getGlobalState().isLockMateExists(zionCoinbaseTransaction.getEqCoinMinerTxOut().getLock());
			minerPassport = transaction.getEQCHive().getGlobalState().getPassportFromLockMateId(minerLockId);
		}
		eqCoinFederalPassport.deposit(eqCoinFederalTxFee);
		minerPassport.deposit(minerTxFee);
		eqCoinFederalPassport.setEQCHive(transaction.getEQCHive()).planting();
		minerPassport.setEQCHive(transaction.getEQCHive()).planting();
	}
	
	public void free() {
		transaction = null;
		passport = null;
	}

	/**
	 * @param passport the passport to set
	 */
	public void setPassport(Passport passport) {
		this.passport = passport;
	}
	
	/**
	 * @return the passport
	 */
	public Passport getPassport() {
		return passport;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isValid()
	 */
	@Override
	public boolean isValid() throws Exception {
		// Check if TxIn's passport id is less than previous EQCHive's total passport numbers
		if(passport.getId().compareTo(transaction.getEQCHive().getPreRoot().getTotalPassportNumbers()) >= 0) {
			Log.Error("Transaction's Passport relevant ID should less than previous EQCHive's total passport numbers");
			return false;
		}
		
		// Check if Nonce is correct
		if (!transaction.getNonce().isNextID(passport.getNonce())) {
			Log.Error("Nonce doesn't correct, current: " + transaction.getNonce() + " expect: " + passport.getNonce().getNextID());
			return false;
		}

		// Here exists one bug need do more job
		// Check balance from current Passport
		if (transaction.getBillingValue().add(Util.MIN_BALANCE).compareTo(passport.getBalance()) > 0) {
			Log.Error("Balance isn't enough");
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(witness == null) {
			Log.Error("witness == null");
			return false;
		}
		return true;
	}
	
	/**
	 * Forbidden relevant lock mate:
	 * Set it's status to forbidden and clear it's publickey.
	 * @throws Exception
	 */
	public void forbidden() throws Exception {}
	
}
