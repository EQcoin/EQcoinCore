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
package org.eqcoin.wallet.persistence;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eqcoin.crypto.EQCECCPublicKey;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.lock.LockTool;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.GlobalState.Mode;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.persistence.h2.EQCH2;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 1, 2020
 * @email 10509759@qq.com
 */
public class EQCDesktopWalletH2 extends GlobalStateH2 implements EQCKeystore {
	private final static String JDBC_URL = "jdbc:h2:" + Util.WALLET_DATABASE_NAME;
	private static EQCDesktopWalletH2 instance;
	private String userName;
	private String password;

	private EQCDesktopWalletH2() throws ClassNotFoundException, SQLException {
		super(JDBC_URL);
	}

	public static EQCDesktopWalletH2 getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			synchronized (EQCDesktopWalletH2.class) {
				if (instance == null) {
					instance = new EQCDesktopWalletH2();
				}
			}
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eqcoin.persistence.globalstate.GlobalStateH2#createTable()
	 */
	@Override
	protected synchronized void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		// Create Lock table.
		boolean result = false;
		result = statement.execute(createLockMateTable(GlobalState.getLockMateTableName(Mode.WALLET)));

		// Create Account table. Each Account should be unique and it's Passport's ID
		// should be one by one
		result = statement.execute(createPassportTable(GlobalState.getPassportTableName(Mode.WALLET)));

		result = statement.execute("CREATE TABLE IF NOT EXISTS ALAIS("
				+ "alais BIGINT PRIMARY KEY CHECK alais >= 0"
				+ ")");
		
		
		statement.close();

		if (result) {
			Log.info("Create table");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eqcoin.wallet.persistence.EQCWalletH2#generateKeyPair(java.lang.Object,
	 * org.eqcoin.keystore.Keystore.ECCTYPE)
	 */
	@Override
	public synchronized <T> boolean generateKeyPair(T alais, ECCTYPE eccType) throws Exception {
		UserProfile userProfile = null;
		userProfile = Keystore.getInstance().createUserProfile(userName, password, eccType, (String) alais);
		userName = null;
		password = null;
		if (userProfile == null) {
			return false;
		} else {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eqcoin.wallet.persistence.EQCWalletH2#getPrivateKey(java.lang.Object)
	 */
	@Override
	public synchronized <T> PrivateKey getPrivateKey(T alais) throws Exception {
		UserProfile userProfile = Keystore.getInstance().getUserProfile((String) alais);
		byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), password);
		password = null;
		return Util.getPrivateKey(privateKey, userProfile.getLockType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eqcoin.wallet.persistence.EQCWalletH2#getPublicKey(java.lang.Object)
	 */
	@Override
	public synchronized <T> PublicKey getPublicKey(T alais) throws Exception {
		UserProfile userProfile = Keystore.getInstance().getUserProfile((String) alais);
		byte[] compressedPublickey = Util.AESDecrypt(userProfile.getPublicKey(), password);
		password = null;
		EQCECCPublicKey eqcPublicKey = new EQCECCPublicKey(userProfile.getECCType());
		eqcPublicKey.setECPoint(compressedPublickey);
		return eqcPublicKey;
	}

	/**
	 * @param userName the userName to set
	 */
	public EQCDesktopWalletH2 setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	/**
	 * @param password the password to set
	 */
	public EQCDesktopWalletH2 setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public String generateAlais() throws Exception {
		ID alais = null;
		alais = getAlais();
		if (alais == null) {
			alais = ID.ZERO;
		} else {
			alais = alais.getNextID();
		}
		saveAlais(alais);
		return alais.toString();
	}

	private boolean saveAlais(ID alais) throws SQLException {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		ID lastAlais = null;
		if (getAlais() != null) {
			preparedStatement = connection.prepareStatement("UPDATE ALAIS SET alais=?");
			preparedStatement.setLong(1, alais.longValue());
		} else {
			lastAlais = getLastAlais();
			if(lastAlais == null) {
				if(!alais.equals(ID.ZERO)) {
					throw new IllegalStateException("Current hasn't any alais the first alais should be 0 but actual it's " + alais);
				}
			}
			else if(!alais.isNextID(lastAlais)) {
				throw new IllegalStateException("Current alais: " + alais + " should be the last alais: " + lastAlais + "'s next ID");
			}
			preparedStatement = connection.prepareStatement("INSERT INTO ALAIS(alais) VALUES(?)");
			preparedStatement.setLong(1, alais.longValue());
		}
		rowCounter = preparedStatement.executeUpdate();
		EQCCastle.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	private ID getAlais() throws SQLException {
		ID id = null;
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT alais FROM ALAIS");
		if (resultSet.next()) {
			id = new ID(resultSet.getLong("alais"));
		}
		statement.close();
		return id;
	}
	
	private ID getLastAlais() throws SQLException {
		ID lastAlais = null;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(
					"SELECT alais FROM ALAIS ORDER BY alais DESC LIMIT 1");
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lastAlais = new ID(resultSet.getLong("alais"));
			}
		return lastAlais;
	}

}
