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
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.lock.publickey;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.Util;


/**
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
public class Publickey extends EQCObject {
	protected byte[] publickey;
	protected Transaction transaction;
	protected LockType lockType;
	private boolean isNew;
	
	public Publickey() {
	}

	public Publickey(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Publickey Parse(ByteArrayInputStream is) throws Exception {
		Publickey publickey = null;
		if (lockType ==LockType.T1) {
			publickey = new T1Publickey(is);
		} else if (lockType == LockType.T2) {
			publickey = new T2Publickey(is);
		} else {
			throw new IllegalStateException("Bad lock type: " + lockType);
		}
		return publickey;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public Publickey Parse(byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		Publickey publickey = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		publickey = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		return publickey;
	}

	/**
	 * Get the PublicKey's bytes for storage it in the EQC block chain.
	 * 
	 * @return byte[]
	 * @throws Exception 
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		if (publickey == null) {
			os.write(EQCCastle.NULL);
		} else {
			os.write(publickey);
		}
		return os;
	}

	/**
	 * @return the publickey
	 */
	public byte[] getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(byte[] publickey) {
		this.publickey = publickey;
	}

	public String toInnerJson() {
		return "\"Publickey\":" + ((publickey == null) ? null : "\"" + Util.bytesTo512HexString(publickey) + "\"");
	}

	@Override
	public boolean isValid() throws Exception {
//		if(!transaction.getTxInLockMate().getPublickey().isNULL()) {
//			Log.Error("Publickey relevant LockMate's Publickey isn't null");
//			return false;
//		}
//		if(LockTool.verifyEQCLockAndPublickey(transaction.getTxInLockMate().getLock(), publickey)) {
//			Log.Error("Publickey doesn't match relevant EQCLock");
//			return false;
//		}
		return true;
	}
	
	public boolean isNULL() {
		return publickey == null;
	}

	/**
	 * @return the isNew
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setNew() {
		this.isNew = true;
	}

	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * @param transaction the transaction to set
	 */
	public Publickey setTransaction(Transaction transaction) {
		this.transaction = transaction;
		lockType = transaction.getLockType();
		return this;
	}

	/**
	 * @return the lockType
	 */
	public LockType getLockType() {
		return lockType;
	}

	/**
	 * @param lockType the lockType to set
	 */
	public Publickey setLockType(LockType lockType) {
		this.lockType = lockType;
		return this;
	}
	
}
