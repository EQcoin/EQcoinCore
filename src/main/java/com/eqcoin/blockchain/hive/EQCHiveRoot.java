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
package com.eqcoin.blockchain.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHiveRoot extends EQCSerializable {
	/*
	 * previous Hive proof |  target  | EQcoinSeed proof | 	 Height 	 |     timestamp 	 |       nonce  
   	 *		 64 bytes	     4 bytes     64 bytes  lengthen(>=1bytes) lengthen(>=6bytes)  lengthen(<=8bytes)   
	 */
	private byte[]	preProof;
	private byte[]	target;
	private byte[]	eqCoinSeedProof;
	private ID height;
	private ID timestamp;
	private ID nonce;
	// The EQCHeader's size is lengthen
	private final int min_size = 139; // Here exists bug need do more job to fix this
	private final static int MIN_TIMESTAMP_LEN = 6;
	private final static int MAX_NONCE_LEN = 8;
	private final static int TARGET_LEN = 4;
	private ChangeLog changeLog;
	private EQcoinSeed eQcoinSeed;
	
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
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		preProof = new byte[Util.HASH_LEN];
		target = new byte[TARGET_LEN];
		eqCoinSeedProof = new byte[Util.HASH_LEN];
		try {
			// Parse PreHash
			preProof = EQCType.parseNBytes(is, Util.HASH_LEN);
			// Parse Target
			target = EQCType.parseNBytes(is, TARGET_LEN);
			// Parse EQCoinSeedHash
			eqCoinSeedProof = EQCType.parseNBytes(is, Util.HASH_LEN);
			// Parse Height
			height = new ID(EQCType.parseEQCBits(is));
			// Parse Timestamp
			timestamp = new ID(EQCType.parseEQCBits(is));
			// Parse Nonce
			nonce = new ID(EQCType.parseEQCBits(is));
		} catch (IOException | NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("During parse EQCHiveRoot error occur: " + e.getMessage());
		}
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
			os.write(preProof);
			os.write(target);
			os.write(eqCoinSeedProof);
			os.write(height.getEQCBits());
			os.write(timestamp.getEQCBits());
			os.write(nonce.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	public void setNonce(ID nonce) {
		this.nonce = nonce;
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
					"\"PreProof\":" + "\"" + Util.getHexString(preProof) + "\"" + ",\n" +
					"\"Target\":" + "\"" + Util.getHexString(Util.targetBytesToBigInteger(target).toByteArray()) + "\"" + ",\n" +
					"\"TargetBytes\":" + "\"" + Integer.toHexString(Util.bytesToInt(target)).toUpperCase() + "\"" + ",\n" +
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
		if(preProof == null || target == null || eqCoinSeedProof == null || height == null || timestamp == null || nonce == null) {
			return false;
		}
		if(preProof.length != Util.HASH_LEN) {
			return false;
		}
		if(target.length != TARGET_LEN) {
			return false;
		}
		if(eqCoinSeedProof.length != Util.HASH_LEN) {
			return false;
		}
		if(timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN) {
			return false;
		}
		if(nonce.getEQCBits().length > MAX_NONCE_LEN) {
			return false;
		}
		return true;
	}

	public boolean isDifficultyValid(ChangeLog changeLog) throws Exception {
		if (!Arrays.equals(target, Util.cypherTarget(changeLog))) {
			return false;
		}
		if (!isDifficultyValid()) {
			Log.info("Difficulty is invalid");
			return false;
		}
		return true;
	}
	
	public boolean isDifficultyValid() throws NoSuchAlgorithmException {
		return (new BigInteger(1, getProof()).compareTo(Util.targetBytesToBigInteger(target)) <= 0);
	}
	
	@Override
	public boolean isValid() throws Exception {
		if(!isSanity()) {
			Log.info("Sanity test failed.");
			return false;
		}
		EQCHiveRoot preEQCHiveRoot = Util.DB().getEQCHiveRoot(height.getPreviousID());
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
	public void setHeight(ID height) {
		this.height = height;
	}
	
	public byte[] getSnapshot() {
		byte[] bytes = new byte[5];
		bytes[0] = preProof[33];
		bytes[1] = preProof[51];
		bytes[2] = eqCoinSeedProof[33];
		bytes[3] = eqCoinSeedProof[51];
		if(timestamp.equals(ID.ZERO)) {
			bytes[4] = 0;
		}
		else {
			bytes[4] = timestamp.getEQCBits()[3];
		}
		return bytes;
	}
	
	public O getO() {
		return new O(ByteBuffer.wrap(this.getBytes()));
	}

	/**
	 * @param eQcoinSeed the eQcoinSeed to set
	 */
	public EQCHiveRoot setEQcoinSeed(EQcoinSeed eQcoinSeed) {
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
	
}
