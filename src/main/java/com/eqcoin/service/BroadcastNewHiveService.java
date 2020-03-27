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
package com.eqcoin.service;

import java.io.IOException;

import com.eqcoin.avro.O;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.Info;
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.service.state.EQCServiceState;
import com.eqcoin.service.state.NewHiveState;
import com.eqcoin.service.state.EQCServiceState.State;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 13, 2019
 * @email 10509759@qq.com
 */
public class BroadcastNewHiveService extends EQCService {
	private static BroadcastNewHiveService instance;
	
	private BroadcastNewHiveService() {
		super();
	}
	
	public static BroadcastNewHiveService getInstance() {
		if (instance == null) {
			synchronized (BroadcastNewHiveService.class) {
				if (instance == null) {
					instance = new BroadcastNewHiveService();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		NewHiveState newHiveState = null;
		try {
			this.state.set(State.BROADCASTNEWHIVE);
			newHiveState = (NewHiveState) state;
			if(!Util.LOCAL_IP.equals(Util.SINGULARITY_IP)) {
				try {
					Log.info("Begin Broadcast new hive with height: " + newHiveState.getNewBlock().getEqcHive().getHeight() + " to SINGULARITY_IP");
					Info info = MinerNetworkClient.broadcastNewBlock(newHiveState.getNewBlock(), Util.SINGULARITY_IP);
					Log.info("Broadcast new hive with height: " + newHiveState.getNewBlock().getEqcHive().getHeight() + " to SINGULARITY_IP result: " + info.getCode());
				}
				catch (Exception e) {
					Log.Error(e.getMessage());
				}
			}
			IPList<O> minerList = EQCBlockChainH2.getInstance().getMinerList();
			if(!minerList.isEmpty()) {
				for(IP ip:minerList.getIpList()) {
					if(!Util.LOCAL_IP.equals(ip)) {
						try {
							Log.info("Begin Broadcast new hive with height: " + newHiveState.getNewBlock().getEqcHive().getHeight() + " to: " + ip);
							Info info = MinerNetworkClient.broadcastNewBlock(newHiveState.getNewBlock(), ip);
							Log.info("Broadcast new hive with height: " + newHiveState.getNewBlock().getEqcHive().getHeight() + " to: " + ip + " result: " + info.getCode());
						}
						catch (Exception e) {
							if(e instanceof IOException) {
								Util.updateDisconnectIPStatus(ip);
							}
							Log.Error(e.getMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(name + e.getMessage());
		}
	}

	public void offerNewBlockState(NewHiveState newBlockState) {
//		Log.info("offerNewBlockState: " + newBlockState);
		offerState(newBlockState);
	}
	
}
