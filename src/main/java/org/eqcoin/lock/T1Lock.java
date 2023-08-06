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
package org.eqcoin.lock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T1Lock extends Lock {
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		type = LockType.T1;
	}

	public T1Lock() {
		super();
	}
	
//	public T1Lock(byte[] bytes) throws Exception {
//		super(bytes);
//	}

	public T1Lock(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		proof = EQCCastle.parseNBytes(is, Util.SHA3_256_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(proof);
		return os;
	}
	
	public Value getProofLength() {
		return  new Value(Util.T1_LOCK_PROOF_SPACE_COST);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.passport.Lock#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(type == null) {
			Log.Error("lockType == null");
			return false;
		}
		if(type != LockType.T1) {
			Log.Error("lockType != LockType.T1");
			return false;
		}
		if(proof == null) {
			Log.Error("lockProof == null");
			return false;
		}
		if(proof.length != Util.SHA3_256_LEN) {
			Log.Error("lockProof.length != Util.SHA3_256_LEN");
			return false;
		}
		return true;
	}
	
	public String toInnerJson() {
		return "\"T1Lock\":" + "{\n" 
				+ "\"LockType\":" + type + ",\n"
				+ "\"PublickeyHash\":" + "\"" + Util.bytesTo512HexString(proof) + "\""
				+ "\n" + "}";
	}
	
}
