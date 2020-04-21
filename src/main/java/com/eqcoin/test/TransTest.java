/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqcoin.test;

import com.eqcoin.blockchain.transaction.Transaction.TRANSACTION_PRIORITY;
import com.eqcoin.blockchain.transaction.operation.ChangeLockOP;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.UserProfile;
import com.eqcoin.persistence.EQCBlockChainH2;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;

import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.LockTool;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.lock.T2Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.TransferTxOut;
import com.eqcoin.blockchain.transaction.TxIn;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.ZionTransaction;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 19, 2020
 * @email 10509759@qq.com
 */
public class TransTest {

	public static void Tranfer(int fromId, double value, TRANSACTION_PRIORITY priority, int... toIds) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
			EQCLockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
			UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(eqcLockMate.getId().intValue());
			TransferTransaction transaction = new TransferTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(passport.getId());
			transaction.setTxIn(txIn);
			for (int id : toIds) {
				TransferTxOut txOut = new TransferTxOut();
				txOut.setPassportId(new ID(id));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}
			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Transaction Len: " + transaction.getBytes().length);
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//			if(eqcLockMate.getEqcPublickey().isNULL()) {
//				eqcLockMate.getEqcPublickey().setPublickey(publickey);
//			}
			Log.info(Util.bytesToHexString(publickey));
			Log.info(Util.bytesToHexString(eqcLockMate.getEqcPublickey().getPublickey()));
			T2Lock t2Lock = new T2Lock(LockTool.AIToReadableLock(LockTool.publickeyToAI(publickey)));
			Log.info("Pub code: " + Util.bytesToHexString(t2Lock.getLockCode()));
			Log.info("Lock code: " + Util.bytesToHexString(eqcLockMate.getLock().getLockCode()));
			transaction.setTxInLockMate(eqcLockMate);
			if(LockTool.verifyEQCLockAndPublickey(eqcLockMate.getLock(), publickey)) {
				Log.info("valid");
			}
			else {
				Log.Error("invalid");
			}
//			if(transaction.verifySignature()) {
//				Log.info("Passed");
//			}
//			else {
//				Log.info("Failed");
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void TranferChangeLock(int fromId, double value, int newLockId, TRANSACTION_PRIORITY priority,
			int... toIds) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
			EQCLockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
			UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(eqcLockMate.getId().intValue());
			TransferOPTransaction transaction = new TransferOPTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(passport.getId());
			transaction.setTxIn(txIn);

			for (int id : toIds) {
				TransferTxOut txOut = new TransferTxOut();
				txOut.setPassportId(new ID(id));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}

			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			ChangeLockOP changeLockOP = new ChangeLockOP();
			changeLockOP.setLock(new T2Lock(Keystore.getInstance().getUserProfiles().get(newLockId).getReadableLock()));
			transaction.setOperation(changeLockOP);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void Zion(int fromId, double value, TRANSACTION_PRIORITY priority, int... zions) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
			EQCLockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
			UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(eqcLockMate.getId().intValue());
			ZionTransaction transaction = new ZionTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(passport.getId());
			transaction.setTxIn(txIn);

			for (int i : zions) {
				ZionTxOut txOut = new ZionTxOut();
				txOut.setLock(new T2Lock(Keystore.getInstance().getUserProfiles().get(i).getReadableLock()));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}

			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
			Log.info("getTxIn().getValue: " + transaction.getTxIn().getValue());
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			transaction.sign(ecdsa);
			Log.info("Signature Len: " + transaction.getEqcWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
//		EQCLockMate eqcLockMate = new EQCLockMate();
//		eqcLockMate.getEqcPublickey().setPublickey(publickey);
//		transaction.setTxInLockMate(eqcLockMate);
//		if(transaction.verifySignature()) {
//			Log.info("Passed");
//		}
//		else {
//			Log.info("Failed");
//		}
			EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
