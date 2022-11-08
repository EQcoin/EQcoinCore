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

import org.eqcoin.avro.O;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.service.PossibleSPService;
import org.eqcoin.service.state.PossibleSPState;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date May 15, 2020
 * @email 10509759@qq.com
 */
public class EQCRPCServiceImpl {
	
	public O registerSP(O sp) {
		O info = null;
		try {
			PossibleSPState possibleSPState = new PossibleSPState();
			possibleSPState.setSp(new SP(sp));
			possibleSPState.setTime(System.currentTimeMillis());
			Log.info("Begin offer possible SP: " + possibleSPState.getSp());
			PossibleSPService.getInstance().offerState(possibleSPState);
			info = Util.getDefaultInfo().getProtocol(O.class);
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return info;
	}

	public O getSPList(O F) {
		O o = null;
		SPList spList = null;
		try {
			spList = Util.MC().getSPList(EQCCastle.parseID(F));
			if(spList != null) {
				o = spList.getProtocol(O.class);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return o;
	}
	
}
