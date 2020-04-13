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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.annotation.security.DenyAll;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.lock.EQCLock;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.Balance;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.rpc.SignHash;
import com.eqcoin.rpc.TransactionIndex;
import com.eqcoin.rpc.TransactionIndexList;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.util.ID;

/**
 * @author Xun Wang
 * @date Oct 2, 2018
 * @email 10509759@qq.com
 */
public interface EQCBlockChain {
	
	public Connection getConnection() throws Exception;

	// Lock relevant interface for H2, avro(optional).
	public boolean saveLock(EQCLockMate lock, Mode mode) throws Exception;

	/**
	 * Get Lock from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public EQCLockMate getLock(ID id, Mode mode) throws Exception;
	
	/**
	 * Get Lock from the specific height if which doesn't exists will return null.
	 * If the height equal to current tail's height will retrieve the Lock from
	 * LOCK_GLOBAL table otherwise will try retrieve it according to Lock snapshot
	 * table to determine if it's publickey should exists.
	 * <p>
	 * 
	 * @param id
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public EQCLockMate getLock(ID id, ID height) throws Exception;

	@Deprecated
	public EQCLockMate getLock(EQCLock eqcLock, Mode mode) throws Exception;
	
	public boolean isLockExists(ID id, Mode mode) throws Exception;
	
	public ID isLockExists(EQCLock eqcLock, Mode mode) throws Exception;

	public boolean deleteLock(ID id, Mode mode) throws Exception;

	public boolean clearLock(Mode mode) throws Exception;

	public ID getTotalLockNumbers(ID height) throws Exception;
	
	public ID getTotalNewLockNumbers(ChangeLog changeLog) throws Exception;
	
	// Passport relevant interface for H2, avro(optional).
	public boolean savePassport(ID passportId, byte[] passportBytes, Mode mode) throws Exception;
	
	/**
	 * Get Passport from Global state DB according to it's ID which is the latest
	 * status.
	 * <p>
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Passport getPassport(ID id, Mode mode) throws Exception;

	public byte[] getPassportBytes(ID id, Mode mode) throws Exception;
	
	public boolean isPassportExists(ID id, Mode mode) throws Exception;
	
	/**
	 * Get passport from the specific height if which doesn't exists will return
	 * null. If the height equal to current tail's height will retrieve the passport
	 * from global state otherwise will try retrieve it from snapshot.
	 * <p>
	 * 
	 * @param id
	 * @param height
	 * @return
	 * @throws Exception
	 */
	public Passport getPassport(ID id, ID height) throws Exception;

	public boolean deletePassport(ID id, Mode mode) throws Exception;

	public boolean clearPassport(Mode mode) throws Exception;
	
	public ID getTotalPassportNumbers(ID height) throws Exception;
	
	public ID getTotalNewPassportNumbers(ChangeLog changeLog) throws Exception;

	// relevant interface for for avro, H2(optional).
	public boolean isEQCHiveExists(ID height) throws Exception;

	public boolean saveEQCHive(EQCHive eqcHive) throws Exception;

	public EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception;

	public boolean deleteEQCHive(ID height) throws Exception;

	// TransactionPool relevant interface for H2, avro.
	public boolean isTransactionExistsInPool(Transaction transaction) throws Exception;

	public boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws Exception;

	public boolean saveTransactionInPool(Transaction transaction) throws Exception;

	public boolean deleteTransactionInPool(Transaction transaction) throws Exception;

	public boolean deleteTransactionsInPool(EQCHive eqcHive) throws Exception;

	public Vector<Transaction> getTransactionListInPool() throws Exception;

	public Vector<Transaction> getPendingTransactionListInPool(Nest nest) throws Exception;

	@Deprecated
	public boolean isTransactionMaxNonceExists(Nest nest) throws Exception;

	@Deprecated
	public boolean saveTransactionMaxNonce(Nest nest, MaxNonce maxNonce) throws Exception;

	public MaxNonce getTransactionMaxNonce(Nest nest) throws Exception;

	@Deprecated
	public boolean deleteTransactionMaxNonce(Nest nest) throws Exception;

	public Balance getBalance(Nest nest) throws Exception;

	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws Exception;

	public TransactionList<byte[]> getTransactionListInPool(TransactionIndexList<byte[]> transactionIndexList)
			throws Exception;

	// For sign and verify Transaction need use relevant TxIn's EQC block header's
	// hash via this function to get it from xxx.EQC.
	public byte[] getEQCHeaderHash(ID height) throws Exception;

	public byte[] getEQCHeaderBuddyHash(ID height, ID currentTailHeight) throws Exception;

	public ID getEQCHiveTailHeight() throws Exception;

	public boolean saveEQCHiveTailHeight(ID height) throws Exception;

	@Deprecated
	public ID getTotalAccountNumbers(ID height) throws Exception;

	public SignHash getSignHash(ID id) throws Exception;

	// MinerNetwork and FullNodeNetwork relevant interface for H2, avro.
	public boolean isIPExists(IP ip, NODETYPE nodeType) throws Exception;

	public boolean isMinerExists(IP ip) throws Exception;

	public boolean saveMiner(IP ip) throws Exception;

	public boolean deleteMiner(IP ip) throws Exception;

	public boolean saveMinerSyncTime(IP ip, long syncTime) throws Exception;

	public long getMinerSyncTime(IP ip) throws Exception;

	public boolean saveIPCounter(IP ip, int counter) throws Exception;

	public int getIPCounter(IP ip) throws Exception;

	public IPList getMinerList() throws Exception;

	public boolean isFullNodeExists(IP ip) throws Exception;

	public boolean saveFullNode(IP ip) throws Exception;

	public boolean deleteFullNode(IP ip) throws Exception;

	public IPList getFullNodeList() throws Exception;

	// Release the relevant database resource
	public boolean close() throws Exception;

	// Clear the relevant database table
	public boolean dropTable() throws Exception;

	// Take Lock's snapshot
	public EQCLockMate getLockSnapshot(ID lockID, ID height) throws Exception;

	/**
	 * Save the lock's publickey update height
	 * @param lock TODO
	 * @param height
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean saveLockSnapshot(EQCLockMate lock, ID height) throws Exception;

	public boolean deleteLockSnapshotFrom(ID height, boolean isForward) throws Exception;

	/**
	 * After verify the new block's state. Merge the new Lock states from Miner
	 * or Valid to Global
	 * 
	 * @param mode
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean mergeLock(Mode mode) throws Exception;

	/**
	 * After verify the new block's state. Take the changed Lock's snapshot from
	 * Miner or Valid.
	 * 
	 * @param mode
	 * @param changeLog TODO
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean takeLockSnapshot(Mode mode, ChangeLog changeLog) throws Exception;

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

	/**
	 * Save relevant changed passport's old state in snapshot in H height. 
	 * If in height H the passport was changed then the relevant passport have two
	 * states in H. One is the old state which store in the global state another is
	 * the new state which store in the mining/valid state. Before merge the new
	 * state from mining/valid to global need backup the old state in the snapshot.
	 * So when roll back to H can retrieve relevant passport in H's old state from
	 * snapshot if any.
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
	public boolean savePassportSnapshot(ID passportID, byte[] passportBytes, ID height) throws Exception;

	public boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws Exception;

	/**
	 * After verify the new block's state. Merge the new Passport states from Miner
	 * or Valid to Global
	 * 
	 * @param mode
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean mergePassport(Mode mode) throws Exception;

	/**
	 * After verify the new block's state. Take the changed Passport's snapshot from
	 * Miner or Valid.
	 * 
	 * @param mode
	 * @param changeLog TODO
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public boolean takePassportSnapshot(Mode mode, ChangeLog changeLog) throws Exception;

	// Audit layer relevant interface for H2

}
