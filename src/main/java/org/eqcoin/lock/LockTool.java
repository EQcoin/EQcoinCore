/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
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
package org.eqcoin.lock;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Base58;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 21, 2020
 * @email 10509759@qq.com
 */
public class LockTool {

	public enum LockType {
		T1, T2;
		public static final LockType get(final int ordinal) {
			LockType lockType = null;
			switch (ordinal) {
			case 0:
				lockType = LockType.T1;
				break;
			case 1:
				lockType = LockType.T2;
				break;
			}
			if(lockType == null) {
				throw new IllegalStateException("Invalid lock type: " + ordinal);
			}
			return lockType;
		}
		public final byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}

	private static String _generateReadableLock(final LockType type, final byte[] lockProof) throws Exception {
		byte[] type_publickey_hash = null;
		byte[] virginCRC32C = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		// Calculate (type + PublickeyHash)'s CRC32C
		os.write(type.ordinal());
		os.write(lockProof);
		type_publickey_hash = os.toByteArray();
		virginCRC32C = Util.CRC32C(type_publickey_hash);
		// Double calculate CRC32C to support double error check
		os = new ByteArrayOutputStream();
		os.write(virginCRC32C);
		os.write(lockProof);
		os.write(type.ordinal());

		byte[] secondCRC32C = null;
		secondCRC32C = Util.CRC32C(os.toByteArray());
		// Generate lock Base58(type) + Base58(publickey_hash + secondCRC32C)
		os = new ByteArrayOutputStream();
		os.write(lockProof);
		os.write(secondCRC32C);

		return Base58.encode(new byte[] { (byte) type.ordinal() }) + Base58.encode(os.toByteArray());
	}

	public static final String EQCLockToReadableLock(final Lock eqcLock) throws Exception {
		Objects.requireNonNull(eqcLock);
		return _generateReadableLock(eqcLock.getType(), eqcLock.getProof());
	}

	/**
	 * @param lockType  EQC Lock's type Here need use lock type due to the others lock type's proof maybe not compressed publickey
	 * @param bytes compressed publickey or code's hash. Each input will be extended 3 times
	 *              using multipleExtend
	 * @return EQC readable lock
	 * @throws Exception
	 */
	public static final String generateReadableLock(final LockType lockType, final byte[] compressedPublickey) throws Exception {
		byte[] lock_proof = null;
		if(lockType == LockType.T1) {
			if(compressedPublickey.length != Util.P256_PUBLICKEY_LEN) {
				throw new IllegalStateException("Invalid T1 publickey length: " + compressedPublickey.length);
			}
			lock_proof = MessageDigest.getInstance(Util.SHA3_256).digest(compressedPublickey);
		}
		else if(lockType == LockType.T2) {
			if(compressedPublickey.length != Util.P521_PUBLICKEY_LEN) {
				throw new IllegalStateException("Invalid T2 publickey length: " + compressedPublickey.length);
			}
			lock_proof = MessageDigest.getInstance(Util.SHA3_512).digest(compressedPublickey);
		}
		else {
			throw new IllegalStateException("Invalid lock type: " + lockType);
		}
		return _generateReadableLock(lockType, lock_proof);
	}

	public static final byte[] getLockProof(final String readableLock) throws Exception {
		byte[] bytes = null;
		bytes = Base58.decode(readableLock.substring(1));
		return Arrays.copyOfRange(bytes, 0, bytes.length - Util.CRC32C_LEN);
	}

	public static final LockType getLockType(final byte[] compressedPublickey) {
		LockType lockType = null;
		if(compressedPublickey.length == Util.P256_PUBLICKEY_LEN) {
			lockType = LockType.T1;
		}
		else if(compressedPublickey.length == Util.P521_PUBLICKEY_LEN) {
			lockType = LockType.T2;
		}
		else {
			throw new IllegalStateException("Invalid publickey length:" + compressedPublickey.length);
		}
		return lockType;
	}

	//	@Deprecated
	//	public static final byte[] readableLockToAI(String readableLock) throws Exception {
	//		byte[] bytes = null;
	//		ByteArrayOutputStream os = null;
	//		os = new ByteArrayOutputStream();
	//		os.write(Base58.decode(readableLock.substring(0, 1)));
	//		bytes = Base58.decode(readableLock.substring(1));
	//		os.write(bytes, 0, bytes.length - Util.CRC32C_LEN);
	//		return os.toByteArray();
	//	}

	//	@Deprecated
	//	public static final byte[] publickeyToAI(byte[] compressedPublickey) throws Exception {
	//		LockType lockType = getLockType(compressedPublickey);
	//		ByteArrayOutputStream os = new ByteArrayOutputStream();
	//		if(lockType == LockType.T1) {
	//			os.write(lockType.ordinal());
	// os.write(MessageDigest.getInstance(Util.SHA3_256).digest(compressedPublickey));
	//		}
	//		else if(lockType == LockType.T2) {
	//			os.write(lockType.ordinal());
	// os.write(MessageDigest.getInstance(Util.SHA3_512).digest(compressedPublickey));
	//		}
	//		return os.toByteArray();
	//	}
	//
	//	@Deprecated
	//	public static final byte[] publickeyHashToAI(LockType lockType, byte[] publickeyHash) throws IOException {
	//		ByteArrayOutputStream os = new ByteArrayOutputStream();
	//		os.write(lockType.ordinal());
	//		os.write(publickeyHash);
	//		return os.toByteArray();
	//	}

	public static final LockType getLockType(final String readableLock) throws Exception {
		LockType lockType = null;
		final char alphabet = readableLock.charAt(0);
		if (alphabet == '1') {
			lockType = LockType.T1;
		} else if (alphabet == '2') {
			lockType = LockType.T2;
		} else {
			throw new IllegalStateException("Invalid lock type: " + alphabet);
		}
		return lockType;
	}

	@Deprecated
	public static final LockType getLockTypeFromAI(final byte[] aiLock) {
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

	public static final boolean isReadableLockSanity(final String readableLock) throws Exception {
		char[] addressChar = null;
		LockType lockType = null;
		byte[] lock_proof_dual_crc32c = null;
		byte[] lock_type_byte = null;
		final byte[] dualCrc32c = new byte[Util.CRC32C_LEN];
		byte[] calculatedDualCrc32c = null;
		byte[] virginCrc32c = null;
		byte[] type_lock_proof = null;
		int lock_proof_len = 0;
		ByteArrayOutputStream os = null;

		// Verify lock type
		addressChar = readableLock.toCharArray();
		if(addressChar[0] == '1') {
			lockType = LockType.T1;
		}
		else if(addressChar[0] == '2') {
			lockType = LockType.T2;
		}
		else {
			Log.Error("Invalid lock type: " + addressChar[0]);
			return false;
		}

		// Verify lock character set
		for (final char alphabet : addressChar) {
			if (!Base58.isBase58Char(alphabet)) {
				Log.Error("Invalid character set which exceed Base1001");
				return false;
			}
		}

		// Verify lock proof's length and crc32c
		lock_type_byte = Base58.decode(readableLock.substring(0, 1));
		lock_proof_dual_crc32c = Base58.decode(readableLock.substring(1));
		System.arraycopy(lock_proof_dual_crc32c, lock_proof_dual_crc32c.length - Util.CRC32C_LEN, dualCrc32c, 0,
				Util.CRC32C_LEN);
		lock_proof_len =  lock_proof_dual_crc32c.length - Util.CRC32C_LEN;
		if(lockType == LockType.T1) {
			if(lock_proof_len != Util.SHA3_256_LEN) {
				Log.Error("Invalid lock proof len, expected: " + Util.SHA3_256_LEN + " but actual: " + lock_proof_len);
				return false;
			}
		}
		else if(lockType == LockType.T2) {
			if(lock_proof_len != Util.SHA3_512_LEN) {
				Log.Error("Invalid lock proof len, expected: " + Util.SHA3_512_LEN + " but actual: " + lock_proof_len);
				return false;
			}
		}
		os = new ByteArrayOutputStream();
		os.write(lock_type_byte);
		os.write(lock_proof_dual_crc32c, 0, lock_proof_len);
		type_lock_proof = os.toByteArray();

		virginCrc32c = Util.CRC32C(type_lock_proof);

		os = new ByteArrayOutputStream();
		os.write(virginCrc32c);
		os.write(lock_proof_dual_crc32c, 0, lock_proof_len);
		os.write(lock_type_byte);
		calculatedDualCrc32c = Util.CRC32C(os.toByteArray());

		if(!Arrays.equals(dualCrc32c, calculatedDualCrc32c)) {
			return false;
		}

		return true;
	}

	public static final Lock publickeyToEQCLock(final LockType lockType, final byte[] compressedPublickey) throws Exception {
		Lock eqcLock = null;
		if(lockType == LockType.T1) {
			eqcLock = new T1Lock();
			eqcLock.setProof(MessageDigest.getInstance(Util.SHA3_256).digest(compressedPublickey));
		}
		else if(lockType == LockType.T2) {
			eqcLock = new T2Lock();
			eqcLock.setProof(MessageDigest.getInstance(Util.SHA3_512).digest(compressedPublickey));
		}
		return eqcLock;
	}

	//	@Deprecated
	//	public static final String AIToReadableLock(byte[] bytes) throws Exception {
	//		EQCType.assertNotNull(bytes);
	//		LockType lockType = null;
	//		if (bytes[0] == 0) {
	//			lockType = LockType.T1;
	//		} else if (bytes[0] == 1) {
	//			lockType = LockType.T2;
	//		}
	//		else {
	//			throw new UnsupportedOperationException("Unsupport lock type: " + bytes[0]);
	//		}
	//
	//		return _generateReadableLock(lockType, Arrays.copyOfRange(bytes, 1, bytes.length));
	//	}

	public static final Lock readableLockToEQCLock(final String readableLock) throws Exception {
		Objects.requireNonNull(readableLock);
		Lock eqcLock = null;
		LockType lockType = null;

		if(!isReadableLockSanity(readableLock)) {
			throw new IllegalStateException("Invalid readable lock: " + readableLock);
		}

		lockType = getLockType(readableLock);
		if(lockType == LockType.T1) {
			eqcLock = new T1Lock();
		}
		else if(lockType == LockType.T2) {
			eqcLock = new T2Lock();
		}
		eqcLock.setProof(getLockProof(readableLock));
		return eqcLock;
	}

	public static final boolean verifyEQCLockAndPublickey(final Lock eqcLock, final byte[] compressedPublickey)
			throws NoSuchAlgorithmException {
		byte[] lock_proof = null;
		int compressed_publickey_len = 0;
		if (eqcLock.getType() == LockType.T1) {
			lock_proof = MessageDigest.getInstance(Util.SHA3_256)
					.digest(compressedPublickey);
			compressed_publickey_len = Util.P256_PUBLICKEY_LEN;
		} else if (eqcLock.getType() == LockType.T2) {
			lock_proof = MessageDigest.getInstance(Util.SHA3_512)
					.digest(compressedPublickey);
			compressed_publickey_len = Util.P521_PUBLICKEY_LEN;
		}
		Log.info("Len: " + lock_proof.length + " Recovery publickey's hash: " + Util.bytesToHexString(lock_proof));
		Log.info("Len: " + eqcLock.getProof().length + " Lock code: " + Util.bytesToHexString(eqcLock.getProof()));
		return (compressed_publickey_len == compressedPublickey.length  && Arrays.equals(lock_proof, eqcLock.getProof()));
	}

	@Deprecated
	public static final boolean verifyReadableLockAndPublickey(final String readableLock, final byte[] compressedPublickey) throws Exception {
		byte[] lock_code0 = null;
		byte[] lock_code = null;
		final LockType lockType = getLockType(readableLock);
		if (lockType == LockType.T1) {
			lock_code = MessageDigest.getInstance(Util.SHA3_256)
					.digest(compressedPublickey);
		} else if (lockType == LockType.T2) {
			lock_code = MessageDigest.getInstance(Util.SHA3_512)
					.digest(compressedPublickey);
		}
		lock_code0 = Base58.decode(readableLock.substring(1));
		return Arrays.equals(lock_code, Arrays.copyOf(lock_code0, lock_code0.length - Util.CRC32C_LEN));
	}

	public static final boolean verifyReadableLockCRC32C(final String readableLock) throws Exception {
		byte[] lock_code_dual_crc32c = null;
		byte[] lock_type_byte = null;
		final byte[] dualCrc32c = new byte[Util.CRC32C_LEN];
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

	private LockTool() {
	}

}
