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
package org.eqcoin.service;

import java.io.IOException;

import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;

/**
 * @author Xun Wang
 * @date Jul 13, 2019
 * @email 10509759@qq.com
 */
public class BroadcastNewEQCHiveService extends EQCService {
	private static BroadcastNewEQCHiveService instance;

	public static BroadcastNewEQCHiveService getInstance() {
		if (instance == null) {
			synchronized (BroadcastNewEQCHiveService.class) {
				if (instance == null) {
					instance = new BroadcastNewEQCHiveService();
				}
			}
		}
		return instance;
	}

	private BroadcastNewEQCHiveService() {
		super();
	}

	public void offerNewEQCHiveState(final NewEQCHiveState newEQCHiveState) {
		//		Log.info("offerNewBlockState: " + newBlockState);
		offerState(newEQCHiveState);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(final EQCServiceState state) {
		NewEQCHiveState newEQCHiveState = null;
		try {
			this.state.set(State.BROADCASTNEWEQCHIVE);
			newEQCHiveState = (NewEQCHiveState) state;
			if(!Util.LOCAL_SP.equals(Util.SINGULARITY_SP)) {
				try {
					Log.info("Begin Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to SINGULARITY_SP");
					final Info info = EQCMinerNetworkClient.broadcastNewEQCHive(newEQCHiveState.getNewEQCHive(), Util.SINGULARITY_SP);
					Log.info("Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to SINGULARITY_SP result: " + info.getCode());
				}
				catch (final Exception e) {
					Log.Error(e.getMessage());
				}
			}
			final SPList spList = Util.MC().getSPList(SP_MODE.getFlag(SP_MODE.EQCMINERNETWORK));
			if(!spList.isEmpty()) {
				for(final SP sp:spList.getSPList()) {
					if(!Util.LOCAL_SP.equals(sp)) {
						try {
							Log.info("Begin Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to: " + sp);
							final Info info = EQCMinerNetworkClient.broadcastNewEQCHive(newEQCHiveState.getNewEQCHive(), sp);
							Log.info("Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to: " + sp + " result: " + info.getCode());
						}
						catch (final Exception e) {
							if(e instanceof IOException) {
								Util.updateDisconnectSPStatus(sp);
							}
							Log.Error(e.getMessage());
						}
					}
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(name + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		getInstance();
		super.start();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		instance = null;
	}

}
