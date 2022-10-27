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
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.rpc.client.avro;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.eqcoin.avro.EQCHiveSyncNetwork;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TailInfo;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

/**
 * @author Xun Wang
 * @date Jun 28, 2019
 * @email 10509759@qq.com
 */
public class EQCHiveSyncNetworkClient extends EQCRPCClient {

	public static EQCHive getEQCHive(final ID height, final SP sp) throws Exception {
		EQCHive eqcHive = null;
		NettyTransceiver nettyTransceiver = null;
		EQCHiveSyncNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.SYNCBLOCK_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCHiveSyncNetwork.class, nettyTransceiver);
			eqcHive = new EQCHive(client.getEQCHive(height.getProtocol(O.class)));
		} catch (final Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return eqcHive;
	}

	public static EQCHiveRoot getEQCHiveRoot(final ID height, final SP sp) throws Exception {
		EQCHiveRoot eqcHiveRoot = null;
		NettyTransceiver nettyTransceiver = null;
		EQCHiveSyncNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.SYNCBLOCK_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCHiveSyncNetwork.class, nettyTransceiver);
			eqcHiveRoot = new EQCHiveRoot(client.getEQCHiveRoot(height.getProtocol(O.class)));
		} catch (final Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return eqcHiveRoot;
	}

	public static byte[] getEQCHiveRootProof(final ID height, final SP sp) throws Exception {
		byte[] eqcHiveRootProof = null;
		NettyTransceiver nettyTransceiver = null;
		EQCHiveSyncNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.SYNCBLOCK_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCHiveSyncNetwork.class, nettyTransceiver);
			eqcHiveRootProof = client.getEQCHiveRootProof(height.getProtocol(O.class)).getO().array();
		} catch (final Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return eqcHiveRootProof;
	}

	public static TailInfo getEQCHiveTail(final SP sp) throws Exception {
		TailInfo tailInfo = null;
		NettyTransceiver nettyTransceiver = null;
		EQCHiveSyncNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.SYNCBLOCK_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCHiveSyncNetwork.class, nettyTransceiver);
			tailInfo = new TailInfo(client.getEQCHiveTail());
		} catch (final Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return tailInfo;
	}

	public static SP getFastestServer(final SPList spList) {
		SP fastestSP = null;
		Info info;
		long maxPing = 0;
		for (final SP sp : spList.getSPList()) {
			info = registerSP(sp);
			if (info.getPing() > maxPing) {
				fastestSP = sp;
				maxPing = info.getPing();
			}
		}
		return fastestSP;
	}

	public static SPList getSPList(final SP sp) throws Exception {
		SPList spList = null;
		NettyTransceiver nettyTransceiver = null;
		EQCMinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCMinerNetwork.class, nettyTransceiver);
			spList = new SPList(client.getSPList(new ID(SP_MODE.getFlag(SP_MODE.EQCHIVESYNCNETWORK, SP_MODE.EQCMINERNETWORK, SP_MODE.EQCTRANSACTIONNETWORK)).getProtocol(O.class)));
		} catch (final Exception e) {
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

	public static Info registerSP(final SP sp) {
		Info info = new Info();
		NettyTransceiver nettyTransceiver = null;
		EQCHiveSyncNetwork client = null;
		long ping = 0;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.SYNCBLOCK_NETWORK_PORT), new OioClientSocketChannelFactory(
							Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCHiveSyncNetwork.class, nettyTransceiver);
			ping = System.currentTimeMillis();
			info = new Info(client.registerSP(Util.LOCAL_SP.getProtocol(O.class)));
			info.setPing(System.currentTimeMillis() - ping);
		} catch (final Exception e) {
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

}
