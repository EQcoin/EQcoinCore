/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
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
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
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
