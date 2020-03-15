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

import com.eqcoin.avro.O;
import com.eqcoin.avro.TransactionNetwork;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.rpc.Balance;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.Info;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.rpc.SignHash;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class TransactionNetworkClient extends EQCRPCClient {

	public static Info ping(Cookie<O> cookie, String ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			info = new Info(client.ping(cookie.getProtocol()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
			throw e;
		} finally {
			if (nettyTransceiver != null) {
				nettyTransceiver.close();
				Log.info("nettyTransceiver closed");
				Log.info("nettyTransceiver closed");
			}
		}
		return info;
	}

	public static IPList getMinerList(String ip) throws Exception {
		IPList ipList = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
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

	public static Info sendTransaction(Transaction transaction, String ip) throws Exception {
		Info info = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			info = new Info(client.sendTransaction(Util.bytes2O(transaction.getRPCBytes())));
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

	public static ID getID(byte[] addressAI, String ip) throws Exception {
		ID id = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			id = new ID(client.getID(new O(ByteBuffer.wrap(addressAI))).getO().array());
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
		return id;
	}

	public static Passport getAccount(byte[] addressAI, String ip) throws Exception {
		Passport account = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			account = Passport.parsePassport(client.getAccount(Util.bytes2O(addressAI)).getO().array());
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
		return account;
	}

	public static MaxNonce getMaxNonce(Nest<O> nest, String ip) throws Exception {
		MaxNonce maxNonce = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			maxNonce = new MaxNonce(client.getMaxNonce(nest.getProtocol()));
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
		return maxNonce;
	}

	public static Balance getBalance(Nest<O> nest, String ip) throws Exception {
		Balance balance = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			balance = new Balance(client.getBalance(nest.getProtocol()));
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
		return balance;
	}

	public static SignHash getSignHash(ID id, String ip) throws Exception {
		SignHash signHash = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			signHash = new SignHash(client.getSignHash(Util.bytes2O(id.getEQCBits())));
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
		return signHash;
	}

	public static TransactionList getPendingTransactionList(ID id, String ip) throws Exception {
		TransactionList transactionList = null;
		NettyTransceiver nettyTransceiver = null;
		TransactionNetwork client = null;
		try {
			nettyTransceiver = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(ip), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			client = SpecificRequestor.getClient(TransactionNetwork.class, nettyTransceiver);
			transactionList = new TransactionList(client.getPendingTransactionList(Util.bytes2O(id.getEQCBits())));
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
		return transactionList;
	}

	public static long ping(String remoteIP) {
		NettyTransceiver client = null;
		TransactionNetwork proxy = null;
		long time = System.currentTimeMillis();
		try {
			client = new NettyTransceiver(
					new InetSocketAddress(InetAddress.getByName(remoteIP), Util.TRANSACTION_NETWORK_PORT), new OioClientSocketChannelFactory(
			                Executors.newCachedThreadPool()),
					Util.DEFAULT_TIMEOUT);
			proxy = SpecificRequestor.getClient(TransactionNetwork.class, client);
			proxy.ping(Util.getCookie().getProtocol());
			time = System.currentTimeMillis() - time;
		} catch (Exception e) {
			Log.Error(e.getMessage());
			time = -1;
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return time;
	}

}
