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
package org.eqcoin.persistence.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.persistence.globalstate.GlobalState.Plantable;
import org.eqcoin.protocol.EQCConstraint;
import org.eqcoin.protocol.EQCProtocol;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Aug 10, 2020
 * @email 10509759@qq.com
 */
public class StateVariable<T extends Enum> extends EQCObject implements EQCConstraint, Plantable {
	protected ID serialNumber;
	protected T state;

	public StateVariable() {
		super();
	}

	public StateVariable(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(serialNumber.getEQCBits());
		os.write(EQCCastle.intToEQCBits(state.ordinal()));
		return os;
	}

	public ID getSerialNumber() {
		return serialNumber;
	}

	/**
	 * @return the state
	 */
	public T getState() {
		return state;
	}

	// Due to the expand ability so here need use isMeetPreconditions
	@Override
	public boolean isMeetConstraint(EQCProtocol eqcProtocol) throws Exception {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if (serialNumber == null) {
			Log.Error("serialNumber == null");
			return false;
		}
		if (!serialNumber.isSanity()) {
			Log.Error("!serialNumber.isSanity()");
			return false;
		}
		if (state == null) {
			Log.Error("state == null");
			return false;
		}
		return true;
	}

	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		serialNumber = EQCCastle.parseID(is);
	}

	public T parseState(final ByteArrayInputStream is) throws Exception {
		return null;
	}

	@Override
	public void planting() throws Exception {
	}

	public void setSerialNumber(final ID serialNumber) {
		this.serialNumber = serialNumber;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(final T state) {
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return "\"State\":\"" + state + "\"";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"StateVariable\":{\"state\":\"" + state + "\"}}";
	}
}
