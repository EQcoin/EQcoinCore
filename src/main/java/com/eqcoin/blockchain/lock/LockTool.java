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
package com.eqcoin.blockchain.lock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Base58;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 21, 2020
 * @email 10509759@qq.com
 */
public class LockTool {

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
	 * @param bytes compressed PublicKey. Each input will be extended 3 times
	 *              using EQCCHA_MULTIPLE or 
	 * @param lockType  EQC Address’ type
	 * @return EQC address
	 * @throws Exception 
	 */
	public static String generateLock(byte[] compressedPublickey, LockType lockType) throws Exception {
		byte[] publickey_hash = null;
		if(lockType == LockType.T1) {
			if(compressedPublickey.length != Util.P256_PUBLICKEY_LEN) {
				throw new IllegalStateException("Invalid publickey length: " + compressedPublickey.length);
			}
			publickey_hash = MessageDigest.getInstance(Util.SHA3_256).digest(Util.multipleExtend(compressedPublickey, Util.THREE));
		}
		else if(lockType == LockType.T2) {
			if(compressedPublickey.length != Util.P521_PUBLICKEY_LEN) {
				throw new IllegalStateException("Invalid publickey length: " + compressedPublickey.length);
			}
			publickey_hash = MessageDigest.getInstance(Util.SHA3_512).digest(Util.multipleExtend(compressedPublickey, Util.THREE));
		}
		else {
			throw new IllegalStateException("Invalid lock type: " + lockType);
		}
		return _generateLock(publickey_hash, lockType);
	}
	
	private static String _generateLock(byte[] publickey_hash, LockType type) throws Exception {
		byte[] type_publickey_hash = null;
		byte[] virginCRC32C = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		// Calculate (type + PublickeyHash)'s CRC32C
		os.write(type.ordinal());
		os.write(publickey_hash);
		type_publickey_hash = os.toByteArray();
		virginCRC32C = Util.CRC32C(type_publickey_hash);
		// Double calculate CRC32C to support double error check
		os = new ByteArrayOutputStream();
		os.write(virginCRC32C);
		os.write(publickey_hash);
		os.write(type.ordinal());
		
		byte[] secondCRC32C = null;
		secondCRC32C = Util.CRC32C(os.toByteArray());
		// Generate lock Base58(type) + Base58(publickey_hash + secondCRC32C)
		os = new ByteArrayOutputStream();
		os.write(publickey_hash);
		os.write(secondCRC32C);

		return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
	}
	
	public static boolean verifyEQCLockAndPublickey(EQCLock eqcLock, byte[] compressedPublickey)
			throws NoSuchAlgorithmException {
		byte[] lock_code = null;
		if (eqcLock.getLockType() == LockType.T1) {
			lock_code = MessageDigest.getInstance(Util.SHA3_256)
					.digest(Util.multipleExtend(compressedPublickey, Util.THREE));
		} else if (eqcLock.getLockType() == LockType.T2) {
			lock_code = MessageDigest.getInstance(Util.SHA3_512)
					.digest(Util.multipleExtend(compressedPublickey, Util.THREE));
		}
		Log.info("Len: " + lock_code.length + " Recovery publickey's hash: " + Util.bytesToHexString(lock_code));
    	Log.info("Len: " + eqcLock.getLockCode().length + " Lock code: " + Util.bytesToHexString(eqcLock.getLockCode()));
		return Arrays.equals(lock_code, eqcLock.getLockCode());
	}
	
	public static boolean verifyLockAndPublickey(String readableLock, byte[] compressedPublickey) throws Exception {
		byte[] lock_code0 = null;
		byte[] lock_code = null;
		LockType lockType = getLockType(readableLock);
		try {
			if(lockType == LockType.T1) {
				lock_code = MessageDigest.getInstance(Util.SHA3_256).digest(Util.multipleExtend(compressedPublickey, Util.THREE));
			}
			else if(lockType == LockType.T2) {
				lock_code = MessageDigest.getInstance(Util.SHA3_512).digest(Util.multipleExtend(compressedPublickey, Util.THREE));
			}
			lock_code0 = Base58.decode(readableLock.substring(1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return Arrays.equals(lock_code, Arrays.copyOf(lock_code0, lock_code0.length - Util.CRC32C_LEN));
	}
	
	public static byte[] readableLockToAI(String address) throws Exception {
		byte[] bytes = null;
		ByteArrayOutputStream os = null;
		os = new ByteArrayOutputStream();
		os.write(Base58.decode(address.substring(0, 1)));
		bytes = Base58.decode(address.substring(1));
		os.write(bytes, 0, bytes.length - Util.CRC32C_LEN);
		return os.toByteArray();
	}
	
	public static byte[] publickeyToAI(byte[] compressedPublickey) throws Exception {
		LockType lockType = getLockType(compressedPublickey);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(lockType == LockType.T1) {
			os.write(lockType.ordinal());
			os.write(MessageDigest.getInstance(Util.SHA3_256).digest(Util.multipleExtend(compressedPublickey, Util.THREE)));
		}
		else if(lockType == LockType.T2) {
			os.write(lockType.ordinal());
			os.write(MessageDigest.getInstance(Util.SHA3_512).digest(Util.multipleExtend(compressedPublickey, Util.THREE)));
		}
		return os.toByteArray();
	}

	public static byte[] publickeyHashToAI(LockType lockType, byte[] publickeyHash) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(lockType.ordinal());
		os.write(publickeyHash);
		return os.toByteArray();
	}
	
	public static String EQCLockToReadableLock(EQCLock eqcLock) throws Exception {
		Objects.requireNonNull(eqcLock);
		return _generateLock(eqcLock.getLockCode(), eqcLock.getLockType());
	}
	
	public static String AIToReadableLock(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		LockType lockType = LockType.T1;
		if (bytes[0] == 0) {
			lockType = LockType.T1;
		} else if (bytes[0] == 1) {
			lockType = LockType.T2;
		} 
//		else if (bytes[0] == 2) {
//			lockType = AddressType.T3;
//		}
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
		// Here need do more job
//			if(readableLock.length() < MIN_ADDRESS_LEN || readableLock.length() > MAX_ADDRESS_LEN) {
//				return false;
//			}
			addressChar = readableLock.toCharArray();
			if(addressChar[0] != '1' && addressChar[0] != '2') {
				return false;
			}
			for(char alphabet : addressChar) {
				if(!Base58.isBase58Char(alphabet)) {
					return false;
				}
			}
//			subString = address.substring(1);
//			bytes = Base58.decode(subString);
//			addressContent = Base58.encode(bytes);
//			if(!addressContent.equals(subString)) {
//				return false;
//			}
		
		return true;
	}
	
	public static boolean verifyAddressCRC32C(String readableLock) throws Exception {
		byte[] lock_code_dual_crc32c = null;
		byte[] lock_type_byte = null;
		byte[] dualCrc32c = new byte[Util.CRC32C_LEN];
		byte[] calculatedDualCrc32c = null;
		byte[] virginCrc32c = null;
		byte[] type_lock_code = null;
		int lock_code_len = 0;

		lock_type_byte = Base58.decode(readableLock.substring(0, 1));
		lock_code_dual_crc32c = Base58.decode(readableLock.substring(1));
		System.arraycopy(lock_code_dual_crc32c, lock_code_dual_crc32c.length - Util.CRC32C_LEN, dualCrc32c, 0,
				Util.CRC32C_LEN);
		lock_code_len =  lock_code_dual_crc32c.length - Util.CRC32C_LEN;
				
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(lock_type_byte);
		os.write(lock_code_dual_crc32c, 0, lock_code_len);
		type_lock_code = os.toByteArray();

		virginCrc32c = Util.CRC32C(type_lock_code);

		os = new ByteArrayOutputStream();
		os.write(virginCrc32c);
		os.write(lock_code_dual_crc32c, 0, lock_code_len);
		os.write(lock_type_byte);
		calculatedDualCrc32c = Util.CRC32C(os.toByteArray());

		return Arrays.equals(dualCrc32c, calculatedDualCrc32c);
	}
	
//	/**
//	 * @param bytes compressed PublicKey. Each input will be extended 100 times
//	 *              using EQCCHA_MULTIPLE
//	 * @param type  EQC Address’ type
//	 * @return EQC address
//	 */
//	@Deprecated 
//	public static String generateAddress(byte[] publicKey, AddressType type) {
//		byte[] bytes = EQCCHA_MULTIPLE(publicKey, Util.HUNDRED, true);
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		// Calculate (type | trim(HASH))'s CRC8ITU
//		os.write(type.ordinal());
//		Log.info("AddressType: " + type.ordinal());
//		try {
//			os.write(trim(bytes));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		byte crc = CRC8ITU.update(os.toByteArray());
//		// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//		// CRC8ITU))
//		os = new ByteArrayOutputStream();
//		try {
//			os.write(trim(bytes));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		os.write(crc);
//		return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
//	}
//	
//	@Deprecated 
//	public static boolean verifyAddress(String address, byte[] publickey) {
//		byte[] bytes = null;
//		byte crc = 0;
//		byte CRC = 0;
//		byte[] publicKey_hash = trim(EQCCHA_MULTIPLE(publickey, HUNDRED, true));
//		try {
//			bytes = Base58.decode(address.substring(1));
//			crc = bytes[bytes.length - 1];
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			os.write(Base58.decode(address.substring(0, 1)));
//			os.write(bytes, 0, bytes.length - 1);
//			CRC = CRC8ITU.update(os.toByteArray());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return (crc == CRC) && Arrays.equals(publicKey_hash, Arrays.copyOf(bytes, bytes.length - 1));
//	}
//	
//	@Deprecated 
//	public static byte[] addressToAI(String address) {
//		byte[] bytes = null;
//		ByteArrayOutputStream os = null;
//		try {
//			os = new ByteArrayOutputStream();
//			os.write(Base58.decode(address.substring(0, 1)));
//			bytes = Base58.decode(address.substring(1));
//			os.write(bytes, 0, bytes.length - 1);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return os.toByteArray();
//	}
//
//	@Deprecated 
//	public static String AIToAddress(byte[] bytes) {
//		AddressType lockType = AddressType.T1;
//		if (bytes[0] == 1) {
//			lockType = AddressType.T1;
//		} else if (bytes[0] == 2) {
//			lockType = AddressType.T2;
//		} else if (bytes[0] == 3) {
//			lockType = AddressType.T3;
//		}
//		ByteArrayOutputStream os = new ByteArrayOutputStream();
//		// Calculate (type | trim(HASH))'s CRC8ITU
//		os.write(lockType.ordinal());
//		try {
//			os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		byte crc = CRC8ITU.update(os.toByteArray());
//		// Generate address Base58(type) + Base58((trim(HASH) + (type | trim(HASH))'s
//		// CRC8ITU))
//		os = new ByteArrayOutputStream();
//		try {
//			os.write(Arrays.copyOfRange(bytes, 1, bytes.length));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		os.write(crc);
//		return Base58.encode(new byte[] { (byte) lockType.ordinal() }) + Base58.encode(os.toByteArray());
//	}
//	
//	@Deprecated 
//	public static boolean verifyAddress(String address) {
//		byte[] bytes = null;
//		byte crc = 0;
//		byte CRC = 0;
//		try {
//			bytes = Base58.decode(address.substring(1));
//			crc = bytes[bytes.length - 1];
//			ByteArrayOutputStream os = new ByteArrayOutputStream();
//			os.write(Base58.decode(address.substring(0, 1)));
//			os.write(bytes, 0, bytes.length - 1);
//			CRC = CRC8ITU.update(os.toByteArray());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return (crc == CRC);
//	}

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
		if(publickey.length == Util.P256_PUBLICKEY_LEN) {
			lockType = LockType.T1;
		}
		else if(publickey.length == Util.P521_PUBLICKEY_LEN) {
			lockType = LockType.T2;
		}
		else {
			throw new IllegalStateException("Invalid publickey length:" + publickey.length);
		}
		return lockType;
	}

	public static LockType getLockType(String readableLock) throws Exception {
		byte lockTypeOrdinal = 0;
		LockType lockType = null;
		lockTypeOrdinal = Base58.decodeToBigInteger(readableLock.substring(0, 1)).byteValue();
		if (lockTypeOrdinal == 0) {
			lockType = LockType.T1;
		} else if (lockTypeOrdinal == 1) {
			lockType = LockType.T2;
		} else {
			throw new IllegalStateException("Invalid lock type: " + lockTypeOrdinal);
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
