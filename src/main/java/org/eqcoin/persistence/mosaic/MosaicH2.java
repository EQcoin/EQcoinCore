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
package org.eqcoin.persistence.mosaic;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.GlobalStateH2;
import org.eqcoin.persistence.globalstate.GlobalState.Mode;
import org.eqcoin.persistence.h2.EQCH2;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.SPList;
import org.eqcoin.rpc.TransactionIndex;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 2, 2020
 * @email 10509759@qq.com
 */
public class MosaicH2 extends EQCH2 implements Mosaic {
	private final static String JDBC_URL = "jdbc:h2:" + Util.TRANSACTION_POOL_DATABASE_NAME;
	private static MosaicH2 instance;
	
	private MosaicH2() throws SQLException {
		super(JDBC_URL);
	}

	protected synchronized void createTable() throws SQLException {
		Statement statement = connection.createStatement();
		boolean result = false;
			// EQC Transaction Pool table
			result = statement.execute("CREATE TABLE IF NOT EXISTS TRANSACTION_POOL("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "passport_id BIGINT,"
					+ "nonce BIGINT,"
					+ "rawdata BINARY,"
					+ "witness BINARY,"
					+ "proof BINARY(4),"
					+ "priority_value BIGINT,"
					+ "receieved_timestamp BIGINT,"
					+ "record_status BOOLEAN,"
					+ "record_height BIGINT"
					+ ")");
			
			// Create EQcoin Network table
		result =	statement.execute("CREATE TABLE IF NOT EXISTS SP_LIST ("
					+ "key BIGINT PRIMARY KEY AUTO_INCREMENT, "
					+ "ip  VARCHAR,"
					+ "flag TINYINT,"
					+ "protocol_version TINYINT,"
					+ "counter TINYINT,"
					+ "sync_time BIGINT"
					+ ")");
			
			statement.close();
			
			if(result) {
				Log.info("Create table");
			}
	}
	
	public static MosaicH2 getInstance() throws ClassNotFoundException, SQLException {
		if(instance == null) {
			synchronized (MosaicH2.class) {
				if(instance == null) {
					instance = new MosaicH2();
				}
			}
		}
		return instance;
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
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE witness=? AND nonce=? AND priority_value<=?");
		preparedStatement.setBytes(1, transaction.getWitness().getWitness());
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
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE passport_id=? AND nonce=? AND proof=?");
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
//		PreparedStatement preparedStatement = null;
//		if (!isTransactionExistsInPool(transaction)) {
//			preparedStatement = connectionAutoCommit.prepareStatement(
//					"INSERT INTO TRANSACTION_POOL (passport_id, nonce, rawdata, witness, proof, priority_value, receieved_timestamp, record_status, record_height) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");
//			preparedStatement.setLong(1, transaction.getWitness().getPassport().getId().longValue());
//			preparedStatement.setLong(2, transaction.getNonce().longValue());
//			preparedStatement.setBytes(3, transaction.getBytes());
//			preparedStatement.setBytes(4, transaction.getWitness().getWitness());
//			preparedStatement.setBytes(5, transaction.getProof());
//			preparedStatement.setLong(6, transaction.getPriorityValue().longValue());
//			preparedStatement.setLong(7, System.currentTimeMillis());
//			preparedStatement.setBoolean(8, false);
//			preparedStatement.setNull(9, Types.BIGINT);
//			result = preparedStatement.executeUpdate();
//		} else {
//			preparedStatement = connectionAutoCommit.prepareStatement(
//					"UPDATE TRANSACTION_POOL SET rawdata=?, witness=?, proof=?, priority_value=?, receieved_timestamp=?, record_status=?, record_height=? WHERE passport_id=? AND nonce=?");
//			preparedStatement.setBytes(1, transaction.getBytes());
//			preparedStatement.setBytes(2, transaction.getWitness().getWitness());
//			preparedStatement.setBytes(3, transaction.getProof());
//			preparedStatement.setLong(4, transaction.getPriorityValue().longValue());
//			preparedStatement.setLong(5, System.currentTimeMillis());
//			preparedStatement.setBoolean(6, false);
//			preparedStatement.setNull(7, Types.BIGINT);
//			preparedStatement.setLong(8, transaction.getWitness().getPassport().getId().longValue());
//			preparedStatement.setLong(9, transaction.getNonce().longValue());
//			result = preparedStatement.executeUpdate();
//		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(Transaction transaction) throws SQLException {
		int result = 0;
		if(Util.IsDeleteTransactionInPool) {
			PreparedStatement preparedStatement = connection
					.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE witness= ?");
			preparedStatement.setBytes(1, transaction.getWitness().getWitness());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}
	
	@Override
	public synchronized boolean deleteTransactionsInPool(EQCHive eqcHive)
			throws ClassNotFoundException, Exception {
		int isSuccessful = 0;
		for (Transaction transaction : eqcHive.getEQCoinSeed().getNewTransactionList()) {
			if (deleteTransactionInPool(transaction)) {
				++isSuccessful;
			}
		}
		return isSuccessful == eqcHive.getEQCoinSeed().getNewTransactionList().size();
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
		Transaction transaction = null;
		PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE passport_id=?");
		preparedStatement.setLong(1, id.longValue());
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transaction = new Transaction().Parse(resultSet.getBytes("rawdata"));
			transactionList.add(transaction);
		}
		return transactionList;
	}

	@Override
	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws SQLException, Exception {
		TransactionIndexList transactionIndexList = new TransactionIndexList();
		TransactionIndex transactionIndex = null;
		transactionIndexList.setSyncTime(new ID(currentSyncTime));
		PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT passport_id, nonce, proof FROM TRANSACTION_POOL WHERE receieved_timestamp>=? AND receieved_timestamp<?");
		preparedStatement.setLong(1, previousSyncTime);
		preparedStatement.setLong(2, currentSyncTime);
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transactionIndex = new TransactionIndex();
			transactionIndex.setId(new ID(resultSet.getLong("passport_id")));
			transactionIndex.setNonce(new ID(resultSet.getLong("nonce")));
			transactionIndex.setProof(resultSet.getBytes("proof"));
			transactionIndexList.addTransactionIndex(transactionIndex);
		}
		return transactionIndexList;
	}

	private Transaction getTransactionInPool(TransactionIndex transactionIndex) throws SQLException, Exception {
		Transaction transaction = null;
//		PreparedStatement preparedStatement = connection
//				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE passport_id=? AND nonce=? AND proof=?");
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
	public  TransactionList getTransactionListInPool(TransactionIndexList transactionIndexList)
			throws SQLException, Exception {
		TransactionList transactionList = new TransactionList();
		for (TransactionIndex transactionIndex : transactionIndexList.getTransactionIndexList()) {
			transactionList.addTransaction(getTransactionInPool(transactionIndex));
		}
		return transactionList;
	}
	
}
