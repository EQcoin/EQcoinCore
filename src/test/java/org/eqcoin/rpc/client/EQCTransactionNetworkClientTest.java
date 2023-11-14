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
package org.eqcoin.rpc.client;

import static org.junit.jupiter.api.Assertions.*;

import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date May 13, 2020
 * @email 10509759@qq.com
 */
class EQCTransactionNetworkClientTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#registerSP(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception 
	 */
	@Test
	final void testRegisterSP() throws Exception {
		Info info = null;
		info = EQCTransactionNetworkClient.registerSP(Util.SINGULARITY_SP);
		assertNotNull(info);
		assertTrue(info.isSanity());
		assertEquals(info.getCode(), Code.OK);
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#getSPList(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception 
	 */
	@Test
	final void testGetSPList() throws Exception {
		SPList spList = null;
		spList = EQCTransactionNetworkClient.getSPList(Util.SINGULARITY_SP);
		assertNotNull(spList);
		assertTrue(spList.isSanity());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#sendTransaction(org.eqcoin.transaction.Transaction, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testSendTransaction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#getLockInfo(org.eqcoin.rpc.object.LockStatus, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetLockInfo() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#getPendingTransactionList(org.eqcoin.util.ID, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetPendingTransactionList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#ping(org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testPing() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#getTransactionIndexList(org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetTransactionIndexList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient#getTransactionList(org.eqcoin.rpc.object.TransactionIndexList, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetTransactionList() {
		fail("Not yet implemented"); // TODO
	}

}
