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
package com.eqcoin.transaction;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

import org.junit.jupiter.api.Test;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.lock.T2Lock;
import com.eqcoin.blockchain.transaction.TransferTxOut;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TxIn;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.operation.ChangeLockOP;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.blockchain.transaction.Transaction.TRANSACTION_PRIORITY;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.UserProfile;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.client.TransactionNetworkClient;
import com.eqcoin.test.TransTest;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.blockchain.transaction.ZionTransaction;

/**
 * @author Xun Wang
 * @date Jul 26, 2019
 * @email 10509759@qq.com
 */
public class TransactionTest {
	
	/**
	 * Transfer from Passport ID 0 to 1
	 */
	@Test
	void TransferTransaction() {
		UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(0);
		TransferTransaction transaction = new TransferTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(0.0001));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
//			UpdateEQCPublickeyOP updatePublickeyOP = new UpdateEQCPublickeyOP();
//			updatePublickeyOP.getEQCPublickey().setPublickey(publickey);
//			transaction.getOperationList().add(updatePublickeyOP);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			try {
				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
				ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Transaction Len: " + transaction.getBytes().length);
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Use Passport ID 0 create new Passport with the lock 3
	 */
	@Test
	void ZionTransaction() {
		UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(0);
	
		try {
			ZionTransaction transaction = new ZionTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(ID.ZERO);
			transaction.setTxIn(txIn);
			ZionTxOut txOut = new ZionTxOut();
			txOut.setLock(new T2Lock(Keystore.getInstance().getUserProfiles().get(2).getReadableLock()));
			txOut.setValue(Util.getValue(51));
			transaction.addTxOut(txOut);
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			try {
				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
				ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
			EQCLockMate eqcLockMate = new EQCLockMate();
			eqcLockMate.getEqcPublickey().setPublickey(publickey);
			transaction.setTxInLockMate(eqcLockMate);
			if(transaction.verifySignature()) {
				Log.info("Passed");
			}
			else {
				Log.info("Failed");
			}
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Change Passport ID 0's lock to lock 3
	 */
	@Test
	void TransferOPTransactionChangeLock() {
		UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(0);
		TransferOPTransaction transaction = new TransferOPTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(0.0001));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			ChangeLockOP changeLockOP = new ChangeLockOP();
			changeLockOP.setLock(new T2Lock(Keystore.getInstance().getUserProfiles().get(3).getReadableLock()));
			transaction.setOperation(changeLockOP);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			try {
				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
				ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	/**
	 * Transfer from Passport ID 0(use new lock 2) to 1
	 */
	@Test
	void TransferTransactionAfterChangeLock() {
		UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(3);
		TransferTransaction transaction = new TransferTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(0.0001));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
//			UpdateEQCPublickeyOP updatePublickeyOP = new UpdateEQCPublickeyOP();
//			updatePublickeyOP.getEQCPublickey().setPublickey(publickey);
//			transaction.getOperationList().add(updatePublickeyOP);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			try {
				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
				ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Transaction Len: " + transaction.getBytes().length);
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	void regresstionTest() {
//		TransTest.Zion(0, 2300, TRANSACTION_PRIORITY.ASAP, 2);
//		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, 3, 4, 5);
		TransTest.Tranfer(2, 1, TRANSACTION_PRIORITY.ASAP, 5);
//		TransTest.TranferChangeLock(2, 1, 13, TRANSACTION_PRIORITY.ASAP, 3);
//		TransTest.TranferChangeLock(3, 1, 12, TRANSACTION_PRIORITY.ASAP, 4);
	}
	
	@Test
	void regresstionChangeLockTest() {
		TransTest.TranferChangeLock(2, 1, 17, TRANSACTION_PRIORITY.ASAP, 3);
	}
	
	@Test
	void regresstionZeroZionTest() {
		TransTest.Zion(0, 2000, TRANSACTION_PRIORITY.ASAP, 2);
	}
	
	@Test
	void regresstionZionTest() {
		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, 3);
	}
	
	@Test
	void regresstionTransferTest() {
		TransTest.Tranfer(2, 1, TRANSACTION_PRIORITY.ASAP, 3);
	}
	
}
