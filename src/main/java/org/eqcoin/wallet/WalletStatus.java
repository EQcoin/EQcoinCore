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
package org.eqcoin.wallet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;

/**
 * @author Xun Wang
 * @date May 9, 2020
 * @email 10509759@qq.com
 */
@Deprecated
public class WalletStatus extends EQCObject {
	protected Status status;
	
	public enum Status {
		UNUSED, LIVELY, FORBIDDEN;
		public static Status get(int ordinal) {
			Status status = null;
			switch (ordinal) {
			case 0:
				status = UNUSED;
				break;
			case 1:
				status = LIVELY;
				break;
			case 2:
				status = FORBIDDEN;
				break;
			}
			return status;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
	public WalletStatus(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public WalletStatus() throws Exception {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		status = Status.get(EQCCastle.parseID(is).intValue());
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(status.getEQCBits());
		return os;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
