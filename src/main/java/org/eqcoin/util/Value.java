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
package org.eqcoin.util;

import java.math.BigInteger;

import org.eqcoin.serialization.EQCCastle;

/**
 * @author Xun Wang
 * @date Mar 31, 2020
 * @email 10509759@qq.com
 */
public class Value extends BigInteger {
	//	public static final Value ZERO = new Value(0);

	//	public Value() {
	//		super(BigInteger.ZERO.toByteArray());
	//	}

	/**
	 * @param BigInteger
	 */
	public Value(final BigInteger value) {
		super(value.toByteArray());
		EQCCastle.assertPositive(this);
		EQCCastle.assertNotBigger(this, Util.MAX_EQC);
	}

	/**
	 * @param EQCBits
	 */
	public Value(final byte[] bytes) {
		super(EQCCastle.eqcBitsToBigInteger(bytes).toByteArray());
		EQCCastle.assertPositive(this);
		EQCCastle.assertNotBigger(this, Util.MAX_EQC);
	}

	/**
	 * @param long
	 */
	public Value(final long value) {
		super(BigInteger.valueOf(value).toByteArray());
		EQCCastle.assertPositive(this);
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#add(java.math.BigInteger)
	 */
	@Override
	public Value add(final BigInteger val) {
		return new Value(super.add(val));
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#divide(java.math.BigInteger)
	 */
	@Override
	public Value divide(final BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.divide(val));
	}

	/**
	 * @return current serial number's EQCBits
	 */
	public byte[] getEQCBits() {
		return EQCCastle.bigIntegerToEQCBits(this);
	}

	public boolean isSanity() throws Exception {
		if(this.compareTo(Value.ZERO) <= 0) {
			Log.Error(this + " <= 0");
			return false;
		}
		if(this.compareTo(Util.MAX_EQC) > 0) {
			Log.Error(this + " > " + Util.MAX_EQC);
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#multiply(java.math.BigInteger)
	 */
	@Override
	public Value multiply(final BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.multiply(val));
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#subtract(java.math.BigInteger)
	 */
	@Override
	public Value subtract(final BigInteger val) {
		// TODO Auto-generated method stub
		return new Value(super.subtract(val));
	}

	/**
	 * @return current ID's EQCLight
	 */
	public byte[] getEQCLight() {
		return EQCCastle.bigIntegerToEQCLight(this);
	}

}
