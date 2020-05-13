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
package org.eqcoin.rpc.client;

import static org.junit.jupiter.api.Assertions.*;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.rpc.TailInfo;
import org.eqcoin.util.ID;
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
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#registerSP(org.eqcoin.rpc.SP)}.
	 */
	@Test
	final void testRegisterSP() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#getSPList(org.eqcoin.rpc.SP)}.
	 */
	@Test
	final void testGetSPList() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#getEQCHiveTail(org.eqcoin.rpc.SP)}.
	 */
	@Test
	final void testGetEQCHiveTail() {
		TailInfo tailInfo = null;
		try {
			tailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(Util.SINGULARITY_SP);
			assertNotNull(tailInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#getEQCHive(org.eqcoin.util.ID, org.eqcoin.rpc.SP)}.
	 */
	@Test
	final void testGetEQCHive() {
		EQCHive eqcHive = null;
		try {
			eqcHive = EQCHiveSyncNetworkClient.getEQCHive(ID.FIVE, Util.SINGULARITY_SP);
			assertNotNull(eqcHive);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#getEQCRootProof(org.eqcoin.util.ID, org.eqcoin.rpc.SP)}.
	 */
	@Test
	final void testGetEQCRootProof() {
		byte[] proof = null;
		try {
			proof = EQCHiveSyncNetworkClient.getEQCRootProof(ID.FIVE, Util.SINGULARITY_SP);
			assertNotNull(proof);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Test method for {@link org.eqcoin.rpc.client.EQCHiveSyncNetworkClient#getFastestServer(org.eqcoin.rpc.SPList)}.
	 */
	@Test
	final void testGetFastestServer() {
		fail("Not yet implemented"); // TODO
	}

}
