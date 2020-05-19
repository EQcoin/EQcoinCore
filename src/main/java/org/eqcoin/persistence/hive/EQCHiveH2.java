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
package org.eqcoin.persistence.hive;

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
import org.eqcoin.transaction.Value;
import org.eqcoin.transaction.ZionTxOut;
import org.eqcoin.transaction.Transaction.TransactionType;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

import org.eqcoin.avro.O;
import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.T1Lock;
import org.eqcoin.lock.T1Publickey;
import org.eqcoin.lock.T2Lock;
import org.eqcoin.lock.T2Publickey;
import org.eqcoin.passport.ExpendablePassport;
import org.eqcoin.passport.Passport;
import org.eqcoin.passport.Passport.PassportType;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.lock.Publickey;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.SPList;
import org.eqcoin.rpc.TransactionIndex;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.seed.EQcoinSeedRoot;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class EQCHiveH2 implements IEQCHive {
	private static final String JDBC_URL = "jdbc:h2:" + Util.H2_DATABASE_NAME;
	private static final String USER = "Believer";
	private static final String PASSWORD = "God bless us...";
	private static final String DRIVER_CLASS = "org.h2.Driver";
	private static Connection connection;
	private static Connection connectionAutoCommit;
//	private static Statement statement;
	private static EQCHiveH2 instance;
	private static final int ONE_ROW = 1;
	private static final String LOCK_GLOBAL = "LOCK_GLOBAL";
	private static final String LOCK_MINING = "LOCK_MINING";
	private static final String LOCK_VALID = "LOCK_VALID";
	private static final String PASSPORT_GLOBAL = "PASSPORT_GLOBAL";
	private static final String PASSPORT_MINING = "PASSPORT_MINING";
	private static final String PASSPORT_VALID = "PASSPORT_VALID";
	
//	public enum STATUS {
//		BEGIN, END
//	}
	
//	public enum TRANSACTION_OP {
//		TXIN, TXOUT, PASSPORT, PUBLICKEY, ADDRESS, TXFEERATE, CHECKPOINT, INVALID;
//		public static TRANSACTION_OP get(int ordinal) {
//			TRANSACTION_OP op = null;
//			switch (ordinal) {
//			case 0:
//				op = TRANSACTION_OP.TXIN;
//				break;
//			case 1:
//				op = TRANSACTION_OP.TXOUT;
//				break;
//			case 2:
//				op = TRANSACTION_OP.PASSPORT;
//				break;
//			case 3:
//				op = TRANSACTION_OP.PUBLICKEY;
//				break;
//			case 4:
//				op = TRANSACTION_OP.ADDRESS;
//				break;
//			case 5:
//				op = TRANSACTION_OP.TXFEERATE;
//				break;
//			case 6:
//				op = TRANSACTION_OP.CHECKPOINT;
//				break;
//			default:
//				op = TRANSACTION_OP.INVALID;
//				break;
//			}
//			return op;
//		}
//		public boolean isSanity() {
//			if((this.ordinal() < TXIN.ordinal()) || (this.ordinal() >= INVALID.ordinal())) {
//				return false;
//			}
//			return true;
//		}
//		public byte[] getEQCBits() {
//			return EQCType.intToEQCBits(this.ordinal());
//		}
//	}
	
	private String createLockTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id >= 0,"
				+ "type TINYINT NOT NULL CHECK type >= 0,"
				+ "proof BINARY(64) NOT NULL UNIQUE,"
				+ "publickey BINARY(67) UNIQUE,"
				+ "code BINARY UNIQUE"
				+ ")";
	}
	
	private String createPassportTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id >= 0,"
				+ "lock_id BIGINT NOT NULL UNIQUE CHECK lock_id >= 0,"
				+ "type TINYINT NOT NULL CHECK type >= 0,"
				+ "balance BIGINT NOT NULL CHECK balance >= 510000,"
				+ "nonce BIGINT NOT NULL CHECK nonce >= 0,"
				+ "storage BINARY,"
				+ "state_proof BINARY"
				+ ")";
	}
	
	private EQCHiveH2() throws ClassNotFoundException, SQLException {
//			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			connectionAutoCommit = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			connectionAutoCommit.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
//			Statement statement = connection.createStatement();
//			statement.execute("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL SNAPSHOT");
//			statement.close();
//			connection.setAutoCommit(false);
//			connection.commit();
			createTable();
	}
	
	@Override
	public synchronized boolean dropTable() throws SQLException {
			Statement statement = connection.createStatement();
			statement.execute("DROP TABLE ACCOUNT");
			statement.execute("DROP TABLE PUBLICKEY");
			statement.execute("DROP TABLE TRANSACTION");
			statement.execute("DROP TABLE TRANSACTIONS_HASH");
			statement.execute("DROP TABLE SIGNATURE_HASH");
			statement.execute("DROP TABLE SYNCHRONIZATION");
			statement.execute("DROP TABLE TRANSACTION_POOL");
			statement.execute("DROP TABLE TXIN_HEADER_HASH");
			statement.execute("DROP TABLE ACCOUNT_SNAPSHOT");
			statement.close();
//			statement.execute("DROP TABLE ");
			return true; // Here need do more job
	}
	
	public synchronized void createTable() throws SQLException {
//		connection.setAutoCommit(true);
		Statement statement = connection.createStatement();
		   	// Create Lock table.
		boolean result = statement.execute(createLockTable(getLockTableName(Mode.GLOBAL)));
		
		 result = statement.execute(createLockTable(getLockTableName(Mode.MINING)));
		 
		 result = statement.execute(createLockTable(getLockTableName(Mode.VALID)));
		 
			// Create Account table. Each Account should be unique and it's Passport's ID should be one by one
			result = statement.execute(createPassportTable(getPassportTableName(Mode.GLOBAL)));
			
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.MINING)));
			 
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.VALID)));
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS EQCHIVE("
						+ "height BIGINT  NOT NULL CHECK height >= 0,"
						+ "eqchive_root BINARY NOT NULL UNIQUE,"
						+ "eqcoinseed_root BINARY NOT NULL UNIQUE,"
						+ "seeds BINARY NOT NULL"
						+ ")");
			 
			// EQCHive tail
			statement.execute("CREATE TABLE IF NOT EXISTS SYNCHRONIZATION("
					+ "tail_height BIGINT"
					+ ")");
			
			// EQC Transaction Pool table
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_POOL("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "txin_id BIGINT,"
					+ "nonce BIGINT,"
					+ "rawdata BINARY,"
					+ "signature BINARY,"
					+ "proof BINARY(4),"
					+ "priority_value BIGINT,"
					+ "receieved_timestamp BIGINT,"
					+ "record_status BOOLEAN,"
					+ "record_height BIGINT"
					+ ")");
			
			// Create Passport snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS PASSPORT_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT NOT NULL,"
					+ "lock_id BIGINT NOT NULL,"
					+ "type TINYINT NOT NULL,"
					+ "balance BIGINT NOT NULL,"
					+ "nonce BIGINT NOT NULL,"
					+ "storage BINARY,"
					+ "state_proof BINARY,"
					+ "snapshot_height BIGINT NOT NULL"
					+ ")");
			
			// Create Lock snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS LOCK_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT NOT NULL,"
					+ "type TINYINT NOT NULL,"
					+ "proof BINARY(64) NOT NULL,"
					+ "publickey BINARY(67),"
					+ "code BINARY,"
					+ "snapshot_height BIGINT NOT NULL"
					+ ")");
			
			// Create EQcoin Network table
			statement.execute("CREATE TABLE IF NOT EXISTS SP_LIST ("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "ip  VARCHAR,"
					+ "flag TINYINT,"
					+ "protocol_version TINYINT,"
					+ "counter TINYINT,"
					+ "sync_time BIGINT"
					+ ")");
			
			connection.setAutoCommit(false);
			
			statement.close();
			
			if(result) {
				Log.info("Create table");
			}
	}
	
	public synchronized static EQCHiveH2 getInstance() throws ClassNotFoundException, SQLException {
		if(instance == null) {
			synchronized (EQCHiveH2.class) {
				if(instance == null) {
					instance = new EQCHiveH2();
				}
			}
		}
		return instance;
	}

	@Override
	public synchronized byte[] getEQCHive(ID height) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			os.write(resultSet.getBytes("eqchive_root"));
			os.write(resultSet.getBytes("eqcoinseed_root"));
			os.write(resultSet.getBytes("seeds"));
		}
		preparedStatement.close();
		return os.toByteArray();
	}

	@Override
	public synchronized boolean isEQCHiveExists(ID height) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		preparedStatement.close();
		return isExists;
	}

	@Override
	public synchronized boolean saveEQCHive(EQCHive eqcHive) throws Exception {
		Objects.requireNonNull(eqcHive);
		EQCHive eqcHive2 = null;
		int rowCounter = 0;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PreparedStatement preparedStatement = null;
		if (isEQCHiveExists(eqcHive.getHeight())) {
			preparedStatement = connection
					.prepareStatement("UPDATE EQCHIVE SET height = ?, eqchive_root = ?,  eqcoinseed_root = ?, seeds = ? WHERE height = ?");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getEQCHiveRoot().getBytes());
			preparedStatement.setBytes(3, eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getBytes());
			preparedStatement.setBytes(4, eqcHive.getEQcoinSeed().getBodyBytes(os).toByteArray());
			preparedStatement.setLong(5, eqcHive.getHeight().longValue());
			rowCounter = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + rowCounter);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO EQCHIVE (height, eqchive_root, eqcoinseed_root, seeds) VALUES (?, ?, ?, ?)");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getEQCHiveRoot().getBytes());
			preparedStatement.setBytes(3, eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getBytes());
			preparedStatement.setBytes(4, eqcHive.getEQcoinSeed().getBodyBytes(os).toByteArray());
			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		}
//		connection.commit();
		EQCType.assertEqual(rowCounter, ONE_ROW);
		preparedStatement.close();
//		eqcHive2 = getEQCHive(eqcHive.getHeight(), false);
//		EQCType.assertEqual(eqcHive.getBytes(), eqcHive2.getBytes());
//		byte[] eqcHeaderHash = getEQCHeaderHash(eqcHive.getHeight());
//		EQCType.assertEqual(eqcHive2.getEqcHeader().getHash(), eqcHeaderHash);
		return true;
	}

	@Override
	public synchronized boolean deleteEQCHive(ID height) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM EQCHIVE WHERE height =?");
		preparedStatement.setLong(1, height.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		preparedStatement.close();
		return true;
	}

	@Override
	public synchronized boolean saveEQCHiveTailHeight(ID height) throws SQLException {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;;
			if (getEQCHiveTailHeight() != null) {
				preparedStatement = connection.prepareStatement("UPDATE SYNCHRONIZATION SET tail_height=?");
				preparedStatement.setLong(1, height.longValue());
			} else {
				preparedStatement = connection.prepareStatement("INSERT INTO SYNCHRONIZATION(tail_height) VALUES(?)");
				preparedStatement.setLong(1, height.longValue());
			}
			rowCounter = preparedStatement.executeUpdate();
			EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public synchronized ID getEQCHiveTailHeight() throws SQLException {
		ID id = null;
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNCHRONIZATION");
		if (resultSet.next()) {
			id = new ID(BigInteger.valueOf(resultSet.getLong("tail_height")));
		}
		statement.close();
		return id;
	}

	@Override
	public synchronized byte[] getEQCHiveRootProof(ID height) throws Exception {
		byte[] hash = null;
		EQCHiveRoot eqcHiveRoot = null;
		eqcHiveRoot = getEQCHiveRoot(height.getNextID());
		if(eqcHiveRoot != null) {
			hash = eqcHiveRoot.getPreProof();
		}
		else {
			eqcHiveRoot = getEQCHiveRoot(height);
			if(eqcHiveRoot != null) {
				hash = eqcHiveRoot.getProof();
			}
		}
		return hash;
	}

	@Override
	public synchronized Vector<Transaction> getTransactionListInPool()
			throws Exception {
		Vector<Transaction> transactions = new Vector<Transaction>();
		ByteArrayInputStream is = null;
		long currentTime = System.currentTimeMillis();
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE priority_value>='5' OR " + "(priority_value='4') OR "
						+ "(priority_value='3' AND receieved_timestamp<=?) OR " + "(priority_value='2' AND receieved_timestamp<=?) OR "
						+ "(priority_value='1' AND receieved_timestamp<=?) AND "
						+ "(record_status = FALSE) ORDER BY priority_value DESC, receieved_timestamp ASC");
		preparedStatement.setLong(1, (currentTime - 200000));
		preparedStatement.setLong(2, (currentTime - 400000));
		preparedStatement.setLong(3, (currentTime - 600000));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
//			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
//
//			// Parse Transaction
//			Transaction transaction = Transaction.parseTransaction(EQCType.parseBIN(is), Passport.AddressShape.READABLE);
//			// Parse PublicKey
//			PublicKey publickey = new PublicKey();
//			publickey.setPublicKey(EQCType.parseBIN(is));
//			transaction.setPublickey(publickey);
//
//			// Parse Signature
//			transaction.setSignature(EQCType.parseBIN(is));
			try {
				// Parse Transaction
				transactions.add(new Transaction().Parse(resultSet.getBytes("rawdata")));
			} catch (Exception e) {
				Log.Error("During parse transaction error occur have to delete it: " + e.getMessage());
//				deleteTransactionInPool(resultSet.getBytes("signature"));
			}
		}
//		Collections.sort(transactions);
		return transactions;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND priority_value<=?");
		preparedStatement.setLong(1, transaction.getTxIn().getPassportId().longValue());
		preparedStatement.setLong(2, transaction.getNonce().longValue());
		preparedStatement.setLong(3, transaction.getPriorityValue().longValue());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws SQLException {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND proof=?");
		preparedStatement.setLong(1, transactionIndex.getId().longValue());
		preparedStatement.setLong(2, transactionIndex.getNonce().longValue());
		preparedStatement.setBytes(3, transactionIndex.getProof());
		resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	/**
	 * @param transaction EQC Transaction include PublicKey and Signature so for
	 *                    every Transaction it's raw is unique
	 * @return boolean If add Transaction successful return true else return false
	 * @throws Exception 
	 */
	@Override
	public synchronized boolean saveTransactionInPool(Transaction transaction) throws Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (!isTransactionExistsInPool(transaction)) {
			preparedStatement = connectionAutoCommit.prepareStatement(
					"INSERT INTO TRANSACTION_POOL (txin_id, nonce, rawdata, signature, proof, priority_value, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, transaction.getTxIn().getPassportId().longValue());
			preparedStatement.setLong(2, transaction.getNonce().longValue());
			preparedStatement.setBytes(3, transaction.getBytes());
			preparedStatement.setBytes(4, transaction.getEqcWitness().getSignature());
			preparedStatement.setBytes(5, transaction.getProof());
			preparedStatement.setLong(6, transaction.getPriorityValue().longValue());
			preparedStatement.setLong(7, System.currentTimeMillis());
			preparedStatement.setBoolean(8, false);
			preparedStatement.setNull(9, Types.BIGINT);
			result = preparedStatement.executeUpdate();
		} else {
			preparedStatement = connectionAutoCommit.prepareStatement(
					"UPDATE TRANSACTION_POOL SET rawdata=?, signature=?, proof=?, priority_value=?, receieved_timestamp=?, record_status=?, record_height=? WHERE txin_id=? AND nonce=?");
			preparedStatement.setBytes(1, transaction.getBytes());
			preparedStatement.setBytes(2, transaction.getEqcWitness().getSignature());
			preparedStatement.setBytes(3, transaction.getProof());
			preparedStatement.setLong(4, transaction.getPriorityValue().longValue());
			preparedStatement.setLong(5, System.currentTimeMillis());
			preparedStatement.setBoolean(6, false);
			preparedStatement.setNull(7, Types.BIGINT);
			preparedStatement.setLong(8, transaction.getTxIn().getPassportId().longValue());
			preparedStatement.setLong(9, transaction.getNonce().longValue());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
		if(Util.IsDeleteTransactionInPool) {
			PreparedStatement preparedStatement = connection
					.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE signature= ?");
			preparedStatement.setBytes(1, transaction.getEqcWitness().getSignature());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}
	
	@Override
	public synchronized boolean deleteTransactionsInPool(EQCHive eqcBlock)
			throws ClassNotFoundException, Exception {
		int isSuccessful = 0;
		for (Transaction transaction : eqcBlock.getEQcoinSeed().getNewTransactionList()) {
			if (deleteTransactionInPool(transaction)) {
				++isSuccessful;
			}
		}
		return isSuccessful == eqcBlock.getEQcoinSeed().getNewTransactionList().size();
	}

	@Override
	public synchronized boolean close() throws SQLException {
		boolean boolResult = true;
		if (connection != null) {
			connection.close();
			connection = null;
		}
		return boolResult;
	}

	public boolean savePassport(Passport passport, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isPassportExists(passport.getId(), mode)) {
			preparedStatement = connection.prepareStatement(
					"UPDATE " + getPassportTableName(mode) + " SET id = ?, lock_id = ?, type = ?, balance = ?, nonce = ?, storage = ?, state_proof = ? WHERE id = ?");
			preparedStatement.setLong(8, passport.getId().longValue());
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO " + getPassportTableName(mode) + "  (id, lock_id, type, balance, nonce, storage, state_proof) VALUES (?, ?, ?, ?, ?, ?, ?)");
		}
		preparedStatement.setLong(1, passport.getId().longValue());
		preparedStatement.setLong(2, passport.getLockID().longValue());
		preparedStatement.setByte(3, (byte) passport.getType().ordinal());
		preparedStatement.setLong(4, passport.getBalance().longValue());
		preparedStatement.setLong(5, passport.getNonce().longValue());
		if(passport.getType() == PassportType.EXTENDABLE || passport.getType() == PassportType.EQCOINROOT ) {
			ExpendablePassport expendablePassport = (ExpendablePassport) passport;
			preparedStatement.setBytes(6, expendablePassport.getStorageBytes());
		}
		else {
			preparedStatement.setNull(6, Types.NULL);
		}
		preparedStatement.setNull(7, Types.NULL);
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}
	
	public boolean savePassport(ResultSet resultSet, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isPassportExists(new ID(resultSet.getLong("id")), mode)) {
			preparedStatement = connection.prepareStatement(
					"UPDATE " + getPassportTableName(mode) + " SET id = ?, lock_id = ?, type = ?, balance = ?, nonce = ?, storage = ?, state_proof = ? WHERE id = ?");
			preparedStatement.setLong(8, resultSet.getLong("id"));
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO " + getPassportTableName(mode) + "  (id, lock_id, type, balance, nonce, storage, state_proof) VALUES (?, ?, ?, ?, ?, ?, ?)");
		}
		preparedStatement.setLong(1, resultSet.getLong("id"));
		preparedStatement.setLong(2, resultSet.getLong("lock_id"));
		preparedStatement.setByte(3,  resultSet.getByte("type"));
		preparedStatement.setLong(4, resultSet.getLong("balance"));
		preparedStatement.setLong(5, resultSet.getLong("nonce"));
		preparedStatement.setBytes(6, resultSet.getBytes("storage"));
		preparedStatement.setBytes(7, resultSet.getBytes("state_proof"));
		
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public synchronized Passport getPassportSnapshot(ID passportID, ID height)
			throws ClassNotFoundException, Exception {
		Passport passport = null;
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM PASSPORT_SNAPSHOT WHERE id=? AND snapshot_height >? ORDER BY snapshot_height LIMIT 1");
		preparedStatement.setLong(1, passportID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet);
		}

		return passport;
	}

	@Override
	public synchronized boolean savePassportSnapshot(Passport passport, ID height) throws Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(
					"INSERT INTO PASSPORT_SNAPSHOT (id, lock_id, type, balance, nonce, storage, state_proof, snapshot_height) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, passport.getId().longValue());
			preparedStatement.setLong(2, passport.getLockID().longValue());
			preparedStatement.setByte(3, (byte) passport.getType().ordinal());
			preparedStatement.setLong(4, passport.getBalance().longValue());
			preparedStatement.setLong(5, passport.getNonce().longValue());
			if(passport.getType() == PassportType.EXTENDABLE || passport.getType() == PassportType.EQCOINROOT ) {
				ExpendablePassport expendablePassport = (ExpendablePassport) passport;
				preparedStatement.setBytes(6, expendablePassport.getStorageBytes());
			}
			else {
				preparedStatement.setNull(6, Types.NULL);
			}
			preparedStatement.setNull(7, Types.NULL);
			preparedStatement.setLong(8, height.longValue());
			result = preparedStatement.executeUpdate();
			
			EQCType.assertEqual(result, ONE_ROW);
//				Log.info("INSERT Account ID: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException {
		// Here need do more job first should get all the numbers need to be remove then check if the altered lines number is equal to what it should be
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM PASSPORT_SNAPSHOT WHERE snapshot_height " + (isForward ? ">=?" : "<=?"));
		preparedStatement.setLong(1, height.longValue());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}

	@Override
	public boolean isSPExists(SP sp) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=? AND flag=?");
			preparedStatement.setString(1, sp.getIp());
			preparedStatement.setInt(2, sp.getFlag().intValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public boolean saveSP(SP sp) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isSPExists(sp)) {
			preparedStatement = connection.prepareStatement("UPDATE SP_LIST SET flag = ?, protocol_version = ?, counter = ? where ip = ?");
			preparedStatement.setByte(1, sp.getFlag().byteValue());
			preparedStatement.setByte(2, sp.getProtocolVersion().byteValue());
			preparedStatement.setByte(3, (byte) 0);
			preparedStatement.setString(4, sp.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO SP_LIST (ip, flag, protocol_version, counter, sync_time) VALUES (?, ?, ?, ?, ?)");
			preparedStatement.setString(1, sp.getIp());
			preparedStatement.setByte(2, sp.getFlag().byteValue());
			preparedStatement.setByte(3, sp.getProtocolVersion().byteValue());
			preparedStatement.setByte(4, (byte) 0);
			preparedStatement.setLong(5, 0);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	public boolean deleteSP(SP sp) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, sp.getIp());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public SPList getSPList(ID flag) throws SQLException, Exception {
		SPList spList = new SPList();
		SP sp = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		byte flagValue = flag.byteValue();
		if(flagValue == 3 || flagValue == 5 || flagValue == 6 || flagValue == 7) {
			preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE flag=?");
			preparedStatement.setByte(1, flagValue);
		}
		else {
			if(flagValue == 1) {
				preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE flag=? or flag=? or flag=?  or flag=?");
				preparedStatement.setByte(1, (byte) 1);
				preparedStatement.setByte(2, (byte) 3);
				preparedStatement.setByte(3, (byte) 5);
				preparedStatement.setByte(4, (byte) 7);
			}
			else if(flagValue == 2) {
				preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE flag=? or flag=? or flag=?  or flag=?");
				preparedStatement.setByte(1, (byte) 2);
				preparedStatement.setByte(2, (byte) 3);
				preparedStatement.setByte(3, (byte) 6);
				preparedStatement.setByte(4, (byte) 7);
			}
			else if(flagValue == 4) {
				preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE flag=? or flag=? or flag=?  or flag=?");
				preparedStatement.setByte(1, (byte) 4);
				preparedStatement.setByte(2, (byte) 5);
				preparedStatement.setByte(3, (byte) 6);
				preparedStatement.setByte(4, (byte) 7);
			}
		}
		resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			sp = new SP();
			sp.setFlag(new ID(resultSet.getByte("flag")));
			sp.setIp(resultSet.getString("ip"));
			sp.setProtocolVersion(new ID(resultSet.getByte("protocol_version")));
			spList.addSP(sp);
		}
		return spList;
	}

	@Override
	public Vector<Transaction> getPendingTransactionListInPool(ID id) throws SQLException, Exception {
		Vector<Transaction> transactionList = new Vector<>();
//		Transaction transaction = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=?");
//		preparedStatement.setLong(1, nest.getId().longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			transaction = Transaction.parseTransaction(resultSet.getBytes("rawdata"));
//			transaction.getTxIn().setPassportId(nest.getId());
//			transactionList.add(transaction);
//		}
		return transactionList;
	}

	@Override
	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws SQLException, Exception {
		TransactionIndexList transactionIndexList = new TransactionIndexList();
		TransactionIndex transactionIndex = null;
		transactionIndexList.setSyncTime(new ID(currentSyncTime));
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT txin_id, nonce, proof FROM TRANSACTION_POOL WHERE receieved_timestamp>=? AND receieved_timestamp<?");
		preparedStatement.setLong(1, previousSyncTime);
		preparedStatement.setLong(2, currentSyncTime);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transactionIndex = new TransactionIndex();
			transactionIndex.setId(new ID(resultSet.getLong("txin_id")));
			transactionIndex.setNonce(new ID(resultSet.getLong("nonce")));
			transactionIndex.setProof(resultSet.getBytes("proof"));
			transactionIndexList.addTransactionIndex(transactionIndex);
		}
		return transactionIndexList;
	}

	private Transaction getTransactionInPool(TransactionIndex transactionIndex) throws SQLException, Exception {
		Transaction transaction = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND proof=?");
//		preparedStatement.setLong(1, transactionIndex.getId().longValue());
//		preparedStatement.setLong(2, transactionIndex.getNonce().longValue());
//		preparedStatement.setBytes(3, transactionIndex.getProof());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			transaction = Transaction.parseTransaction(resultSet.getBytes("rawdata"));
//		}
		return transaction;
	}

	@Override
	public boolean saveSyncTime(SP sp, ID syncTime) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isSPExists(sp)) {
			preparedStatement = connection.prepareStatement("UPDATE SP_LIST SET flag = ?, sync_time = ? where ip = ?");
			preparedStatement.setByte(1, sp.getFlag().byteValue());
			preparedStatement.setLong(2, syncTime.longValue());
			preparedStatement.setString(3, sp.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO SP_LIST (ip, flag, counter, sync_time) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, sp.getIp());
			preparedStatement.setByte(2, sp.getFlag().byteValue());
			preparedStatement.setByte(3, (byte) 0);
			preparedStatement.setLong(4, syncTime.longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public ID getSyncTime(SP sp) throws SQLException, Exception {
		ID sync_time = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, sp.getIp());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			sync_time = new ID(resultSet.getLong("sync_time"));
		}
		return sync_time;
	}

	@Override
	public boolean saveSPCounter(SP sp, byte counter) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isSPExists(sp)) {
			preparedStatement = connection.prepareStatement("UPDATE SP_LIST SET flag = ?, counter = ? where ip = ?");
			preparedStatement.setByte(1, sp.getFlag().byteValue());
			preparedStatement.setByte(2, counter);
			preparedStatement.setString(3, sp.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection.prepareStatement("INSERT INTO SP_LIST (ip, flag, counter, sync_time) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, sp.getIp());
			preparedStatement.setByte(2, sp.getFlag().byteValue());
			preparedStatement.setByte(3, counter);
			preparedStatement.setLong(4, 0);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public byte getSPCounter(SP ip) throws SQLException, Exception {
		byte counter = 0;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, ip.getIp());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			counter = resultSet.getByte("counter");
		}
		return counter;
	}

	@Override
	public Passport getPassport(ID id, Mode mode) throws Exception {
		Passport passport = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet);
		}
		return passport;
	}
	
	@Override
	public boolean isPassportExists(ID id, Mode mode) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id = ?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}
	
	@Override
	public Passport getPassport(ID id, ID height) throws Exception {
		EQCType.assertNotBigger(height, getEQCHiveTailHeight());
		Passport passport = null;
		ID tailHeight = getEQCHiveTailHeight();
		if (height.equals(tailHeight)) {
			passport = getPassport(id, Mode.GLOBAL);
		} else {
			// If from check point height to tail height the relevant passport was changed
			// will get it from snapshot.
			passport = getPassportSnapshot(id, height);
			if (passport == null) {
				// If can't find the relevant passport's snapshot which means the passport
				// hasn't any change since check point height just retrieve it from the global
				// state.
				passport = getPassport(id, Mode.GLOBAL);
			}
		}
		return passport;
	}
	
	public boolean deletePassport(ID id, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM " + getPassportTableName(mode) + " WHERE id =?");
		preparedStatement.setLong(1, id.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	/* (non-Javadoc)
	 * Here only need clear PASSPORT_MINING or PASSPORT_VALID
	 * @see com.eqcoin.persistence.EQCBlockChain#clear(com.eqcoin.blockchain.accountsmerkletree.Filter.Mode)
	 */
	@Override
	public boolean clearPassport(Mode mode) throws Exception {
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM " + getPassportTableName(mode));
		preparedStatement.executeUpdate();
		return true;
	}

	/* (non-Javadoc)
	 * Here only need merge from ACCOUNT_MINING or ACCOUNT_VALID to ACCOUNT
	 * @see com.eqcoin.persistence.EQCBlockChain#merge(com.eqcoin.blockchain.accountsmerkletree.Filter.Mode)
	 */
	@Override
	public boolean mergePassport(Mode mode) throws SQLException, Exception {
		Passport passport = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getPassportTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			savePassport(resultSet, Mode.GLOBAL);
		}
		return true;
	}

	@Override
	public boolean takePassportSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		Passport passport = null;
		ID id = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + ((mode == Mode.MINING) ? "PASSPORT_MINING" : "PASSPORT_VALID") + " WHERE id <?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalPassportNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			id = new ID(resultSet.getLong("id"));
			passport = getPassport(id, mode.GLOBAL);
			savePassportSnapshot(passport, changeLog.getHeight());
		}
		return true;
	}


	public synchronized EQCHive getEQCHiveFile(ID height, boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				eqcHive = new EQCHive(is.readAllBytes());
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
		}
		return eqcHive;
	}

	public synchronized boolean isEQCBlockExistsFile(ID height) {
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

	public synchronized EQCHiveRoot getEQCHeaderFile(ID height) throws Exception {
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
				// TODO Auto-generated catch block
				e.printStackTrace();
				eqcHiveRoot = null;
				Log.Error(e.getMessage());
			}
		}
		return eqcHiveRoot;
	}
	
	private String getLockTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = LOCK_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = LOCK_MINING;
		}
		else if(mode == Mode.VALID) {
			table = LOCK_VALID;
		}
		return table;
	}
	
	private String getPassportTableName(Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = PASSPORT_GLOBAL;
		}
		else if(mode == Mode.MINING) {
			table = PASSPORT_MINING;
		}
		else if(mode == Mode.VALID) {
			table = PASSPORT_VALID;
		}
		return table;
	}

	@Override
	public  TransactionList getTransactionListInPool(TransactionIndexList transactionIndexList)
			throws SQLException, Exception {
		TransactionList transactionList = new TransactionList();
		for (TransactionIndex transactionIndex : transactionIndexList.getTransactionIndexList()) {
			transactionList.addTransaction(getTransactionInPool(transactionIndex));
		}
		return transactionList;
	}
	
	@Override
	public boolean saveLock(LockMate eqcLockMate, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isLockExists(eqcLockMate.getId(), mode)) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getLockTableName(mode)
					+ " SET id = ?,  type = ?, proof = ?, publickey = ?, code = ? WHERE id = ?");
			preparedStatement.setLong(6, eqcLockMate.getId().longValue());
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getLockTableName(mode)
							+ " (id, type, proof, publickey, code) VALUES (?, ?, ?, ?, ?)");
		}
		
		preparedStatement.setLong(1, eqcLockMate.getId().longValue());
		preparedStatement.setByte(2, (byte) eqcLockMate.getLock().getLockType().ordinal());
		preparedStatement.setBytes(3, eqcLockMate.getLock().getLockProof());
		if(eqcLockMate.getPublickey().isNULL()) {
			preparedStatement.setNull(4, Types.BINARY);
		}
		else {
			preparedStatement.setBytes(4, eqcLockMate.getPublickey().getBytes());
		}
		if(eqcLockMate.getLock() instanceof T1Lock || eqcLockMate.getLock() instanceof T2Lock) {
			preparedStatement.setNull(5, Types.BINARY);
		}
		else {
			throw new IllegalStateException("Wrong lock type");
		}
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public LockMate getLock(ID id, Mode mode) throws Exception {
		LockMate lockMate = null;
		byte[] publickey = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getLockTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lockMate = new LockMate();
			lockMate.setId(new ID(resultSet.getLong("id")));
			LockType lockType = LockType.get(resultSet.getByte("type"));
			Lock lock = null;
			if(lockType == LockType.T1) {
				lock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				lock = new T2Lock();
			}
			lockMate.setLock(lock);
			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
			publickey = resultSet.getBytes("publickey");
			if (publickey == null) {
				lockMate.setPublickey(new Publickey());
			} else {
				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
			}
		}
		return lockMate;
	}

	@Override
	public LockMate getLock(ID id, ID height) throws Exception {
		LockMate lock = null;
		if (height.equals(getEQCHiveTailHeight())) {
			lock = getLock(id, Mode.GLOBAL);
		} else {
			lock = getLockSnapshot(id, height);
		}
		return lock;
	}

	@Override
	public boolean deleteLock(ID id, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM " + getLockTableName(mode) + " WHERE id =?");
		preparedStatement.setLong(1, id.longValue());
		rowCounter = preparedStatement.executeUpdate();
		Log.info("rowCounter: " + rowCounter);
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public boolean clearLock(Mode mode) throws Exception {
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM " + getLockTableName(mode));
		preparedStatement.executeUpdate();
		return true;
	}

	@Override
	public LockMate getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		LockMate lockMate = null;
		LockType lockType = null;
		Lock lock = null;
		byte[] publickey = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM LOCK_SNAPSHOT WHERE id=? AND snapshot_height >? ORDER BY snapshot_height LIMIT 1");
		preparedStatement.setLong(1, lockID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lockMate = new LockMate();
			lockMate.setId(new ID(resultSet.getLong("id")));
			lockType = LockType.get(resultSet.getShort("type"));
			if(lockType == LockType.T1) {
				lock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				lock = new T2Lock();
			}
			lockMate.setLock(lock);
			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
			publickey = resultSet.getBytes("publickey");
			if (publickey == null) {
				lockMate.setPublickey(new Publickey());
			} else {
				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
			}
		}

		return lockMate;
	}

	@Override
	public boolean saveLockSnapshot(LockMate lock, ID height) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(
					"INSERT INTO LOCK_SNAPSHOT (id, type, proof, publickey, code, snapshot_height) VALUES (?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, lock.getId().longValue());
			preparedStatement.setByte(2, (byte) lock.getLock().getLockType().ordinal());
			preparedStatement.setBytes(3, lock.getLock().getLockProof());
			if(lock.getPublickey().isNULL()) {
				preparedStatement.setNull(4, Types.BINARY);
			}
			else {
				preparedStatement.setBytes(4, lock.getPublickey().getBytes());
			}
			if(lock.getLock() instanceof T1Lock || lock.getLock() instanceof T2Lock) {
				preparedStatement.setNull(5, Types.BINARY);
			}
			else {
				throw new IllegalStateException("Wrong lock type");
			}
			preparedStatement.setLong(6, height.longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT Account ID: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteLockSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM LOCK_SNAPSHOT WHERE snapshot_height " + (isForward ? ">=?" : "<=?"));
		preparedStatement.setLong(1, height.longValue());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public boolean mergeLock(Mode mode) throws SQLException, Exception {
		LockMate lockMate;
		LockType lockType = null;
		Lock lock = null;
		byte[] publickey;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getLockTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			lockMate = null;
			lockMate = new LockMate();
			lockMate.setId(new ID(resultSet.getLong("id")));
			lockType = LockType.get(resultSet.getByte("type"));
			if(lockType == LockType.T1) {
				lock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				lock = new T2Lock();
			}
			lockMate.setLock(lock);
			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
			publickey = resultSet.getBytes("publickey");
			if (publickey == null) {
				lockMate.setPublickey(new Publickey());
			} else {
				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
			}
 			saveLock(lockMate, Mode.GLOBAL);
		}
		return true;
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public ID getTotalLockNumbers(ID height) throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		ID tailHeight = getEQCHiveTailHeight();
		if(height.compareTo(tailHeight) > 0) {
			throw new IllegalStateException("");
		}
		else if(height.equals(tailHeight)) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ LOCK_GLOBAL);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		else {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ LOCK_GLOBAL + " WHERE id < ?");
			EQcoinSeedRoot eQcoinSeedRoot = getEQcoinSeedRoot(height);
			preparedStatement.setLong(1, eQcoinSeedRoot.getTotalLockNumbers().longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewLockNumbers;
	}

	@Override
	public ID getTotalNewLockNumbers(ChangeLog changeLog) throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
				+ getLockTableName(changeLog.getFilter().getMode()) + " WHERE id >= ?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalLockNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			totalNewLockNumbers = new ID(resultSet.getLong(1));
		}
		return totalNewLockNumbers;
	}

	@Override
	public ID getTotalPassportNumbers(ID height) throws Exception {
		ID totalNewPassportNumbers = ID.ZERO;
		ID tailHeight = getEQCHiveTailHeight();
		if(height.compareTo(tailHeight) > 0) {
			throw new IllegalStateException("");
		}
		else if(height.equals(tailHeight)) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ PASSPORT_GLOBAL);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		else {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ PASSPORT_GLOBAL + " WHERE id < ?");
			EQcoinSeedRoot eQcoinSeedRoot = getEQcoinSeedRoot(height);
			preparedStatement.setLong(1, eQcoinSeedRoot.getTotalPassportNumbers().longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewPassportNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewPassportNumbers;
	}

	@Override
	public ID getTotalNewPassportNumbers(ChangeLog changeLog) throws Exception {
		ID totalNewPassportNumbers = ID.ZERO;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
				+ getPassportTableName(changeLog.getFilter().getMode()) + " WHERE id >= ?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalPassportNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			totalNewPassportNumbers = new ID(resultSet.getLong(1));
		}
		return totalNewPassportNumbers;
	}

	@Override
	public boolean isLockExists(ID id, Mode mode) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getLockTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isExists = true;
		}
		return isExists;
	}

	@Override
	public ID isLockExists(Lock lock, Mode mode) throws Exception {
		ID lockId = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getLockTableName(mode) + " WHERE proof=?");
		preparedStatement.setBytes(1, lock.getLockProof());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lockId = new ID(resultSet.getLong("id"));
		}
		return lockId;
	}

	@Override
	public LockMate getLock(Lock lock, Mode mode) throws Exception {
		LockMate lockMate = null;
		Lock lock1 = null;
		byte[] publickey = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getLockTableName(mode) + " WHERE type=? AND proof=?");
		preparedStatement.setByte(1, (byte) lock.getLockType().ordinal());
		preparedStatement.setBytes(2, lock.getLockProof());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lockMate = new LockMate();
			lockMate.setId(new ID(resultSet.getLong("id")));
			if(lock.getLockType() == LockType.T1) {
				lock1 = new T1Lock();
			}
			else if(lock.getLockType() == LockType.T2) {
				lock1 = new T2Lock();
			}
			lockMate.setLock(lock1);
			lockMate.getLock().setLockProof(resultSet.getBytes("proof"));
			publickey = resultSet.getBytes("publickey");
			if (publickey == null) {
				lockMate.setPublickey(new Publickey());
			} else {
				lockMate.setPublickey(new Publickey().setLockType(lockMate.getLock().getLockType()).Parse(publickey));
			}
		}
		return lockMate;
	}

	@Override
	public boolean takeLockSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		LockMate lock = null;
		ID id = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + ((mode == Mode.MINING) ? "LOCK_MINING" : "LOCK_VALID") + " WHERE id <?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalLockNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			id = new ID(resultSet.getLong("id"));
			lock = getLock(id, Mode.GLOBAL);
			saveLockSnapshot(lock, changeLog.getHeight());
		}
		return true;
	}

	@Override
	public EQCHiveRoot getEQCHiveRoot(ID height) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement("SELECT eqchive_root FROM EQCHIVE WHERE height=?");
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				eqcHiveRoot = new EQCHiveRoot(resultSet.getBytes("eqchive_root"));
			}
		} finally {
			preparedStatement.close();
		}
		
		return eqcHiveRoot;
	}

	@Override
	public EQcoinSeedRoot getEQcoinSeedRoot(ID height) throws Exception {
		EQcoinSeedRoot eQcoinSeedRoot = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement("SELECT eqcoinseed_root FROM EQCHIVE WHERE height=?");
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				eQcoinSeedRoot = new EQcoinSeedRoot(resultSet.getBytes("eqcoinseed_root"));
			}
		} finally {
			preparedStatement.close();
		}
		return eQcoinSeedRoot;
	}

	@Override
	public Passport getPassportFromLockId(ID lockId, Mode mode) throws Exception {
		Passport passport = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE lock_id=?");
		preparedStatement.setLong(1, lockId.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet);
		}
		return passport;
	}

}
