/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.ut.misc;

import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 19, 2020
 * @email 10509759@qq.com
 */
public class TransTest {

	public static void Tranfer(int fromId, double value, TRANSACTION_PRIORITY priority, boolean isRpc, int... toIds) {
		try {
			Transaction transaction = TransFactory.Tranfer(fromId, value, priority, toIds);
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				Util.MC().saveTransactionInPool(transaction);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void TranferChangeLock(int fromId, double value, int newLockId, TRANSACTION_PRIORITY priority, boolean isRpc,
			int... toIds) {
		try {
			Transaction transaction = TransFactory.TranferChangeLock(fromId, value, newLockId, priority, toIds);
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				Util.MC().saveTransactionInPool(transaction);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void Zion(int fromId, double value, TRANSACTION_PRIORITY priority, boolean isRpc, int... zions) {
		try {
			Transaction transaction = TransFactory.Zion(fromId, value, priority, zions);
			if(isRpc) {
				EQCTransactionNetworkClient.sendTransaction(transaction, Util.SINGULARITY_SP);
			}
			else {
				Util.MC().saveTransactionInPool(transaction);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
