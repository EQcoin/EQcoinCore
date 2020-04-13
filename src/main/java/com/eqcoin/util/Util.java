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
package com.eqcoin.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32C;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHiveRoot;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.SmartContractPassport.LanguageType;
import com.eqcoin.blockchain.passport.SmartContractPassport.State;
import com.eqcoin.blockchain.seed.EQcoinSeedRoot;
import com.eqcoin.blockchain.transaction.TransferCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.TransferTxOut;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.transaction.ZeroZionCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.blockchain.transaction.ZionCoinbaseTransaction;
import com.eqcoin.configuration.Configuration;
import com.eqcoin.crypto.EQCPublicKey;
import com.eqcoin.crypto.MerkleTree;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.persistence.EQCBlockChain;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.Code;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.Info;
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.test.Test;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Util {

	/*
	 * Singularity - EQC's basic unit of measure. 1 EQC = 10000 singularity
	 */
	public final static BigInteger ABC = BigInteger.valueOf(10000);
	
	public final static byte[] MAGIC_HASH = new BigInteger("200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5", 16).toByteArray();

	public final static byte[] SINGULARITY_HASH = {};

	public final static BigInteger MAX_EQC = BigInteger.valueOf(210000000000L).multiply(ABC);
	
	public final static ID MAX_EQcoin = new ID(Util.MAX_EQC);

	public final static Value MIN_EQC = new Value(BigInteger.valueOf(51).multiply(ABC));
	
//	public final static long SINGULARITY_TOTAL_SUPPLY = 16800000 * ABC;

//	public final static long MINER_TOTAL_SUPPLY = 42000000000L * ABC;
//	
//	public final static long EQCOIN_FOUNDATION_TOTAL_SUPPLY = 168000000000L * ABC;

	public final static BigInteger BASIC_BLOCK_INTERVAL = BigInteger.valueOf(600000);

	public final static BigInteger TARGET_BLOCK_INTERVAL = BigInteger.valueOf(10000);

//	public final static long MINER_COINBASE_REWARD = 1 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);
	
//	public final static long EQZIP_COINBASE_REWARD = 4 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);
	
//	public final static long EQC_FEDERATION_COINBASE_REWARD = 19 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);
	
	public final static BigInteger BASIC_COINBASE_REWARD = BigInteger.valueOf(1200).multiply(ABC);//MINER_COINBASE_REWARD + EQC_FEDERATION_COINBASE_REWARD;

	// Here exists one bug when change the block_interval the Max_coinbase_height also changed need change it to determine according to if max supply - total supply = 0
//	public final static long MAX_COINBASE_HEIGHT = MAX_EQC / BASIC_COINBASE_REWARD.longValue();

//	public final static int TXFEE_RATE = 10;
	
	public final static byte DEFAULT_TXFEE_RATE = 10;

	public final static int ZERO = 0;

	public final static int ONE = 1;

	public final static int TWO = 2;
	
	public final static int ELEVEN = 11;

	public final static int SIXTEEN = 16;

	public final static int HUNDRED = 100;
	
	public final static int HUNDREDPULS = 101;
	
	public final static int F01 = 401;
	
	public final static int THOUSANDPLUS = 1001;

	public final static int HUNDRED_THOUSAND = 100000;

	public final static int MILLIAN = 1000000;

	public final static int KILOBYTE = 1024;
	
	public final static int ONE_MB = KILOBYTE * KILOBYTE;
	
	public final static int MAX_EQCHIVE_SIZE = ONE_MB;
	
	public final static int MAX_NONCE = (int) Math.pow(2, 28);//268435455;
	
	public final static int HASH_LEN = 64;

//	public final static String WINDOWS_PATH = "C:/EQCOIN";
//
//	public final static String MAC_PATH = "C:/Program Files/EQCOIN";
//
//	public final static String LINUX_PATH = "C:/Program Files/EQCOIN";
	/*
	 * Set the default PATH value WINDOWS_PATH
	 */
	private static String CURRENT_PATH = System.getProperty("user.dir");
	
	public static String WINDOWS = "C:";
	
	public static String LINUX = "/usr";

	/**
	 * In Windows due to haven't the permission to access the Program File folder so have to save it to C but in Linux can access the CURRENT_PATH
	 */
	public static String PATH = CURRENT_PATH + File.separator + "EQcoin";// System.getProperty("user.dir") + File.separator +
																	// "EQCOIN";
//	static {
//		PATH = System.getProperty("user.dir") + "/EQCOIN";
//	}
	
	public static final String MAGIC_PATH = ".\\src\\main\\QuidditchHelixFlashForward";

	public static final String KEYSTORE_PATH = PATH + File.separator + "EQcoin.keystore";

	public static final String KEYSTORE_PATH_BAK = PATH + File.separator + "EQcoin.keystore.bak";

	public static final String LOG_PATH = PATH + File.separator + "log.txt";

	public final static String AVRO_PATH = PATH + File.separator + "AVRO";

	public final static String EQC_SUFFIX = ".EQC";
	
	public final static String DB_PATH = PATH + File.separator + "DB";
	
	public final static String HIVE_PATH = DB_PATH + File.separator + "HIVE/";
	
	public final static String H2_PATH = DB_PATH + File.separator + "H2";
	
	public final static String ROCKSDB_PATH = DB_PATH + File.separator + "ROCKSDB";
	
	public final static String H2_DATABASE_NAME = H2_PATH + File.separator + "EQcoin";

	/**  
	 * Compressed publickey and ASN.1 DER signature's length specification
	 *	ECC curve compressed publickey length(bytes)  signature length(bytes)
	 *	   P256 							33 												70、71、72
	 *	   P521 							67 			  								137、138、139
	 */
//	public final static int P256_PUBLICKEY_MIN_LEN = 32;
//	
//	public final static int P256_PUBLICKEY_MAX_LEN = 36;
//	
//	public final static int P521_PUBLICKEY_MIN_LEN = 64;
//	
//	public final static int P521_PUBLICKEY_MAX_LEN = 70;
	
	public final static int P256_PUBLICKEY_LEN = 33;
	
	public final static int P521_PUBLICKEY_LEN = 67;
	
	public final static BigInteger P256_MAX_SIGNATURE_LEN = BigInteger.valueOf(73);

	public final static BigInteger P521_MAX_SIGNATURE_LEN = BigInteger.valueOf(140);
	
	public final static BigInteger P256_POINT_LEN = BigInteger.valueOf(32);

	public final static BigInteger P521_POINT_LEN = BigInteger.valueOf(66);
	
	public final static BigInteger P256_SIGNATURE_LEN = BigInteger.valueOf(64);

	public final static BigInteger P521_SIGNATURE_LEN = BigInteger.valueOf(132);

//	public final static int P256_BASIC_PUBLICKEY_LEN = 34;
//
//	public final static int P521_BASIC_PUBLICKEY_LEN = 68;
	
	public final static int MAX_SERIAL_NUMBER_LEN = 5;
	
	public final static BigInteger MAX_TXFEE_LEN = BigInteger.valueOf(6);
	
	public final static int BASIC_VALUE_NUMBER_LEN = 8;

	public final static int INIT_ADDRESS_SERIAL_NUMBER = 0;

	public final static ID DEFAULT_PROTOCOL_VERSION = ID.ZERO;
	
	public final static ID PROTOCOL_VERSION = DEFAULT_PROTOCOL_VERSION;
	
	private static Cookie<O> cookie = null;

	private static Info<O> info = null;
	
	public static final long DEFAULT_TIMEOUT = 3000;
	
	public static final int MAX_ADDRESS_LEN = 51;
	
	public static final int MIN_ADDRESS_LEN = 41;
	
	public static final int MAX_ADDRESS_AI_LEN = 33;
	
	public static final int MAX_T3_ADDRESS_CODE_LEN = 213;
	
	public static final int CRC32C_LEN = 4;

	public static final int MAX_DIFFICULTY_MULTIPLE = 4;
	
	public static final BigInteger EUROPA = BigInteger.valueOf(1008);
	
	// Here exists one bug need change null hash to SHA3-512(EQCType.NULL_ARRAY)
	public static final byte[] NULL_HASH = Arrays.copyOfRange(new BigInteger("C333A8150751C675CDE1312860731E54818F95EDC1563839501CE5F486DE1C79EA6675EECA26833E41341B5B5D1E72800CBBB13AE6AA289D11ACB4D4413B1B2D", 16).toByteArray(), 1, 65);
	
	public static final byte[] SINGULARITY = EQCType.NULL_ARRAY;
	
	public static final String REGEX_IP = "";
	
	public static final String REGEX_VERSION = "";
	
	public static final IP SINGULARITY_IP = new IP("129.28.206.27");//"14.221.176.195";
	
	public static final IP LOCAL_IP = null;//"14.221.176.18";//"14.221.177.212";//"192.168.0.101";//"14.221.177.223";//"129.28.206.27";
	
	public static final int MINER_NETWORK_PORT = 7799;
	
	public static final int SYNCBLOCK_NETWORK_PORT = 7997;
	
	public static final int TRANSACTION_NETWORK_PORT = 9977;
	
	public static final String SINGULARITY_A = "2gVXCVhzQBGVkhDZtUHt6hBM7UEs3wopNnLLA1q5Bjbs1DKEkY";
	
	public static final String SINGULARITY_B = "2J9DMRSrUD9gZWWLEfKbwwv9GFED4szodSyFrfcpNinX8Ke9SW";
	
	public static final String SINGULARITY_C = "22diDLSo59iEa2ySEYXm3W5rGQj1ofB14BL6hNSUCApo9sC3EQU";
	
	public static final int MAX_COUNTER = 3;
	
	public static final String SHA3_512 = "SHA3-512";
	
	public static final int SHA3_256_LEN = 32;
	
	public static final int SHA3_512_LEN = 64;
	// 2020-04-1 Here need do more job to calculate the detailed value
	public static final BigInteger ASSET_PASSPORT_PROOF_SPACE_COST = BigInteger.valueOf(51);
	
	public static final BigInteger T1_LOCK_PROOF_SPACE_COST = BigInteger.valueOf(50);
	
	public static final BigInteger T2_LOCK_PROOF_SPACE_COST = BigInteger.valueOf(82);
	
	public static boolean IsDeleteTransactionInPool = false;
	
//	public static ID [] FIBONACCI = {
//			new ID(1597),
//			new ID(2584),
//			new ID(4181),
//			new ID(6765),
//			new ID(10946),
//			new ID(17711),
//			new ID(28657),
//			new ID(46368),
//			new ID(75025),
//			new ID(121393)
//	};
	
	public static ID [] FIBONACCI = {
			new ID(1597), // 17
			new ID(5702887), // 34
			new ID(new BigInteger("1134903170")), // 45
			new ID(new BigInteger("1548008755920")), // 60
			new ID(new BigInteger("5527939700884757")), // 77
			new ID(new BigInteger("1779979416004714189")), // 89
			new ID(new BigInteger("1500520536206896083277")), // 103
			new ID(new BigInteger("3311648143516982017180081")), // 119
			new ID(new BigInteger("1066340417491710595814572169")), // 131
			new ID(new BigInteger("3807901929474025356630904134051")), // 148
			new ID(new BigInteger("1206484255615496768210420703829205488386909032955899056732883572731058504300529011053")) // 404
	};
	
	public static ID[] PRIME101 = { new ID(2), new ID(3), new ID(5), new ID(7), new ID(11), new ID(13), new ID(17),
			new ID(19), new ID(23), new ID(29), new ID(31), new ID(37), new ID(41), new ID(43), new ID(47), new ID(53),
			new ID(59), new ID(61), new ID(67), new ID(71), new ID(73), new ID(79), new ID(83), new ID(89), new ID(97),
			new ID(101), new ID(103), new ID(107), new ID(109), new ID(113), new ID(127), new ID(131), new ID(137),
			new ID(139), new ID(149), new ID(151), new ID(157), new ID(163), new ID(167), new ID(173), new ID(179),
			new ID(181), new ID(191), new ID(193), new ID(197), new ID(199), new ID(211), new ID(223), new ID(227),
			new ID(229), new ID(233), new ID(239), new ID(241), new ID(251), new ID(257), new ID(263), new ID(269),
			new ID(271), new ID(277), new ID(281), new ID(283), new ID(293), new ID(307), new ID(311), new ID(313),
			new ID(317), new ID(331), new ID(337), new ID(347), new ID(349), new ID(353), new ID(359), new ID(367),
			new ID(373), new ID(379), new ID(383), new ID(389), new ID(397), new ID(401), new ID(409), new ID(419),
			new ID(421), new ID(431), new ID(433), new ID(439), new ID(443), new ID(449), new ID(457), new ID(461),
			new ID(463), new ID(467), new ID(479), new ID(487), new ID(491), new ID(499), new ID(503), new ID(509),
			new ID(521), new ID(523), new ID(541), new ID(547) };
	
//	static {
//		init(OS.WINDOWS); 
//	}

	public enum OS {
		WINDOWS, MAC, LINUX
	}

	public enum PERSISTENCE {
		ROCKSDB, H2, RPC
	}
	
	public enum MODE {
		LIGHT, FULL, MINER;
		public static MODE get(int ordinal) {
			MODE mode = null;
			switch (ordinal) {
			case 0:
				mode = LIGHT;
				break;
			case 1:
				mode = FULL;
				break;
			case 2:
				mode = MINER;
				break;
			default:
				mode = LIGHT;
				break;
			}
			return mode;
		}
		public boolean isSanity() {
			if((this.ordinal() < LIGHT.ordinal()) || (this.ordinal() > MINER.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}

	private Util() {
	}

	public static void init() throws Exception {
		System.setProperty("sun.net.client.defaultConnectTimeout", "3000");  
		System.err.close();
	    System.setErr(System.out);
		createDir(PATH);
//		createDir(AVRO_PATH);
		createDir(DB_PATH);
		createDir(HIVE_PATH);
		createDir(H2_PATH);
		createDir(ROCKSDB_PATH);
//		Test.testKeystore(); // Test stub
//		File file = new File(ROCKSDB_PATH + File.separator + "LOCK");
//		if(file.exists()) {
//			if(file.delete()) {
//				Log.info("Lock delete");
//			}
//			else {
//				Log.info("Lock undelete");
//			}
//		}
//		else {
//			Log.info("Lock doesn't exists");
//		}
		if (!Configuration.getInstance().isInitSingularityBlock()
				/* && Keystore.getInstance().getUserAccounts().size() > 0 Will Remove when Cold Wallet ready */) {
//			Log.info("0");
//			Test.testKeystore();
			DB().saveEQCHiveTailHeight(ID.ZERO);
			EQCHive eqcBlock = recoverySingularityStatus();
//			EQCBlockChainH2.getInstance().saveEQCBlock(eqcBlock);
//			Log.info("1");
			DB().saveEQCHive(eqcBlock);
//			Address address = eqcBlock.getTransactions().getAddressList().get(0);
//			if(!EQCBlockChainH2.getInstance().isAddressExists(address)) {
//				EQCBlockChainH2.getInstance().appendAddress(address, SerialNumber.ZERO);
//			}
//			EQCBlockChainH2.getInstance().addAllTransactions(eqcBlock);// .addTransaction(eqcBlock.getTransactions().getTransactionList().get(0),
																		// SerialNumber.ZERO, 0);
//			EQCBlockChainH2.getInstance().saveEQCBlockTailHeight(new ID(BigInteger.ZERO));
//			Log.info("2");
//			EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(ID.ZERO);
//			Log.info("3");
			Configuration.getInstance().updateIsInitSingularityBlock(true);
//			Log.info("4");
		}
		cookie = new Cookie();
//		Util.IP = getIP();
		cookie.setIp(LOCAL_IP);
		cookie.setVersion(PROTOCOL_VERSION);
//		info = new Info();
//		info.setCode(Code.OK);
//		info.setCookie(cookie);
//		syncMinerList(); // For light node need design a way to sync miner list
	}

//	private static void init(final OS os) {
//		switch (os) {
//		case MAC:
//			PATH = MAC_PATH;
//			break;
//		case LINUX:
//			PATH = LINUX_PATH;
//			break;
//		case WINDOWS:
//		default:
//			PATH = WINDOWS_PATH;
//			break;
//		}
//	}

	public static byte[] dualSHA3_512(final byte[] data) {
		byte[] bytes = null;
		try {
			bytes = MessageDigest.getInstance(SHA3_512).digest(MessageDigest.getInstance(SHA3_512).digest(data));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}

//	public static byte[] multipleExtend(final byte[] data, final int multiple) {
//		byte[] result = null;
//		MathContext mathContext = new MathContext(512, RoundingMode.HALF_EVEN);
//		BigInteger begin = new BigInteger(1, data);
//		BigDecimal multipleBigDecimal = new BigDecimal(BigInteger.valueOf(multiple));
//		BigDecimal beginBigDecimal = new BigDecimal(begin);
//		BigDecimal endBigDecimalMultiply = beginBigDecimal.multiply(beginBigDecimal).multiply(new BigDecimal(FIBONACCI[9]));
//		BigDecimal endBigDecimalDivide = beginBigDecimal.divide(beginBigDecimal.divide(multipleBigDecimal, mathContext).multiply(new BigDecimal(FIBONACCI[0])), mathContext);
//		int halfBufferLen = multiple / 2 + 1;
//		int bufferLen = endBigDecimalMultiply.toPlainString().getBytes(StandardCharsets.US_ASCII).length * 3 * multiple;// + 
//				//endBigDecimalDivide.toPlainString().getBytes(StandardCharsets.US_ASCII).length * halfBufferLen;
//		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLen);
//		for (int i = 1; i <= multiple; ++i) {
//			if(i%2 == 1) {
////				Log.info(i + " : " + beginBigDecimal.multiply(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i)).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])))).toPlainString());
//				String[] number = beginBigDecimal.multiply(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i)).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])))).toPlainString().split("\\.");
////				Log.info(beginBigDecimal.multiply(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i)).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])))).toPlainString());
////				Log.info("Len: " + number.length);
//				if (number.length == 2) {
//					byte[] number1 = new BigInteger(number[0]).toByteArray();
//					byte[] number2 = new BigInteger(number[1]).toByteArray();
//					byte[] number3 = new byte[number1.length + number2.length];
//					System.arraycopy(number1, 0, number3, 0, number1.length);
//					System.arraycopy(number2, 0, number3, number1.length, number2.length);
////					Log.info(Util.dumpBytes(number3, 16));
//					byteBuffer.put(number3);
//				}
//				else {
//					byte[] number1 = new BigInteger(number[0]).toByteArray();
//					byteBuffer.put(number1);
//				}
////				byteBuffer.put(beginBigDecimal.multiply(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i)).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])))).toPlainString().getBytes(StandardCharsets.US_ASCII));
//			}
//			else {
////				Log.info(i + " : " + beginBigDecimal.divide(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i))).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])), new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).toPlainString());
//				String[] number = beginBigDecimal.divide(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i))).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])), new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).toPlainString().split("\\.");
//				byte[] number2 = new BigInteger(number[1]).toByteArray();
////				Log.info(Util.dumpBytes(number2, 16));
//				byteBuffer.put(number2);
////				byteBuffer.put(beginBigDecimal.divide(beginBigDecimal.multiply(new BigDecimal(BigInteger.valueOf(i))).divide(multipleBigDecimal, new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).multiply(new BigDecimal(FIBONACCI[(i-1)%10])), new MathContext(F01+i%HUNDRED, RoundingMode.HALF_EVEN)).toPlainString().getBytes(StandardCharsets.US_ASCII));
//			}
//		}
//		byteBuffer.flip();
//		if(byteBuffer.remaining() == bufferLen) {
////			Log.info("multipleExtend equal: " + bufferLen);
//			result = byteBuffer.array();
//		}
//		else {
////			Log.info("multipleExtend not equal");
//			result = new byte[byteBuffer.remaining()];
//			byteBuffer.get(result);
////			Log.info(Util.dumpBytes(Util.CRC32C(result), 16));
////			Log.info("Len: " + result.length);
//		}
////		Log.info("Len: " + result.length);
//		return result;
//	}
	
//	public static byte[] multipleExtend(final byte[] data, final int multiple) {
//		byte[] result = null;
//		BigInteger begin = new BigInteger(1, data);
//		BigInteger divisor = begin.divide(BigInteger.valueOf(multiple));
//		BigInteger end = begin.multiply(BigInteger.valueOf(multiple).multiply(FIBONACCI[9]).multiply(divisor));
//		
//		int bufferLen = end.toByteArray().length * multiple;
//		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLen);
//		for (int i = 1; i <= multiple; ++i) {
//			byteBuffer.put(begin.add(begin.divide(BigInteger.valueOf(i)).multiply(FIBONACCI[i%10]).subtract(FIBONACCI[i%10])).toByteArray());
//		}
//		byteBuffer.flip();
//		if(byteBuffer.remaining() == bufferLen) {
////			Log.info("multipleExtend equal: " + bufferLen);
//			result = byteBuffer.array();
//		}
//		else {
////			Log.info("multipleExtend not equal");
//			result = new byte[byteBuffer.remaining()];
//			byteBuffer.get(result);
////			Log.info(Util.dumpBytes(Util.CRC32C(result), 16));
////			Log.info("Len: " + result.length);
//		}
//		
//		
////		for (int i = 0; i < multiple; ++i) {
////			for (int j = 0; j < data.length; ++j) {
////				result[j + data.length * i] = data[j];
////			}
////		}
//		return result;
//	}
	
	public static byte[] multipleExtendMix(final byte[] data, final int multiple) {
		byte[] result = null;

		BigDecimal begin = new BigDecimal(new BigInteger(1, data));
		MathContext mc = new MathContext(Util.THOUSANDPLUS, RoundingMode.HALF_EVEN);
		BigDecimal a = null, b = null, c = null, d = null;
		int bufferLen = ((data.length+1)*2+417) * multiple;
		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLen);
		// Put the original raw data
		byteBuffer.put(data);
		byteBuffer.put(SINGULARITY);
		// Put the multiple extended data
		BigInteger random;
		byte[] randomBytes, partOfRandomBytes;
		for (int i = 1; i <= multiple; ++i) {
//			Log.info("Begin: " + begin.toPlainString());
			random = begin.toBigInteger();
			randomBytes = random.toByteArray();
			partOfRandomBytes = new byte[(random.toByteArray().length/2)];
			if(random.mod(ID.TWO).equals(ID.ZERO)) {
				for(int j=0;j<partOfRandomBytes.length;++j) {
					partOfRandomBytes[j] = randomBytes[2*j];
				}
			}
			else {
				for(int j=0;j<partOfRandomBytes.length;++j) {
					partOfRandomBytes[j] = randomBytes[2*j+1];
				}
			}
			a = begin.divide(new BigDecimal(new BigInteger(1, partOfRandomBytes)), mc);
			b = a.divide(new BigDecimal(FIBONACCI[2]), mc);
			c = a.divide(new BigDecimal(FIBONACCI[10]), mc);
			d = b.subtract(c).abs().multiply(new BigDecimal(PRIME101[(multiple - i)%HUNDREDPULS]), mc);

			begin = begin.add(a).add(b).add(c).add(d);
//			Log.info("i: " + i + " " + begin.toPlainString());
			String[] abc = begin.toPlainString().split("\\.");
			if (abc.length == 2) {
//				Log.info("...");
				BigInteger e = new BigInteger(abc[0]);
				BigInteger f = new BigInteger(abc[1]);
				byteBuffer.put(e.toByteArray());
				byteBuffer.put(SINGULARITY);
//				Log.info("F: " + f.toByteArray().length);
				byteBuffer.put(f.toByteArray());
				if(i < multiple) {
					byteBuffer.put(SINGULARITY);
				}
			} else {
				BigInteger e = new BigInteger(abc[0]);
				byteBuffer.put(e.toByteArray());
				if(i < multiple) {
					byteBuffer.put(SINGULARITY);
				}
			}
		}
		byteBuffer.flip();
		if (byteBuffer.remaining() == bufferLen) {
			result = byteBuffer.array();
		} else {
			result = new byte[byteBuffer.remaining()];
			byteBuffer.get(result);
		}
		return result;
	}
	
	public static byte[] multipleExtend(final byte[] data, final int multiple) {
		ByteBuffer byteBuffer = ByteBuffer.allocate((data.length + SINGULARITY.length) * multiple);
		// Put the multiple extended data
		for (int i = 0; i < multiple; ++i) {
				byteBuffer.put(data);
				if(i < multiple - 1) {
					byteBuffer.put(SINGULARITY);
				}
		}
		byteBuffer.flip();
		return byteBuffer.array();
	}

	public static byte[] updateNonce(final byte[] bytes, final int nonce) {
		System.arraycopy(Util.intToBytes(nonce), 0, bytes, 140, 4);
		return bytes;
	}

	public static String getGMTTime(final long timestamp) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(timestamp);
	}

	public static String getHexString(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
//		return	bigIntegerTo128String(new BigInteger(1, bytes));
		return	bigIntegerTo512String(new BigInteger(1, bytes));
	}
	
	public static String bytesToHexString(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return	new BigInteger(1, bytes).toString(16);
	}
	
//	public static BigInteger getDefaultTarget() {
//		return BigInteger.valueOf(Long.parseLong("dafedcba9876543", 16)).shiftLeft(8)
//				.add(BigInteger.valueOf(Long.parseLong("21", 16))).shiftLeft(60);
//	}

	public static byte[] getDefaultTargetBytes() {
//		return new byte[] { 0x68, (byte) 0xda, (byte) 0xab, (byte) 0xcd };
		return new byte[] { (byte) 0xF4, (byte) 0x4F, (byte) 0xAB, (byte) 0xCD };
	}

	public static BigInteger targetBytesToBigInteger(byte[] foo) {
		int target = bytesToInt(foo);
//		long l = target;
//		int i = target >>> 23;
//		int j = target << 9;
//		Log.info(Util.dumpBytes(Util.intToBytes(target >>> 23), 2));
//		Log.info("" + Util.dumpBytes(Util.intToBytes(target >>>23), 16));
//		Log.info(Util.dumpBytes(foo, 16));
//		return BigInteger.valueOf(Long.valueOf(target & 0x00ffffff)).shiftLeft((target & 0xff000000) >>> 24);
//		Log.info("" + (target << 9));
//		Log.info(bigIntegerTo512String(BigInteger.valueOf(Long.valueOf((target << 9) >>> 9)).shiftLeft(target >>> 23)));
		return BigInteger.valueOf(Long.valueOf((target << 9) >>> 9)).shiftLeft(target >>> 23);
	}

	public static byte[] bigIntegerToTargetBytes(BigInteger foo) {
		EQCType.assertNotNegative(foo);
		byte[] bytes = foo.toByteArray();
		byte[] target;
		int offset;
		// Exists Leading zero
		if ((bytes[0] == 0) && (bytes[1] < 0)) {
			target = new byte[] { bytes[1], bytes[2], bytes[3], bytes[4] };
			offset = (bytes.length - 1) * 8 - 23;
		} 
		// Doesn't exists Leading zero
		else {
			target = new byte[] { bytes[0], bytes[1], bytes[2], bytes[3] };
			offset = bytes.length * 8 - 23;
		}
//		Log.info("" + offset);
//		Log.info("" + (offset & 0x1FF));
		return intToBytes((bytesToInt(target) >>> 9) | ((offset & 0x1FF) << 23));
//		return intToBytes((bytesToInt(target) >>> 9) | (((((offset * 8) == 512)?511:(offset * 8)) & 0x1FF) << 23));
//		if (bytes.length <= 3) {
//			return intToBytes(foo.intValue() & 0x00FFFFFF);
//		} else {
//			byte[] target;
//			int offset;
//			if ((bytes[0] == 0) && (bytes[1] < 0)) {
//				target = new byte[] { 0, bytes[1], bytes[2], bytes[3] };
//				offset = bytes.length - 4;
//			} else {
//				target = new byte[] { 0, bytes[0], bytes[1], bytes[2] };
//				offset = bytes.length - 3;
//			}
//			return intToBytes((bytesToInt(target) & 0x00FFFFFF) | (((offset * 8) & 0xFF) << 24));
//		}
		
		
//		String target = foo.toString(2);
//		if(target.length() <= 24) {
//			int value = new BigInteger(target, 2).intValue();
//			return intToBytes(value & 0x00FFFFFF);
//		}
//		else {
//			int value = new BigInteger(target.substring(0, 24), 2).intValue();
//			int a = (value & 0x00FFFFFF);
//			int d = (target.length() - 24) & 0xFF;
//			int e = d << 24;
//			int b = (((target.length() - 24) & 0xFF) << 24);
//			int c = a | b;
//			return intToBytes((value & 0x00FFFFFF) | (((target.length() - 24) & 0xFF) << 24));
//		}
	}

	public static byte[] bigIntegerTo64Bytes(final BigInteger foo) {
		byte[] tmp = new byte[64];
		byte[] fooBytes = foo.toByteArray();
		if (fooBytes.length == 65) {
			System.arraycopy(fooBytes, 1, tmp, 0, tmp.length);
		} else {
			// Because big-endian byte-order so fill 0 in the high position to fill the gap
			System.arraycopy(fooBytes, 0, tmp, tmp.length - fooBytes.length, fooBytes.length);
		}
		return tmp;
	}

	public static byte[] bigIntegerTo16Bytes(final BigInteger foo) {
		byte[] tmp = new byte[16];
		byte[] fooBytes = foo.toByteArray();
		if (fooBytes.length == 17) {
			System.arraycopy(fooBytes, 1, tmp, 0, tmp.length);
		} else {
			// Because big-endian byte-order so fill 0 in the high position to fill the gap
			System.arraycopy(fooBytes, 0, tmp, tmp.length - fooBytes.length, fooBytes.length);
		}
		return tmp;
	}

	public static BigInteger bytesToBigInteger(final byte[] foo) {
		return new BigInteger(foo);
	}

	public static String bigIntegerTo512String(final BigInteger foo) {
		return bigIntegerToFixedLengthString(foo, 512).toUpperCase();
	}

	public static String bigIntegerTo128String(final BigInteger foo) {
		return bigIntegerToFixedLengthString(foo, 128);
	}

	public static String bigIntegerToFixedLengthString(final BigInteger foo, final int len) {
		String tmp = foo.toString(16);
//		Log.info(tmp.length() + "  " + tmp);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len / 4 - tmp.length(); ++i) {
			sb.append("0");
		}
		sb.append(tmp);
		return sb.toString();
	}

	public static byte[] shortToBytes(final short foo) {
//		return ByteBuffer.allocate(2).putLong(foo).array();
		return new byte[] { (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static short bytesToShort(final byte[] bytes) {
//		return ByteBuffer.allocate(2).put(bytes, 0, bytes.length).flip().getShort();
		return (short) (bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8);
	}

	public static byte[] intToBytes(final int foo) {
		return new byte[] { (byte) ((foo >> 24) & 0xFF), (byte) ((foo >> 16) & 0xFF), (byte) ((foo >> 8) & 0xFF),
				(byte) (foo & 0xFF) };
//		return ByteBuffer.allocate(4).putInt(foo).array();
	}

	public static byte intToByte(final int foo) {
		return (byte) (foo & 0xFF);
	}

	public static byte[] intTo2Bytes(final int foo) {
		return new byte[] { (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static byte[] intTo3Bytes(final int foo) {
		return new byte[] { (byte) ((foo >> 16) & 0xFF), (byte) ((foo >> 8) & 0xFF), (byte) (foo & 0xFF) };
	}

	public static int bytesToInt(final byte[] bytes) {
		int foo = 0;
		if (bytes.length == 1) {
			foo = (bytes[0] & 0xFF);
		} else if (bytes.length == 2) {
			foo = (bytes[1] & 0xFF | (bytes[0] & 0xFF) << 8);
		} else if (bytes.length == 3) {
			foo = (bytes[2] & 0xFF | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF) << 16);
		} else if (bytes.length == 4) {
			foo = (bytes[3] & 0xFF | (bytes[2] & 0xFF) << 8 | (bytes[1] & 0xFF) << 16 | (bytes[0] & 0xFF) << 24);
		}
		return foo;
	}
	
	public static byte[] longToBytes(final long foo) {
		return ByteBuffer.allocate(8).putLong(foo).array();
	}

	public static long bytesToLong(final byte[] bytes) {
		return new BigInteger(1, bytes).longValue();
//		return ByteBuffer.allocate(8).put(bytes, 0, bytes.length).flip().getLong();
	}

	public static boolean createDir(final String dir) {
		boolean boolIsSuccessful = true;
		File file = new File(dir);
		if (!file.isDirectory()) {
			boolIsSuccessful = file.mkdir();
			Log.info("Create directory " + dir + boolIsSuccessful);
		} else {
			if (file.isDirectory()) {
				Log.info(dir + " already exists.");
			} else {
				Log.Error("Create directory " + dir + " failed and this directory doesn't exists.");
			}
		}
		return boolIsSuccessful;
	}

	public static byte[] getSecureRandomBytes() {
		byte[] bytes = new byte[64];
		try {
			SecureRandom.getInstanceStrong().nextBytes(bytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.info(e.toString());
		}
		return bytes;
	}

	public static String dumpBytes(final byte[] bytes, final int radix) {
		return new BigInteger(1, bytes).toString(radix).toUpperCase();
	}

//	public static byte[] EQCCHA_MULTIPLE(final byte[] bytes, int multiple, boolean isCompress) {
//		return EQCCHA_MULTIPLE_DUAL(bytes, multiple, true, isCompress);
//	}
	
	/**
	 * EQCCHA_MULTIPLE_DUAL - EQchains complex hash algorithm used for calculate the hash
	 * of EQC block chain's header and address. Each input data will be expanded by
	 * a factor of multiple.
	 * 
	 * @param bytes      The raw data for example EQC block chain's header or
	 *                   address
	 * @param multiple   The input data will be expanded by a factor of multiple
	 * @param isDual 	 If use SHA3-512 handle the input data
	 * @param isCompress If this is an address or signatures. Then at the end use
	 *                   SHA3-256 to reduce the size of it
	 * @return Hash value processed by EQCCHA
	 */
	public static byte[] EQCCHA_MULTIPLE_DUAL(final byte[] bytes, int multiple, boolean isDual, boolean isCompress) {
		if(bytes == null) {
			return EQCType.NULL_ARRAY;
		}
		byte[] hash = bytes;
//		Log.info("Len: " + bytes.length);
		try {
//			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(hash, multiple));
			if(isDual) {
				hash = MessageDigest.getInstance(SHA3_512).digest(multipleExtend(hash, multiple));
			}
			// Due to this is an address or signature so here use SHA3-256 reduce the size of it
			if (isCompress) {
				hash = SHA3_256(multipleExtend(hash, multiple));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return hash;
	}
	
	/**
	 * EQCCHA_MULTIPLE_DUAL_MIX - EQchains complex hash algorithm used for calculate the hash
	 * of EQC block chain's header. Each input data will be expanded by
	 * a factor of multiple.
	 * 
	 * @param bytes      The raw data for example EQC block chain's header or
	 *                   address
	 * @param multiple   The input data will be expanded by a factor of multiple
	 * @param isDual 	 If use SHA3-512 handle the input data
	 * @param isCompress If this is an address or signatures. Then at the end use
	 *                   SHA3-256 to reduce the size of it
	 * @return Hash value processed by EQCCHA
	 */
	public static byte[] EQCCHA_MULTIPLE_DUAL_MIX(final byte[] bytes, int multiple, boolean isDual, boolean isCompress) {
		byte[] hash = bytes;
//		Log.info("Len: " + bytes.length);
		try {
//			hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtend(hash, multiple));
			if(isDual) {
				hash = MessageDigest.getInstance("SHA3-512").digest(multipleExtendMix(hash, multiple));
			}
			// Due to this is an address or signature so here use SHA3-256 reduce the size of it
			if (isCompress) {
				hash = SHA3_256(multipleExtendMix(hash, multiple));
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return hash;
	}
	
	public static byte[] EQCCHA_MULTIPLE_FIBONACCI_MERKEL(final byte[] bytes, int multiple) throws NoSuchAlgorithmException {
		Vector<byte[]> ten = new Vector<byte[]>();
		BigInteger begin = new BigInteger(1, bytes);
		BigInteger divisor = begin.divide(BigInteger.valueOf(multiple));
		MerkleTree merkleTree = null;
		for(int i=1; i<=10; ++i) {
			ten.add(EQCCHA_MULTIPLE_DUAL(begin.multiply(BigInteger.valueOf(i).multiply(divisor).multiply(FIBONACCI[i-1])).toByteArray(), multiple, true, false));
//			Log.info("i: " + i + " len: " + ten.get(i).length);
		}
		merkleTree = new MerkleTree(ten, true);
		merkleTree.generateRoot();
		return EQCCHA_MULTIPLE_DUAL(merkleTree.getRoot(), multiple, true, false);
	}
	
	public static byte[] SHA3_256(byte[] bytes) {
		byte[] result = null;
		try {
			result =  MessageDigest.getInstance("SHA3-256").digest(bytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result;
	}

	public static byte[] RIPEMD160(final byte[] bytes) {
		RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(bytes, 0, bytes.length);
		byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

	public static byte[] RIPEMD128(final byte[] bytes) {
		RIPEMD128Digest digest = new RIPEMD128Digest();
		digest.update(bytes, 0, bytes.length);
		byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

	public static String dumpBytesBigEndianHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
//			if (i % 8 == 0) {
//				sb.append(" ");
//			}
			sb.append(Integer.toHexString(bytes[i]).toUpperCase());
		}
		return sb.toString();
	}

	public static String dumpBytesBigEndianBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(binaryString(Integer.toBinaryString(bytes[i] & 0xFF)));
		}
		return sb.toString();
	}

	public static String dumpBytesLittleEndianHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(Integer.toHexString(bytes[i] & 0xFF));
		}
		return sb.toString();
	}

	public static String dumpBytesLittleEndianBinary(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(Integer.toBinaryString(bytes[i] & 0xFF));
		}
		return sb.toString();
	}

	/**
	 * Add leading zero when the original binary string's length is less than 8.
	 * <p>
	 * For example when foo is 101111 the output is 00101111.
	 * 
	 * @param foo This value is a string of ASCII digitsin binary (base 2) with no
	 *            extra leading 0s.
	 * @return Fixed 8-bit long binary number with leading 0s.
	 */
	public static String binaryString(String foo) {
		if (foo.length() == 8) {
			return foo;
		} else {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 8 - foo.length(); ++i) {
				sb.append(0);
			}
			sb.append(foo);
			return sb.toString();
		}
	}

	public static byte[] AESEncrypt(byte[] bytes, String password) {
		byte[] result = null;
		try {
			KeyGenerator kgen;
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(256, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			result = cipher.doFinal(bytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getLocalizedMessage());
		}
		return result;
	}

	public static byte[] AESDecrypt(byte[] bytes, String password) {
		byte[] result = null;
		try {
			KeyGenerator kgen;
//			Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding","SunJCE");
			kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(password.getBytes());
			kgen.init(256, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			result = cipher.doFinal(bytes);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result;
	}

	public static byte[] reverseBytes(final byte[] bytes) {
		byte[] foo = new byte[bytes.length];
		for (int i = 0; i <= foo.length - 1; ++i) {
			foo[i] = bytes[bytes.length - 1 - i];
		}
		return foo;
	}

	public static ECPrivateKey getPrivateKey(byte[] privateKeyBytes, LockType lockType) {
		ECPrivateKey privateKey = null;
		try {
			AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
			if (lockType == LockType.T1) {
				parameters.init(new ECGenParameterSpec(Keystore.SECP256R1));
			} else if (lockType == LockType.T2) {
				parameters.init(new ECGenParameterSpec(Keystore.SECP521R1));
			}
			ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
			ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(privateKeyBytes), ecParameterSpec);
			privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(ecPrivateKeySpec);
		} catch (Exception e) {
			Log.Error(e.toString());
		}
		return privateKey;
	}

//	public static boolean verifySignature(LockType lockType, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
//		return verifySignature(transaction.getPublickey().getPublickey(), transaction.getEqcSegWit().getSignature(), lockType, transaction, TXIN_HEADER_HASH, SN);
//	}

	public static boolean verifySignature(byte[] compressedPublickey, byte[] userSignature, LockType lockType, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
		boolean isTransactionValid = false;
		Signature signature = null;

//		// Verify Address
//		if (!AddressTool.verifyAddress(transaction.getTxIn().getAddress().getAddress(),
//				transaction.getPublickey().getPublicKey())) {
//			Log.Error("Transaction's TxIn's Address error.");
//			return isTransactionValid;
//		}

		// Verify Signature
		try {
			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
			ECCTYPE eccType = null;
			if (lockType == LockType.T1) {
				eccType = ECCTYPE.P256;
			} else if (lockType == LockType.T2) {
				eccType = ECCTYPE.P521;
			}
			EQCPublicKey eqPublicKey = new EQCPublicKey(eccType);
			// Create EQPublicKey according to java Publickey
			eqPublicKey.setECPoint(compressedPublickey);
			Log.info(Util.dumpBytesBigEndianBinary(eqPublicKey.getEncoded()));
			signature.initVerify(eqPublicKey);
//			signature.update(EQCBlockChainH2.getInstance().getBlockHeaderHash(
//					EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getAddress())));
			signature.update(TXIN_HEADER_HASH);
			if(SN.length == 1) {
				signature.update(intToBytes(SN[0]));
			}
			signature.update(transaction.getBytes());
			isTransactionValid = signature.verify(userSignature);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return isTransactionValid;
	}
	
	public static byte[] signTransaction(LockType lockType, byte[] privateKey, Transaction transaction, byte[] TXIN_HEADER_HASH, int ...SN) {
		byte[] sign = null;
//		Signature signature = null;
		try {
//			signature = Signature.getInstance("SHA1withECDSA", "SunEC");
//			ECCTYPE eccType = null;
//			if (lockType == AddressType.T1) {
//				eccType = ECCTYPE.P256;
//			} else if (lockType == AddressType.T2) {
//				eccType = ECCTYPE.P521;
//			}
			Signature ecdsa;
			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
			ecdsa.initSign(Util.getPrivateKey(privateKey, lockType));
//			// Add current Transaction's relevant TxIn's Address's EQC block height which
//			// record the TxIn's Address.
//			ecdsa.update(EQCBlockChainH2.getInstance().getBlockHeaderHash(
//					EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getAddress())));
			ecdsa.update(TXIN_HEADER_HASH);
			if(SN.length == 1) {
				ecdsa.update(intToBytes(SN[0]));
			}
			ecdsa.update(transaction.getBytes());
			sign = ecdsa.sign();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return sign;
	}

	/**
	 * @author Xun Wang
	 * @date Sep 24, 2018
	 * @email 10509759@qq.com
	 */
	public static class LockTool {
		public final static int T1_PUBLICKEY_LEN = 33;
		public final static int T2_PUBLICKEY_LEN = 67;

		public enum LockType {
			T1, T2;
			public static LockType get(int ordinal) {
				LockType lockType = null;
				switch (ordinal) {
				case 0:
					lockType = LockType.T1;
					break;
				case 1:
					lockType = LockType.T2;
					break;
				}
				return lockType;
			}
			public byte[] getEQCBits() {
				return EQCType.intToEQCBits(this.ordinal());
			}
		}

		private LockTool() {
		}
	
		/**
		 * @param bytes compressed PublicKey. Each input will be extended 100 times
		 *              using EQCCHA_MULTIPLE
		 * @param type  EQC Address’ type
		 * @return EQC address
		 */
		public static String generateAddress(byte[] publicKey, LockType type) {
			byte[] publickey_hash = null;
			if(type == LockType.T1) {
				publickey_hash = EQCCHA_MULTIPLE_DUAL(publicKey, ELEVEN, false, true);
			}
			else if(type == LockType.T2) {
				publickey_hash = EQCCHA_MULTIPLE_DUAL(publicKey, HUNDREDPULS, true, true);
			}
			return _generateLock(publickey_hash, type);
		}
		
		private static String _generateLock(byte[] publickey_hash, LockType type) {
			byte[] type_publickey_hash = null;
			byte[] CRC32C = null;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			// Calculate (type + PublickeyHash)'s CRC32C
			try {
				os.write(type.ordinal());
//				Log.info("AddressType: " + type.ordinal());
				os.write(publickey_hash);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			try {
				type_publickey_hash = os.toByteArray();
				CRC32C = CRC32C(type_publickey_hash);
				os = new ByteArrayOutputStream();
				os.write(CRC32C);
				os.write(type.ordinal());
				os.write(CRC32C);
				os.write(publickey_hash);
				os.write(CRC32C);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			byte[] crc32c = null;
			if(type == LockType.T1) {
				crc32c = CRC32C(multipleExtend(os.toByteArray(), ELEVEN));
			}
			else if(type == LockType.T2) {
				crc32c = CRC32C(multipleExtend(os.toByteArray(), HUNDREDPULS));
			}
			// Generate address Base58(type) + Base58((HASH + (type + HASH)'s CRC32C))
			try {
				os = new ByteArrayOutputStream();
				os.write(publickey_hash);
				os.write(crc32c);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
		}
		
		public static boolean verifyEQCLockAndPublickey(EQCLock eqcLock, byte[] compressedPublickey) {
			byte[] publickey_hash = null;
			try {
				if(eqcLock.getLockType() == LockType.T1) {
					publickey_hash = EQCCHA_MULTIPLE_DUAL(compressedPublickey, ELEVEN, false, true);
				}
				else if(eqcLock.getLockType() == LockType.T2) {
					publickey_hash = EQCCHA_MULTIPLE_DUAL(compressedPublickey, HUNDREDPULS, true, true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return Arrays.equals(publickey_hash, eqcLock.getLockHash());
		}
		
		public static boolean verifyLockAndPublickey(String address, byte[] compressedPublickey) {
			byte[] hidden_address = null;
			byte[] publickey_hash = null;
			LockType lockType = getLockType(address);
			try {
				if(lockType == LockType.T1) {
					publickey_hash = EQCCHA_MULTIPLE_DUAL(compressedPublickey, ELEVEN, false, true);
				}
				else if(lockType == LockType.T2) {
					publickey_hash = EQCCHA_MULTIPLE_DUAL(compressedPublickey, HUNDREDPULS, true, true);
				}
				hidden_address = Base58.decode(address.substring(1));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return Arrays.equals(publickey_hash, Arrays.copyOf(hidden_address, hidden_address.length - CRC32C_LEN));
		}
		
		public static byte[] readableLockToAI(String address) throws Exception {
			byte[] bytes = null;
			ByteArrayOutputStream os = null;
			os = new ByteArrayOutputStream();
			os.write(Base58.decode(address.substring(0, 1)));
			bytes = Base58.decode(address.substring(1));
			os.write(bytes, 0, bytes.length - CRC32C_LEN);
			return os.toByteArray();
		}
		
		public static byte[] publickeyToAI(byte[] compressedPublickey) throws IOException {
			LockType lockType = getLockType(compressedPublickey);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			if(lockType == LockType.T1) {
				os.write(lockType.ordinal());
				os.write(EQCCHA_MULTIPLE_DUAL(compressedPublickey, ELEVEN, false, true));
			}
			else if(lockType == LockType.T2) {
				os.write(lockType.ordinal());
				os.write(EQCCHA_MULTIPLE_DUAL(compressedPublickey, HUNDREDPULS, true, true));
			}
			return os.toByteArray();
		}

		public static byte[] publickeyHashToAI(LockType lockType, byte[] publickeyHash) throws IOException {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write(lockType.ordinal());
			os.write(publickeyHash);
			return os.toByteArray();
		}
		
		public static String EQCLockToReadableLock(EQCLock eqcLock) throws NoSuchFieldException {
			Objects.requireNonNull(eqcLock);
			return _generateLock(eqcLock.getLockHash(), eqcLock.getLockType());
		}
		
		public static String AIToReadableLock(byte[] bytes) throws NoSuchFieldException {
			EQCType.assertNotNull(bytes);
			LockType lockType = LockType.T1;
			if (bytes[0] == 0) {
				lockType = LockType.T1;
			} else if (bytes[0] == 1) {
				lockType = LockType.T2;
			} 
//			else if (bytes[0] == 2) {
//				lockType = AddressType.T3;
//			}
			else {
				throw new UnsupportedOperationException("Unsupport type: " + bytes[0]);
			}
			
			return _generateLock(Arrays.copyOfRange(bytes, 1, bytes.length), lockType);
		}
		
		// 2020-02-23 need review this
		public static boolean isReadableLockSanity(String readableLock) {
			byte[] bytes = null;
			String addressContent = null, subString = null;
			char[] addressChar = null;
				if(readableLock.length() < MIN_ADDRESS_LEN || readableLock.length() > MAX_ADDRESS_LEN) {
					return false;
				}
				addressChar = readableLock.toCharArray();
				if(addressChar[0] != '1' && addressChar[0] != '2') {
					return false;
				}
				for(char alphabet : addressChar) {
					if(!Base58.isBase58Char(alphabet)) {
						return false;
					}
				}
//				subString = address.substring(1);
//				bytes = Base58.decode(subString);
//				addressContent = Base58.encode(bytes);
//				if(!addressContent.equals(subString)) {
//					return false;
//				}
			
			return true;
		}
		
		public static boolean verifyAddressCRC32C(String address) {
			byte[] bytes = null;
			byte[] crc32c = new byte[CRC32C_LEN];
			byte[] CRC32C = null;
			byte[] CRC32CC = null;
			byte[] type_publickey_hash = null;
			LockType lockType = getLockType(address);
			
			try {
				bytes = Base58.decode(address.substring(1));
				System.arraycopy(bytes, bytes.length - CRC32C_LEN, crc32c, 0, CRC32C_LEN);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(Base58.decode(address.substring(0, 1)));
				os.write(bytes, 0, bytes.length - CRC32C_LEN);
				try {
					type_publickey_hash = os.toByteArray();
					CRC32CC = CRC32C(type_publickey_hash);
					os = new ByteArrayOutputStream();
					os.write(CRC32CC);
					os.write(Base58.decode(address.substring(0, 1)));
					os.write(CRC32CC);
					os.write(bytes, 0, bytes.length - CRC32C_LEN);
					os.write(CRC32CC);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(lockType == LockType.T1) {
					CRC32C = CRC32C(multipleExtend(os.toByteArray(), ELEVEN));
				}
				else if(lockType == LockType.T2) {
					CRC32C = CRC32C(multipleExtend(os.toByteArray(), HUNDREDPULS));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			return Arrays.equals(crc32c, CRC32C);
		}
		
//		/**
//		 * @param bytes compressed PublicKey. Each input will be extended 100 times
//		 *              using EQCCHA_MULTIPLE
//		 * @param type  EQC Address’ type
//		 * @return EQC address
//		 */
//		@Deprecated 
//		public static String generateAddress(byte[] publicKey, AddressType type) {
//			byte[] bytes = EQCCHA_MULTIPLE(publicKey, Util.HUNDRED, true);
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			// Calculate (type | trim(HASH))'s CRC8ITU
//			os.write(type.ordinal());
//			Log.info("AddressType: " + type.ordinal());
//			try {
//				os.write(trim(bytes));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			byte crc = CRC8ITU.update(os.toByteArray());
//			// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//			// CRC8ITU))
//			os = new ByteArrayOutputStream();
//			try {
//				os.write(trim(bytes));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			os.write(crc);
//			return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
//		}
//		
//		@Deprecated 
//		public static boolean verifyAddress(String address, byte[] publickey) {
//			byte[] bytes = null;
//			byte crc = 0;
//			byte CRC = 0;
//			byte[] publicKey_hash = trim(EQCCHA_MULTIPLE(publickey, HUNDRED, true));
//			try {
//				bytes = Base58.decode(address.substring(1));
//				crc = bytes[bytes.length - 1];
//				ByteArrayOutputStream os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				os.write(bytes, 0, bytes.length - 1);
//				CRC = CRC8ITU.update(os.toByteArray());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return (crc == CRC) && Arrays.equals(publicKey_hash, Arrays.copyOf(bytes, bytes.length - 1));
//		}
//		
//		@Deprecated 
//		public static byte[] addressToAI(String address) {
//			byte[] bytes = null;
//			ByteArrayOutputStream os = null;
//			try {
//				os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				bytes = Base58.decode(address.substring(1));
//				os.write(bytes, 0, bytes.length - 1);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return os.toByteArray();
//		}
//
//		@Deprecated 
//		public static String AIToAddress(byte[] bytes) {
//			AddressType lockType = AddressType.T1;
//			if (bytes[0] == 1) {
//				lockType = AddressType.T1;
//			} else if (bytes[0] == 2) {
//				lockType = AddressType.T2;
//			} else if (bytes[0] == 3) {
//				lockType = AddressType.T3;
//			}
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			// Calculate (type | trim(HASH))'s CRC8ITU
//			os.write(lockType.ordinal());
//			try {
//				os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			byte crc = CRC8ITU.update(os.toByteArray());
//			// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//			// CRC8ITU))
//			os = new ByteArrayOutputStream();
//			try {
//				os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			os.write(crc);
//			return Base58.encode(new byte[] { (byte) lockType.ordinal() }) + Base58.encode(os.toByteArray());
//		}
//		
//		@Deprecated 
//		public static boolean verifyAddress(String address) {
//			byte[] bytes = null;
//			byte crc = 0;
//			byte CRC = 0;
//			try {
//				bytes = Base58.decode(address.substring(1));
//				crc = bytes[bytes.length - 1];
//				ByteArrayOutputStream os = new ByteArrayOutputStream();
//				os.write(Base58.decode(address.substring(0, 1)));
//				os.write(bytes, 0, bytes.length - 1);
//				CRC = CRC8ITU.update(os.toByteArray());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				Log.Error(e.getMessage());
//			}
//			return (crc == CRC);
//		}

		public static byte[] trim(final byte[] bytes) {
			int i = 0;
			for (; i < bytes.length; ++i) {
				if (bytes[i] != 0) {
					break;
				}
			}
			int j = bytes.length - 1;
			for (; j > 0; --j) {
				if (bytes[j] != 0) {
					break;
				}
			}
			byte[] trim = new byte[j - i + 1];
			System.arraycopy(bytes, i, trim, 0, trim.length);
			return trim;
		}
		
		// 2020-04-01 Here exists bug need change another algorithm for example compare the publickey range? Maybe doesn't need do this because of the length is fixed
		public static LockType getLockType(byte[] publickey) {
			LockType lockType = null;
			if(publickey.length == P256_PUBLICKEY_LEN) {
				lockType = LockType.T1;
			}
			else if(publickey.length == P521_PUBLICKEY_LEN) {
				lockType = LockType.T2;
			}
			else {
				throw new IllegalStateException("Invalid publickey length:" + publickey.length);
			}
			return lockType;
		}

		public static LockType getLockType(String address) {
			byte type = 0;
			LockType lockType = LockType.T1;
			try {
				type = Base58.decodeToBigInteger(address.substring(0, 1)).byteValue();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			if (type == 0) {
				lockType = LockType.T1;
			} else if (type == 1) {
				lockType = LockType.T2;
			} 
			else {
				throw new IllegalStateException("Invalid lock type: " + lockType);
			}
			return lockType;
		}
		
		public static LockType getLockTypeFromAI(byte[] aiLock) {
			byte type = 0;
			LockType lockType = null;
			type = aiLock[0];
			if (type == 0) {
				lockType = LockType.T1;
			} else if (type == 1) {
				lockType = LockType.T2;
			} else {
				throw new IllegalStateException("Invalid lock type: " + type);
			}
			return lockType;
		}
		
	}

	public static TransferCoinbaseTransaction generateTransferCoinbaseTransaction(EQCLockMate minerLock,
			ChangeLog changeLog) {
		TransferCoinbaseTransaction transaction = new TransferCoinbaseTransaction();
		TransferTxOut eqcFederalTxOut = new TransferTxOut();
		TransferTxOut minerTxOut = new TransferTxOut();
		try {
			minerTxOut.setPassportId(ID.ZERO);
			
			Value minerCoinbaseReward = getCurrentMinerCoinbaseReward(getCurrentCoinbaseReward(changeLog));
			eqcFederalTxOut.setValue(getCurrentCoinbaseReward(changeLog).subtract(minerCoinbaseReward));
			minerTxOut.setValue(minerCoinbaseReward);

			transaction.setEqCoinFederalTxOut(eqcFederalTxOut);
			transaction.setEqCoinMinerTxOut(minerTxOut);
			transaction.setNonce(changeLog.getHeight().getNextID());
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return transaction;
	}
	
	public static ZionCoinbaseTransaction generateZionCoinbaseTransaction(EQCLock minerLock,
			ChangeLog changeLog) {
		ZionCoinbaseTransaction transaction = new ZionCoinbaseTransaction();
		TransferTxOut eqcFederalTxOut = new TransferTxOut();
		ZionTxOut minerTxOut = new ZionTxOut();
		try {
			eqcFederalTxOut.setPassportId(ID.ZERO);
			minerTxOut.setLock(minerLock);
			
			Value minerCoinbaseReward = getCurrentMinerCoinbaseReward(getCurrentCoinbaseReward(changeLog));
			eqcFederalTxOut.setValue(getCurrentCoinbaseReward(changeLog).subtract(minerCoinbaseReward));
			minerTxOut.setValue(minerCoinbaseReward);

			transaction.setEqCoinFederalTxOut(eqcFederalTxOut);
			transaction.setEqCoinMinerTxOut(minerTxOut);
			transaction.setNonce(changeLog.getHeight().getNextID());
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return transaction;
	}

	public static EQCHive recoverySingularityStatus() throws Exception {
		EQCHive eqcHive = null;
		// @echo off
		// If exists old status need clear it
		for(int i=0; i<2; ++i) {
			try {
				Passport account = Util.DB().getPassport(new ID(i), Mode.GLOBAL);
				if(account != null) {
					Util.DB().deletePassport(new ID(i), Mode.GLOBAL);
				}
			}
			catch (Exception e) {
				Log.info("Try to remove old Account but it doesn't exists: " + e.getMessage());
			}
		}
		// Create PassportMerkleTree
		ChangeLog changeLog = new ChangeLog(ID.ZERO,
				new Filter(Mode.MINING));

		// Create EQC block
		eqcHive = new EQCHive();
		eqcHive.setChangeLog(changeLog);

		// Create TransactionsHeader
//		TransactionsHeader transactionsHeader = new TransactionsHeader();
//		transactionsHeader.setSignaturesHash(null);
//		eqcBlock.getTransactions().setTransactionsHeader(transactionsHeader);

//		// Create Transaction
//		ZeroZionCoinbaseTransaction zeroZionCoinbaseTransaction = new ZeroZionCoinbaseTransaction();
//
//		AITxOut txOut = new AITxOut();
//		txOut.getLock().setReadableLock(SINGULARITY_A);
//		Value minerCoinbaseReward = getCurrentMinerCoinbaseReward(getCurrentCoinbaseReward(changeLog));
//		txOut.setValue(getCurrentCoinbaseReward(changeLog).subtract(minerCoinbaseReward));
//		zeroZionCoinbaseTransaction.setEqCoinFederalTxOut(txOut);
//
//		txOut = new AITxOut();
//		txOut.getLock().setReadableLock(SINGULARITY_B);
//		txOut.setValue(minerCoinbaseReward);
//		zeroZionCoinbaseTransaction.setEqCoinMinerTxOut(txOut);
//		
//		eqcHive.getEQcoinSeed().addCoinbaseTransaction(zeroZionCoinbaseTransaction);
		
		eqcHive.plantingEQCHive();
		
		// Add new address in address list
//		if (!eqcBlock.getTransactions().isAddressExists(address.getAddress())) {
//			eqcBlock.getTransactions().getAddressList().addElement(address);
//		}

//		changeLog.buildProofBase();
//		changeLog.generateProofRoot();

//		// Create Index
//		Index index = new Index();
//		index.setTotalSupply(cypherTotalSupply(SerialNumber.ZERO));
//		index.setTotalAccountNumbers(BigInteger.TWO);
//		index.setTotalTransactionNumbers(BigInteger.ONE);
//		index.setAccountsMerkleTreeRootList(changeLog.getAccountsMerkleTreeRootList());
//		index.setTransactionsHash(eqcBlock.getTransactions().getHash());

		// Create Root
//		EQCRoot root = new EQCRoot();
//		root.setAccountsMerkelTreeRoot(changeLog.getPassportMerkleTreeRoot());
//		root.setSubchainsMerkelTreeRoot(eqcHive.geteQcoinSeed().getRoot());

		// Create EQC block header
		EQCHiveRoot header = new EQCHiveRoot();
		header.setPreHash(MAGIC_HASH);
		header.setTarget(Util.getDefaultTargetBytes());
		header.setHeight(ID.ZERO);
		header.setTimestamp(new ID(0));
		header.setNonce(ID.ZERO);
		header.setEQCoinSeedHash(eqcHive.getEQcoinSeed().getProofRoot());
		eqcHive.setEqcHeader(header);
		
		while(!header.isDifficultyValid()) {
			header.setNonce(header.getNonce().getNextID());
		}
		
//		eqcBlock.setIndex(index);
		changeLog.updateGlobalState();
		Log.info(eqcHive.toString());
		return eqcHive;
	}

	public static Value cypherTotalSupply(ChangeLog changeLog) throws Exception {
		if(changeLog.getHeight().equals(ID.ZERO)) {
			return new Value(BASIC_COINBASE_REWARD);
		}
		else {
			EQCHive eqcHive = DB().getEQCHive(changeLog.getHeight().getPreviousID(), true);
			Value totalSupply = eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getTotalSupply();
			if(totalSupply.compareTo(MAX_EQC) < 0) {
				return totalSupply.add(getCurrentCoinbaseReward(changeLog));
			}
			else {
				return totalSupply;
			}
		}
	}

	public static byte[] cypherTarget(ID height, ChangeLog changeLog) throws Exception {
		// Here need dore more job change Util.DB() to changelog
		byte[] target = null;
		BigInteger oldDifficulty;
		BigInteger newDifficulty;
		if (height.longValue() <= 9) {
			return getDefaultTargetBytes();
		}
		ID serialNumber_end = new ID(height.subtract(BigInteger.ONE));
		ID serialNumber_begin = new ID(height.subtract(BigInteger.valueOf(10)));
		if (height.longValue() % 10 != 0) {
//			Log.info(serialNumber_end.toString());
			target = Util.DB().getEQCHive(serialNumber_end, true).getEqcHeader().getTarget();//EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTarget();
//			Log.info(Util.bigIntegerTo128String(Util.targetBytesToBigInteger(target)));
		} else {
			Log.info(
					"Old target: "
							+ Util.bigIntegerTo512String(Util.targetBytesToBigInteger(
									Util.DB().getEQCHive(serialNumber_end, true).getEqcHeader().getTarget()))
							+ "\r\naverge time: "
							+ (Util.DB().getEQCHive(serialNumber_end, true).getEqcHeader().getTimestamp().longValue()
									- Util.DB().getEQCHive(serialNumber_begin, true).getEqcHeader().getTimestamp().longValue())
									/ 9);
			oldDifficulty = Util
					.targetBytesToBigInteger(Util.DB().getEQCHive(serialNumber_end, true).getEqcHeader().getTarget());
			EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) changeLog.getFilter().getPassport(ID.ZERO, false);
			BigInteger current_block_interval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval()).multiply(BigInteger.TEN);
			newDifficulty = oldDifficulty
					.multiply(BigInteger
							.valueOf((Util.DB().getEQCHive(serialNumber_end, true).getEqcHeader().getTimestamp().longValue()
									- Util.DB().getEQCHive(serialNumber_begin, true).getEqcHeader().getTimestamp().longValue())))
					.divide(BigInteger.valueOf(9).multiply(current_block_interval));
			// Compare if old difficulty divide new difficulty is bigger than MAX_DIFFICULTY_MULTIPLE
			if(oldDifficulty.divide(newDifficulty).compareTo(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE)) > 0) {
				Log.info("Due to old difficulty divide new difficulty(" + Util.bigIntegerTo512String(newDifficulty) +") = " + oldDifficulty.divide(newDifficulty).toString() + " is bigger than MAX_DIFFICULTY_MULTIPLE so here just divide MAX_DIFFICULTY_MULTIPLE");
				newDifficulty = oldDifficulty.divide(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE));
			}
			if (Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(newDifficulty) >= 0) {
				Log.info("New target: " + Util.bigIntegerTo512String(newDifficulty));
				target = Util.bigIntegerToTargetBytes(newDifficulty);
			} else {
				Log.info("New target: " + Util.bigIntegerTo512String(newDifficulty)
						+ " but due to it's bigger than the default target so still use default target.");
				target = Util.getDefaultTargetBytes();
			}
		}
		return target;
	}

	public static byte[] getMerkleTreeRoot(Vector<byte[]> bytes, boolean isHashing) throws NoSuchAlgorithmException {
		MerkleTree merkleTree = new MerkleTree(bytes, isHashing);
		merkleTree.generateRoot();
		return merkleTree.getRoot();
	}

	public static boolean isAddressFormatValid(String address) {
		Log.info("address' length: " + address.length());
		String mode = "^[1-3][1-9A-Za-z]{20,25}";
		Pattern pattern = Pattern.compile(mode);
		Matcher matcher = pattern.matcher(address);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isTXValueValid(String address) {
		Log.info("address' length: " + address.length());
		String mode = "^[0-9]+(.[0-9]{1,4})?$";// "^[1-9][0-9]*|[1-9][0-9]*\\.[0-9]{0,4}";
		Pattern pattern = Pattern.compile(mode);
		Matcher matcher = pattern.matcher(address);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public static String getIP() {
		InputStream ins = null;
		String ip = "";
		for(int i=0; i<3; ++i) {
			try {
				URL url = new URL("http://www.cip.cc/");
				URLConnection con = url.openConnection();
				ins = con.getInputStream();
				InputStreamReader isReader = new InputStreamReader(ins, "utf-8");
				BufferedReader bReader = new BufferedReader(isReader);
				StringBuffer webContent = new StringBuffer();
				String str = null;
				while ((str = bReader.readLine()) != null) {
					webContent.append(str);
				}
				int start = webContent.indexOf("IP	: ") + 5;
				int end = webContent.indexOf("地址	:");
				ip = webContent.substring(start, end);
				Log.info(ip);
				break;
			} catch (Exception e) {
				Log.Error(e.toString());
			} finally {
				if (ins != null) {
					try {
						ins.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.toString());
					}
				}
			}
		}
		return ip;
	}

	public static Cookie<O> getCookie() {
		return cookie;
	}

//	public static void updateCookie() {
//		cookie.setIp(getIP());
//	}

	public static Info<O> getDefaultInfo() {
		return info;
	}

	public static Info<O> getInfo(Code code, String message) {
		Info<O> info = new Info();
		info.setCookie(cookie);
		info.setCode(code);
		info.setMessage(message);
		return info;
	}

	public static long getNTPTIME() {
		NTPUDPClient timeClient = new NTPUDPClient();
		String timeServerUrl = "time.windows.com";
		InetAddress timeServerAddress;
		TimeStamp timeStamp = null;
		try {
			timeClient.setDefaultTimeout((int) DEFAULT_TIMEOUT);
			timeServerAddress = InetAddress.getByName(timeServerUrl);
			TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
			timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Log.info("Current time: " + dateFormat.format(timeStamp.getDate()));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return (timeStamp!=null)?timeStamp.getDate().getTime():0;
	}

	public static boolean isTimeCorrect() {
		boolean boolCorrect = true;
		if(Math.abs(System.currentTimeMillis() - getNTPTIME()) >= DEFAULT_TIMEOUT) {
			boolCorrect = false;
		}
		return boolCorrect;
	}
	
	public static boolean isNetworkAvailable() {
		boolean boolIsNetworkAvailable = false;
		InputStream ins = null;
		String ip = "";
		try {
			URL url = new URL("http://www.bing.com");
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			httpURLConnection.setConnectTimeout((int) DEFAULT_TIMEOUT);
			httpURLConnection.connect();
			if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				boolIsNetworkAvailable = true;
			}
			httpURLConnection.disconnect();
		} catch (Exception e) {
			Log.Error(e.toString());
		} 
		return boolIsNetworkAvailable;
	}
	
//	public static byte[] getBlockHeaderHash(Transaction transaction) throws Exception {
//		return EQCBlockChainH2.getInstance().getEQCHeaderHash(
//				EQCBlockChainH2.getInstance().getTxInHeight(transaction.getTxIn().getLock()));
//	}
	
	public static byte[] CRC32C(byte[] bytes) {
		CRC32C crc32c = new CRC32C();
		crc32c.update(bytes);
//		Log.info(dumpBytes(longToBytes(crc32c.getValue()), 16) + " Len: " + longToBytes(crc32c.getValue()).length);
//		Log.info(dumpBytes(intToBytes((int) crc32c.getValue()), 16) + " Len: " + intToBytes((int) crc32c.getValue()).length);
//		Log.info("" + crc32c.getValue());
		return intToBytes((int) (crc32c.getValue() & 0xFFFFFFFF));
	}
	
	public static EQCBlockChain DB(PERSISTENCE persistence) throws ClassNotFoundException, SQLException {
		EQCBlockChain eqcBlockChain = null;
		switch (persistence) {
		case H2:
			eqcBlockChain = EQCBlockChainH2.getInstance();
			break;
//		case ROCKSDB:
//		default:
//			eqcBlockChain = EQCBlockChainRocksDB.getInstance();
//			break;
		}
		return eqcBlockChain;
	}
	
	public static EQCBlockChain DB() throws ClassNotFoundException, SQLException {
		return DB(PERSISTENCE.H2);
	}
	
	public static BigInteger UnsignedBiginteger(BigInteger foo) {
		BigInteger value = foo;
		if(foo.signum() == -1) {
			value = new BigInteger(1, foo.toByteArray());
		}
		return value;
	}
	
	public static void cypherSingularityEQCBlockPreHash() throws NoSuchAlgorithmException {
		File file = new File(MAGIC_PATH);
		Vector<byte[]> vector = new Vector<>();
		MerkleTree merkleTree = null;
		for (File photo : file.listFiles()) {
			try {
				vector.add(new FileInputStream(photo).readAllBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Vector<byte[]> reverse = new Vector<>();
		for(int i=vector.size()-1; i>=0; --i) {
			reverse.add(vector.get(i));
		}
		merkleTree = new MerkleTree(reverse, true);
		merkleTree.generateRoot();
		Log.info("Root: " + dumpBytes(merkleTree.getRoot(), 16));
		Log.info("Magic: " + dumpBytes(EQCCHA_MULTIPLE_FIBONACCI_MERKEL(merkleTree.getRoot(), HUNDRED_THOUSAND), 16));
		Log.info(new BigInteger(1, EQCCHA_MULTIPLE_FIBONACCI_MERKEL(merkleTree.getRoot(), HUNDRED_THOUSAND)).toString());
	}

	public static ID fibonacci(long number) {
		ID a = ID.ONE, b = ID.ONE, c = ID.ZERO;
		if(number <= 2) {
			return ID.ONE;
		}
		for(int i=3; i<=number; ++i) {
			c = a.add(b);
			a = b;
			b = c;
		}
		return c;
	}

	public static String getSizeJson(int size) {
		return 
		"{\n" +
		"\"Size\":\"" + size + "\"" +
		"\n}";
	}
	
	@Deprecated
	public static void saveEQCBlockTailHeight(ID height) throws ClassNotFoundException, SQLException {
		EQCBlockChainH2.getInstance().saveEQCHiveTailHeight(height);
//		EQCBlockChainRocksDB.getInstance().saveEQCBlockTailHeight(height);
	}
	
	public static ID getMaxCoinbaseHeight(ID height){
		// Here can use new algorithm to calculate the max coinbase height just use the MaxSupport-TotalSupply
		// so the name need changed to is have unmining coin
		ID maxCoinbaseHeight = null;
//		if(PROTOCOL_VERSION.equals(DEFAULT_PROTOCOL_VERSION)) {
//			maxCoinbaseHeight = new ID(MAX_EQC / COINBASE_REWARD);
//		}
		return maxCoinbaseHeight;
	}
	
	public static Value getCurrentSupply() {
		Value currentSupply = Value.ZERO;
		
		return currentSupply;
	}
	
	public static boolean regex(String regex, String value) {
		if(value == null || value.equals("")) {
			return false;
		}
		return value.matches(regex);
	}
	
	public static void syncMinerList() throws ClassNotFoundException, SQLException, Exception {
		IPList<O> ipList = EQCBlockChainH2.getInstance().getMinerList();
		IPList<O> ipList2 = null;
		if (ipList.isEmpty()) {
			if (LOCAL_IP.equals(SINGULARITY_IP)) {
				return;
			}
			ipList = MinerNetworkClient.getMinerList(SINGULARITY_IP);
			if (ipList == null || ipList.isEmpty()) {
				return;
			} else {
				for (IP ip : ipList.getIpList()) {
					EQCBlockChainH2.getInstance().saveMiner(ip);
				}
			}
		}
		for (IP ip : ipList.getIpList()) {
			if(!LOCAL_IP.equals(ip)) {
				try {
					ipList2 = MinerNetworkClient.getMinerList(ip);
				} catch (Exception e) {
					Log.Error(e.getMessage());
				}
				if (ipList2 != null) {
					for (IP ip1 : ipList2.getIpList()) {
						EQCBlockChainH2.getInstance().saveMiner(ip1);
					}
				}
			}
		}
		ipList2 = EQCBlockChainH2.getInstance().getMinerList();
		for (IP ip : ipList2.getIpList()) {
			if (!LOCAL_IP.equals(ip)) {
				if (MinerNetworkClient.ping(ip) == -1) {
					updateDisconnectIPStatus(ip);
				}
			}
		}
	}
	
	public static void recoveryAccounts(ID height) throws ClassNotFoundException, SQLException, Exception {
		// Here exists one bug
//		// From height to checkpoint verify if Block is valid
//		EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
//		if(height.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) < 0) {
//			throw new IllegalStateException("Can't recovery to the height: " + height + " which below the check point: " + eQcoinSubchainAccount.getCheckPointHeight());
//		}
//		if(height.compareTo(Util.DB().getEQCBlockTailHeight()) > 0) {
//			throw new IllegalStateException("Can't recovery to the height: " + height + " which above current block's tail: " + Util.DB().getEQCBlockTailHeight());
//		}
//		long checkPointHeight = eQcoinSubchainAccount.getCheckPointHeight().longValue();
//		if(checkPointHeight == 0) {
//			checkPointHeight = 1;
//		}
//		long base = height.longValue();
//		boolean isSanity = false;
//		ChangeLog changeLog = null;
//		Log.info("Begin Recovery Account's status from height: " + height);
//		for (; base >= checkPointHeight; --base) {
//			Log.info("Try to recovery No. " + base + "'s Account status");
//			changeLog = new ChangeLog(new ID(base), new Filter(Mode.VALID));
//			if (Util.DB().getEQCHive(new ID(base), true).isValid(changeLog)) {
//				Log.info("No. " + base + " verify passed");
//				// Through merge recovery all relevant Account
//				changeLog.merge();
//				Log.info("Successful recovery No. " + base + " 's Account Status");
//				changeLog.clear();
//				isSanity = true;
//				break;
//			}
//			else {
//				Log.info("Try to recovery No. " + base + "'s Account status failed");
//			}
//		}
//
//		if (!isSanity) {
//			throw new IllegalStateException("Sanity test failed please check your computer");
//		}
//
//		// Due to base height's Account status saved in the Account table so here just deleteAccountSnapshotFrom base
//		EQCBlockChainH2.getInstance().deletePassportSnapshotFrom(new ID(base), true);
//		
//		// Delete extra Account
//		EQcoinSeedPassport eQcoinSubchainAccount2 = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
//		EQcoinSeedRoot eQcoinSeedRoot = Util.DB().getEQCHive(Util.DB().getEQCBlockTailHeight(), true).getEQcoinSeed().getEQcoinSeedRoot();
//		for (long i=eQcoinSubchainAccount2.getTotalPassportNumbers().getNextID().longValue(); i<=eQcoinSubchainAccount.getTotalPassportNumbers().longValue(); ++i) {
//			Util.DB().deletePassport(new ID(i), Mode.GLOBAL);
//		}
//		
//		// Delete extra EQCHive
//		for(long i=base; i<=Util.DB().getEQCBlockTailHeight().longValue(); ++i) {
//			Util.DB().deleteEQCHive(new ID(i));
//		}
//		
//		Util.DB().saveEQCBlockTailHeight(new ID(base));
//		Log.info("Recovery to new tail: " + base);
////		long i = base;
////		for (; i <= height.longValue(); ++i) {
////			changeLog = new AccountsMerkleTree(new ID(i), new Filter(Mode.VALID));
////			if (Util.DB().getEQCBlock(new ID(i), false).isValid(changeLog)) {
////				Log.info("No. " + base + " verify passed");
////				changeLog.takeSnapshot();
////				changeLog.merge();
////				changeLog.clear();
////				Util.DB().saveEQCBlockTailHeight(new ID(i));
////			} else {
////				break;
////			}
////		}
////		if (i < height.longValue()) {
////			for (i += 1; i <= height.longValue(); ++i) {
////				Log.info("Begin delete No. " + i + " Hive");
////				Util.DB().deleteEQCBlock(new ID(i));
////			}
////		}
	}
	
	public static void regenerateAccountStatus() throws ClassNotFoundException, SQLException, Exception {
		// If need here need do more job to support H2
//		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT));
//		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_AI));
//		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_HASH));
		Log.info("Delete all AccountSnapshot");
		EQCBlockChainH2.getInstance().deletePassportSnapshotFrom(ID.ZERO, true);
		recoverySingularityStatus();
		ID tail = Util.DB().getEQCHiveTailHeight();
		Log.info("Current have " + tail + " EQCHive");
		long base = 1;
		ChangeLog changeLog = null;
		EQCHive eqcHive = null;
		for(; base<=tail.longValue(); ++base) {
			eqcHive = Util.DB().getEQCHive(new ID(base), false);
			if(eqcHive != null) {
				changeLog = new ChangeLog(new ID(base), new Filter(Mode.VALID));
				if(eqcHive.isValid()) {
					changeLog.takeSnapshot();
					changeLog.merge();
					changeLog.clear();
					Log.info("No. " + base + " verify passed");
					Log.info("Current tail: " + base);
//					Util.DB().saveEQCBlockTailHeight(new ID(base));
				}
				else {
					Log.info("No. " + base + " verify failed");
					break;
				}
				eqcHive = null;
			}
			else {
				Log.info("No. " + base + "'s EQCHive is null just exit");
			}
		}
//		for(long i=base; i<=tail.longValue(); ++i) {
//			Util.DB().deleteEQCBlock(new ID(i));
//			Log.info("Successful delete extra EQCHive No. " + i);
//		}
		
	}
	
	public static void recoveryAccountsStatusTo(ID height) throws ClassNotFoundException, SQLException, Exception {
		// Here need use new method to recovery
		//		Log.info("Begin recoveryAccountsStatusTo " + height);
//		EQcoinSubchainHeader eQcoinSubchainHeader = Util.DB().getEQCHive(height, true).getEQcoinSubchain().getEQcoinSubchainHeader();
//		Passport account = null;
//		ID id = null;
//		for(long i=1; i<=eQcoinSubchainHeader.getTotalPassportNumbers().longValue(); ++i) {
//			id = new ID(i);
//			account = Util.DB().getPassport(id, Mode.GLOBAL);
//			Objects.requireNonNull(account);
//			if(account.getUpdateHeight().compareTo(height) > 0) {
//				Log.info("Accounts table's No. " + i + "'s update height:" + account.getUpdateHeight() + " bigger than new tail base:" + height + " recovery it's current height's history state from snapshot");
//				account = EQCBlockChainH2.getInstance().getPassportSnapshot(id, height);
//				Objects.requireNonNull(account);
//				Util.DB().savePassport(account, Mode.GLOBAL);
//			}
//		}
	}
	
	public static void updateDisconnectIPStatus(IP ip) {
		try {
			int counter = 0;
			if (Util.DB().isIPExists(ip, NODETYPE.NONE)) {
				counter = Util.DB().getIPCounter(ip) + 1;
				if (counter > Util.MAX_COUNTER) {
					Log.info(ip + "'s discount counter exceed 3 times just delete it");
					Util.DB().deleteMiner(ip);
				} else {
					Log.info(ip + "'s discount counter is " + counter + " just update it's disconect state");
					Util.DB().saveIPCounter(ip, counter);
				}
			}
			else {
				Log.info("Received RPC message from " + ip + " but can't access just discard it");
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
	}
	
	public static O bytes2O(byte[] bytes) {
		return new O(ByteBuffer.wrap(bytes));
	}

	public static Value getCurrentCoinbaseReward(ChangeLog changeLog) throws Exception {
		Value currentCoinbaseReward = Value.ZERO;
		BigInteger currentBlockInterval = BASIC_BLOCK_INTERVAL;
		if (changeLog.getHeight().compareTo(ID.ZERO) > 0) {
			EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) changeLog.getFilter().getPassport(ID.ZERO,
					false);
			currentBlockInterval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval())
					.multiply(TARGET_BLOCK_INTERVAL);
		}
		currentCoinbaseReward = new Value(
				BASIC_COINBASE_REWARD.multiply(currentBlockInterval).divide(BASIC_BLOCK_INTERVAL));
		return currentCoinbaseReward;
	}
	
	public static Value getCurrentMinerCoinbaseReward(Value currentCoinbaseReward) {
		return new Value(currentCoinbaseReward.multiply(BigInteger.valueOf(5)).divide(BigInteger.valueOf(100)));
	}
	
	public static Value getValue(double d) {
		long loneValue = (long) (d * ABC.longValue());
		return new Value(BigInteger.valueOf(loneValue));
	}
	
	public static BigInteger getCurrentBlockInterval() throws Exception {
		BigInteger currentBlockInterval = BigInteger.ZERO;
		EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) DB().getPassport(ID.ZERO, Mode.GLOBAL);
		currentBlockInterval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval()).multiply(TARGET_BLOCK_INTERVAL);
		return currentBlockInterval;
	}
	
}
