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
package org.eqcoin.passport.passport;

import java.sql.ResultSet;

/**
 * @author Xun Wang
 * @date May 11, 2019
 * @email 10509759@qq.com
 */
public class AssetPassport extends Passport {

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.passport.Passport#init()
	 */
	public AssetPassport() throws Exception {
		super();
	}

	public AssetPassport(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public AssetPassport(final ResultSet resultSet) throws Exception {
		super(resultSet);
	}

	@Override
	protected void init() {
		super.init();
		type = PassportType.ASSET;
	}

	@Override
	public String toInnerJson() {
		return "\"AssetPassport\":" + "{\n" +
				super.toInnerJson() +
				"\n}";
	}

}
