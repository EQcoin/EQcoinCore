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
