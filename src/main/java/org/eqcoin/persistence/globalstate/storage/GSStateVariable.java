/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection 
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take 
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
package org.eqcoin.persistence.globalstate.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.persistence.globalstate.storage.GSStateVariable.GSState;
import org.eqcoin.persistence.storage.StateVariable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Aug 10, 2020
 * @email 10509759@qq.com
 */
public class GSStateVariable extends StateVariable<GSState> {
	public enum GSState {
		//		ProtocolVersion, MaxBlockSize, BlockInterval, TxFeeRate, CheckPoint
		SNCOUNTER, PROTOCOLVERSION, MAXEQCHIVESIZE, EQCHIVEINTERVAL, POWERPRICE, TAILHEIGHT, CHECKPOINT;
		public static GSState get(final int ordinal) {
			GSState state = null;
			switch (ordinal) {
			case 0:
				state = SNCOUNTER;
				break;
			case 1:
				state = PROTOCOLVERSION;
				break;
			case 2:
				state = MAXEQCHIVESIZE;
				break;
			case 3:
				state = EQCHIVEINTERVAL;
				break;
			case 4:
				state = POWERPRICE;
				break;
			case 5:
				state = TAILHEIGHT;
				break;
			case 6:
				state = CHECKPOINT;
				break;
			}
			if (state == null) {
				throw new IllegalStateException("Invalid state: " + state);
			}
			return state;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	protected ID height;

	public GSStateVariable() {
		super();
	}

	public GSStateVariable(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.
	 * ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		super.getHeaderBytes(os);
		os.write(height.getEQCBits());
		return os;
	}

	/**
	 * @return the state
	 */
	@Override
	public GSState getState() {
		return state;
	}

	@Override
	public boolean isSanity() throws Exception {
		if(!super.isSanity()) {
			return false;
		}
		if(height == null) {
			Log.Error("height == null");
			return false;
		}
		if(!height.isSanity()) {
			Log.Error("!height.isSanity()");
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public GSStateVariable Parse(final ByteArrayInputStream is) throws Exception {
		GSStateVariable stateVariable = null;
		final GSState state = parseState(is);
		if (state == GSState.TAILHEIGHT) {
			stateVariable = new TailHeight(is);
		}
		return stateVariable;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eqcoin.serialization.EQCSerializable#parseHeader(java.io.
	 * ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		super.parseHeader(is);
		height = EQCCastle.parseID(is);
	}

	@Override
	public GSState parseState(final ByteArrayInputStream is) throws Exception {
		GSState state = null;
		try {
			is.mark(0);
			state = GSState.get(EQCCastle.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return state;
	}

	/**
	 * @param state the state to set
	 */
	@Override
	public void setState(final GSState state) {
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
		return "{\"GSStateVariable\":{\"state\":\"" + state + "\"}}";
	}

}
