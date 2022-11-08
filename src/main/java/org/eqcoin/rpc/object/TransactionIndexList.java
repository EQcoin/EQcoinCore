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
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.eqcoin.avro.O;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 27, 2019
 * @email 10509759@qq.com
 */
public class TransactionIndexList extends IO {
	private Vector<TransactionIndex> transactionIndexList;
	private ID syncTime;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		transactionIndexList = new Vector<>();
	}

	public TransactionIndexList() {
		super();
	}
	
	public <T> TransactionIndexList(T type) throws Exception {
		super(type);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(transactionIndexList == null) {
			Log.Error("transactionIndexList == null");
			return false;
		}
		if(syncTime == null) {
			Log.Error("syncTime == null");
			return false;
		}
		if(!syncTime.isSanity()) {
			Log.Error("!syncTime.isSanity()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		transactionIndexList = EQCCastle.parseArray(is, new TransactionIndex());
		syncTime = EQCCastle.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCCastle.eqcSerializableListToArray(transactionIndexList));
		os.write(syncTime.getEQCBits());
		return os;
	}

	public void addTransactionIndex(TransactionIndex transactionIndex) {
		transactionIndexList.add(transactionIndex);
	}

	/**
	 * @return the transactionIndexList
	 */
	public Vector<TransactionIndex> getTransactionIndexList() {
		return transactionIndexList;
	}

	/**
	 * @param transactionIndexList the transactionIndexList to set
	 */
	public void setTransactionIndexList(Vector<TransactionIndex> transactionIndexList) {
		this.transactionIndexList = transactionIndexList;
	}

	/**
	 * @return the syncTime
	 */
	public ID getSyncTime() {
		return syncTime;
	}

	/**
	 * @param syncTime the syncTime to set
	 */
	public void setSyncTime(ID syncTime) {
		this.syncTime = syncTime;
	}
	
}
