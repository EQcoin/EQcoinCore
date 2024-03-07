/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.persistence.mosaic.h2;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.h2.EQCH2;
import org.eqcoin.persistence.mosaic.Mosaic;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TransactionIndex;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
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
	private final static String JDBC_URL = "jdbc:h2:" + Util.TRANSACTION_POOL_DATABASE_NAME + ";DB_CLOSE_DELAY=60";
	private static MosaicH2 instance;

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

	private MosaicH2() throws SQLException {
		super(JDBC_URL);
	}

	@Override
	protected synchronized void createTable() throws SQLException {
		final Statement statement = connection.createStatement();
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


	@Override
	public boolean deleteSP(final SP sp) throws SQLException {
		int result = 0;
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement("DELETE FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, sp.getIp());
		result = preparedStatement.executeUpdate();
		Log.info("result: " + result);
		return result >= ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionInPool(final Transaction transaction) throws SQLException {
		int result = 0;
		if(Util.IsDeleteTransactionInPool) {
			final PreparedStatement preparedStatement = connection
					.prepareStatement("DELETE FROM TRANSACTION_POOL WHERE witness= ?");
			preparedStatement.setBytes(1, transaction.getWitness().getWitness());
			result = preparedStatement.executeUpdate();
		}
		Log.info("result: " + result);
		return result == ONE_ROW;
	}

	@Override
	public synchronized boolean deleteTransactionsInPool(final EQCHive eqcHive)
			throws ClassNotFoundException, Exception {
		int isSuccessful = 0;
		for (final Transaction transaction : eqcHive.getEQCoinSeeds().getNewTransactionList()) {
			if (deleteTransactionInPool(transaction)) {
				++isSuccessful;
			}
		}
		return isSuccessful == eqcHive.getEQCoinSeeds().getNewTransactionList().size();
	}

	@Override
	public Vector<Transaction> getPendingTransactionListInPool(final ID id) throws SQLException, Exception {
		final Vector<Transaction> transactionList = new Vector<>();
		Transaction transaction = null;
		final PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT rawdata FROM TRANSACTION_POOL WHERE passport_id=?");
		preparedStatement.setLong(1, id.longValue());
		final ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transaction = new Transaction().Parse(resultSet.getBytes("rawdata"));
			transactionList.add(transaction);
		}
		return transactionList;
	}

	@Override
	public byte getSPCounter(final SP ip) throws SQLException, Exception {
		byte counter = 0;
		final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, ip.getIp());
		final ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			counter = resultSet.getByte("counter");
		}
		return counter;
	}

	@Override
	public SPList getSPList(final ID flag) throws SQLException, Exception {
		final SPList spList = new SPList();
		SP sp = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		final byte flagValue = flag.byteValue();
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
	public ID getSyncTime(final SP sp) throws SQLException, Exception {
		ID sync_time = null;
		final PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=?");
		preparedStatement.setString(1, sp.getIp());
		final ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			sync_time = new ID(resultSet.getLong("sync_time"));
		}
		return sync_time;
	}

	@Override
	public TransactionIndexList getTransactionIndexListInPool(final long previousSyncTime, final long currentSyncTime)
			throws SQLException, Exception {
		final TransactionIndexList transactionIndexList = new TransactionIndexList();
		TransactionIndex transactionIndex = null;
		transactionIndexList.setSyncTime(new ID(currentSyncTime));
		final PreparedStatement preparedStatement = connection.prepareStatement(
				"SELECT passport_id, nonce, proof FROM TRANSACTION_POOL WHERE receieved_timestamp>=? AND receieved_timestamp<?");
		preparedStatement.setLong(1, previousSyncTime);
		preparedStatement.setLong(2, currentSyncTime);
		final ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			transactionIndex = new TransactionIndex();
			transactionIndex.setId(new ID(resultSet.getLong("passport_id")));
			transactionIndex.setNonce(new ID(resultSet.getLong("nonce")));
			transactionIndex.setProof(resultSet.getBytes("proof"));
			transactionIndexList.addTransactionIndex(transactionIndex);
		}
		return transactionIndexList;
	}

	private Transaction getTransactionInPool(final TransactionIndex transactionIndex) throws SQLException, Exception {
		final Transaction transaction = null;
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
	public synchronized Vector<Transaction> getTransactionListInPool()
			throws Exception {
		final Vector<Transaction> transactions = new Vector<>();
		final ByteArrayInputStream is = null;
		final long currentTime = System.currentTimeMillis();
		final PreparedStatement preparedStatement = connection
				.prepareStatement("SELECT * FROM TRANSACTION_POOL WHERE priority_value>='5' OR " + "(priority_value='4') OR "
						+ "(priority_value='3' AND receieved_timestamp<=?) OR " + "(priority_value='2' AND receieved_timestamp<=?) OR "
						+ "(priority_value='1' AND receieved_timestamp<=?) AND "
						+ "(record_status = FALSE) ORDER BY priority_value DESC, receieved_timestamp ASC");
		preparedStatement.setLong(1, (currentTime - 200000));
		preparedStatement.setLong(2, (currentTime - 400000));
		preparedStatement.setLong(3, (currentTime - 600000));
		final ResultSet resultSet = preparedStatement.executeQuery();
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
			} catch (final Exception e) {
				Log.Error("During parse transaction error occur have to delete it: " + e.getMessage());
				//				deleteTransactionInPool(resultSet.getBytes("signature"));
			}
		}
		//		Collections.sort(transactions);
		return transactions;
	}

	@Override
	public  TransactionList getTransactionListInPool(final TransactionIndexList transactionIndexList)
			throws SQLException, Exception {
		final TransactionList transactionList = new TransactionList();
		for (final TransactionIndex transactionIndex : transactionIndexList.getTransactionIndexList()) {
			transactionList.addTransaction(getTransactionInPool(transactionIndex));
		}
		return transactionList;
	}

	@Override
	public boolean isSPExists(final SP sp) throws SQLException {
		boolean isSucc = false;
		PreparedStatement preparedStatement = null;
		preparedStatement = connection.prepareStatement("SELECT * FROM SP_LIST WHERE ip=? AND flag=?");
		preparedStatement.setString(1, sp.getIp());
		preparedStatement.setInt(2, sp.getFlag().intValue());
		final ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			isSucc = true;
		}
		return isSucc;
	}

	@Override
	public synchronized boolean isTransactionExistsInPool(final Transaction transaction) throws Exception {
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
	public synchronized boolean isTransactionExistsInPool(final TransactionIndex transactionIndex) throws SQLException {
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

	@Override
	public boolean saveSP(final SP sp) throws SQLException, Exception {
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

	@Override
	public boolean saveSPCounter(final SP sp, final byte counter) throws SQLException, Exception {
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
	public boolean saveSyncTime(final SP sp, final ID syncTime) throws SQLException, Exception {
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

	/**
	 * @param transaction EQC Transaction include PublicKey and Signature so for
	 *                    every Transaction it's raw is unique
	 * @return boolean If add Transaction successful return true else return false
	 * @throws Exception
	 */
	@Override
	public synchronized boolean saveTransactionInPool(final Transaction transaction) throws Exception {
		final int result = 0;
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

}
