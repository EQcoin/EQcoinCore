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
