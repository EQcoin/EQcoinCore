/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.persistence.mosaic;

import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TransactionIndex;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;

/**
 * @author Xun Wang
 * @date Jun 2, 2020
 * @email 10509759@qq.com
 */
public interface Mosaic {
	
		// TransactionPool relevant interface for H2, avro.
		public boolean isTransactionExistsInPool(Transaction transaction) throws Exception;

		public boolean isTransactionExistsInPool(TransactionIndex transactionIndex) throws Exception;

		public boolean saveTransactionInPool(Transaction transaction) throws Exception;

		public boolean deleteTransactionInPool(Transaction transaction) throws Exception;

		public boolean deleteTransactionsInPool(EQCHive eqcHive) throws Exception;

		public Vector<Transaction> getTransactionListInPool() throws Exception;

		public Vector<Transaction> getPendingTransactionListInPool(ID id) throws Exception;

		public TransactionIndexList getTransactionIndexListInPool(long previousSyncTime, long currentSyncTime)
				throws Exception;

		public TransactionList getTransactionListInPool(TransactionIndexList transactionIndexList)
				throws Exception;

		// EQC service provider relevant interface for H2, avro.
		public boolean isSPExists(SP sp) throws Exception;

		public boolean saveSP(SP sp) throws Exception;

		public boolean deleteSP(SP sp) throws Exception;

		public boolean saveSyncTime(SP sp, ID syncTime) throws Exception;

		public ID getSyncTime(SP sp) throws Exception;

		public boolean saveSPCounter(SP sp, byte counter) throws Exception;

		public byte getSPCounter(SP sp) throws Exception;

		public SPList getSPList(ID flag) throws Exception;
		
}
