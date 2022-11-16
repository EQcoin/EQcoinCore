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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Vector;

import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.util.Base58;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * All testes at here need move to MiscTest in UT
 * @author Xun Wang
 * @date Oct 15, 2018
 * @email 10509759@qq.com
 */
public class Test {

	public static void testHashTime() {
		EQCHiveRoot header = new EQCHiveRoot();
		
		try {
			header.setNonce(ID.ONE);
			header.setPreProof(Util.getSecureRandomBytes());
			header.setTarget(Util.getDefaultTargetBytes());
			header.setHeight(ID.ZERO);
			header.setTimestamp(new ID(System.currentTimeMillis()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Log.info(header.toString());
		long c0 = System.currentTimeMillis();
		int n = 100;
		for (int i = 0; i < n; ++i) {
//			Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(header.getBytes(), Util.HUNDRED_THOUSAND);
//			Util.EQCCHA_MULTIPLE_DUAL_MIX(header.getBytes(), 10509, true, false);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}
	
	public static void test999Len() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<1001; ++i) {
			sb.append("9");
		}
		Log.info("Len:" + new BigInteger(sb.toString()).toByteArray().length);
	}

	public static void testMultiExtendTime() throws NoSuchAlgorithmException {
//		EQCHeader header = new EQCHeader();
//		header.setNonce(ID.ONE);
//		header.setPreHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, true));
//		header.setTimestamp(new ID(System.currentTimeMillis()));
//		Log.info(header.toString());
		byte[] asd = Util.getSecureRandomBytes();
		byte[] asdf = new byte[64];
		System.arraycopy(asd, 0, asdf, 0, asdf.length);
//		for(int i=0; i<asdf.length; ++i) {
//			asdf[i] = (byte) 0xFF;
//		}
		long c0 = System.currentTimeMillis();
		int n = 100000;
		for (int i = 0; i < n; ++i) {
			Util.multipleExtend(asdf, Util.HUNDREDPULS);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}
	
//	const calculate = (d, n) => {
//		  const exponent = (-n * (n - 1)) / (2 * d)
//		  return 1 - Math.E ** exponent;
//		}
	public static double testHashCounter(double values, double range) {
		double vv = ((-values*(values-1))/(2*range));
		Log.info("vv: " + vv);
		Log.info("Math.exp(vv): " + Math.exp(vv));
//		BigDecimal aBigDecimal = new BigDecimal(Math.E).po
		return 1- Math.exp(vv);
	}

	public static void testMultiExtendTimeMix() throws NoSuchAlgorithmException {
//		EQCHeader header = new EQCHeader();
//		header.setNonce(ID.ONE);
//		header.setPreHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, true));
//		header.setTimestamp(new ID(System.currentTimeMillis()));
//		Log.info(header.toString());
		byte[] asd = Util.getSecureRandomBytes();
		byte[] asdf = new byte[64];
		System.arraycopy(asd, 0, asdf, 0, asdf.length);
//		for(int i=0; i<asdf.length; ++i) {
//			asdf[i] = (byte) 0xFF;
//		}
		long c0 = System.currentTimeMillis();
		int n = 100;
		for (int i = 0; i < n; ++i) {
			asdf = Util.getSecureRandomBytes();
			try {
				Util.multipleExtendMix(asdf, Util.HUNDREDPULS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}

	public static void testEQCCHA_MULTIPLETime() {
		long c0 = System.currentTimeMillis();
		try {
			byte[] asd = Util.getSecureRandomBytes();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int n = 100;
		for (int i = 0; i < n; ++i) {
//			Util.EQCCHA_MULTIPLE_DUAL(asd, Util.HUNDREDPULS, true, false);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}

	public static void testECPubKeySignature(ECCTYPE type, String text) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			ECPublicKey ecPublicKey = (ECPublicKey) pubKey;
			Log.info(pubKey.toString() + " public key's len: " + pubKey.getEncoded().length + " ec x: "
					+ ecPublicKey.getW().getAffineX().toByteArray().length);
			EQCECCPublicKey eqPublicKey = new EQCECCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			Log.info("Compress Public Key's len: " + eqPublicKey.getCompressedPublicKeyEncoded().length
					+ "\nPublic Key:" + eqPublicKey.getCompressedPublicKeyEncoded()[0]);

			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(privKey);
//			String text = "In teaching others we teach ourselves";
			System.out.println("Text len: " + text.length());
			byte[] baText = text.getBytes();
			ecdsa.update(baText);
			byte[] baSignature = ecdsa.sign();
			Log.info("signature' len: " + baSignature.length);
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException
				| InvalidKeyException | SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public static void testEC(ECCTYPE type) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			if (pubKey instanceof ECPublicKey) {
				Log.info("ECPublicKey, len: " + pubKey.getEncoded().length);
			} else {
				Log.info("Not ECPublicKey");
			}
			Log.info("getAlgorithm: " + pubKey.getAlgorithm() + " getFormat: " + pubKey.getFormat());

			Log.info(privKey.toString());
			Log.info(pubKey.toString());
			Signature ecdsa;
			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
			ECPrivateKey ecPrivateKey = (ECPrivateKey) privKey;
			ecdsa.initSign(Util.getPrivateKey(ecPrivateKey.getS().toByteArray(), LockType.T1));// privKey);
			String text = "In teaching others we teach ourselves";
			System.out.println("Text: " + text);
			byte[] baText = text.getBytes();
//			ecdsa.update(Util.EQCCHA_MULTIPLE(Util.getDefaultTargetBytes(), 1, true));
			ecdsa.update(baText);
			byte[] baSignature = ecdsa.sign();
			System.out.println("Signature: 0x" + (new BigInteger(1, baSignature).toString(16)).toUpperCase()
					+ "\n Len: " + baSignature.length);
			Signature signature;
			signature = Signature.getInstance("NONEwithECDSA", "SunEC");
//			pubKey = new sun.security.ec.ECPublicKeyImpl(baSignature);
			EQCECCPublicKey eqPublicKey = new EQCECCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			byte[] compressedPubkey = eqPublicKey.getCompressedPublicKeyEncoded();
			Log.info("compressedPubkey: " + Util.dumpBytes(compressedPubkey, 10) + " len: " + compressedPubkey.length);// (compressedPubkey));
			eqPublicKey = new EQCECCPublicKey(type);
			eqPublicKey.setECPoint(compressedPubkey);
//			eqPublicKey.setECPoint(pubKey.getEncoded());
			Log.info(Util.dumpBytesBigEndianBinary(pubKey.getEncoded()));
			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			ECPublicKey abc = (ECPublicKey) pubKey;
			Log.info("getAlgorithm: " + abc.getAlgorithm() + " getFormat: " + abc.getFormat());
//			abc.getW().getAffineX()
			signature.initVerify(eqPublicKey);
//			signature.update(Util.EQCCHA_MULTIPLE(Util.getDefaultTargetBytes(), 1, true));
			signature.update(baText);
			boolean result = signature.verify(baSignature);
			System.out.println("Valid: " + result);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public static void test() {
//		Account acc = new Account();
//    	acc.setAddress("abc");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(new byte[64]);
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("abcd");
//    	Keystore.getInstance().(acc);
//    	Account acc1 = new Account();
//    	acc1.setAddress("abc");
//    	acc1.setBalance(1000000000);
//    	acc1.setPrivateKey(new byte[64]);
//    	acc1.setPwdHash(Util.getSecureRandomBytes());
//    	acc1.setUserName("abcd");
//    	if(acc.equals(acc1)) {
//    		Log.info("equal");
//    	}
//    	Keystore.getInstance().(acc1);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("a");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("a");
//    	Keystore.getInstance().(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("b");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("b");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
	}

	public static void testUserAccount() {
		UserProfile account;
		account = Keystore.getInstance().createUserProfile("nju2006", "abc", ECCTYPE.P521, "0");
		Log.info(account.toString());
//		Log.info(Keystore.getInstance().getUserProfiles().get(0).toString());
	}

	public static void testBytesToBIN() {
		byte[] bytes = EQCCastle.bytesToBIN(new byte[16]);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		try {
			byte[] bytes1 = EQCCastle.parseBIN(is);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testBase58() {
		byte[] key = new byte[18];
		for (int i = 0; i < 1; ++i) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(0);
			ByteArrayInputStream is = new ByteArrayInputStream(key);
			Log.info("os0:\n" + Util.dumpBytesBigEndianHex(os.toByteArray()));
			StringBuilder sb = new StringBuilder();
//    	sb.append("00");
			BigInteger pubKeyHash = null;
			try {
				pubKeyHash = new BigInteger(1, Util.getSecureRandomBytes());

				Log.info("pubKeyHash:\n" + Util.dumpBytesBigEndianHex(pubKeyHash.toByteArray()));
				os.write(pubKeyHash.toByteArray());
			} catch (IOException | NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(pubKeyHash.toString(16));
//			sb.append(CRC8ITU.update(os.toByteArray()));
			Log.info("os1:\n" + Util.dumpBytesBigEndianHex(os.toByteArray()));
			BigInteger mod = new BigInteger(1, os.toByteArray());
			if (mod.mod(BigInteger.valueOf(0x7)).compareTo(BigInteger.ZERO) == 0) {
				Log.info("crc check passed.");
			} else {
				Log.info("crc check failed.");
			}
			byte[] version = new byte[1];
			version[0] = 1;
			Log.info(Base58.encode(new byte[] { 0 }));
			String add = Base58.encode(version)
					+ Base58.encode(ByteBuffer.wrap(os.toByteArray(), 1, os.toByteArray().length - 1).array());
			Log.info(add);
//    	String str = sb.toString();
//    	Log.info(str);
//    	byte[] bytes = new BigInteger(str, 16).toByteArray();
//    	Log.info(Base58.encode(new BigInteger(str, 16).toByteArray()) + " len: " + Base58.encode(new BigInteger(str, 16).toByteArray()).length());
//    	Log.info(Base58.encode1(new BigInteger(str, 16).toByteArray()));
		}
	}

//	public static void testCRC8ITU() {
//		byte[] bytes = "123456789".getBytes();
//		short b = CRC8ITU.update(bytes);
//		Log.info(Integer.toHexString(CRC8ITU.update(bytes) & 0xff));
//	}

	public static void testRIPEMD() {
		Log.info(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16));
		Log.info(Util.dumpBytes(Util.RIPEMD128("abc".getBytes()), 16));
//    	assertNotNull(Util.dumpBytes(Util.RIPEMD160("abc".getBytes()), 16), "userAttribute");
	}

	public static void testBigIntegerToBits() {

		// 127 = ‭01111111‬
		Log.info(Util.dumpBytes(Util.longToBytes(127l), 16) + "\n" + Util.dumpBytes(EQCCastle.longToEQCBits(127l), 16)
				+ "\n" + Util.dumpBytes(EQCCastle.eqcBitsToBigInteger(EQCCastle.longToEQCBits(127l)).toByteArray(), 16));
		// 128 = ‭10000000‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(128)) + "\n" + Util.dumpBytes(Util.longToBits(128)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(128)).toByteArray()));
//    	// 255 = ‭11111111‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(255)) + "\n" + Util.dumpBytes(Util.longToBits(255)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(255)).toByteArray()));
//    	// 4095 = ‭111111111111‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(4095)) + "\n" + Util.dumpBytes(Util.longToBits(4095)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(4095)).toByteArray()));
//    	// 1234 = ‭‭010011010010‬‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(1234)) + "\n" + Util.dumpBytes(Util.longToBits(1234)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(1234)).toByteArray()));
//    	// 12345 = ‭‭0011000000111001‬‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(12345)) + "\n" + Util.dumpBytes(Util.longToBits(12345)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(12345)).toByteArray()));
//    	// ‭Integer.MAX_VALUE‬
//    	Log.info(Util.dumpBytes(Util.longToBytes(Integer.MAX_VALUE-1)) + "\n" + Util.dumpBytes(Util.longToBits(Integer.MAX_VALUE-1)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(Integer.MAX_VALUE-1)).toByteArray()));
		// 1844674407370955161 =
		// ‭0001100110011001100110011001100110011001100110011001100110011001
//    	Log.info(Util.dumpBytes(Util.longToBytes(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.longToBits(Long.MAX_VALUE)) + "\n" + Util.dumpBytes(Util.bitsToBigInteger(Util.longToBits(Long.MAX_VALUE)).toByteArray()));‬
	}

	public static void testSN() {
		ID addressSN = new ID(BigInteger.ZERO);
		Vector<ID> vec = new Vector<ID>();
		vec.add(addressSN);
		for (int i = 1; i < 1000; ++i) {
			vec.add(vec.get(i - 1).getNextID());
			if (vec.get(i).isNextID(vec.get(i - 1)))
				Log.info("isNextSN：" + " current: " + vec.get(i).longValue() + " previous:" + vec.get(i - 1).longValue()
						+ " bits: " + Util.dumpBytes(vec.get(i).getEQCBits(), 2));
		}
	}

//	public static void testBlockchain() {
//		System.out.println("testBlockchain");
//		Log.info("Default target: " + Util.bigIntegerTo128String(Util.targetBytesToBigInteger((Util.getDefaultTargetBytes()))));
//		
//		// 0.avro
//		EQCBlock eqcBlock = new EQCBlock();
//		
//		EQCHeader header = new EQCHeader();
//		header.setNonce(1);
//		header.setPreHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header.setTimestamp(System.currentTimeMillis());
////    	System.out.println(header.toString());
//		
//		TransactionsHeader transactionsHeader = new TransactionsHeader();
//		eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
//		Transaction transaction = new  Transaction();
//		Address address = new Address();
//		address.setAddress(Keystore.getInstance().getUserProfiles().get(0).getAddress());
//		address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//		TxOut txOut = new TxOut();
//		txOut.setAddress(address);
//		txOut.setValue(25*Util.ABC);
//		transaction.addTxOut(txOut);
//		eqcBlock.addTransaction(transaction);
//		
//		header.setRootHash(eqcBlock.getTransactionsHash());
//		eqcBlock.setEqcHeader(header);
//		
//		EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
//		
//		Vector<EQCBlock> vec = new Vector<EQCBlock>();
//		vec.add(eqcBlock);
//		
//		// 1.avro
//		EQCBlock eqcBlock1 = new EQCBlock();
//		
//		EQCHeader header1 = new EQCHeader();
//		header1.setNonce(1);
//		header1.setPreHash(vec.get(0).getEqcHeader().getEqcHeaderHash());
//		header1.setTarget(Util.getDefaultTargetBytes());
////		header1.setTxHash(Util.EQCCHA_MULTIPLE(Util.getSecureRandomBytes(), 1, true));
//		header1.setTimestamp(System.currentTimeMillis());
////    	System.out.println(header.toString());
//		
//		TransactionsHeader transactionsHeader1 = new TransactionsHeader();
//		transactionsHeader1.setHeight(SerialNumber.ZERO);
//		eqcBlock1.getTransactions().setTransactionsHeader(transactionsHeader1);
//		Transaction transaction1 = new  Transaction();
//		Address address1 = new Address();
//		address1.setAddress(Keystore.getInstance().getUserProfiles().get(0).getAddress());
//		address1.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//		TxOut txOut1 = new TxOut();
//		txOut1.setAddress(address1);
//		txOut1.setValue(25*Util.ABC);
//		transaction1.addTxOut(txOut1);
//		eqcBlock1.addTransaction(transaction1);
//		
//		header1.setTxHash(eqcBlock1.getTransactionsHash());
//		eqcBlock1.setEqcHeader(header1);
//		
////		EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock1);
//		vec.add(eqcBlock1);
//		
//		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
//		long time0 = System.currentTimeMillis();
//		long time1;
//		int lCount = 1;
//		long Totaltime = 0;
//		long i = 0;
//		byte[] bytes;
//		while (true) {
//			BigInteger hash = new BigInteger(1, Util
//					.EQCCHA_MULTIPLE((bytes = Util.updateNonce(vec.get(lCount).getEqcHeader().getBytes(), ++i)), Util.MILLIAN, true));// Util.dualSHA3_512(Util.multipleExtend((bytes=Util.updateNonce(vec.get(lCount).getBytes(),
//																														// ++i)),
//																														// 100)));
////        	System.out.println("hash: " + Util.bigIntegerTo128String(hash));
//			if (hash.compareTo(target) <= 0) {
////        		time1 = System.currentTimeMillis();
//				Log.info("EQC Block No." + lCount + " Find use: "
//						+ (System.currentTimeMillis() - vec.get(lCount).getEqcHeader().getTimestamp()) + " ms, details:");
//				// Update the relevant EQCHeader to the right one
//				//vec.set(lCount, new EQCHeader(bytes));
//				vec.get(lCount).setEqcHeader(new EQCHeader(bytes));
//				Log.info(vec.get(lCount).getEqcHeader().toString());
//				
//				EQCBlockChainH2.getInstance().saveEQCBlock(vec.get(lCount));
////				if(lCount == 0) {
////					EQCBlock eqcBlock = new EQCBlock();
////					eqcBlock.setEqcHeader(vec.get(lCount));
////					TransactionsHeader transactionsHeader = new TransactionsHeader();
////					transactionsHeader.setHeight(BigInteger.ZERO);
////					eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
////					Transaction transaction = new  Transaction();
////					Address address = new Address();
////					address.setAddress(Keystore.getInstance().getUserProfiles().get(0).getAddress());
////					address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
////					TxOut txOut = new TxOut();
////					txOut.setAddress(address);
////					txOut.setValue(25*Util.ABC);
////					transaction.addTxOut(txOut);
////					eqcBlock.addTransaction(transaction);
////					EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
////				}
////				else {
//					
//					
////				}
//				
//				header = new EQCHeader();
//				header.setNonce(0);
//				header.setPreHash(vec.get(lCount).getEqcHeader().getEqcHeaderHash());
//				
//				eqcBlock = new EQCBlock();
//				
//				transactionsHeader = new TransactionsHeader();
//				transactionsHeader.setHeight(new SerialNumber(vec.get(lCount).getTransactions().getTransactionsHeader().getHeight().getSerialNumber().add(BigInteger.ONE)));
//				eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);
//				transaction = new  Transaction();
//				address = new Address();
//				address.setAddress(Keystore.getInstance().getUserProfiles().get(0).getAddress());
//				address.setSerialNumber(new SerialNumber(BigInteger.ZERO));
//				txOut = new TxOut();
//				txOut.setAddress(address);
//				txOut.setValue(25*Util.ABC);
//				transaction.addTxOut(txOut);
//				eqcBlock.addTransaction(transaction);
//				
//				++lCount;
//				if (lCount % 10 != 0) {
//					header.setTarget(vec.get(lCount - 1).getEqcHeader().getTarget());
//				} else {
//					Log.info("Old target: "
//							+ Util.bigIntegerTo128String(Util.targetBytesToBigInteger(vec.get(lCount - 1).getEqcHeader().getTarget()))
//							+ "\r\naverge time: "
//							+ (vec.get(lCount - 1).getEqcHeader().getTimestamp() - vec.get(lCount - 10).getEqcHeader().getTimestamp()) / 10);
//					target = target
//							.multiply(BigInteger.valueOf(
//									(vec.get(lCount - 1).getEqcHeader().getTimestamp() - vec.get(lCount - 10).getEqcHeader().getTimestamp())))
//							.divide(BigInteger.valueOf(90000));
//					if(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(target) >= 0)
//					{
//						Log.info("New target: " + Util.bigIntegerTo128String(target));
//						header.setTarget(Util.bigIntegerToTargetBytes(target));
//					}
//					else {
//						Log.info("New target: " + Util.bigIntegerTo128String(target) + " but due to it's less than the default target so still use default target.");
//						header.setTarget(Util.getDefaultTargetBytes());
//					}
//				}
//				header.setTxHash(eqcBlock.getTransactionsHash());
//				header.setTimestamp(System.currentTimeMillis());
//				eqcBlock.setEqcHeader(header);
//				vec.add(eqcBlock);
//				i = 0;
//				if (lCount == 2000) {
//					break;
//				}
////        		System.out.println(hash.toString(2));
////        		System.out.println(" len: " + hash.toString(2).length() + " i: " +i);
////        		System.out.println(" len: " + hash.toString(2).length() + " i: " + (i-1) + "\n" + Base64.getEncoder().encodeToString(Util.dualSHA3_512(Util.multipleExtend((""+(i-1)).getBytes(), 1))));
////        		if(lCount%10 == 0) {
////        			System.out.println("Old target: " + target.toString(16) + "averge time: " + Totaltime/10);
////        			target = target.multiply(BigInteger.valueOf(Totaltime)).divide(BigInteger.valueOf(100000));
//////        			Totaltime = 0;
////        			System.out.println("New target: " + target.toString(16));
////        		}
//			}
//		}
//		Log.info("averge time: " + (vec.get(vec.length() - 1).getEqcHeader().getTimestamp() - vec.get(0).getEqcHeader().getTimestamp()) / lCount
//				+ " total time: " + (vec.get(vec.length() - 1).getEqcHeader().getTimestamp() - vec.get(0).getEqcHeader().getTimestamp()) + " count:"
//				+ lCount);
//	}

	public static void testLongToBytes() {
		byte[] foo = Util.longToBytes(Long.MAX_VALUE);
		long lValue = Util.bytesToLong(foo);
		System.out.println("lValue: " + lValue);
	}

	public static void testTarget() {

//		BigInteger a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16))
//				.multiply(BigInteger.valueOf(2).pow(512 - 60 - 17));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).multiply(BigInteger.valueOf(2).pow(3))
//				.add(BigInteger.valueOf(Long.parseLong("21", 16)));
//		System.out.println(a.toString(16));
//		a = BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
//				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(424);// .multiply(BigInteger.valueOf(2).pow(3)).add(BigInteger.valueOf(Long.parseLong("21",
//																					// 16)));
//		System.out.println(a.shiftRight(512 - a.bitLength()).toString(16) + " len: " + a.bitLength());

		Log.info(Util.dumpBytes(Util.getDefaultTargetBytes(), 16));
		Log.info(Util.bigIntegerTo512HexString(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes())));

//		Util.bigIntegerToTargetBytes(Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()));

		Util.bigIntegerToTargetBytes(
				Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).divide(BigInteger.valueOf(10)));

	}

	public static void testSignBigIntegerPadingZero() {

		BigInteger negative_number = BigInteger.valueOf(Long.MAX_VALUE + 1);
		byte[] negative_bytes = negative_number.toByteArray();
		System.out.println(
				"negative_number:" + negative_number.toString() + " negative_bytes' len: " + negative_bytes.length);

		BigInteger positive_number = new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray());
		byte[] positive_bytes = positive_number.toByteArray();
		System.out.println(
				"positive_number:" + positive_number.toString() + " positive_bytes' len: " + positive_bytes.length);

		BigInteger number = BigInteger.valueOf(Long.MAX_VALUE + 1);// .add(BigInteger.ONE);
		byte[] number_bytes = number.toByteArray();
		System.out.println("number:" + number.toString() + " number_bytes' len: " + number_bytes.length);

		BigInteger number1 = BigInteger.valueOf(128).shiftLeft(80);
		byte[] number_bytes1 = number1.toByteArray();
		System.out.println("number1:" + number1.toString(16) + " number_bytes1' len: " + number_bytes1.length);

		BigInteger number2 = new BigInteger(1, number_bytes1);
		byte[] number_bytes2 = number2.toByteArray();
		System.out.println("number2:" + number2.toString(16) + " number_bytes2' len: " + number_bytes2.length);

		BigInteger number3 = new BigInteger(number_bytes1);
		byte[] number_bytes3 = number3.toByteArray();
		System.out.println("number3:" + number3.toString(16) + " number_bytes3' len: " + number_bytes3.length);

	}

	public static void testTargetToBytes() {

		// Display default target
		BigInteger target = Util.targetBytesToBigInteger(Util.getDefaultTargetBytes());
		System.out.println("Default target's length: " + Util.getDefaultTargetBytes().length);
//		// Display 512 bit length target
//		byte[] tmp = Util.bigIntegerTo64Bytes(target);
//		System.out.print("128 bit bytes' len: " + tmp.length + "\n");

//		System.out.println(Util.bytesToBigInteger(tmp).toString(16));
//		System.out.println(Util.bigIntegerTo128String(Util.getDefaultTargetBytes()));

	}

	public static void testEQCBlock() throws Exception {
		for (int i = 0; i < 2; ++i) {
			EQCHive eqcBlock;
			try {
				eqcBlock = new EQCHive(GlobalStateH2.getInstance().getEQCHive(new ID(BigInteger.valueOf(i)))); 
				Log.info(eqcBlock.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		eqcBlock = EQCBlockChainH2.getInstance().getEQCBlock(new SerialNumber(BigInteger.valueOf(1)), true);
//		Log.info(eqcBlock.toString());
	}

	public static void testToString() {
//		TxIn txIn = new TxIn();
//		txIn.setPassportId(ID.ZERO);
//		txIn.setValue(Util.getValue(25));
		Lock key = null;
		try {
//			key = LockTool.readableLockToEQCLock(Keystore.getInstance().getUserProfiles().get(0).getReadableLock());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		Log.info(txIn.toString());
		Log.info(key.toString());
		// Create Transaction
		TransferTransaction transaction = new TransferTransaction();
		TransferTxOut txOut = new TransferTxOut();
		txOut.setPassportId(ID.ONE);
		txOut.setValue(Util.getValue(25));
		transaction.addTxOut(txOut);
		Log.info(transaction.toString());
		try {
			EQCHive transactions = new EQCHive();
			transactions.getEQCoinSeeds().addTransaction(transaction);
			transactions.getEQCoinSeeds().addTransaction(transaction);
			transactions.getEQCoinSeeds().addTransaction(transaction);
			Log.info(transactions.toString());
			Util.recoverySingularityStatus();
//			eqcBlock.setTransactions(transactions);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testVerifyPublicKey(ECCTYPE type) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if (type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			} else if (type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp);
			KeyPair kp = kpg.genKeyPair();
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			ECPublicKey ecPublicKey = (ECPublicKey) pubKey;
			Log.info(Util.dumpBytes(ecPublicKey.getEncoded(), 16));
			EQCECCPublicKey eqPublicKey = new EQCECCPublicKey(type);
			// Create EQPublicKey according to java pubkey
			eqPublicKey.setECPoint((ECPublicKey) pubKey);
			eqPublicKey.setECPoint(eqPublicKey.getCompressedPublicKeyEncoded());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testValue() {
		String key = "1";
		if (Util.isTXValueValid(key)) {
			Log.info(key + " Passed.");
		} else {
			Log.info(key + " Failed.");
		}
		key = "1.1";
		if (Util.isTXValueValid(key)) {
			Log.info(key + " Passed.");
		} else {
			Log.info(key + " Failed.");
		}
		key = "MwG";
		if (Util.isTXValueValid(key)) {
			Log.info(key + " Passed.");
		} else {
			Log.info(key + " Failed.");
		}
		key = "1.1110";
		if (Util.isTXValueValid(key)) {
			Log.info(key + " Passed.");
		} else {
			Log.info(key + " Failed.");
		}
	}

	public static void testAddressFormat() {
		String key = "1w6WJRsMFEcGVEqXMwGmLHWW";
		try {
			if (LockTool.isReadableLockSanity(key)) {
				Log.info(key + " Passed.");
			} else {
				Log.info(key + " Failed.");
			}
			key = "4w6WJRsMFEcGVEqXMwGmLHWW";
			if (LockTool.isReadableLockSanity(key)) {
				Log.info(key + " Passed.");
			} else {
				Log.info(key + " Failed.");
			}
			key = "1w6WJRsMFEcGVEqXMwG";
			if (LockTool.isReadableLockSanity(key)) {
				Log.info(key + " Passed.");
			} else {
				Log.info(key + " Failed.");
			}
			key = "1w6WJRsMFEcGVEq0XMwGmLHW";
			if (LockTool.isReadableLockSanity(key)) {
				Log.info(key + " Passed.");
			} else {
				Log.info(key + " Failed.");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testP2SH() {
//		P2SHAddress pAddress = new P2SHAddress();
//		Peer peer1 = new Peer(), peer2 = new Peer(), peer3 = new Peer();
//		peer1.setPeerSN(1);
//		peer1.setTimestamp(Time.UTC(2019, 2, 6, 10, 0, 0));
//		peer1.addAddress(Keystore.getInstance().getUserProfiles().get(1).getAddress(), 
//				Keystore.getInstance().getUserProfiles().get(2).getAddress(),
//				Keystore.getInstance().getUserProfiles().get(3).getAddress());
//		
//		peer2.setPeerSN(2);
//		peer2.setTimestamp(Time.UTC(2029, 2, 2, 24, 0, 0));
//		peer2.addAddress(Keystore.getInstance().getUserProfiles().get(4).getAddress(), 
//				Keystore.getInstance().getUserProfiles().get(5).getAddress(),
//				Keystore.getInstance().getUserProfiles().get(6).getAddress());
//		
//		peer3.setPeerSN(3);
//		peer3.setTimestamp(Time.UTC(2029, 2, 2, 24, 0, 0));
//		peer3.addAddress(Keystore.getInstance().getUserProfiles().get(7).getAddress(), 
//				Keystore.getInstance().getUserProfiles().get(8).getAddress(),
//				Keystore.getInstance().getUserProfiles().get(9).getAddress());
//		
//		pAddress.addPeer(peer1, peer2, peer3);
//		pAddress.generate();
//		Log.info(pAddress.getAddress());
//		Log.info("Code len: " + pAddress.getCode().length);
//		
//		Address address = new Address();
//		address.setAddress(pAddress.getAddress());
//		address.setSerialNumber(SerialNumber.ZERO);
//		address.setCode(pAddress.getCode());
//		
//		TransferTransaction transaction = new TransferTransaction();
//		TxIn txIn = new TxIn();
//		txIn.setAddress(address);
//		transaction.setTxIn(txIn);
//		transaction.setNonce(BigInteger.valueOf(1l));
//		TxOut txOut = new TxOut();
//		address = new Address();
//		address.setAddress(Keystore.getInstance().getUserProfiles().get(1).getAddress());
//		txOut.setAddress(address);
//		txOut.setValue(50 * Util.ABC);
//		transaction.addTxOut(txOut);
//		
//		int sn = 1;
//		PeerPublickeys peerPublickeys2 = new PeerPublickeys();
//		peerPublickeys2.setPublickeySN(sn);
//		peerPublickeys2.addPublickey(Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+1).getPublicKey(), "abc"),
//				Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+2).getPublicKey(), "abc"),
//				Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+3).getPublicKey(), "abc"));
//		com.eqzip.eqcoin.blockchain.PublicKey publicKey = new com.eqzip.eqcoin.blockchain.PublicKey();
//		publicKey.setPublicKey(peerPublickeys2.getBytes());
//		transaction.setPublickey(publicKey);
//		
//		transaction.setTxFeeLimit(TXFEE_RATE.POSTPONE0);
//		
//		PeerSignatures peerSignatures2 = new PeerSignatures();
//		peerSignatures2.setSignatureSN(sn);
//		peerSignatures2.addSignature(Util.signTransaction(Keystore.getInstance().getUserProfiles().get((sn-1)*3+1).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+1).getPrivateKey(), "abc"), transaction, new byte[16], sn),
//				Util.signTransaction(Keystore.getInstance().getUserProfiles().get((sn-1)*3+2).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+2).getPrivateKey(), "abc"), transaction, new byte[16], sn),
//				Util.signTransaction(Keystore.getInstance().getUserProfiles().get((sn-1)*3+3).getAddressType(), Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get((sn-1)*3+3).getPrivateKey(), "abc"), transaction, new byte[16], sn)
//				);
//		transaction.setSignature(peerSignatures2.getBytes());
//		
//		if(transaction.verifyPublickey()) {
//			Log.info("verifyPublickey passed");
//		}
//		else {
//			Log.info("verifyPublickey failed");
//		}
//		
//		if(transaction.verifySignature()) {
//			Log.info("verifySignature passed");
//		}
//		else {
//			Log.info("verifySignature failed");
//		}
//		
//		if(transaction.verify()) {
//			Log.info("verify passed");
//		}
//		else {
//			Log.info("verify failed");
//		}
//		
	}

	public static void testMinAndMaxAddress() {
		byte[] bytes = new byte[32];
		bytes[0] = 1;
		bytes[31] = 1;
		Log.info("32 bytes 0: " + Base58.encode(bytes));
		try {
			Log.info(LockTool.generateReadableLock(LockType.T1, bytes) + " len: "
					+ LockTool.generateReadableLock(LockType.T1, bytes).length());
			for (int i = 0; i < bytes.length; ++i) {
				bytes[i] = (byte) 0xff;
			}
			Log.info("32 bytes ff: " + Base58.encode(bytes));
			Log.info(LockTool.generateReadableLock(LockType.T1, bytes) + " len: "
					+ LockTool.generateReadableLock(LockType.T1, bytes).length());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testDisplayUserProfiles() {
		Vector<UserProfile> userAccounts = Keystore.getInstance().getUserProfileList();
		for (UserProfile userAccount : userAccounts) {
			Log.info(userAccount.toString());
		}
	}

	public static void testInterface() {
		try {
			GlobalState eqcBlockChain = GlobalStateH2.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testRocksDB() {
//		try {
//			System.gc();
//			byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE);
//			long begin = System.currentTimeMillis();
//			Log.info("" + begin);
//			for (int i = 0; i < 10000000; ++i) {
//				EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT, new ID(BigInteger.valueOf(i)).getEQCBits(), bytes);
//			}
//			long end = System.currentTimeMillis();
//			Log.info("Total put time: " + (end - begin) + " ms");
//			begin = System.currentTimeMillis();
//			Log.info("" + Util.dumpBytes(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, ID.ZERO.getEQCBits()), 16));
//			Log.info("" + begin);
//			for (int i = 0; i < 10000000; ++i) {
//				EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT, new ID(BigInteger.valueOf(i)).getEQCBits());
//			}
//			end = System.currentTimeMillis();
//			Log.info("Total get time: " + (end - begin) + " ms");
//			EQCBlockChainRocksDB.getInstance().getInstance().close();
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testRocksDB1() {
//		System.gc();
//		final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(false);
//		org.rocksdb.Options options = new Options().setCompressionType(CompressionType.NO_COMPRESSION)
//				.setCreateIfMissing(true);
//		ColumnFamilyHandle columnFamilyHandle;
//		try {
//			RocksDB.destroyDB(Util.ROCKSDB_PATH, new Options());
//			RocksDB rocksDB = RocksDB.open(options, Util.ROCKSDB_PATH);
//			final ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions()
//					.setCompressionType(CompressionType.NO_COMPRESSION);
//			final List<ColumnFamilyDescriptor> columnFamilyDescriptors = Arrays.asList(
//					new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, columnFamilyOptions),
//					new ColumnFamilyDescriptor(TABLE.EQCBLOCK.name().getBytes(), columnFamilyOptions),
//					new ColumnFamilyDescriptor(TABLE.ACCOUNT.name().getBytes(), columnFamilyOptions),
//					new ColumnFamilyDescriptor(TABLE.ACCOUNT_MINERING.name().getBytes(), columnFamilyOptions),
//					new ColumnFamilyDescriptor(EQCBlockChainRocksDB.getInstance().MISC_TABLE, columnFamilyOptions));
//
//			columnFamilyHandle = rocksDB.createColumnFamily(columnFamilyDescriptors.get(1));
//			rocksDB.setOptions(columnFamilyHandle,
//					MutableColumnFamilyOptions.builder().setCompressionType(CompressionType.NO_COMPRESSION).build());
//
//			byte[] bytes = Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE);
//			long begin = System.currentTimeMillis();
//			Log.info("" + begin);
//			for (int i = 0; i < 10000000; ++i) {
//				rocksDB.put(columnFamilyHandle, new ID(BigInteger.valueOf(i)).getEQCBits(), bytes);
//
//			}
//			long end = System.currentTimeMillis();
//			Log.info("Total put time: " + (end - begin) + " ms");
//			begin = System.currentTimeMillis();
////				Log.info("" + Util.dumpBytes(rocksDB.get(columnFamilyHandles.get(1), SerialNumber.ZERO.getEQCBits()), 16));
//			Log.info("" + begin);
//			for (int i = 0; i < 10000000; ++i) {
//				rocksDB.get(columnFamilyHandle, new ID(BigInteger.valueOf(i)).getEQCBits());
//			}
//			end = System.currentTimeMillis();
//			rocksDB.close();
//			Log.info("Total get time: " + (end - begin) + " ms");
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void destoryRocksDB() {
//		try {
//			RocksDB.destroyDB(Util.ROCKSDB_PATH, new Options());
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testTimestamp() {
		Log.info("Current timestamp's length: "
				+ new ID(BigInteger.valueOf(System.currentTimeMillis())).getEQCBits().length);
	}

	public static void testNonce() {
		Log.info("268435455 len: " + new ID(268435455).getEQCBits().length);
	}

	public static void testRocksDBAccount() {
//		Account account = new AssetAccount();
//		Passport passport = new Passport();
//		passport.setReadableAddress(Keystore.getInstance().getUserProfiles().get(0).getReadableAddress());
//		passport.setID(ID.TWO);
//		account.setPassport(passport);
//		account.setLockCreateHeight(ID.ZERO);
//		Asset asset = new CoinAsset();
//		asset.deposit(new ID(500000));
//		account.setAsset(asset);
//		try {
//			EQCBlockChainRocksDB.getInstance().getInstance().saveAccount(account);
//			account = EQCBlockChainRocksDB.getInstance().getInstance().getAccount(ID.TWO);
//			Log.info(account.getPassport().toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		RocksIterator rocksIterator = EQCBlockChainRocksDB.getInstance().getInstance().getRocksDB().newIterator(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT));
//		rocksIterator.seekToFirst();
//		while(rocksIterator.isValid()) {
//			account = null;
//			try {
//				account = new Account(rocksIterator.key());
//			} catch (NoSuchFieldException | IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Log.info(account.getAddress().toString());
//			rocksIterator.next();
//		}
	}

	public static void testDisplayAccount() {
//		Account account;
//		try {
//			account = EQCBlockChainRocksDB.getInstance().getInstance().getAccount(ID.TWO);
//			Log.info(account.getPassport().toString());
//			account = EQCBlockChainRocksDB.getInstance().getInstance().getAccount(new ID(3));
//			Log.info(account.getPassport().toString());
//		} catch (NoSuchFieldException | IllegalStateException | RocksDBException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testDisplayEQCBlock(ID height) {
//		try {
//			Log.info(EQCBlockChainRocksDB.getInstance().getInstance().getEQCHive(height, false).getRoot().toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testDisplayAllAccount() throws Exception {
//		BigInteger serialNumber;
//		try {
//			serialNumber = EQCBlockChainRocksDB.getInstance().getInstance()
//					.getTotalAccountNumbers(EQCBlockChainRocksDB.getInstance().getInstance().getEQCBlockTailHeight());
//			for (int i = Util.INIT_ADDRESS_SERIAL_NUMBER; i < serialNumber.longValue()
//					+ Util.INIT_ADDRESS_SERIAL_NUMBER; ++i) {
//				Log.info(EQCBlockChainRocksDB.getInstance().getInstance().getAccount(new ID(i)).toString());
//			}
//		} catch (NoSuchFieldException | IllegalStateException | RocksDBException | IOException | ClassNotFoundException | SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testMisc() {
//		TransferTransaction transaction = new TransferTransaction();
//		transaction.getBytes();
//		transaction.getMaxTxFeeLimit();
//		Log.info("" + TransactionType.COINBASE);
//		Log.info("" + TransactionType.COINBASE.ordinal());
	}

	public static void testBigintegerLeadingzero() {
		Log.info(BigInteger.valueOf(-Long.MAX_VALUE).toString(10));
		Log.info("Len: " + BigInteger.valueOf(-Long.MAX_VALUE).toByteArray().length);
		Log.info("Len: " + BigInteger.valueOf(Long.MAX_VALUE).toByteArray().length);
		Log.info("Len: " + BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray().length);
		Log.info(
				"Len: " + new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray()).toByteArray().length);
		Log.info(BigInteger.valueOf(Long.MAX_VALUE).toString(10));
		Log.info(Util.dumpBytes(BigInteger.valueOf(Long.MAX_VALUE).toByteArray(), 16));
		Log.info(BigInteger.valueOf(Long.MAX_VALUE + 1).toString(10));
		Log.info(Util.dumpBytes(BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray(), 16));
		byte[] bytes = BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray();
		Log.info(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray()).toString(10));
		Log.info(Util.dumpBytes(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 1).toByteArray()).toByteArray(),
				16));
		bytes = new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 123).toByteArray()).toByteArray();
		Log.info(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 2).toByteArray()).toString(10));
		Log.info(Util.dumpBytes(new BigInteger(1, BigInteger.valueOf(Long.MAX_VALUE + 2).toByteArray()).toByteArray(),
				16));
		Log.info(new BigInteger("-9223372036854775808", 10).toString(10));
		Log.info(Util.dumpBytes(new BigInteger("9223372036854775808", 10).toByteArray(), 16));
	}

	public static void testSingularBlockBytes() {
//		Configuration.getInstance().updateIsInitSingularityBlock(false);
//		EQCHive eqcBlock;
//		try {
//			eqcBlock = Util.recoverySingularityStatus();
//			Log.info(eqcBlock.toString());
//			EQCBlockChainH2.getInstance().saveEQCHive(eqcBlock);
//			EQCBlockChainRocksDB.getInstance().getInstance().saveEQCHive(eqcBlock);
//			eqcBlock = EQCBlockChainRocksDB.getInstance().getInstance().getEQCHive(eqcBlock.getHeight(), false);
//			Log.info(eqcBlock.toString());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testVerifyAddress() {
		try {
			byte[] privateKey = Util.AESDecrypt(Keystore.getInstance().getUserProfileList().get(0).getPrivateKey(), "abc");
			byte[] publickey = Util.AESDecrypt(Keystore.getInstance().getUserProfileList().get(0).getPublicKey(), "abc");
			String key;
			key = LockTool.generateReadableLock(LockType.T2, publickey);
//			Log.info(Keystore.getInstance().getUserProfiles().get(0).getReadableLock());
			Log.info(key);
//			if (LockTool.verifyReadableLockAndPublickey(Keystore.getInstance().getUserProfiles().get(0).getReadableLock(),
//					publickey)) {
//				Log.info("Publickey verify passed");
//			} else {
//				Log.info("Publickey verify failed");
//			}
//			if (LockTool.verifyReadableLockCRC32C(Keystore.getInstance().getUserProfiles().get(0).getReadableLock())) {
//				Log.info("crc passed");
//			} else {
//				Log.info("crc failed");
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testBase582() {
		byte[] publickey = null;
		try {
			publickey = Util.AESDecrypt(Keystore.getInstance().getUserProfileList().get(0).getPublicKey(), "abc");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.info(Base58.encode(publickey));
		try {
			if (Arrays.equals(publickey, Base58.decode(Base58.encode(publickey)))) {
				Log.info("passed");
			} else {
				Log.info("failed");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void testCF() {
//		String data;
//		ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor("abc".getBytes());
//		try {
//
//			ColumnFamilyHandle columnFamilyHandle = EQCBlockChainRocksDB.getInstance().getRocksDB()
//					.createColumnFamily(columnFamilyDescriptor);
////			data = "a";
////			EQCBlockChainRocksDB.getInstance().getRocksDB().put(columnFamilyHandle, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.getInstance().getRocksDB().put(columnFamilyHandle, data.getBytes(), data.getBytes());
//
////			Log.info("" + new String(EQCBlockChainRocksDB.getInstance().getRocksDB().get(columnFamilyHandle, data.getBytes())));
////			Log.info("" + new String(EQCBlockChainRocksDB.getInstance().getRocksDB().get(data.getBytes())));
//			EQCBlockChainRocksDB.getInstance().getRocksDB().dropColumnFamily(columnFamilyHandle);
//			Log.info("" + new String(EQCBlockChainRocksDB.getInstance().getRocksDB().get(columnFamilyHandle, "b".getBytes())));
////			Log.info("" + new String(EQCBlockChainRocksDB.getInstance().getRocksDB().get(data.getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testCF2() {
//		ColumnFamilyDescriptor columnFamilyDescriptor = new ColumnFamilyDescriptor("abc".getBytes());
//		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			Log.info("ab");
//			EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getInstance().getInstance().close();
//			EQCBlockChainRocksDB.getInstance().dropTable(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING).close();
//			Thread.sleep(1000);
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info("abc");
//			EQCBlockChainRocksDB.getInstance().getInstance().close();

//			EQCBlockChainRocksDB.getInstance().getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, data.getBytes())));
////			EQCBlockChainRocksDB.getInstance().dropTable(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			EQCBlockChainRocksDB.getInstance().getRocksDB().delete(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING), data.getBytes());
//			
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, data.getBytes())));

//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testCF3() {
		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, "b".getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testCF4() {
		String data;
//		try {
//			data = "a";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			data = "b";
//			EQCBlockChainRocksDB.getInstance().put(TABLE.ACCOUNT_MINERING, data.getBytes(), data.getBytes());
//			EQCBlockChainRocksDB.getInstance().dropTable(EQCBlockChainRocksDB.getInstance().getInstance().getTableHandle(TABLE.ACCOUNT_MINERING));
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, "a".getBytes())));
//			Log.info(new String(EQCBlockChainRocksDB.getInstance().get(TABLE.ACCOUNT_MINERING, "b".getBytes())));
//		} catch (RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testTakeSnapshot() throws Exception {
//		Passport account = new AssetPassport();
//		Lock key = new Lock();
//		key.setID(ID.ZERO);
//		key.setReadableLock(Keystore.getInstance().getUserProfiles().get(0).getReadableLock());
//		account.setKey(key);
//		account.setLockCreateHeight(ID.ZERO);
//		Asset asset = new CoinAsset();
//		asset.deposit(new ID(150000));
//		account.setAsset(asset);
//		try {
//			EQCBlockChainH2.getInstance().savePassportSnapshot(account, ID.ZERO);
//			key.setID(ID.ZERO);
//			key.setReadableLock(Keystore.getInstance().getUserProfiles().get(0).getReadableLock());
//			account.setKey(key);
//			account.setLockCreateHeight(ID.ZERO);
//			asset = new CoinAsset();
//			asset.deposit(new ID(150001));
//			account.setAsset(asset);
//			EQCBlockChainH2.getInstance().savePassportSnapshot(account, ID.ONE);
//
//			key.setID(ID.ZERO);
//			key.setReadableLock(Keystore.getInstance().getUserProfiles().get(0).getReadableLock());
//			account.setKey(key);
//			account.setLockCreateHeight(ID.ZERO);
//			asset = new CoinAsset();
//			asset.deposit(new ID(150002));
//			account.setAsset(asset);
//			EQCBlockChainH2.getInstance().savePassportSnapshot(account, ID.TWO);
//
//			Log.info(EQCBlockChainH2.getInstance().getPassportSnapshot(ID.ZERO, ID.ONE).toString());
//		} catch (ClassNotFoundException | SQLException | NoSuchFieldException | IllegalStateException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testTarget1() {
		if (Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(new BigInteger(
				"200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5",
				16)) > 0) {
			Log.info("Passed");
		}
	}

	public static void testTransaction() {
//		UserAccount userAccount = Keystore.getInstance().getUserProfiles().get(1);
//		UserAccount userAccount1 = Keystore.getInstance().getUserProfiles().get(3);
//		TransferTransaction transaction = new TransferTransaction();
//		TxIn txIn = new TxIn();
//		txIn.setKey(new Lock(userAccount.getReadableLock()));
//		transaction.setTxIn(txIn);
//		TxOut txOut = new TxOut();
//		txOut.setKey(new Lock(userAccount1.getReadableLock()));
//		txOut.setValue(500 * Util.ABC);
//		transaction.addTxOut(txOut);
//		try {
//			transaction.setNonce(Util.DB().getPassport(txIn.getKey().getAddressAI(), Mode.GLOBAL)
//					.getAsset(Asset.EQCOIN).getNonce().getNextID());
//			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
//			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
//			com.eqcoin.blockchain.transaction.CompressedPublickey publicKey2 = new com.eqcoin.blockchain.transaction.CompressedPublickey();
//			publicKey2.setCompressedPublickey(publickey);
//			transaction.setCompressedPublickey(publicKey2);
//			transaction.cypherTxInValue(TXFEE_RATE.POSTPONE0);
//			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
//			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
//			Log.info("getQosRate: " + transaction.getQosRate());
//			Log.info("getQos: " + transaction.getQos());
//
//			Signature ecdsa = null;
//			try {
//				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
//				ecdsa.initSign(Util.getPrivateKey(privateKey, transaction.getTxIn().getKey().getAddressType()));
//			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			PassportsMerkleTree changeLog = new PassportsMerkleTree(
//					Util.DB().getEQCBlockTailHeight(),
//					new Filter(Mode.MINING));
//			publicKey2.setID(changeLog.getPassport(transaction.getTxIn().getKey(), true).getId());
//			transaction.getTxIn().getKey()
//					.setID(changeLog.getPassport(transaction.getTxIn().getKey(), true).getId());
//			transaction.sign(ecdsa);
//		
//			if (transaction.verify(changeLog)) {
//				Log.info("passed");
////				Transaction transaction2 = Transaction.parseRPC(transaction.getRPCBytes());
////				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
//				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
//			} else {
//				Log.info("failed");
//			}
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	public static void testTransaction1() {
//		UserAccount userAccount = Keystore.getInstance().getUserProfiles().get(1);
//		UserAccount userAccount1 = Keystore.getInstance().getUserProfiles().get(2);
//		TxIn txIn = new TxIn();
//		txIn.setKey(new Lock(userAccount.getReadableLock()));
//
//		byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
//		byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
//		Signature ecdsa = null;
//		try {
//			ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
//			ecdsa.initSign(Util.getPrivateKey(privateKey, txIn.getKey().getAddressType()));
//		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		OperationTransaction operationTransaction = new OperationTransaction();
//		com.eqcoin.blockchain.transaction.CompressedPublickey publicKey2 = new com.eqcoin.blockchain.transaction.CompressedPublickey();
//		publicKey2.setCompressedPublickey(publickey);
//		operationTransaction.setCompressedPublickey(publicKey2);
//		UpdateAddressOperation updateAddressOperation = new UpdateAddressOperation();
//		UserAccount userAccount2 = Keystore.getInstance().getUserProfiles().get(3);
//		updateAddressOperation.setAddress(new Lock(userAccount2.getReadableLock()));
//		operationTransaction.setOperation(updateAddressOperation);
//		operationTransaction.setTxIn(txIn);
//		try {
//			operationTransaction.setNonce(Util.DB()
//					.getPassport(txIn.getKey().getAddressAI(), Mode.GLOBAL).getAsset(Asset.EQCOIN).getNonce().getNextID());
//			operationTransaction.cypherTxInValue(TXFEE_RATE.POSTPONE0);
//			Log.info("getMaxBillingSize: " + operationTransaction.getMaxBillingLength());
//			Log.info("getTxFeeLimit: " + operationTransaction.getTxFeeLimit());
//			Log.info("getQosRate: " + operationTransaction.getQosRate());
//			Log.info("getQos: " + operationTransaction.getQos());
//			PassportsMerkleTree changeLog = new PassportsMerkleTree(
//					Util.DB().getEQCBlockTailHeight(),
//					new Filter(Mode.MINING));
//			operationTransaction.sign(ecdsa);
//			EQCBlockChainH2.getInstance().saveTransactionInPool(operationTransaction);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void testAccountHashTime() {
//		Passport account = new AssetPassport();
//		Lock key = new Lock();
//		key.setReadableLock(Keystore.getInstance().getUserProfiles().get(1).getReadableLock());
//		key.setID(ID.ONE);
//		account.setKey(key);
//		account.setLockCreateHeight(ID.ONE);
//		Asset asset = new CoinAsset();
//		asset.deposit(new ID(50 * Util.ABC));
//		account.setAsset(asset);
//		byte[] publickey = Util.AESDecrypt(Keystore.getInstance().getUserProfiles().get(1).getPublicKey(), "abc");
//		Publickey publicKey2 = new Publickey();
//		publicKey2.setCompressedPublickey(publickey);
//		publicKey2.setPublickeyCreateHeight(ID.ONE);
//		account.setPublickey(publicKey2);
//
//		long c0 = System.currentTimeMillis();
//		int n = 10;
//		for (int i = 0; i < n; ++i) {
//			try {
//				account.getHash();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		long c1 = System.currentTimeMillis();
//		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}

	public static void testMultiExtendLen() throws NoSuchAlgorithmException {
//		BigInteger number = new BigInteger(1, Util.getSecureRandomBytes());
////		Log.info("Len:" + number.pow(1579).toByteArray().length);
////		Log.info("Len:" + number.pow(1580).toByteArray().length);
//		for(int i=0; i<100; ++i)
//		Log.info("Len: " + new BigInteger(1, Util.getSecureRandomBytes()).pow(1000).toByteArray().length);

		BigInteger a = new BigInteger(1, Util.getSecureRandomBytes());// BigInteger.ONE.toByteArray());
		BigInteger b = a.pow(2);
		BigInteger c = null;
		for (int i = 3; i < 10000; ++i) {
//			c = a.multiply(b);
//			a = b;
//			b = c;
//			a = a.multiply(a);
			a = a.pow(i);
			Log.info("i: " + i);// + " a: " + a + " b: " + b);
		}
		Log.info("emn" + c.toByteArray().length);
	}

	public static void testMultiExtendLen1() throws NoSuchAlgorithmException {
		BigInteger aBigInteger = new BigInteger(1, Util.getSecureRandomBytes());
		for (int i = 0; i < 100; ++i) {
			aBigInteger = aBigInteger.multiply(aBigInteger);
			Log.info(Util.dumpBytes(aBigInteger.toByteArray(), 2));
		}
	}

	public static void testDisplayBase58() {
		final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
		for (int i = 0; i < ALPHABET.length(); ++i) {
			Log.info("i: " + i + " " + ALPHABET.charAt(i) + " value: " + (byte) ALPHABET.charAt(i));
		}
	}

	public static void testSb() {
		StringBuffer sb = new StringBuffer();
		sb.append("abc");
		sb.insert(0, "d");
		Log.info(sb.toString());
	}

	public static void testCRC32C() {
		for (int i = 0; i < 1000; ++i) {
			if (Util.dumpBytes(Util.CRC32C(Util.longToBytes(i)), 16).endsWith("00")) {
				Log.info(Util.dumpBytes(Util.CRC32C(Util.longToBytes(i)), 16) + " Len: "
						+ Util.CRC32C(Util.longToBytes(i)).length);
			}
			if (Util.dumpBytes(Util.CRC32C(Util.longToBytes(i)), 16).length() <= 6) {
				Log.info(Util.dumpBytes(Util.CRC32C(Util.longToBytes(i)), 16) + " Len: "
						+ Util.CRC32C(Util.longToBytes(i)).length);
			}
		}
	}

	public static void testCreateAddressTime() {
//		EQCHeader header = new EQCHeader();
//		header.setNonce(ID.ONE);
//		header.setPreHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND, true));
//		header.setTarget(Util.getDefaultTargetBytes());
//		header.setRootHash(Util.EQCCHA_MULTIPLE_FIBONACCI_MERKEL(Util.getSecureRandomBytes(), Util.ONE, true));
//		header.setTimestamp(new ID(System.currentTimeMillis()));
//		Log.info(header.toString());
		byte[] publickey = null;
		try {
			publickey = Util.AESDecrypt(Keystore.getInstance().getUserProfileList().get(1).getPublicKey(), "abc");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.info("Publickey Len: " + publickey.length);
		long c0 = System.currentTimeMillis();
		int n = 1000;
		for (int i = 0; i < n; ++i) {
//			Util.multipleExtend(Util.getSecureRandomBytes(), Util.HUNDRED_THOUSAND);
//			Util.EQCCHA_MULTIPLE(header.getBytes(), Util.HUNDRED_THOUSAND, true);
			try {
				LockTool.generateReadableLock(LockType.T2, publickey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}

	public static void testVerrifyAddressTime() {
		byte[] publickey = null;
		try {
			publickey = Util.AESDecrypt(Keystore.getInstance().getUserProfileList().get(1).getPublicKey(), "abc");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Log.info("Publickey Len: " + publickey.length);
		long c0 = System.currentTimeMillis();
		int n = 10000;
		for (int i = 0; i < n; ++i) {
			try {
//				LockTool.verifyReadableLockAndPublickey(
//						Keystore.getInstance().getUserProfiles().get(1).getReadableLock(), publickey);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long c1 = System.currentTimeMillis();
		Log.info("total time: " + (c1 - c0) + " average time:" + (double) (c1 - c0) / n);
	}

	public static void testAddressCRC32C() {
//		Log.info(Keystore.getInstance().getUserProfiles().get(2).getReadableLock());
		try {
//			if (LockTool.verifyReadableLockCRC32C(Keystore.getInstance().getUserProfiles().get(2).getReadableLock())) {
//				Log.info("Passed");
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void testBufferLen() {
		byte[] bytes = new byte[64];
		for (int i = 0; i < bytes.length; ++i) {
			bytes[i] = (byte) 0xff;
		}
		for (int i = 1; i <= bytes.length; ++i) {
			byte[] bytes1 = new byte[i];
			System.arraycopy(bytes, 0, bytes1, 0, i);
			Util.multipleExtend(bytes1, 1);
		}
	}

	public static void testVerifyBlock() {
		ID id;
		try {
			id = Util.GS().getEQCHiveTailHeight();
//			for (int i = 1; i < id.intValue(); ++i) {
//				ChangeLog changeLog = new ChangeLog(new ID(i),
//						new Filter(Mode.MINING));
//				EQCHive eqcBlock = new EQCHive(Util.GS().getEQCHive(new ID(i)));
//				try {
//					eqcBlock.isValid();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static boolean isPrime(int n) {
		if (n < 2)
			return false;
		if (n == 2)
			return true;
		if (n % 2 == 0)
			return false;
		for (int i = 3; i < n; i += 2)
			if (n % i == 0)
				return false;
		return true;
	}

	public static void printPrime() {
		int number = 0, n = 2;
		StringBuffer sb = new StringBuffer();
		while (number < Util.HUNDREDPULS) {
			if (isPrime(n)) {
				sb.append("new ID(" + n + "),");
				++number;
				Log.info("" + number);
			}
			++n;
		}
		Log.info(sb.toString());
	}

	public static void sendTransaction() {
//		UserAccount userAccount = Keystore.getInstance().getUserProfiles().get(0);
//		UserAccount userAccount1 = Keystore.getInstance().getUserProfiles().get(3);
		TransferTransaction transaction = new TransferTransaction();
//		TxIn txIn = new TxIn();
//		txIn.setLock(new EQCLock(userAccount.getReadableLock()));
//		try {
//			txIn.getLock().setId(EQCBlockChainRPC.getInstance().getPassport(txIn.getLock().getId(), Mode.GLOBAL).getId());
//		} catch (Exception e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		transaction.setTxIn(txIn);
//		TransferTxOut txOut = new TransferTxOut();
//		txOut.setLock(new EQCLock(userAccount1.getReadableLock()));
//		txOut.setValue(Util.getValue(500));
//		transaction.addTxOut(txOut);
		try {
//			transaction.setNonce(EQCBlockChainRPC.getInstance().getTransactionMaxNonce(transaction.getNest()).getNonce().getNextID());
////			Log.info("Nonce: " + transaction.getNonce());
//			byte[] privateKey = Util.AESDecrypt(userAccount.getPrivateKey(), "abc");
//			byte[] publickey = Util.AESDecrypt(userAccount.getPublicKey(), "abc");
//			com.eqcoin.blockchain.transaction.EQCPublickey publicKey2 = new com.eqcoin.blockchain.transaction.EQCPublickey();
//			publicKey2.setPublickey(publickey);
//			transaction.setPriority(TRANSACTION_PRIORITY.ASAP, null);
////			Log.info("getMaxBillingSize: " + transaction.getMaxBillingLength());
////			Log.info("getTxFeeLimit: " + transaction.getTxFeeLimit());
////			Log.info("getQosRate: " + transaction.getQosRate());
////			Log.info("getQos: " + transaction.getQos());
//
//			Signature ecdsa = null;
//			try {
//				ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
//				ecdsa.initSign(Util.getPrivateKey(privateKey, transaction.getTxIn().getLock().getLockType()));
//			} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
////			AccountsMerkleTree changeLog = new AccountsMerkleTree(
////					EQCBlockChainRocksDB.getInstance().getInstance().getEQCBlockTailHeight(),
////					new Filter(Mode.MINING));
////			publicKey2.setID(changeLog.getAddressID(transaction.getTxIn().getPassport()));
////			transaction.getTxIn().getPassport()
////					.setID(changeLog.getAddressID(transaction.getTxIn().getPassport()));
//			transaction.sign(ecdsa);
		
//			if (transaction.verify(changeLog)) {
//				Log.info("passed");
//				Transaction transaction2 = Transaction.parseRPC(transaction.getRPCBytes());
//				if(transaction2.verifySignature()) {
////					Log.info("Passed");
//				}
//				Log.info(Util.dumpBytes(transaction.getSignature(), 16));
//				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
				SPList ipList = EQCMinerNetworkClient.getSPList(Util.SINGULARITY_SP);//EQCBlockChainH2.getInstance().getMinerList();
				ipList.addSP(Util.SINGULARITY_SP);
//				IPList ipList = new IPList();
//				ipList.addIP(Util.IP);
				Info info = null;
				for(SP ip:ipList.getSPList()) {
					try {
						Log.info("Send transaction with nonce " + transaction.getNonce() + " to " + ip);
						info = EQCTransactionNetworkClient.sendTransaction(transaction, ip);
					}
					catch(Exception e){
						Log.info(e.getMessage());
					}
					if(info == null) {
						Log.info("Send Failed");
					}
					else {
						Log.info("Send succ");
					}
				}
//			} else {
//				Log.info("Transaction verify failed");
//			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public static void dumpPassport(long lastId) {
		for (long i = 0; i < lastId; ++i) {
			try {
				Log.info(Util.GS().getPassport(new ID(i)).toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void dumpLockMate(long lastId) {
		for (long i = 0; i < lastId; ++i) {
			try {
				Log.info(Util.GS().getLockMate(new ID(i)).toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
