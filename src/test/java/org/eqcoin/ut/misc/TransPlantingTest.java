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
 */
package org.eqcoin.ut.misc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.passport.passport.AssetPassport;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date May 30, 2020
 * @email 10509759@qq.com
 */
public class TransPlantingTest {
	
	private static GlobalState globalState;
	private static EQCHive currentHive;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.info("setUpBeforeClass");
		Util.init();
		globalState = new GlobalStateH2();
		Util.recoveryGlobalStateTo(ID.ZERO, globalState);
		currentHive = new EQCHive(globalState.getEQCHiveRootProof(globalState.getEQCHiveTailHeight()), globalState.getEQCHiveTailHeight().getNextID(), globalState);
	}
	
	@Test
	void regresstionTest() {
		try {
			Passport passport = Util.GS().getPassportFromLockMateId(ID.ZERO);
			Transaction transaction = TransFactory.Zion(0, 1000, TRANSACTION_PRIORITY.ASAP, 2);
			transaction.init(currentHive);
			assertTrue(transaction.planting());
			passport = null;
			passport = Util.GS().getPassport(new ID(2));
			assertNotNull(passport);
			assertTrue(passport instanceof AssetPassport);
			assertEquals(passport.getId(), ID.TWO);
			assertEquals(passport.getLockID(), ID.TWO);
			assertEquals(passport.getBalance(), Util.getValue(1000));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
