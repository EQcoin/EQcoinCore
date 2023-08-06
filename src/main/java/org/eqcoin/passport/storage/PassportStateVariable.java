/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by Xun
 * Wang with cooperative partners are owned by Xun Wang and entitled to
 * protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang
 * reserves all rights to take any legal action and pursue any rights or remedies
 * under applicable law.
 */
package org.eqcoin.passport.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.passport.storage.PassportStateVariable.PassportState;
import org.eqcoin.persistence.storage.StateVariable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.passport.passport.Passport;

/**
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class PassportStateVariable extends StateVariable<PassportState> {
	public enum PassportState {
		SNCOUNTER;
		public static PassportState get(final int ordinal) {
			PassportState state = null;
			switch (ordinal) {
			case 0:
				state = SNCOUNTER;
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
	protected Passport passport;

	public PassportStateVariable() {
		super();
	}

	public PassportStateVariable(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(state.getEQCBits());
		return os;
	}

	/**
	 * @return the passport
	 */
	public Passport getPassport() {
		return passport;
	}

	/**
	 * @return the state
	 */
	@Override
	public PassportState getState() {
		return state;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public PassportStateVariable Parse(final ByteArrayInputStream is) throws Exception {
		final PassportStateVariable stateVariable = null;
		final PassportState state = parseState(is);
		if (state == PassportState.SNCOUNTER) {
			//			stateVariable = new UpdateHeight(is);
		}
		return stateVariable;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		state = PassportState.get(EQCCastle.parseID(is).intValue());
	}

	@Override
	public PassportState parseState(final ByteArrayInputStream is) throws Exception {
		PassportState state = null;
		try {
			is.mark(0);
			state = PassportState.get(EQCCastle.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return state;
	}

	/**
	 * @param passport the passport to set
	 */
	public PassportStateVariable setPassport(final Passport passport) {
		this.passport = passport;
		return this;
	}

	/**
	 * @param state the state to set
	 */
	@Override
	public void setState(final PassportState state) {
		this.state = state;
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
		return "{\"PassportStateVariable\":{\"state\":\"" + state + "\"}}";
	}

}
