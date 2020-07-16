/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package org.eqcoin.transaction.txout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;

import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockMate;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class ZionTxOut extends EQCSerializable {
	protected Lock lock;
	protected Value value;
	
	public ZionTxOut(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public ZionTxOut(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public ZionTxOut() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public ZionTxOut Parse(ByteArrayInputStream is) throws Exception {
		return new ZionTxOut(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse Lock
		lock = new Lock().Parse(is);
		// Parse Value
		value = EQCType.parseValue(is);
	}

	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(lock.getBytes());
		os.write(value.getEQCBits());
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(lock == null) {
			Log.Error("lock == null");
			return false;
		}
		if(!lock.isSanity()) {
			Log.Error("!lock.isSanity()");
			return false;
		}
		if(value == null) {
			Log.Error("value == null");
			return false;
		}
		if(!value.isSanity()) {
			Log.Error("!value.isSanity()");
			return false;
		}
		if(value.compareTo(Util.MIN_BALANCE) <0) {
			Log.Error(value + " < " + Util.MIN_BALANCE);
			return false;
		}
		return true;
	}

	public String toInnerJson() {
		return 
		"\"ZionTxOut\":" + 
		"\n{" +
			lock.toInnerJson() + ",\n" +
			"\"Value\":" + "\"" +  Long.toString(value.longValue()) + "\"" + "\n" +
		"}";
	}

	/**
	 * @return the lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @param lock the lock to set
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	/**
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Value value) {
		this.value = value;
	}
	
}
