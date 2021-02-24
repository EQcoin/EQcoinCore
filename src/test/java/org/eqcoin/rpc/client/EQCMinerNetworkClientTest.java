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
package org.eqcoin.rpc.client;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.NewEQCHive;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
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
 * @date May 13, 2020
 * @email 10509759@qq.com
 */
class EQCMinerNetworkClientTest {

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
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCMinerNetworkClient#registerSP(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception 
	 */
	@Test
	final void testRegisterSP() throws Exception {
		Info info = null;
		info = EQCMinerNetworkClient.registerSP(Util.SINGULARITY_SP);
		assertNotNull(info);
		assertTrue(info.isSanity());
		assertEquals(info.getCode(), Code.OK);
		Log.info(info.toString());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCMinerNetworkClient#getSPList(org.eqcoin.rpc.object.SP)}.
	 * @throws Exception 
	 */
	@Test
	final void testGetSPList() throws Exception {
		SPList spList = null;
		spList = EQCMinerNetworkClient.getSPList(Util.SINGULARITY_SP);
		assertNotNull(spList);
		assertTrue(spList.isSanity());
		Log.info(spList.toString());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCMinerNetworkClient#broadcastNewEQCHive(org.eqcoin.rpc.object.NewEQCHive, org.eqcoin.rpc.object.SP)}.
	 * @throws Exception 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	final void testBroadcastNewEQCHive() throws ClassNotFoundException, SQLException, Exception {
		EQCHive eqcHive = null;
		NewEQCHive newEQCHive = new NewEQCHive();
		newEQCHive.setEQCHive(GlobalStateH2.getInstance().getEQCHiveFile(ID.ZERO, false));
		Info info = null;
		info = EQCMinerNetworkClient.broadcastNewEQCHive(newEQCHive, Util.SINGULARITY_SP);
		assertNotNull(info);
		assertTrue(info.isSanity());
		assertEquals(info.getCode(), Code.OK);
		Log.info(info.toString());
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.avro.EQCMinerNetworkClient#getFastestServer(org.eqcoin.rpc.object.SPList)}.
	 * @throws Exception 
	 */
	@Test
	final void testGetFastestServer() throws Exception {
		SP fastestSP = null;
		SPList spList = new SPList();
		spList.addSP(Util.LOCAL_SP);
		spList.addSP(Util.SINGULARITY_SP);
		fastestSP = EQCMinerNetworkClient.getFastestServer(spList);
		assertNotNull(fastestSP);
		assertTrue(fastestSP.isSanity());
		Log.info(fastestSP.toString());
	}

}
