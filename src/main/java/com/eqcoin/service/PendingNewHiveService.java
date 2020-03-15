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

import java.util.concurrent.PriorityBlockingQueue;

import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.NewHive;
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.rpc.client.SyncblockNetworkClient;
import com.eqcoin.service.state.EQCServiceState;
import com.eqcoin.service.state.NewHiveState;
import com.eqcoin.service.state.PossibleNodeState;
import com.eqcoin.service.state.SleepState;
import com.eqcoin.service.state.SyncHiveState;
import com.eqcoin.service.state.EQCServiceState.State;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jul 5, 2019
 * @email 10509759@qq.com
 */
public class PendingNewHiveService extends EQCService {
	private static PendingNewHiveService instance;
	
	private PendingNewHiveService() {
		super();
	}

	public static PendingNewHiveService getInstance() {
		if (instance == null) {
			synchronized (PendingNewHiveService.class) {
				if (instance == null) {
					instance = new PendingNewHiveService();
				}
			}
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.
	 * EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		NewHiveState newBlockState = null;
//		long ping = 0;
		try {
//			if (!(SyncBlockService.getInstance().getState() == State.MINER)) {
//				Log.info(name + "Current SyncBlockService's state is: " + SyncBlockService.getInstance().getState()
//						+ " just return");
//				return;
//			}

			this.state.set(State.PENDINGNEWBLOCK);
			newBlockState = (NewHiveState) state;
			Log.info("PendingNewBlockService receive new hive from: " + newBlockState.getNewBlock().getCookie().getIp()
					+ " height: " + newBlockState.getNewBlock().getEqcHive().getHeight());

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
			EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
			// Here need do more job to check if the checkpoint is valid need add checkpoint
			// transaction in NewBlock add isValid in NewBlock to handle this
			if (newBlockState.getNewBlock().getEqcHive().getHeight().compareTo(Util.DB().getEQCBlockTailHeight()) > 0
					&& newBlockState.getNewBlock().getCheckPointHeight()
							.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& newBlockState.getNewBlock().getEqcHive().getEqcHeader().isDifficultyValid()) {
				if(newBlockState.getNewBlock().getEqcHive().getHeight().compareTo(Util.DB().getEQCBlockTailHeight().getNextID()) > 0) {
					if(SyncblockNetworkClient.ping(newBlockState.getNewBlock().getCookie().getIp()) == -1) {
						Log.info("Received new hive and which height:" + newBlockState.getNewBlock().getEqcHive().getHeight() + " is more than one bigger than local tail:" + Util.DB().getEQCBlockTailHeight() + " but it's IP:" + newBlockState.getNewBlock().getCookie().getIp() + " can't reach here have nothing to do");
						return;
					}
				}
				
				// Begin handle PossibleNode
				PossibleNodeState possibleNodeState = new PossibleNodeState();
				possibleNodeState.setIp(newBlockState.getNewBlock().getCookie().getIp());
				possibleNodeState.setNodeType(NODETYPE.MINER);
				PossibleNodeService.getInstance().offerNode(possibleNodeState);

				// Call SyncBlockService valid the new block
				SyncHiveState syncBlockState = new SyncHiveState();
				syncBlockState.setIp(newBlockState.getNewBlock().getCookie().getIp());
				syncBlockState.setEqcHive(newBlockState.getNewBlock().getEqcHive());
				SyncBlockService.getInstance().offerState(syncBlockState);
				Log.info("Newblock is valid call SyncBlockService valid the new block");
			} else {
				Log.info("New block is invalid just discard it");
			}
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public synchronized void offerNewBlockState(NewHiveState newBlockState) {
//		Log.info("PendingNewBlockService offerNewBlockState");
		offerState(newBlockState);
	}

}
