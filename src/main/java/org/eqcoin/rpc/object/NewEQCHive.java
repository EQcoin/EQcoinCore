/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
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
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;

import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.GlobalState.Mode;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 4, 2019
 * @email 10509759@qq.com
 */
public class NewEQCHive extends IO implements Comparable<NewEQCHive> {
	private SP sp;
	private EQCHive eqcHive;
	private ID checkPointHeight;
	private long time;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		time = System.currentTimeMillis();
	}

	public NewEQCHive() throws Exception {
		super();
		sp = Util.LOCAL_SP;
		EQcoinRootPassport eQcoinRootPassport = (EQcoinRootPassport) Util.GS().getPassport(ID.ZERO);
		checkPointHeight = eQcoinRootPassport.getCheckPointHeight();
	}
	
	public <T> NewEQCHive(T type) throws Exception {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(sp == null || eqcHive == null) {
			return false;
		}
		if(!sp.isSanity()) {
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
	 * @see com.eqchains.serialization.EQCInheritable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		sp = new SP(is);
		eqcHive = new EQCHive(EQCCastle.parseBIN(is));
		checkPointHeight = EQCCastle.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCInheritable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(sp.getBytes());
		os.write(eqcHive.getBin());
		os.write(checkPointHeight.getEQCBits());
		return os;
	}

	/**
	 * @return the sp
	 */
	public SP getSp() {
		return sp;
	}

	/**
	 * @param sp the sp to set
	 */
	public void setSp(SP sp) {
		this.sp = sp;
	}

	/**
	 * @return the eqcHive
	 */
	public EQCHive getEQCHive() {
		return eqcHive;
	}

	/**
	 * @param eqcHive the eqcHive to set
	 */
	public void setEQCHive(EQCHive eqcHive) {
		this.eqcHive = eqcHive;
	}

	@Override
	public int compareTo(NewEQCHive o) {
		return (int) (o.time - time);
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}
	
}
