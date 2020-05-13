/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTON) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Objects;

import org.eqcoin.avro.O;
import org.eqcoin.serialization.EQCInheritable;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class IO extends EQCSerializable implements Protocol {
	
	public IO() {
		super();
	}
	
	public IO(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public IO(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public <T> IO(T type) throws Exception {
		super();
		parse(type);
	}
	
	/**
	 * Parse different network protocol relevant wrap object for communication
	 * If need support new protocol type just add new type parse in here
	 * @param type
	 * @throws Exception
	 */
	public <T> void parse(T type) throws Exception {
		Objects.requireNonNull(type);
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	/**
	 *  Return different network protocol relevant wrap object for communication
	 *  If need support new protocol type just add new type wrap in here
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public <T> T getProtocol(Class<T> type) throws Exception {
		T protocol = null;
		if(type.equals(O.class)) {
			protocol = (T) new O(ByteBuffer.wrap(getBytes()));
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		return protocol;
	}
	
}
