/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.ut.misc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Savepoint;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.lock.LockTool;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.AssetPassport;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;
import org.junit.jupiter.api.Test;


/**
 * @author Xun Wang
 * @date Apr 25, 2019
 * @email 10509759@qq.com
 */
public class MiscTest {

	@Test
	void compressSignature() {
		byte[] bytes = null;
		try {
			bytes = MessageDigest.getInstance(Util.SHA3_256).digest(Util.getSecureRandomBytes());
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final java.math.BigInteger max = java.math.BigInteger.TWO.pow(128).subtract(java.math.BigInteger.ONE);
		final BigInteger middle = BigInteger.TWO.pow(64).subtract(BigInteger.ONE);
		final byte[] bytess = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
		final java.math.BigInteger aBigInteger = new java.math.BigInteger(1, bytes);
		final java.math.BigInteger devider = new java.math.BigInteger(1, bytess);
		final java.math.BigInteger result = aBigInteger.divide(max);
		final java.math.BigInteger mod = aBigInteger.subtract(max.multiply(result));
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
	void h2Test() {
		try {
			long begin = System.currentTimeMillis();
			Log.info("" + begin);
			for (int i = 0; i < 100000; ++i) {
				final Passport passport= Util.GS().getPassport(ID.ONE);
				GlobalStateH2.getInstance().savePassportSnapshot(passport, ID.ZERO);
			}
			long end = System.currentTimeMillis();
			Log.info("Total put time: " + (end - begin) + " ms");
			begin = System.currentTimeMillis();
			//						Log.info("" + Util.dumpBytes(rocksDB.get(columnFamilyHandles.get(1), SerialNumber.ZERO.getEQCBits()), 16));
			Log.info("" + begin);
			for (int i = 0; i < 100000; ++i) {
				GlobalStateH2.getInstance().getPassportSnapshot(ID.ONE, ID.ZERO);
			}
			end = System.currentTimeMillis();
			Log.info("Total get time: " + (end - begin) + " ms");
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void ID() {
		final ID id = ID.ZERO;
		final ID id2 = id.add(ID.ONE);
		assertEquals(id, ID.ZERO);
		assertEquals(id2, ID.ONE);
	}

	@Test
	void multipleExtendMixTest() {
		// MathContext mc = new MathContext(Util.HUNDREDPULS, RoundingMode.HALF_EVEN);
		final byte[] hash = new byte[67];
		for (int i = 0; i < hash.length; ++i) {
			hash[i] = (byte) 0xff;
		}
		try {
			Log.info(Util.bytesToHexString(Util.multipleExtendMix(hash, Util.TWO)));
			Log.info(Util.dumpBytesLittleEndianHex(Util.multipleExtendMix(hash, Util.TWO)));
			double time0 = System.currentTimeMillis();
			for (int i = 0; i < 1000000; ++i) {
				Util.multipleExtendMix(hash, Util.TWO);
			}
			time0 = System.currentTimeMillis() - time0;
			Log.info("Total time: " + time0 + " average time: " + time0 / 1000000);
			// [2020-04-20 21:51:03:003][com.eqcoin.misc.MiscTest.multipleExtendMixTest
			// 252]Total time: 36108.0 average time: 0.036108

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void multipleExtendTest() {
		final byte[] hash = new byte[67];
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
	void rollbackTest() {
		try {
			final Savepoint savepoint = Util.GS().setSavepoint();
			Util.GS().saveEQCHiveTailHeight(ID.THREE);
			Log.info("" + Util.GS().getEQCHiveTailHeight());
			Util.GS().saveEQCHiveTailHeight(ID.FIVE);
			Log.info("" + Util.GS().getEQCHiveTailHeight());
			Util.GS().rollback(savepoint);
			Log.info("" + Util.GS().getEQCHiveTailHeight());
			Util.GS().commit("Test");
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
	void snapshot() throws Exception {
		Passport account;
		try {
			account = GlobalStateH2.getInstance().getPassportSnapshot(ID.TWO.getNextID(), ID.ONE);
			//			 assertEquals(account.getAsset(Asset.EQCOIN).getBalanceUpdateHeight(), ID.ONE);
		} catch (NoSuchFieldException | IllegalStateException | ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testAnd() {
		final long begin = System.nanoTime();
		final String a = null, b = null, c = null, d = null, e = null, f = null;
		boolean isTrue = false;
		for (int i = 0; i < 100000000; ++i) {
			isTrue = (a != null && b != null && c != null && d != null && e != null && f != null);
		}
		final long end = System.nanoTime();
		Log.info("" + (end - begin));
		// [2020-04-26 17:53:18:018][com.eqcoin.misc.MiscTest.testAnd 287]2592400 this is for 1000000
		// [2020-04-26 17:55:52:052][com.eqcoin.misc.MiscTest.testAnd 287]3211600 this is for 100000000
	}

	@Test
	void testEQCBits() throws NoSuchFieldException, IllegalStateException, IOException {
		//			BigInteger bigInteger = BigInteger.valueOf(200000001);
		final BigInteger bigInteger = BigInteger.valueOf(128);
		//			Log.info(Util.dumpBytes(EQCType.bigIntegerToEQCBits(bigInteger), 16));
		assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(EQCCastle.bigIntegerToEQCBits(bigInteger)));
		final byte[] eqcBits = EQCCastle.bigIntegerToEQCBits(bigInteger);
		final ByteArrayInputStream is = new ByteArrayInputStream(eqcBits);
		final byte[] eqcBits1 = EQCCastle.parseEQCBits(is);
		assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(eqcBits1));
		assertArrayEquals(eqcBits, eqcBits1);
	}

	@Test
	void testEQCBits1() throws NoSuchFieldException, IllegalStateException, IOException {
		for (long i = 0; i < 1000000000l; ++i) {
			//			Log.info(""+i);
			final BigInteger bigInteger = BigInteger.valueOf(i);
			//			Log.info(Util.dumpBytes(EQCType.bigIntegerToEQCBits(bigInteger), 16));
			assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(EQCCastle.bigIntegerToEQCBits(bigInteger)));
			final byte[] eqcBits = EQCCastle.bigIntegerToEQCBits(bigInteger);
			final ByteArrayInputStream is = new ByteArrayInputStream(eqcBits);
			final byte[] eqcBits1 = EQCCastle.parseEQCBits(is);
			assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(eqcBits1));
			assertArrayEquals(eqcBits, eqcBits1);
		}
	}

	@Test
	void testIf() {
		final long begin = System.nanoTime();
		final String a = null, b = null, c = null, d = null, e = null, f = null;
		boolean isTrue = false;
		for (int i = 0; i < 100000000; ++i) {
			if(a == null) {
				isTrue = false;
			}
			if(b == null) {}
			if(c == null) {}
			if(d == null) {}
			if(e == null) {}
			if(f == null) {}
		}
		final long end = System.nanoTime();
		Log.info("" + (end - begin));
		// [2020-04-26 17:53:40:040][com.eqcoin.misc.MiscTest.testIf 307]2163500 this is for 1000000
		// [2020-04-26 17:57:16:016][com.eqcoin.misc.MiscTest.testIf 308]2789800 this is for 100000000
	}

	@Test
	void testKeystore() {
		UserProfile userProfile;
		Log.info("testKeystore");
		for (int i = 0; i < 30; ++i) {
			Log.info("i: " + i);
			userProfile = Keystore.getInstance().createUserProfile("nju2006", "abc", ECCTYPE.P521, "" + i);
			try {
				Log.info(LockTool.generateReadableLock(userProfile.getLockType(),
						Util.AESDecrypt(userProfile.getPublicKey(), "abc")));
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//			if(account.getAddress().length() > 51 || account.getAddress().length() < 49) {
			//			Log.info(account.getReadableLock() + " len: " + account.getReadableLock().length());
			//			}
		}
		Log.info("end");
	}

	@Test
	void testMix() {
		byte[] bytes, result;
		try {
			bytes = Util.getSecureRandomBytes();
			result = Util.mix(bytes);
			for(int i=0; i<1000000000; ++i) {
				assertArrayEquals(result, Util.mix(bytes));
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testMix2() {
		byte[] bytes, result;
		try {
			bytes = Util.getSecureRandomBytes();
			result = Util.mix(bytes);
			for(int i=0; i<10000; ++i) {
				bytes = Util.mix2(bytes);
				Log.info("i: " + i);
				assertNotNull(bytes);
			}
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testMixTime() {
		byte[] bytes, result;
		try {
			bytes = Util.getSecureRandomBytes();
			result = Util.mix(bytes);
			final double l0 = System.currentTimeMillis();
			final long times = 100000000;
			for(int i=0; i<times; ++i) {
				Util.mix(bytes);
			}
			final double l1 = System.currentTimeMillis();
			Log.info("Total times: " + times + "total time: " + (l1-l0) + " average time: " + (l1-l0)/times);
			//[2020-08-05 13:37:56:056][org.eqcoin.misc.MiscTest.testMixTime 478]Total times: 100000000total time: 588402.0 average time: 0.00588402
		} catch (NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void testPassportSanity() {
		final long begin = System.currentTimeMillis();
		Passport passport = null;
		try {
			passport = new AssetPassport();
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		passport.setId(ID.ZERO);
		passport.setLockID(ID.ZERO);
		passport.deposit(new Value(Util.ABC.multiply(BigInteger.valueOf(51))));
		passport.increaseNonce();
		for (int i = 0; i < 100000000; ++i) {
			try {
				passport.isSanity();
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final long end = System.currentTimeMillis();
		Log.info("" + (end - begin));
		// [2020-04-27 15:23:51:051][com.eqcoin.misc.MiscTest.testPassportSanity 333]841
	}

	@Test
	void testPassportSanity1() {
		final long begin = System.currentTimeMillis();
		Passport passport = null;
		try {
			passport = new AssetPassport();
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		passport.setId(ID.ZERO);
		passport.setLockID(ID.ZERO);
		passport.deposit(new Value(Util.ABC.multiply(BigInteger.valueOf(51))));
		passport.increaseNonce();
		for (int i = 0; i < 100000000; ++i) {
			try {
				//				passport.isSanity1();
				//				public boolean isSanity1() throws Exception {
				//					return (passportType != null && id != null && id.isSanity() && lockID != null && lockID.isSanity()
				//							&& balance != null && balance.isSanity() && balance.compareTo(Util.MIN_EQC) >= 0 && nonce != null && nonce.isSanity() && updateHeight != null
				//							&& updateHeight.isSanity());
				//				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final long end = System.currentTimeMillis();
		Log.info("" + (end - begin));
		// [2020-04-27 15:22:46:046][com.eqcoin.misc.MiscTest.testPassportSanity1 355]748

	}

	@Test
	void testTransmutedIntoCandidateTime() {
		byte[] bytes, result;
		try {
			bytes = Util.getSecureRandomBytes();
			result = Util.mix(bytes);
			final double l0 = System.currentTimeMillis();
			final long times = 100000000;
			for(int i=0; i<times; ++i) {
				Util.transmutedIntoCandidate(bytes);
			}
			final double l1 = System.currentTimeMillis();
			Log.info("Total times: " + times + " total time: " + (l1-l0) + " average time: " + (l1-l0)/times);
			//[2020-08-05 14:07:42:042][org.eqcoin.misc.MiscTest.testTransmutedIntoCandidateTime 515]Total times: 100000000 total time: 23870.0 average time: 2.387E-4
		} catch (NoSuchAlgorithmException | IOException e) {
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

	/**
	 * Due to the relevant snapshot will be deleted before check point so only
	 * support self re-verify EQCHive from check point to tail. This test will re
	 * write all relevant global state. So also can use this recovery global state
	 * to specific height. Due to self verify is a pseudo-demand so doesn't support
	 * this in future. If want re verify locally need recovery global state to
	 * specific height then valid each EQCHive.
	 */
	@Test
	void verifyEQCHive() {
		ID id;
		try {
			id = Util.GS().getEQCHiveTailHeight();
			Log.info("Current tail height: " + id);
			//			 for(int i=1; i<=id.intValue(); ++i) {
			//			   ChangeLog changeLog = new ChangeLog(new ID(i), new Filter(Mode.MINING));
			//			   EQCHive eqcBlock = new EQCHive(Util.GS().getEQCHive(new ID(i)));
			//			   eqcBlock.setChangeLog(changeLog);
			//			   Log.info("Begin verify No." + i + " EQCHive");
			//			   Log.info("\n" + eqcBlock.toString());
			//				assertTrue(eqcBlock.isValid());
			//				changeLog.clear();
			//				 Log.info("EQCHive No." + i + " verify passed");
			//			   }
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Test
	void verifyPublickey2Address() {
		final UserProfile userAccount = Keystore.getInstance().getUserProfileList().get(0);
		byte[] publickey = null;
		try {
			publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
		} catch (final Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.info("" + publickey.length);
		String readableAddress = null;
		try {
			readableAddress = LockTool.generateReadableLock(LockTool.getLockType(publickey), publickey);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		   assertEquals(readableAddress, userAccount.getReadableLock());
	}
	
	@Test
	void optimalNumOfBits() {
		long n = 1000000000;
		double p = 0.001;
		if (p == 0) {
			p = Double.MIN_VALUE;
		}
		Log.info("" + (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2))));
	}

	@Test
	void testEQCLight() throws Exception {
		//			BigInteger bigInteger = BigInteger.valueOf(200000001);
		final BigInteger bigInteger = BigInteger.valueOf(128);
		//			Log.info(Util.dumpBytes(EQCType.bigIntegerToEQCBits(bigInteger), 16));
		assertEquals(bigInteger, EQCCastle.eqcLightToBigInteger(EQCCastle.bigIntegerToEQCLight(bigInteger)));
		final byte[] eqcBits = EQCCastle.bigIntegerToEQCBits(bigInteger);
		final ByteArrayInputStream is = new ByteArrayInputStream(eqcBits);
		final byte[] eqcBits1 = EQCCastle.parseEQCLight(is);
		assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(eqcBits1));
		assertArrayEquals(eqcBits, eqcBits1);
	}

	@Test
	void testEQCLight1() throws NoSuchFieldException, IllegalStateException, IOException {
		for (long i = 0; i < 1000000000l; ++i) {
			//			Log.info(""+i);
			final BigInteger bigInteger = BigInteger.valueOf(i);
			//			Log.info(Util.dumpBytes(EQCType.bigIntegerToEQCBits(bigInteger), 16));
			assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(EQCCastle.bigIntegerToEQCBits(bigInteger)));
			final byte[] eqcBits = EQCCastle.bigIntegerToEQCBits(bigInteger);
			final ByteArrayInputStream is = new ByteArrayInputStream(eqcBits);
			final byte[] eqcBits1 = EQCCastle.parseEQCBits(is);
			assertEquals(bigInteger, EQCCastle.eqcBitsToBigInteger(eqcBits1));
			assertArrayEquals(eqcBits, eqcBits1);
		}
	}

}
