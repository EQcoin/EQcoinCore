/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection 
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;

import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.EQCTransactionNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.LockInfo;
import org.eqcoin.rpc.object.LockStatus;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.passport.Passport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;
import org.eqcoin.util.Util.SP_MODE;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCTransactionNetworkClient extends EQCRPCClient {

	public static Info registerSP(SP sp) throws Exception {
		Info info = new Info();
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		long ping = 0;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
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

	public static Info sendTransaction(Transaction transaction, SP sp) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			info = new Info(client.sendTransaction(Gateway.getProtocol(O.class, transaction.getBytes())));
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

	public static LockInfo getLockInfo(LockStatus lockStatus, SP sp) throws Exception {
		LockInfo lockInfo = null;
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			lockInfo = new LockInfo(client.getLockInfo(lockStatus.getProtocol(O.class)).getO().array());
		} catch (Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return lockInfo;
	}

	public static TransactionList getPendingTransactionList(ID id, SP sp) throws Exception {
		TransactionList transactionList = null;
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			transactionList = new TransactionList(client.getPendingTransactionList(id.getProtocol(O.class)));
		} catch (Exception e) {
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
			}
		}
		return transactionList;
	}

	public static long ping(SP sp) {
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		long ping = 0;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			ping = System.currentTimeMillis();
			client.ping();
			ping = System.currentTimeMillis() - ping;
		} catch (Exception e) {
			ping = -1;
			Log.Error(e.getMessage());
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
			}
		}
		return ping;
	}

	public static TransactionIndexList getTransactionIndexList(SP sp) throws Exception {
		TransactionIndexList transactionIndexList = null;
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(sp.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			transactionIndexList = new TransactionIndexList(client.getTransactionIndexList(Gateway.getProtocol(O.class, Util.MC().getSyncTime(sp).getEQCBits())));
			Util.MC().saveSyncTime(sp, transactionIndexList.getSyncTime());
		} catch (Exception e) {
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

	public static TransactionList getTransactionList(TransactionIndexList transactionList, SP ip) throws Exception {
		TransactionList transactionList2 = null;
		NettyTransceiver nettyTransceiver = null;
		EQCTransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip.getIp()), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()), Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(EQCTransactionNetwork.class, nettyTransceiver);
			transactionList2 = new TransactionList(client.getTransactionList(transactionList.getProtocol(O.class)));
		} catch (Exception e) {
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
	
}
