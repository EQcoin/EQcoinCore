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

import java.net.InetSocketAddress;

import org.apache.avro.ipc.netty.NettyServer;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.eqcoin.avro.EQCHiveSyncNetwork;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jan 24, 2019
 * @email 10509759@qq.com
 */
public class EQCHiveSyncNetworkService extends EQCRPCService {
	private static EQCHiveSyncNetworkService instance;

	private EQCHiveSyncNetworkService() {
	}
	
	public static EQCHiveSyncNetworkService getInstance() {
		if (instance == null) {
			synchronized (EQCHiveSyncNetworkService.class) {
				if (instance == null) {
					instance = new EQCHiveSyncNetworkService();
				}
			}
		}
		return (EQCHiveSyncNetworkService) instance;
	}

	public void start() {
		super.start();
		server = new NettyServer(new SpecificResponder(EQCHiveSyncNetwork.class, new EQCHiveSyncNetworkServiceImpl()),
				new InetSocketAddress(Util.SYNCBLOCK_NETWORK_PORT));
		Log.info(this.getClass().getSimpleName() + " started...");
	}

}
