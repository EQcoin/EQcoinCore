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
