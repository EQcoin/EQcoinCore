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
package org.eqcoin.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

import javax.print.attribute.standard.RequestingUserName;

import org.eqcoin.avro.O;
import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.rpc.Protocol;
import org.eqcoin.seed.EQCoinSeed;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHiveRoot extends EQCSerializable implements Protocol {
	// The EQCHeader's size is lengthen
	private final int min_size = 139; // Here exists bug need do more job to fix this
	private final static int MIN_TIMESTAMP_LEN = 6;
	private final static int MAX_NONCE_LEN = 8;
	private final static int TARGET_LEN = 4;
	/**
	 * Current EQCHive's height. Height also used as version symbol.
	 *  lengthen >= 1 byte 
	 */
	private ID height;
	/**
	 * lengthen = 64 bytes
	 */
	private byte[]	preProof;
	/**
	 * lengthen = 4 bytes
	 */
	private byte[]	target;
	/**
	 * lengthen = 64 bytes
	 */
	private byte[]	eqCoinSeedProof;
	/**
	 *  lengthen >= 1 byte 
	 */
	private ID timestamp;
	/**
	 *  lengthen <= 8 bytes 
	 */
	private ID nonce;

	private ChangeLog changeLog;
	private EQCoinSeed eQcoinSeed;
	
	/**
	 * @param header
	 * @throws Exception 
	 */
	public EQCHiveRoot(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public EQCHiveRoot(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public <T> EQCHiveRoot(T type) throws Exception {
		super();
		parse(type);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse Height
		height = new ID(EQCType.parseEQCBits(is));
		// Parse PreHash
		preProof = EQCType.parseNBytes(is, Util.HASH_LEN);
		// Parse Target
		target = EQCType.parseNBytes(is, TARGET_LEN);
		// Parse EQCoinSeedHash
		eqCoinSeedProof = EQCType.parseNBytes(is, Util.HASH_LEN);
		// Parse Timestamp
		timestamp = new ID(EQCType.parseEQCBits(is));
		// Parse Nonce
		nonce = new ID(EQCType.parseEQCBits(is));
	}

//	public static boolean isValid(byte[] bytes) {
//		byte validCount = 0;
//		byte[] preHash = new byte[Util.HASH_LEN];
//		byte[] target = new byte[TARGET_LEN];
//		byte[] eqCoinSeedHash = new byte[Util.HASH_LEN];
//		int result = 0;
//		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//		byte[] data = null;
//		try {
//			// Parse PreHash
//			result = is.read(preHash);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse Target
//			result = is.read(target);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse eqCoinSeedHash
//			result = is.read(eqCoinSeedHash);
//			if(result != -1) {
//				++validCount;
//			}
//			// Parse Height
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				++validCount;
//			}
//			// Parse Timestamp
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null && !EQCType.isNULL(data)) {
//				++validCount;
//			}
//			// Parse Nonce
//			data = null;
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				++validCount;
//			}
//		} catch (IOException | NoSuchFieldException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		
//		return (validCount == VERIFICATION_COUNT) && EQCType.isInputStreamEnd(is);
//	}
	
	public EQCHiveRoot() {
	}

	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(height.getEQCBits());
			os.write(preProof);
			os.write(target);
			os.write(eqCoinSeedProof);
			os.write(timestamp.getEQCBits());
			os.write(nonce.getEQCBits());
		} catch (IOException e) {
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public ByteBuffer getByteBuffer() {
		return ByteBuffer.wrap(getBytes());
	}
	
	/**
	 * @return the preProof
	 */
	public byte[] getPreProof() {
		return preProof;
	}
	/**
	 * @param preProof the preProof to set
	 */
	public void setPreProof(byte[] preProof) {
		this.preProof = preProof;
	}
	/**
	 * @return the target
	 */
	public byte[] getTarget() {
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(byte[] target) {
		this.target = target;
	}
	/**
	 * @return the EQCoinSeedProof
	 */
	public byte[] getEQCoinSeedProof() {
		return eqCoinSeedProof;
	}
	/**
	 * @param EQCoinSeedProof the EQCoinSeedProof to set
	 */
	public void setEQCoinSeedProof(byte[] eqCoinSeedProof) {
		this.eqCoinSeedProof = eqCoinSeedProof;
	}
	/**
	 * @return the timestamp
	 */
	public ID getTimestamp() {
		return timestamp;
	}
	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(ID timestamp) {
		this.timestamp = timestamp;
	}
	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}
	/**
	 * @param nonce the nonce to set
	 */
	public EQCHiveRoot setNonce(ID nonce) {
		this.nonce = nonce;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString() 
	 */
	@Override
	public String toString() {
		return 
				"{\n" +
				toInnerJson() +
				"\n}";
	}
	
	public String toInnerJson() {
		return "\"EQCHeader\":" + 
				"{\n" +
					"\"PreProof\":" + "\"" + Util.bytesTo512HexString(preProof) + "\"" + ",\n" +
					"\"Target\":" + "\"" + Util.bytesTo512HexString(Util.targetBytesToBigInteger(target).toByteArray()) + "\"" + ",\n" +
					"\"TargetBytes\":" + "\"" + Integer.toHexString((int) Util.bytesToLong(target)).toUpperCase() + "\"" + ",\n" +
					"\"EQCoinSeedProof\":" + "\"" + Util.dumpBytes(eqCoinSeedProof, 16) + "\"" + ",\n" +
					"\"Height\":" + "\"" + height + "\"" + ",\n" +
					"\"Timestamp\":" + "\"" + Util.getGMTTime(timestamp.longValue()) + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + Integer.toHexString(nonce.intValue()).toUpperCase() + "\"" + "\n" +
				"}";
	}

	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}
	
	public int getMinSize() {
		return min_size;
	}

	/**
	 * @return byte[] the eqcHeader's EQCCHA hash
	 * @throws NoSuchAlgorithmException 
	 */
	public byte[] getProof() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA3-512").digest(Util.multipleExtendMix(getBytes(), Util.HUNDREDPULS));
	}

	@Override
	public boolean isSanity() {
		if(preProof == null) {
			Log.Error("preProof is null: " + preProof);
			return false;
		}
		if(target == null) {
			Log.Error("target is null: " + target);
			return false;
		}
		if(eqCoinSeedProof == null) {
			Log.Error("eqCoinSeedProof is null: " + eqCoinSeedProof);
			return false;
		}
		if(height == null) {
			Log.Error("height is null: " + height);
			return false;
		}
		if(timestamp == null) {
			Log.Error("timestamp is null: " + timestamp);
			return false;
		}
		if(nonce == null) {
			Log.Error("nonce is null: " + nonce);
			return false;
		}
		if(preProof.length != Util.SHA3_512_LEN) {
			Log.Error("preProof.length != Util.SHA3_512_LEN(64): " + preProof.length);
			return false;
		}
		if(target.length != TARGET_LEN) {
			Log.Error("target.length != TARGET_LEN(4): " + target.length);
			return false;
		}
		if(eqCoinSeedProof.length != Util.SHA3_512_LEN) {
			Log.Error("eqCoinSeedProof.length != Util.SHA3_512_LEN(64): " + eqCoinSeedProof.length);
			return false;
		}
		if(height.equals(ID.ZERO)) {
			if(!timestamp.equals(ID.ZERO)) {
				Log.Error("ZKP: " + timestamp);
				return false;
			}
		}
		else {
			if(timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN) {
				Log.Error("timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN(6): " + timestamp.getEQCBits().length);
				return false;
			}
		}
		if(nonce.getEQCBits().length > MAX_NONCE_LEN) {
			Log.Error("nonce.getEQCBits().length > MAX_NONCE_LEN(8): " + nonce.getEQCBits().length);
			return false;
		}
		return true;
	}

	public boolean isDifficultyValid(ChangeLog changeLog) throws Exception {
		if (!Arrays.equals(target, Util.cypherTarget(changeLog))) {
			Log.Error("target isn't valid");
			return false;
		}
		if (!isDifficultyValid()) {
			Log.info("Difficulty is invalid");
			return false;
		}
		return true;
	}
	
	// 20200514 here need check if the target is valid
	public boolean isDifficultyValid() throws NoSuchAlgorithmException {
		return (new BigInteger(1, getProof()).compareTo(Util.targetBytesToBigInteger(target)) <= 0);
	}
	
	@Override
	public boolean isValid() throws Exception {
		if(!isSanity()) {
			Log.info("Sanity test failed.");
			return false;
		}
		EQCHiveRoot preEQCHiveRoot = Util.GS().getEQCHiveRoot(height.getPreviousID());
		if(!Arrays.equals(preProof, preEQCHiveRoot.getProof())) {
			Log.Error("PreHash is invalid.");
			return false;
		}
		if(!Arrays.equals(eqCoinSeedProof, eQcoinSeed.getProof())) {
			Log.Error("eqCoinSeedHash is invalid.");
			return false;
		}
		if(!height.isNextID(preEQCHiveRoot.getHeight())) {
			Log.Error("Height should be the previous EQCBlock's next Height.");
			return false;
		}
		if(timestamp.compareTo(preEQCHiveRoot.getTimestamp()) <= 0) {
			Log.Error("Timestamp should bigger than previous EQCBlock's timestamp.");
			return false;
		}
		if(timestamp.compareTo(new ID(System.currentTimeMillis())) > 0) {
			Log.Error("Timestamp should less than current GMT time.");
			return false;
		}
		return true;
	}

	/**
	 * @return the height
	 */
	public ID getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public EQCHiveRoot setHeight(ID height) {
		this.height = height;
		return this;
	}
	
//	public byte[] getSnapshot() {
//		byte[] bytes = new byte[5];
//		bytes[0] = preProof[33];
//		bytes[1] = preProof[51];
//		bytes[2] = eqCoinSeedProof[33];
//		bytes[3] = eqCoinSeedProof[51];
//		if(timestamp.equals(ID.ZERO)) {
//			bytes[4] = 0;
//		}
//		else {
//			bytes[4] = timestamp.getEQCBits()[3];
//		}
//		return bytes;
//	}
	
	/**
	 * @param eQcoinSeed the eQcoinSeed to set
	 */
	public EQCHiveRoot setEQCoinSeed(EQCoinSeed eQcoinSeed) {
		this.eQcoinSeed = eQcoinSeed;
		return this;
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public EQCHiveRoot setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
		return this;
	}

	@Override
	public <T> void parse(T type) throws Exception {
		Objects.requireNonNull(type);
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}

	@Override
	public <T> T getProtocol(Class<T> type) throws Exception {
		T protocol = null;
		if(type.equals(O.class)) {
			protocol = (T) new O(ByteBuffer.wrap(getBytes()));
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		return protocol;
	}
	
}
