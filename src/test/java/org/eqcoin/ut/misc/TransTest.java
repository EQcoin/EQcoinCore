/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation retains all current and future right, title and interest
 * in all of Wandering Earth Corporation’s intellectual property, including, without
 * limitation, inventions, ideas, concepts, code, discoveries, processes, marks,
 * methods, software, compositions, formulae, techniques, information and data,
 * whether or not patentable, copyrightable or protectable in trademark, and
 * any trademarks, copyright or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
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
