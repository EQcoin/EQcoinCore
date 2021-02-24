/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
package org.eqcoin.persistence.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.persistence.globalstate.GlobalState.Plantable;
import org.eqcoin.protocol.Constraint;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Aug 10, 2020
 * @email 10509759@qq.com
 */
public class StateVariable<T extends Enum> extends EQCObject implements Constraint, Plantable {
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
	public boolean isMeetConstraint() throws Exception {
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
