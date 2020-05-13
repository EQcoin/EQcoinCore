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
package org.eqcoin.service;

import java.io.IOException;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Filter;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.passport.EQcoinRootPassport;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.SPList;
import org.eqcoin.rpc.TailInfo;
import org.eqcoin.rpc.client.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.client.EQCMinerNetworkClient;
import org.eqcoin.rpc.service.EQCHiveSyncNetworkService;
import org.eqcoin.rpc.service.EQCMinerNetworkService;
import org.eqcoin.rpc.service.EQCTransactionNetworkService;
import org.eqcoin.seed.EQcoinSeedRoot;
import org.eqcoin.service.state.EQCHiveSyncState;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.service.state.SleepState;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;

/**
 * The main entrance for EQCServiceProvider.
 * <P>
 * EQcoin core support 3 types of EQCServiceProvider:
 * <P>
 * 1. EQCMinerNetwork.
 * <P>
 * 2. EQCHiveSyncNetwork.
 * <P>
 * 3. EQCTransactionNetwork.
 * <P>
 * If the SP is miner node or full mode need use this class to boot up. When the
 * SP is boot up which will try to find the longest EQCHive tail then sync it.
 * After finished the longest EQCHive tail sync if the SP is full node it will
 * sleep some time then repeat the FIND&SYNC operation, if the SP is miner mode
 * it will begin mining new EQCHive.
 * 
 * @author Xun Wang
 * @date Jul 6, 2019
 * @email 10509759@qq.com
 */
public class EQCServiceProvider extends EQCService {
	private static EQCServiceProvider instance;
	private SP sp;

	public static EQCServiceProvider getInstance() {
		if (instance == null) {
			synchronized (EQCServiceProvider.class) {
				if (instance == null) {
					instance = new EQCServiceProvider();
				}
			}
		}
		return instance;
	}

	private EQCServiceProvider() {
		super();
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
		offerState(new EQCServiceState(State.BOOTUP));
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
		if(sp.isEQCMinerNetwork()) {
			EQCMinerNetworkService.getInstance().stop();
		}
		if(sp.isEQCHiveSyncNetwork()) {
			EQCHiveSyncNetworkService.getInstance().stop();
		}
		if(sp.isEQCTransactionNetwork()) {
			EQCTransactionNetworkService.getInstance().stop();
		}
		// Begin stop the dependent service process
		PossibleSPService.getInstance().stop();
		instance = null;
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
		case BOOTUP:
			// Start the relevant service according to the mode
			this.state.set(State.BOOTUP);
			onBootUp(state);
			break;
		case FIND:
			// Try to find the longest EQCHive tail in current miner network
			this.state.set(State.FIND);
			onFind(state);
			break;
		case SYNC:
			// Sync the new EQCHive from current longest EQCHive tail
			this.state.set(State.SYNC);
			onSync(state);
			break;
		case MINER:
			// Mining new EQCHive based on current longest EQCHive tail
			this.state.set(State.MINER);
			onMiner(state);
			break;
		default:
			break;
		}
	}

	private void onFind(EQCServiceState state) {
		SPList minerList = null;
		SP maxTail = null;
		TailInfo minerTailInfo = null;
		TailInfo maxTailInfo = null;
		boolean isMaxTail = true;

		try {
			// Before here already do syncMinerList in Util.init during every time EQcoin core startup
			minerList = Util.DB().getSPList(SP_MODE.getFlag(SP_MODE.EQCMINERNETWORK));
			if (minerList.isEmpty()) {
				if (Util.LOCAL_SP.equals(Util.SINGULARITY_SP)) {
					// This is Singularity node and miner list is empty just start Minering
					Log.info("This is Singularity node and miner list is empty just start Minering");
					// Here just remove extra message because when have new miner join will register in miner list
					// 20200509 here maybe exists bugs need do more job
					pendingMessage.clear();
					offerState(new EQCServiceState(State.MINER));
					return;
				}
			}

			if (!Util.LOCAL_SP.equals(Util.SINGULARITY_SP)) {
				minerList.addSP(Util.SINGULARITY_SP);
			}
			
			Log.info("MinerList's size: " + minerList.getSPList().size());
			Vector<TailInfo> minerTailList = new Vector<>();
			for (SP sp : minerList.getSPList()) {
				try {
					// Doesn't need include current Node's ip
					if (!sp.equals(Util.LOCAL_SP)) {
						Log.info("Try to get miner tail: " + sp);
						minerTailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(sp);
					} else {
						Log.info("Current SP " + sp + " is the same as local SP " + Util.LOCAL_SP + " just ignore it");
						continue;
					}
				} catch (Exception e) {
					minerTailInfo = null;
					Log.Error("During get " + sp + " miner tail error occur: " + e.getMessage());
					if(e instanceof IOException) {
						Util.updateDisconnectSPStatus(sp);
					}
				}
				if (minerTailInfo != null) {
					minerTailInfo.setSp(sp);
					minerTailList.add(minerTailInfo);
					Log.info(sp + "'s miner tail is: " + minerTailInfo);
				} else {
					Log.Error("Get " + sp + "'s miner tail is null");
				}
			}
			
			if (minerTailList.isEmpty()) {
				if (Util.LOCAL_SP.equals(Util.SINGULARITY_SP)) {
					// This is Singularity node and miner list is empty just start Minering
					// Here just remove extra message because when have new miner join will register in miner list
					pendingMessage.clear(); // Here maybe exists bugs need do more job
					offerState(new EQCServiceState(State.MINER));
					return;
				} else {
					// Network error all miner node and singularity node can't connect just sleep then try again
					Log.Error(
							"Network error all miner node and singularity node can't connect just sleep then try again");
					sleeping(Util.getCurrentEQCHiveInterval().longValue());
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
			Log.info("LocalTail: " + Util.DB().getEQCHiveTailHeight());
			EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) Util.DB().getPassport(ID.ZERO, Mode.GLOBAL);
			if (maxTailInfo.getCheckPointHeight().compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& maxTailInfo.getHeight().compareTo(Util.DB().getEQCHiveTailHeight()) > 0) {
				isMaxTail = false;
				SPList minerIpList = new SPList();
				for (TailInfo tailInfo2 : minerTailList) {
					if (tailInfo2.equals(minerTailList.get(0))) {
						minerIpList.addSP(tailInfo2.getSp());
					}
				}
				maxTail = EQCMinerNetworkClient.getFastestServer(minerIpList);
				if (maxTail == null) {
					Log.info("MaxTail can't connect now just sleep then try find&sync again");
					sleeping(Util.getCurrentEQCHiveInterval().longValue());
					return;
				}
			}
			
			if (!isMaxTail) {
				Log.info("Find max tail begin sync to: " + maxTail);
				// Begin sync to MaxTail
				EQCHiveSyncState eqcHiveSyncState = new EQCHiveSyncState();
				eqcHiveSyncState.setSp(maxTail);
				offerState(eqcHiveSyncState);
			} else {
				if (sp.isEQCMinerNetwork()) {
					Log.info("Miner node just begin mining");
					offerState(new EQCServiceState(State.MINER));
				} else {
					// Full node just sleep then try to find&sync again
					Log.info("Full node just sleep then try to find&sync again");
					sleeping(Util.getCurrentEQCHiveInterval().longValue());
				}
			}
		} catch (Exception e) {
			Log.Error(name + e.getMessage());
			try {
				sleeping(Util.getCurrentEQCHiveInterval().longValue() / 60);
			} catch (Exception e1) {
				Log.Error(e1.getMessage());
			}
		}
	}

	private void onSync(EQCServiceState state) {
		EQCHiveSyncState eqcHiveSyncState = (EQCHiveSyncState) state;
		ChangeLog changeLog = null;
		boolean isValidChain = false;

		try {
			// Here add synchronized to avoid conflict with Miner service handle new mining block
			if (eqcHiveSyncState.getEQCHive() != null) {
				// Received new EQCHive from the Miner network
				synchronized (EQCService.class) {
					Log.info("Begin synchronized (EQCService.class)");
					Log.info("Received new hive tail from " + eqcHiveSyncState.getSp());
					// Check if new block's height is bigger than local tail
					if (eqcHiveSyncState.getEQCHive().getHeight().compareTo(Util.DB().getEQCHiveTailHeight()) <= 0) {
						Log.info("New block's height: " + eqcHiveSyncState.getEQCHive().getHeight()
								+ " not bigger than local tail: " + Util.DB().getEQCHiveTailHeight()
								+ " just discard it");
						return;
					} else {
						EQCHive localTailHive = new EQCHive( Util.DB().getEQCHive(Util.DB().getEQCHiveTailHeight()));
						if (eqcHiveSyncState.getEQCHive().getHeight().isNextID(localTailHive.getHeight())) {
							if (Arrays.equals(eqcHiveSyncState.getEQCHive().getEQCHiveRoot().getPreProof(),
									localTailHive.getProof())) {
								Log.info("New block is current tail's next block just begin verify it");
								changeLog = new ChangeLog(eqcHiveSyncState.getEQCHive().getHeight(),
										new Filter(Mode.VALID));
								if (eqcHiveSyncState.getEQCHive().isValid()) {
									Savepoint savepoint = Util.DB().getConnection().setSavepoint();
									try {
										changeLog.updateGlobalState(eqcHiveSyncState.getEQCHive(), savepoint);
									} catch (Exception e) {
										Log.Error(e.getMessage());
									}
								} else {
									Log.info("NewEQCHive valid failed");
								}
							}
							Log.info("End synchronized (EQCService.class)");
							return;
						} else {
							Log.info("MaxTail's height: " + eqcHiveSyncState.getEQCHive().getHeight()
									+ " bigger than local tail: " + Util.DB().getEQCHiveTailHeight()
									+ " begin sync to it");
						}
					}
					Log.info("End synchronized (EQCService.class)");
				}
			} else {
				Log.info("Received MaxTail from Find just begin sync");
			}
			
			TailInfo maxTailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(eqcHiveSyncState.getSp());
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
				localTail = Util.DB().getEQCHiveTailHeight();
				Log.info("LocalTail: " + localTail);
				EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) Util.DB().getPassport(ID.ZERO, Mode.GLOBAL);
				EQcoinSeedRoot eQcoinSeedRoot = Util.DB().getEQcoinSeedRoot(localTail);
				long base = localTail.longValue();
				// Check if it is valid chain
				if (maxTailInfo.getHeight().compareTo(localTail) > 0 && maxTailInfo.getCheckPointHeight()
						.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0) {
					// Try to find local chain's which height match with max tail chain
					Log.info("Try to find local chain's which height match with max tail chain");
					for (; base >= eQcoinSubchainAccount.getCheckPointHeight().longValue(); --base) {
						if (Arrays.equals(Util.DB().getEQCHiveRootProof(new ID(base)),
								EQCHiveSyncNetworkClient.getEQCRootProof(new ID(base), eqcHiveSyncState.getSp()))) {
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
//							if (SyncblockNetworkClient.getEQCHeader(new ID(i), syncHiveState.getIp())
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
						Savepoint savepoint = Util.DB().getConnection().setSavepoint();
						Log.info("MaxTail is valid chain begin sync: " + savepoint);
						// Recovery Accounts' state to base height
						Log.info("Begin recovery Accounts' state to base height: " + base);
						// Remove fork block
						if (base < localTail.longValue()) {
							Log.info("Begin delete EQCHive from " + (base + 1) + " to " + localTail.longValue());
							for(long i=(base+1); i<=localTail.longValue(); ++i) {
								Util.DB().deleteEQCHive(new ID(i));
							}
						} else {
							Log.info("Base " + base + " equal to local tail " + localTail.longValue() + " do nothing");
						}

						if (new ID(base).compareTo(Util.DB().getEQCHiveTailHeight()) < 0) {
							Log.info("Base " + base + " equal to local tail " + localTail.longValue() + " beginning recovery and remove Account snapshot");
							// Recovery base height Accounts table's status
							Util.recoveryAccountsStatusTo(new ID(base));
							// Remove Snapshot
							Util.DB().deletePassportSnapshotFrom(new ID(base + 1), true);
						}
					
						// Remove extra Account here need remove accounts after base
						ID originalAccountNumbers = eQcoinSeedRoot.getTotalPassportNumbers();
						EQcoinSeedRoot eQcoinSubchainHeader = Util.DB().getEQcoinSeedRoot(new ID(base));
						if (eQcoinSubchainHeader.getTotalPassportNumbers().compareTo(originalAccountNumbers) < 0) {
							Log.info("Begin delete extra Account from "
									+ eQcoinSubchainHeader.getTotalPassportNumbers().getNextID() + " to "
									+ originalAccountNumbers);
							for(long i=eQcoinSubchainHeader.getTotalPassportNumbers().getNextID().longValue(); i<=originalAccountNumbers.longValue(); ++i) {
								Util.DB().deletePassport(new ID(i), Mode.GLOBAL);
							}
						} else {
							Log.info(
									"Base height's TotalAccountNumbers " + eQcoinSubchainHeader.getTotalPassportNumbers()
											+ " equal to local tail " + originalAccountNumbers + " do nothing");
						}
						Util.DB().saveEQCHiveTailHeight(new ID(base));

						// Begin sync to tail
						EQCHive maxTailHive = null;
						for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
							Log.info("Begin sync No. " + i + " block from " + eqcHiveSyncState.getSp());
							maxTailHive = EQCHiveSyncNetworkClient.getEQCHive(new ID(i), eqcHiveSyncState.getSp());
							if (maxTailHive == null) {
								Log.Error("During sync block error occur just  goto find again: " + savepoint);
								if (savepoint != null) {
									Log.info("Begin rollback");
									Util.DB().getConnection().rollback(savepoint);
									Log.info("Rollback successful");
								}
								break;
							}
							changeLog = new ChangeLog(new ID(i), new Filter(Mode.VALID));
							if (maxTailHive.isValid()) {
								Log.info("Verify No. " + i + " hive passed");
								try {
									changeLog.updateGlobalState(maxTailHive, null);
								} catch (Exception e) {
									Log.Error("Valid new EQCHive failed just goto sleep: " + savepoint);
									if (savepoint != null) {
										Log.info("Begin rollback");
										Util.DB().getConnection().rollback(savepoint);
										Log.info("Rollback successful");
									}
									throw e;
								}
								Log.info("Current new tail: " + Util.DB().getEQCHiveTailHeight());
							} else {
								Log.Error("Valid max EQCHive chain failed just goto sleep: " + savepoint);
								if (savepoint != null) {
									Log.info("Begin rollback");
									Util.DB().getConnection().rollback(savepoint);
									Log.info("Rollback successful");
								}
								sleeping(Util.getCurrentEQCHiveInterval().longValue());
								return;
							}
						}
						
						if(isRunning()) {
							// Remove extra message because of when sync finished will begin find
							pendingMessage.clear();
						}
						Log.info("Successful sync to current tail just goto find to check if reach the tail");
						offerState(new EQCServiceState(State.FIND));
					} else {
						Log.info("MaxTail is invliad chain just just discard it");
					}
				} else {
					Log.info("MaxTail isn't bigger than local tail just discard it");
				}
				Log.info("End synchronized (EQCService.class)");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(name + e.getMessage());
			if(e instanceof IOException) {
				Util.updateDisconnectSPStatus(eqcHiveSyncState.getSp());
			}
			try {
				sleeping(Util.getCurrentEQCHiveInterval().longValue());
			} catch (Exception e1) {
				Log.Error(e1.getMessage());
			}
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
					if (!MinerService.getInstance().getNewEQCHiveHeight().isNextID(Util.DB().getEQCHiveTailHeight())) {
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
				try {
					sleeping(Util.getCurrentEQCHiveInterval().longValue());
				} catch (Exception e1) {
					e1.printStackTrace();
					Log.Error(e1.getMessage());
				}
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

	private void onBootUp(EQCServiceState state) {
		// During start service if any exception occur will interrupt the process then
		// here will do nothing
		// Begin start the Full node relevant service process
		if (!PossibleSPService.getInstance().isRunning()) {
			PossibleSPService.getInstance().start();
		}
		if(sp.isEQCHiveSyncNetwork()) {
			if (!EQCHiveSyncNetworkService.getInstance().isRunning()) {
				EQCHiveSyncNetworkService.getInstance().start();
			}
		}
		if(sp.isEQCTransactionNetwork()) {
			if (!EQCTransactionNetworkService.getInstance().isRunning()) {
				EQCTransactionNetworkService.getInstance().start();
			}
		}
		// Begin start the Miner node relevant service process
		if(sp.isEQCMinerNetwork()) {
			// Begin start the network service process
			if (!EQCMinerNetworkService.getInstance().isRunning()) {
				EQCMinerNetworkService.getInstance().start();
			}
		}
		try {
			Util.syncSPList();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		offerState(new EQCServiceState(State.FIND));
	}

	/**
	 * @param sp_mode the mode to set
	 */
	public EQCServiceProvider setSp(SP sp) {
		this.sp = sp;
		return this;
	}
	
}
