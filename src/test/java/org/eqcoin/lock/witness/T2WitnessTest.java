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
package org.eqcoin.lock.witness;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import java.security.Signature;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.UserProfile;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date May 30, 2020
 * @email 10509759@qq.com
 */
class T2WitnessTest {

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
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#parse(java.io.ByteArrayInputStream)}.
	 */
	@Test
	final void testParse() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#isSanity()}.
	 */
	@Test
	final void testIsSanity() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#toInnerJson()}.
	 */
	@Test
	final void testToInnerJson() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#setWitness(byte[])}.
	 * @throws Exception 
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	final void testSetWitness() throws NoSuchAlgorithmException, Exception {
		UserProfile userProfile = Keystore.getInstance().getUserProfileList().get(0);
		byte[] privateKey = Util.AESDecrypt(userProfile.getPrivateKey(), "abc");
		Signature ecdsa = null;
		ecdsa = Signature.getInstance("NONEwithECDSA", "SunEC");
		ecdsa.initSign(Util.getPrivateKey(privateKey, LockType.T2));
		ecdsa.update(Util.getSecureRandomBytes());
		byte[] sign = ecdsa.sign();
		Witness witness = new T2Witness();
		witness.setWitness(sign);
		assertNotNull(witness.getWitness());
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#getProof()}.
	 */
	@Test
	final void testGetProof() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#getMaxBillingLength()}.
	 */
	@Test
	final void testGetMaxBillingLength() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#verifySignature()}.
	 */
	@Test
	final void testVerifySignature() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#T2Witness()}.
	 */
	@Test
	final void testT2Witness() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#T2Witness(byte[])}.
	 */
	@Test
	final void testT2WitnessByteArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#T2Witness(java.io.ByteArrayInputStream)}.
	 */
	@Test
	final void testT2WitnessByteArrayInputStream() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#DERToEQCSignature(byte[])}.
	 */
	@Test
	final void testDERToEQCSignature() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.T2Witness#getDERSignature()}.
	 */
	@Test
	final void testGetDERSignature() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#getBytes(java.io.ByteArrayOutputStream)}.
	 */
	@Test
	final void testGetBytesByteArrayOutputStream() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#isValid()}.
	 */
	@Test
	final void testIsValid() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#toString()}.
	 */
	@Test
	final void testToString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#Witness()}.
	 */
	@Test
	final void testWitness() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#Witness(byte[])}.
	 */
	@Test
	final void testWitnessByteArray() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#Witness(java.io.ByteArrayInputStream)}.
	 */
	@Test
	final void testWitnessByteArrayInputStream() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#Parse(java.io.ByteArrayInputStream)}.
	 */
	@Test
	final void testParseByteArrayInputStream() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#isNull()}.
	 */
	@Test
	final void testIsNull() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#getWitness()}.
	 */
	@Test
	final void testGetWitness() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#setTransaction(org.eqcoin.transaction.Transaction)}.
	 */
	@Test
	final void testSetTransaction() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#isMeetPreCondition()}.
	 */
	@Test
	final void testIsMeetPreCondition() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#planting()}.
	 */
	@Test
	final void testPlanting() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#free()}.
	 */
	@Test
	final void testFree() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#getPassport()}.
	 */
	@Test
	final void testGetPassport() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.eqcoin.lock.witness.Witness#getLockMate()}.
	 */
	@Test
	final void testGetLockMate() {
		fail("Not yet implemented"); // TODO
	}

}
