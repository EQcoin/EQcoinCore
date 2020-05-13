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
package org.eqcoin.keystore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.TransferTransaction;
import org.eqcoin.transaction.Value;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.h2.engine.User;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class UserProfile extends EQCSerializable {

	private String userName;
	private byte[] pwdProof;
	private byte[] privateKey;
	private byte[] publicKey;
	private String readableLock;

	public UserProfile() {
	}

	/**
	 * @param userName
	 * @param pwdProof
	 * @param privateKey
	 * @param publicKey
	 * @param readableAddress
	 */
	public UserProfile(String userName, byte[] pwdProof, byte[] privateKey, byte[] publicKey, String readableAddress) {
		super();
		this.userName = userName;
		this.pwdProof = pwdProof;
		this.privateKey = privateKey;
		this.publicKey = publicKey;
		this.readableLock = readableAddress;
	}

	public UserProfile(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public UserProfile(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse userName
		userName = EQCType.bytesToASCIISting(EQCType.parseBIN(is));

		// Parse pwdHash
		pwdProof = EQCType.parseNBytes(is, Util.SHA3_512_LEN);

		// Parse privateKey
		privateKey = EQCType.parseBIN(is);

		// Parse publicKey
		publicKey = EQCType.parseBIN(is);

		// Parse address
		readableLock = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public UserProfile Parse(ByteArrayInputStream is) throws Exception {
		return new UserProfile(is);
	}

	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// userName
		os.write(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(userName)));
		// pwdHash
		os.write(pwdProof);
		// privateKey
		os.write(EQCType.bytesToBIN(privateKey));
		// publicKey
		os.write(EQCType.bytesToBIN(publicKey));
		// address
		os.write(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(readableLock)));
		return os.toByteArray();
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the pwdProof
	 */
	public byte[] getPwdProof() {
		return pwdProof;
	}

	/**
	 * @param pwdProof the pwdProof to set
	 */
	public void setPwdProof(byte[] pwdProof) {
		this.pwdProof = pwdProof;
	}

	/**
	 * @return the privateKey
	 */
	public byte[] getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}
	
	/**
	 * @return the publicKey
	 */
	public byte[] getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return the readableAddress
	 */
	public String getReadableLock() {
		return readableLock;
	}
	
	/**
	 * @param readableAddress the readableAddress to set
	 */
	public void setReadableLock(String readableAddress) {
		this.readableLock = readableAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readableLock == null) ? 0 : readableLock.hashCode());
		result = prime * result + Arrays.hashCode(privateKey);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserProfile other = (UserProfile) obj;
		if (readableLock == null) {
			if (other.readableLock != null)
				return false;
		} else if (!readableLock.equals(other.readableLock))
			return false;
		if (!Arrays.equals(privateKey, other.privateKey))
			return false;
		return true;
	}
	
	public boolean isPasswordCorrect(String password) throws NoSuchAlgorithmException {
		return Arrays.equals(pwdProof, MessageDigest.getInstance(Util.SHA3_512).digest(password.getBytes()));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return
				"{\n" +
				"\"UserProfile\":" + 
				"{\n" +
					"\"UserName\":" + "\"" + userName + "\"" + ",\n" +
					"\"PasswordProof\":" + "\"" + Util.dumpBytes(pwdProof, 16) + "\"" + ",\n" +
					"\"PrivateKey\":" + "\"" + Util.dumpBytes(privateKey, 16)  + "\"" + ",\n" +
					"\"PublicKey\":" + "\"" + Util.dumpBytes(publicKey, 16)  + "\"" + ",\n" +
					"\"ReadableLock\":" + "\"" + readableLock + "\"" + "\n" +
				"}\n" +
			"}";
	}

	public LockType getLockType() throws Exception {
		return LockTool.getLockType(readableLock);
	}

	@Override
	public boolean isSanity() throws Exception {
		if(userName == null) {
			Log.Error("userName == null");
			return false;
		}
		if(pwdProof == null) {
			Log.Error("pwdProof == null");
			return false;
		}
		if(privateKey == null) {
			Log.Error("privateKey == null");
			return false;
		}
		if(publicKey == null) {
			Log.Error("publicKey == null");
			return false;
		}
		if(readableLock == null) {
			Log.Error("readableLock == null");
			return false;
		}
		if(pwdProof.length != Util.SHA3_512_LEN) {
			Log.Error("pwdProof.length != Util.SHA3_512_LEN");
			return false;
		}
		if(!LockTool.isReadableLockSanity(readableLock)) {
			Log.Error("Readable lock isn't sanity: " + readableLock);
			return false;
		}
		return true;
	}

}
