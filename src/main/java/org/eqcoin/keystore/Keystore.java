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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Vector;

import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Value;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Keystore {
	public static final int P256 = 1;
	public static final int P521 = 2;
	public final static String SECP256R1 = "secp256r1";
	public final static String SECP521R1 = "secp521r1";
	private Vector<UserProfile> userProfiles;
	private static Keystore instance;

	public enum ECCTYPE{
		P256, P521
	}
	
	private Keystore() {
		userProfiles = loadUserProfiles(Util.KEYSTORE_PATH);
	}

	public static Keystore getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new Keystore();
				}
			}
		}
		return instance;
	}

	public synchronized UserProfile createUserProfile(String userName, String password, ECCTYPE type) {
		UserProfile userProfile = new UserProfile();
		KeyPairGenerator kpg;
		LockType lockType = LockType.T1;
		
		try {
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if(type == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
				lockType = LockType.T1;
			}
			else if(type == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
				lockType = LockType.T2;
			}
//			Log.info("SecureRandom.getInstanceStrong");
			kpg.initialize(ecsp, SecureRandom.getInstanceStrong());
//			kpg.initialize(ecsp, SecureRandom.getInstance("SHA1PRNG"));
//			Log.info("after SecureRandom.getInstanceStrong");
//			Log.info("x0");
			KeyPair kp = kpg.genKeyPair();
//			Log.info("x1");
			PrivateKey privKey = kp.getPrivate();
			PublicKey pubKey = kp.getPublic();
			EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(type);
			eqcPublicKey.setECPoint((ECPublicKey) pubKey);
//			Log.info("x");
			userProfile.setUserName(userName);
			userProfile.setPwdProof(MessageDigest.getInstance(Util.SHA3_512).digest(password.getBytes()));
			userProfile.setPrivateKey(Util.AESEncrypt(((ECPrivateKey)privKey).getS().toByteArray(), password));
			userProfile.setPublicKey(Util.AESEncrypt(eqcPublicKey.getCompressedPublicKeyEncoded(), password));
			userProfile.setReadableLock(LockTool.generateReadableLock(lockType, eqcPublicKey.getCompressedPublicKeyEncoded()));
//			Log.info("x1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error("error occur: " + e.getMessage());
		}

//		Log.info("x2");
		if (userProfile != null && !isUserProfileExist(userProfile)) {
//			Log.info("x3");
			if(userProfiles == null) {
				userProfiles = new Vector<>();
			}
			userProfiles.add(userProfile);
//			Log.info("x4");
			saveUserProfiles(userProfiles);
		}
		return userProfile;
	}

	public Vector<UserProfile> loadUserProfiles(String path) {
		Vector<UserProfile> userProfileList = null;
		File file = new File(path);
		if (file.exists()) {
			if (file.length() == 0) {
				Log.info("EQCoin.keystore exists but haven't any account just return.");
				return userProfileList;
			}
			Log.info("EQCoin.keystore exists and not empty just load it.");
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				userProfileList = EQCType.parseArray(bis, new UserProfile());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("EQCoin.keystore not found: " + e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.info("Load userProfiles failed: " + e.getMessage());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.info(e.getMessage());
					}
				}
			}
		}
		return userProfileList;
	}

	public boolean saveUserProfiles(final Vector<UserProfile> userProfiles) {
		boolean bool = true;
		try {
			Log.info(Util.KEYSTORE_PATH);
			File file = new File(Util.KEYSTORE_PATH);
			File fileBak = new File(Util.KEYSTORE_PATH_BAK);
			// Backup old key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}

			// Save all userProfiles to EQCoin.keystore
			OutputStream os = new FileOutputStream(file);
			os.write(EQCType.eqcSerializableListToArray(userProfiles));
			os.flush();
			os.close();

			// Backup new key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bool = false;
			Log.Error(e.getMessage());
		}
		return bool;
	}

	public boolean isUserProfileExist(UserProfile account) {
		boolean bool = false;
		if(userProfiles != null) {
			for (UserProfile acc : userProfiles) {
				if (acc.equals(account)) {
					bool = true;
					Log.info(account.toString() + " exist.");
					break;
				}
			}
		}
		return bool;
	}

	/**
	 * @return the userProfiles
	 */
	public Vector<UserProfile> getUserProfiles() {
		return userProfiles;
	}

}
