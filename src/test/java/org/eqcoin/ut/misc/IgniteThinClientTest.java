/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.ut.misc;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.util.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date Feb 28, 2021
 * @email 10509759@qq.com
 */
public class IgniteThinClientTest {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		final ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
		final IgniteClient client = Ignition.startClient(cfg);
	}

	private IgniteClient client;

	@Test
	void readPassport() {
		final Passport passport;
		try {
			final IgniteCache<Long, Passport> cache = (IgniteCache<Long, Passport>) client.cache("passportCache");
			Log.info(cache.get(0l).toString());
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
