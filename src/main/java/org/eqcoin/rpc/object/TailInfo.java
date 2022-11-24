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
import java.util.Arrays;

import org.eqcoin.avro.O;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 24, 2019
 * @email 10509759@qq.com
 */
public class TailInfo extends IO implements Comparable<TailInfo> {
	private ID height;
	private ID checkPointHeight;
	private byte[] tailProof;
	private SP sp;

	public TailInfo() {
		super();
	}
	
	public TailInfo(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public <T> TailInfo(T type) throws Exception {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(height == null) {
			Log.Error("height == null");
			return false;
		}
		if(!height.isSanity()) {
			Log.Error("!height.isSanity()");
			return false;
		}
		if(checkPointHeight == null) {
			Log.Error("checkPointHeight == null");
			return false;
		}
		if(!checkPointHeight.isSanity()) {
			Log.Error("!checkPointHeight.isSanity()");
			return false;
		}
		if(tailProof == null) {
			Log.Error("tailProof == null");
			return false;
		}
		if(tailProof.length != Util.SHA3_512_LEN) {
			Log.Error("tailProof.length != Util.SHA3_512_LEN");
			return false;
		}
		if(sp == null) {
			Log.Error("sp == null");
			return false;
		}
		if(!sp.isSanity()) {
			Log.Error("!sp.isSanity()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		height = EQCCastle.parseID(is);
		checkPointHeight = EQCCastle.parseID(is);
		tailProof = EQCCastle.parseNBytes(is, Util.SHA3_512_LEN);
		sp = new SP(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(height.getEQCBits());
		os.write(checkPointHeight.getEQCBits());
		os.write(tailProof);
		os.write(sp.getBytes());
		return os;
	}

	@Override
	public int compareTo(TailInfo o) {
		if(!checkPointHeight.equals(o.checkPointHeight)) {
			return checkPointHeight.intValue() - o.checkPointHeight.intValue();
		}
		else {
			return height.intValue() - o.height.intValue();
		}
	}

	/**
	 * @return the height
	 */
	public ID getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(ID height) {
		this.height = height;
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

	/**
	 * @return the tailProof
	 */
	public byte[] getTailProof() {
		return tailProof;
	}

	/**
	 * @param tailProof the tailProof to set
	 */
	public void setTailProof(byte[] tailProof) {
		this.tailProof = tailProof;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((checkPointHeight == null) ? 0 : checkPointHeight.hashCode());
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((sp == null) ? 0 : sp.hashCode());
		result = prime * result + Arrays.hashCode(tailProof);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TailInfo other = (TailInfo) obj;
		if (checkPointHeight == null) {
			if (other.checkPointHeight != null) {
				return false;
			}
		} else if (!checkPointHeight.equals(other.checkPointHeight)) {
			return false;
		}
		if (height == null) {
			if (other.height != null) {
				return false;
			}
		} else if (!height.equals(other.height)) {
			return false;
		}
		if (sp == null) {
			if (other.sp != null) {
				return false;
			}
		} else if (!sp.equals(other.sp)) {
			return false;
		}
		if (!Arrays.equals(tailProof, other.tailProof)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\"TailInfo\":{\"height\":\"" + height + "\", \"checkPointHeight\":\"" + checkPointHeight
				+ "\", \"tailProof\":\"" + Arrays.toString(tailProof) + "\", \"sp\":\"" + sp + "\"}}";
	}
	
}
