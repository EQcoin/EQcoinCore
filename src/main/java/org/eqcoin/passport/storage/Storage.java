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
package org.eqcoin.passport.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.eqcoin.passport.Passport;
import org.eqcoin.passport.storage.StateVariable.STATE;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.Log;

/**
 * Provide the storage for expendable passport store relevant storage variable
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class Storage extends EQCSerializable {
	private Vector<StateVariable> stateVariableList;
	private Passport passport;
	
	public Storage() {
		super();
	}
	
	public Storage(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public Storage(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		stateVariableList = EQCType.parseArray(is, new StateVariable().setPassport(passport));
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCType.eqcSerializableListToArray(stateVariableList));
		return os;
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

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		stateVariableList = new Vector<>();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return super.toInnerJson();
	}

	/**
	 * @return the passport
	 */
	public Passport getPassport() {
		return passport;
	}

	/**
	 * @param passport the passport to set
	 */
	public Storage setPassport(Passport passport) {
		this.passport = passport;
		for(StateVariable stateVariable:stateVariableList) {
			stateVariable.setPassport(passport);
		}
		return this;
	}
	
	public void planting() throws Exception {
		for(StateVariable stateVariable:stateVariableList) {
			if(stateVariable instanceof UpdateHeight && stateVariable.isMeetPreconditions()) {
				stateVariable.planting();
			}
		}
	}
	
	public boolean isStateVariableExists(StateVariable stateVariable) {
		for(StateVariable stateVariable2:stateVariableList) {
			if(stateVariable2.getState() == stateVariable.getState()) {
				return true;
			}
		}
		return false;
	}
	
	public void addStateVariable(StateVariable stateVariable) {
		if(isStateVariableExists(stateVariable)) {
			throw new IllegalStateException("Exists duplicate state variable");
		}
		stateVariableList.add(stateVariable);
	}
	
	public StateVariable getStateVariable(STATE state) {
		StateVariable stateVariable = null;
		for(StateVariable stateVariable2:stateVariableList) {
			if(stateVariable2.getState() == state) {
				stateVariable = stateVariable2;
			}
		}
		return stateVariable;
	}
	
}
