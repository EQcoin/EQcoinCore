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
//package org.eqcoin.transaction.operation;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.util.Arrays;
//
//import org.eqcoin.serialization.EQCCastle;
//import org.eqcoin.transaction.ModerateOPTransaction;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//import org.eqcoin.util.Util;
//
///**
// * @author Xun Wang
// * @date Aug 19, 2019
// * @email 10509759@qq.com
// */
//public class ChangeCheckPoint extends Operation {
//	private byte[] checkPointProof;
//	private ID checkPointHeight;
//	
//	public ChangeCheckPoint() throws Exception {
//		super();
//	}
//
//	public ChangeCheckPoint(final ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(final Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (!super.equals(obj)) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		final ChangeCheckPoint other = (ChangeCheckPoint) obj;
//		if (!Arrays.equals(checkPointProof, other.checkPointProof)) {
//			return false;
//		}
//		if (checkPointHeight == null) {
//			if (other.checkPointHeight != null) {
//				return false;
//			}
//		} else if (!checkPointHeight.equals(other.checkPointHeight)) {
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
//	 */
//	@Override
//	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
//		os.write(checkPointProof);
//		os.write(checkPointHeight.getEQCBits());
//		return os;
//	}
//
//	/**
//	 * @return the checkPointHash
//	 */
//	public byte[] getCheckPointHash() {
//		return checkPointProof;
//	}
//
//	/**
//	 * @return the checkPointHeight
//	 */
//	public ID getCheckPointHeight() {
//		return checkPointHeight;
//	}
//	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + Arrays.hashCode(checkPointProof);
//		result = prime * result + ((checkPointHeight == null) ? 0 : checkPointHeight.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#init()
//	 */
//	@Override
//	protected void init() {
//		op = OP.CHECKPOINT;
//	}
//
//	@Override
//	public boolean isMeetConstraint() throws Exception {
//		if(!transaction.getWitness().getPassport().getId().equals(ID.ONE)) {
//			Log.Error("Only Passport one can execute ChangeCheckPointOP");
//			return false;
//		}
//		if(!(transaction instanceof ModerateOPTransaction)) {
//			Log.Error("Only ModerateOPTransaction can execute ChangeCheckPointOP");
//			return false;
//		}
//		return true;
//	}
//
//	@Override
//	public boolean isSanity() {
//		if(op == null) {
//			Log.Error("op == null");
//			return false;
//		}
//		if(op != OP.CHECKPOINT) {
//			Log.Error("op != OP.CHECKPOINT");
//			return false;
//		}
//		if(checkPointProof == null) {
//			Log.Error("checkPointHash == null");
//			return false;
//		}
//		if(checkPointProof.length != Util.SHA3_512_LEN) {
//			Log.Error("checkPointHash.length != Util.SHA3_512_LEN");
//			return false;
//		}
//		if(!checkPointHeight.isSanity()) {
//			Log.Error("!checkPointHeight.isSanity()");
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.transaction.operation.Operation#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
//	 */
//	@Override
//	public boolean isValid() throws Exception {
//		if(checkPointHeight.compareTo(transaction.getEQCHive().getRoot().getHeight()) >=0) {
//			Log.Error("Check point height exceed tail height");
//			return false;
//		}
//		if (!Arrays.equals(checkPointProof, transaction.getEQCHive().getGlobalState().getEQCHiveRootProof(checkPointHeight))) {
//			Log.Error("Check point proof doesn't equal to  relevant EQCHiveRoot's proof");
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
//	 */
//	@Override
//	public void parseBody(final ByteArrayInputStream is)
//			throws Exception {
//		checkPointProof = EQCCastle.parseNBytes(is, Util.SHA3_512_LEN);
//		checkPointHeight = EQCCastle.parseID(is);
//	}
//
//	@Override
//	public void planting() throws Exception {
//		final EQcoinRootPassport eQcoinSeedPassport = (EQcoinRootPassport) transaction.getEQCHive().getGlobalState().getPassport(ID.ZERO);
//		eQcoinSeedPassport.setCheckPointHash(checkPointProof);
//		eQcoinSeedPassport.setCheckPointHeight(checkPointHeight);
//		eQcoinSeedPassport.setEQCHive(transaction.getEQCHive()).planting();
//		// 2020-04-29 Here need do more job to remove the snapshot before the check point
//	}
//
//	/**
//	 * @param checkPointHash the checkPointHash to set
//	 */
//	public void setCheckPointHash(final byte[] checkPointHash) {
//		this.checkPointProof = checkPointHash;
//	}
//
//	/**
//	 * @param checkPointHeight the checkPointHeight to set
//	 */
//	public void setCheckPointHeight(final ID checkPointHeight) {
//		this.checkPointHeight = checkPointHeight;
//	}
//
//	@Override
//	public String toInnerJson() {
//		return 
//		"\"ChangeCheckPointOP\":" + 
//		"\n{\n" +
//		super.toInnerJson()  + ",\n"  + 
//		"\"CheckPointProof\":" + "\"" + Util.dumpBytes(checkPointProof, 16) + "\",\n"  + 
//		"\"CheckPointHeight\":" + "\"" + checkPointHeight + "\"\n"  + 
//		"}\n";
//	}
//	
//}
