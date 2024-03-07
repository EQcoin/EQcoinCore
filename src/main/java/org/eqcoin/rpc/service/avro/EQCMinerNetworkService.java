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

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.netty.NettyServer;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.service.BroadcastNewEQCHiveService;
import org.eqcoin.service.PendingNewEQCHiveService;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jan 24, 2019
 * @email 10509759@qq.com
 */
public class EQCMinerNetworkService extends EQCRPCService {
	private static EQCMinerNetworkService instance;
	
	private EQCMinerNetworkService() {
	}
	
	public static EQCRPCService getInstance() {
		if (instance == null) {
			synchronized (EQCMinerNetworkService.class) {
				if (instance == null) {
					instance = new EQCMinerNetworkService();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.rpc.service.EQCRPCService#start()
	 */
	@Override
	public synchronized void start() {
		super.start();
		if (!PendingNewEQCHiveService.getInstance().isRunning()) {
			PendingNewEQCHiveService.getInstance().start();
		}
		if (!BroadcastNewEQCHiveService.getInstance().isRunning()) {
			BroadcastNewEQCHiveService.getInstance().start();
		}
		server = new NettyServer(new SpecificResponder(EQCMinerNetwork.class, new EQCMinerNetworkServiceImpl()),
				new InetSocketAddress(Util.MINER_NETWORK_PORT));
		isRunning.set(true);
		Log.info(this.getClass().getSimpleName() + " started...");
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.rpc.service.EQCRPCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		PendingNewEQCHiveService.getInstance().stop();
		BroadcastNewEQCHiveService.getInstance().stop();
	}
	
}
