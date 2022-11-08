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
