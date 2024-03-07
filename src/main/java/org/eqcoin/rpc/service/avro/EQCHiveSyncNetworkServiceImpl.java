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
package org.eqcoin.rpc.service.avro;

import org.eqcoin.avro.EQCHiveSyncNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.rpc.object.TailInfo;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCHiveSyncNetworkServiceImpl extends EQCRPCServiceImpl implements EQCHiveSyncNetwork {

	@Override
	public O getEQCHiveTail() {
		O io = null;
		TailInfo tailInfo = null;
//		EQcoinRootPassport eQcoinSubchainPassport = null;
//		try {
//			tailInfo = new TailInfo();
//			tailInfo.setHeight(Util.GS().getEQCHiveTailHeight());
//			eQcoinSubchainPassport = (EQcoinRootPassport) Util.GS().getPassport(ID.ZERO);
//			tailInfo.setCheckPointHeight(eQcoinSubchainPassport.getCheckPointHeight());
//			tailInfo.setTailProof(Util.GS().getEQCHiveRootProof(tailInfo.getHeight()));
//			tailInfo.setSp(Util.LOCAL_SP);
//			io = tailInfo.getProtocol(O.class);
//		} catch (Exception e) {
//			Log.Error(e.getMessage());
//		}
		return io;
	}

	@Override
	public O getEQCHive(O h) {
		O hive = null;
		byte[] eqcHive = null;
		try {
			eqcHive = Util.GS().getEQCHive(new ID(h.getO().array()));
			if(eqcHive != null) {
				hive = Gateway.getProtocol(O.class, eqcHive);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return hive;
	}

	/**
	 * @param S
	 */
	@Override
	public O getLockInfo(O S) {
		return null;
	}

	@Override
	public O getEQCHiveRootProof(O h) {
		O rootProof = null;
		byte[] eqcRootProof = null;
		try {
			eqcRootProof = Util.GS().getEQCHiveRootProof(new ID(h.getO().array()));
			if(eqcRootProof != null) {
				rootProof = Gateway.getProtocol(O.class, eqcRootProof);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return rootProof;
	}

	@Override
	public O getEQCHiveRoot(O H) {
		O eqcHiveRoot = null;
		EQCHiveRoot eqcHiveRoot2 = null;
		try {
			eqcHiveRoot2 = Util.GS().getEQCHiveRoot(new ID(H.getO().array()));
			if(eqcHiveRoot2 != null) {
				eqcHiveRoot = eqcHiveRoot2.getProtocol(O.class);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return eqcHiveRoot;
	}
	
}
