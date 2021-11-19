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
package org.eqcoin.persistence.globalstate.h2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.T1Lock;
import org.eqcoin.lock.T2Lock;
import org.eqcoin.lock.publickey.Publickey;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.storage.GSStateVariable;
import org.eqcoin.persistence.globalstate.storage.GSStateVariable.GSState;
import org.eqcoin.persistence.h2.EQCH2;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.passport.ExpendablePassport;
import org.eqcoin.stateobject.passport.Passport;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class GlobalStateH2 extends EQCH2 implements GlobalState {
	private final static String JDBC_URL = "jdbc:h2:" + Util.GLOBAL_STATE_DATABASE_NAME + ";DB_CLOSE_DELAY=60";// +
	// ";TRACE_LEVEL_FILE=3;";
	GlobalStateH2 instance;
	protected static String LOCKMATE_TABLE;
	protected static String PASSPORT_TABLE;

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

	public GlobalStateH2() throws ClassNotFoundException, SQLException {
		super(JDBC_URL);
	}

	protected GlobalStateH2(final String jdbc_url) throws ClassNotFoundException, SQLException {
		super(jdbc_url);
	}

	@Override
	public boolean close() throws Exception {
		if(connection != null) {
			connection.close();
			connection = null;
		}
		return true;
	}

	@Override
	public <T> void commit(final T checkPoint) throws Exception {
		try (Statement statement = connection.createStatement()) {
			statement.execute("PREPARE COMMIT " + checkPoint);
			connection.commit();
		}
	}

	protected String createLockMateTable(final String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ LockMateTable.ID + " BIGINT PRIMARY KEY CHECK " + LockMateTable.ID + ">= 0,"
				+ LockMateTable.TYPE + " TINYINT NOT NULL CHECK " + LockMateTable.TYPE + ">= 0,"
				+ LockMateTable.PROOF + " BINARY NOT NULL UNIQUE,"
				+ LockMateTable.STATUS + " TINYINT NOT NULL CHECK " + LockMateTable.STATUS + ">= 0,"
				+ LockMateTable.PUBLICKEY + " BINARY(67) UNIQUE"
				+ ")";
	}

	protected String createPassportTable(final String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + "("
				+ PassportTable.TYPE + " TINYINT NOT NULL CHECK " + PassportTable.TYPE + ">= 0,"
				+ PassportTable.ID + " BIGINT PRIMARY KEY CHECK " + PassportTable.ID + ">=" + Util.MIN_ID.longValue() + " AND " + PassportTable.ID + "<" + Util.MAX_PASSPORT_ID.longValue() + ","
				+ PassportTable.LOCK_ID + " BIGINT NOT NULL UNIQUE CHECK " + PassportTable.LOCK_ID + ">=" + Util.MIN_ID.longValue() + ","
				+ PassportTable.BALANCE + " BIGINT NOT NULL CHECK " + PassportTable.BALANCE + ">=" + Util.MIN_BALANCE.longValue() + " AND " + PassportTable.BALANCE + "<" + Util.MAX_BALANCE.longValue() + ","
				+ PassportTable.NONCE  + " BIGINT NOT NULL CHECK " + PassportTable.NONCE + ">= 0,"
				+ PassportTable.UPDATE_HEIGHT  + " BIGINT NOT NULL CHECK " + PassportTable.UPDATE_HEIGHT + ">= 0,"
				+ PassportTable.STORAGE + " BINARY"
				+ ")";
	}

	@Override
	protected synchronized void createTable() throws SQLException {
		boolean result = false;
		LOCKMATE_TABLE = GlobalState.getLockMateTableName(Mode.GLOBAL);
		PASSPORT_TABLE = GlobalState.getPassportTableName(Mode.GLOBAL);
		try (Statement statement = connection.createStatement()) {
			result = statement.execute(createLockMateTable(LOCKMATE_TABLE));

			result = statement.execute(createPassportTable(PASSPORT_TABLE));

			result = statement.execute("CREATE TABLE IF NOT EXISTS " + EQCHiveTable.EQCHIVE + "("
					+ EQCHiveTable.HEIGHT
					+ " BIGINT  PRIMARY KEY CHECK " + EQCHiveTable.HEIGHT + ">=0," + EQCHiveTable.ROOT_BODY
					+ " BINARY NOT NULL UNIQUE,"
					+ EQCHiveTable.EQCOINSEEDS + " BINARY NOT NULL" + ")");

			// Create GSStateVariableTable table
			result = statement.execute("CREATE TABLE IF NOT EXISTS " + GSStateVariableTable.GSSTATEVARIABLE + "("
					+ GSStateVariableTable.GSSTATE + " TINYINT NOT NULL CHECK " + GSStateVariableTable.GSSTATE + ">=0,"
					+ GSStateVariableTable.HEIGHT
					+ " BIGINT NOT NULL CHECK " + GSStateVariableTable.HEIGHT + ">=0," + GSStateVariableTable.DATA
					+ " BINARY)");

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

	@Override
	public synchronized boolean deleteEQCHive(final ID height) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return true;
	}

	public synchronized boolean deleteEQCHiveFile(final ID height) throws Exception {
		final File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
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

	@Override
	public boolean deleteLockMate(final ID id) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public boolean deleteLockMateSnapshotFrom(final ID height, final boolean isForward) throws SQLException, Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM " + LockMateTable.LOCKMATE_SNAPSHOT + " WHERE " + LockMateTable.SNAPSHOT_HEIGHT + (isForward ? " >=?" : " <=?"))){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertNotLess(rowCounter, ONE_ROW);
		}
		return rowCounter >= ONE_ROW;
	}

	@Override
	public boolean deletePassport(final ID id) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(final ID height, final boolean isForward) throws SQLException {
		// Here need do more job first should get all the numbers need to be remove then check if the altered lines number is equal to what it should be
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM " + PassportTable.PASSPORT_SNAPSHOT + " WHERE " + PassportTable.SNAPSHOT_HEIGHT + (isForward ? ">=?" : "<=?"))){
			preparedStatement.setLong(1, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertNotLess(rowCounter, ONE_ROW);
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
				Log.info("Begin close connection");
				connection.close();
				connection = null;
				Log.info("Connection closed");
			} catch (final SQLException e) {
				Log.Error(e.getMessage());
			}
		}
	}

	@Override
	public synchronized byte[] getEQCHive(final ID height) throws Exception {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")){
			preparedStatement.setLong(1, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				os.write(resultSet.getBytes(EQCHiveTable.HEIGHT));
				os.write(resultSet.getBytes(EQCHiveTable.ROOT_BODY));
				os.write(resultSet.getBytes(EQCHiveTable.EQCOINSEEDS));
			}
		}
		return os.toByteArray();
	}

	public synchronized EQCHive getEQCHiveFile(final ID height, final boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		final File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
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

	@Override
	public EQCHiveRoot getEQCHiveRoot(final ID height) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + EQCHiveTable.ROOT_BODY
				+ " FROM " + EQCHiveTable.EQCHIVE + " WHERE " + EQCHiveTable.HEIGHT + "=?")) {
			preparedStatement.setLong(1, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				os.write(height.getEQCBits());
				os.write(resultSet.getBytes(EQCHiveTable.ROOT_BODY));
				eqcHiveRoot = new EQCHiveRoot().Parse(os.toByteArray());
			}
		}
		return eqcHiveRoot;
	}

	public synchronized EQCHiveRoot getEQCHiveRootFile(final ID height) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		final File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				final ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCCastle.parseBIN(bis)) != null) {
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
	public synchronized byte[] getEQCHiveRootProof(final ID height) throws Exception {
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

	@Override
	public synchronized ID getEQCHiveTailHeight() throws SQLException {
		ID id = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + GSStateVariableTable.DATA
				+ " FROM " + GSStateVariableTable.GSSTATEVARIABLE + " WHERE " + GSStateVariableTable.GSSTATE + "=?")) {
			preparedStatement.setShort(1, (short) GSState.TAILHEIGHT.ordinal());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				id = new ID(resultSet.getBytes(GSStateVariableTable.DATA));
			}
		}
		return id;
	}

	@Override
	public <T extends GSStateVariable> T getGSStateVariable(final GSState gsState, final ID height) throws Exception {
		T gsStateVariable = null;
		try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "
				+ GSStateVariableTable.GSSTATEVARIABLE + " WHERE " + GSStateVariableTable.GSSTATE + "=? AND "
				+ GSStateVariableTable.HEIGHT + "<=?")) {
			preparedStatement.setShort(1, (short) gsState.ordinal());
			preparedStatement.setLong(2, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			ByteArrayOutputStream os = null;
			if (resultSet.next()) {
				os = new ByteArrayOutputStream();
				os.writeBytes(new ID(resultSet.getShort(GSStateVariableTable.GSSTATE)).getEQCBits());
				os.writeBytes(new ID(resultSet.getLong(GSStateVariableTable.HEIGHT)).getEQCBits());
				os.writeBytes(resultSet.getBytes(GSStateVariableTable.DATA));
				gsStateVariable = new GSStateVariable().Parse(os.toByteArray());
			}
		}
		return gsStateVariable;
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


	@Override
	public ID getLastEQCHiveHeight() throws Exception {
		return null;
	}

	@Override
	public ID getLastLockMateId() throws Exception {
		ID lastLockId = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " ORDER BY " + LockMateTable.ID + " DESC LIMIT 1")){
			final ResultSet resultSet = preparedStatement.executeQuery();
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
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lastPassportId = new ID(resultSet.getLong(PassportTable.ID));
			}
		}
		return lastPassportId;
	}

	@Override
	public LockMate getLockMate(final ID id) throws Exception {
		LockMate lockMate = null;
		byte[] publickey = null;
		try(PreparedStatement 	preparedStatement = connection
				.prepareStatement("SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lockMate = new LockMate();
				lockMate.setId(new ID(resultSet.getLong(LockMateTable.ID)));
				final LockType lockType = LockType.get(resultSet.getByte(LockMateTable.TYPE));
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

	@Override
	public LockMate getLockMate(final Lock lock) throws Exception {
		LockMate lockMate = null;
		Lock lock1 = null;
		byte[] publickey = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.TYPE + "=? AND " + LockMateTable.PROOF + "=?")){
			preparedStatement.setByte(1, (byte) lock.getType().ordinal());
			preparedStatement.setBytes(2, lock.getProof());
			final ResultSet resultSet = preparedStatement.executeQuery();
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

	@Override
	public LockMate getLockMateSnapshot(final ID lockID, final ID height) throws SQLException, Exception {
		LockMate lockMate = null;
		LockType lockType = null;
		Lock lock = null;
		byte[] publickey = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + LockMateTable.LOCKMATE_SNAPSHOT + " WHERE " + LockMateTable.ID + "=? AND " + LockMateTable.SNAPSHOT_HEIGHT + ">? ORDER BY " + LockMateTable.SNAPSHOT_HEIGHT + " LIMIT 1")){
			preparedStatement.setLong(1, lockID.longValue());
			preparedStatement.setLong(2, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
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
	public Passport getPassport(final ID id) throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
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
	public Passport getPassportFromLockMateId(final ID lockMateId) throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.LOCK_ID + "=?")){
			preparedStatement.setLong(1, lockMateId.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
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
	public synchronized Passport getPassportSnapshot(final ID passportID, final ID height)
			throws ClassNotFoundException, Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + PassportTable.PASSPORT_SNAPSHOT + " WHERE " + PassportTable.ID + "=? AND " + PassportTable.SNAPSHOT_HEIGHT + ">? ORDER BY " + PassportTable.SNAPSHOT_HEIGHT + " LIMIT 1")){
			preparedStatement.setLong(1, passportID.longValue());
			preparedStatement.setLong(2, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}

	@Override
	public synchronized Passport getPassportSnapshotFromLockMateId(final ID lockID, final ID height)
			throws Exception {
		Passport passport = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM PASSPORT_SNAPSHOT WHERE lock_id=? AND snapshot_height >? ORDER BY snapshot_height LIMIT 1")){
			preparedStatement.setLong(1, lockID.longValue());
			preparedStatement.setLong(2, height.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				passport = Passport.parsePassport(resultSet);
			}
		}
		return passport;
	}

	@Override
	public Statistics getStatistics() throws Exception {
		final Statistics statistics = new Statistics();
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT SUM(" + PassportTable.NONCE + "), SUM(" + PassportTable.BALANCE + ") FROM " + PASSPORT_TABLE)){
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				statistics.setTotalTransactionNumbers(new ID(resultSet.getLong(1)));
				statistics.setTotalSupply(new Value(resultSet.getLong(2)));
			}
		}
		final EQCHiveRoot eqcHiveRoot = getEQCHiveRoot(getEQCHiveTailHeight());
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT SUM(" + PassportTable.NONCE + "), SUM(" + PassportTable.BALANCE + ") FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + ">=?")){
			preparedStatement.setLong(1, eqcHiveRoot.getTotalPassportNumbers().longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				statistics.setTotalTransactionNumbers(statistics.getTotalTransactionNumbers().add(new ID(resultSet.getLong(1))));
				statistics.setTotalSupply(statistics.getTotalSupply().add(new Value(resultSet.getLong(2))));
			}
		}
		statistics.setTotalLockMateNumbers(getTotalLockMateNumbers());
		statistics.setTotalPassportNumbers(getTotalPassportNumbers());
		return statistics;
	}

	@Override
	public ID getTotalLockMateNumbers() throws Exception {
		ID totalLockNumbers = ID.ZERO;
		try(PreparedStatement 	preparedStatement = connection
				.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM " + LOCKMATE_TABLE)){
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalLockNumbers;
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
	public ID getTotalNewLockMateNumbers() throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		final EQCHiveRoot eqcHiveRoot = getEQCHiveRoot(getEQCHiveTailHeight());
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM "
				+ LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + ">=?")){
			preparedStatement.setLong(1, eqcHiveRoot.getTotalLockMateNumbers().longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewLockNumbers;
	}

	@Override
	public ID getTotalNewPassportNumbers() throws Exception {
		ID totalNewPassportNumbers = ID.ZERO;
		final EQCHiveRoot eqcHiveRoot = getEQCHiveRoot(getEQCHiveTailHeight());
		try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(" + LockMateTable.ID + ") FROM "
				+ PASSPORT_TABLE + " WHERE " + PassportTable.ID + ">=?")){
			preparedStatement.setLong(1, eqcHiveRoot.getTotalPassportNumbers().longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewPassportNumbers;
	}

	@Override
	public ID getTotalPassportNumbers() throws Exception {
		ID totalPassportNumbers = ID.ZERO;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT COUNT(" + PassportTable.ID + ") FROM " + PASSPORT_TABLE)) {
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalPassportNumbers;
	}

	public synchronized boolean isEQCHiveExistsFile(final ID height) {
		boolean isEQCBlockExists = false;
		final File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	@Override
	public boolean isLockMateExists(final ID id) throws Exception {
		boolean isExists = false;
		try(PreparedStatement 	preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.ID + "=?")){
			preparedStatement.setLong(1, id.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				isExists = true;
			}
		}
		return isExists;
	}

	@Override
	public ID isLockMateExists(final Lock lock) throws Exception {
		ID lockId = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + LockMateTable.ID + " FROM " + LOCKMATE_TABLE + " WHERE " + LockMateTable.PROOF + "=?")){
			preparedStatement.setBytes(1, lock.getProof());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				lockId = new ID(resultSet.getLong(LockMateTable.ID));
			}
		}
		return lockId;
	}

	@Override
	public boolean isPassportExists(final ID id) throws Exception {
		boolean isExists = false;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT " + PassportTable.ID + " FROM " + PASSPORT_TABLE + " WHERE " + PassportTable.ID + "= ?");){
			preparedStatement.setLong(1, id.longValue());
			final ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				isExists = true;
			}
		}
		return isExists;
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
	public <T> void releaseSavepoint(final T savepoint) throws Exception {
		connection.releaseSavepoint((Savepoint) savepoint);
	}

	@Override
	public <T> void rollback(final T savepoint) throws Exception {
		connection.rollback((Savepoint) savepoint);
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
	public synchronized boolean saveEQCHive(final EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		int rowCounter = 0;
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + EQCHiveTable.EQCHIVE
				+ " (" + EQCHiveTable.HEIGHT + "," + EQCHiveTable.ROOT_BODY + "," + EQCHiveTable.EQCOINSEEDS + ") VALUES (?, ?, ?)")) {
			preparedStatement.setLong(1, eqcHive.getRoot().getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getRoot().getBodyBytes(os).toByteArray());
			preparedStatement.setBytes(3, eqcHive.getEQCoinSeeds().getHeaderBytes(os).toByteArray());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return true;
	}

	public synchronized boolean saveEQCHiveFile(final EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		final EQCHive eqcHive2 = null;
		final File file = new File(Util.HIVE_PATH + eqcHive.getRoot().getHeight().longValue() + Util.EQC_SUFFIX);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(eqcHive.getBytes());
		// Save EQCBlock
		final OutputStream os = new FileOutputStream(file);
		os.write(bos.toByteArray());
		os.flush();
		os.close();
		return true;
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
	public synchronized boolean saveEQCHiveTailHeight(final ID height) throws SQLException {
		int rowCounter = 0;
		if (getEQCHiveTailHeight() != null) {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("UPDATE " + GSStateVariableTable.GSSTATEVARIABLE + " SET "
							+ GSStateVariableTable.HEIGHT + "=?, " + GSStateVariableTable.DATA + "=?")) {
				preparedStatement.setLong(1, height.longValue());
				preparedStatement.setBytes(2, height.getEQCBits());
				rowCounter = preparedStatement.executeUpdate();
			}
		} else {
			try (PreparedStatement preparedStatement = connection
					.prepareStatement("INSERT INTO " + GSStateVariableTable.GSSTATEVARIABLE + "("
							+ GSStateVariableTable.GSSTATE + "," + GSStateVariableTable.HEIGHT + ","
							+ GSStateVariableTable.DATA + ") VALUES(?,?,?)")) {
				preparedStatement.setShort(1, (short) GSState.TAILHEIGHT.ordinal());
				preparedStatement.setLong(2, height.longValue());
				preparedStatement.setBytes(3, height.getEQCBits());
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCCastle.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public boolean saveGSStateVariable(final GSStateVariable gsStateVariable, final ID height) throws Exception {
		int rowCounter = 0;
		try (PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO " + GSStateVariableTable.GSSTATEVARIABLE + "(" + GSStateVariableTable.GSSTATE + ","
						+ GSStateVariableTable.HEIGHT + "," + GSStateVariableTable.DATA + ") VALUES(?,?,?)")) {
			preparedStatement.setShort(1, (short) gsStateVariable.getState().ordinal());
			preparedStatement.setLong(2, height.longValue());
			preparedStatement.setBytes(3, gsStateVariable.getBodyBytes(new ByteArrayOutputStream()).toByteArray());
			rowCounter = preparedStatement.executeUpdate();
		}
		EQCCastle.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public boolean saveLockMate(final LockMate lockMate) throws Exception {
		int rowCounter = 0;
		ID lastLockMateId = null;
		if (isLockMateExists(lockMate.getId())) {
			final StringBuilder sb = new StringBuilder();
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
				preparedStatement.setShort(2, (short) lockMate.getLock().getType().ordinal());
				preparedStatement.setBytes(3, lockMate.getLock().getProof());
				preparedStatement.setByte(4, lockMate.getStatus());
				preparedStatement.setNull(5, Types.BINARY);
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCCastle.assertEqual(rowCounter, ONE_ROW);
		return rowCounter == ONE_ROW;
	}

	//	public boolean saveLockMate(final ResultSet resultSet) throws Exception {
	//		int rowCounter = 0;
	//		ID lastLockMateId = null;
	//		if (isLockMateExists(new ID(resultSet.getLong(LockMateTable.ID)))) {
	//			try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + LOCKMATE_TABLE + " SET "
	//					+ LockMateTable.TYPE + "=?," + LockMateTable.STATUS + "=?," + LockMateTable.PROOF + "=?,"
	//					+ LockMateTable.PUBLICKEY + "=? WHERE " + LockMateTable.ID + "= ?");) {
	//				preparedStatement.setByte(1, resultSet.getByte(LockMateTable.TYPE));
	//				preparedStatement.setByte(2, resultSet.getByte(LockMateTable.STATUS));
	//				preparedStatement.setBytes(3, resultSet.getBytes(LockMateTable.PROOF));
	//				preparedStatement.setBytes(4, resultSet.getBytes(LockMateTable.PUBLICKEY));
	//				preparedStatement.setLong(6, resultSet.getLong(LockMateTable.ID));
	//				rowCounter = preparedStatement.executeUpdate();
	//			}
	//		} else {
	//			lastLockMateId = getLastLockMateId();
	//			if (lastLockMateId == null) {
	//				if (resultSet.getLong(LockMateTable.ID) != 0) {
	//					throw new IllegalStateException(
	//							"Current hasn't any lock the first lock's ID should be 0 but actual it's: "
	//									+ resultSet.getLong(LockMateTable.ID));
	//				}
	//			} else if (!new ID(resultSet.getLong(LockMateTable.ID)).isNextID(lastLockMateId)) {
	//				throw new IllegalStateException("Current LockMate's ID: " + resultSet.getLong(LockMateTable.ID)
	//				+ " should be the last LockMate: " + lastLockMateId + " 's ID's next ID");
	//			}
	//			try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + LOCKMATE_TABLE + "("
	//					+ LockMateTable.ID + "," + LockMateTable.TYPE + "," + LockMateTable.STATUS + ","
	//					+ LockMateTable.PROOF + "," + LockMateTable.PUBLICKEY + ") VALUES (?, ?, ?, ?, ?)")) {
	//				preparedStatement.setLong(1, resultSet.getLong(LockMateTable.ID));
	//				preparedStatement.setByte(2, resultSet.getByte(LockMateTable.TYPE));
	//				preparedStatement.setByte(3, resultSet.getByte(LockMateTable.STATUS));
	//				preparedStatement.setBytes(4, resultSet.getBytes(LockMateTable.PROOF));
	//				preparedStatement.setBytes(5, resultSet.getBytes(LockMateTable.PUBLICKEY));
	//				rowCounter = preparedStatement.executeUpdate();
	//			}
	//		}
	//		EQCType.assertEqual(rowCounter, ONE_ROW);
	//		return true;
	//	}

	//	public boolean savePassport(final ResultSet resultSet) throws Exception {
	//		int rowCounter = 0;
	//		ID lastPassportId = null;
	//		if (isPassportExists(new ID(resultSet.getLong(PassportTable.ID)))) {
	//			try(PreparedStatement preparedStatement = connection.prepareStatement(
	//					"UPDATE " + PASSPORT_TABLE + " SET " + PassportTable.TYPE + "=?," + PassportTable.LOCK_ID + "=?,"
	//							+ PassportTable.BALANCE + "=?," + PassportTable.NONCE + "=?," + PassportTable.STORAGE
	//							+ "=? WHERE " + PassportTable.ID + "=?")) {
	//				preparedStatement.setByte(2, resultSet.getByte(PassportTable.TYPE));
	//				preparedStatement.setLong(1, resultSet.getLong(PassportTable.LOCK_ID));
	//				preparedStatement.setLong(3, resultSet.getLong(PassportTable.BALANCE));
	//				preparedStatement.setLong(4, resultSet.getLong(PassportTable.NONCE));
	//				preparedStatement.setBytes(5, resultSet.getBytes(PassportTable.STORAGE));
	//				preparedStatement.setLong(6, resultSet.getLong(PassportTable.ID));
	//				rowCounter = preparedStatement.executeUpdate();
	//			}
	//		} else {
	//			lastPassportId = getLastPassportId();
	//			if(lastPassportId == null) {
	//				if(resultSet.getLong(PassportTable.ID) != 0) {
	//					throw new IllegalStateException("Current hasn't any passport the first passport's ID should be 0 but actual it's: " + resultSet.getLong(PassportTable.ID));
	//				}
	//			}
	//			else if(!new ID(resultSet.getLong(PassportTable.ID)).isNextID(lastPassportId)) {
	//				throw new IllegalStateException("Current passport's ID: " + resultSet.getLong(PassportTable.ID) + " should be the last passport's ID: " + lastPassportId + "'s next ID");
	//			}
	//			try(PreparedStatement preparedStatement = connection.prepareStatement(
	//					"INSERT INTO " + PASSPORT_TABLE + "  (" + PassportTable.TYPE + "," + PassportTable.ID + ","
	//							+ PassportTable.LOCK_ID + "," + PassportTable.BALANCE + "," + PassportTable.NONCE + ","
	//							+ PassportTable.STORAGE + "," + PassportTable.STATE_PROOF
	//							+ ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
	//				preparedStatement.setLong(1, resultSet.getLong(PassportTable.ID));
	//				preparedStatement.setLong(2, resultSet.getLong(PassportTable.LOCK_ID));
	//				preparedStatement.setByte(3,  resultSet.getByte(PassportTable.TYPE));
	//				preparedStatement.setLong(4, resultSet.getLong(PassportTable.BALANCE));
	//				preparedStatement.setLong(5, resultSet.getLong(PassportTable.NONCE));
	//				preparedStatement.setBytes(6, resultSet.getBytes(PassportTable.STORAGE));
	//				preparedStatement.setBytes(7, resultSet.getBytes(PassportTable.STATE_PROOF));
	//				rowCounter = preparedStatement.executeUpdate();
	//			}
	//		}
	//		EQCType.assertEqual(rowCounter, ONE_ROW);
	//		return true;
	//	}

	@Override
	public boolean saveLockMateSnapshot(final LockMate lockMate, final ID height) throws SQLException, Exception {
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
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public boolean savePassport(final Passport passport) throws Exception {
		int rowCounter = 0;
		ID lastPassportId = null;
		if (isPassportExists(passport.getId())) {
			final StringBuilder sb = new StringBuilder();
			int index = 0;
			sb.append("UPDATE ");
			sb.append(PASSPORT_TABLE);
			sb.append(" SET ");
			if (passport.isTypeUpdate()) {
				sb.append(PassportTable.TYPE);
				sb.append("=?");
			}
			if (passport.isLockIDUpdate()) {
				sb.append(",");
				sb.append(PassportTable.ID);
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
				final ExpendablePassport expendablePassport = (ExpendablePassport) passport;
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
					final ExpendablePassport expendablePassport = (ExpendablePassport) passport;
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
					+ " (" + PassportTable.TYPE + "," + PassportTable.ID + "," + PassportTable.LOCK_ID + ","
					+ PassportTable.BALANCE + "," + PassportTable.NONCE + "," + PassportTable.UPDATE_HEIGHT + ","
					+ PassportTable.STORAGE
					+ ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
				preparedStatement.setShort(1, (short) passport.getType().ordinal());
				preparedStatement.setLong(2, passport.getId().longValue());
				preparedStatement.setLong(3, passport.getLockID().longValue());
				preparedStatement.setLong(4, passport.getBalance().longValue());
				preparedStatement.setLong(5, passport.getNonce().longValue());
				preparedStatement.setLong(6, passport.getUpdateHeight().longValue());
				if (passport instanceof ExpendablePassport) {
					final ExpendablePassport expendablePassport = (ExpendablePassport) passport;
					preparedStatement.setBytes(7, expendablePassport.getStorage().getBytes());
				} else {
					preparedStatement.setNull(7, Types.NULL);
				}
				rowCounter = preparedStatement.executeUpdate();
			}
		}
		EQCCastle.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public synchronized boolean savePassportSnapshot(final Passport passport, final ID height) throws Exception {
		int rowCounter = 0;
		try(PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO " + PassportTable.PASSPORT_SNAPSHOT + "(" + PassportTable.TYPE + "," + PassportTable.ID
				+ "," + PassportTable.LOCK_ID + "," + PassportTable.BALANCE + "," + PassportTable.NONCE + ","
				+ PassportTable.STORAGE + "," + PassportTable.SNAPSHOT_HEIGHT
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			preparedStatement.setByte(1, (byte) passport.getType().ordinal());
			preparedStatement.setLong(2, passport.getId().longValue());
			preparedStatement.setLong(3, passport.getLockID().longValue());
			preparedStatement.setLong(4, passport.getBalance().longValue());
			preparedStatement.setLong(5, passport.getNonce().longValue());
			if(passport instanceof ExpendablePassport) {
				final ExpendablePassport expendablePassport = (ExpendablePassport) passport;
				preparedStatement.setBytes(6, expendablePassport.getStorage().getBytes());
			}
			else {
				preparedStatement.setNull(6, Types.NULL);
			}
			preparedStatement.setLong(7, height.longValue());
			rowCounter = preparedStatement.executeUpdate();
			EQCCastle.assertEqual(rowCounter, ONE_ROW);
		}
		return rowCounter == ONE_ROW;
	}

	@Override
	public Savepoint setSavepoint() throws Exception {
		return connection.setSavepoint();
	}

	@Override
	public <T> void updateGlobalState(final EQCHive eqcHive, final Savepoint savepoint, final T checkPoint)
			throws Exception {
		try {
			saveEQCHive(eqcHive);
			// takeSnapshot();
			// merge();
			// clear();
			saveEQCHiveTailHeight(eqcHive.getRoot().getHeight());
			if (savepoint != null) {
				Log.info("Begin commit at EQCHive No." + eqcHive.getRoot().getHeight() + " check point: " + checkPoint);
				commit(checkPoint);
				Log.info("Commit successful at EQCHive No." + eqcHive.getRoot().getHeight());
			}
			try {
				Util.MC().deleteTransactionsInPool(eqcHive);
			} catch (final Exception e) {
				Log.Error(e.getMessage());
			}
		} catch (final Exception e) {
			Log.Error("During update global state error occur: " + e + " savepoint: " + savepoint);
			if (savepoint != null) {
				Log.info("Begin rollback at EQCHive No." + eqcHive.getRoot().getHeight());
				rollback(savepoint);
				Log.info("Rollback successful at EQCHive No." + eqcHive.getRoot().getHeight());
			}
			throw e;
		} finally {
			if (savepoint != null) {
				releaseSavepoint(savepoint);
			}
		}
	}

}
