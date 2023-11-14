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

import java.sql.SQLException;

import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date May 4, 2019
 * @email 10509759@qq.com
 */
public class CleanUpTest {
	
	@Test
	public void dropTable() {
//		try {
//			EQCBlockChainH2.getInstance().dropTable();
//			EQCBlockChainRocksDB.getInstance().dropTable();
//		} catch (ClassNotFoundException | SQLException | RocksDBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Test
	public void initSingularity() {
		try {
			Util.init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
