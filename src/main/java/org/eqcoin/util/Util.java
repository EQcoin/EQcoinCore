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
package org.eqcoin.util;

import java.io.BufferedReader;
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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32C;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.eqcoin.crypto.MerkleTree;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.persistence.mosaic.Mosaic;
import org.eqcoin.persistence.mosaic.h2.MosaicH2;
import org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.TransferCoinbaseTransaction;
import org.eqcoin.transaction.ZionCoinbaseTransaction;
import org.eqcoin.transaction.txout.TransferTxOut;
import org.eqcoin.transaction.txout.ZionTxOut;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Util {

	public enum OS {
		WINDOWS, MAC, LINUX
	}

	public enum PERSISTENCE {
		ROCKSDB, H2, RPC
	}

	public enum SP_MODE {
		EQCMINERNETWORK(1), EQCHIVESYNCNETWORK(2), EQCTRANSACTIONNETWORK(4);
		public final static SP_MODE get(final int flag) {
			SP_MODE sp_mode = null;
			switch (flag) {
			case 1:
				sp_mode = SP_MODE.EQCMINERNETWORK;
				break;
			case 2:
				sp_mode = SP_MODE.EQCHIVESYNCNETWORK;
				break;
			case 4:
				sp_mode = SP_MODE.EQCTRANSACTIONNETWORK;
				break;
			}
			return sp_mode;
		}

		public final static ID getFlag(final SP_MODE... modes) {
			int flag = 0;
			for (final SP_MODE sp_mode : modes) {
				flag += sp_mode.getFlag();
			}
			return new ID(flag);
		}

		private int flag;

		private SP_MODE(final int flag) {
			this.flag = flag;
		}

		public int getFlag() {
			return flag;
		}

	}

	/*
	 * Singularity - EQC's basic unit of measure. 1 EQC = 100000000 singularity
	 */
	public final static BigInteger ABC = BigInteger.valueOf(100000000);

	public final static BigInteger TARGET_INTERVAL = BigInteger.valueOf(51);

	public final static byte[] MAGIC_HASH = new BigInteger(
			"200189AC5AFA3CF07356C09C311B01619BC5513AF0792434F2F9CBB7E1473F39711981A4D8AB36CA2BEF35673EA7BF12F0673F6040659832E558FAEFBE4075E5",
			16).toByteArray();

	public final static byte[] SINGULARITY_HASH = {};

	public final static BigInteger MAX_EQC = BigInteger.valueOf(210000000000L).multiply(ABC);

	public final static Value MIN_BALANCE = new Value(BigInteger.valueOf(51).multiply(ABC));

	//	public final static long SINGULARITY_TOTAL_SUPPLY = 16800000 * ABC;

	//	public final static long MINER_TOTAL_SUPPLY = 42000000000L * ABC;
	//
	//	public final static long EQCOIN_FOUNDATION_TOTAL_SUPPLY = 168000000000L * ABC;

	public final static Value MAX_BALANCE = new Value(MAX_EQC.subtract(MIN_BALANCE.abs()));

	public final static Value MAX_VALUE = MAX_BALANCE.subtract(MIN_BALANCE);

	public final static ID MIN_ID = ID.ZERO;

	//	public final static long MINER_COINBASE_REWARD = 1 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);

	//	public final static long EQZIP_COINBASE_REWARD = 4 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);

	//	public final static long EQC_FEDERATION_COINBASE_REWARD = 19 * ABC * (BLOCK_INTERVAL / TARGET_INTERVAL);

	public final static ID MAX_PASSPORT_ID = new ID(MAX_EQC.divide(MIN_BALANCE));

	// Here exists one bug when change the block_interval the Max_coinbase_height
	// also changed need change it to determine according to if max supply - total
	// supply = 0
	//	public final static long MAX_COINBASE_HEIGHT = MAX_EQC / BASIC_COINBASE_REWARD.longValue();

	//	public final static int TXFEE_RATE = 10;

	public final static BigInteger TARGET_EQCHIVE_INTERVAL = BigInteger.valueOf(10000);

	public final static BigInteger BASIC_EQCHIVE_INTERVAL = TARGET_EQCHIVE_INTERVAL;// BigInteger.valueOf(600000);

	public final static BigInteger BASIC_COINBASE_REWARD = BigInteger.valueOf(1200).multiply(ABC);// MINER_COINBASE_REWARD
	// +
	// EQC_FEDERATION_COINBASE_REWARD;

	public final static byte DEFAULT_POWER_PRICE = 10;

	public final static int ZERO = 0;

	public final static int ONE = 1;

	public final static int TWO = 2;

	public final static int THREE = 3;

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

	public final static int MAX_NONCE = (int) Math.pow(2, 28);// 268435455;

	public final static int HASH_LEN = 64;

	//	public final static String WINDOWS_PATH = "C:/EQCOIN";
	//
	//	public final static String MAC_PATH = "C:/Program Files/EQCOIN";
	//
	//	public final static String LINUX_PATH = "C:/Program Files/EQCOIN";
	/*
	 * Set the default PATH value WINDOWS_PATH
	 */
	public final static String CURRENT_PATH = System.getProperty("user.dir");

	public final static String WINDOWS = "C:";

	public final static String LINUX = "/usr";

	/**
	 * In Windows due to haven't the permission to access the Program File folder so
	 * have to save it to C but in Linux can access the CURRENT_PATH
	 */
	public final static String PATH = CURRENT_PATH + File.separator + "EQcoin";// System.getProperty("user.dir") +
	// File.separator +
	// "EQCOIN";
	//	static {
	//		PATH = System.getProperty("user.dir") + "/EQCOIN";
	//	}

	public final static String MAGIC_PATH = ".\\src\\main\\QuidditchHelixFlashForward";

	public final static String KEYSTORE_PATH = PATH + File.separator + "EQcoin.keystore";

	public final static String KEYSTORE_PATH_BAK = PATH + File.separator + "EQcoin.keystore.bak";

	public final static String LOG_PATH = PATH + File.separator + "log.%u.%g.txt";

	public final static String AVRO_PATH = PATH + File.separator + "AVRO";

	public final static String EQC_SUFFIX = ".EQC";

	public final static String DB_PATH = PATH + File.separator + "DB";

	public final static String HIVE_PATH = DB_PATH + File.separator + "HIVE/";

	public final static String H2_PATH = DB_PATH + File.separator + "H2";

	public final static String ROCKSDB_PATH = DB_PATH + File.separator + "ROCKSDB";

	public final static String GLOBAL_STATE_DATABASE_NAME = H2_PATH + File.separator + "GlobalState";

	public final static String TRANSACTION_POOL_DATABASE_NAME = H2_PATH + File.separator + "Mosaic";

	public final static String WALLET_DATABASE_NAME = H2_PATH + File.separator + "Wallet";

	/**
	 * Compressed publickey and ASN.1 DER signature's length specification ECC curve
	 * compressed publickey length(bytes) signature length(bytes) P256 33 70、71、72
	 * P521 67 137、138、139
	 */
	public final static int P256_PUBLICKEY_LEN = 33;

	public final static int P521_PUBLICKEY_LEN = 67;

	public final static BigInteger P256_POINT_LEN = BigInteger.valueOf(32);

	public final static BigInteger P521_POINT_LEN = BigInteger.valueOf(66);

	public final static BigInteger P256_SIGNATURE_LEN = BigInteger.valueOf(64);

	public final static BigInteger P521_SIGNATURE_LEN = BigInteger.valueOf(132);

	public final static int MAX_SERIAL_NUMBER_LEN = 5;

	public final static BigInteger MAX_TXFEE_LEN = BigInteger.valueOf(6);

	public final static int BASIC_VALUE_NUMBER_LEN = 8;

	public final static int INIT_ADDRESS_SERIAL_NUMBER = 0;

	public final static ID DEFAULT_PROTOCOL_VERSION = ID.ZERO;

	public final static ID PROTOCOL_VERSION = DEFAULT_PROTOCOL_VERSION;

	public static Info info = null;

	public final static long DEFAULT_TIMEOUT = 3000;

	public final static int MAX_ADDRESS_LEN = 51;

	public final static int MIN_ADDRESS_LEN = 41;

	public final static int MAX_ADDRESS_AI_LEN = 33;

	public final static int MAX_T3_ADDRESS_CODE_LEN = 213;

	public final static int CRC32C_LEN = 4;

	public final static int MAX_DIFFICULTY_MULTIPLE = 4;

	public final static BigInteger EUROPA = BigInteger.valueOf(1008);

	// Here exists one bug need change null hash to SHA3-512(EQCType.NULL_ARRAY)
	public final static byte[] NULL_HASH = Arrays.copyOfRange(new BigInteger(
			"C333A8150751C675CDE1312860731E54818F95EDC1563839501CE5F486DE1C79EA6675EECA26833E41341B5B5D1E72800CBBB13AE6AA289D11ACB4D4413B1B2D",
			16).toByteArray(), 1, 65);

	public final static byte[] SINGULARITY = EQCCastle.NULL_ARRAY;

	public final static String REGEX_IP = "";

	public final static String REGEX_VERSION = "";

	public final static SP SINGULARITY_SP = new SP().setProtocolVersion(PROTOCOL_VERSION)
			.setFlag(
					SP_MODE.getFlag(SP_MODE.EQCHIVESYNCNETWORK, SP_MODE.EQCMINERNETWORK, SP_MODE.EQCTRANSACTIONNETWORK))
			.setIp("129.28.206.27");// "129.28.206.27";192.168.154.128

	public final static SP LOCAL_SP = new SP().setProtocolVersion(PROTOCOL_VERSION)
			.setFlag(
					SP_MODE.getFlag(SP_MODE.EQCHIVESYNCNETWORK, SP_MODE.EQCMINERNETWORK, SP_MODE.EQCTRANSACTIONNETWORK))
			.setIp("192.168.43.80");// "14.221.176.18";//"14.221.177.212";//"192.168.0.101";//"14.221.177.223";//"129.28.206.27";

	public final static int MINER_NETWORK_PORT = 7799;

	public final static int SYNCBLOCK_NETWORK_PORT = 7997;

	public final static int TRANSACTION_NETWORK_PORT = 9977;

	public final static String SINGULARITY_A = "2Tjo9gTXTKnEeiCUVsyMjBgdTEYD2TuuwsNUfKwrx8nzifpypjba5NtWPwnx2boM62rjLRQMEhxGxzZtdCWrPjojSoQq96";

	public final static String SINGULARITY_B = "2a9zXF4zdfoevE2MA6RYf86M9gXqSLvPB2fN44cqhrAMM8NuUZUpF1YMBbbNxcGdGYaUQQsr9TQdtbb5RsPYmKWz7RryFb";

	public final static String SINGULARITY_C = "2KEA1vfP9v31Jhx2rz7MMvZRixGv5jxTmjtygZa2mKroBQAQAgWJfXj4dcLU9rTeF6vSxsb3woK7FrPy3dnitFcsr8fnag";

	public final static byte MAX_COUNTER = 3;

	public final static String SHA3_512 = "SHA3-512";
	public final static String SHA3_256 = "SHA3-256";

	public final static int SHA3_256_LEN = 32;

	public final static int SHA3_512_LEN = 64;

	// 2020-04-1 Here need do more job to calculate the detailed value
	public final static BigInteger ASSET_PASSPORT_PROOF_SPACE_COST = BigInteger.valueOf(51);

	public final static BigInteger T1_LOCK_PROOF_SPACE_COST = BigInteger.valueOf(50);

	public final static BigInteger T2_LOCK_PROOF_SPACE_COST = BigInteger.valueOf(82);

	public static boolean IsDeleteTransactionInPool = false;

	public final static int PROOF_SIZE = 4;

	public final static byte BIT_0 = 1;

	public final static byte BIT_1 = 2;

	public final static byte BIT_2 = 4;

	public final static byte BIT_3 = 8;

	public final static byte BIT_4 = 16;


	//	static {
	// 2020-04-19 If use this way initialize will break log output into console
	// still don't konw how to slove it
	//		SINGULARITY_A = (Keystore.getInstance().getUserProfiles() == null)?null:Keystore.getInstance().getUserProfiles().get(0).getReadableLock();
	//
	//		SINGULARITY_B = (Keystore.getInstance().getUserProfiles() == null)?null:Keystore.getInstance().getUserProfiles().get(1).getReadableLock();
	//
	//		SINGULARITY_C = (Keystore.getInstance().getUserProfiles() == null)?null:Keystore.getInstance().getUserProfiles().get(2).getReadableLock();
	//	}

	//	public final static ID [] FIBONACCI = {
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

	public final static byte BIT_5 = 32;

	public final static byte BIT_6 = 64;

	public final static byte BIT_7 = (byte) 128;

	public final static ID[] FIBONACCI = { new ID(1597), // 17
			new ID(5702887), // 34
			new ID(new BigInteger("1134903170")), // 45
			new ID(new BigInteger("1548008755920")), // 60
			new ID(new BigInteger("5527939700884757")), // 77
			new ID(new BigInteger("1779979416004714189")), // 89
			new ID(new BigInteger("1500520536206896083277")), // 103
			new ID(new BigInteger("3311648143516982017180081")), // 119
			new ID(new BigInteger("1066340417491710595814572169")), // 131
			new ID(new BigInteger("3807901929474025356630904134051")), // 148
			new ID(new BigInteger(
					"1206484255615496768210420703829205488386909032955899056732883572731058504300529011053")) // 404
	};

	public final static ID[] PRIME101 = { new ID(2), new ID(3), new ID(5), new ID(7), new ID(11), new ID(13),
			new ID(17), new ID(19), new ID(23), new ID(29), new ID(31), new ID(37), new ID(41), new ID(43), new ID(47),
			new ID(53), new ID(59), new ID(61), new ID(67), new ID(71), new ID(73), new ID(79), new ID(83), new ID(89),
			new ID(97), new ID(101), new ID(103), new ID(107), new ID(109), new ID(113), new ID(127), new ID(131),
			new ID(137), new ID(139), new ID(149), new ID(151), new ID(157), new ID(163), new ID(167), new ID(173),
			new ID(179), new ID(181), new ID(191), new ID(193), new ID(197), new ID(199), new ID(211), new ID(223),
			new ID(227), new ID(229), new ID(233), new ID(239), new ID(241), new ID(251), new ID(257), new ID(263),
			new ID(269), new ID(271), new ID(277), new ID(281), new ID(283), new ID(293), new ID(307), new ID(311),
			new ID(313), new ID(317), new ID(331), new ID(337), new ID(347), new ID(349), new ID(353), new ID(359),
			new ID(367), new ID(373), new ID(379), new ID(383), new ID(389), new ID(397), new ID(401), new ID(409),
			new ID(419), new ID(421), new ID(431), new ID(433), new ID(439), new ID(443), new ID(449), new ID(457),
			new ID(461), new ID(463), new ID(467), new ID(479), new ID(487), new ID(491), new ID(499), new ID(503),
			new ID(509), new ID(521), new ID(523), new ID(541), new ID(547) };

	public final static byte[] AESDecrypt(final byte[] bytes, final String password) throws Exception {
		byte[] result = null;
		KeyGenerator kgen;
		kgen = KeyGenerator.getInstance("AES");
		final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(password.getBytes());
		kgen.init(256, secureRandom);
		final SecretKey secretKey = kgen.generateKey();
		final byte[] enCodeFormat = secretKey.getEncoded();
		final SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
		final Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		result = cipher.doFinal(bytes);
		return result;
	}

	public final static byte[] AESEncrypt(final byte[] bytes, final String password) throws Exception {
		byte[] result = null;
		KeyGenerator kgen;
		kgen = KeyGenerator.getInstance("AES");
		// Can't use SecureRandom.getInstanceStrong() otherwise can't AESDecrypt because
		// of
		// javax.crypto.BadPaddingException: Given final block not properly padded. Such
		// issues can arise if a bad key is used during decryption.
		final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
		secureRandom.setSeed(password.getBytes());
		kgen.init(256, secureRandom);
		final SecretKey secretKey = kgen.generateKey();
		final byte[] enCodeFormat = secretKey.getEncoded();
		final SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
		final Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		result = cipher.doFinal(bytes);
		return result;
	}

	//	void init(final OS os) {
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

	//	public final static byte[] multipleExtendMix(final byte[] data, final int multiple) {
	//		byte[] result = null;
	//
	//		BigDecimal begin = new BigDecimal(new BigInteger(1, data));
	//		MathContext mc = new MathContext(Util.HUNDREDPULS, RoundingMode.HALF_EVEN);
	//		BigDecimal a = null, b = null, c = null, d = null;
	//		int bufferLen = ((data.length + 1) * 2 + 417) * multiple;
	//		ByteBuffer byteBuffer = ByteBuffer.allocate(bufferLen);
	//		// Put the original raw data
	//		byteBuffer.put(data);
	//		byteBuffer.put(SINGULARITY);
	//		// Put the multiple extended data
	//		BigInteger random;
	//		byte[] randomBytes, partOfRandomBytes;
	//		for (int i = 1; i <= multiple; ++i) {
	////			Log.info("Begin: " + begin.toPlainString());
	//			random = begin.toBigInteger();
	//			randomBytes = random.toByteArray();
	//			partOfRandomBytes = new byte[(random.toByteArray().length / 2)];
	//			if (random.mod(ID.TWO).equals(ID.ZERO)) {
	//				for (int j = 0; j < partOfRandomBytes.length; ++j) {
	//					partOfRandomBytes[j] = randomBytes[2 * j];
	//				}
	//			} else {
	//				for (int j = 0; j < partOfRandomBytes.length; ++j) {
	//					partOfRandomBytes[j] = randomBytes[2 * j + 1];
	//				}
	//			}
	//			a = begin.divide(new BigDecimal(new BigInteger(1, partOfRandomBytes)), mc);
	//			b = a.divide(new BigDecimal(FIBONACCI[2]), mc);
	//			c = a.divide(new BigDecimal(FIBONACCI[10]), mc);
	//			d = b.subtract(c).abs().multiply(new BigDecimal(PRIME101[(multiple - i) % HUNDREDPULS]), mc);
	//
	//			begin = begin.add(a).add(b).add(c).add(d);
	////			Log.info("i: " + i + " " + begin.toPlainString());
	//			String[] abc = begin.toPlainString().split("\\.");
	//			if (abc.length == 2) {
	////				Log.info("...");
	//				BigInteger e = new BigInteger(abc[0]);
	//				BigInteger f = new BigInteger(abc[1]);
	//				byteBuffer.put(e.toByteArray());
	//				byteBuffer.put(SINGULARITY);
	////				Log.info("F: " + f.toByteArray().length);
	//				byteBuffer.put(f.toByteArray());
	//				if (i < multiple) {
	//					byteBuffer.put(SINGULARITY);
	//				}
	//			} else {
	//				BigInteger e = new BigInteger(abc[0]);
	//				byteBuffer.put(e.toByteArray());
	//				if (i < multiple) {
	//					byteBuffer.put(SINGULARITY);
	//				}
	//			}
	//		}
	//		byteBuffer.flip();
	//		if (byteBuffer.remaining() == bufferLen) {
	//			result = byteBuffer.array();
	//		} else {
	//			result = new byte[byteBuffer.remaining()];
	//			byteBuffer.get(result);
	//		}
	//		return result;
	//	}

	public final static byte[] bigInteger2Base255(BigInteger foo) {
		final BigInteger BASE255 = BigInteger.valueOf(128);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		BigInteger remainder = null;
		byte[] bytes = null;
		while (foo.compareTo(BASE255) >= 0) {
			remainder = foo.mod(BASE255);
			bytes = remainder.toByteArray();
			os.write(bytes.length == 1?bytes[0]:bytes[1]);
			foo = foo.subtract(remainder).divide(BASE255);
		}
		bytes = foo.toByteArray();
		os.write(bytes.length == 1?bytes[0]:bytes[1]);
		return os.toByteArray();
	}

	public final static String bigIntegerTo512HexString(final BigInteger foo) {
		return bigIntegerToFixedLengthHexString(foo, 512).toUpperCase();
	}

	public final static String bigIntegerToFixedLengthHexString(final BigInteger foo, final int bitLength) {
		final String tmp = foo.toString(16);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bitLength / 4 - tmp.length(); ++i) {
			sb.append("0");
		}
		sb.append(tmp);
		return sb.toString();
	}

	public final static byte[] bigIntegerToTargetBytes(final BigInteger foo) {
		EQCCastle.assertNotNegative(foo);
		final byte[] bytes = foo.toByteArray();
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
		return longToBytes((bytesToLong(target) >>> 9) | ((offset & 0x1FF) << 23));
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

	/**
	 * Add leading zero when the original binary string's length is less than 8.
	 * <p>
	 * For example when foo is 101111 the output is 00101111.
	 *
	 * @param foo This value is a string of ASCII digitsin binary (base 2) with no
	 *            extra leading 0s.
	 * @return Fixed 8-bit long binary number with leading 0s.
	 */
	public final static String binaryString(final String foo) {
		if (foo.length() == 8) {
			return foo;
		} else {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 8 - foo.length(); ++i) {
				sb.append(0);
			}
			sb.append(foo);
			return sb.toString();
		}
	}

	public final static String bytesTo512HexString(final byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		return bigIntegerTo512HexString(new BigInteger(1, bytes));
	}

	public final static String bytesToHexString(final byte[] bytes) {
		return dumpBytes(bytes, 16);
	}

	public final static long bytesToLong(final byte[] bytes) {
		return new BigInteger(1, bytes).longValue();
	}

	public final static byte[] CRC32C(final byte[] bytes) {
		final CRC32C crc32c = new CRC32C();
		crc32c.update(bytes);
		//		Log.info(dumpBytes(longToBytes(crc32c.getValue()), 16) + " Len: " + longToBytes(crc32c.getValue()).length);
		//		Log.info(dumpBytes(intToBytes((int) crc32c.getValue()), 16) + " Len: " + intToBytes((int) crc32c.getValue()).length);
		//		Log.info("" + crc32c.getValue());
		// longToBytes exists bugs need use new method to generate fixed length bytes
		return longToBytes(crc32c.getValue());
	}

	public final static boolean createDir(final String dir) {
		boolean boolIsSuccessful = true;
		final File file = new File(dir);
		if (!file.isDirectory()) {
			boolIsSuccessful = file.mkdir();
			Log.info("Create directory " + dir + " " + boolIsSuccessful);
		} else {
			if (file.isDirectory()) {
				Log.info(dir + " already exists.");
			} else {
				Log.Error("Create directory " + dir + " failed and this directory doesn't exists.");
			}
		}
		return boolIsSuccessful;
	}

	public final static void cypherSingularityEQCHivePreProof() throws NoSuchAlgorithmException, IOException {
		final File file = new File(MAGIC_PATH);
		final Vector<byte[]> vector = new Vector<>();
		MerkleTree merkleTree = null;
		for (final File photo : file.listFiles()) {
			try {
				vector.add(new FileInputStream(photo).readAllBytes());
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		final Vector<byte[]> reverse = new Vector<>();
		for (int i = vector.size() - 1; i >= 0; --i) {
			reverse.add(vector.get(i));
		}
		merkleTree = new MerkleTree(reverse, true);
		merkleTree.generateRoot();
		Log.info("Root: " + dumpBytes(merkleTree.getRoot(), 16));
		Log.info("Magic: " + dumpBytes(multipleExtendMix(merkleTree.getRoot(), HUNDREDPULS), 16));
		Log.info(new BigInteger(1, multipleExtendMix(merkleTree.getRoot(), HUNDREDPULS)).toString());
	}

	public final static byte[] cypherTarget(final GlobalState globalState) throws Exception {
		// Here need dore more job change Util.DB() to changelog
		byte[] target = null;
		BigInteger oldDifficulty;
		BigInteger newDifficulty;
		final ID currentHeight = globalState.getEQCHiveTailHeight().getNextID();
		if (currentHeight.compareTo(TARGET_INTERVAL) < 0) {
			return getDefaultTargetBytes();
		}
		final ID interval_end = currentHeight.subtract(BigInteger.ONE);
		final ID interval_begin = currentHeight.subtract(TARGET_INTERVAL);
		if (!currentHeight.mod(TARGET_INTERVAL).equals(ID.ZERO)) {
			//			Log.info(serialNumber_end.toString());
			target = globalState.getEQCHiveRoot(interval_end).getTarget();// EQCBlockChainH2.getInstance().getEQCHeader(serialNumber_end).getTarget();
			//			Log.info(Util.bigIntegerTo128String(Util.targetBytesToBigInteger(target)));
		} else {
			Log.info("Old target: "
					+ Util.bigIntegerTo512HexString(
							Util.targetBytesToBigInteger(globalState.getEQCHiveRoot(interval_end).getTarget()))
					+ "\r\naverge time: " + (globalState.getEQCHiveRoot(interval_end).getTimestamp().longValue()
							- globalState.getEQCHiveRoot(interval_begin).getTimestamp().longValue()) / TARGET_INTERVAL.longValue());
			oldDifficulty = Util.targetBytesToBigInteger(globalState.getEQCHiveRoot(interval_end).getTarget());
			final EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
			final BigInteger current_block_interval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval())
					.multiply(TARGET_EQCHIVE_INTERVAL);
			newDifficulty = oldDifficulty
					.multiply(BigInteger.valueOf((globalState.getEQCHiveRoot(interval_end).getTimestamp().longValue()
							- globalState.getEQCHiveRoot(interval_begin).getTimestamp().longValue())))
					.divide(TARGET_INTERVAL.multiply(current_block_interval));
			// Compare if old difficulty divide new difficulty is bigger than
			// MAX_DIFFICULTY_MULTIPLE
			if (oldDifficulty.divide(newDifficulty).compareTo(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE)) > 0) {
				Log.info("Due to old difficulty divide new difficulty(" + Util.bigIntegerTo512HexString(newDifficulty)
				+ ") = " + oldDifficulty.divide(newDifficulty).toString()
				+ " is bigger than MAX_DIFFICULTY_MULTIPLE so here just divide MAX_DIFFICULTY_MULTIPLE");
				newDifficulty = oldDifficulty.divide(BigInteger.valueOf(MAX_DIFFICULTY_MULTIPLE));
			}
			if (Util.targetBytesToBigInteger(Util.getDefaultTargetBytes()).compareTo(newDifficulty) >= 0) {
				Log.info("New target: " + Util.bigIntegerTo512HexString(newDifficulty));
				target = Util.bigIntegerToTargetBytes(newDifficulty);
			} else {
				Log.info("New target: " + Util.bigIntegerTo512HexString(newDifficulty)
				+ " but due to it's less than the default target so still use default target.");
				target = Util.getDefaultTargetBytes();
			}
		}
		return target;
	}

	public final static Value cypherTotalSupply(final EQCHive eqcHive) throws Exception {
		if (eqcHive.getRoot().getHeight().equals(ID.ZERO)) {
			return new Value(BASIC_COINBASE_REWARD);
		} else {
			final EQCHiveRoot eqcHiveRoot = eqcHive.getGlobalState().getEQCHiveRoot(eqcHive.getRoot().getHeight().getPreviousID());
			final Value preTotalSupply = eqcHiveRoot.getTotalSupply();
			if (preTotalSupply.compareTo(MAX_EQC) < 0) {
				final Value totalSupply = preTotalSupply.add(getCurrentCoinbaseReward(eqcHive.getGlobalState()));
				if(totalSupply.compareTo(MAX_EQC) > 0) {
					new IllegalStateException("Current total supply: " + totalSupply + " exceed MAX_EQC: " + MAX_EQC + " this is invalid");
				}
				return totalSupply;
			} else {
				if(!preTotalSupply.equals(MAX_EQC)) {
					new IllegalStateException("Current total supply: " + preTotalSupply + " doesn't equals to MAX_EQC: " + MAX_EQC + " this is invalid");
				}
				return preTotalSupply;
			}
		}
	}

	public final static String dumpBytes(final byte[] bytes, final int radix) {
		if (bytes == null) {
			return null;
		}
		return new BigInteger(1, bytes).toString(radix).toUpperCase();
	}

	public final static String dumpBytesBigEndianBinary(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(binaryString(Integer.toBinaryString(bytes[i] & 0xFF)));
		}
		return sb.toString();
	}

	public final static String dumpBytesBigEndianHex(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (int i = bytes.length - 1; i >= 0; --i) {
			//			if (i % 8 == 0) {
			//				sb.append(" ");
			//			}
			sb.append(Integer.toHexString(bytes[i]).toUpperCase());
		}
		return sb.toString();
	}

	public final static String dumpBytesLittleEndianBinary(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" ");
			}
			sb.append(Integer.toBinaryString(bytes[i] & 0xFF));
		}
		return sb.toString();
	}

	public final static String dumpBytesLittleEndianHex(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		String hex = null;
		int j = 0;
		for (int i = 0; i < bytes.length; ++i) {
			if (i % 8 == 0) {
				sb.append(" " + j++ + ":");
			}
			hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public final static ID fibonacci(final long number) {
		ID a = ID.ONE, b = ID.ONE, c = ID.ZERO;
		if (number <= 2) {
			return ID.ONE;
		}
		for (int i = 3; i <= number; ++i) {
			c = a.add(b);
			a = b;
			b = c;
		}
		return c;
	}

	public final static byte[] flipBytes(final byte[] foo) {
		final byte[] flip = new byte[foo.length];
		for(int i=0; i<foo.length; ++i) {
			flip[i] = (byte) ~foo[i];
		}
		return flip;
	}

	public final static TransferCoinbaseTransaction generateTransferCoinbaseTransaction(final ID minerPassportId,
			final GlobalState globalState) throws Exception {
		final TransferCoinbaseTransaction transaction = new TransferCoinbaseTransaction();
		final TransferTxOut eqcFederalTxOut = new TransferTxOut();
		final TransferTxOut minerTxOut = new TransferTxOut();
		eqcFederalTxOut.setPassportId(ID.ZERO);
		minerTxOut.setPassportId(minerPassportId);

		final Value minerCoinbaseReward = getCurrentMinerCoinbaseReward(getCurrentCoinbaseReward(globalState));
		eqcFederalTxOut.setValue(getCurrentCoinbaseReward(globalState).subtract(minerCoinbaseReward));
		minerTxOut.setValue(minerCoinbaseReward);

		transaction.setEqCoinFederalTxOut(eqcFederalTxOut);
		transaction.setEqCoinMinerTxOut(minerTxOut);
		transaction.setNonce(globalState.getEQCHiveTailHeight().getNextID().getNextID());
		return transaction;
	}

	public final static ZionCoinbaseTransaction generateZionCoinbaseTransaction(final Lock minerLock, final GlobalState globalState)
			throws Exception {
		final ZionCoinbaseTransaction transaction = new ZionCoinbaseTransaction();
		final TransferTxOut eqcFederalTxOut = new TransferTxOut();
		final ZionTxOut minerTxOut = new ZionTxOut();
		eqcFederalTxOut.setPassportId(ID.ZERO);
		minerTxOut.setLock(minerLock);

		final Value minerCoinbaseReward = getCurrentMinerCoinbaseReward(getCurrentCoinbaseReward(globalState));
		eqcFederalTxOut.setValue(getCurrentCoinbaseReward(globalState).subtract(minerCoinbaseReward));
		minerTxOut.setValue(minerCoinbaseReward);

		transaction.setEqCoinFederalTxOut(eqcFederalTxOut);
		transaction.setEqCoinMinerTxOut(minerTxOut);
		transaction.setNonce(globalState.getEQCHiveTailHeight().getNextID().getNextID());
		return transaction;
	}

	public final static Value getCurrentCoinbaseReward(final GlobalState globalState) throws Exception {
		Value currentCoinbaseReward = null;
		EQcoinRootPassport eQcoinRootPassport = null;
		BigInteger currentBlockInterval = BASIC_EQCHIVE_INTERVAL;
		eQcoinRootPassport = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
		if (eQcoinRootPassport != null) {
			currentBlockInterval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval())
					.multiply(TARGET_EQCHIVE_INTERVAL);
		}
		currentCoinbaseReward = new Value(
				BASIC_COINBASE_REWARD.multiply(currentBlockInterval).divide(BASIC_EQCHIVE_INTERVAL));
		return currentCoinbaseReward;
	}

	public final static BigInteger getCurrentEQCHiveInterval(final GlobalState globalState) throws Exception {
		BigInteger currentEQCHiveInterval = BigInteger.ZERO;
		final EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
		currentEQCHiveInterval = BigInteger.valueOf(eQcoinRootPassport.getBlockInterval())
				.multiply(TARGET_EQCHIVE_INTERVAL);
		return currentEQCHiveInterval;
	}

	public final static Value getCurrentMinerCoinbaseReward(final Value currentCoinbaseReward) {
		return new Value(currentCoinbaseReward.multiply(BigInteger.valueOf(5)).divide(BigInteger.valueOf(100)));
	}

	public final static Info getDefaultInfo() {
		return info;
	}

	public final static byte[] getDefaultTargetBytes() {
		return new byte[] { (byte) 0xF4, (byte) 0x4F, (byte) 0xAB, (byte) 0xCD };
	}

	public final static String getGMTTime(final long timestamp) {
		final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.format(timestamp);
	}

	public final static Info getInfo(final Code code, final String message) {
		final Info info = new Info();
		info.setSp(LOCAL_SP);
		info.setCode(code);
		info.setMessage(message);
		return info;
	}

	public final static String getIP() {
		InputStream ins = null;
		String ip = "";
		for (int i = 0; i < 3; ++i) {
			try {
				final URL url = new URL("http://www.cip.cc/");
				final URLConnection con = url.openConnection();
				ins = con.getInputStream();
				final InputStreamReader isReader = new InputStreamReader(ins, "utf-8");
				final BufferedReader bReader = new BufferedReader(isReader);
				final StringBuffer webContent = new StringBuffer();
				String str = null;
				while ((str = bReader.readLine()) != null) {
					webContent.append(str);
				}
				final int start = webContent.indexOf("IP	: ") + 5;
				final int end = webContent.indexOf("地址	:");
				ip = webContent.substring(start, end);
				Log.info(ip);
				break;
			} catch (final Exception e) {
				Log.Error(e.toString());
			} finally {
				if (ins != null) {
					try {
						ins.close();
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.toString());
					}
				}
			}
		}
		return ip;
	}

	public final static byte[] getMerkleTreeRoot(final Vector<byte[]> bytes, final boolean isHashing)
			throws NoSuchAlgorithmException {
		final MerkleTree merkleTree = new MerkleTree(bytes, isHashing);
		merkleTree.generateRoot();
		return merkleTree.getRoot();
	}

	public final static long getNTPTIME() throws Exception {
		final NTPUDPClient timeClient = new NTPUDPClient();
		final String timeServerUrl = "time.windows.com";
		InetAddress timeServerAddress;
		TimeStamp timeStamp = null;
		timeClient.setDefaultTimeout((int) DEFAULT_TIMEOUT);
		timeServerAddress = InetAddress.getByName(timeServerUrl);
		final TimeInfo timeInfo = timeClient.getTime(timeServerAddress);
		timeStamp = timeInfo.getMessage().getTransmitTimeStamp();
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Log.info("Current time: " + dateFormat.format(timeStamp.getDate()));
		return ((timeStamp != null) ? timeStamp.getDate().getTime() : 0);
	}

	public final static ECPrivateKey getPrivateKey(final byte[] privateKeyBytes, final LockType lockType) throws Exception {
		ECPrivateKey privateKey = null;
		final AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
		if (lockType == LockType.T1) {
			parameters.init(new ECGenParameterSpec(Keystore.SECP256R1));
		} else if (lockType == LockType.T2) {
			parameters.init(new ECGenParameterSpec(Keystore.SECP521R1));
		}
		final ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
		final ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(privateKeyBytes), ecParameterSpec);
		privateKey = (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(ecPrivateKeySpec);
		return privateKey;
	}

	public final static byte[] getSecureRandomBytes() throws NoSuchAlgorithmException {
		final byte[] bytes = new byte[64];
		SecureRandom.getInstanceStrong().nextBytes(bytes);
		return bytes;
	}

	public final static Value getValue(final double d) {
		final long loneValue = (long) (d * ABC.longValue());
		return new Value(BigInteger.valueOf(loneValue));
	}

	/**
	 * GlobalState readable helper for read only access to the global state.
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public final static GlobalState GS() throws ClassNotFoundException, SQLException {
		return GS(PERSISTENCE.H2);
	}

	public final static GlobalState GS(final PERSISTENCE persistence) throws ClassNotFoundException, SQLException {
		GlobalState globalState = null;
		switch (persistence) {
		case H2:
			globalState = GlobalStateH2.getInstance();
			break;
		}
		return globalState;
	}

	public final static void init() throws Exception {
		System.setProperty("sun.net.client.defaultConnectTimeout", "3000");
		System.err.close();
		System.setErr(System.out);
		createDir(PATH);
		createDir(DB_PATH);
		createDir(HIVE_PATH);
		createDir(H2_PATH);
		recoverySingularityStatus();
		//		Util.IP = getIP();
		info = new Info();
		info.setCode(Code.OK);
		info.setSp(LOCAL_SP);
	}

	@Deprecated // should move to LockTool
	public final static boolean isAddressFormatValid(final String address) {
		Log.info("address' length: " + address.length());
		final String mode = "^[1-3][1-9A-Za-z]{20,25}";
		final Pattern pattern = Pattern.compile(mode);
		final Matcher matcher = pattern.matcher(address);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public final static boolean isFileExists(final String pathname) {
		boolean isExists = false;
		final File file = new File(pathname);
		if (file.length() > 0) {
			isExists = true;
		}
		return isExists;
	}

	public final static boolean isNetworkAvailable() throws Exception {
		boolean boolIsNetworkAvailable = false;
		final URL url = new URL("http://www.bing.com");
		final HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setConnectTimeout((int) DEFAULT_TIMEOUT);
		httpURLConnection.connect();
		if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			boolIsNetworkAvailable = true;
		}
		httpURLConnection.disconnect();
		return boolIsNetworkAvailable;
	}

	public final static boolean isTimeCorrect() throws Exception {
		boolean boolCorrect = true;
		if (Math.abs(System.currentTimeMillis() - getNTPTIME()) >= DEFAULT_TIMEOUT) {
			boolCorrect = false;
		}
		return boolCorrect;
	}

	public final static boolean isTXValueValid(final String value) {
		Log.info("Value's length: " + value.length());
		final String mode = "^[0-9]+(.[0-9]{1,4})?$";// "^[1-9][0-9]*|[1-9][0-9]*\\.[0-9]{0,4}";
		final Pattern pattern = Pattern.compile(mode);
		final Matcher matcher = pattern.matcher(value);
		if (matcher.find()) {
			return true;
		} else {
			return false;
		}
	}

	public final static byte[] longToBytes(final long foo) {
		final byte[] bytes = BigInteger.valueOf(foo).toByteArray();
		if(bytes.length == 1) {
			return bytes;
		}
		return ((bytes[0] != 0) ? bytes : Arrays.copyOfRange(bytes, 1, bytes.length));
	}

	public final static Mosaic MC() throws ClassNotFoundException, SQLException {
		return MC(PERSISTENCE.H2);
	}

	public final static Mosaic MC(final PERSISTENCE persistence) throws ClassNotFoundException, SQLException {
		Mosaic mosaic = null;
		switch (persistence) {
		case H2:
			mosaic = MosaicH2.getInstance();
			break;
		}
		return mosaic;
	}

	public final static byte[] mix(final byte[] data) throws IOException {
		ByteArrayOutputStream os = null;
		BigInteger foo = null;
		BigDecimal originalData = null, divide;
		byte[] bytes = null, halfOfBytes = null;
		final MathContext mc = new MathContext(HUNDREDPULS, RoundingMode.HALF_EVEN);
		os = new ByteArrayOutputStream();
		foo = new BigInteger(1, data);
		originalData = new BigDecimal(foo);
		bytes = foo.toByteArray();
		halfOfBytes = new byte[(bytes.length / 2)];
		if (foo.mod(ID.TWO).equals(ID.ZERO)) {
			for (int i = 0; i < halfOfBytes.length; ++i) {
				halfOfBytes[i] = bytes[2 * i];
			}
		} else {
			for (int i = 0; i < halfOfBytes.length; ++i) {
				halfOfBytes[i] = bytes[2 * i + 1];
			}
		}
		divide = originalData.divide(new BigDecimal(new BigInteger(1, halfOfBytes)), mc);
		final String[] abc = divide.toPlainString().split("\\.");
		if (abc.length == 2) {
			//				Log.info("...");
			final BigInteger e = new BigInteger(abc[0]);
			final BigInteger f = new BigInteger(abc[1]);
			//				Log.info("0: " + abc[0].length() + " data: " + e);
			//				Log.info("1: " + abc[1].length() + " data: " + f);
			os.write(e.toByteArray());
			os.write(f.toByteArray());
		} else {
			final BigInteger e = new BigInteger(abc[0]);
			Log.info("0: " + abc[0].length() + " data: " + e);
			os.write(e.toByteArray());
		}
		return os.toByteArray();
	}

	public final static byte[] mix2(final byte[] data) throws IOException {
		ByteArrayOutputStream os = null;
		BigInteger foo = null;
		BigDecimal originalData = null, divide;
		byte[] bytes = null, halfOfBytes = null;
		final MathContext mc = new MathContext(HUNDREDPULS, RoundingMode.HALF_EVEN);
		os = new ByteArrayOutputStream();
		foo = new BigInteger(1, data);
		originalData = new BigDecimal(foo);
		bytes = foo.toByteArray();
		halfOfBytes = new byte[(bytes.length / 2)];
		if (foo.mod(ID.TWO).equals(ID.ZERO)) {
			for (int i = 0; i < halfOfBytes.length; ++i) {
				halfOfBytes[i] = bytes[2 * i];
			}
		} else {
			for (int i = 0; i < halfOfBytes.length; ++i) {
				halfOfBytes[i] = bytes[2 * i + 1];
			}
		}
		divide = originalData.divide(new BigDecimal(new BigInteger(1, halfOfBytes)), mc);
		final String[] abc = divide.toPlainString().split("\\.");
		if (abc.length == 2) {
			//				Log.info("...");
			final BigInteger e = new BigInteger(abc[0]);
			final BigInteger f = new BigInteger(abc[1]);
			//				Log.info("0: " + abc[0].length() + " data: " + e);
			//				Log.info("1: " + abc[1].length() + " data: " + f);
			os.write(e.toByteArray());
			os.write(f.toByteArray());
		} else {
			final BigInteger e = new BigInteger(abc[0]);
			Log.info("0: " + abc[0].length() + " data: " + e);
			os.write(e.toByteArray());
		}
		return ((abc.length == 2)?os.toByteArray():null);
	}

	public final static byte[] multipleExtend(final byte[] data, final int multiple) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate((data.length + SINGULARITY.length) * multiple);
		// Put the multiple extended data
		for (int i = 0; i < multiple; ++i) {
			byteBuffer.put(data);
			if (i < multiple - 1) {
				byteBuffer.put(SINGULARITY);
			}
		}
		byteBuffer.flip();
		return byteBuffer.array();
	}

	public final static byte[] multipleExtendMix(final byte[] data, final int multiple) throws IOException {
		byte[] bytes = null;
		ByteArrayOutputStream os = null;
		bytes = data;
		os = new ByteArrayOutputStream();
		for(int i=0; i<multiple; ++i) {
			os.write(bytes);
			bytes = mix(bytes);
		}
		return os.toByteArray();
	}

	public final static void recoveryAccounts(final ID height) throws ClassNotFoundException, SQLException, Exception {
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

	//	public final static void regenerateAccountStatus() throws ClassNotFoundException, SQLException, Exception {
	//		// If need here need do more job to support H2
	////		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT));
	////		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_AI));
	////		EQCBlockChainRocksDB.getInstance().clearTable(EQCBlockChainRocksDB.getInstance().getTableHandle(TABLE.ACCOUNT_HASH));
	//		Log.info("Delete all AccountSnapshot");
	//		GlobalStateH2.getInstance().deletePassportSnapshotFrom(ID.ZERO, true);
	//		recoverySingularityStatus();
	//		ID tail = Util.GS().getEQCHiveTailHeight();
	//		Log.info("Current have " + tail + " EQCHive");
	//		long base = 1;
	//		ChangeLog changeLog = null;
	//		EQCHive eqcHive = null;
	//		for (; base <= tail.longValue(); ++base) {
	//			eqcHive = new EQCHive(Util.GS().getEQCHive(new ID(base)));
	//			if (eqcHive != null) {
	//				changeLog = new ChangeLog(new ID(base), new Filter(Mode.VALID));
	//				if (eqcHive.isValid()) {
	//					changeLog.takeSnapshot();
	//					changeLog.merge();
	//					changeLog.clear();
	//					Log.info("No. " + base + " verify passed");
	//					Log.info("Current tail: " + base);
	////					Util.DB().saveEQCBlockTailHeight(new ID(base));
	//				} else {
	//					Log.info("No. " + base + " verify failed");
	//					break;
	//				}
	//				eqcHive = null;
	//			} else {
	//				Log.info("No. " + base + "'s EQCHive is null just exit");
	//			}
	//		}
	////		for(long i=base; i<=tail.longValue(); ++i) {
	////			Util.DB().deleteEQCBlock(new ID(i));
	////			Log.info("Successful delete extra EQCHive No. " + i);
	////		}
	//
	//	}

	public final static void recoveryGlobalStateTo(final ID height, final GlobalState globalState) throws Exception {
		ID checkPointHeight = null;
		EQCHiveRoot eqcHiveRoot = null;
		EQcoinRootPassport eQcoinRootPassport = null;
		ID id = null;
		eqcHiveRoot = globalState.getEQCHiveRoot(height);
		eQcoinRootPassport = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
		checkPointHeight = eQcoinRootPassport.getCheckPointHeight();
		if ((height.compareTo(checkPointHeight) < 0) || (height.compareTo(globalState.getEQCHiveTailHeight()) > 0)) {
			throw new IllegalStateException(
					"Only support recovery global state from check point height: " + checkPointHeight
					+ " to tail height: " + globalState.getEQCHiveTailHeight() + " but current height: " + height + " is invalid");
		}
		Log.info("Begin recovery lock global status to " + height);
		LockMate lockMate = null;
		for (long i = 0; i < eqcHiveRoot.getTotalLockMateNumbers().longValue(); ++i) {
			id = new ID(i);
			lockMate = globalState.getLockMateSnapshot(id, height);
			if (lockMate != null) {
				Log.info("Begin recovery No." + i + " lock");
				globalState.saveLockMate(lockMate);
			}
		}
		Log.info("Begin recovery passport global status to " + height);
		Passport passport = null;
		for (long i = 0; i < eqcHiveRoot.getTotalPassportNumbers().longValue(); ++i) {
			id = new ID(i);
			passport = globalState.getPassportSnapshot(id, height);
			if (passport != null) {
				Log.info("Begin recovery No." + i + " passport");
				globalState.savePassport(passport);
			}
		}
	}

	public final static void recoverySingularityStatus() throws Exception {
		EQCHive eqcHive = null;
		GlobalState globalState = null;
		try {
			globalState = new GlobalStateH2();
			if(globalState.getEQCHiveTailHeight() == null) {
				final Savepoint savepoint = globalState.setSavepoint();
				Log.info("Begin recoverySingularityStatus: " + savepoint);
				globalState.saveEQCHiveTailHeight(ID.ZERO);
				// Create EQCHive
				eqcHive = new EQCHive(MAGIC_HASH, ID.ZERO, globalState);
				eqcHive.planting();
				eqcHive.getRoot().setTimestamp(new ID(0));
				while (!eqcHive.getRoot().isDifficultyValid()) {
					eqcHive.getRoot().setNonce(eqcHive.getRoot().getNonce().getNextID());
				}
				globalState.updateGlobalState(eqcHive, savepoint, GlobalState.MINING);
				Log.info("\n" + eqcHive.toString());
			}
		}
		finally {
			if(globalState != null) {
				globalState.close();
			}
		}
	}

	public final static boolean regex(final String regex, final String value) {
		if (value == null || value.equals("")) {
			return false;
		}
		return value.matches(regex);
	}

	public final static byte[] reverseBytes(final byte[] bytes) {
		final byte[] foo = new byte[bytes.length];
		final int lastIndex = foo.length - 1;
		for (int i = 0; i <= lastIndex; ++i) {
			foo[i] = bytes[lastIndex - i];
		}
		return foo;
	}

	public final static byte[] RIPEMD128(final byte[] bytes) {
		final RIPEMD128Digest digest = new RIPEMD128Digest();
		digest.update(bytes, 0, bytes.length);
		final byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

	public final static byte[] RIPEMD160(final byte[] bytes) {
		final RIPEMD160Digest digest = new RIPEMD160Digest();
		digest.update(bytes, 0, bytes.length);
		final byte[] out = new byte[digest.getDigestSize()];
		digest.doFinal(out, 0);
		return out;
	}

	public final static void syncSPList() throws ClassNotFoundException, SQLException, Exception {
		Log.info("syncSPList, local SP: " + LOCAL_SP + " singularity SP: " + SINGULARITY_SP);
		SPList spList = MC().getSPList(
				SP_MODE.getFlag(SP_MODE.EQCMINERNETWORK, SP_MODE.EQCHIVESYNCNETWORK, SP_MODE.EQCTRANSACTIONNETWORK));
		SPList spList2 = null;
		if (spList.isEmpty()) {
			if (LOCAL_SP.equals(SINGULARITY_SP)) {
				Log.info("This is SINGULARITY_SP and sp list is empty");
				return;
			}
			spList = EQCMinerNetworkClient.getSPList(SINGULARITY_SP);
			Log.info("Get sp list from SINGULARITY_SP: " + spList.toString());
		}
		if (!LOCAL_SP.equals(SINGULARITY_SP)) {
			Log.info("Begin register SP: " + LOCAL_SP + " to SINGULARITY_SP: " + SINGULARITY_SP);
			if (LOCAL_SP.isEQCTransactionNetwork()) {
				if (EQCTransactionNetworkClient.registerSP(SINGULARITY_SP).getPing() > 0) {
					Log.info("New EQCTransactionNetwork SP: " + LOCAL_SP + " register successful");
				} else {
					Log.info("New EQCTransactionNetwork SP: " + LOCAL_SP + " register failed");
				}
			} else if (LOCAL_SP.isEQCHiveSyncNetwork()) {
				if (EQCHiveSyncNetworkClient.registerSP(SINGULARITY_SP).getPing() > 0) {
					Log.info("New EQCHiveSyncNetwork SP: " + LOCAL_SP + " register successful");
				} else {
					Log.info("New EQCHiveSyncNetwork SP: " + LOCAL_SP + " register failed");
				}
			} else if (LOCAL_SP.isEQCMinerNetwork()) {
				if (EQCMinerNetworkClient.registerSP(SINGULARITY_SP).getPing() > 0) {
					Log.info("New EQCMinerNetwork SP: " + LOCAL_SP + " register successful");
				} else {
					Log.info("New EQCMinerNetwork SP: " + LOCAL_SP + " register failed");
				}
			}
		}
		for (final SP sp : spList.getSPList()) {
			if (!LOCAL_SP.equals(sp)) {
				try {
					spList2 = EQCMinerNetworkClient.getSPList(sp);
				} catch (final Exception e) {
					Util.updateDisconnectSPStatus(sp);
					Log.Error("During get sp list from: " + sp + " error occur: " + e.getMessage());
				}
				if (spList2 != null) {
					for (final SP sp1 : spList2.getSPList()) {
						if (!LOCAL_SP.equals(sp1)) {
							if (sp1.isEQCTransactionNetwork()) {
								if (EQCTransactionNetworkClient.registerSP(sp1).getPing() > 0) {
									Log.info("Received New EQCTransactionNetwork SP: " + sp1 + " save it to SPList");
									MC().saveSP(sp1);
								} else {
									Util.updateDisconnectSPStatus(sp1);
								}
							} else if (sp1.isEQCHiveSyncNetwork()) {
								if (EQCHiveSyncNetworkClient.registerSP(sp1).getPing() > 0) {
									Log.info("Received New EQCHiveSyncNetwork SP: " + sp1 + " save it to SPList");
									MC().saveSP(sp1);
								} else {
									Util.updateDisconnectSPStatus(sp1);
								}
							} else if (sp1.isEQCMinerNetwork()) {
								if (EQCMinerNetworkClient.registerSP(sp1).getPing() > 0) {
									Log.info("Received New EQCMinerNetwork SP: " + sp1 + " save it to SPList");
									MC().saveSP(sp1);
								} else {
									Util.updateDisconnectSPStatus(sp1);
								}
							}
						}
					}
				}
			}
		}
	}

	public final static BigInteger targetBytesToBigInteger(final byte[] foo) {
		final int target = (int) bytesToLong(foo);
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

	public final static byte[] transmutedIntoCandidate(final byte[] bytes) throws IOException {
		byte[] reverse = null, flip = null;
		ByteArrayOutputStream os = null;
		reverse = reverseBytes(bytes);
		flip = flipBytes(reverse);
		os = new ByteArrayOutputStream();
		os.write(bytes);
		os.write(reverse);
		os.write(flip);
		return os.toByteArray();
	}

	public final static void updateDisconnectSPStatus(final SP sp) {
		try {
			byte counter = 0;
			if (MC().isSPExists(sp)) {
				counter = (byte) (MC().getSPCounter(sp) + 1);
				if (counter > Util.MAX_COUNTER) {
					Log.info(sp + "'s discount counter exceed 3 times just delete it");
					MC().deleteSP(sp);
				} else {
					Log.info(sp + "'s discount counter is " + counter + " just update it's disconect state");
					MC().saveSPCounter(sp, counter);
				}
			} else {
				Log.info("Received register SP message from " + sp + " but can't access it have to discard it");
			}
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}
	}

	public final static void updateSP() {
		LOCAL_SP.setIp(getIP());
	}

	private Util() {
	}

}
