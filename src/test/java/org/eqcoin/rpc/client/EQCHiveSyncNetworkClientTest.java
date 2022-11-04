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
