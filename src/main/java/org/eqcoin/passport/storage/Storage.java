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
package org.eqcoin.passport.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.eqcoin.passport.storage.PassportStateVariable.PassportState;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.util.Log;

/**
 * Provide the storage for expendable passport store relevant storage variable
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class Storage extends EQCObject {
	private Vector<PassportStateVariable> stateVariableList;

	private Passport passport;

	public Storage() {
		super();
	}

	public Storage(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public Storage(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public void addStateVariable(final PassportStateVariable stateVariable) {
		if(isStateVariableExists(stateVariable)) {
			throw new IllegalStateException("Exists duplicate state variable");
		}
		stateVariableList.add(stateVariable);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(EQCCastle.eqcSerializableListToArray(stateVariableList));
		return os;
	}

	/**
	 * @return the passport
	 */
	public Passport getPassport() {
		return passport;
	}

	public PassportStateVariable getStateVariable(final PassportState state) {
		PassportStateVariable stateVariable = null;
		for(final PassportStateVariable stateVariable2:stateVariableList) {
			if(stateVariable2.getState() == state) {
				stateVariable = stateVariable2;
			}
		}
		return stateVariable;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		stateVariableList = new Vector<>();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(stateVariableList == null) {
			Log.Error("stateVariableList == null");
			return false;
		}
		if(!(stateVariableList.size() > 0)) {
			Log.Error("!(stateVariableList.size() > 0)");
			return false;
		}
		for(int i=0; i<stateVariableList.size(); ++i) {
			if(!stateVariableList.get(i).isSanity()) {
				Log.Error("The state variable in the storage isn't sanity: " + stateVariableList.get(i));
				return false;
			}
			for(int j=i+1; j<stateVariableList.size(); ++j) {
				if(stateVariableList.get(i).getState() == stateVariableList.get(j).getState()) {
					Log.Error("Exists duplicate state variable");
					return false;
				}
			}
		}
		return true;
	}

	public boolean isStateVariableExists(final PassportStateVariable stateVariable) {
		for(final PassportStateVariable stateVariable2:stateVariableList) {
			if(stateVariable2.getState() == stateVariable.getState()) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(final ByteArrayInputStream is) throws Exception {
		stateVariableList = EQCCastle.parseArray(is, new PassportStateVariable().setPassport(passport));
	}

	public void planting() throws Exception {
		for(final PassportStateVariable stateVariable:stateVariableList) {
			//			if(stateVariable instanceof UpdateHeight && stateVariable.isMeetConstraint()) {
			//				stateVariable.planting();
			//			}
		}
	}

	public void removeStateVariable(final PassportStateVariable stateVariable) {
		if(!isStateVariableExists(stateVariable)) {
			throw new IllegalStateException(stateVariable + " doesn't exists");
		}
		for (final PassportStateVariable stateVariable2 : stateVariableList) {
			if (stateVariable2.getState() == stateVariable.getState()) {
				stateVariableList.remove(stateVariable2);
			}
		}
	}

	/**
	 * @param passport the passport to set
	 */
	public Storage setPassport(final Passport passport) {
		this.passport = passport;
		for(final PassportStateVariable stateVariable:stateVariableList) {
			stateVariable.setPassport(passport);
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return super.toInnerJson();
	}

}
