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

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 27, 2019
 * @email 10509759@qq.com
 */
public class Balance extends IO {
	private Value balance;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		balance = new Value();
	}

	public Balance() {
		super();
	}
	
	public <T> Balance(T type) throws Exception {
		super(type);
	}

	@Override
	public boolean isSanity() throws Exception {
		return ((balance != null) && (balance.isSanity()));
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		balance = new Value(EQCType.parseEQCBits(is));
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(balance.getEQCBits());
		return os;
	}

	/**
	 * @return the balance
	 */
	public Value getBalance() {
		return balance;
	}
	
}
