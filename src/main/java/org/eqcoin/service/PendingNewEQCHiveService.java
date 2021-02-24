/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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

import java.util.concurrent.PriorityBlockingQueue;

import org.eqcoin.keystore.Keystore;
import org.eqcoin.persistence.globalstate.GlobalState.Mode;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.object.NewEQCHive;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.service.state.PossibleSPState;
import org.eqcoin.service.state.SleepState;
import org.eqcoin.service.state.EQCHiveSyncState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.stateobject.passport.EQcoinRootPassport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 5, 2019
 * @email 10509759@qq.com
 */
public class PendingNewEQCHiveService extends EQCService {
	private static PendingNewEQCHiveService instance;
	
	private PendingNewEQCHiveService() {
		super();
	}

	public static PendingNewEQCHiveService getInstance() {
		if (instance == null) {
			synchronized (PendingNewEQCHiveService.class) {
				if (instance == null) {
					instance = new PendingNewEQCHiveService();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.
	 * EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		NewEQCHiveState newBlockState = null;
//		long ping = 0;
		try {
//			if (!(SyncBlockService.getInstance().getState() == State.MINER)) {
//				Log.info(name + "Current SyncBlockService's state is: " + SyncBlockService.getInstance().getState()
//						+ " just return");
//				return;
//			}

			this.state.set(State.PENDINGNEWEQCHIVE);
			newBlockState = (NewEQCHiveState) state;
			Log.info("PendingNewBlockService receive new hive from: " + newBlockState.getNewEQCHive().getSp()
					+ " height: " + newBlockState.getNewEQCHive().getEQCHive().getRoot().getHeight());

//			onPause();
//			if (!isRunning.get()) {
//				Log.info("Exit from PendingNewBlockService");
//				return;
//			}

//			if (!(SyncBlockService.getInstance().getState() == State.MINER)) {
//				// Here doesn't need do anything because when Find or Sync finished will reach
//				// the tail
//				Log.info(name + "Current SyncBlockService's state is: " + SyncBlockService.getInstance().getState()
//						+ " just return");
//				return;
//			} else {
			EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) Util.GS().getPassport(ID.ZERO);
			ID localTailHeight = Util.GS().getEQCHiveTailHeight();
			// Here need do more job to check if the checkpoint is valid need add checkpoint
			// transaction in NewBlock add isValid in NewBlock to handle this
			if (newBlockState.getNewEQCHive().getEQCHive().getRoot().getHeight().compareTo(localTailHeight) > 0
					&& newBlockState.getNewEQCHive().getCheckPointHeight()
							.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& newBlockState.getNewEQCHive().getEQCHive().getRoot().isDifficultyValid()) {
				if(newBlockState.getNewEQCHive().getEQCHive().getRoot().getHeight().compareTo(Util.GS().getEQCHiveTailHeight().getNextID()) > 0) {
					if(EQCHiveSyncNetworkClient.registerSP(newBlockState.getNewEQCHive().getSp()).getPing() == -1) {
						Log.info("Received new EQCHive height:" + newBlockState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " is more than one bigger than local tail:" + Util.GS().getEQCHiveTailHeight() + " but the SP:" + newBlockState.getNewEQCHive().getSp() + " can't access have to discard it");
						return;
					}
				}
				
				// Begin handle PossibleSP
				PossibleSPState possibleNodeState = new PossibleSPState();
				possibleNodeState.setSp(newBlockState.getNewEQCHive().getSp());
				Log.info("Begin offer possible SP: " + possibleNodeState.getSp());
				PossibleSPService.getInstance().offerState(possibleNodeState);

				// Call EQCServiceProvider valid the new block
				EQCHiveSyncState syncBlockState = new EQCHiveSyncState();
				syncBlockState.setSp(newBlockState.getNewEQCHive().getSp());
				syncBlockState.setEQCHive(newBlockState.getNewEQCHive().getEQCHive());
				EQCServiceProvider.getInstance().offerState(syncBlockState);
				Log.info("New EQCHive is valid call SyncBlockService valid it");
			} else {
				Log.info("New EQCHive's height: " + newBlockState.getNewEQCHive().getEQCHive().getRoot().getHeight() + " is less than local tail: " + localTailHeight + " just discard it");
			}
//			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
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
