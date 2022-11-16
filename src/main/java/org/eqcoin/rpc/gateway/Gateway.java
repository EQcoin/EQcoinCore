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
package org.eqcoin.rpc.gateway;

import java.nio.ByteBuffer;

import org.eqcoin.avro.O;

/**
 * @author Xun Wang
 * @date Apr 28, 2020
 * @email 10509759@qq.com
 */
public interface Gateway {

	/**
	 * Convert byte array to relevant protocol. If need support new protocol type
	 * just add new type wrap in here.
	 *
	 * @param type
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public static <T> T getProtocol(final Class<T> type, final byte[] bytes) throws Exception{
		T protocol = null;
		if(type == O.class) {
			protocol = (T) new O(ByteBuffer.wrap(bytes));
		}
		else {
			throw new IllegalStateException("Invalid Protocol type: " + type);
		}
		return protocol;
	}

	/**
	 * Parse different network protocol relevant wrap object for communication
	 * If need support new protocol type just add new type parse in here
	 * @param type
	 * @throws Exception
	 */
	public static <T> byte[] parseProtocol(final T type) throws Exception
	{
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type: " + type);
		}
		return bytes;
	}

	/**
	 *  Return different network protocol relevant wrap object for communication
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public <T> T getProtocol(Class<T> type) throws Exception;

	/**
	 * Parse different network protocol relevant wrap object for create Object
	 * @param type
	 * @throws Exception
	 */
	public <T> void parse(T type) throws Exception;

}
