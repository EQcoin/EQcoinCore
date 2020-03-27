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
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.EQCPublickey;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TxOut;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.MODE;

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
				+ "id BIGINT PRIMARY KEY CHECK id > 0,"
				+ "passport_id BIGINT NOT NULL  CHECK passport_id > 0,"
				+ "readable_lock VARCHAR(255) NOT NULL UNIQUE,"
//				+ "create_height BIGINT NOT NULL CHECK create_height >= 0,"
				+ "publickey BINARY(64) UNIQUE"
				// For in case exists fork chain need keep this
//				+ "publickey_create_height BIGINT NOT NULL CHECK publickey_create_height > 0"
				+ ")";
	}
	
	private String createPassportTable(String tableName) {
		return "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ "id BIGINT PRIMARY KEY CHECK id > 0,"
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
						+ "bytes BINARY NOT NULL UNIQUE,"
						+ "eqcheader_hash BINARY(64) NOT NULL"
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
					+ "txin_value BIGINT,"
					+ "nonce BIGINT,"
					+ "rawdata BINARY,"
					+ "signature BINARY,"
					+ "proof BINARY(5),"
					+ "qos BIGINT,"
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
					+ "id BIGINT NOT NULL,"
//					+ "address_ai BINARY(33),"
					+ "passport BINARY NOT NULL,"
				/*	+ "account_hash BIGINT(64),"*/
					+ "snapshot_height BIGINT NOT NULL"
					+ ")");
			
			// Create Lock snapshot table
			result = statement.execute("CREATE TABLE IF NOT EXISTS LOCK_SNAPSHOT("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "id BIGINT NOT NULL,"
//					+ "passport_id BIGINT NOT NULL,"
//					+ "readable_lock VARCHAR(255) NOT NULL,"
//					+ "publickey BINARY(64) UNIQUE,"
					+ "publickey_update_height BIGINT NOT NULL"
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

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressSerialNumber(com.eqzip.eqcoin.blockchain.Address)
	 */
	@Deprecated
	public synchronized ID getAddressID(Lock address) {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddress(com.eqzip.eqcoin.util.SerialNumber)
	 */
	@Deprecated
	public synchronized Lock getAddress(ID serialNumber) {
		Lock address = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				address = new Lock();
				address.setReadableLock(resultSet.getString("address"));
				address.setId(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
				// Here need do more job to retrieve the code of address. Need consider
				// sometimes the code is null but otherwise the code isn't null
				address.setCode(resultSet.getBytes("code"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return address;
	}
	
	@Deprecated
	public synchronized Vector<Passport> getAllAccounts(ID height) {
		Vector<Passport> accounts = new Vector<Passport>();
//		Account account = null;
//		Address address = null;
//		try {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM ACCOUNT WHERE address_update_height>=?");
//			preparedStatement.setLong(1, height.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			while (resultSet.next()) {
//				account = new AssetAccount();
//				address = new Address();
//				address.setReadableAddress(resultSet.getString("address"));
//				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
//				// Here need do more job to retrieve the code of address. Need consider
//				// sometimes the code is null but otherwise the code isn't null
//				account.setAddress(address);
//				account.setBalance(resultSet.getLong("balance"));
//				accounts.add(account);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return accounts;
	}
	
	@Deprecated
	public synchronized Vector<Passport> getAccounts(ID begin, ID end, ID height) {
		Vector<Passport> accounts = new Vector<>();
//		Account account = null;
//		Address address = null;
//		Publickey publickey = null;
//		try {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM ACCOUNT WHERE id>=? AND id<? AND address_update_height<=?");
//			preparedStatement.setLong(1, begin.longValue());
//			preparedStatement.setLong(2, end.longValue());
//			preparedStatement.setLong(3, height.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			while (resultSet.next()) {
//				account = new Account();
//				address = new Address();
//				address.setReadableAddress(AddressTool.AIToAddress(resultSet.getBytes("address")));
//				address.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
//				account.setAddress(address);
//				account.setAddressCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("address_update_height"))));
//				if(resultSet.getBytes("publickey") != null) {
//					publickey = new Publickey();
//					publickey.setPublickey(resultSet.getBytes("publickey"));
//					publickey.setPublickeyCreateHeight(new ID(BigInteger.valueOf(resultSet.getLong("publickey_update_height"))));
//					account.setPublickey(publickey);
//				}
//				account.setBalance(resultSet.getLong("balance"));
//				account.setBalanceUpdateHeight(new ID(BigInteger.valueOf(resultSet.getLong("balance_update_height"))));
//				account.setNonce(new ID(BigInteger.valueOf(resultSet.getLong("nonce"))));
//				accounts.add(account);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return accounts;
	}

	@Deprecated
	public synchronized ID getTotalAccountNumber(ID height) {
		long accountsNumber = 0;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address_update_height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT COUNT(*) FROM ACCOUNT AS rowcount WHERE address_update_height<=?");
			preparedStatement.setLong(1, height.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				accountsNumber = resultSet.getLong(1);//.getLong("rowcount");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return new ID(accountsNumber);
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#isAddressExists(com.eqzip.eqcoin.blockchain.Address)
	 */
	@Deprecated
	public synchronized boolean isAddressExists(Lock address) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			 PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
			 preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
			 ResultSet resultSet = preparedStatement.executeQuery();
			 if(resultSet.next()) {
				 return true;
			 }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}
	
	@Deprecated
	public synchronized boolean appendAddress(Lock address, ID address_update_height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("INSERT INTO ACCOUNT (sn, address, code, height) VALUES('" 
//					+ address.getSerialNumber().longValue() + "','"
//					+ address.getAddress() + "','"
//					+ ((null == address.getCode())?"":address.getCode()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
//					+ height.longValue() + "')");
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO ACCOUNT (id, address, code, address_update_height, balance, nonce) VALUES (?, ?, ?, ?, ?, ?, ?)");
			preparedStatement.setLong(1, address.getId().longValue());
			preparedStatement.setBytes(2, address.getBytes(LockShape.AI));
			preparedStatement.setBytes(3,  address.getCode());
			preparedStatement.setLong(5, address_update_height.longValue() );
			preparedStatement.setLong(6, 0);
			preparedStatement.setLong(7, 0);
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean deleteAddress(Lock address) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM ACCOUNT WHERE address='" + address.getAddress() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT WHERE address=?");
			preparedStatement.setBytes(1, address.getBytes(LockShape.AI));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean deleteAddressFromHeight(ID height) {
		int result = 0;
		try {
//			result = statement.executeUpdate("DELETE FROM ACCOUNT WHERE height>='" + height.longValue() + "'");
			PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM ACCOUNT WHERE height>=?");
			preparedStatement.setLong(1, height.longValue());
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result >= ONE_ROW;
	}
	
	/* (non-Javadoc)
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getLastAddressSerialNumber()
	 */
	@Deprecated
	public synchronized ID getLastAddressSerialNumber() {
		ID serialNumber = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM ACCOUNT WHERE id = SELECT MAX(id) FROM ACCOUNT");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE key = SELECT MAX(key) FROM ACCOUNT");
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return serialNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getAddressTotalNumbers()
	 */
	@Deprecated
	public synchronized long getAddressTotalNumbers() {
		// TODO Auto-generated method stub
		return 0;
	}

//	public synchronized boolean updateAccount(Account account) {
//		int result = 0;
//		int validCount = 0;
//		try {
//			if (account.isAddressChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"INSERT INTO ACCOUNT (sn, address, code, code_hash, address_update_height, nonce) VALUES (?, ?, ?, ?, ?, ?)");
//				preparedStatement.setLong(1, account.getAddress().getSerialNumber().longValue());
//				preparedStatement.setBytes(2, account.getAddress().getAddressAI());
//				preparedStatement.setBytes(3, account.getAddress().getCode());
//				preparedStatement.setBytes(4, account.getAddress().getCodeHash());
//				preparedStatement.setLong(5, account.getAddressCreateHeight().longValue());
//				preparedStatement.setLong(6, 0);
//				result += preparedStatement.executeUpdate();
//			}
//			
//			if(account.isPublickeyChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET publickey = ?, publickey_update_height = ? where sn = ?");
//				preparedStatement.setBytes(1, account.getPublickey().getPublickey());
//				preparedStatement.setLong(2, account.getPublickey().getPublickeyCreateHeight().longValue());
//				preparedStatement.setLong(3, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//			
//			if(account.isBalanceChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET balance = ?, balance_update_height  = ? where sn = ?");
//				preparedStatement.setLong(1, account.getBalance());
//				preparedStatement.setLong(2, account.getBalanceUpdateHeight().longValue());
//				preparedStatement.setLong(3, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//			
//			if(account.isNonceChanged()) {
//				++validCount;
//				PreparedStatement preparedStatement = connection.prepareStatement(
//						"UPDATE ACCOUNT SET nonce = ? where sn = ?");
//				preparedStatement.setLong(1, account.getNonce().longValue());
//				preparedStatement.setLong(2, account.getAddress().getSerialNumber().longValue());
////				result += preparedStatement.executeUpdate();
//				if(preparedStatement.executeUpdate() != 0) {
//					++result;
//				}
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return result == validCount;
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeySerialNumber(com.eqzip.
	 * eqcoin.blockchain.Address)
	 */
	@Deprecated
	public synchronized ID getPublicKeySerialNumber(Lock address) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKey(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	@Deprecated
	public synchronized EQCPublickey getPublicKey(ID serialNumber) {
		EQCPublickey publicKey = null;
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM PUBLICKEY WHERE address_sn='" + serialNumber.longValue() + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM PUBLICKEY WHERE address_id=?");
			preparedStatement.setLong(1, serialNumber.longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				publicKey = new EQCPublickey();
				publicKey.setPublickey(resultSet.getBytes("publickey"));
//				 publicKey.setID(new ID(BigInteger.valueOf(resultSet.getLong("id"))));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return publicKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#isPublicKeyExists(com.eqzip.eqcoin.
	 * blockchain.PublicKey)
	 */
	@Deprecated
	public synchronized boolean isPublicKeyExists(EQCPublickey publicKey) {
		try {
			ResultSet resultSet;
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM ACCOUNT WHERE publickey = ?");
			preparedStatement.setBytes(1, publicKey.getPublickey());
			resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}

	@Deprecated
	public synchronized boolean savePublicKey(EQCPublickey publicKey, ID height) {
		int result = 0;
//		try {
////			result = statement.executeUpdate("INSERT INTO PUBLICKEY (address_sn, publickey, height) VALUES('" 
////					+ publicKey.getSerialNumber().longValue() + "','"
////					+ ((null == publicKey.getPublicKey())?"":publicKey.getPublicKey()) + "','" // still exists bugs need do more job to find how to insert null but due to network is bad so...
////					+ height.longValue() + "')");
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("INSERT INTO PUBLICKEY (address_id, publickey, height) VALUES (?, ?, ?)");
////			preparedStatement.setLong(1, publicKey.getId().longValue());
//			preparedStatement.setBytes(2, publicKey.getCompressedPublickey());
//			preparedStatement.setLong(3, height.longValue());
//			result = preparedStatement.executeUpdate();
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean deletePublicKey(EQCPublickey publicKey) {
		int result = 0;
//		try {
//			result = statement.executeUpdate(
//					"DELETE FROM PUBLICKEY WHERE publickey='" + publicKey.getCompressedPublickey() + "'");
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result >= 1;
	}

	@Deprecated
	public synchronized boolean deletePublicKeyFromHeight(ID height) {
		int result = 0;
//		try {
//			result = statement.executeUpdate("DELETE FROM PUBLICKEY WHERE height>='" + height.longValue() + "'");
//			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getLastPublicKeySerialNumber()
	 */
	@Deprecated
	public synchronized ID getLastPublicKeySerialNumber() {
		ID serialNumber = null;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM PUBLICKEY WHERE key = SELECT MAX(key) FROM PUBLICKEY");
//			if (resultSet.next()) {
//				serialNumber = new ID(BigInteger.valueOf(resultSet.getLong("id")));
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return serialNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getPublicKeyTotalNumbers()
	 */
	@Deprecated
	public synchronized long getPublicKeyTotalNumbers() {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCBlock(com.eqzip.eqcoin.util.
	 * SerialNumber, boolean)
	 */
	@Override
	public synchronized EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception {
		EQCHive eqcHive = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			eqcHive = new EQCHive(resultSet.getBytes("bytes"));
		}
		preparedStatement.close();
		return eqcHive;
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
		PreparedStatement preparedStatement = null;
		if (isEQCHiveExists(eqcHive.getHeight())) {
			preparedStatement = connection
					.prepareStatement("UPDATE EQCHIVE SET height = ?, bytes = ?,  eqcheader_hash = ? WHERE height = ?");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getBytes());
			preparedStatement.setBytes(3, eqcHive.getEqcHeader().getHash());
			preparedStatement.setLong(4, eqcHive.getHeight().longValue());
			rowCounter = preparedStatement.executeUpdate();
//					Log.info("UPDATE: " + rowCounter);
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO EQCHIVE (height, bytes, eqcheader_hash) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, eqcHive.getHeight().longValue());
			preparedStatement.setBytes(2, eqcHive.getBytes());
			preparedStatement.setBytes(3, eqcHive.getEqcHeader().getHash());
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
//		if (isEQCHiveExists(height)) {
//			throw new IllegalStateException("deleteEQCHive No." + height + " failed EQCHive still exists");
//		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.eqzip.eqcoin.blockchain.EQCBlockChain#getEQCHeader(com.eqzip.eqcoin.util.
	 * SerialNumber)
	 */
	@Deprecated
	public synchronized EQCHiveRoot getEQCHeader(ID height) {
		EQCHiveRoot eqcHeader = null;
		File file = new File(Util.HIVE_PATH + height.longValue() + Util.EQC_SUFFIX);
		if (file.exists() && file.isFile() && (file.length() > 0)) {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				byte[] bytes = null;
				// Parse EqcHeader
				if ((bytes = EQCType.parseBIN(bis)) != null) {
					eqcHeader = new EQCHiveRoot(bytes);
				}
			} catch (IOException | NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				eqcHeader = null;
				Log.Error(e.getMessage());
			}
		}
		return eqcHeader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqzip.eqcoin.blockchain.EQCBlockChain#getBalance(com.eqzip.eqcoin.
	 * blockchain.Address)
	 */
	@Deprecated
	public synchronized long getBalance(Lock address) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
//		try {
//			// Get all TxIn's value
//			resultSet = statement.executeQuery(
//					"SELECT * FROM TRANSACTION WHERE address_id = '" + address.getId().longValue() + "' AND io = true");
//			while (resultSet.next()) {
//				txInValue += resultSet.getLong("value");
//			}
//
//			// Get all TxOut's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = false");
//			while (resultSet.next()) {
////				Log.info("balance: " + txOutValue);
//				txOutValue += resultSet.getLong("value");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return txOutValue - txInValue;
	}

	@Deprecated
	public synchronized long getBalance(Lock address, ID height) {
		ResultSet resultSet;
		long txInValue = 0;
		long txOutValue = 0;
//		try {
//			// Get all TxIn's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
//			while (resultSet.next()) {
//				txInValue += resultSet.getLong("value");
//			}
//
//			// Get all TxOut's value
//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getId().longValue() + "' AND io = true AND height >= '" + height.longValue() + "'");
//			while (resultSet.next()) {
//				txOutValue += resultSet.getLong("value");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return txInValue - txOutValue;
	}

	@Deprecated
	public synchronized ID getTxInHeight(Lock txInAddress) {
		ID height = null;
		try {
			ResultSet resultSet;
			if (txInAddress.getReadableLock() != null) {
				PreparedStatement preparedStatement = connection
						.prepareStatement("SELECT * FROM ACCOUNT WHERE address=?");
				preparedStatement.setBytes(1, txInAddress.getBytes(LockShape.AI));
				resultSet = preparedStatement.executeQuery();
			} else if (txInAddress.getId() != null) {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM ACCOUNT WHERE id=?");
				preparedStatement.setLong(1, txInAddress.getId().longValue());
				resultSet = preparedStatement.executeQuery();
			} else {
				throw new NullPointerException("Address and SerialNumber cannot both be null");
			}
			if (resultSet.next()) {
				height = new ID(BigInteger.valueOf(resultSet.getLong("address_update_height")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return height;
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
	public synchronized ID getTotalAccountNumbers(ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCHiveTailHeight());
		EQCHive eqcHive = null;
		eqcHive = getEQCHive(height, true);
		return eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getTotalPassportNumbers();
	}

	@Deprecated
	public synchronized boolean setTotalAccountNumbers(ID numbers) {
		int result = 0;
//		try {
//			if (getEQCHiveTailHeight() != null) {
//				result = statement.executeUpdate(
//						"UPDATE SYNCHRONIZATION SET total_account_numbers='" + numbers.longValue() + "' WHERE key='1'");
//
//			} else {
//				result = statement.executeUpdate(
//						"INSERT INTO SYNCHRONIZATION (total_account_numbers) VALUES('" + numbers.longValue() + "')");
//			}
////			Log.info("result: " + result);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized byte[] getEQCHeaderHash(ID height) throws Exception {
//		EQCType.assertNotBigger(height, getEQCHiveTailHeight());
		byte[] hash = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM EQCHIVE WHERE height=?");
		preparedStatement.setLong(1, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			hash = resultSet.getBytes("eqcheader_hash");
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
			throws SQLException, NoSuchFieldException, IllegalStateException, IOException {
		Vector<Transaction> transactions = new Vector<Transaction>();
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE qos>='9' OR " + "(qos='8') OR "
						+ "(qos='4' AND receieved_timestamp<=?) OR " + "(qos='2' AND receieved_timestamp<=?) OR "
						+ "(qos='1' AND receieved_timestamp<=?) AND "
						+ "(record_status = FALSE) ORDER BY qos DESC, receieved_timestamp ASC");
		preparedStatement.setLong(1, (System.currentTimeMillis() - 200000));
		preparedStatement.setLong(2, (System.currentTimeMillis() - 400000));
		preparedStatement.setLong(3, (System.currentTimeMillis() - 600000));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			byte[] bytes = resultSet.getBytes("rawdata");
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
			// Parse Transaction
			transaction = Transaction.parseTransaction(bytes);
			transactions.add(transaction);
		}
//		Collections.sort(transactions);
		return transactions;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws SQLException {
		boolean isExists = false;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND qos<=?");
		preparedStatement.setLong(1, transaction.getTxIn().getLock().getId().longValue());
		preparedStatement.setLong(2, transaction.getNonce().longValue());
		preparedStatement.setLong(3, transaction.getQosRate().longValue());
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
	 * @throws SQLException
	 */
	@Override
	public synchronized boolean saveTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
		int nResult = 0;
		PreparedStatement preparedStatement = null;
//		if (!isTransactionExistsInPool(transaction)) {
//			preparedStatement = connection.prepareStatement(
//					"INSERT INTO TRANSACTION_POOL (asset_id, txin_id, nonce, rawdata, signature, proof, qos, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			preparedStatement.setLong(1, transaction.getAssetID().longValue());
//			preparedStatement.setLong(2, transaction.getTxIn().getKey().getId().longValue());
//			preparedStatement.setLong(3, transaction.getNonce().longValue());
//			preparedStatement.setBytes(4, transaction.getRPCBytes());
//			preparedStatement.setBytes(5, transaction.getSignature());
//			preparedStatement.setBytes(6, transaction.getProof());
//			preparedStatement.setLong(7, transaction.getQosRate().longValue());
//			preparedStatement.setLong(8, System.currentTimeMillis());
//			preparedStatement.setBoolean(9, false);
//			preparedStatement.setBoolean(10, false);
//			result = preparedStatement.executeUpdate();
//		} else {
//			preparedStatement = connection.prepareStatement(
//					"UPDATE TRANSACTION_POOL SET rawdata=?, signature=?, proof=?, qos=?, receieved_timestamp=?, record_status=?, record_height=? WHERE txin_id=? AND nonce=?");
//			preparedStatement.setBytes(1, transaction.getRPCBytes());
//			preparedStatement.setBytes(2, transaction.getSignature());
//			preparedStatement.setBytes(3, transaction.getProof());
//			preparedStatement.setLong(4, transaction.getQosRate().longValue());
//			preparedStatement.setLong(5, System.currentTimeMillis());
//			preparedStatement.setBoolean(6, false);
//			preparedStatement.setBoolean(7, false);
//			preparedStatement.setLong(8, transaction.getTxIn().getKey().getId().longValue());
//			preparedStatement.setLong(9, transaction.getNonce().longValue());
//			result = preparedStatement.executeUpdate();
//		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
//			result = statement.executeUpdate("DELETE FROM TRANSACTION_POOL WHERE rawdata='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
		PreparedStatement preparedStatement = connection
				.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE signature= ?");
//		preparedStatement.setBytes(1, transaction.getSignature());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

//	@Override
//	public synchronized boolean isTransactionExistsInPool(Transaction transaction) throws SQLException {
////			 ResultSet resultSet = statement.executeQuery("SELECT * FROM TRANSACTION_POOL WHERE signature='" + transaction.getBytes(AddressShape.ADDRESS) + "'");
//			PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE signature= ?");
//			preparedStatement.setBytes(1, transaction.getSignature());
//			ResultSet resultSet  = preparedStatement.executeQuery(); 
//			if(resultSet.next()) {
//				 return true;
//			 }
//		return false;
//	}

	@Deprecated
	public synchronized Vector<Transaction> getTransactionList(Lock address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public synchronized Vector<Transaction> getTransactionList(Lock address, ID height) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	public synchronized boolean addTransaction(Transaction transaction, ID height, int trans_id) throws SQLException {
		int result = 0;
		int validCount = 0;// transaction.isCoinBase()?(transaction.getTxOutNumber()):(1+transaction.getTxOutNumber());

//		if (!transaction.isCoinBase()) {
//			result += statement
//					.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('"
//							+ height.longValue() + "','" + trans_id + "','" + true + "','"
//							+ transaction.getTxIn().getKey().getId().longValue() + "','"
//							+ transaction.getTxIn().getValue() + "')");
//		}
//		for (TxOut txOut : transaction.getTxOutList()) {
//			result += statement
//					.executeUpdate("INSERT INTO TRANSACTION (height, trans_id, io, address_id, value) VALUES('"
//							+ height.longValue() + "','" + trans_id + "','" + false + "','"
//							+ txOut.getKey().getId().longValue() + "','" + txOut.getValue() + "')");
//		}
//			Log.info("result: " + result);
		return result == validCount;
	}

	@Deprecated
	public synchronized boolean deleteTransactionFromHeight(ID height) throws SQLException {
		int result = 0;
//		result = statement.executeUpdate("DELETE FROM TRANSACTION WHERE height>='" + height.longValue() + "'");
//		Log.info("result: " + result);
		return result >= ONE_ROW;
	}


	@Deprecated
	public synchronized int getTransactionNumbersIn24hours(Lock address, ID currentHeight) {
		int numbers = 0;
		long heightOffset = ((currentHeight.longValue() - 8640) > 0) ? (currentHeight.longValue() - 8640) : 0;
		try {
			ResultSet resultSet;
			if (address.getId() == null) {
				address.setId(getAddressID(address));
			}

//			resultSet = statement.executeQuery("SELECT * FROM TRANSACTION WHERE address_id = '"
//					+ address.getSerialNumber().longValue() + "' AND height >='" + heightOffset + "'");

			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM TRANSACTION WHERE address_id = ? AND height >= ? AND io = TRUE");
			preparedStatement.setLong(1, address.getId().longValue());
			preparedStatement.setLong(2, heightOffset);
			resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				++numbers;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return numbers;
	}

//	@Deprecated
//	public synchronized boolean addAllTransactions(EQCHive eqcBlock) throws SQLException {
//		int isSuccessful = 0;
//		Vector<Transaction> transactions = eqcBlock.getTransactions().getNewTransactionList();
//		for(int i=0; i<transactions.size(); ++i) {
//			if(addTransaction(transactions.get(i), eqcBlock.getHeight(), i)) {
//				++isSuccessful;
//			}
//		}
//		return isSuccessful == transactions.size();
//	}

	@Deprecated
	public synchronized boolean isSignatureExists(byte[] signature) {
		try {
//			 ResultSet resultSet = statement.executeQuery("SELECT * FROM SIGNATURE_HASH WHERE signature='" + signature + "'");
			PreparedStatement preparedStatement = connection
					.prepareStatement("SELECT * FROM SIGNATURE_HASH WHERE signature = ?");
			preparedStatement.setBytes(1, Util.EQCCHA_MULTIPLE_DUAL(signature, Util.ONE, false, true));
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return false;
	}

	@Deprecated
	public synchronized boolean addAllAddress(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(Passport passport : eqcBlock.getTransactions().getNewPassportList()) {
//			isSuccess = appendAddress(passport, eqcBlock.getHeight());
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized boolean addAllPublicKeys(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(CompressedPublickey publicKey : eqcBlock.getTransactions().getNewCompressedPublickeyList()) {
//			isSuccess = savePublicKey(publicKey, eqcBlock.getHeight());
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized boolean appendSignature(ID height, int trans_id, ID txin_id, byte[] signature) {
		int result = 0;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(
					"INSERT INTO SIGNATURE_HASH (height, trans_id, txin_id, signature) VALUES(?, ?, ?, ?)");
			preparedStatement.setLong(1, height.longValue());
			preparedStatement.setInt(2, trans_id);
			preparedStatement.setLong(3, txin_id.longValue());
			preparedStatement.setBytes(4, Util.EQCCHA_MULTIPLE_DUAL(signature, Util.ONE, false, true));
			result = preparedStatement.executeUpdate();
			Log.info("result: " + result);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized boolean addAllSignatures(EQCHive eqcBlock) {
		boolean isSuccess = false;
//		for(int i=0; i<eqcBlock.getSignatures().getSignatureList().size(); ++i) {
//			isSuccess = appendSignature(eqcBlock.getHeight(), i, eqcBlock.getTransactions().getNewTransactionList().get(i+1).getTxIn().getPassport().getId(), eqcBlock.getSignatures().getSignatureList().get(i));
//		}
		return isSuccess;
	}

	@Deprecated
	public synchronized long getBalanceFromAccount(Lock address) {
		long balance = 0;
//		try {
//			ResultSet resultSet = statement
//					.executeQuery("SELECT * FROM ACCOUNT WHERE address='" + address.getReadableLock() + "'");
//			if (resultSet.next()) {
//				balance = resultSet.getLong("balance");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return balance;
	}

	@Deprecated
	public synchronized boolean updateBalanceInAccount(Lock address, long balance) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET balance = ? where address = ?");
			preparedStatement.setLong(1, balance);
			preparedStatement.setString(2, address.getReadableLock());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
		return result == ONE_ROW;
	}

	@Deprecated
	public synchronized int getNonce(Lock address) {
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

	@Deprecated
	public synchronized boolean updateNonce(Lock address, int nonce) {
		int result = 0;
		PreparedStatement preparedStatement;
		try {
			preparedStatement = connection.prepareStatement("UPDATE ACCOUNT SET nonce = ? where address = ?");
			preparedStatement.setInt(1, nonce);
			preparedStatement.setString(2, address.getReadableLock());
			result = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.toString());
		}
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
	public synchronized Passport getPassportSnapshot(ID accountID, ID height)
			throws ClassNotFoundException, Exception {
		EQCType.assertNotBigger(height, getEQCHiveTailHeight());
		Passport passport = null;
		
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT * FROM PASSPORT_SNAPSHOT WHERE id=? AND snapshot_height <=? ORDER BY snapshot_height DESC LIMIT 1");
		preparedStatement.setLong(1, accountID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			passport = Passport.parsePassport(resultSet.getBytes("account"));
		}

//		if(account == null) {
//			throw new IllegalStateException("getAccountSnapshot No. " + accountID + " relevant Account is NULL");
//		}

		return passport;
	}

	@Override
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

	public synchronized boolean isPassportSnapshotExists(ID accountID, ID height) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM ACCOUNT_SNAPSHOT WHERE id=? AND snapshot_height=?");
		preparedStatement.setLong(1, accountID.longValue());
		preparedStatement.setLong(2, height.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public synchronized boolean savePassportSnapshot(Passport passport, ID height) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement = null;
		if (isPassportSnapshotExists(passport.getId(), height)) {
			preparedStatement = connection.prepareStatement(
					"UPDATE PASSPORT_SNAPSHOT SET passport = ?, snapshot_height = ? where id = ?");
			preparedStatement.setBytes(1, passport.getBytes());
			preparedStatement.setLong(2, height.longValue());
			result = preparedStatement.executeUpdate();
//					Log.info("UPDATE Account: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		} else {
			preparedStatement = connection.prepareStatement(
					"INSERT INTO PASSPORT_SNAPSHOT (id, passport, snapshot_height) VALUES (?, ?, ?)");
			preparedStatement.setLong(1, passport.getId().longValue());
			preparedStatement.setBytes(2, passport.getBytes());
			preparedStatement.setLong(3, height.longValue());
			result = preparedStatement.executeUpdate();
//				Log.info("INSERT Account ID: " + account.getId() +  "'s Snapshot at height: " + account.getUpdateHeight() + " Result：" + result);
		}
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection
				.prepareStatement("DELETE FROM ACCOUNT_SNAPSHOT WHERE snapshot_height " + (isForward ? ">=?" : "<=?"));
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
		preparedStatement.setLong(1, transaction.getTxIn().getLock().getId().longValue());
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
		preparedStatement.setLong(1, transaction.getTxIn().getLock().getId().longValue());
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
			hash = getEQCHeaderHash(height);
		} else if (height.equals(currentTailHeight)) {
			hash = getEQCHeaderHash(height.getPreviousID());
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
		balance.deposit(getPassport(nest.getId(), Mode.GLOBAL).getBalance());
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE txin_id=? AND record_status=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		preparedStatement.setBoolean(2, false);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			balance.withdraw(ID.valueOf(resultSet.getLong("txin_value")));
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
	public IPList<O> getMinerList() throws SQLException, Exception {
		IPList<O> ipList = new IPList();
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
	public IPList<O> getFullNodeList() throws SQLException, Exception {
		IPList<O> ipList = new IPList();
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
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=?");
		preparedStatement.setLong(1, nest.getId().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transaction = Transaction.parseTransaction(resultSet.getBytes("rawdata"));
			transaction.getTxIn().getLock().setId(nest.getId());
			transactionList.add(transaction);
		}
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
			transactionIndex.setId(ID.valueOf(resultSet.getLong("txin_id")));
			transactionIndex.setNonce(ID.valueOf(resultSet.getLong("nonce")));
			transactionIndex.setProof(resultSet.getBytes("proof"));
			transactionIndexList.addTransactionIndex(transactionIndex);
		}
		return transactionIndexList;
	}

	private Transaction getTransactionInPool(TransactionIndex transactionIndex) throws SQLException, Exception {
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE txin_id=? AND nonce=? AND proof=?");
		preparedStatement.setLong(1, transactionIndex.getId().longValue());
		preparedStatement.setLong(2, transactionIndex.getNonce().longValue());
		preparedStatement.setBytes(3, transactionIndex.getProof());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			transaction = Transaction.parseTransaction(resultSet.getBytes("rawdata"));
		}
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

	@Override
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
	public Passport getPassport(ID id, ID height) throws Exception {
		Passport passport = null;
		if (height.equals(getEQCHiveTailHeight())) {
			passport = getPassport(id, Mode.GLOBAL);
		} else {
			passport = getPassportSnapshot(id, height);
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
		Passport account = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + getPassportTableName(mode));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			account = Passport.parsePassport(resultSet.getBytes("bytes"));
			savePassport(account, Mode.GLOBAL);
		}
		return true;
	}

	@Override
	public boolean takePassportSnapshot(Mode mode, ID height) throws SQLException, Exception {
		Passport account = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM " + ((mode == Mode.MINING) ? "ACCOUNT_MINING" : "ACCOUNT_VALID"));
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			account = Passport.parsePassport(resultSet.getBytes("bytes"));
			savePassportSnapshot(account, height);
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
		eqcHive2 = getEQCHive(eqcHive.getHeight(), false);
		EQCType.assertEqual(eqcHive.getBytes(), Objects.requireNonNull(eqcHive2).getBytes());
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
		if (getEQCHive(height, true) != null) {
			throw new IllegalStateException("EQCHive No." + height + " delete failed");
		}
		return true;
	}

	public synchronized EQCHiveRoot getEQCHeaderFile(ID height) {
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
	public  TransactionList<byte[]> getTransactionListInPool(TransactionIndexList<byte[]> transactionIndexList)
			throws SQLException, Exception {
		TransactionList<byte[]> transactionList = new TransactionList();
		for (TransactionIndex<byte[]> transactionIndex : transactionIndexList.getTransactionIndexList()) {
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
	public boolean saveLock(Lock lock, Mode mode) throws Exception {
		int rowCounter = 0;
		PreparedStatement preparedStatement = null;
		if (getLock(lock.getReadableLock(), mode) != null) {
			preparedStatement = connection.prepareStatement("UPDATE "
					+ getLockTableName(mode)
					+ " SET id = ?, passport_id = ?,  readable_lock = ?, publickey = ? WHERE id = ?");
		} else {
			preparedStatement = connection
					.prepareStatement("INSERT INTO " + getPassportTableName(mode)
							+ " (id, passport_id, readable_lock, publickey) VALUES (?, ?, ?, ?)");
		}
		
		preparedStatement.setLong(1, lock.getId().longValue());
		preparedStatement.setLong(2, lock.getPassportId().longValue());
		preparedStatement.setString(3, lock.getReadableLock());
		if(lock.getPublickey() == null) {
			preparedStatement.setNull(4, Types.BINARY);
		}
		else {
			preparedStatement.setBytes(4, lock.getPublickey());
		}
		rowCounter = preparedStatement.executeUpdate();
		
		EQCType.assertEqual(rowCounter, ONE_ROW);
		return true;
	}

	@Override
	public Lock getLock(ID id, Mode mode) throws Exception {
		Lock lock = null;
//		PreparedStatement preparedStatement = connection.prepareStatement(
//				"SELECT * FROM " + getLockTableName(mode) + " WHERE id=?");
//		preparedStatement.setLong(1, id.longValue());
//		ResultSet resultSet = preparedStatement.executeQuery();
//		if (resultSet.next()) {
//			lock = Passport.parsePassport(resultSet.getBytes("bytes"));
//		}
		return lock;
	}

	@Override
	public Lock getLock(ID id, ID height) throws Exception {
		Lock lock = null;
//		if (height.equals(getEQCHiveTailHeight())) {
//			PreparedStatement preparedStatement = connection
//					.prepareStatement("SELECT * FROM " + LOCK_GLOBAL + " WHERE id=?");
//			preparedStatement.setLong(1, id.longValue());
//			ResultSet resultSet = preparedStatement.executeQuery();
//			if (resultSet.next()) {
//				lock = Passport.parsePassport(resultSet.getBytes("bytes"));
//			}
//		} else {
//			account = getAccountSnapshot(id, height);
//		}
		return lock;
	}

	@Override
	public Lock getLock(String readableLock, Mode mode) throws Exception {
		Lock lock = null;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM "
				+ getLockTableName(mode) + " WHERE readable_lock = ?");
		preparedStatement.setString(1, readableLock);
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			lock = new Lock(resultSet.getBytes("bytes"));
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
	public Lock getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock getLockSnapshot(byte[] addressAI, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLockSnapshot(Lock lock, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteLockSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mergeLock(Mode mode) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean takeLockSnapshot(Mode mode, ID height) throws SQLException, Exception {
		return false;
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
					+ LOCK_GLOBAL + " WHERE id >= ?");
			EQCHive eqcHive = getEQCHive(height, false);
			preparedStatement.setLong(1, eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getTotalLockNumbers().longValue());
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
		ID totalNewLockNumbers = ID.ZERO;
		ID tailHeight = getEQCHiveTailHeight();
		if(height.compareTo(tailHeight) > 0) {
			throw new IllegalStateException("");
		}
		else if(height.equals(tailHeight)) {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ PASSPORT_GLOBAL);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		else {
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
					+ PASSPORT_GLOBAL + " WHERE id >= ?");
			EQCHive eqcHive = getEQCHive(height, false);
			preparedStatement.setLong(1, eqcHive.getEQcoinSeed().getEQcoinSeedRoot().getTotalPassportNumbers().longValue());
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				totalNewLockNumbers = new ID(resultSet.getLong(1));
			}
		}
		return totalNewLockNumbers;
	}

	@Override
	public ID getTotalNewPassportNumbers(ChangeLog changeLog) throws Exception {
		ID totalNewLockNumbers = ID.ZERO;
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM "
				+ getPassportTableName(changeLog.getFilter().getMode()) + " WHERE id >= ?");
		preparedStatement.setLong(1, changeLog.getPreviousTotalLockNumbers().longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			totalNewLockNumbers = new ID(resultSet.getLong(1));
		}
		return totalNewLockNumbers;
	}
	
}
