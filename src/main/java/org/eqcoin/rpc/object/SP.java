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
import java.io.OutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * @author Xun Wang
 * @date Mar 26, 2020
 * @email 10509759@qq.com
 */
public class SP extends IO {
	private String ip;
	private ID flag;
	private ID protocolVersion;
	
	public SP() {
		super();
	}
	
	public SP(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public <T> SP(T type) throws Exception {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public SP Parse(ByteArrayInputStream is) throws Exception {
		return new SP(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		ip = EQCCastle.parseString(is);
		flag = EQCCastle.parseID(is);
		protocolVersion = EQCCastle.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCCastle.stringToBIN(ip));
		os.write(flag.getEQCBits());
		os.write(protocolVersion.getEQCBits());
		return os;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public SP setIp(String ip) {
		this.ip = ip;
		return this;
	}

	/**
	 * @return the flag
	 */
	public ID getFlag() {
		return flag;
	}

	/**
	 * @param flag the flag to set
	 */
	public SP setFlag(ID flag) {
		this.flag = flag;
		return this;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((flag == null) ? 0 : flag.hashCode());
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((protocolVersion == null) ? 0 : protocolVersion.hashCode());
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
		SP other = (SP) obj;
		if (flag == null) {
			if (other.flag != null) {
				return false;
			}
		} else if (!flag.equals(other.flag)) {
			return false;
		}
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (protocolVersion == null) {
			if (other.protocolVersion != null) {
				return false;
			}
		} else if (!protocolVersion.equals(other.protocolVersion)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + toInnerJson() + "}";
	}

	public String toInnerJson() {
		return
				 "\"SP\":{\"ip\":\"" + ip + "\", \"flag\":\"" + flag + "\", \"protocolVersion\":\"" + protocolVersion
					+ "\"}";
	}
	
	public boolean isEQCMinerNetwork() {
		
		return ((flag.intValue() & SP_MODE.EQCMINERNETWORK.getFlag()) == SP_MODE.EQCMINERNETWORK.getFlag());
	}
	
	public boolean isEQCHiveSyncNetwork() {
		return ((flag.intValue() & SP_MODE.EQCHIVESYNCNETWORK.getFlag()) == SP_MODE.EQCHIVESYNCNETWORK.getFlag());
	}
	
	public boolean isEQCTransactionNetwork() {
		return ((flag.intValue() & SP_MODE.EQCTRANSACTIONNETWORK.getFlag()) == SP_MODE.EQCTRANSACTIONNETWORK.getFlag());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(ip == null) {
			Log.Error("ip == null");
			return false;
		}
		if(flag == null) {
			Log.Error("flag == null");
			return false;
		}
		if(!flag.isSanity()) {
			Log.Error("!flag.isSanity()");
			return false;
		}
		if(protocolVersion == null) {
			Log.Error("protocolVersion == null");
			return false;
		}
		if(!protocolVersion.isSanity()) {
			Log.Error("!protocolVersion.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * @return the protocolVersion
	 */
	public ID getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @param protocolVersion the protocolVersion to set
	 */
	public SP setProtocolVersion(ID protocolVersion) {
		this.protocolVersion = protocolVersion;
		return this;
	}

}
