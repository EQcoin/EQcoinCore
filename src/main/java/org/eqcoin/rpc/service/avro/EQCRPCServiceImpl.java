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
