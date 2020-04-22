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
package com.eqcoin.misc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.LockTool;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.UserProfile;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

import jdk.nashorn.internal.objects.Global;


/**
 * @author Xun Wang
 * @date Apr 25, 2019
 * @email 10509759@qq.com
 */
public class MiscTest {
	
	@Test
	void saveAccount() {
//		Account account = new AssetAccount();
//		Passport passport = new Passport();
//		passport.setReadableAddress(Keystore.getInstance().getUserAccounts().get(0).getReadableAddress());
//		passport.setID(ID.ONE);
//		account.setPassport(passport);
//		account.setLockCreateHeight(ID.ZERO);
//		account.getAsset(Asset.EQCOIN).deposit(new ID(Util.MIN_EQC));
//		try {
//			EQCBlockChainRocksDB.getInstance().saveAccount(account);
//			Account account2 = EQCBlockChainRocksDB.getInstance().getAccount(ID.ONE);
//			assertEquals(account, account2);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	   @Test
	   void ID() {
		   ID id = ID.ZERO;
		   ID id2 = id.add(ID.ONE);
		   assertEquals(id, ID.ZERO);
		   assertEquals(id2, ID.ONE);
	   }
	   
	   @Test
	   void snapshot() throws Exception {
		   Passport account;
		try {
			account = EQCBlockChainH2.getInstance().getPassportSnapshot(ID.TWO.getNextID(), ID.ONE);
//			 assertEquals(account.getAsset(Asset.EQCOIN).getBalanceUpdateHeight(), ID.ONE);
		} catch (NoSuchFieldException | IllegalStateException | ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   @Test
	   void verifyAccountsMerkelTreeRoot() {
//		   ID id;
//		try {
//			id = EQCBlockChainRocksDB.getInstance().getEQCBlockTailHeight();
//			 for(int i=22; i<=id.intValue(); ++i) {
//				   AccountsMerkleTree changeLog = new AccountsMerkleTree(new ID(i), new Filter(Mode.MINING));
//					changeLog.buildAccountsMerkleTree();
//					changeLog.generateRoot();
//					Log.info(Util.dumpBytes(changeLog.getRoot(), 16));
//					EQCHive eqcBlock = EQCBlockChainRocksDB.getInstance().getEQCHive(new ID(i), true);
//					changeLog.clear();
//					Log.info("Begin verify No. " + i);
//					assertArrayEquals(changeLog.getRoot(), eqcBlock.getRoot().getAccountsMerkelTreeRoot());
//					Log.info("Passed verify No. " + i);
//				   }
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	   }
	   
	   @Test
	   void verifyEQCHive() {
		   ID id;
		try {
			id = Util.DB().getEQCHiveTailHeight();
			Log.info("Current tail height: " + id);
			 for(int i=1; i<=id.intValue(); ++i) {
			   ChangeLog changeLog = new ChangeLog(new ID(i), new Filter(Mode.MINING));
			   EQCHive eqcBlock = new EQCHive(Util.DB().getEQCHive(new ID(i)));
			   eqcBlock.setChangeLog(changeLog);
			   Log.info("Begin verify No." + i + " EQCHive");
			   Log.info("\n" + eqcBlock.toString());
				assertTrue(eqcBlock.isValid());
				changeLog.clear();
				 Log.info("EQCHive No." + i + " verify passed");
			   }
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   }
	   
	   @Test
	   void verifyPublickey2Address() {
		   UserProfile userAccount = Keystore.getInstance().getUserProfiles().get(0);
		   byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
		   Log.info("" + publickey.length);
		   String readableAddress = null;
		try {
			readableAddress = LockTool.generateLock(publickey, LockTool.getLockType(publickey));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   assertEquals(readableAddress, userAccount.getReadableLock());
	   }
	   
	   @Test
	   void rocksDBCompress() {
//				try {
//					byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE);
//					long begin = System.currentTimeMillis();
//					Log.info("" + begin);
//					for (int i = 0; i < 100000; ++i) {
//						EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT, ID.valueOf(i).getEQCBits(), bytes);
//					}
//					long end = System.currentTimeMillis();
//					Log.info("Total put time: " + (end - begin) + " ms");
//					begin = System.currentTimeMillis();
////						Log.info("" + Util.dumpBytes(rocksDB.get(columnFamilyHandles.get(1), SerialNumber.ZERO.getEQCBits()), 16));
//					Log.info("" + begin);
//					for (int i = 0; i < 100000; ++i) {
//						EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, ID.valueOf(i).getEQCBits());
//					}
//					end = System.currentTimeMillis();
//					Log.info("Total get time: " + (end - begin) + " ms");
//				} catch (RocksDBException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
	   }
	   
	   @Test
	   void h2Test() {
				try {
					byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE);
					long begin = System.currentTimeMillis();
					Log.info("" + begin);
					for (int i = 0; i < 100000; ++i) {
						Passport account = Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
						EQCBlockChainH2.getInstance().savePassportSnapshot(account.getId(), account.getBytes(), ID.ZERO);
					}
					long end = System.currentTimeMillis();
					Log.info("Total put time: " + (end - begin) + " ms");
					begin = System.currentTimeMillis();
//						Log.info("" + Util.dumpBytes(rocksDB.get(columnFamilyHandles.get(1), SerialNumber.ZERO.getEQCBits()), 16));
					Log.info("" + begin);
					for (int i = 0; i < 100000; ++i) {
						EQCBlockChainH2.getInstance().getPassportSnapshot(ID.ONE, ID.ZERO);
					}
					end = System.currentTimeMillis();
					Log.info("Total get time: " + (end - begin) + " ms");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	   }
	   
	   @Test
	   void compressSignature() {
		   byte[] bytes = Util.SHA3_256(Util.getSecureRandomBytes());
			java.math.BigInteger max = java.math.BigInteger.TWO.pow(128).subtract(java.math.BigInteger.ONE);
			BigInteger middle = BigInteger.TWO.pow(64).subtract(BigInteger.ONE);
			byte[] bytess = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
			java.math.BigInteger aBigInteger = new java.math.BigInteger(1, bytes);
			java.math.BigInteger devider = new java.math.BigInteger(1, bytess);
			java.math.BigInteger result = aBigInteger.divide(max);
			java.math.BigInteger mod = aBigInteger.subtract(max.multiply(result));
			if(result.compareTo(middle) > 0) {
				Log.info("fr: " + result.subtract(middle) + " Len: " + result.subtract(middle).toByteArray().length);
			}
			if(mod.compareTo(middle) > 0) {
				Log.info("fm: " + mod.subtract(middle) + " Len: " + mod.subtract(middle).toByteArray().length);
			}
			if(aBigInteger.equals(result.multiply(devider).add(mod))) {
				Log.info("tr");
			}
			Log.info("" + result.toByteArray().length + " result: " + result + " mod: " + mod + " " + mod.toByteArray().length);
	   }
	   
	   @Test
	   void multipleExtendMixTest() {
		   // MathContext mc = new MathContext(Util.HUNDREDPULS, RoundingMode.HALF_EVEN);
		   byte[] hash = new byte[67];
		   for(int  i=0; i<hash.length; ++i) {
			   hash[i] = (byte) 0xff;
		   }
		   Log.info(Util.bytesToHexString(Util.multipleExtendMix(hash, Util.TWO)));
		   Log.info(Util.dumpBytesLittleEndianHex(Util.multipleExtendMix(hash, Util.TWO)));
		   double time0 = System.currentTimeMillis();
		   for(int i=0; i<1000000; ++i) {
			   Util.multipleExtendMix(hash, Util.TWO);
		   }
		   time0 = System.currentTimeMillis() - time0;
		   Log.info("Total time: " + time0 + " average time: " + time0/1000000);
		   // [2020-04-20 21:51:03:003][com.eqcoin.misc.MiscTest.multipleExtendMixTest 252]Total time: 36108.0 average time: 0.036108
	   }
	   
	   @Test
	   void multipleExtendTest() {
		   byte[] hash = new byte[67];
		   for(int  i=0; i<hash.length; ++i) {
			   hash[i] = (byte) 0xff;
		   }
		   Log.info(Util.bytesToHexString(Util.multipleExtend(hash, Util.THREE)));
		   Log.info(Util.dumpBytesLittleEndianHex(Util.multipleExtend(hash, Util.THREE)));
		   double time0 = System.currentTimeMillis();
		   for(int i=0; i<1000000; ++i) {
			   Util.multipleExtend(hash, Util.THREE);
		   }
		   time0 = System.currentTimeMillis() - time0;
		   Log.info("Total time: " + time0 + " average time: " + time0/1000000);
		   // [2020-04-20 21:54:34:034][com.eqcoin.misc.MiscTest.multipleExtendTest 269]Total time: 82.0 average time: 8.2E-5
	   }
	   
}
