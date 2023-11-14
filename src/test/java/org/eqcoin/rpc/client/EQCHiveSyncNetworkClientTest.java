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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TailInfo;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date May 11, 2020
 * @email 10509759@qq.com
 */
class EQCHiveSyncNetworkClientTest {

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
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#getEQCHive(org.eqcoin.util.ID, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetEQCHive() {
		EQCHive eqcHive = null;
		try {
			eqcHive = EQCHiveSyncNetworkClient.getEQCHive(ID.FIVE, Util.SINGULARITY_SP);
			assertNotNull(eqcHive);
			assertTrue(eqcHive.isSanity());
			Log.info(eqcHive.toString());
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	final void testGetEQCHiveRoot() {
		EQCHiveRoot eqcHiveRoot = null;
		try {
			eqcHiveRoot = EQCHiveSyncNetworkClient.getEQCHiveRoot(ID.FIVE, Util.SINGULARITY_SP);
			assertNotNull(eqcHiveRoot);
			assertTrue(eqcHiveRoot.isSanity());
			Log.info(eqcHiveRoot.toString());
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#getEQCRootProof(org.eqcoin.util.ID, org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetEQCHiveRootProof() {
		byte[] proof = null;
		try {
			proof = EQCHiveSyncNetworkClient.getEQCHiveRootProof(ID.FIVE, Util.SINGULARITY_SP);
			assertNotNull(proof);
			assertEquals(proof.length, Util.SHA3_512_LEN);
			Log.info(Util.dumpBytes(proof, 16));
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#getEQCHiveTail(org.eqcoin.rpc.object.SP)}.
	 */
	@Test
	final void testGetEQCHiveTail() {
		TailInfo tailInfo = null;
		try {
			tailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(Util.SINGULARITY_SP);
			assertNotNull(tailInfo);
			assertTrue(tailInfo.isSanity());
			Log.info(tailInfo.toString());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#getFastestServer(org.eqcoin.rpc.object.SPList)}.
	 * @throws Exception
	 */
	@Test
	final void testGetFastestServer() throws Exception {
		SP fastestSP = null;
		final SPList spList = new SPList();
		spList.addSP(Util.LOCAL_SP);
		spList.addSP(Util.SINGULARITY_SP);
		fastestSP = EQCHiveSyncNetworkClient.getFastestServer(spList);
		assertNotNull(fastestSP);
		assertTrue(fastestSP.isSanity());
		Log.info(fastestSP.toString());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#getSPList(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception
	 */
	@Test
	final void testGetSPList() throws Exception {
		SPList spList = null;
		spList = EQCHiveSyncNetworkClient.getSPList(Util.SINGULARITY_SP);
		assertNotNull(spList);
		assertTrue(spList.isSanity());
		Log.info(spList.toString());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient#registerSP(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception
	 */
	@Test
	final void testRegisterSP() throws Exception {
		Info info = null;
		info = EQCHiveSyncNetworkClient.registerSP(Util.SINGULARITY_SP);
		assertNotNull(info);
		assertTrue(info.isSanity());
		assertEquals(info.getCode(), Code.OK);
		Log.info(info.toString());
	}

}
