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
package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public abstract class IO<T> extends EQCSerializable {
	public IO() {}
	
	public IO(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
	}

	protected void parse(T type) throws Exception {
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid IO type");
		}
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public T getProtocol() throws Exception {
		T type = null;
		if (type instanceof O) {
			type = (T) new O(ByteBuffer.wrap(getBytes()));
		}
		else {
			throw new IllegalStateException("Invalid IO type");
		}
		return type;
	}
	
//	public O getProtocol() throws Exception {
////		if(type instanceof O) {
////			return (T) new O(ByteBuffer.wrap(getBytes()));
////		}
//		return new O(ByteBuffer.wrap(getBytes()));
//	}

	public byte[] getBytes() throws Exception {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}
	
}
