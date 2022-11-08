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
package org.eqcoin.lock.publickey;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T1Publickey extends Publickey {
	
	public T1Publickey() {
	}

	public T1Publickey(ByteArrayInputStream is) throws Exception {
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
