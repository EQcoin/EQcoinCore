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
