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
public class PublicKey extends EQCObject {
	protected byte[] publickey;
	protected Transaction transaction;
	protected LockType lockType;
	private boolean isNew;
	
	public PublicKey() {
	}

	public PublicKey(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	public PublicKey(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		is.close();
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public PublicKey Parse(ByteArrayInputStream is) throws Exception {
		PublicKey publickey = null;
		if (lockType ==LockType.T1) {
			publickey = new T1PublicKey(is);
		} else if (lockType == LockType.T2) {
			publickey = new T2PublicKey(is);
		} else {
			throw new IllegalStateException("Bad lock type: " + lockType);
		}
		return publickey;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public PublicKey Parse(byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		PublicKey publickey = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		publickey = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		is.close();
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

//	@Override
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
	public PublicKey setTransaction(Transaction transaction) {
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
	public PublicKey setLockType(LockType lockType) {
		this.lockType = lockType;
		return this;
	}
	
}
