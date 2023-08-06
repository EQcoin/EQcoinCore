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
//package org.eqcoin.passport.storage;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//
///**
// * @author Xun Wang
// * @date May 4, 2020
// * @email 10509759@qq.com
// */
//@Deprecated
//public class UpdateHeight extends PassportStateVariable {
//	private ID updateHeight;
//
//	public UpdateHeight() {}
//
//	public UpdateHeight(final ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.serialization.EQCSerializable#getBodyBytes(java.io.ByteArrayOutputStream)
//	 */
//	@Override
//	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
//		os.write(height.getEQCBits());
//		return os;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.serialization.EQCSerializable#init()
//	 */
//	@Override
//	protected void init() {
//		//		state = PassportState.UPDATEHEIGHT;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.passport.storage.StateVariable#isMeetPreconditions()
//	 */
//	@Override
//	public boolean isMeetConstraint() throws Exception {
//		if (!(passport.getId().equals(ID.ZERO))) {
//			Log.Error("Passport isn't EQcoinRootPassport or relevant lock doesn't support update height op");
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.passport.storage.StateVariable#isSanity()
//	 */
//	@Override
//	public boolean isSanity() throws Exception {
//		if(!super.isSanity()) {
//			return false;
//		}
//		if(height == null) {
//			Log.Error("updateHeight == null");
//			return false;
//		}
//		if(!height.isSanity()) {
//			Log.Error("!updateHeight.isSanity()");
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parseBody(final ByteArrayInputStream is) throws Exception {
//		height = EQCType.parseID(is);
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.passport.storage.StateVariable#planting()
//	 */
//	@Override
//	public void planting() throws Exception {
//		height = passport.getEQCHive().getRoot().getHeight();
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
//	 */
//	@Override
//	public String toInnerJson() {
//		return "\"UpdateHeight\":" + "{\n" +
//				super.toInnerJson() + ",\n" +
//				"\"UpdateHeight\":" + "\"" + height + "\"" +
//				"\n}";
//	}
//
//}
