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
