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
package org.eqcoin.service;

import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.PossibleSPState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.transaction.operation.Operation.OP;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public final class PossibleSPService extends EQCService {
	private static PossibleSPService instance;
	private SPList blackList;
	
	private PossibleSPService() {
    	super();
    	blackList = new SPList();
	}
	
	public static PossibleSPService getInstance() {
		if (instance == null) {
			synchronized (PossibleSPService.class) {
				if (instance == null) {
					instance = new PossibleSPService();
				}
			}
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected synchronized void onDefault(EQCServiceState state) {
		PossibleSPState possibleSPState = null;
		try {
			this.state.set(State.POSSIBLENODE);
			possibleSPState = (PossibleSPState) state;
			Log.info("Receieve possible SP: " + possibleSPState.getSp());
//			if(blackList.contains(possibleNode.getIp())) {
//				Log.info(possibleNode.getIp() + " already in the black list just return");
//				return;
//			}
			if(Util.SINGULARITY_SP.equals(possibleSPState.getSp())) {
				Log.info(possibleSPState.getSp().getIp() + " is SINGULARITY_SP just return");
				return;
			}
			if(Util.MC().isSPExists(possibleSPState.getSp())) {
				Log.info(possibleSPState.getSp().getIp() + " already in the SP list just return");
				return;
			}
			if(possibleSPState.getSp().isEQCTransactionNetwork()) {
				if(EQCTransactionNetworkClient.registerSP(possibleSPState.getSp()).getPing() > 0) {
					Log.info("Received New EQCTransactionNetwork SP: " + possibleSPState.getSp() + " save it to SPList");
					Util.MC().saveSP(possibleSPState.getSp());
				}
				else {
					Util.updateDisconnectSPStatus(possibleSPState.getSp());
				}
			}
			else if(possibleSPState.getSp().isEQCHiveSyncNetwork()) {
				if(EQCHiveSyncNetworkClient.registerSP(possibleSPState.getSp()).getPing() > 0) {
					Log.info("Received New EQCHiveSyncNetwork SP: " + possibleSPState.getSp() + " save it to SPList");
					Util.MC().saveSP(possibleSPState.getSp());
				}
				else {
					Util.updateDisconnectSPStatus(possibleSPState.getSp());
				}
			}
			else	if(possibleSPState.getSp().isEQCMinerNetwork()) {
				if(EQCMinerNetworkClient.registerSP(possibleSPState.getSp()).getPing() > 0) {
					Log.info("Received New EQCMinerNetwork SP: " + possibleSPState.getSp() + " save it to SPList");
					Util.MC().saveSP(possibleSPState.getSp());
				}
				else {
					Util.updateDisconnectSPStatus(possibleSPState.getSp());
				}
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		instance = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		getInstance();
		super.start();
	}
	
}
