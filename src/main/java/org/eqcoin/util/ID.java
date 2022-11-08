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
 */
package org.eqcoin.util;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.util.Objects;

import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.serialization.EQCCastle;

/**
 * @author Xun Wang
 * @date 9-17-2018
 * @email 10509759@qq.com
 */
public class ID extends BigInteger implements Gateway {
	/**
	 *
	 */
	final long serialVersionUID = -8644553965845085710L;

	public static final ID ZERO = new ID(BigInteger.ZERO);

	public static final ID ONE = new ID(BigInteger.ONE);

	public static final ID TWO = new ID(BigInteger.TWO);

	public static final ID THREE = new ID(3);

	public static final ID FOUR = new ID(4);

	public static final ID FIVE = new ID(5);

	public static final ID SIX = new ID(6);

	public static final ID SEVEN = new ID(7);

	public static final ID NINE = new ID(9);

	public ID() {
		super(BigInteger.ZERO.toByteArray());
	}

	/**
	 * @param BigInteger
	 */
	public ID(final BigInteger value) {
		super(value.toByteArray());
		EQCCastle.assertNotNegative(this);
	}

	/**
	 * @param EQCBits
	 */
	public ID(final byte[] bytes) {
		super(EQCCastle.eqcBitsToBigInteger(bytes).toByteArray());
	}

	/**
	 * @param long
	 */
	public ID(final long value) {
		super(BigInteger.valueOf(value).toByteArray());
		EQCCastle.assertNotNegative(this);
	}

	public <T> ID(final T type) throws Exception{
		super(BigInteger.ZERO.toByteArray());
		parse(type);
		EQCCastle.assertNotNegative(this);
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#add(java.math.BigInteger)
	 */
	@Override
	public ID add(final BigInteger val) {
		return new ID(super.add(val));
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#divide(java.math.BigInteger)
	 */
	@Override
	public ID divide(final BigInteger val) {
		// TODO Auto-generated method stub
		return new ID(super.divide(val));
	}

	/**
	 * @return current ID's EQCBits
	 */
	public byte[] getEQCBits() {
		return EQCCastle.bigIntegerToEQCBits(this);
	}

	/**
	 * @return current ID's EQCQuantum
	 */
	public byte[] getEQCQuantum() {
		return EQCCastle.bigIntegerToEQCQuantum(this);
	}

	/**
	 * @return the next ID
	 */
	public ID getNextID() {
		return new ID(this.add(BigInteger.ONE));
	}

	/**
	 * @return the previous ID
	 */
	public ID getPreviousID() {
		return new ID(this.subtract(BigInteger.ONE));
	}

	@Override
	public <T> T getProtocol(final Class<T> type) throws Exception {
		return Gateway.getProtocol(type, EQCCastle.bigIntegerToEQCBits(this));
	}

	/**
	 * @param bytes previous ID's EQCBits
	 * @return return true if current ID equal to previous ID + 1 otherwise return false
	 */
	public boolean isNextID(final byte[] bytes) {
		final BigInteger previousID = EQCCastle.eqcBitsToBigInteger(bytes);
		return this.compareTo(previousID.add(BigInteger.ONE)) == 0;
	}

	/**
	 * @param previousID previous ID
	 * @return return true if current ID equal to previous ID + 1 otherwise return false
	 */
	public boolean isNextID(final ID previousID) {
		return this.compareTo(previousID.add(BigInteger.ONE)) == 0;
	}

	public boolean isSanity() {
		return this.compareTo(ID.ZERO) >= 0;
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#multiply(java.math.BigInteger)
	 */
	@Override
	public ID multiply(final BigInteger val) {
		// TODO Auto-generated method stub
		return new ID(super.multiply(val));
	}

	/**
	 * Due to the limit of BigInteger so have to use EQCType.parseID(T type) instead of this
	 * @see org.eqcoin.rpc.gateway.Gateway#parse(java.lang.Object)
	 */
	@Override
	public <T> void parse(final T type) throws Exception {
		Objects.requireNonNull(type);
		final ByteArrayInputStream is = new ByteArrayInputStream(Gateway.parseProtocol(type));
		parse(is);
		EQCCastle.assertNoRedundantData(is);
	}

	/* (non-Javadoc)
	 * @see java.math.BigInteger#subtract(java.math.BigInteger)
	 */
	@Override
	public ID subtract(final BigInteger val) {
		// TODO Auto-generated method stub
		return new ID(super.subtract(val));
	}

}
