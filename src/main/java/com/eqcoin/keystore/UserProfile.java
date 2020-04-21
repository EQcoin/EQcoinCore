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
package com.eqcoin.keystore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.blockchain.lock.LockTool;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.crypto.EQCECCPublicKey;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class UserProfile extends EQCSerializable {

	private String userName;
	private byte[] pwdHash;
	private byte[] privateKey;
	private byte[] publicKey;
	private String readableLock;

	public UserProfile() {
	}

	/**
	 * @param userName
	 * @param pwdHash
	 * @param privateKey
	 * @param publicKey
	 * @param readableAddress
	 */
	public UserProfile(String userName, byte[] pwdHash, byte[] privateKey, byte[] publicKey, String readableAddress) {
		super();
		this.userName = userName;
		this.pwdHash = pwdHash;
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
		pwdHash = EQCType.parseBIN(is);

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
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		return (T) new UserProfile(is);
	}

	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		// userName
		os.write(EQCType.bytesToBIN(EQCType.stringToASCIIBytes(userName)));
		// pwdHash
		os.write(EQCType.bytesToBIN(pwdHash));
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
	 * @return the pwdHash
	 */
	public byte[] getPwdHash() {
		return pwdHash;
	}

	/**
	 * @param pwdHash the pwdHash to set
	 */
	public void setPwdHash(byte[] pwdHash) {
		this.pwdHash = pwdHash;
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
	
	public byte[] getAddressAI() throws Exception {
		return LockTool.readableLockToAI(readableLock);
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
	
	public boolean isPasswordCorrect(String password) {
		return Arrays.equals(pwdHash, Util.EQCCHA_MULTIPLE_DUAL(password.getBytes(), Util.HUNDREDPULS, true, false));
	}
	
	public byte[] signTransaction(String password, TransferTransaction transaction) {
		byte[] signature = null;
		try {
//			Signature ecdsa;
//			ecdsa = Signature.getInstance("SHA1withECDSA", "SunEC");
//			PrivateKey privateKey = Util.getPrivateKey(Util.AESDecrypt(this.privateKey, password), transaction.getTxIn().getLock().getLockType());
//			ecdsa.initSign(privateKey);
//			ecdsa.update(transaction.getBytes());
//			signature = ecdsa.sign();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return signature;
	}
	
	public boolean verifyTransaction(String password, TransferTransaction transaction) {
		boolean boolVerifyResult = false;
//		EQCPublicKey eqcPublicKey = null;
//		if(Util.LockTool.getLockType(transaction.getTxIn().getLock().getReadableLock()) == LockType.T1) {
//			eqcPublicKey = new EQCPublicKey(ECCTYPE.P256);
//		}
//		else if(transaction.getTxIn().getLock().getLockType() == LockType.T2) {
//			eqcPublicKey = new EQCPublicKey(ECCTYPE.P521);
//		}
//		eqcPublicKey.setECPoint(Util.AESDecrypt(publicKey, password));
//		Signature sign;
//		try {
//			sign = Signature.getInstance("SHA1withECDSA", "SunEC");
//			sign.initVerify(eqcPublicKey);
//			sign.update(transaction.getBytes());
//			boolVerifyResult = sign.verify(transaction.getEqcWitness().getSignature());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		} 
		return boolVerifyResult;
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
					"\"PasswordHash\":" + "\"" + Util.dumpBytes(pwdHash, 16) + "\"" + ",\n" +
					"\"PrivateKey\":" + "\"" + Util.dumpBytes(privateKey, 16)  + "\"" + ",\n" +
					"\"PublicKey\":" + "\"" + Util.dumpBytes(publicKey, 16)  + "\"" + ",\n" +
					"\"ReadableLock\":" + "\"" + readableLock + "\"" + "\n" +
				"}\n" +
			"}";
	}

	public LockType getAddressType() throws Exception {
		return LockTool.getLockType(readableLock);
	}

	@Override
	public boolean isSanity() throws Exception {
		if(userName == null || pwdHash == null || privateKey == null || publicKey == null || readableLock == null) {
			return false;
		}
		if(pwdHash.length != Util.HASH_LEN) {
			return false;
		}
		if(readableLock.length() < Util.MIN_ADDRESS_LEN || readableLock.length() > Util.MAX_ADDRESS_LEN) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
