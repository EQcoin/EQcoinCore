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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;

import org.h2.util.IntIntHashMap;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQcoinSeedHeader;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.TailInfo;
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.rpc.client.SyncblockNetworkClient;
import com.eqcoin.rpc.service.MinerNetworkService;
import com.eqcoin.rpc.service.SyncblockNetworkService;
import com.eqcoin.rpc.service.TransactionNetworkService;
import com.eqcoin.service.state.EQCServiceState;
import com.eqcoin.service.state.SleepState;
import com.eqcoin.service.state.SyncHiveState;
import com.eqcoin.service.state.EQCServiceState.State;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.MODE;

/**
 * @author Xun Wang
 * @date Jul 6, 2019
 * @email 10509759@qq.com
 */
public class SyncBlockService extends EQCService {
	private static SyncBlockService instance;
	private MODE mode;

	public static SyncBlockService getInstance() {
		if (instance == null) {
			synchronized (SyncBlockService.class) {
				if (instance == null) {
					instance = new SyncBlockService();
				}
			}
		}
		return instance;
	}

	private SyncBlockService() {
		super();
		mode = MODE.MINER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
		offerState(new EQCServiceState(State.SERVER));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		// Begin stop the network service process
		MinerNetworkService.getInstance().stop();
		SyncblockNetworkService.getInstance().stop();
		// Begin stop the dependent service process
		PossibleNodeService.getInstance().stop();
		PendingTransactionService.getInstance().stop();
		PendingNewHiveService.getInstance().stop();
		BroadcastNewHiveService.getInstance().stop();
		// Begin stop Sync Service
//		SyncService.getInstance().stop();
		TransactionNetworkService.getInstance().stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.
	 * EQCServiceState)
	 */
	@Override
	protected void onDefault(EQCServiceState state) {
		switch (state.getState()) {
		case SERVER:
			this.state.set(State.SERVER);
			onServer(state);
			break;
		case FIND:
			this.state.set(State.FIND);
			onFind(state);
			break;
		case SYNC:
			this.state.set(State.SYNC);
			onSync(state);
			break;
		case MINER:
			this.state.set(State.MINER);
			onMiner(state);
			break;
		default:
			break;
		}
	}

	private void onFind(EQCServiceState state) {
		IPList<O> minerList = null;
		String maxTail = null;
		TailInfo minerTailInfo = null;
		TailInfo maxTailInfo = null;
		boolean isMaxTail = true;

		try {
			// Before here already do syncMinerList in Util.init during every time EQchains core startup
			minerList = Util.DB().getMinerList();
			if (minerList.isEmpty()) {
				if (Util.IP.equals(Util.SINGULARITY_IP)) {
					// This is Singularity node and miner list is empty just start Minering
					Log.info("This is Singularity node and miner list is empty just start Minering");
					// Here just remove extra message because when have new miner join will register in miner list
					pendingMessage.clear();
					offerState(new EQCServiceState(State.MINER));
					return;
				}
			}

			if (!Util.IP.equals(Util.SINGULARITY_IP)) {
				minerList.addIP(Util.SINGULARITY_IP);
			}
			
			Log.info("MinerList's size: " + minerList.getIpList().size());
			Vector<TailInfo<O>> minerTailList = new Vector<>();
			for (String ip : minerList.getIpList()) {
				try {
					// Doesn't need include current Node's ip
					if (!ip.equals(Util.IP)) {
						Log.info("Try to get miner tail: " + ip);
						minerTailInfo = SyncblockNetworkClient.getBlockTail(ip);
					} else {
						Log.info("Current ip " + ip + " is the same as local ip " + Util.IP + " just ignore it");
						continue;
					}
				} catch (Exception e) {
					minerTailInfo = null;
					Log.Error("During get " + ip + " miner tail error occur: " + e.getMessage());
					if(e instanceof IOException) {
						Util.updateDisconnectIPStatus(ip);
					}
				}
				if (minerTailInfo != null) {
					minerTailInfo.setIp(ip);
					minerTailList.add(minerTailInfo);
					Log.info(ip + "'s miner tail is: " + minerTailInfo);
				} else {
					Log.Error("Get " + ip + "'s miner tail is null");
				}
			}
			
			// Miner tail list doesn't need include current Node's ip
			if (minerTailList.isEmpty()) {
				// Miner list isn't empty but can retrieve any miner tail from the miner network
				if (Util.IP.equals(Util.SINGULARITY_IP)) {
					// This is Singularity node and miner list is empty just start Minering
					// Here just remove extra message because when have new miner join will register in miner list
					pendingMessage.clear();
					offerState(new EQCServiceState(State.MINER));
					return;
				} else {
					// Network error all miner node and singularity node can't connect just sleep then try again
					Log.Error(
							"Network error all miner node and singularity node can't connect just sleep then try again");
					sleeping(Util.BLOCK_INTERVAL);
					return;
				}
			}
			
			Comparator<TailInfo> reverseComparator = Collections.reverseOrder();
			Collections.sort(minerTailList, reverseComparator);
			// Retrieve the max Hive TailInfo
			Log.info("minerTailListSize: " + minerTailList.size());
			Log.info("MinerTailList: " + minerTailList.toString());
			maxTailInfo = minerTailList.get(0);
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			Log.info("LocalTail: " + Util.DB().getEQCBlockTailHeight());
			EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
			if (maxTailInfo.getCheckPointHeight().compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& maxTailInfo.getHeight().compareTo(Util.DB().getEQCBlockTailHeight()) > 0) {
				isMaxTail = false;
				IPList minerIpList = new IPList();
				for (TailInfo tailInfo2 : minerTailList) {
					if (tailInfo2.equals(minerTailList.get(0))) {
						minerIpList.addIP(tailInfo2.getIp());
					}
				}
				maxTail = MinerNetworkClient.getFastestServer(minerIpList);
				if (maxTail == null) {
					Log.info("MaxTail can't connect now just sleep then try find again");
					sleeping(Util.BLOCK_INTERVAL);
					return;
				}
			}
			
			if (!isMaxTail) {
				Log.info("Find max tail begin sync to: " + maxTail);
				// Begin sync to MaxTail
				SyncHiveState syncBlockState = new SyncHiveState();
				syncBlockState.setIp(maxTail);
				offerState(syncBlockState);
			} else {
				if (mode == MODE.MINER) {
					Log.info("Miner node just begin mining");
					offerState(new EQCServiceState(State.MINER));
				} else {
					// Full node just sleep then try to find&sync again
					Log.info("Full node just sleep then try to find&sync again");
					sleeping(Util.BLOCK_INTERVAL);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch bloc
			e.printStackTrace();
			Log.Error(name + e.getMessage());
			sleeping(Util.BLOCK_INTERVAL / 60);
		}
	}

	private void onSync(EQCServiceState state) {
		SyncHiveState syncHiveState = (SyncHiveState) state;
		ChangeLog changeLog = null;
		boolean isValidChain = false;

		try {
			// Here add synchronized to avoid conflict with Miner service handle new mining block
			if (syncHiveState.getEqcHive() != null) {
				// Received new EQCHive from the Miner network
				synchronized (EQCService.class) {
					Log.info("Begin synchronized (EQCService.class)");
					Log.info("Received new hive tail from " + syncHiveState.getIp());
					// Check if new block's height is bigger than local tail
					if (syncHiveState.getEqcHive().getHeight().compareTo(Util.DB().getEQCBlockTailHeight()) <= 0) {
						Log.info("New block's height: " + syncHiveState.getEqcHive().getHeight()
								+ " not bigger than local tail: " + Util.DB().getEQCBlockTailHeight()
								+ " just discard it");
						return;
					} else {
						EQCHive localTailHive = Util.DB().getEQCHive(Util.DB().getEQCBlockTailHeight(), true);
						if (syncHiveState.getEqcHive().getHeight().isNextID(localTailHive.getHeight())) {
							if (Arrays.equals(syncHiveState.getEqcHive().getEqcHeader().getPreHash(),
									localTailHive.getHash())) {
								Log.info("New block is current tail's next block just begin verify it");
								changeLog = new ChangeLog(syncHiveState.getEqcHive().getHeight(),
										new Filter(Mode.VALID));
								if (syncHiveState.getEqcHive().isValid(changeLog)) {
									// Maybe here need do more job
									Util.DB().saveEQCHive(syncHiveState.getEqcHive());
									changeLog.updateGlobalState();
									Util.DB().saveEQCBlockTailHeight(changeLog.getHeight());
									Log.info("New block valid passed and saved, changed to new tail");
									// Have changed tail so offer miner
									Log.info("Have changed tail so offer miner");
									offerState(new EQCServiceState(State.MINER));
								} else {
									Log.info("New block valid failed");
								}
							}
							Log.info("End synchronized (EQCService.class)");
							return;
						} else {
							Log.info("MaxTail's height: " + syncHiveState.getEqcHive().getHeight()
									+ " bigger than local tail: " + Util.DB().getEQCBlockTailHeight()
									+ " begin sync to it");
						}
					}
					Log.info("End synchronized (EQCService.class)");
				}
			} else {
				Log.info("Received MaxTail from Find just begin sync");
			}
			
			TailInfo maxTailInfo = SyncblockNetworkClient.getBlockTail(syncHiveState.getIp());
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			ID localTail = null;
			synchronized (EQCService.class) {
				Log.info("Begin synchronized (EQCService.class)");
				if (MinerService.getInstance().isRunning()) {
					Log.info("MinerService is running now begin pause");
					MinerService.getInstance().pause();
				} else {
					Log.info("MinerService isn't running now has nothing to do");
				}
				localTail = Util.DB().getEQCBlockTailHeight();
				Log.info("LocalTail: " + localTail);
				EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
				long base = localTail.longValue();
				// Check if it is valid chain
				if (maxTailInfo.getHeight().compareTo(localTail) > 0 && maxTailInfo.getCheckPointHeight()
						.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0) {
					// Try to find local chain's which height match with max tail chain
					Log.info("Try to find local chain's which height match with max tail chain");
					for (; base >= eQcoinSubchainAccount.getCheckPointHeight().longValue(); --base) {
						if (Arrays.equals(Util.DB().getEQCHeaderHash(ID.valueOf(base)),
								SyncblockNetworkClient.getEQCHeaderHash(ID.valueOf(base), syncHiveState.getIp()))) {
							Log.info("Current max tail chain's local base height is: " + base);
							isValidChain = true;
							break;
						}
					}
					
					if (isValidChain) {
						// Check if from base + 1 to the fork chain's tail's difficulty is valid.
						// And also need check if Checkpoint is valid.
						// Changed to check if Checkpoint transaction is valid.
//						for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
//							Log.info("Begin verify if No." + i + "'s EQCHeader's difficulty is valid");
//							if (SyncblockNetworkClient.getEQCHeader(ID.valueOf(i), syncHiveState.getIp())
//									.isDifficultyValid()) {
//								Log.info("No." + i + "'s EQCHeader's difficulty is valid");
//							} else {
//								Log.info(syncHiveState.getIp() + " isn't valid chain because No. " + i
//										+ " 's EQCHeader's difficulty is invalid");
//								isValidChain = false;
//								break;
//							}
//						}
					}

					// Check if need sync block
					if (isValidChain) {
						Log.info("MaxTail is valid chain begin sync");
						// Recovery Accounts' state to base height
						Log.info("Begin recovery Accounts' state to base height: " + base);
						// Remove fork block
						if (base < localTail.longValue()) {
							Log.info("Begin delete EQCHive from " + (base + 1) + " to " + localTail.longValue());
							for(long i=(base+1); i<=localTail.longValue(); ++i) {
								Util.DB().deleteEQCHive(ID.valueOf(i));
							}
						} else {
							Log.info("Base " + base + " equal to local tail " + localTail.longValue() + " do nothing");
						}

						if (ID.valueOf(base).compareTo(Util.DB().getEQCBlockTailHeight()) < 0) {
							Log.info("Base " + base + " equal to local tail " + localTail.longValue() + " beginning recovery and remove Account snapshot");
							// Recovery base height Accounts table's status
							Util.recoveryAccountsStatusTo(ID.valueOf(base));
							// Remove Snapshot
							Util.DB().deletePassportSnapshotFrom(ID.valueOf(base + 1), true);
						}
					
						// Remove extra Account here need remove accounts after base
						ID originalAccountNumbers = eQcoinSubchainAccount.getTotalPassportNumbers();
						EQcoinSeedHeader eQcoinSubchainHeader = Util.DB().getEQCHive(ID.valueOf(base), true)
								.geteQcoinSeed().getEQcoinSubchainHeader();
						if (eQcoinSubchainHeader.getTotalPassportNumbers().compareTo(originalAccountNumbers) < 0) {
							Log.info("Begin delete extra Account from "
									+ eQcoinSubchainHeader.getTotalPassportNumbers().getNextID() + " to "
									+ originalAccountNumbers);
							for(long i=eQcoinSubchainHeader.getTotalPassportNumbers().getNextID().longValue(); i<=originalAccountNumbers.longValue(); ++i) {
								Util.DB().deletePassport(ID.valueOf(i), Mode.GLOBAL);
							}
						} else {
							Log.info(
									"Base height's TotalAccountNumbers " + eQcoinSubchainHeader.getTotalPassportNumbers()
											+ " equal to local tail " + originalAccountNumbers + " do nothing");
						}
						Util.DB().saveEQCBlockTailHeight(ID.valueOf(base));

						// Begin sync to tail
						EQCHive maxTailHive = null;
						for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
							Log.info("Begin sync No. " + i + " block from " + syncHiveState.getIp());
							maxTailHive = SyncblockNetworkClient.getBlock(ID.valueOf(i), syncHiveState.getIp());
							if (maxTailHive == null) {
								Log.Error("During sync block error occur just  goto find again");
								break;
							}
							changeLog = new ChangeLog(ID.valueOf(i), new Filter(Mode.VALID));
							if (maxTailHive.isValid(changeLog)) {
								Log.info("Verify No. " + i + " hive passed");
								Util.DB().saveEQCHive(maxTailHive);
								changeLog.updateGlobalState();
								Util.DB().saveEQCBlockTailHeight(changeLog.getHeight());
								Log.info("Current new tail: " + Util.DB().getEQCBlockTailHeight());
							} else {
								Log.Error("Valid blockchain failed just goto sleep");
								changeLog.clear();
								sleeping(Util.BLOCK_INTERVAL);
								return;
							}
						}
					} else {
						Log.info("MaxTail is invliad chain just goto find");
					}
					
					// Remove extra message because of when sync finished will begin find
					if(isRunning()) {
						Log.info("Remove extra message because of when sync finished will begin find");
						pendingMessage.clear();
					}
					// Successful sync to current tail just goto find to check if reach the tail
					Log.info("Successful sync to current tail just goto find to check if reach the tail");
					offerState(new EQCServiceState(State.FIND));
				} else {
					Log.info("MaxTail isn't bigger than local tail just discard it");
				}
				Log.info("End synchronized (EQCService.class)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(name + e.getMessage());
			if(e instanceof IOException) {
				Util.updateDisconnectIPStatus(syncHiveState.getIp());
			}
			sleeping(Util.BLOCK_INTERVAL);
		}
	}

	private void onMiner(EQCServiceState state) {
		boolean isNeedRestart = false;
		Log.info("onMiner");
		if (!MinerService.getInstance().isRunning()) {
			Log.info("Begin start new MinerService");
			MinerService.getInstance().start();
		} else {
			try {
				// Here need add synchronized lock to double avoid conflict with MinerService
				synchronized (EQCService.class) {
					Log.info("Begin synchronized (EQCService.class)");
					if (!MinerService.getInstance().getNewBlockHeight().isNextID(Util.DB().getEQCBlockTailHeight())) {
						Log.info("Changed to new mining base begin stop current mining progress");
						MinerService.getInstance().stopMining();
						isNeedRestart = true;
					}
					Log.info("End synchronized (EQCService.class)");
				}
				if (isNeedRestart) {
					Log.info("Need restart just beginning new mining progress");
					MinerService.getInstance().startMining();;
				} else {
					if (MinerService.getInstance().isPausing.get()) {
						Log.info("Still mining in the tail and Miner service was paused just resume it");
						MinerService.getInstance().resumePause();
					} else {
						Log.info("Still mining in the tail and Miner service is running now doesn't need do anything");
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
				sleeping(Util.BLOCK_INTERVAL);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#onSleep(com.eqchains.service.state.
	 * SleepState)
	 */
	@Override
	protected void onSleep(SleepState state) {
		Log.info("Sleep finished begin find");
		offerState(new EQCServiceState(State.FIND));
	}

	private void onServer(EQCServiceState state) {
		// During start service if any exception occur will interrupt the process then
		// here will do nothing
		// Begin start the Full node relevant service process
		if (!PossibleNodeService.getInstance().isRunning()) {
			PossibleNodeService.getInstance().start();
		}
		if (!SyncblockNetworkService.getInstance().isRunning()) {
			SyncblockNetworkService.getInstance().start();
		}
		// Begin start the Miner node relevant service process
		if(mode == MODE.MINER) {
			if (!PendingTransactionService.getInstance().isRunning()) {
				PendingTransactionService.getInstance().start();
			}
			if (!PendingNewHiveService.getInstance().isRunning()) {
				PendingNewHiveService.getInstance().start();
			}
			if (!BroadcastNewHiveService.getInstance().isRunning()) {
				BroadcastNewHiveService.getInstance().start();
			}
			// Begin Sync Service
//			SyncService.getInstance().start();
			// Begin start the network service process
			if (!MinerNetworkService.getInstance().isRunning()) {
				MinerNetworkService.getInstance().start();
			}
			if (!TransactionNetworkService.getInstance().isRunning()) {
				TransactionNetworkService.getInstance().start();
			}
		}
	
		try {
			if (mode == MODE.MINER) {
				if (!Util.IP.equals(Util.SINGULARITY_IP)) {
					long time = 0;
					for (int i = 0; i < 3; ++i) {
						Log.info(Util.IP + " begin send ping to register to " + Util.SINGULARITY_IP);
						time = MinerNetworkClient.ping(Util.SINGULARITY_IP);
						if (time != -1) {
							break;
						}
					}
				}
			}
			Util.syncMinerList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		offerState(new EQCServiceState(State.FIND));
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(MODE mode) {
		this.mode = mode;
	}

}
