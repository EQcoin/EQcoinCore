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

import com.eqcoin.avro.O;
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
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.rpc.client.TransactionNetworkClient;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 29, 2019
 * @email 10509759@qq.com
 */
public class EQCBlockChainRPC implements EQCBlockChain {
	private static EQCBlockChainRPC instance;
	// Current fastest Miner Server
	private IP ip;
	
	private EQCBlockChainRPC() {
		try {
//			IPList ipList = EQCBlockChainH2.getInstance().getMinerList();
//			ip = MinerNetworkClient.getFastestServer(ipList);
			if(ip == null) {
				ip = Util.SINGULARITY_IP;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	public synchronized static EQCBlockChain getInstance() {
		if (instance == null) {
			synchronized (EQCBlockChainH2.class) {
				if (instance == null) {
					instance = new EQCBlockChainRPC();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveEQCBlock(com.eqchains.blockchain.EQCHive)
	 */
	@Override
	public boolean saveEQCHive(EQCHive eqcHive) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getEQCBlock(com.eqchains.util.ID, boolean)
	 */
	@Override
	public EQCHive getEQCHive(ID height, boolean isSegwit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteEQCBlock(com.eqchains.util.ID)
	 */
	@Override
	public boolean deleteEQCHive(ID height) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#isTransactionExistsInPool(com.eqchains.blockchain.transaction.Transaction)
	 */
	@Override
	public boolean isTransactionExistsInPool(Transaction transaction) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveTransactionInPool(com.eqchains.blockchain.transaction.Transaction)
	 */
	@Override
	public boolean saveTransactionInPool(Transaction transaction) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteTransactionInPool(com.eqchains.blockchain.transaction.Transaction)
	 */
	@Override
	public boolean deleteTransactionInPool(Transaction transaction) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteTransactionsInPool(com.eqchains.blockchain.EQCHive)
	 */
	@Override
	public boolean deleteTransactionsInPool(EQCHive eqcBlock) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getTransactionListInPool()
	 */
	@Override
	public Vector<Transaction> getTransactionListInPool() throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getPendingTransactionListInPool(com.eqchains.util.ID)
	 */
	@Override
	public Vector<Transaction> getPendingTransactionListInPool(Nest nest) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#isTransactionMaxNonceExists(com.eqchains.rpc.Nest)
	 */
	@Override
	public boolean isTransactionMaxNonceExists(Nest nest) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveTransactionMaxNonce(com.eqchains.rpc.Nest, com.eqchains.rpc.MaxNonce)
	 */
	@Override
	public boolean saveTransactionMaxNonce(Nest nest, MaxNonce maxNonce) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getTransactionMaxNonce(com.eqchains.rpc.Nest)
	 */
	@Override
	public MaxNonce getTransactionMaxNonce(Nest nest)
			throws Exception {
		MaxNonce maxNonce = null;
		maxNonce = TransactionNetworkClient.getMaxNonce(nest, ip);
		return maxNonce;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteTransactionMaxNonce(com.eqchains.rpc.Nest)
	 */
	@Override
	public boolean deleteTransactionMaxNonce(Nest nest) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getBalance(com.eqchains.rpc.Nest)
	 */
	@Override
	public Balance getBalance(Nest nest) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getTransactionIndexListInPool(long, long)
	 */
	@Override
	public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
			throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getTransactionListInPool(com.eqchains.rpc.TransactionIndexList)
	 */
	@Override
	public TransactionList getTransactionListInPool(TransactionIndexList transactionIndexList)
			throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getEQCHeaderHash(com.eqchains.util.ID)
	 */
	@Override
	public byte[] getEQCHeaderHash(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getEQCHeaderBuddyHash(com.eqchains.util.ID)
	 */
	@Override
	public byte[] getEQCHeaderBuddyHash(ID height, ID currentHeight) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getEQCBlockTailHeight()
	 */
	@Override
	public ID getEQCHiveTailHeight() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveEQCBlockTailHeight(com.eqchains.util.ID)
	 */
	@Override
	public boolean saveEQCHiveTailHeight(ID height) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getTotalAccountNumbers(com.eqchains.util.ID)
	 */
	@Override
	public ID getTotalAccountNumbers(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#isIPExists(java.lang.String, com.eqchains.persistence.EQCBlockChainH2.NODETYPE)
	 */
	@Override
	public boolean isIPExists(IP ip, NODETYPE nodeType) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#isMinerExists(java.lang.String)
	 */
	@Override
	public boolean isMinerExists(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveMiner(java.lang.String)
	 */
	@Override
	public boolean saveMiner(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteMiner(java.lang.String)
	 */
	@Override
	public boolean deleteMiner(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveMinerSyncTime(java.lang.String, long)
	 */
	@Override
	public boolean saveMinerSyncTime(IP ip, long syncTime) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getMinerSyncTime(java.lang.String)
	 */
	@Override
	public long getMinerSyncTime(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getMinerList()
	 */
	@Override
	public IPList getMinerList() throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#isFullNodeExists(java.lang.String)
	 */
	@Override
	public boolean isFullNodeExists(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#saveFullNode(java.lang.String)
	 */
	@Override
	public boolean saveFullNode(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#deleteFullNode(java.lang.String)
	 */
	@Override
	public boolean deleteFullNode(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#getFullNodeList()
	 */
	@Override
	public IPList getFullNodeList() throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#close()
	 */
	@Override
	public boolean close() throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.EQCBlockChain#dropTable()
	 */
	@Override
	public boolean dropTable() throws Exception, SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public SignHash getSignHash(ID id) throws Exception {
		SignHash signHash = null;
		signHash = TransactionNetworkClient.getSignHash(id, ip);
		return signHash;
	}

	@Override
	public boolean saveIPCounter(IP ip, int counter) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public int getIPCounter(IP ip) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	public void deleteAccountFromTo(ID fromID, ID toID) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void deleteEQCHiveFromTo(ID fromHeight, ID toHeight) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Passport getPassport(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean clearPassport(Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mergePassport(Mode mode) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean takePassportSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEQCHiveExists(ID height) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Passport getPassport(ID id, ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deletePassport(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveLock(EQCLockMate lock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EQCLockMate getLock(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EQCLockMate getLock(ID id, ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteLock(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean clearLock(Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Passport getPassportSnapshot(ID accountID, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deletePassportSnapshotFrom(ID height, boolean isForward) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EQCLockMate getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLockSnapshot(EQCLockMate lock, ID height) throws SQLException, Exception {
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
	public boolean takeLockSnapshot(Mode mode, ChangeLog changeLog) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Connection getConnection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID getTotalLockNumbers(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID getTotalNewLockNumbers(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID getTotalPassportNumbers(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID getTotalNewPassportNumbers(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLockExists(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean savePassport(ID passportId, byte[] passportBytes, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte[] getPassportBytes(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPassportExists(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean savePassportSnapshot(ID passportID, byte[] passportBytes, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EQCLockMate getLock(EQCLock eqcLock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID isLockExists(EQCLock eqcLock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
