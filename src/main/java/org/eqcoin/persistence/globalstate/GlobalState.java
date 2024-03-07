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
package org.eqcoin.persistence.globalstate;

import java.sql.SQLException;
import java.sql.Savepoint;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.persistence.globalstate.storage.GSStateVariable;
import org.eqcoin.persistence.globalstate.storage.GSStateVariable.GSState;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.util.ID;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Oct 2, 2018
 * @email 10509759@qq.com
 */
public interface GlobalState {

	public class EQCHiveTable {
		public final static String EQCHIVE = "EQCHIVE";
		public final static String HEIGHT = "height";
		public final static String ROOT_BODY = "root_body";
		public final static String EQCOIN_SEEDS = "eqcoin_seeds";
	}

	public class GSStateVariableTable {
		public final static String GSSTATEVARIABLE = "GSSTATEVARIABLE";
		public final static String SERIAL_NUMBER = "serial_number";
		public final static String GS_STATE = "gs_state";
		public final static String HEIGHT = "height";
		public final static String DATA = "data";
	}

	public class LockMateTable {
		public final static String ID = "id";
		public final static String KEY = "key";
		public final static String LOCKMATE_GLOBAL = "LOCKMATE_GLOBAL";
		public final static String LOCKMATE_SNAPSHOT = "LOCKMATE_SNAPSHOT";
		public final static String LOCKMATE_WALLET = "LOCKMATE_WALLET";
		public final static String PROOF = "proof";
		public final static String PUBLICKEY = "publickey";
		public final static String SNAPSHOT_HEIGHT = "snapshot_height";
		public final static String STATUS = "status";
		public final static String TYPE = "type";
	}

	public enum Mode {
		GLOBAL, WALLET
	}

	public class PassportTable {
		public final static String BALANCE = "balance";
		public final static String ID = "id";
		public final static String KEY = "key";
		public final static String LOCK_ID = "lock_id";
		public final static String NONCE = "nonce";
		public final static String PASSPORT_GLOBAL = "PASSPORT_GLOBAL";
		public final static String PASSPORT_SNAPSHOT = "PASSPORT_SNAPSHOT";
		public final static String PASSPORT_WALLET = "PASSPORT_WALLET";
		public final static String SNAPSHOT_HEIGHT = "snapshot_height";
		public final static String STORAGE = "storage";
		public final static String TYPE = "type";
		public final static String UPDATE_HEIGHT = "update_height";
	}

	public interface Plantable {
		public void planting() throws Exception;
	}

	public class Statistics {
		private ID totalLockMateNumbers;
		private ID totalPassportNumbers;
		private Value totalSupply;
		private ID totalTransactionNumbers;
		public ID getTotalLockMateNumbers() {
			return totalLockMateNumbers;
		}
		public ID getTotalPassportNumbers() {
			return totalPassportNumbers;
		}
		/**
		 * @return the totalSupply
		 */
		public Value getTotalSupply() {
			return totalSupply;
		}
		/**
		 * @return the totalTransactionNumbers
		 */
		public ID getTotalTransactionNumbers() {
			return totalTransactionNumbers;
		}
		public void setTotalLockMateNumbers(final ID totalLockMateNumbers) {
			this.totalLockMateNumbers = totalLockMateNumbers;
		}
		public void setTotalPassportNumbers(final ID totalPassportNumbers) {
			this.totalPassportNumbers = totalPassportNumbers;
		}
		/**
		 * @param totalSupply the totalSupply to set
		 */
		public void setTotalSupply(final Value totalSupply) {
			this.totalSupply = totalSupply;
		}
		/**
		 * @param totalTransactionNumbers the totalTransactionNumbers to set
		 */
		public void setTotalTransactionNumbers(final ID totalTransactionNumbers) {
			this.totalTransactionNumbers = totalTransactionNumbers;
		}
	}

	public static String MINING = "MINING";

	public static String SYNC_MAX_TAIL = "SYNC_MAX_TAIL";

	public static String VALID_NEW_TAIL = "VALID_NEW_TAIL";

	public static String VALID_NEXT_HIVE = "VALID_NEXT_HIVE";

	// 2021-02-22 Remove this due to Wallet will use different db
	public static String getLockMateTableName(final Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = LockMateTable.LOCKMATE_GLOBAL;
		}
		else if(mode == Mode.WALLET) {
			table = LockMateTable.LOCKMATE_WALLET;
		}
		else {
			throw new IllegalStateException("Invalid Mode: " + mode);
		}
		return table;
	}

	// 2021-02-22 Remove this due to Wallet will use different db
	public static String getPassportTableName(final Mode mode) {
		String table = null;
		if(mode == Mode.GLOBAL) {
			table = PassportTable.PASSPORT_GLOBAL;
		}
		else if(mode == Mode.WALLET) {
			table = PassportTable.PASSPORT_WALLET;
		}
		else {
			throw new IllegalStateException("Invalid Mode: " + mode);
		}
		return table;
	}

	// Release the relevant database resource
	public boolean close() throws Exception;

	public <T> void commit(T checkPoint) throws Exception;

	public void createStateObjectHive(String hiveName) throws Exception;

	public boolean deleteEQCHive(ID height) throws Exception;

	public boolean deleteLockMate(ID id) throws Exception;

	public boolean deleteLockMateSnapshotFrom(ID height, boolean isForward) throws Exception;

	public boolean deletePassport(ID id) throws Exception;

	public boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws Exception;

	//	/**
	//	 * Get Lock from the specific height if which doesn't exists will return null.
	//	 * If the height equal to current tail's height will retrieve the Lock from
	//	 * LOCK_GLOBAL table otherwise will try retrieve it according to Lock snapshot
	//	 * table to determine if it's publickey should exists.
	//	 * <p>
	//	 *
	//	 * @param id
	//	 * @param height
	//	 * @return
	//	 * @throws Exception
	//	 */
	////	public LockMate getLockMate(ID id, ID height) throws Exception;

	public void deleteStateObject(String hiveName, byte[] key) throws Exception;

	public void deleteStateObjectHive(String hiveName) throws Exception;

	public byte[] getEQCHive(ID height) throws Exception;

	public EQCHiveRoot getEQCHiveRoot(ID height) throws Exception;

	//	public boolean clearLockMate() throws Exception;

	public byte[] getEQCHiveRootProof(ID height) throws Exception;

	public ID getEQCHiveTailHeight() throws Exception;

	public <T extends GSStateVariable> T getGSStateVariable(GSState gsState, ID height) throws Exception;

	public ID getLastEQCHiveHeight() throws Exception;

	public ID getLastLockMateId() throws Exception;

	//	public byte[] getPassportBytes(ID id) throws Exception;

	public ID getLastPassportId() throws Exception;

	/**
	 * Get LockMate from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public LockMate getLockMate(ID id) throws Exception;

	//	/**
	//	 * Get passport from the specific height if which doesn't exists will return
	//	 * null. If the height equal to current tail's height will retrieve the passport
	//	 * from global state otherwise will try retrieve it from snapshot.
	//	 * <p>
	//	 *
	//	 * @param id
	//	 * @param height
	//	 * @return
	//	 * @throws Exception
	//	 */
	////	public Passport getPassport(ID id, ID height) throws Exception;

	public LockMate getLockMate(Lock lock) throws Exception;

	//	public boolean clearPassport() throws Exception;

	public LockMate getLockMateSnapshot(ID lockMateId, ID height) throws Exception;

	/**
	 * Get Passport from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 *
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Passport getPassport(ID id) throws Exception;

	/**
	 * Get Passport from relevant state DB according to it's lock's ID.
	 *
	 * @param lockMateId
	 * @return Passport
	 * @throws Exception
	 */
	public Passport getPassportFromLockMateId(ID lockMateId) throws Exception;

	// relevant interface for for avro, H2(optional).
	//	public boolean isEQCHiveExists(ID height) throws Exception;

	/**
	 * Retrieve relevant passport's snapshot from check point height to tail
	 * height if any. If the passport's snapshot doesn't exists in snapshot which
	 * means from from check point height to tail height the passport hasn't any
	 * change. Just search in the passport snapshot table from height H to tail
	 * height if exists record which means since H the relevant passport was changed
	 * and can retrieve it's snapshot from snapshot table otherwise which means
	 * since H the passport's state which stored in the global state hasn't any
	 * change. So the passport's global state is the same as the passport in current
	 * height's state.
	 *
	 * @param passportID
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public Passport getPassportSnapshot(ID passportID, ID height) throws Exception;

	public Passport getPassportSnapshotFromLockMateId(ID lockMateId, ID height) throws Exception;

	public <T> T getStateObject(String hiveName, byte[] key);

	public Statistics getStatistics() throws Exception;

	public ID getTotalLockMateNumbers() throws Exception;

	public ID getTotalNewLockMateNumbers() throws Exception;

	public ID getTotalNewPassportNumbers() throws Exception;

	public ID getTotalPassportNumbers() throws Exception;

	public boolean isLockMateExists(ID id) throws Exception;

	public ID isLockMateExists(Lock lock) throws Exception;

	public boolean isPassportExists(ID id) throws Exception;

	public <T> void releaseSavepoint(T savepoint) throws Exception;

	//	/**
	//	 * After verify the new block's state. Merge the new Lock states from Miner
	//	 * or Valid to Global
	//	 *
	//	 * @param mode
	//	 * @return
	//	 * @throws SQLException
	//	 * @throws Exception
	//	 */
	//	public boolean mergeLockMate() throws Exception;



	//	/**
	//	 * After verify the new block's state. Take the changed Lock's snapshot from
	//	 * Miner or Valid.
	//	 *
	//	 * @param mode
	//	 * @param height TODO
	//	 * @return
	//	 * @throws SQLException
	//	 * @throws Exception
	//	 */
	//	public boolean takeLockMateSnapshot(ID height) throws Exception;

	public <T> void rollback(T savepoint) throws Exception;

	public boolean saveEQCHive(EQCHive eqcHive) throws Exception;


	public boolean saveEQCHiveTailHeight(ID height) throws Exception;

	public boolean saveGSStateVariable(GSStateVariable gsStateVariable, ID height) throws Exception;

	//	/**
	//	 * After verify the new block's state. Merge the new Passport states from Miner
	//	 * or Valid to Global
	//	 *
	//	 * @param mode
	//	 * @return
	//	 * @throws SQLException
	//	 * @throws Exception
	//	 */
	//	public boolean mergePassport() throws Exception;

	//	/**
	//	 * After verify the new block's state. Take the changed Passport's snapshot from
	//	 * Miner or Valid.
	//	 *
	//	 * @param mode
	//	 * @param height TODO
	//	 * @return
	//	 * @throws SQLException
	//	 * @throws Exception
	//	 */
	//	public boolean takePassportSnapshot(ID height) throws Exception;

	//	// Audit layer relevant interface for H2
	//	public Vector<LockMate> getForbiddenLockList() throws Exception;

	//	public ID getTotalLivelyMasterLockNumbers

	public boolean saveLockMate(LockMate lockMate) throws Exception;

	/**
	 * Save the lock's publickey update height
	 * @param lock TODO
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean saveLockMateSnapshot(LockMate lockMate, ID height) throws Exception;

	public boolean savePassport(Passport passport) throws Exception;

	/**
	 * Save relevant changed passport's old state in snapshot in H height which is
	 * relevant passport's state before H. If in height H the passport was changed
	 * then the relevant passport have two states in H. One is the old state which
	 * store in the global state another is the new state which store in the
	 * mining/valid state. Before merge the new state from mining/valid to global
	 * need backup the old state in the snapshot. So when roll back to H can
	 * retrieve relevant passport in H's old state from snapshot if any.
	 *
	 * Due to the new create passport in H only have one state so doen't need backup
	 * it's old state. If from the check point height to tail height the passport
	 * hasn't any change then which doesn't need any backup so can't find the
	 * relevant passport in snapshot.
	 *
	 * @param passportID
	 * @param passportBytes
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean savePassportSnapshot(Passport passport, ID height) throws Exception;

	public void saveStateObject(String hiveName, byte[] key, byte[] value) throws Exception;

	public <T> T setSavepoint() throws Exception;

	// 2021-0222 here exists bug when meet fork chain before verify to tail can't
	// commit
	public <T> void updateGlobalState(EQCHive eqcHive, Savepoint savepoint, T checkPoint) throws Exception;

}
