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
package org.eqcoin.test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.util.concurrent.locks.Lock;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Filter;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.T2Lock;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.passport.Passport;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.rpc.client.EQCTransactionNetworkClient;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.TransferTxOut;
import org.eqcoin.transaction.TxIn;
import org.eqcoin.transaction.Value;
import org.eqcoin.transaction.ZionTransaction;
import org.eqcoin.transaction.ZionTxOut;
import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.transaction.operation.ChangeLock;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 19, 2020
 * @email 10509759@qq.com
 */
public class TransTest {

	public static void Tranfer(int fromId, double value, TRANSACTION_PRIORITY priority, boolean isRpc, int... toIds) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
			LockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
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
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				EQCHiveH2.getInstance().saveTransactionInPool(transaction);
			}
//			if(eqcLockMate.getEqcPublickey().isNULL()) {
//				eqcLockMate.getEqcPublickey().setPublickey(publickey);
//			}
			
			T2Lock t2Lock = (T2Lock) LockTool.publickeyToEQCLock(LockType.T2, publickey);
			Log.info("Pub proof: " + Util.bytesToHexString(t2Lock.getLockProof()));
			Log.info("Lock proof: " + Util.bytesToHexString(eqcLockMate.getLock().getLockProof()));
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
	
	public static void TranferChangeLock(int fromId, double value, int newLockId, TRANSACTION_PRIORITY priority, boolean isRpc,
			int... toIds) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
			LockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
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
			ChangeLock changeLock = new ChangeLock();
			changeLock.setLock(LockTool.readableLockToEQCLock(Keystore.getInstance().getUserProfiles().get(newLockId).getReadableLock()));
			transaction.setOperation(changeLock);
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
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				EQCHiveH2.getInstance().saveTransactionInPool(transaction);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void Zion(int fromId, double value, TRANSACTION_PRIORITY priority, boolean isRpc, int... zions) {
		try {
			Passport passport = Util.DB().getPassport(new ID(fromId), Mode.GLOBAL);
//			LockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
			UserProfile userProfile = Keystore.getInstance().getUserProfiles().get(passport.getLockID().intValue());
			ZionTransaction transaction = new ZionTransaction();
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_TXFEE_RATE));
			TxIn txIn = new TxIn();
			txIn.setPassportId(passport.getId());
			transaction.setTxIn(txIn);

			for (int i : zions) {
				ZionTxOut txOut = new ZionTxOut();
				txOut.setLock(LockTool.readableLockToEQCLock(Keystore.getInstance().getUserProfiles().get(i).getReadableLock()));
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
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				EQCHiveH2.getInstance().saveTransactionInPool(transaction);
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
