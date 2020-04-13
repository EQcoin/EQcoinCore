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
import com.eqcoin.blockchain.transaction.TransferTxOut;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TxIn;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.operation.ChangeLockOP;
import com.eqcoin.blockchain.transaction.operation.UpdateEQCPublickeyOP;
import com.eqcoin.crypto.EQCPublicKey;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.blockchain.transaction.Transaction.TRANSACTION_PRIORITY;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.UserAccount;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.client.TransactionNetworkClient;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;
import com.eqcoin.blockchain.transaction.ZionTransaction;

/**
 * @author Xun Wang
 * @date Jul 26, 2019
 * @email 10509759@qq.com
 */
public class TransactionTest {
	@Test
	void TransferOPTransactionVirgin() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(0);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(1);
		TransferOPTransaction transaction = new TransferOPTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(500));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
			UpdateEQCPublickeyOP updatePublickeyOP = new UpdateEQCPublickeyOP();
			updatePublickeyOP.getEQCPublickey().setPublickey(publickey);
			transaction.getOperationList().add(updatePublickeyOP);
			transaction.getTxInLockMate().getEqcPublickey().setPublickey(publickey);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			try {
//				 Signature signature = Signature.getInstance("SHA256withECDSA");
				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
				ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBin().length);
			Log.info(Util.getHexString(transaction.getEqcWitness().getBytes()));
			Log.info("Len: " + transaction.getBytes().length);
			if (transaction.verifySignature()) {
				Log.info("passed");
//				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
				TransferOPTransaction transaction2 = (TransferOPTransaction) Transaction.parseTransaction(transaction.getBytes());
				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//				IPList<O> ipList = Util.DB().getMinerList();
//				for(IP ip:ipList.getIpList()) {
//					TransactionNetworkClient.sendTransaction(transaction, ip);
//				}
			} else {
				Log.info("failed");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	void TransferTransaction() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(0);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(1);
		TransferTransaction transaction = new TransferTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(500));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
//			UpdateEQCPublickeyOP updatePublickeyOP = new UpdateEQCPublickeyOP();
//			updatePublickeyOP.getEQCPublickey().setPublickey(publickey);
//			transaction.getOperationList().add(updatePublickeyOP);
				transaction.getTxInLockMate().getEqcPublickey().setPublickey(publickey);
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
			Log.info("Signature Len: " + transaction.getEqcWitness().getBin().length);
			Log.info("Len: " + transaction.getBytes().length);
			if (transaction.verifySignature()) {
				Log.info("passed");
//				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//				IPList<O> ipList = Util.DB().getMinerList();
//				for(IP ip:ipList.getIpList()) {
//					TransactionNetworkClient.sendTransaction(transaction, ip);
//				}
			} else {
				Log.info("failed");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	void TransferOPTransactionChangeLock() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(0);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(1);
		TransferOPTransaction transaction = new TransferOPTransaction();
		transaction.setLockType(LockType.T2);
		transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
		TxIn txIn = new TxIn();
		txIn.setPassportId(ID.ZERO);
		transaction.setTxIn(txIn);
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(500));
		transaction.addTxOut(txOut);
		try {
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
			ChangeLockOP changeLockOP = new ChangeLockOP();
			changeLockOP.getLock().cloneFromReadableLock(Keystore.getInstance().getUserAccounts().get(2).getReadableLock());
			transaction.getOperationList().add(changeLockOP);
				transaction.getTxInLockMate().getEqcPublickey().setPublickey(publickey);
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
			Log.info("Signature Len: " + transaction.getEqcWitness().getBin().length);
			Log.info("Len: " + transaction.getBytes().length);
			if (transaction.verifySignature()) {
				Log.info("passed");
//				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
				TransferOPTransaction transaction2 = (TransferOPTransaction) Transaction.parseTransaction(transaction.getBytes());
				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//				IPList<O> ipList = Util.DB().getMinerList();
//				for(IP ip:ipList.getIpList()) {
//					TransactionNetworkClient.sendTransaction(transaction, ip);
//				}
			} else {
				Log.info("failed");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Test
	void ZionTransaction() {
		UserAccount userAccount = Keystore.getInstance().getUserAccounts().get(0);
		UserAccount userAccount1 = Keystore.getInstance().getUserAccounts().get(1);
	
		try {
			ZionTransaction transaction = new ZionTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long)Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(ID.ZERO);
			transaction.setTxIn(txIn);
			ZionTxOut txOut = new ZionTxOut();
			txOut.getLock().cloneFromReadableLock("22iijEhRj5Cs7MYQXQBjHsCGDsdbz3Zy1iokTaYnwGxrVUDkwY7");
			txOut.setValue(Util.getValue(500));
			transaction.addTxOut(txOut);
			transaction.setNonce(Util.DB().getPassport(txIn.getPassportId(), Mode.GLOBAL).getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
//			UpdateEQCPublickeyOP updatePublickeyOP = new UpdateEQCPublickeyOP();
//			updatePublickeyOP.getEQCPublickey().setPublickey(publickey);
//			transaction.getOperationList().add(updatePublickeyOP);
				transaction.getTxInLockMate().getEqcPublickey().setPublickey(publickey);
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
			Log.info("Signature Len: " + transaction.getEqcWitness().getBin().length);
			Log.info("Len: " + transaction.getBytes().length);
			if (transaction.verifySignature()) {
				Log.info("passed");
//				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
				ZionTransaction zionTransaction = (com.eqcoin.blockchain.transaction.ZionTransaction) Transaction.parseTransaction(transaction.getBytes());
				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//				IPList<O> ipList = Util.DB().getMinerList();
//				for(IP ip:ipList.getIpList()) {
//					TransactionNetworkClient.sendTransaction(transaction, ip);
//				}
			} else {
				Log.info("failed");
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
