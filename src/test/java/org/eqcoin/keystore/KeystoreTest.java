/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.keystore;

import static org.junit.jupiter.api.Assertions.*;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	 * Test method for {@link org.eqcoin.keystore.Keystore#getInstance()}.
	 */
	@Test
	void testGetInstance() {
		Log.info("testGetInstance");
		Keystore.getInstance();
	}

	/**
	 * Test method for {@link org.eqcoin.keystore.Keystore#createPassport(com.eqzip.eqcoin.keystore.Account)}.
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
	 * Test method for {@link org.eqcoin.keystore.Keystore#updateAccounts()}.
	 */
	@Test
	void testUpdateAccounts() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.eqcoin.keystore.Keystore#isAccountExist(com.eqzip.eqcoin.keystore.Account)}.
	 */
	@Test
	void testIsAccountExist() {
		fail("Not yet implemented");
	}
	
	

}
