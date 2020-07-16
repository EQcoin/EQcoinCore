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
package org.eqcoin.persistence.globalstate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;

import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TransactionType;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;
import org.apache.commons.lang3.text.StrBuilder;
import org.eqcoin.avro.O;
import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.T1Lock;
import org.eqcoin.lock.T2Lock;
import org.eqcoin.lock.publickey.Publickey;
import org.eqcoin.passport.EQcoinRootPassport;
import org.eqcoin.passport.ExpendablePassport;
import org.eqcoin.passport.Passport;
import org.eqcoin.passport.Passport.PassportType;
import org.eqcoin.persistence.h2.EQCH2;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.SPList;
import org.eqcoin.rpc.TransactionIndex;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.seed.EQCoinSeedRoot;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class GlobalStateH2 extends EQCH2 implements GlobalState {
	private final static String JDBC_URL = "jdbc:h2:" + Util.GLOBAL_STATE_DATABASE_NAME ;//+ ";TRACE_LEVEL_FILE=3;";
	private static GlobalStateH2 instance;
	protected static String LOCKMATE_TABLE;
	protected static String PASSPORT_TABLE;
	
	protected String createLockMateTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ LockMateTable.ID + " BIGINT PRIMARY KEY CHECK " + LockMateTable.ID + ">= 0,"
				+ LockMateTable.TYPE + " TINYINT NOT NULL CHECK " + LockMateTable.TYPE + ">= 0,"
				+ LockMateTable.PROOF + " BINARY NOT NULL UNIQUE,"
				+ LockMateTable.STATUS + " TINYINT NOT NULL CHECK " + LockMateTable.STATUS + ">= 0,"
				+ LockMateTable.PUBLICKEY + " BINARY(67) UNIQUE"
				+ ")";
	}
	
	protected String createPassportTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + "("
				+ PassportTable.ID + " BIGINT PRIMARY KEY CHECK " + PassportTable.ID + ">=" + Util.MIN_ID.longValue() + " AND " + PassportTable.ID + "<" + Util.MAX_PASSPORT_ID.longValue() + ","
				+ PassportTable.LOCK_ID + " BIGINT NOT NULL UNIQUE CHECK " + PassportTable.LOCK_ID + ">=" + Util.MIN_ID.longValue() + ","
				+ PassportTable.TYPE + " TINYINT NOT NULL CHECK " + PassportTable.TYPE + ">= 0,"
				+ PassportTable.BALANCE + " BIGINT NOT NULL CHECK " + PassportTable.BALANCE + ">=" + Util.MIN_BALANCE.longValue() + " AND " + PassportTable.BALANCE + "<" + Util.MAX_BALANCE.longValue() + ","
				+ PassportTable.NONCE  + " BIGINT NOT NULL CHECK " + PassportTable.NONCE + ">= 0,"
				+ PassportTable.UPDATE_HEIGHT  + " BIGINT NOT NULL CHECK " + PassportTable.UPDATE_HEIGHT + ">= 0,"
				+ PassportTable.STORAGE + " BINARY"
				+ ")";
	}
	
	private GlobalStateH2() throws ClassNotFoundException, SQLException {
		super(JDBC_URL);
		LOCKMATE_TABLE = GlobalState.getLockMateTableName(Mode.GLOBAL);
		PASSPORT_TABLE = GlobalState.getPassportTableName(Mode.GLOBAL);
	}
	
	protected GlobalStateH2(String jdbc_url) throws ClassNotFoundException, SQLException {
		super(jdbc_url);
	}
	
	protected synchronized void createTable() throws SQLException {
		boolean result = false;
		try (Statement statement = connection.createStatement()) {
			result = statement.execute(createLockMateTable(LOCKMATE_TABLE));

			result = statement.execute(createPassportTable(PASSPORT_TABLE));

			result = statement.execute("CREATE TABLE IF NOT EXISTS " + EQCHiveTable.EQCHIVE + "(" 
					+ EQCHiveTable.HEIGHT
					+ " BIGINT  PRIMARY KEY CHECK " + EQCHiveTable.HEIGHT + ">=0," + EQCHiveTable.EQCHIVE_ROOT
					+ " BINARY NOT NULL UNIQUE," + EQCHiveTable.EQCOINSEED_ROOT + " BINARY NOT NULL UNIQUE,"
					+ EQCHiveTable.EQCOINSEEDS + " BINARY NOT NULL" + ")");

			// EQCHive tail
			statement.execute("CREATE TABLE IF NOT EXISTS " + SynchronizationTable.SYNCHRONIZATION + "("
					+ SynchronizationTable.TAIL_HEIGHT + " BIGINT" + ")");

			// Create Passport snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS " + PassportTable.PASSPORT_SNAPSHOT + "("
					+ PassportTable.KEY + " BIGINT PRIMARY KEY AUTO_INCREMENT, " + PassportTable.ID
					+ " BIGINT NOT NULL," + PassportTable.LOCK_ID + " BIGINT NOT NULL," + PassportTable.TYPE
					+ " TINYINT NOT NULL," + PassportTable.BALANCE + " BIGINT NOT NULL," + PassportTable.NONCE
					+ " BIGINT NOT NULL," + PassportTable.STORAGE + " BINARY," + PassportTable.SNAPSHOT_HEIGHT
					+ " BIGINT NOT NULL" + ")");

			// Create Lock snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS " + LockMateTable.LOCKMATE_SNAPSHOT + "("
					+ LockMateTable.KEY + " BIGINT PRIMARY KEY AUTO_INCREMENT," + LockMateTable.ID + " BIGINT NOT NULL,"
					+ LockMateTable.TYPE + " TINYINT NOT NULL," + LockMateTable.PROOF + " BINARY NOT NULL,"
					+ LockMateTable.PUBLICKEY + " BINARY(67)," + LockMateTable.SNAPSHOT_HEIGHT + " BIGINT NOT NULL"
					+ ")");
		}

		connection.setAutoCommit(false);

		if (result) {
			Log.info("Create all table successful");
		}
	}
	
	public static GlobalStateH2 getInstance() throws ClassNotFoundException, SQLException {
		if(instance == null) {
			synchronized (GlobalStateH2.class) {
				if(instance == null) {
					instance = new GlobalStateH2();
				}
			}
		}
		return instance;
	}

	@Override
	public synchronized byte[] getEQCHive(ID height) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?")){
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				os.write(resultSet.getBytes(EQCHiveTable.EQCHIVE_ROOT));
				os.write(resultSet.getBytes(EQCHiveTable.EQCOINSEEDS));
				os.write(resultSet.getBytes(EQCHiveTable.EQCOINSEED_ROOT));
			}
		}
		return os.toByteArray();
	}

	@Override
	public synchronized boolean saveEQCHive(EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		int rowCounter = 0;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + EQCHiveTable.EQCHIVE
				+ " (" + EQCHiveTable.HEIGHT + "," + EQCHiveTable.EQCHIVE_ROOT + "," + EQCHiveTable.EQCOINSEEDS + ","
				+ EQCHiveTable.EQCOINSEED_ROOT + ") VALUES (?, ?, ?, ?)")) {
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getEQCHiveRoot().getBytes());
			preparedStatement.setBytes(3, eqcHive.getEQCoinSeed().getHeaderBytes(os).toByteArray());
			preparedStatement.setBytes(4, eqcHive.getEQCoinSeed().getEQCoinSeedRoot().getBytes());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return true;
	}

	@Override
	public synchronized boolean deleteEQCHive(ID height) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return true;
	}

	@Override
	public synchronized boolean saveEQCHiveTailHeight(ID height) throws SQLException {
		int rowCounter = 0;
		if (getEQCHiveTailHeight() != null) {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("UPDATE " + SynchronizationTable.SYNCHRONIZATION + " SET " + SynchronizationTable.TAIL_HEIGHT +"=?")) {
				preparedStatement.setLong(1, height.longValue());
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT INTO " + SynchronizationTable.SYNCHRONIZATION + "(" + SynchronizationTable.TAIL_HEIGHT + ") VALUES(?)")) {
				preparedStatement.setLong(1, height.longValue());
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public synchronized ID getEQCHiveTailHeight() throws SQLException {
		ID id = null;
		try(Statement statement = connection.createStatement()){
			ResultSet resultSet = statement.executeQuery("SELECT * FROM " + SynchronizationTable.SYNCHRONIZATION);
			if (resultSet.next()) {
				id = new ID(resultSet.getLong("tail_height"));
			}
		}
		return id;
	}

	@Override
	public synchronized byte[] getEQCHiveRootProof(ID height) throws Exception {
		byte[] proof = null;
		EQCHiveRoot eqcHiveRoot = null;
		eqcHiveRoot = getEQCHiveRoot(height.getNextID());
		if(eqcHiveRoot != null) {
			proof = eqcHiveRoot.getPreProof();
		}
		else {
			eqcHiveRoot = getEQCHiveRoot(height);
			if(eqcHiveRoot != null) {
				proof = eqcHiveRoot.getProof();
			}
		}
		return proof;
	}

	public boolean savePassport(Passport passport) throws Exception {
		int rowCounter = 0;
		ID lastPassportId = null;
		if (isPassportExists(passport.getId())) {
			StringBuilder sb = new StringBuilder();
			int index = 0;
			sb.append("UPDATE ");
			sb.append(PASSPORT_TABLE);
			sb.append(" SET ");
			if (passport.isLockIDUpdate()) {
				sb.append(PassportTable.LOCK_ID);
				sb.append("=?");
			}
			if (passport.isTypeUpdate()) {
				sb.append(",");
				sb.append(PassportTable.TYPE);
				sb.append("=?");
			}
			if (passport.isBalanceUpdate()) {
				sb.append(",");
				sb.append(PassportTable.BALANCE);
				sb.append("=?");
			}
			if (passport.isNonceUpdate()) {
				sb.append(",");
				sb.append(PassportTable.NONCE);
				sb.append("=?");
			}
			if (passport.isUpdateHeightUpdate()) {
				sb.append(",");
				sb.append(PassportTable.UPDATE_HEIGHT);
				sb.append("=?");
			}
			if (!(passport instanceof ExpendablePassport)) {
				ExpendablePassport expendablePassport = (ExpendablePassport) passport;
				if (expendablePassport.isStorageUpdate()) {
					sb.append(",");
					sb.append(PassportTable.STORAGE);
					sb.append("=?");
				}
			}
			sb.append(" WHERE ");
			sb.append(PassportTable.ID);
			sb.append("=?");
			try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
				if (passport.isLockIDUpdate()) {
					preparedStatement.setLong(++index, passport.getLockID().longValue());
				}
				if (passport.isTypeUpdate()) {
					preparedStatement.setByte(++index, (byte) passport.getType().ordinal());
				}
				if (passport.isBalanceUpdate()) {
					preparedStatement.setLong(++index, passport.getBalance().longValue());
				}
				if (passport.isNonceUpdate()) {
					preparedStatement.setLong(++index, passport.getNonce().longValue());
				}
				if (passport.isUpdateHeightUpdate()) {
					preparedStatement.setLong(++index, passport.getUpdateHeight().longValue());
				}
				if (!(passport instanceof ExpendablePassport)) {
					ExpendablePassport expendablePassport = (ExpendablePassport) passport;
					if (expendablePassport.isStorageUpdate()) {
						preparedStatement.setBytes(++index, expendablePassport.getStorage().getBytes());
					}
				}
				preparedStatement.setLong(++index, passport.getId().longValue());
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			lastPassportId = getLastPassportId();
			if (lastPassportId == null) {
				if (!passport.getId().equals(ID.ZERO)) {
					throw new IllegalStateException(
							"Current hasn't any passport the first passport's ID should be 0 but actual it's: "
									+ passport.getId());
				}
			} else if (!passport.getId().isNextID(lastPassportId)) {
				throw new IllegalStateException("Current passport's ID: " + passport.getId()
						+ " should be the last passport's ID: " + lastPassportId + "'s next ID");
			}
			try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + PASSPORT_TABLE
					+ " (" + PassportTable.ID + "," + PassportTable.LOCK_ID + "," + PassportTable.TYPE + "," + PassportTable.BALANCE + "," + PassportTable.NONCE + "," + PassportTable.STORAGE + "," + PassportTable.STATE_PROOF + ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				preparedStatement.setLong(1, passport.getId().longValue());
				preparedStatement.setLong(2, passport.getLockID().longValue());
				preparedStatement.setByte(3, (byte) passport.getType().ordinal());
				preparedStatement.setLong(4, passport.getBalance().longValue());
				preparedStatement.setLong(5, passport.getNonce().longValue());
				if (passport instanceof ExpendablePassport) {
					ExpendablePassport expendablePassport = (ExpendablePassport) passport;
					preparedStatement.setBytes(6, expendablePassport.getStorage().getBytes());
				} else {
					preparedStatement.setNull(6, Types.NULL);
				}
				preparedStatement.setNull(7, Types.NULL);
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}
	
	public boolean savePassport(ResultSet resultSet) throws Exception {
		int rowCounter = 0;
		ID lastPassportId = null;
		if (isPassportExists(new ID(resultSet.getLong(PassportTable.ID)))) {
			try(PreparedStatement preparedStatement = connection.prepareStatement(
					"UPDATE " + PASSPORT_TABLE + " SET " + PassportTable.LOCK_ID + "=?," + PassportTable.TYPE + "=?," + PassportTable.BALANCE + "=?," + PassportTable.NONCE + "=?," + PassportTable.STORAGE + "=?," + PassportTable.STATE_PROOF + "=? WHERE " + PassportTable.ID + "= ?")){
				preparedStatement.setLong(1, resultSet.getLong(PassportTable.LOCK_ID));
				preparedStatement.setByte(2,  resultSet.getByte(PassportTable.TYPE));
				preparedStatement.setLong(3, resultSet.getLong(PassportTable.BALANCE));
				preparedStatement.setLong(4, resultSet.getLong(PassportTable.NONCE));
				preparedStatement.setBytes(5, resultSet.getBytes(PassportTable.STORAGE));
				preparedStatement.setBytes(6, resultSet.getBytes(PassportTable.STATE_PROOF));
				preparedStatement.setLong(7, resultSet.getLong(PassportTable.ID));
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			lastPassportId = getLastPassportId();
			if(lastPassportId == null) {
				if(resultSet.getLong(PassportTable.ID) != 0) {
					throw new IllegalStateException("Current hasn't any passport the first passport's ID should be 0 but actual it's: " + resultSet.getLong(PassportTable.ID));
				}
			}
			else if(!new ID(resultSet.getLong(PassportTable.ID)).isNextID(lastPassportId)) {
				throw new IllegalStateException("Current passport's ID: " + resultSet.getLong(PassportTable.ID) + " should be the last passport's ID: " + lastPassportId + "'s next ID");
			}
			try(PreparedStatement preparedStatement = connection.prepareStatement(
					"INSERT INTO " + PASSPORT_TABLE + "  (" + PassportTable.ID + "," + PassportTable.LOCK_ID + "," + PassportTable.TYPE + "," + PassportTable.BALANCE + "," + PassportTable.NONCE + "," + PassportTable.STORAGE + "," + PassportTable.STATE_PROOF + ") VALUES (?, ?, ?, ?, ?, ?, ?)")){
				preparedStatement.setLong(1, resultSet.getLong(PassportTable.ID));
				preparedStatement.setLong(2, resultSet.getLong(PassportTable.LOCK_ID));
				preparedStatement.setByte(3,  resultSet.getByte(PassportTable.TYPE));
				preparedStatement.setLong(4, resultSet.getLong(PassportTable.BALANCE));
				preparedStatement.setLong(5, resultSet.getLong(PassportTable.NONCE));
				preparedStatement.setBytes(6, resultSet.getBytes(PassportTable.STORAGE));
				preparedStatement.setBytes(7, resultSet.getBytes(PassportTable.STATE_PROOF));
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public synchronized Passport getPassportSnapshot(ID passportID, ID height)
			throws ClassNotFoundException, Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PassportTable.PASSPORT_SNAPSHOT + " WHERE " + PassportTable.ID + "=? AND " + PassportTable.SNAPSHOT_HEIGHT + ">? ORDER BY " + PassportTable.SNAPSHOT_HEIGHT + " LIMIT 1")){
			preparedStatement.setLong(1, passportID.longValue());
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}
	
	@Override
	public synchronized Passport getPassportSnapshotFromLockMateId(ID lockID, ID height)
			throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM PASSPORT_SNAPSHOT WHERE lock_id=? AND snapshot_height >? ORDER BY snapshot_height LIMIT 1")){
			preparedStatement.setLong(1, lockID.longValue());
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}

	@Override
	public synchronized boolean savePassportSnapshot(Passport passport, ID height) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO " + PassportTable.PASSPORT_SNAPSHOT + "(" + PassportTable.ID + "," + PassportTable.LOCK_ID + "," + PassportTable.TYPE + "," + PassportTable.BALANCE + "," + PassportTable.NONCE + "," + PassportTable.STORAGE + "," + PassportTable.STATE_PROOF + "," + PassportTable.SNAPSHOT_HEIGHT + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)")){
			preparedStatement.setLong(1, passport.getId().longValue());
			preparedStatement.setLong(2, passport.getLockID().longValue());
			preparedStatement.setByte(3, (byte) passport.getType().ordinal());
			preparedStatement.setLong(4, passport.getBalance().longValue());
			preparedStatement.setLong(5, passport.getNonce().longValue());
			if(passport instanceof ExpendablePassport) {
				ExpendablePassport expendablePassport = (ExpendablePassport) passport;
				preparedStatement.setBytes(6, expendablePassport.getStorage().getBytes());
			}
			else {
				preparedStatement.setNull(6, Types.NULL);
			}
			preparedStatement.setNull(7, Types.NULL);
			preparedStatement.setLong(8, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException {
		// Here need do more job first should get all the numbers need to be remove then check if the altered lines number is equal to what it should be
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM " + PassportTable.PASSPORT_SNAPSHOT + " WHERE " + PassportTable.SNAPSHOT_HEIGHT + (isForward ? ">=?" : "<=?"))){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertNotLess(rowCounter, ONE_ROW);
		}
		return rowCounter >= ONE_ROW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() {
		if(connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (SQLException e) {
				Log.Error(e.getMessage());
			}
		}
	}

	@Override
	public Passport getPassport(ID id) throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}
	
	@Override
	public boolean isPassportExists(ID id) throws Exception {
		boolean isExists = false;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + PassportTable.ID + " FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "= ?");){
			preparedStatement.setLong(1, id.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				isExists = true;
			}
		}
		return isExists;
	}
	
	public boolean deletePassport(ID id) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

//	@Override
//	public boolean takePassportSnapshot(ID height) throws SQLException, Exception {
//		Passport passport = null;
//		ID id = null;
//		PreparedStatement preparedStatement = null;
//		ResultSet resultSet = null;
//		
//		if(height.compareTo(ID.ZERO) > 0) {
//			EQCoinSeedRoot eQcoinRootPassport = getEQCoinSeedRoot(height.getPreviousID());
//			preparedStatement = connection
//					.prepareStatement("SELECT * FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "<? AND " + PassportTable.UPDATE_HEIGHT + "=?");
//			preparedStatement.setLong(1, eQcoinRootPassport.getTotalPassportNumbers().longValue());
//			preparedStatement.setLong(2, height.longValue());
//			resultSet = preparedStatement.executeQuery();
//			while (resultSet.next()) {
//				id = new ID(resultSet.getLong(PassportTable.ID));
//				passport = getPassport(id);
//				savePassportSnapshot(passport, height);
//			}
//		}
//		return true;
//	}


	public synchronized EQCHive getEQCHiveFile(ID height, boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				eqcHive = new EQCHive(is.readAllBytes());
			} catch (IOException | NoSuchFieldException e) {
				Log.Error(e.getMessage());
			}
		}
		return eqcHive;
	}

	public synchronized boolean isEQCHiveExistsFile(ID height) {
		boolean isEQCBlockExists = false;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	public synchronized boolean saveEQCHiveFile(EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		EQCHive eqcHive2 = null;
		File file = new File(Util.HIVE_PATH + eqcHive.getHeight().longValue() + Util.EQC_SUFFIX);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(eqcHive.getBytes());
		// Save EQCBlock
		OutputStream os = new FileOutputStream(file);
		os.write(bos.toByteArray());
		os.flush();
		os.close();
		return true;
	}

	public synchronized boolean deleteEQCHiveFile(ID height) throws Exception {
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists()) {
			if (file.delete()) {
				Log.info("EQCHive No." + height + " delete successful");
			} else {
				Log.info("EQCHive No." + height + " delete failed");
			}
		} else {
			Log.info("EQCHive No." + height + " doesn't exists");
		}
		if (getEQCHive(height) != null) {
			throw new IllegalStateException("EQCHive No." + height + " delete failed");
		}
		return true;
	}

	public synchronized EQCHiveRoot getEQCHiveRootFile(ID height) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					eqcHiveRoot = new EQCHiveRoot(bytes);
				}
			} catch (IOException | NoSuchFieldException e) {
				eqcHiveRoot = null;
				Log.Error(e.getMessage());
			}
		}
		return eqcHiveRoot;
	}
	
	@Override
	public boolean saveLockMate(LockMate lockMate) throws Exception {
		int rowCounter = 0;
		ID lastLockMateId = null;
		if (isLockMateExists(lockMate.getId())) {
			StringBuilder sb = new StringBuilder();
			int index = 0;
			sb.append("UPDATE ");
			sb.append(LOCKMATE_TABLE);
			sb.append(" SET ");
			if (lockMate.isLockUpdated()) {
				sb.append(LockMateTable.TYPE);
				sb.append("=?,");
				sb.append(LockMateTable.PROOF);
				sb.append("=?");
			}
			if (lockMate.isStatusUpdated()) {
				sb.append(",");
				sb.append(LockMateTable.STATUS);
				sb.append("=?");
			}
			if (lockMate.isPublickeyUpdated()) {
				sb.append(",");
				sb.append(LockMateTable.PUBLICKEY);
				sb.append("=?");
			}
			sb.append(" WHERE ");
			sb.append(LockMateTable.ID);
			sb.append("=?");
			try (PreparedStatement preparedStatement = connection.prepareStatement(sb.toString())) {
				if (lockMate.isLockUpdated()) {
					preparedStatement.setByte(++index, (byte) lockMate.getLock().getType().ordinal());
					preparedStatement.setBytes(++index, lockMate.getLock().getProof());
				}
				if (lockMate.isStatusUpdated()) {
					preparedStatement.setByte(++index, lockMate.getStatus());
				}
				if (lockMate.isPublickeyUpdated()) {
					if (lockMate.getPublickey() == null) {
						preparedStatement.setNull(++index, Types.BINARY);
					} else {
						preparedStatement.setBytes(++index, lockMate.getPublickey().getBytes());
					}
				}
				preparedStatement.setLong(++index, lockMate.getId().longValue());
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			lastLockMateId = getLastLockMateId();
			if (lastLockMateId == null) {
				if (!lockMate.getId().equals(ID.ZERO)) {
					throw new IllegalStateException(
							"Current hasn't any lock the first lock's ID should be 0 but actual it's: "
									+ lockMate.getId());
				}
			} else if (!lockMate.getId().isNextID(lastLockMateId)) {
				throw new IllegalStateException("Current LockMate: " + lockMate + " 's ID should be the last LockMate: "
						+ lastLockMateId + " 's ID's next ID");
			}
			try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOCKMATE_TABLE + "("
					+ LockMateTable.ID + "," + LockMateTable.TYPE + "," + LockMateTable.PROOF + ","
					+ LockMateTable.STATUS + "," + LockMateTable.PUBLICKEY + ")VALUES(?, ?, ?, ?, ?)")) {
				preparedStatement.setLong(1, lockMate.getId().longValue());
				preparedStatement.setByte(2, (byte) lockMate.getLock().getType().ordinal());
				preparedStatement.setBytes(3, lockMate.getLock().getProof());
				preparedStatement.setByte(4, lockMate.getStatus());
				preparedStatement.setNull(5, Types.BINARY);
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return rowCounter == ONE_ROW;
	}

	@Override
	public LockMate getLockMate(ID id) throws Exception {
		LockMate lockMate = null;
		byte[] publickey = null;
		try(PreparedStatement 	preparedStatement = connection
				.prepareStatement("SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) { 
				lockMate = new LockMate();
				lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
				LockType lockType = LockType.get(resultSet.getByte(LockMateTable.TYPE));
				Lock lock = null;
				if(lockType == LockType.T1) {
					lock = new T1Lock();
				}
				else if(lockType == LockType.T2) {
					lock = new T2Lock();
				}
				lockMate.setLock(lock);
				lockMate.setStatus(resultSet.getByte(LockMateTable.STATUS));
				lockMate.getLock().setProof(resultSet.getBytes(LockMateTable.PROOF));
				publickey = resultSet.getBytes(LockMateTable.PUBLICKEY);
				if (publickey == null) {
					lockMate.setPublickey(new Publickey());
				} else {
					lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getType()).Parse(publickey));
				}
			}
		}
		return lockMate;
	}

//	@Override
//	public LockMate getLock(ID id, ID height) throws Exception {
//		LockMate lock = null;
//		if (height.equals(getEQCHiveTailHeight())) {
//			lock = getLock(id, Mode.GLOBAL);
//		} else {
//			lock = getLockSnapshot(id, height);
//		}
//		return lock;
//	}

	@Override
	public boolean deleteLockMate(ID id) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

//	@Override
//	public boolean clearLockMate() throws Exception {
//		PreparedStatement preparedStatement;
//		preparedStatement = connection
//				.prepareStatement("DELETE FROM " + LOCKMATE_TABLE);
//		preparedStatement.executeUpdate();
//		return true;
//	}

	@Override
	public LockMate getLockMateSnapshot(ID lockID, ID height) throws SQLException, Exception {
		LockMate lockMate = null;
		LockType lockType = null;
		Lock lock = null;
		byte[] publickey = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + LockMateTable.LOCKMATE_SNAPSHOT + " WHERE " + LockMateTable.ID + "=? AND " + LockMateTable.SNAPSHOT_HEIGHT + ">? ORDER BY " + LockMateTable.SNAPSHOT_HEIGHT + " LIMIT 1")){
			preparedStatement.setLong(1, lockID.longValue());
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lockMate = new LockMate();
				lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
				lockType = LockType.get(resultSet.getShort(LockMateTable.TYPE));
				if(lockType == LockType.T1) {
					lock = new T1Lock();
				}
				else if(lockType == LockType.T2) {
					lock = new T2Lock();
				}
				lockMate.setLock(lock);
				lockMate.setStatus(resultSet.getByte(LockMateTable.STATUS));
				lockMate.getLock().setProof(resultSet.getBytes(LockMateTable.PROOF));
				publickey = resultSet.getBytes(LockMateTable.PUBLICKEY);
				if (publickey == null) {
					lockMate.setPublickey(new Publickey());
				} else {
					lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getType()).Parse(publickey));
				}
			}
		}
		return lockMate;
	}

	@Override
	public boolean saveLockMateSnapshot(LockMate lockMate, ID height) throws SQLException, Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LockMateTable.LOCKMATE_SNAPSHOT + "("
				+ LockMateTable.ID + "," + LockMateTable.TYPE + "," + LockMateTable.STATUS + "," + LockMateTable.PROOF
				+ "," + LockMateTable.PUBLICKEY + "," + LockMateTable.SNAPSHOT_HEIGHT + ") VALUES (?, ?, ?, ?, ?, ?)")){
			preparedStatement.setLong(1, lockMate.getId().longValue());
			preparedStatement.setByte(2, (byte) lockMate.getLock().getType().ordinal());
			preparedStatement.setByte(3, lockMate.getStatus());
			preparedStatement.setBytes(4, lockMate.getLock().getProof());
			if (lockMate.getPublickey().isNULL()) {
				preparedStatement.setNull(5, Types.BINARY);
			} else {
				preparedStatement.setBytes(5, lockMate.getPublickey().getBytes());
			}
			preparedStatement.setLong(6, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			if (preparedStatement != null) {
				preparedStatement.close();
			}
			EQCType.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public boolean deleteLockMateSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM " + LockMateTable.LOCKMATE_SNAPSHOT + " WHERE " + LockMateTable.SNAPSHOT_HEIGHT + (isForward ? " >=?" : " <=?"))){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertNotLess(rowCounter, ONE_ROW);
		}
		return rowCounter >= ONE_ROW;
	}

	public boolean saveLockMate(ResultSet resultSet) throws Exception {
		int rowCounter = 0;
		ID lastLockMateId = null;
		if (isLockMateExists(new ID(resultSet.getLong(LockMateTable.ID)))) {
			try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + LOCKMATE_TABLE + " SET "
					+ LockMateTable.TYPE + "=?," + LockMateTable.STATUS + "=?," + LockMateTable.PROOF + "=?,"
					+ LockMateTable.PUBLICKEY + "=? WHERE " + LockMateTable.ID + "= ?");) {
				preparedStatement.setByte(1, resultSet.getByte(LockMateTable.TYPE));
				preparedStatement.setByte(2, resultSet.getByte(LockMateTable.STATUS));
				preparedStatement.setBytes(3, resultSet.getBytes(LockMateTable.PROOF));
				preparedStatement.setBytes(4, resultSet.getBytes(LockMateTable.PUBLICKEY));
				preparedStatement.setLong(6, resultSet.getLong(LockMateTable.ID));
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			lastLockMateId = getLastLockMateId();
			if (lastLockMateId == null) {
				if (resultSet.getLong(LockMateTable.ID) != 0) {
					throw new IllegalStateException(
							"Current hasn't any lock the first lock's ID should be 0 but actual it's: "
									+ resultSet.getLong(LockMateTable.ID));
				}
			} else if (!new ID(resultSet.getLong(LockMateTable.ID)).isNextID(lastLockMateId)) {
				throw new IllegalStateException("Current LockMate's ID: " + resultSet.getLong(LockMateTable.ID)
						+ " should be the last LockMate: " + lastLockMateId + " 's ID's next ID");
			}
			try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOCKMATE_TABLE + "("
					+ LockMateTable.ID + "," + LockMateTable.TYPE + "," + LockMateTable.STATUS + ","
					+ LockMateTable.PROOF + "," + LockMateTable.PUBLICKEY + ") VALUES (?, ?, ?, ?, ?)")) {
				preparedStatement.setLong(1, resultSet.getLong(LockMateTable.ID));
				preparedStatement.setByte(2, resultSet.getByte(LockMateTable.TYPE));
				preparedStatement.setByte(3, resultSet.getByte(LockMateTable.STATUS));
				preparedStatement.setBytes(4, resultSet.getBytes(LockMateTable.PROOF));
				preparedStatement.setBytes(5, resultSet.getBytes(LockMateTable.PUBLICKEY));
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}
	
//	@Override
//	public boolean mergeLockMate() throws SQLException, Exception {
//		LockMate lockMate;
//		LockType lockType = null;
//		Lock lock = null;
//		byte[] publickey;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT * FROM " + LOCKMATE_TABLE);
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			lockMate = null;
//			lockMate = new LockMate();
//			lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
//			lockType = LockType.get(resultSet.getByte(LockMateTable.TYPE));
//			if(lockType == LockType.T1) {
//				lock = new T1Lock();
//			}
//			else if(lockType == LockType.T2) {
//				lock = new T2Lock();
//			}
//			lockMate.setLock(lock);
//			lockMate.setStatus(resultSet.getByte(LockMateTable.STATUS));
//			lockMate.getLock().setLockProof(resultSet.getBytes(LockMateTable.PROOF));
//			publickey = resultSet.getBytes(LockMateTable.PUBLICKEY);
//			if (publickey == null) {
//				lockMate.setPublickey(new Publickey());
//			} else {
//				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
//			}
// 			saveLockMate(lockMate);
//		}
//		return true;
//	}

	@Override
	public Connection getConnection() {
		return connection;
	}
	
	@Override
	public ID getTotalLockMateNumbers() throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		try(PreparedStatement 	preparedStatement = connection
				.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM " + LOCKMATE_TABLE)){
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewLockNumbers;
	}

	@Override
	public ID getTotalNewLockMateNumbers() throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		EQCoinSeedRoot eQcoinSeedRoot = getEQCoinSeedRoot(getEQCHiveTailHeight());
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM "
				+ LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + ">=?")){
			preparedStatement.setLong(1, eQcoinSeedRoot.getTotalLockNumbers().longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewLockNumbers;
	}
	
	@Override
	public ID getTotalPassportNumbers() throws Exception {
		ID totalPassportNumbers = ID.ZERO;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT COUNT(" + PassportTable.ID + ") FROM " + PASSPORT_TABLE)){
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalPassportNumbers;
	}

	@Override
	public ID getTotalNewPassportNumbers() throws Exception {
		ID totalNewPassportNumbers = ID.ZERO;
		EQCoinSeedRoot eQcoinSeedRoot = getEQCoinSeedRoot(getEQCHiveTailHeight());
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM "
				+ PASSPORT_TABLE + " WHERE " + PassportTable.ID + ">=?")){
			preparedStatement.setLong(1, eQcoinSeedRoot.getTotalPassportNumbers().longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewPassportNumbers;
	}

	@Override
	public boolean isLockMateExists(ID id) throws Exception {
		boolean isExists = false;
		try(PreparedStatement 	preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				isExists = true;
			}
		}
		return isExists;
	}

	@Override
	public ID isLockMateExists(Lock lock) throws Exception {
		ID lockId = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.PROOF + "=?")){
			preparedStatement.setBytes(1, lock.getProof());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lockId = new ID(resultSet.getLong(LockMateTable.ID));
			}
		}
		return lockId;
	}

	@Override
	public LockMate getLockMate(Lock lock) throws Exception {
		LockMate lockMate = null;
		Lock lock1 = null;
		byte[] publickey = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.TYPE + "=? AND " + LockMateTable.PROOF + "=?")){
			preparedStatement.setByte(1, (byte) lock.getType().ordinal());
			preparedStatement.setBytes(2, lock.getProof());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lockMate = new LockMate();
				lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
				if(lock.getType() == LockType.T1) {
					lock1 = new T1Lock();
				}
				else if(lock.getType() == LockType.T2) {
					lock1 = new T2Lock();
				}
				lockMate.setLock(lock1);
				lockMate.getLock().setProof(resultSet.getBytes(LockMateTable.PROOF));
				publickey = resultSet.getBytes(LockMateTable.PUBLICKEY);
				if (publickey == null) {
					lockMate.setPublickey(new Publickey());
				} else {
					lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getType()).Parse(publickey));
				}
			}
		}
		return lockMate;
	}

//	@Override
//	public boolean takeLockMateSnapshot(ID height) throws SQLException, Exception {
//		LockMate lock = null;
//		ID id = null;
//		EQCoinSeedRoot eQcoinSeedRoot = null;
//		if (height.compareTo(ID.ZERO) > 0) {
//			eQcoinSeedRoot = getEQCoinSeedRoot(height.getPreviousID());
//			try (PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "<?")) {
//				preparedStatement.setLong(1, eQcoinSeedRoot.getTotalLockNumbers().longValue());
//				ResultSet resultSet = preparedStatement.executeQuery();
//				while (resultSet.next()) {
//					id = new ID(resultSet.getLong(LockMateTable.ID));
//					lock = getLockMate(id);
//					saveLockMateSnapshot(lock, height);
//				}
//			}
//		}
//		return true;
//	}

	@Override
	public EQCHiveRoot getEQCHiveRoot(ID height) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + EQCHiveTable.EQCHIVE_ROOT
				+ " FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")) {
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				eqcHiveRoot = new EQCHiveRoot().setHeight(height).Parse(resultSet.getBytes(EQCHiveTable.EQCHIVE_ROOT));
			}
		}
		return eqcHiveRoot;
	}

	@Override
	public EQCoinSeedRoot getEQCoinSeedRoot(ID height) throws Exception {
		EQCoinSeedRoot eQcoinSeedRoot = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + EQCHiveTable.EQCOINSEED_ROOT + " FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")){
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				eQcoinSeedRoot = new EQCoinSeedRoot(resultSet.getBytes(EQCHiveTable.EQCOINSEED_ROOT));
			}
		}
		return eQcoinSeedRoot;
	}

	@Override
	public Passport getPassportFromLockMateId(ID lockMateId) throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.LOCK_ID + "=?")){
			preparedStatement.setLong(1, lockMateId.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}

//	@Override
//	public Vector<LockMate> getForbiddenLockList() throws Exception {
//		LockMate lockMate = null;
//		byte[] publickey = null;
//		Vector<LockMate> lockMateList = new Vector<>();
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.STATUS + "= ? OR " + LockMateTable.STATUS + "= ? ORDER BY " + LockMateTable.ID);
//		preparedStatement.setByte(1, (byte)2); // master forbidden lock mate
//		preparedStatement.setByte(2, (byte)3); // sub forbidden lock mate
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			lockMate = new LockMate();
//			lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
//			LockType lockType = LockType.get(resultSet.getByte(LockMateTable.TYPE));
//			Lock lock = null;
//			if(lockType == LockType.T1) {
//				lock = new T1Lock();
//			}
//			else if(lockType == LockType.T2) {
//				lock = new T2Lock();
//			}
//			lockMate.setLock(lock);
//			lockMate.setStatus(resultSet.getByte(LockMateTable.STATUS));
//			lockMate.getLock().setLockProof(resultSet.getBytes(LockMateTable.PROOF));
//			publickey = resultSet.getBytes(LockMateTable.PUBLICKEY);
//			if (publickey == null) {
//				lockMate.setPublickey(new Publickey());
//			} else {
//				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
//			}
//			lockMateList.add(lockMate);
//		}
//		return lockMateList;
//	}

	@Override
	public ID getLastLockMateId() throws Exception {
		ID lastLockId = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " ORDER BY " + LockMateTable.ID + " DESC LIMIT 1")){
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lastLockId = new ID(resultSet.getLong(LockMateTable.ID));
			}
		}
		return lastLockId;
	}

	@Override
	public ID getLastPassportId() throws Exception {
		ID lastPassportId = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + PassportTable.ID + " FROM " + PASSPORT_TABLE + " ORDER BY " + PassportTable.ID + " DESC LIMIT 1")){
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lastPassportId = new ID(resultSet.getLong("id"));
			}
		}
		return lastPassportId;
	}

//	@Override
//	public ID getLastEQCHiveHeight() throws Exception {
//		ID lastEQCHiveHeight = null;
//		PreparedStatement preparedStatement = null;
//		ResultSet resultSet = null;
//		preparedStatement = connection.prepareStatement("SELECT height FROM EQCHIVE ORDER BY id DESC LIMIT 1");
//		resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lastEQCHiveHeight = new ID(resultSet.getLong("height"));
//		}
//		return lastEQCHiveHeight;
//	}

	@Override
	public Statistics getStatistics() throws Exception {
		Statistics statistics = new Statistics();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
//		if ((mode != Mode.MINING) || (mode != Mode.VALID)) {
//			throw new UnsupportedModeException(mode);
//		}
		preparedStatement = connection.prepareStatement("SELECT SUM(" + PassportTable.NONCE + "), SUM(" + PassportTable.BALANCE + ") FROM " + PASSPORT_TABLE);
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			statistics.setTotalTransactionNumbers(new ID(resultSet.getLong(1)));
			statistics.setTotalSupply(new Value(resultSet.getLong(2)));
		}
		EQCoinSeedRoot eQcoinSeedRoot = getEQCoinSeedRoot(getEQCHiveTailHeight());
		preparedStatement = connection.prepareStatement("SELECT SUM(" + PassportTable.NONCE + "), SUM(" + PassportTable.BALANCE + ") FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + ">=?");
		preparedStatement.setLong(1, eQcoinSeedRoot.getTotalPassportNumbers().longValue());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			statistics.setTotalTransactionNumbers(statistics.getTotalTransactionNumbers().add(new ID(resultSet.getLong(1))));
			statistics.setTotalSupply(statistics.getTotalSupply().add(new Value(resultSet.getLong(2))));
		}
		return statistics;
	}

}
