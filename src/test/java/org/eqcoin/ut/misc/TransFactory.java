/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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
package org.eqcoin.ut.misc;

import java.security.Signature;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.witness.T2Witness;
import org.eqcoin.lock.witness.Witness;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.ZionTransaction;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.transaction.operation.ChangeLock;
import org.eqcoin.transaction.txout.TransferTxOutQuantum;
import org.eqcoin.transaction.txout.ZionTxOut;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date May 31, 2020
 * @email 10509759@qq.com
 */
public class TransFactory {

	public static Transaction Tranfer(int fromId, double value, TRANSACTION_PRIORITY priority, int... toIds) {
		TransferTransaction transaction = null;
		try {
			Passport passport = Util.GS().getPassport(new ID(fromId));
			LockMate eqcLockMate = Util.GS().getLockMate(passport.getLockID());
			UserProfile userProfile = Keystore.getInstance().getUserProfileList().get(eqcLockMate.getId().intValue());
			transaction = new TransferTransaction();
			Witness witness = new T2Witness();
			witness.setPassport(passport);
			transaction.setWitness(witness);
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_POWER_PRICE));
	
			for (int id : toIds) {
				TransferTxOutQuantum txOut = new TransferTxOutQuantum();
				txOut.setPassportId(new ID(id));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}
			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP);
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			ecdsa.update(transaction.getSignBytesHash());
			witness.setWitness(ecdsa.sign());
			transaction.setWitness(witness);
			Log.info("Signature Len: " + transaction.getWitness().getBytes().length);
			Log.info("Transaction Len: " + transaction.getBytes().length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transaction;
	}
	
	public static Transaction TranferChangeLock(int fromId, double value, int newLockId, TRANSACTION_PRIORITY priority, int... toIds) {
		TransferOPTransaction transaction = null;
		try {
			Passport passport = Util.GS().getPassport(new ID(fromId));
			LockMate eqcLockMate = Util.GS().getLockMate(passport.getLockID());
			UserProfile userProfile = Keystore.getInstance().getUserProfileList().get(eqcLockMate.getId().intValue());
			transaction = new TransferOPTransaction();
			Witness witness = new T2Witness();
			witness.setPassport(passport);
			transaction.setWitness(witness);
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_POWER_PRICE));

			for (int id : toIds) {
				TransferTxOutQuantum txOut = new TransferTxOutQuantum();
				txOut.setPassportId(new ID(id));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}

			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			ChangeLock changeLock = new ChangeLock();
			changeLock.setLock(LockTool.publickeyToEQCLock(Keystore.getInstance().getUserProfileList().get(newLockId).getLockType(), Keystore.getInstance().getUserProfileList().get(newLockId).getPublicKey()));
			transaction.setOperation(changeLock);
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP);
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			ecdsa.update(transaction.getSignBytesHash());
			witness.setWitness(ecdsa.sign());
			transaction.setWitness(witness);
			Log.info("Signature Len: " + transaction.getWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return transaction;
	}
	
	public static Transaction Zion(int fromId, double value, TRANSACTION_PRIORITY priority, int... zions) {
		ZionTransaction transaction = null;
		try {
			Passport passport = Util.GS().getPassport(new ID(fromId));
//			LockMate eqcLockMate = Util.DB().getLock(passport.getLockID(), Mode.GLOBAL);
			UserProfile userProfile = Keystore.getInstance().getUserProfileList().get(passport.getLockID().intValue());
			transaction = new ZionTransaction();
			Witness witness = new T2Witness();
			witness.setPassport(passport);
			transaction.setWitness(witness);
			transaction.setLockType(LockType.T2);
			transaction.setTxFeeRate(new Value((long) Util.DEFAULT_POWER_PRICE));

			for (int i : zions) {
				ZionTxOut txOut = new ZionTxOut();
				txOut.setLock(LockTool.publickeyToEQCLock(Keystore.getInstance().getUserProfileList().get(i).getLockType(), Keystore.getInstance().getUserProfileList().get(i).getPublicKey()));
				txOut.setValue(Util.getValue(value));
				transaction.addTxOut(txOut);
			}

			transaction.setNonce(passport.getNonce().getNextID());
			byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(userProfile.getPublicKey(), "abc");
			transaction.setPriority(TRANSACTION_PRIORITY.ASAP);
			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
			Log.info("getPriorityRate: " + transaction.getPriorityValue());
			Log.info("getTransactionPriority: " + transaction.getPriority());

			Signature ecdsa = null;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
			ecdsa.update(transaction.getSignBytesHash());
			transaction.getWitness().setWitness(ecdsa.sign());
			Log.info("Signature Len: " + transaction.getWitness().getBytes().length);
			Log.info("Len: " + transaction.getBytes().length);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return transaction;
	}
	
}
