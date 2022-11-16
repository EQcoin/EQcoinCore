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
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.util.Objects;

import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class IO extends EQCObject implements Gateway {
	protected long ping;

	public IO() {
		super();
	}

	public IO(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public IO(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public <T> IO(final T type) throws Exception {
		super();
		parse(type);
	}

	/**
	 * @return the ping
	 */
	public long getPing() {
		return ping;
	}

	/**
	 * Return different network protocol relevant wrap object for communication
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@Override
	public <T> T getProtocol(final Class<T> type) throws Exception {
		return Gateway.getProtocol(type, getBytes());
	}

	/**
	 * Parse different network protocol relevant wrap object for create Object
	 * @param type
	 * @throws Exception
	 */
	@Override
	public <T> void parse(final T type) throws Exception {
		Objects.requireNonNull(type);
		final ByteArrayInputStream is = new ByteArrayInputStream(Gateway.parseProtocol(type));
		parse(is);
		EQCCastle.assertNoRedundantData(is);
	}

	/**
	 * @param ping the ping to set
	 */
	public void setPing(final long ping) {
		this.ping = ping;
	}

}
