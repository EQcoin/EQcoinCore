/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
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
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.lock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Lock extends EQCObject {
	protected LockType type;
	protected byte[] proof;

	public Lock() {
		super();
	}

	public Lock(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public Lock(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		is.close();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Lock Parse(ByteArrayInputStream is) throws Exception {
		Lock lock = null;
		LockType lockType = parseLockType(is);
		if(lockType == LockType.T1) {
			lock = new T1Lock(is);
		}
		else if(lockType == LockType.T2) {
			lock = new T2Lock(is);
		}
		return lock;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public Lock Parse(byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		Lock lock = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		lock = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		is.close();
		return lock;
	}

	private final LockType parseLockType(ByteArrayInputStream is) throws Exception {
		LockType lockType = null;
		try {
			is.mark(0);
			lockType = LockType.get(EQCCastle.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return lockType;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		type = LockType.get(EQCCastle.parseID(is).intValue());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(type.getEQCBits());
		return os;
	}

	public String toInnerJson() {
		return null;
	}

	@Override
	public boolean isSanity() {
		return false;
	}

	/**
	 * @return the proof
	 */
	public byte[] getProof() {
		return proof;
	}

	/**
	 * @param proof the proof to set
	 */
	public void setProof(byte[] proof) {
		this.proof = proof;
	}

	/**
	 * @return the type
	 */
	public LockType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LockType lockType) {
		this.type = lockType;
	}
	
	public Value getGlobalStateLength() {
		return  null;
	}

	public String getReadableLock() throws Exception {
		return LockTool.EQCLockToReadableLock(this);
	}
	
}
