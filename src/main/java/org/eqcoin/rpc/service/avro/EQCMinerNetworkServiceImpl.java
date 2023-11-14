/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.rpc.service.avro;

import org.apache.avro.AvroRemoteException;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.NewEQCHive;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.service.PendingNewEQCHiveService;
import org.eqcoin.service.PossibleSPService;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.service.state.PossibleSPState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCMinerNetworkServiceImpl extends EQCRPCServiceImpl implements EQCMinerNetwork {

	@Override
	public O broadcastNewEQCHive(O e) {
		O info = null;
		NewEQCHive newEQCHive = null;
		NewEQCHiveState newHiveState = null;
		try {
			newEQCHive = new NewEQCHive(e);
			Log.info("MinerNetworkServiceImpl received new EQCHive");
			if(newEQCHive.getSp().isSanity()) {
				info = Util.getDefaultInfo().getProtocol(O.class);
				newHiveState = new NewEQCHiveState();
				newHiveState.setState(State.PENDINGNEWEQCHIVE);
				newHiveState.setNewEQCHive(newEQCHive);
				PendingNewEQCHiveService.getInstance().offerState(newHiveState);
				Log.info("Call PendingNewBlockService handle the new block");
			}
			else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null).getProtocol(O.class);
			}
		} catch (Exception exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
			Log.Error(exception.getMessage());
		}
		return info;
	}

}
