/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.serialization;

import org.eqcoin.protocol.EQCConstraint;
import org.eqcoin.protocol.EQCProtocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Xun Wang
 * @date Mar 26, 2020
 * @email 10509759@qq.com
 */
public abstract class EQCObject implements EQCSerializable, EQCInheritable, EQCConstraint {

	public EQCObject() {
		init();
	}

	/**
	 * If derived class hasn't any sub class can override this constructor to create
	 * new class from byte[] otherwise need override @see
	 * com.eqcoin.serialization.EQCSerializable#Parse(byte[]) to support constructor
	 * different sub class
	 *
	 * @param bytes
	 * @throws Exception
	 */
	public EQCObject(final byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		init();
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCCastle.assertNoRedundantData(is);
	}

	/**
	 * If the sub class has multiple sub class then shouldn't implement this
	 * constructor and use Parse(ByteArrayInputStream is) instead this.
	 *
	 * @param is
	 * @throws Exception
	 */
	public EQCObject(final ByteArrayInputStream is) throws Exception {
		init();
		parse(is);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.eqcoin.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCCastle.bytesToBIN(getBytes());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCTypable#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		return getBodyBytes(new ByteArrayOutputStream()).toByteArray();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.eqcoin.serialization.EQCInheritable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#getBytes()
	 */
	@Override
	public byte[] getBytes() throws Exception {
		return getBytes(new ByteArrayOutputStream()).toByteArray();
	}

	@Override
	public ByteArrayOutputStream getBytes(final ByteArrayOutputStream os) throws Exception {
		getHeaderBytes(os);
		getBodyBytes(os);
		return os;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCTypable#getHeaderBytes()
	 */
	@Override
	public byte[] getHeaderBytes() throws Exception {
		return getHeaderBytes(new ByteArrayOutputStream()).toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		return os;
	}

	protected void init() {
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMeetConstraint(EQCProtocol eqcProtocol) throws Exception {
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parse(java.io.ByteArrayInputStream, java.lang.Object[])
	 */
	@Override
	public void parse(final ByteArrayInputStream is) throws Exception {
		parseHeader(is);
		parseBody(is);
	}

	/**
	 * When the object which extends from EQCSerializable have multiple sub classes
	 * need implement this to support parse different sub class from the
	 * byte[].
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
	public <T extends EQCObject> T Parse(final byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		T eqcSerializable = null;
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		eqcSerializable = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		return eqcSerializable;
	}

	/**
	 * When the object which extends from EQCSerializable have multiple sub classes
	 * need implement this to support parse different sub class from the
	 * ByteArrayInputStream.
	 *
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public <T extends EQCObject> T Parse(final ByteArrayInputStream is) throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCTypable#parseBody(byte[])
	 */
	@Override
	public void parseBody(final byte[] bytes) throws Exception {
		parseBody(new ByteArrayInputStream(bytes));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.eqcoin.serialization.EQCInheritable#parseBody(java.io.
	 * ByteArrayInputStream)
	 */
	@Override
	public void parseBody(final ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCTypable#parseHeader(byte[])
	 */
	@Override
	public void parseHeader(final byte[] bytes) throws Exception {
		parseHeader(new ByteArrayInputStream(bytes));
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	public String toInnerJson() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" + toInnerJson() + "\n}";
	}

}
