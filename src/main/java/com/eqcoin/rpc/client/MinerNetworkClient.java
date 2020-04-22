/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
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
package com.eqcoin.rpc.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

import com.eqcoin.avro.MinerNetwork;
import com.eqcoin.avro.O;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.Info;
import com.eqcoin.rpc.NewHive;
import com.eqcoin.rpc.TransactionIndexList;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class MinerNetworkClient extends EQCRPCClient {

	public static Info ping(Cookie cookie, String ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			info = new Info(client.ping(cookie.getProtocol(O.class)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public static IPList getMinerList(IP ip) throws Exception {
		IPList ipList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			ipList = new IPList(client.getMinerList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return ipList;
	}

	public static IPList getFullNodeList(String ip) throws Exception {
		IPList ipList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			ipList = new IPList(client.getFullNodeList());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return ipList;
	}

	public static Info broadcastNewBlock(NewHive newHive, IP ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			info = new Info(client.broadcastNewBlock(newHive.getProtocol(O.class)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	public static TransactionIndexList getTransactionIndexList(IP ip) throws Exception {
		TransactionIndexList transactionIndexList = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			O syncTime = new O(ByteBuffer.wrap(Util.longToBytes(EQCBlockChainH2.getInstance().getMinerSyncTime(ip))));
			transactionIndexList = new TransactionIndexList(client.getTransactionIndexList(syncTime));
			EQCBlockChainH2.getInstance().saveMinerSyncTime(ip, transactionIndexList.getSyncTime());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return transactionIndexList;
	}

	public static TransactionList getTransactionList(TransactionIndexList transactionList, IP ip) throws Exception {
		TransactionList transactionList2 = null;
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			transactionList2 = new TransactionList(client.getTransactionList(transactionList.getProtocol(O.class)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return transactionList2;
	}

	public static long ping(IP remoteIP) {
		Log.info("Begin ping: " + remoteIP);
		NettyTransceiver nettyTransceiver = null;
		MinerNetwork client = null;
		long time = System.currentTimeMillis();
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(remoteIP.getIp()), Util.MINER_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(MinerNetwork.class, nettyTransceiver);
			client.ping(Util.getCookie().getProtocol(O.class));
			time = System.currentTimeMillis() - time;
			Log.info("Get ping response from " + remoteIP + " used " + time + "ms");
		} catch (Exception e) {
			Log.Error(e.getMessage());
			time = -1;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return time;
	}

	public static IP getFastestServer(IPList ipList) {
		IP fastestServer = null;
		long time = 0;
		long maxTime = 0;
		for (IP ip : ipList.getIpList()) {
			Log.info("Try to get the fastest server current ip: " + ip);
			time = ping(ip);
			if(maxTime == 0 && time != -1) {
				fastestServer = ip;
				maxTime = time;
			}
			if (time < maxTime) {
				fastestServer = ip;
				maxTime = time;
			}
		}
		return fastestServer;
	}

}
