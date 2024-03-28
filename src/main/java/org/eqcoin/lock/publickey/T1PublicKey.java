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

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T1PublicKey extends PublicKey {
	
	public T1PublicKey() {
	}

	public T1PublicKey(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse publicKey
		publickey = EQCCastle.parseNBytes(is, Util.P256_PUBLICKEY_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.lock.EQCPublickey#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(publickey == null) {
			Log.Error("publickey == null");
			return false;
		}
		if(publickey.length == Util.P256_PUBLICKEY_LEN) {
			Log.Error("publickey.length == Util.P256_PUBLICKEY_LEN");
			return false;
		}
		return true;
	}
	
	public String toInnerJson() {
		return "\"T1Publickey\":" + "\"" + Util.bytesTo512HexString(publickey) + "\"";
	}
	
}
