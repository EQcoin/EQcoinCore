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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import org.eqcoin.avro.O;
import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.passport.Passport;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.SPList;
import org.eqcoin.rpc.TransactionIndex;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.rpc.client.EQCMinerNetworkClient;
import org.eqcoin.rpc.client.EQCTransactionNetworkClient;
import org.eqcoin.seed.EQCSeed;
import org.eqcoin.seed.EQcoinSeedRoot;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 29, 2019
 * @email 10509759@qq.com
 */
public class EQCHiveRPC implements IEQCHive {
	private static EQCHiveRPC instance;
	// Current fastest Miner Server
	private SP sp;
	
	private EQCHiveRPC() {
		try {
//			IPList ipList = EQCBlockChainH2.getInstance().getMinerList();
//			ip = MinerNetworkClient.getFastestServer(ipList);
			if(sp == null) {
				sp = Util.SINGULARITY_SP;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	public synchronized static IEQCHive getInstance() {
		if (instance == null) {
			synchronized (EQCHiveH2.class) {
				if (instance == null) {
					instance = new EQCHiveRPC();
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

	@Override
	public byte[] getEQCHive(ID height) throws Exception {
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
	public Vector<Transaction> getPendingTransactionListInPool(ID id) throws SQLException, Exception {
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
	public byte[] getEQCHiveRootProof(ID height) throws Exception {
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

	public int getIPCounter(SP ip) throws SQLException, Exception {
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
	public boolean saveLock(LockMate lock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LockMate getLock(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockMate getLock(ID id, ID height) throws Exception {
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
	public LockMate getLockSnapshot(ID lockID, ID height) throws SQLException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveLockSnapshot(LockMate lock, ID height) throws SQLException, Exception {
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
	public boolean isPassportExists(ID id, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LockMate getLock(Lock eqcLock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ID isLockExists(Lock eqcLock, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EQCHiveRoot getEQCHiveRoot(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EQcoinSeedRoot getEQcoinSeedRoot(ID height) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSPExists(SP sp) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveSP(SP sp) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteSP(SP sp) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean saveSyncTime(SP sp, ID syncTime) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ID getSyncTime(SP sp) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean saveSPCounter(SP sp, byte counter) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public byte getSPCounter(SP sp) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SPList getSPList(ID flag) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean savePassport(Passport passport, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Passport getPassportFromLockId(ID lockId, Mode mode) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean savePassportSnapshot(Passport passport, ID height) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

}
