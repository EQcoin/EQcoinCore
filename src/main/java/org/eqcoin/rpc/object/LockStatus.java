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
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date May 10, 2020
 * @email 10509759@qq.com
 */
public class LockStatus extends IO {
	private LOCKSTATUS status;
	private String readableLock;
	private ID id;
	
	public enum LOCKSTATUS {
		READABLELOCK, ID;
		public static LOCKSTATUS get(int ordinal) {
			LOCKSTATUS lockStatus = null;
			switch (ordinal) {
			case 0:
				lockStatus = LOCKSTATUS.READABLELOCK;
				break;
			case 1:
				lockStatus = LOCKSTATUS.ID;
				break;
			}
			if(lockStatus == null) {
				throw new IllegalStateException("Invalid lock status: " + lockStatus);
			}
			return lockStatus;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
	public LockStatus() {
		super();
	}
	
	public LockStatus(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public <T> LockStatus(T type) throws Exception {
		super(type);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		status = LOCKSTATUS.get(EQCCastle.parseID(is).intValue());
		if(status == LOCKSTATUS.READABLELOCK) {
			readableLock = EQCCastle.parseString(is);
		}
		else {
			id = EQCCastle.parseID(is);
		}
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(status.getEQCBits());
		if(status == LOCKSTATUS.READABLELOCK) {
			os.write(EQCCastle.stringToBIN(readableLock));
		}
		else {
			os.write(id.getEQCBits());
		}
		return os;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(status == null) {
			Log.Error("status == null");
			return false;
		}
		if(status == LOCKSTATUS.READABLELOCK && readableLock == null) {
			Log.Error("status == LOCKSTATUS.READABLELOCK && readableLock == null");
			return false;
		}
		else if(status == LOCKSTATUS.ID && id == null) {
			Log.Error("status == LOCKSTATUS.ID && id == null");
			return false;
		}
		return true;
	}

	/**
	 * @return the type
	 */
	public LOCKSTATUS getType() {
		return status;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LOCKSTATUS type) {
		this.status = type;
	}

	/**
	 * @return the readableLock
	 */
	public String getReadableLock() {
		return readableLock;
	}

	/**
	 * @param readableLock the readableLock to set
	 */
	public void setReadableLock(String readableLock) {
		this.readableLock = readableLock;
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}
	
}
