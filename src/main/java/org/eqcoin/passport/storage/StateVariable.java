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

import org.eqcoin.passport.Passport;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class StateVariable extends EQCSerializable {
	protected STATE state;
	protected Passport passport;
	
	public StateVariable() {
		super();
	}
	
	public StateVariable(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public StateVariable Parse(ByteArrayInputStream is) throws Exception {
		StateVariable stateVariable = null;
		STATE state = parseState(is);
		if(state == STATE.UPDATEHEIGHT) {
			stateVariable = new UpdateHeight(is);
		}
		return stateVariable;
	}

	public STATE parseState(ByteArrayInputStream is) throws Exception {
		STATE state = null;
		try {
			is.mark(0);
			state = STATE.get(EQCType.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return state;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		state = STATE.get(EQCType.parseID(is).intValue());
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(state.getEQCBits());
		return os;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(state == null) {
			Log.Error("state == null");
			return false;
		}
		return true;
	}

	public enum STATE {
		UPDATEHEIGHT;
		public static STATE get(int ordinal) {
			STATE state = null;
			switch (ordinal) {
			case 0:
				state = STATE.UPDATEHEIGHT;
				break;
			}
			if(state == null) {
				throw new IllegalStateException("Invalid state: " + state);
			}
			return state;
		}
		
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
		
	}

	/**
	 * @return the state
	 */
	public STATE getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(STATE state) {
		this.state = state;
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
	public StateVariable setPassport(Passport passport) {
		this.passport = passport;
		return this;
	}
	
	// Due to the expand ability so here need use isMeetPreconditions
	public boolean isMeetPreconditions() throws Exception {
		return true;
	}
	
	public void planting() throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return "\"State\":\"" + state + "\"";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"StateVariable\":{\"state\":\"" + state + "\"}}";
	}
	
	
	
}
