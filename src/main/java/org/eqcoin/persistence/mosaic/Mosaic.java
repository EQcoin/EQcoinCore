/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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
