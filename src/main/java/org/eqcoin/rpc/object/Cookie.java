package org.eqcoin.rpc.object;
/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by Xun
 * Wang with cooperative partners are owned by Xun Wang and entitled to
 * protection available from copyright law by country as well as international
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
 * property rights of Xun Wang without prior written permission, Xun Wang
 * reserves all rights to take any legal action and pursue any rights or remedies
 * under applicable law.
 */
//package org.eqcoin.rpc;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import org.eqcoin.avro.O;
//import org.eqcoin.serialization.EQCInheritable;
//import org.eqcoin.serialization.EQCTypable;
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Util;
//
///**
// * @author Xun Wang
// * @date Jun 25, 2019
// * @email 10509759@qq.com
// */
//@Deprecated // Instead use SP
//public class Cookie extends IO {
//	private SP sp;
//	private ID version;
//	
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#init()
//	 */
//	@Override
//	protected void init() {
//		sp = Util.LOCAL_SP;
//		version = Util.PROTOCOL_VERSION;
//	}
//
//	public Cookie() {
//		super();
//	}
//	
//	public Cookie(ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//	
//	public <T> Cookie(T type) throws Exception {
//		super(type);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.serialization.EQCTypable#isSanity()
//	 */
//	@Override
//	public boolean isSanity() {
//		if(version == null || !version.equals(Util.PROTOCOL_VERSION)) {
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
//	 */
//	@Override
//	public boolean isValid() throws Exception {
//		return false;
//	}
//
//	/**
//	 * @return the sp
//	 */
//	public SP getSp() {
//		return sp;
//	}
//
//	/**
//	 * @param sp the sp to set
//	 */
//	public void setSp(SP sp) {
//		this.sp = sp;
//	}
//
//	/**
//	 * @return the version
//	 */
//	public ID getVersion() {
//		return version;
//	}
//
//	/**
//	 * @param version the version to set
//	 */
//	public void setVersion(ID version) {
//		this.version = version;
//	}
//
//	public boolean isIPNull() {
//		// Here need do more job to check if the sp format is valid
//		return sp == null || sp.getIp().isEmpty();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parse(ByteArrayInputStream is) throws Exception {
//		sp = new SP(is);
//		version = EQCType.parseID(is);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
//	 */
//	@Override
//	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
//		os.write(sp.getBytes());
//		os.write(version.getEQCBits());
//		return os;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "{\"Cookie\":{" + sp + "\", \"version\":\"" + version + "\"}}";
//	}
//
//}
