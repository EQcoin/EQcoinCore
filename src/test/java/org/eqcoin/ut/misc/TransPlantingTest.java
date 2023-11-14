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
