/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
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
package org.eqcoin.service;

import java.io.IOException;

import org.eqcoin.avro.O;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.service.avro.EQCMinerNetworkService;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;

/**
 * @author Xun Wang
 * @date Jul 13, 2019
 * @email 10509759@qq.com
 */
public class BroadcastNewEQCHiveService extends EQCService {
	private static BroadcastNewEQCHiveService instance;
	
	private BroadcastNewEQCHiveService() {
		super();
	}
	
	public static BroadcastNewEQCHiveService getInstance() {
		if (instance == null) {
			synchronized (BroadcastNewEQCHiveService.class) {
				if (instance == null) {
					instance = new BroadcastNewEQCHiveService();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		getInstance();
		super.start();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		NewEQCHiveState newEQCHiveState = null;
		try {
			this.state.set(State.BROADCASTNEWEQCHIVE);
			newEQCHiveState = (NewEQCHiveState) state;
			if(!Util.LOCAL_SP.equals(Util.SINGULARITY_SP)) {
				try {
					Log.info("Begin Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to SINGULARITY_SP");
					Info info = EQCMinerNetworkClient.broadcastNewEQCHive(newEQCHiveState.getNewEQCHive(), Util.SINGULARITY_SP);
					Log.info("Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to SINGULARITY_SP result: " + info.getCode());
				}
				catch (Exception e) {
					Log.Error(e.getMessage());
				}
			}
			SPList spList = Util.MC().getSPList(SP_MODE.getFlag(SP_MODE.EQCMINERNETWORK));
			if(!spList.isEmpty()) {
				for(SP sp:spList.getSPList()) {
					if(!Util.LOCAL_SP.equals(sp)) {
						try {
							Log.info("Begin Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to: " + sp);
							Info info = EQCMinerNetworkClient.broadcastNewEQCHive(newEQCHiveState.getNewEQCHive(), sp);
							Log.info("Broadcast new EQCHive with height: " + newEQCHiveState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " to: " + sp + " result: " + info.getCode());
						}
						catch (Exception e) {
							if(e instanceof IOException) {
								Util.updateDisconnectSPStatus(sp);
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

	public void offerNewEQCHiveState(NewEQCHiveState newEQCHiveState) {
//		Log.info("offerNewBlockState: " + newBlockState);
		offerState(newEQCHiveState);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		instance = null;
	}
	
}
