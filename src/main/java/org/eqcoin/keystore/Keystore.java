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
package org.eqcoin.keystore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Vector;

import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-19-2018
 * @email 10509759@qq.com
 */
public class Keystore {
	public enum ECCTYPE {
		P256, P521;
		public static final ECCTYPE get(final int ordinal) {
			ECCTYPE eccType = null;
			switch (ordinal) {
			case 0:
				eccType = ECCTYPE.P256;
				break;
			case 1:
				eccType = P521;
				break;
			}
			if (eccType == null) {
				throw new IllegalStateException("Invalid ECCTYPE: " + ordinal);
			}
			return eccType;
		}

		public final byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	public static final int P256 = 1;
	public static final int P521 = 2;
	public final static String SECP256R1 = "secp256r1";
	public final static String SECP521R1 = "secp521r1";
	private static Keystore instance;

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

	private Vector<UserProfile> userProfileList;

	private Keystore() {
		userProfileList = loadUserProfileList(Util.KEYSTORE_PATH);
	}

	public synchronized UserProfile createUserProfile(final String userName, final String password, final ECCTYPE eccType, final String alais) {
		UserProfile userProfile = null;
		KeyPairGenerator kpg = null;

		try {
			userProfile = new UserProfile();
			kpg = KeyPairGenerator.getInstance("EC", "SunEC");
			ECGenParameterSpec ecsp = null;
			if(eccType == ECCTYPE.P256) {
				ecsp = new ECGenParameterSpec("secp256r1");
			}
			else if(eccType == ECCTYPE.P521) {
				ecsp = new ECGenParameterSpec("secp521r1");
			}
			kpg.initialize(ecsp, SecureRandom.getInstanceStrong());
			final KeyPair kp = kpg.genKeyPair();
			final PrivateKey privKey = kp.getPrivate();
			final PublicKey pubKey = kp.getPublic();
			final EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(eccType);
			eqcPublicKey.setECPoint((ECPublicKey) pubKey);
			userProfile.setECCType(eccType);
			userProfile.setUserName(userName);
			userProfile.setPwdProof(MessageDigest.getInstance(Util.SHA3_512).digest(password.getBytes()));
			userProfile.setPrivateKey(Util.AESEncrypt(((ECPrivateKey)privKey).getS().toByteArray(), password));
			userProfile.setPublicKey(Util.AESEncrypt(eqcPublicKey.getCompressedPublicKeyEncoded(), password));
			userProfile.setAlais(alais);
		} catch (final Exception e) {
			Log.Error("error occur: " + e.getMessage());
		}

		if (userProfile != null && !isUserProfileExist(userProfile)) {
			if(userProfileList == null) {
				userProfileList = new Vector<>();
			}
			userProfileList.add(userProfile);
			saveUserProfileList(userProfileList);
		}
		return userProfile;
	}

	public UserProfile getUserProfile(final String alais) {
		UserProfile userProfile = null;
		for (final UserProfile userProfile2 : userProfileList) {
			if (userProfile2.getAlais().equals(alais)) {
				userProfile = userProfile2;
				break;
			}
		}
		return userProfile;
	}

	/**
	 * @return the userProfileList
	 */
	public Vector<UserProfile> getUserProfileList() {
		return userProfileList;
	}

	public boolean isUserProfileExist(final UserProfile account) {
		boolean bool = false;
		if (userProfileList != null) {
			for (final UserProfile userProfile : userProfileList) {
				if (userProfile.equals(account)) {
					bool = true;
					Log.info(account.toString() + " exist.");
					break;
				}
			}
		}
		return bool;
	}

	public Vector<UserProfile> loadUserProfileList(final String path) {
		Vector<UserProfile> userProfileList = null;
		final File file = new File(path);
		if (file.exists()) {
			if (file.length() == 0) {
				Log.info("EQCoin.keystore exists but haven't any account just return.");
				return userProfileList;
			}
			Log.info("EQCoin.keystore exists and not empty just load it.");
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				final ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				userProfileList = EQCCastle.parseArray(bis, new UserProfile());
			} catch (final FileNotFoundException e) {
				Log.info("EQCoin.keystore not found: " + e.getMessage());
			} catch (final Exception e) {
				Log.info("Load userProfileList failed: " + e.getMessage());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (final IOException e) {
						Log.info(e.getMessage());
					}
				}
			}
		}
		return userProfileList;
	}

	public boolean saveUserProfileList(final Vector<UserProfile> userProfileList) {
		boolean bool = true;
		try {
			Log.info(Util.KEYSTORE_PATH);
			final File file = new File(Util.KEYSTORE_PATH);
			final File fileBak = new File(Util.KEYSTORE_PATH_BAK);
			// Backup old key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}

			// Save all userProfileList to EQCoin.keystore
			final OutputStream os = new FileOutputStream(file);
			os.write(EQCCastle.eqcSerializableListToArray(userProfileList));
			os.flush();
			os.close();

			// Backup new key store file to EQCoin.keystore.bak
			if (file.exists() && file.length() > 0) {
				if (fileBak.exists()) {
					fileBak.delete();
				}
				Files.copy(file.toPath(), fileBak.toPath());
			}
		} catch (final Exception e) {
			bool = false;
			Log.Error(e.getMessage());
		}
		return bool;
	}

}
