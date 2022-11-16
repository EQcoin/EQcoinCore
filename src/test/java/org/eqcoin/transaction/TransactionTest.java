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
* Wandering Earth Corporation reserves any and all current and future rights,
* titles and interests in any and all intellectual property rights of Wandering Earth
* Corporation, including but not limited to discoveries, ideas, marks, concepts,
* methods, formulas, processes, codes, software, inventions, compositions, techniques,
* information and data, whether or not protectable in trademark, copyrightable
* or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.transaction;

import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.ut.misc.TransTest;
import org.eqcoin.util.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date Jul 26, 2019
 * @email 10509759@qq.com
 */
public class TransactionTest {
	
	private static boolean isRpc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.info("setUpBeforeClass");
		isRpc = false;
	}
	
	@Test
	void regresstionTest() {
//		TransTest.Zion(0, 2300, TRANSACTION_PRIORITY.ASAP, 2);
//		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, 3, 4, 5);
		TransTest.Tranfer(2, 1, TRANSACTION_PRIORITY.ASAP, false, 5);
//		TransTest.TranferChangeLock(2, 1, 13, TRANSACTION_PRIORITY.ASAP, 3);
//		TransTest.TranferChangeLock(3, 1, 12, TRANSACTION_PRIORITY.ASAP, 4);
	}
	
	@Test
	void regresstionChangeLockTest() {
		TransTest.TranferChangeLock(2, 1, 4, TRANSACTION_PRIORITY.ASAP, isRpc, 1);
	}
	
	@Test
	void regresstionZeroZionTest() {
		TransTest.Zion(0, 2000, TRANSACTION_PRIORITY.ASAP, isRpc, 2);
	}
	
	@Test
	void regresstionZionTest() {
		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, isRpc, 3);
	}
	
	@Test
	void regresstionTransferTest() {
		TransTest.Tranfer(0, 1, TRANSACTION_PRIORITY.ASAP, isRpc, 1);
	}
	
}
