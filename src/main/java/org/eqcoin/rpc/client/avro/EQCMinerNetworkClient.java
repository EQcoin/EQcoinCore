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
package org.eqcoin.rpc.client.avro;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.NewEQCHive;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCMinerNetworkClient extends EQCRPCClient {

	public static Info registerSP(SP sp) {
		Info info = new Info();
		NettyTransceiver nettyTransceiver = null;
		EQCMinerNetwork client = null;
		long ping = 0;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCMinerNetwork.class, nettyTransceiver);
			ping = System.currentTimeMillis();
			info = new Info(client.registerSP(Util.LOCAL_SP.getProtocol(O.class)));
			info.setPing(System.currentTimeMillis() - ping);
		} catch (Exception e) {
			info.setPing(-1);
			Log.Error(e.getMessage());
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return info;
	}

	public static SPList getSPList(SP sp) throws Exception {
		SPList spList = null;
		NettyTransceiver nettyTransceiver = null;
		EQCMinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCMinerNetwork.class, nettyTransceiver);
			spList = new SPList(client.getSPList(new ID(SP_MODE.getFlag(SP_MODE.EQCHIVESYNCNETWORK, SP_MODE.EQCMINERNETWORK, SP_MODE.EQCTRANSACTIONNETWORK)).getProtocol(O.class)));
		} catch (Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return spList;
	}

	public static Info broadcastNewEQCHive(NewEQCHive newEQCHive, SP ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		EQCMinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCMinerNetwork.class, nettyTransceiver);
			info = new Info(client.broadcastNewEQCHive(newEQCHive.getProtocol(O.class)));
		} catch (Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return info;
	}

	public static SP getFastestServer(SPList spList) {
		SP fastestSP = null;
		Info info;
		long maxPing = 0;
		for (SP sp : spList.getSPList()) {
			info = registerSP(sp);
			if (info.getPing() > maxPing) {
				fastestSP = sp;
				maxPing = info.getPing();
			}
		}
		return fastestSP;
	}

}
