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
package com.eqchains.keystore;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eqcoin.keystore.Keystore;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-21-2018
 * @email 10509759@qq.com
 */
class KeystoreTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.info("setUpBeforeClass");
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
	 * Test method for {@link com.eqcoin.keystore.Keystore#getInstance()}.
	 */
	@Test
	void testGetInstance() {
		Log.info("testGetInstance");
		Keystore.getInstance();
	}

	/**
	 * Test method for {@link com.eqcoin.keystore.Keystore#createAccount(com.eqzip.eqcoin.keystore.Account)}.
	 */
	@Test
	void testCreateAccount() {
		
		byte b = (byte) 128;
		byte c = 127;
		int d = b&0xff;
		
//		Account acc = new Account();
//    	acc.setAddress("abc");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("abcd");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("a");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("a");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
//    	acc = new Account();
//    	acc.setAddress("b");
//    	acc.setBalance(1000000000);
//    	acc.setPrivateKey(Util.getSecureRandomBytes());
//    	acc.setPwdHash(Util.getSecureRandomBytes());
//    	acc.setUserName("b");
//    	Keystore.getInstance().createAccount(acc);
//    	assertTrue(Keystore.getInstance().isAccountExist(acc));
	}

	/**
	 * Test method for {@link com.eqcoin.keystore.Keystore#updateAccounts()}.
	 */
	@Test
	void testUpdateAccounts() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link com.eqcoin.keystore.Keystore#isAccountExist(com.eqzip.eqcoin.keystore.Account)}.
	 */
	@Test
	void testIsAccountExist() {
		fail("Not yet implemented");
	}
	
	

}
