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
* Wandering Earth Corporation reserves any and all current and future rights,
* titles and interests in any and all intellectual property rights of Wandering Earth
* Corporation, including but not limited to discoveries, ideas, marks, concepts,
* methods, formulas, processes, codes, software, inventions, compositions, techniques,
* information and data, whether or not protectable in trademark, copyrightable
* or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.service.state;

import org.eqcoin.rpc.object.NewEQCHive;

/**
 * @author Xun Wang
 * @date Jul 7, 2019
 * @email 10509759@qq.com
 */
public class NewEQCHiveState extends EQCServiceState {
	private NewEQCHive newEQCHive;
	
	public NewEQCHiveState() throws Exception {
		super(State.BROADCASTNEWEQCHIVE);
		newEQCHive =  new NewEQCHive();
	}
	/**
	 * @return the newEQCHive
	 */
	public NewEQCHive getNewEQCHive() {
		return newEQCHive;
	}

	/**
	 * @param newEQCHive the newEQCHive to set
	 */
	public void setNewEQCHive(NewEQCHive newEQCHive) {
		this.newEQCHive = newEQCHive;
	}
	
}
