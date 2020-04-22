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
package com.eqcoin.persistence;

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
import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.hive.EQCHiveRoot;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.lock.T1Lock;
import com.eqcoin.blockchain.lock.T1Publickey;
import com.eqcoin.blockchain.lock.T2Lock;
import com.eqcoin.blockchain.lock.T2Publickey;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.rpc.Balance;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.rpc.SignHash;
import com.eqcoin.rpc.TransactionIndex;
import com.eqcoin.rpc.TransactionIndexList;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeedRoot;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.ZionTxOut;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.MODE;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * @author Xun Wang
 * @date Oct 6, 2018
 * @email 10509759@qq.com
 */
public class EQCBlockChainH2 implements EQCBlockChain {
	private static final String JDBC_URL = "jdbc:h2:" + Util.H2_DATABASE_NAME;
	private static final String USER = "Believer";
	private static final String PASSWORD = "God bless us...";
	private static final String DRIVER_CLASS = "org.h2.Driver";
	private static Connection connection;
//	private static Statement statement;
	private static EQCBlockChainH2 instance;
	private static final int ONE_ROW = 1;
	private static final String TRANSACTION_GLOBAL = "TRANSACTION_GLOBAL";
	private static final String TRANSACTION_MINING = "TRANSACTION_MINING";
	private static final String TRANSACTION_VALID = "TRANSACTION_VALID";
	private static final String TRANSACTION_INDEX_GLOBAL = "TRANSACTION_INDEX_GLOBAL";
	private static final String TRANSACTION_INDEX_MINING = "TRANSACTION_INDEX_MINING";
	private static final String TRANSACTION_INDEX_VALID = "TRANSACTION_INDEX_VALID";
	private static final String LOCK_GLOBAL = "LOCK_GLOBAL";
	private static final String LOCK_MINING = "LOCK_MINING";
	private static final String LOCK_VALID = "LOCK_VALID";
	private static final String PASSPORT_GLOBAL = "PASSPORT_GLOBAL";
	private static final String PASSPORT_MINING = "PASSPORT_MINING";
	private static final String PASSPORT_VALID = "PASSPORT_VALID";
	
	public enum NODETYPE {
		NONE, FULL, MINER
	}
	
	public enum STATUS {
		BEGIN, END
	}
	
	public enum TRANSACTION_OP {
		TXIN, TXOUT, PASSPORT, PUBLICKEY, ADDRESS, TXFEERATE, CHECKPOINT, INVALID;
		public static TRANSACTION_OP get(int ordinal) {
			TRANSACTION_OP op = null;
			switch (ordinal) {
			case 0:
				op = TRANSACTION_OP.TXIN;
				break;
			case 1:
				op = TRANSACTION_OP.TXOUT;
				break;
			case 2:
				op = TRANSACTION_OP.PASSPORT;
				break;
			case 3:
				op = TRANSACTION_OP.PUBLICKEY;
				break;
			case 4:
				op = TRANSACTION_OP.ADDRESS;
				break;
			case 5:
				op = TRANSACTION_OP.TXFEERATE;
				break;
			case 6:
				op = TRANSACTION_OP.CHECKPOINT;
				break;
			default:
				op = TRANSACTION_OP.INVALID;
				break;
			}
			return op;
		}
		public boolean isSanity() {
			if((this.ordinal() < TXIN.ordinal()) || (this.ordinal() >= INVALID.ordinal())) {
				return false;
			}
			return true;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	private String createLockTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id >= 0,"
				+ "passport_id BIGINT NOT NULL  CHECK passport_id >= 0,"
				+ "lock_type TINYINT NOT NULL CHECK lock_type >= 0,"
				+ "publickey_hash BINARY(64) NOT NULL UNIQUE,"
				+ "publickey BINARY(67) UNIQUE,"
				+ "code BINARY UNIQUE"
				+ ")";
	}
	
	private String createPassportTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id >= 0,"
//				+ "lock_id BIGINT NOT NULL UNIQUE CHECK lock_id > 0,"
//				+ "create_height BIGINT NOT NULL CHECK create_height >= 0,"
//				+ "hash BINARY(64) NOT NULL,"
				// For in case exists fork chain need keep this
//				+ "update_height BIGINT NOT NULL CHECK update_height >= 0,"
				+ "bytes BINARY NOT NULL UNIQUE"
				+ ")";
	}
	
	private String createTransactionIndexTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "sn BIGINT NOT NULL UNIQUE CHECK sn > 0,"
				+ "type TINYINT NOT NULL CHECK type >= 0,"
				+ "height BIGINT NOT NULL CHECK height >= 0,"
				+ "index INT CHECK index >= 0,"
				+ "asset_id BIGINT NOT NULL CHECK asset_id > 0,"
				+ "nonce BIGINT NOT NULL CHECK nonce > 0"
//				+ "FOREIGN KEY (sn) REFERENCES " + tableName.substring(0, tableName.lastIndexOf("_INDEX")) + " (sn) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ")";
	}
	
	private String createTransactionTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "sn BIGINT NOT NULL CHECK sn > 0,"
				+ "op TINYINT NOT NULL CHECK op >= 0,"
				+ "id BIGINT NOT NULL CHECK id > 0,"
				+ "value BIGINT CHECK value > 0,"
				+ "object BINARY,"
				+ ")";
	}
	
	private EQCBlockChainH2() throws ClassNotFoundException, SQLException {
			Class.forName("org.h2.Driver");
			connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
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
		Statement statement = connection.createStatement();
		   	// Create Lock table.
		boolean result = statement.execute(createLockTable(getLockTableName(Mode.GLOBAL)));
		
		 result = statement.execute(createLockTable(getLockTableName(Mode.MINING)));
		 
		 result = statement.execute(createLockTable(getLockTableName(Mode.VALID)));
		 
			// Create Account table. Each Account should be unique and it's Passport's ID should be one by one
			result = statement.execute(createPassportTable(getPassportTableName(Mode.GLOBAL)));
			
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.MINING)));
			 
			 result = statement.execute(createPassportTable(getPassportTableName(Mode.VALID)));
			 
				// Create Global state relevant table's update status table
				result = statement.execute("CREATE TABLE IF NOT EXISTS ACCOUNT_UPDATE_STATUS("
						+ "height BIGINT NOT NULL CHECK height >= 0,"
						+ "snapshot TINYINT NOT NULL,"
						+ "account_merge TINYINT NOT NULL,"
						+ "transaction_merge TINYINT NOT NULL,"
						+ "clear TINYINT NOT NULL"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS EQCHIVE("
						+ "height BIGINT  NOT NULL CHECK height >= 0,"
						+ "eqchive_root BINARY NOT NULL UNIQUE,"
						+ "eqcoinseed_root BINARY NOT NULL UNIQUE,"
						+ "seeds BINARY NOT NULL"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS TEST("
					 	+ "id BIGINT PRIMARY KEY CHECK id > 0,"
						+ "height BINARY NOT NULL CHECK height >= 0"
						+ ")");
			 
			 result = statement.execute("CREATE TABLE IF NOT EXISTS TEST1("
					    + "id BIGINT PRIMARY KEY CHECK id > 0,"
						+ "height BINARY NOT NULL CHECK height >= 0"
						+ ")");
			
			// Create Balance table which contain every Account's history balance
			statement.execute("CREATE TABLE IF NOT EXISTS BALANCE("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "height BIGINT,"
					+ "balance BIGINT"
					+ ")");
			
			// Create PublicKey table
			statement.execute("CREATE TABLE IF NOT EXISTS PUBLICKEY("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "address_id BIGINT,"
					+ "publickey BINARY,"
					+ "height BIGINT"
					+ ")");
			
			// Create Transaction relevant table
			statement.execute(createTransactionTable(TRANSACTION_GLOBAL));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_GLOBAL));
			
			// Create Transaction mining relevant table
			statement.execute(createTransactionTable(TRANSACTION_MINING));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_MINING));
			
			// Create Transaction valid relevant table
			statement.execute(createTransactionTable(TRANSACTION_VALID));
			statement.execute(createTransactionIndexTable(TRANSACTION_INDEX_VALID));
			
			// Create EQC block transactions hash table for fast verify the transaction saved in the TRANSACTION table.
			// Calculate the HASH according to the transactions saved in the TRANSACTION table.
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTIONS_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "height BIGINT,"
					+ "hash BINARY(16)"
					+ ")");
						
			// Create EQC block signatures hash table each transaction's signature hash should be unique
			statement.execute("CREATE TABLE IF NOT EXISTS SIGNATURE_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "height BIGINT,"
					+ "trans_id BIGINT,"
					+ "txin_id BIGINT,"
					+ "signature BINARY(16)"
					+ ")");
			
			// EQC block tail and Account tail height table for synchronize EQC block and Account
			statement.execute("CREATE TABLE IF NOT EXISTS SYNCHRONIZATION("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "block_tail_height BIGINT,"
					+ "total_account_numbers BIGINT"
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
			
			// Create TxIn's EQC Block header's hash table
			statement.execute("CREATE TABLE IF NOT EXISTS TXIN_HEADER_HASH("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "header_hash BINARY,"
					+ "address_id BIGINT,"
					+ "height BIGINT"
					+ ")");
			
			// Create Passport snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS PASSPORT_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT NOT NULL ,"
//					+ "address_ai BINARY(33),"
					+ "passport BINARY NOT NULL,"
				/*	+ "account_hash BIGINT(64),"*/
					+ "snapshot_height BIGINT NOT NULL"
					+ ")");
			
			// Create Lock snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS LOCK_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT NOT NULL CHECK id >= 0,"
					+ "passport_id BIGINT NOT NULL  CHECK passport_id >= 0,"
					+ "lock_type TINYINT NOT NULL  CHECK lock_type >= 0,"
					+ "publickey_hash BINARY(64) NOT NULL UNIQUE,"
					+ "publickey BINARY(67) UNIQUE,"
					+ "code BINARY UNIQUE,"
					+ "snapshot_height BIGINT NOT NULL"
					+ ")");
			
			// Create Transaction max continues Nonce table
			statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_MAX_NONCE("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT,"
					+ "subchain_id BIGINT,"
					+ "max_nonce BIGINT"
					+ ")");
			
			// Create EQcoin Network table
			statement.execute("CREATE TABLE IF NOT EXISTS NETWORK ("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "ip  VARCHAR,"
					+ "type INT,"
					+ "counter INT,"
					+ "sync_time BIGINT"
					+ ")");
			
			// Create EQcoin Configuration table
			statement.execute("CREATE TABLE IF NOT EXISTS CONFIGURATION ("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "name  VARCHAR,"
					+ "sync_time BIGINT"
					+ ")");
			
			statement.close();
			
			if(result) {
				Log.info("Create table");
			}
	}
	
	public synchronized static EQCBlockChainH2 getInstance() throws ClassNotFoundException, SQLException {
		if(instance == null) {
			synchronized (EQCBlockChainH2.class) {
				if(instance == null) {
					instance = new EQCBlockChainH2();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isEQCBlockExists(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
	@Deprecated
	public synchronized boolean isEQCBlockExists(ID height) {
		boolean isEQCBlockExists = false;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			isEQCBlockExists = true;
		}
		return isEQCBlockExists;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#saveEQCBlock(com.eqzip.eqcoin.
	 * blockchain.EQCBlock)
	 */
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
		connection.commit();
		EQCType.assertEqual(rowCounter, ONE_ROW);
		preparedStatement.close();
//		eqcHive2 = getEQCHive(eqcHive.getHeight(), false);
//		EQCType.assertEqual(eqcHive.getBytes(), eqcHive2.getBytes());
//		byte[] eqcHeaderHash = getEQCHeaderHash(eqcHive.getHeight());
//		EQCType.assertEqual(eqcHive2.getEqcHeader().getHash(), eqcHeaderHash);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#deleteEQCBlock(com.eqzip.eqcoin.
	 * util.SerialNumber)
	 */
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
		Statement statement = null;
			statement = connection.createStatement();
			if (getEQCHiveTailHeight() != null) {
				rowCounter = statement.executeUpdate(
						"UPDATE SYNCHRONIZATION SET block_tail_height='" + height.longValue() + "' WHERE key='1'");

			} else {
				rowCounter = statement.executeUpdate(
						"INSERT INTO SYNCHRONIZATION (block_tail_height) VALUES('" + height.longValue() + "')");
			}
			EQCType.assertEqual(rowCounter, ONE_ROW);
			ID savedHeight = getEQCHiveTailHeight();
			Objects.requireNonNull(savedHeight);
			EQCType.assertEqual(height.longValue(), savedHeight.longValue());
			Log.info("saveEQCBlockTailHeight " + height + " successful");
		return true;
	}

	@Override
	public synchronized ID getEQCHiveTailHeight() throws SQLException {
		ID id = null;
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("SELECT * FROM SYNCHRONIZATION");
		if (resultSet.next()) {
			id = new ID(BigInteger.valueOf(resultSet.getLong("block_tail_height")));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getTransactionList(com.eqzip.eqcoin
	 * .blockchain.Address)
	 */
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
				Log.Error("During parse transaction error occur have to delete it");
				deleteTransactionInPool(resultSet.getBytes("signature"));
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
			preparedStatement = connection.prepareStatement(
					"INSERT INTO TRANSACTION_POOL (txin_id, nonce, rawdata, signature, proof, priority_value, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, transaction.getTxIn().getPassportId().longValue());
			preparedStatement.setLong(2, transaction.getNonce().longValue());
			preparedStatement.setBytes(3, transaction.getBytes());
			preparedStatement.setBytes(4, transaction.getEqcWitness().getEqcSignature());
			preparedStatement.setBytes(5, transaction.getProof());
			preparedStatement.setLong(6, transaction.getPriorityValue().longValue());
			preparedStatement.setLong(7, System.currentTimeMillis());
			preparedStatement.setBoolean(8, false);
			preparedStatement.setNull(9, Types.BIGINT);
			result = preparedStatement.executeUpdate();
		} else {
			preparedStatement = connection.prepareStatement(
					"UPDATE TRANSACTION_POOL SET rawdata=?, signature=?, proof=?, priority_value=?, receieved_timestamp=?, record_status=?, record_height=? WHERE txin_id=? AND nonce=?");
			preparedStatement.setBytes(1, transaction.getBytes());
			preparedStatement.setBytes(2, transaction.getEqcWitness().getEqcSignature());
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
			preparedStatement.setBytes(1, transaction.getEqcWitness().getEqcSignature());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}
	
	private synchronized boolean deleteTransactionInPool(byte[] signature) throws SQLException {
		int result = 0;
		if(Util.IsDeleteTransactionInPool) {
			PreparedStatement preparedStatement = connection
					.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE signature= ?");
			preparedStatement.setBytes(1, signature);
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized int getNonce(EQCLockMate address) {
		int nonce = 0;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableLock() + "'");
//			if (resultSet.next()) {
//				nonce = resultSet.getInt("nonce");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return nonce;
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

	public boolean savePassport(Passport passport) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getPassport(passport.getId()) != null) {
			preparedStatement = connection.prepareStatement(
					"UPDATE ACCOUNT SET id = ?, bytes = ? WHERE id = ?");
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO ACCOUNT (id, bytes) VALUES (?, ?)");
		}
		
		preparedStatement.setLong(1, passport.getId().longValue());
		preparedStatement.setBytes(2, passport.getBytes());
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}
	
	public boolean savePassport(ID passportId, byte[] passportBytes, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isPassportExists(passportId, mode)) {
			preparedStatement = connection.prepareStatement(
					"UPDATE " + getPassportTableName(mode) + " SET id = ?, bytes = ? WHERE id = ?");
			preparedStatement.setLong(3, passportId.longValue());
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO " + getPassportTableName(mode) + "  (id, bytes) VALUES (?, ?)");
		}
		
		preparedStatement.setLong(1, passportId.longValue());
		preparedStatement.setBytes(2, passportBytes);
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	public Passport getPassport(ID id) throws Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parsePassport(resultSet.getBytes("bytes"));
		}
		return account;
	}

	public Passport getPassport(byte[] addressAI)
			throws Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address_ai=?");
		preparedStatement.setBytes(1, addressAI);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parsePassport(resultSet.getBytes("bytes"));
		}
		return account;
	}

	@Override
	public synchronized Passport getPassportSnapshot(ID passportID, ID height)
			throws ClassNotFoundException, Exception {
		Passport passport = null;
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM PASSPORT_SNAPSHOT WHERE id=? AND snapshot_height >=? ORDER BY snapshot_height LIMIT 1");
		preparedStatement.setLong(1, passportID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet.getBytes("passport"));
		}

		return passport;
	}

	@Deprecated
	public synchronized Passport getPassportSnapshot(byte[] addressAI, ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCHiveTailHeight());
		Passport account = null;
		if (height.compareTo(getEQCHiveTailHeight()) == 0) {
			account = getPassport(addressAI, Mode.GLOBAL);
		} else {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"SELECT * FROM ACCOUNT_SNAPSHOT WHERE address_ai=? AND snapshot_height <=? ORDER BY snapshot_height DESC LIMIT 1");
			preparedStatement.setBytes(1, addressAI);
			preparedStatement.setLong(2, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				account = Passport.parsePassport(resultSet.getBytes("account"));
			}
		}

//		if(account == null) {
//			throw new NullPointerException(AddressTool.AIToAddress(addressAI) + " relevant Account is NULL");
//		}

		return account;
	}

	public synchronized boolean isPassportSnapshotExists(ID passportId, ID height) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM PASSPORT_SNAPSHOT WHERE id=? AND snapshot_height=?");
		preparedStatement.setLong(1, passportId.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public synchronized boolean savePassportSnapshot(ID passportId, byte[] passportBytes, ID height) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(
					"INSERT INTO PASSPORT_SNAPSHOT (id, passport, snapshot_height) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, passportId.longValue());
			preparedStatement.setBytes(2, passportBytes);
			preparedStatement.setLong(3, height.longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT Account ID: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException {
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

	@Deprecated
	public ID getTransactionMaxNonce(Transaction transaction) throws SQLException {
		ID nonce = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=?");
		preparedStatement.setLong(1, transaction.getTxIn().getPassportId().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			nonce = new ID(resultSet.getLong("max_nonce"));
		}
		return nonce;
	}

	@Deprecated
	public boolean saveTransactionMaxNonce(Transaction transaction) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = connection
				.prepareStatement("INSERT INTO TRANSACTION_MAX_NONCE (id, max_nonce) VALUES (?, ?)");
		preparedStatement.setLong(1, transaction.getTxIn().getPassportId().longValue());
		preparedStatement.setLong(2, transaction.getNonce().longValue());
		result = preparedStatement.executeUpdate();
		return result == ONE_ROW;
	}

	@Override
	public byte[] getEQCHeaderBuddyHash(ID height, ID currentTailHeight) throws Exception {
		byte[] hash = null;
		// Due to the latest Account is got from current node so it's xxxUpdateHeight
		// doesn't higher than currentTailHeight
//		EQCType.assertNotBigger(height, tail);
		// Here need pay attention to shouldn't include tail height because
		if (height.compareTo(currentTailHeight) < 0) {
			hash = getEQCHiveRootProof(height);
		} else if (height.equals(currentTailHeight)) {
			hash = getEQCHiveRootProof(height.getPreviousID());
		}
//		else if(height.equals(tail.getNextID())){
//			hash = getEQCHeaderHash(tail);
//		}
		else {
			throw new IllegalArgumentException(
					"Height " + height + " shouldn't bigger than current tail height " + currentTailHeight);
		}
		return hash;
	}

	@Override
	public synchronized MaxNonce getTransactionMaxNonce(Nest nest)
			throws ClassNotFoundException, Exception {
		MaxNonce maxNonce = null;
//		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=? AND subchain_id=?");
//		preparedStatement.setLong(1, nest.getId().longValue());
//		preparedStatement.setLong(2, nest.getAssetID().longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if(resultSet.next()) {
//			maxNonce = new MaxNonce();
//			maxNonce.setNonce(ID.valueOf(resultSet.getLong("max_nonce")));
//		}
//		if(maxNonce == null) {
//			maxNonce = new MaxNonce();
//			maxNonce.setNonce(Util.DB().getAccount(nest.getId()).getAsset(nest.getAssetID()).getNonce());
//		}
		ID currentNonce = getPassport(nest.getId(), Mode.GLOBAL).getNonce();
		maxNonce = new MaxNonce(currentNonce);
		Vector<Transaction> transactions = getPendingTransactionListInPool(nest);
		if (!transactions.isEmpty()) {
			Comparator<Transaction> reverseComparator = Collections.reverseOrder();
			Collections.sort(transactions, reverseComparator);
			Vector<ID> unique = new Vector<>();
			for (Transaction transaction : transactions) {
				if (!unique.contains(transaction.getNonce())) {
					unique.add(transaction.getNonce());
				} else {
					Log.info("Current transaction's nonce is duplicate just discard it");
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				}
				if (transaction.getNonce().compareTo(currentNonce) <= 0) {
					Log.info("Current transaction's nonce is invalid just discard it");
					EQCBlockChainH2.getInstance().deleteTransactionInPool(transaction);
				}
			}
			transactions = getPendingTransactionListInPool(nest);
			if (!transactions.isEmpty()) {
				Collections.sort(transactions);
				if (transactions.firstElement().getNonce().equals(currentNonce.getNextID())) {
					int i = 0;
					for (; i < (transactions.size() - 1); ++i) {
//						if (i < (transactions.size() - 2)) {
						if (!transactions.get(i).getNonce().getNextID().equals(transactions.get(i + 1).getNonce())) {
							break;
						}
//						}
					}
					maxNonce = new MaxNonce(transactions.get(i).getNonce());
				}
			}
		}
		return maxNonce;
	}

	@Override
	public synchronized boolean isTransactionMaxNonceExists(Nest nest) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_MAX_NONCE WHERE id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Deprecated
	@Override
	public boolean saveTransactionMaxNonce(Nest nest, MaxNonce maxNonce) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isTransactionMaxNonceExists(nest)) {
			preparedStatement = connection
					.prepareStatement("UPDATE TRANSACTION_MAX_NONCE SET max_nonce = ? WHERE id = ?");
			preparedStatement.setLong(1, maxNonce.getNonce().longValue());
			preparedStatement.setLong(2, nest.getId().longValue());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO TRANSACTION_MAX_NONCE (id, max_nonce) VALUES (?, ?)");
			preparedStatement.setLong(1, nest.getId().longValue());
			preparedStatement.setLong(2, maxNonce.getNonce().longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteTransactionMaxNonce(Nest nest) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM TRANSACTION_MAX_NONCE WHERE id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public Balance getBalance(Nest nest) throws SQLException, Exception {
		Balance balance = new Balance();
		balance.getBalance().add(getPassport(nest.getId(), Mode.GLOBAL).getBalance());
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND record_status=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setBoolean(2, false);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			balance.getBalance().subtract(new Value(resultSet.getLong("txin_value")));
		}
		return balance;
	}

	@Override
	public boolean isIPExists(IP ip, NODETYPE nodeType) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = null;
		if (nodeType == NODETYPE.MINER || nodeType == NODETYPE.FULL) {
			preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=? AND type=?");
			preparedStatement.setString(1, ip.getIp());
			preparedStatement.setInt(2, nodeType.ordinal());
		} else {
			preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
			preparedStatement.setString(1, ip.getIp());
		}
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public boolean isMinerExists(IP ip) throws SQLException, Exception {
		return isIPExists(ip, NODETYPE.MINER);
	}

	@Override
	public boolean saveMiner(IP ip) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, counter = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(2, 0);
			preparedStatement.setString(3, ip.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO NETWORK (ip, type, counter, sync_time) VALUES (?, ?, ?, ?)");
			preparedStatement.setString(1, ip.getIp());
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(3, 0);
			preparedStatement.setLong(4, 0);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteMiner(IP ip) throws SQLException, Exception {
		return deleteNode(ip);
	}

	public boolean deleteNode(IP ip) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip.getIp());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public IPList getMinerList() throws SQLException, Exception {
		IPList ipList = new IPList();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
		preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			ipList.addIP(new IP(resultSet.getString("ip")));
		}
		return ipList;
	}

	@Override
	public boolean isFullNodeExists(IP ip) throws SQLException, Exception {
		return isIPExists(ip, NODETYPE.FULL);
	}

	@Override
	public boolean saveFullNode(IP ip) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (!isIPExists(ip, NODETYPE.MINER)) {
			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type) VALUES (?, ?)");
			preparedStatement.setString(1, ip.getIp());
			preparedStatement.setInt(2, NODETYPE.FULL.ordinal());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public boolean deleteFullNode(IP ip) throws SQLException, Exception {
		return deleteNode(ip);
	}

	@Override
	public IPList getFullNodeList() throws SQLException, Exception {
		IPList ipList = new IPList();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
		preparedStatement.setInt(1, NODETYPE.FULL.ordinal());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			ipList.addIP(new IP(resultSet.getString("ip")));
		}
		return ipList;
	}

	@Override
	public Vector<Transaction> getPendingTransactionListInPool(Nest nest) throws SQLException, Exception {
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
		transactionIndexList.setSyncTime(currentSyncTime);
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
	public boolean saveMinerSyncTime(IP ip, long syncTime) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, sync_time = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setLong(2, syncTime);
			preparedStatement.setString(3, ip.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO NETWORK (ip, type, sync_time) VALUES (?, ?, ?)");
			preparedStatement.setString(1, ip.getIp());
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setLong(3, syncTime);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public long getMinerSyncTime(IP ip) throws SQLException, Exception {
		long sync_time = 0;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip.getIp());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			sync_time = resultSet.getLong("sync_time");
		}
		return sync_time;
	}

	@Override
	public SignHash getSignHash(ID id) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveIPCounter(IP ip, int counter) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isIPExists(ip, NODETYPE.NONE)) {
			preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ?, counter = ? where ip = ?");
			preparedStatement.setInt(1, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(2, counter);
			preparedStatement.setString(3, ip.getIp());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + result);
		} else {
			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type, counter) VALUES (?, ?, ?)");
			preparedStatement.setString(1, ip.getIp());
			preparedStatement.setInt(2, NODETYPE.MINER.ordinal());
			preparedStatement.setInt(3, counter);
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public int getIPCounter(IP ip) throws SQLException, Exception {
		int counter = 0;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE ip=?");
		preparedStatement.setString(1, ip.getIp());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			counter = resultSet.getInt("counter");
		}
		return counter;
	}

	public void deleteAccountFromTo(ID fromID, ID toID) throws Exception {
		// TODO Auto-generated method stub

	}

	public void deleteEQCHiveFromTo(ID fromHeight, ID toHeight) throws Exception {
		// TODO Auto-generated method stub

	}

	@Deprecated
	public boolean savePassport(Passport passport, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getPassport(passport.getId(), mode) != null) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getPassportTableName(mode)
					+ " SET id = ?, bytes = ? WHERE id = ?");
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getPassportTableName(mode)
							+ " (id, bytes) VALUES (?, ?)");
		}
		preparedStatement.setLong(1, passport.getId().longValue());
		preparedStatement.setBytes(2, passport.getBytes());
		rowCounter = preparedStatement.executeUpdate();
		EQCType.assertEqual(rowCounter, ONE_ROW);
		
		return true;
	}

	@Override
	public Passport getPassport(ID id, Mode mode) throws Exception {
		Passport passport = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet.getBytes("bytes"));
		}
		return passport;
	}
	
	@Override
	public byte[] getPassportBytes(ID id, Mode mode) throws Exception {
		byte[] passport = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = resultSet.getBytes("bytes");
		}
		return passport;
	}
	
	@Override
	public boolean isPassportExists(ID id, Mode mode) throws Exception {
		boolean isExists = false;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getPassportTableName(mode) + " WHERE id=?");
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
	
	@Deprecated
	public Passport getPassport(byte[] addressAI, Mode mode) throws Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "
				+ getPassportTableName(mode) + " WHERE address_ai = ?");
		preparedStatement.setBytes(1, addressAI);
		;
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			account = Passport.parsePassport(resultSet.getBytes("bytes"));
		}
		return account;
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
			savePassport(new ID(resultSet.getLong("id")), resultSet.getBytes("bytes"), Mode.GLOBAL);
		}
		return true;
	}

	@Override
	public boolean takePassportSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		byte[] passport = null;
		ID id = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + ((mode == Mode.MINING) ? "PASSPORT_MINING" : "PASSPORT_VALID") + " WHERE id <?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalPassportNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			id = new ID(resultSet.getLong("id"));
			passport = getPassportBytes(id, mode.GLOBAL);
			savePassportSnapshot(id, passport, changeLog.getHeight());
		}
		return true;
	}

//	public boolean savePossibleNode(IP ip, NODETYPE nodeType) throws SQLException, Exception {
//		int result = 0;
//		PreparedStatement preparedStatement = null;
//		if (!isIPExists(ip, NODETYPE.FULL)) {
//			preparedStatement = connection.prepareStatement("INSERT INTO NETWORK (ip, type) VALUES (?, ?)");
//			preparedStatement.setString(1, ip.getIp());
//			if(nodeType == NODETYPE.MINER) {
//				preparedStatement.setInt(2, NODETYPE.POSSIBLEMINER.ordinal());
//			}
//			else {
//				preparedStatement.setInt(2, NODETYPE.POSSIBLEFULL.ordinal());
//			}
//			result = preparedStatement.executeUpdate();
////				Log.info("INSERT: " + result);
//		}
//		return result == ONE_ROW;
//	}
//	
//	public IPList getPossibleNode(NODETYPE nodeType) throws SQLException, Exception {
//		IPList ipList = new IPList();
//		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM NETWORK WHERE type=?");
//		preparedStatement.setInt(1, nodeType.ordinal());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		while (resultSet.next()) {
//			ipList.addIP(resultSet.getString("ip"));
//		}
//		return ipList;
//	}
//	
//	public boolean updatePossibleNode(IP ip, NODETYPE nodeType) throws SQLException {
//		int result = 0;
//		PreparedStatement preparedStatement = null;
//		preparedStatement = connection.prepareStatement("UPDATE NETWORK SET type = ? where ip = ?");
//		preparedStatement.setInt(1, nodeType.ordinal());
//		preparedStatement.setString(2, ip.getIp());
//		result = preparedStatement.executeUpdate();
////					Log.info("UPDATE: " + result);
//		return result == ONE_ROW;
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
	
	public synchronized boolean test(ID id) throws Exception {
		int rowCounter = 0;
		connection.setAutoCommit(false);
		PreparedStatement preparedStatement = null;
			preparedStatement = connection
					.prepareStatement("INSERT INTO TEST (id, height) VALUES (?, ?)");
			preparedStatement.setLong(1, id.longValue());
			preparedStatement.setBytes(2, id.getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.setLong(1, id.getNextID().longValue());
			preparedStatement.setBytes(2, id.getNextID().getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.executeBatch();
			preparedStatement.close();
//			int i = 1/0;
			
			preparedStatement = connection
					.prepareStatement("INSERT INTO TEST1 (id, height) VALUES (?, ?)");
			preparedStatement.setLong(1, id.longValue());
			preparedStatement.setBytes(2, id.getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.setLong(1, id.getNextID().longValue());
			preparedStatement.setBytes(2, id.getNextID().getEQCBits());
			preparedStatement.addBatch();
			preparedStatement.executeBatch();
			
			connection.commit();
			
//			rowCounter = preparedStatement.executeUpdate();
//				Log.info("INSERT: " + rowCounter);
		return true;
	}
	
	public synchronized void test1(ID height) throws Exception {
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TEST WHERE height=?");
		preparedStatement.setBytes(1, height.getEQCBits());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			Log.info(new ID(resultSet.getBytes(1)).toString());
		}
	}

	@Override
	public boolean saveLock(EQCLockMate eqcLockMate, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (isLockExists(eqcLockMate.getId(), mode)) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getLockTableName(mode)
					+ " SET id = ?, passport_id = ?,  lock_type = ?, publickey_hash = ?, publickey = ?, code = ? WHERE id = ?");
			preparedStatement.setLong(7, eqcLockMate.getId().longValue());
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getLockTableName(mode)
							+ " (id, passport_id, lock_type, publickey_hash, publickey, code) VALUES (?, ?, ?, ?, ?, ?)");
		}
		
		preparedStatement.setLong(1, eqcLockMate.getId().longValue());
		preparedStatement.setLong(2, eqcLockMate.getPassportId().longValue());
		preparedStatement.setByte(3, (byte) eqcLockMate.getLock().getLockType().ordinal());
		preparedStatement.setBytes(4, eqcLockMate.getLock().getLockCode());
		if(eqcLockMate.getEqcPublickey().isNULL()) {
			preparedStatement.setNull(5, Types.BINARY);
		}
		else {
			preparedStatement.setBytes(5, eqcLockMate.getEqcPublickey().getBytes());
		}
		if(eqcLockMate.getLock() instanceof T1Lock || eqcLockMate.getLock() instanceof T2Lock) {
			preparedStatement.setNull(6, Types.BINARY);
		}
		else {
			throw new IllegalStateException("Wrong lock type");
		}
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public EQCLockMate getLock(ID id, Mode mode) throws Exception {
		EQCLockMate lock = null;
		byte[] publickey = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getLockTableName(mode) + " WHERE id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lock = new EQCLockMate();
			lock.setId(new ID(resultSet.getLong("id")));
			lock.setPassportId(new ID(resultSet.getLong("passport_id")));
			LockType lockType = LockType.get(resultSet.getByte("lock_type"));
			EQCLock eqcLock = null;
			if(lockType == LockType.T1) {
				eqcLock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				eqcLock = new T2Lock();
			}
			lock.setLock(eqcLock);
			lock.getLock().setLockCode(resultSet.getBytes("publickey_hash"));
			if (lock.getLock().getLockType() == LockType.T1) {
				publickey = resultSet.getBytes("publickey");
				if (publickey == null) {
					lock.setEqcPublickey(new T1Publickey());
				} else {
					lock.setEqcPublickey(new T1Publickey(publickey));
				}

			} else if (lock.getLock().getLockType() == LockType.T2) {
				publickey = resultSet.getBytes("publickey");
				if (publickey == null) {
					lock.setEqcPublickey(new T2Publickey());
				} else {
					lock.setEqcPublickey(new T2Publickey(publickey));
				}
			}
		}
		return lock;
	}

	@Override
	public EQCLockMate getLock(ID id, ID height) throws Exception {
		EQCLockMate lock = null;
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
	public EQCLockMate getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		EQCLockMate lock = null;
		LockType lockType = null;
		EQCLock eqcLock = null;
		byte[] publickey = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM LOCK_SNAPSHOT WHERE id=? AND snapshot_height >=? ORDER BY snapshot_height LIMIT 1");
		preparedStatement.setLong(1, lockID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lock = new EQCLockMate();
			lock.setId(new ID(resultSet.getLong("id")));
			lock.setPassportId(new ID(resultSet.getLong("passport_id")));
			lockType = LockType.get(resultSet.getShort("lock_type"));
			if(lockType == LockType.T1) {
				eqcLock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				eqcLock = new T2Lock();
			}
			lock.setLock(eqcLock);
			lock.getLock().setLockCode(resultSet.getBytes("publickey_hash"));
			publickey = resultSet.getBytes("publickey");
			if(publickey == null) {
				if(lock.getLock().getLockType() == LockType.T1) {
					lock.setEqcPublickey(new T1Publickey());
				}
				else if(lock.getLock().getLockType() == LockType.T2) {
					lock.setEqcPublickey(new T2Publickey());
				}
			}
			else {
				if(lock.getLock().getLockType() == LockType.T1) {
					lock.setEqcPublickey(new T1Publickey(publickey));
				}
				else if(lock.getLock().getLockType() == LockType.T2) {
					lock.setEqcPublickey(new T2Publickey(publickey));
				}
			}
		}

		return lock;
	}

	@Override
	public boolean saveLockSnapshot(EQCLockMate lock, ID height) throws SQLException, Exception {
		int result = 0;
		PreparedStatement preparedStatement = null;
			preparedStatement = connection.prepareStatement(
					"INSERT INTO LOCK_SNAPSHOT (id, passport_id, lock_type, publickey_hash, publickey, code, snapshot_height) VALUES (?, ?, ?, ?,?, ?, ?)");
			preparedStatement.setLong(1, lock.getId().longValue());
			preparedStatement.setLong(2, lock.getPassportId().longValue());
			preparedStatement.setByte(3, (byte) lock.getLock().getLockType().ordinal());
			preparedStatement.setBytes(4, lock.getLock().getLockCode());
			if(lock.getEqcPublickey().isNULL()) {
				preparedStatement.setNull(5, Types.BINARY);
			}
			else {
				preparedStatement.setBytes(5, lock.getEqcPublickey().getBytes());
			}
			if(lock.getLock() instanceof T1Lock || lock.getLock() instanceof T2Lock) {
				preparedStatement.setNull(6, Types.BINARY);
			}
			else {
				throw new IllegalStateException("Wrong lock type");
			}
			preparedStatement.setLong(7, height.longValue());
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
		EQCLockMate lock;
		LockType lockType = null;
		EQCLock eqcLock = null;
		byte[] publickey;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getLockTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			lock = null;
			lock = new EQCLockMate();
			lock.setId(new ID(resultSet.getLong("id")));
			lock.setPassportId(new ID(resultSet.getLong("passport_id")));
			lockType = LockType.get(resultSet.getByte("lock_type"));
			if(lockType == LockType.T1) {
				eqcLock = new T1Lock();
			}
			else if(lockType == LockType.T2) {
				eqcLock = new T2Lock();
			}
			lock.setLock(eqcLock);
			lock.getLock().setLockCode(resultSet.getBytes("publickey_hash"));
			publickey = resultSet.getBytes("publickey");
			if(publickey == null) {
				if(lock.getLock().getLockType() == LockType.T1) {
					lock.setEqcPublickey(new T1Publickey());
				}
				else if(lock.getLock().getLockType() == LockType.T2) {
					lock.setEqcPublickey(new T2Publickey());
				}
			}
			else {
				if(lock.getLock().getLockType() == LockType.T1) {
					lock.setEqcPublickey(new T1Publickey(publickey));
				}
				else if(lock.getLock().getLockType() == LockType.T2) {
					lock.setEqcPublickey(new T2Publickey(publickey));
				}
			}
			saveLock(lock, Mode.GLOBAL);
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
	public ID isLockExists(EQCLock eqcLock, Mode mode) throws Exception {
		ID lockId = null;
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM " + getLockTableName(mode) + " WHERE publickey_hash=?");
		preparedStatement.setBytes(1, eqcLock.getLockCode());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lockId = new ID(resultSet.getLong("id"));
		}
		return lockId;
	}

	@Override
	public EQCLockMate getLock(EQCLock eqcLock, Mode mode) throws Exception {
		EQCLockMate lock = null;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + getLockTableName(mode) + " WHERE lock_type=? AND publickey_hash=?");
//		preparedStatement.setByte(1, (byte) eqcLock.getLockType().ordinal());
//		preparedStatement.setBytes(2, eqcLock.getLockCode());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lock = new EQCLockMate();
//			lock.setId(new ID(resultSet.getLong("id")));
//			lock.setPassportId(new ID(resultSet.getLong("passport_id")));
//			lock.setReadableLock(LockTool.AIToReadableLock(resultSet.getBytes("ai_lock")));
//			lock.setPublickey(resultSet.getBytes("publickey"));
//			lock.setCode(resultSet.getBytes("code"));
//		}
		return lock;
	}

	@Override
	public boolean takeLockSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		EQCLockMate lock = null;
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
	
}
