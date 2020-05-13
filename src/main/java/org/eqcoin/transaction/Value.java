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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.eqcoin.serialization.EQCInheritable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 31, 2020
 * @email 10509759@qq.com
 */
public class Value extends BigInteger {
	public static final Value ZERO = new Value(0);
	
	public Value() {
		super(BigInteger.ZERO.toByteArray());
	}
	
	/**
	 * @param EQCBits
	 */
	public Value(byte[] bytes) {
		super(EQCType.eqcBitsToBigInteger(bytes).toByteArray());
	}
	
	/**
	 * @param long
	 */
	public Value(final long value) {
		super(BigInteger.valueOf(value).toByteArray());
		EQCType.assertNotNegative(value);
	}
	
	/**
	 * @param BigInteger
	 */
	public Value(final BigInteger value) {
		super(value.toByteArray());
		EQCType.assertNotNegative(value);
	}
	
	public boolean isSanity() throws Exception {
		return (this.compareTo(Value.ZERO) > 0 && this.compareTo(Util.MAX_EQcoin) <= 0);
	}
	
	/**
	 * @return current serial number's EQCBits
	 */
	public byte[] getEQCBits() {
		return EQCType.bigIntegerToEQCBits(this);
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#add(java.math.BigInteger)
	 */
	@Override
	public Value add(BigInteger val) {
		return new Value(super.add(val));
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#subtract(java.math.BigInteger)
	 */
	@Override
	public Value subtract(BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.subtract(val));
	}
	
	/* (non-Javadoc)
	 * @see java.math.BigInteger#multiply(java.math.BigInteger)
	 */
	@Override
	public Value multiply(BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.multiply(val));
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#divide(java.math.BigInteger)
	 */
	@Override
	public Value divide(BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.divide(val));
	}
	
}
