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
import org.eqcoin.hive.EQCHiveRoot;
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
		getInstance();
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
		Savepoint savepointSync = null;
		Savepoint savepointBroadcastNewEQCHive = null;
		
		try {
			// Here add synchronized to avoid conflict with Miner service handle new mining EQCHive
			if (eqcHiveSyncState.getEQCHive() != null) {
				// Current sp is EQCMinerNetwork and received new EQCHive from the Miner network
				synchronized (EQCService.class) {
					Log.info("Begin synchronized (EQCService.class)");
					Log.info("Received new EQCHive tail from " + eqcHiveSyncState.getSp());
					// Check if new block's height is bigger than local tail
					ID localTailHeight = Util.DB().getEQCHiveTailHeight();
					if (eqcHiveSyncState.getEQCHive().getHeight().compareTo(localTailHeight) <= 0) {
						Log.info("New EQCHive's height: " + eqcHiveSyncState.getEQCHive().getHeight()
								+ " not bigger than local tail: " + localTailHeight
								+ " just discard it");
						return;
					} else {
						byte[] localTailRootProof = Util.DB().getEQCHiveRootProof(localTailHeight);
						if (eqcHiveSyncState.getEQCHive().getHeight().isNextID(localTailHeight)) {
							if (Arrays.equals(eqcHiveSyncState.getEQCHive().getEQCHiveRoot().getPreProof(),
									localTailRootProof)) {
								Log.info("New EQCHive is current tail's next hive just begin verify it");
								changeLog = new ChangeLog(eqcHiveSyncState.getEQCHive().getHeight(),
										new Filter(Mode.VALID));
								eqcHiveSyncState.getEQCHive().setChangeLog(changeLog);
								if (eqcHiveSyncState.getEQCHive().isValid()) {
									try {
										Savepoint savepointNext = Util.DB().getConnection().setSavepoint("Verfiy next hive");
										changeLog.updateGlobalState(eqcHiveSyncState.getEQCHive(), savepointNext);
										Log.info("New EQCHive is valid and saved successful");
										Log.info("Miner service isRunning: " + MinerService.getInstance().isRunning + " isPausing: " + MinerService.getInstance().isPausing + " isMining: " + MinerService.getInstance().isMining());
										if(MinerService.getInstance().isMining()) {
											MinerService.getInstance().stopMining();
											MinerService.getInstance().startMining();
										}
									} catch (Exception e) {
										// Due to new EQCHive is invalid here have nothing to do just continue mining
										Log.Error(e.getMessage());
									}
								} else {
									Log.info("New EQCHive valid failed");
								}
							}
							Log.info("End synchronized (EQCService.class)");
							return;
						} else {
							Log.info("MaxTail's height: " + eqcHiveSyncState.getEQCHive().getHeight()
									+ " bigger than local tail: " + localTailHeight
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
			ID localTailHeight = null;
			synchronized (EQCService.class) {
				Log.info("Begin synchronized (EQCService.class)");
				if(eqcHiveSyncState.getEQCHive() != null) {
					if (MinerService.getInstance().isRunning() && MinerService.getInstance().isMining.get() && !MinerService.getInstance().isPausing.get()) {
						Log.info("MinerService is running and mining just begin pause it");
						MinerService.getInstance().pause();
						Log.info("MinerService paused now");
					} else {
						Log.info("MinerService isRunning:" + MinerService.getInstance().isRunning() + " and isMining: " + MinerService.getInstance().isMining.get() + " and isPausing: " + MinerService.getInstance().isPausing.get() + " now has nothing to do");
					}
				}

				localTailHeight = Util.DB().getEQCHiveTailHeight();
				Log.info("LocalTailHeight: " + localTailHeight);
				// 20200514 here need analysis if need retrieve the passport from current mining table if exists?
				EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) Util.DB().getPassport(ID.ZERO, Mode.GLOBAL);
				EQcoinSeedRoot eQcoinSeedRoot = Util.DB().getEQcoinSeedRoot(localTailHeight);
				long base = localTailHeight.longValue();
				// Check if it is valid chain
				if (maxTailInfo.getHeight().compareTo(localTailHeight) > 0 && maxTailInfo.getCheckPointHeight()
						.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0) {
					Log.info("Try to find local chain's which height match with max tail chain");
					byte[] maxProof = null;
					byte[] localProof = null;
					for (; base >= eQcoinSubchainAccount.getCheckPointHeight().longValue(); --base) {
						maxProof = EQCHiveSyncNetworkClient.getEQCHiveRootProof(new ID(base), eqcHiveSyncState.getSp());
						localProof = Util.DB().getEQCHiveRootProof(new ID(base));
						if (Arrays.equals(localProof, maxProof)) {
							Log.info("Current max tail chain's local base height is: " + base);
							isValidChain = true;
							break;
						}
					}
					
					// If is valid max chain and is EQCMinerNetwork need check if the POW is valid
					if(isValidChain && (eqcHiveSyncState.getEQCHive() != null)) {
						EQCHiveRoot maxEQCHiveRoot = null;
						EQCHiveRoot localEQCHiveRoot = null;
						for(long i=base; i<=maxTailInfo.getHeight().longValue(); ++i) {
							maxEQCHiveRoot = EQCHiveSyncNetworkClient.getEQCHiveRoot(new ID(i), eqcHiveSyncState.getSp());
//							localEQCHiveRoot = new EQCHiveRoot(localProof);
							// Here still need verify if the target is valid can calculate the relevant target according to the relevant EQCHiveRoot
							// Due to the time limit so doesn't need check target which is calculated through mining time
//							if(Util.targetBytesToBigInteger(maxProof).compareTo(Util.targetBytesToBigInteger(localEQCHiveRoot.getTarget())) > 0) {
//								//20200514 here need do more job 1. how to determine the target is valid? 2. If only need check this in miner network?
//								Log.info("Due to the time limit of EQcoin the max chain's difficulty should bigger than the shorter chain");
//								isValidChain = false;
//								break;
//							}
							if(!maxEQCHiveRoot.isDifficultyValid()) {
								Log.info("Begin verify if No." + maxEQCHiveRoot.getHeight() + "'s EQCHeader's difficulty is valid");
								if (maxEQCHiveRoot.isDifficultyValid()) {
									Log.info("No." + maxEQCHiveRoot.getHeight() + "'s EQCHeader's difficulty is valid");
								} else {
									Log.info(eqcHiveSyncState.getSp() + " isn't valid chain because No. " + maxEQCHiveRoot.getHeight()
											+ " 's EQCHiveRoot's difficulty is invalid");
									isValidChain = false;
									break;
								}
							}
						}
					}
					
					// Check if need sync EQCHive
					if (isValidChain) {
						if(eqcHiveSyncState.getEQCHive() != null) {
						Log.info("Received broadcast new EQCHive and is valid chain begin set savepoint");
						savepointBroadcastNewEQCHive = Util.DB().getConnection().setSavepoint("Sync to broadcast new EQCHive");
						Log.info("Savepoint: " + savepointBroadcastNewEQCHive);
						}
						Log.info("MaxTail is valid chain begin sync");
						Log.info("Begin recovery relevant global state to base height: " + base);
						// Remove fork EQCHive
						if (base < localTailHeight.longValue()) {
							Log.info("Begin delete EQCHive from " + (base + 1) + " to " + localTailHeight.longValue());
							for(long i=(base+1); i<=localTailHeight.longValue(); ++i) {
								Util.DB().deleteEQCHive(new ID(i));
							}
						} else {
							Log.info("Base " + base + " equal to local tail " + localTailHeight.longValue() + " do nothing");
						}

						if (new ID(base).compareTo(localTailHeight) < 0) {
							Log.info("Base height " + base + " less than local tail height " + localTailHeight.longValue() + " beginning recovery global state and remove snapshot");
							// Recovery relevant global state to base height
							Util.recoveryGlobalStatusTo(new ID(base));
							// Remove relevant snapshot after base height to avoid exists wrong state
							Util.DB().deletePassportSnapshotFrom(new ID(base+1), true);
							Util.DB().deleteLockSnapshotFrom(new ID(base+1), true);
						}
					
						// Remove extra passport and lock here need remove passport and lock after base
						// Passport
						ID originalPassportNumbers = eQcoinSeedRoot.getTotalPassportNumbers();
						EQcoinSeedRoot eQcoinSeedRootBase = Util.DB().getEQcoinSeedRoot(new ID(base));
						// Here throw null pointer will cause savepoint invalid?
						if (eQcoinSeedRootBase.getTotalPassportNumbers().compareTo(originalPassportNumbers) < 0) {
							Log.info("Begin delete extra Passport from "
									+ eQcoinSeedRootBase.getTotalPassportNumbers().getNextID() + " to "
									+ originalPassportNumbers);
							for(long i=eQcoinSeedRootBase.getTotalPassportNumbers().getNextID().longValue(); i<=originalPassportNumbers.longValue(); ++i) {
								Util.DB().deletePassport(new ID(i), Mode.GLOBAL);
							}
						} else {
							Log.info(
									"Base height's TotalPassportNumbers " + eQcoinSeedRootBase.getTotalPassportNumbers()
											+ " equal to local tail " + originalPassportNumbers + " do nothing");
						}
						// Lock
						ID originalLockNumbers = eQcoinSeedRoot.getTotalLockNumbers();
						if (eQcoinSeedRootBase.getTotalLockNumbers().compareTo(originalLockNumbers) < 0) {
							Log.info("Begin delete extra Lock from "
									+ eQcoinSeedRootBase.getTotalLockNumbers().getNextID() + " to "
									+ originalLockNumbers);
							for(long i=eQcoinSeedRootBase.getTotalLockNumbers().getNextID().longValue(); i<=originalLockNumbers.longValue(); ++i) {
								Util.DB().deleteLock(new ID(i), Mode.GLOBAL);
							}
						} else {
							Log.info(
									"Base height's TotalLockNumbers " + eQcoinSeedRootBase.getTotalLockNumbers()
											+ " equal to local tail " + originalLockNumbers + " do nothing");
						}
						Util.DB().saveEQCHiveTailHeight(new ID(base));

						// Begin sync to tail
						EQCHive maxTailHive = null;
						for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
							if(eqcHiveSyncState.getEQCHive() == null) {
								savepointSync = null;
								Log.info("onSync begin set savepoint");
								savepointSync = Util.DB().getConnection().setSavepoint("onSync");
								Log.info("onSync end set savepoint: " + savepointSync);
							}
							Log.info("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp());
							maxTailHive = EQCHiveSyncNetworkClient.getEQCHive(new ID(i), eqcHiveSyncState.getSp());
							if (maxTailHive == null) {
								Log.info("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp() + " but which is null");
								throw new MaxTailValidException("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp() + " but which is null");
							}
							changeLog = new ChangeLog(new ID(i), new Filter(Mode.VALID));
							maxTailHive.setChangeLog(changeLog);
							if (maxTailHive.isValid()) {
								Log.info("Verify No." + i + " hive passed");
								try {
									if(eqcHiveSyncState.getEQCHive() == null) {
										Log.info("onSync just commit it every EQCHive");
										changeLog.updateGlobalState(maxTailHive, savepointSync);
									}
									else {
										changeLog.updateGlobalState(maxTailHive, null);
									}
								} catch (Exception e) {
									Log.Error("During update No." + i + " hive's global state error occur: " + e.getMessage());
									throw new MaxTailValidException("During update No." + i + " hive's global state error occur: " + e.getMessage());
								}
								Log.info("Current new tail: " + Util.DB().getEQCHiveTailHeight());
							} else {
								Log.info("No." + i + " hive is invalid");
								throw new MaxTailValidException("No." + i + " hive is invalid");
							}
						}
						
						if (isRunning()) {
							// Remove extra new EQCHive message if any because of when sync finished will
							// begin find which will retrieve the latest status of new EQCHive from the
							// EQCMinerNetwork
							pendingMessage.clear();
						}
						Log.info("Successful sync to current tail just begin commit");
						Util.DB().getConnection().commit();
						Log.info("End commit");
						if(eqcHiveSyncState.getEQCHive() != null) {
							Log.info("Changed to max chain just stop mining");
							MinerService.getInstance().stopMining();
						}
						Log.info("Goto find to check if reach the max tail");
						offerState(new EQCServiceState(State.FIND));
					} else {
						Log.info("MaxTail is invliad chain have to discard it");
					}
				} else {
					Log.info("MaxTail isn't bigger than local tail just discard it");
				}
				Log.info("End synchronized (EQCService.class)");
			}
		} catch (Exception e) {
			Log.Error(name + e.getMessage());
			if(e instanceof IOException) {
				Util.updateDisconnectSPStatus(eqcHiveSyncState.getSp());
			}
			if(eqcHiveSyncState.getEQCHive() != null) {
				if(savepointBroadcastNewEQCHive != null) {
					try {
						Log.info("Begin rollback, Savepoint: " + savepointBroadcastNewEQCHive);
						Util.DB().getConnection().rollback(savepointBroadcastNewEQCHive);
						Log.info("Rollback successful");
					} catch (Exception e1) {
						Log.Error(e1.getMessage());
					}
				}
				if (MinerService.getInstance().isRunning() && MinerService.getInstance().isMining.get() && MinerService.getInstance().isPausing.get()) {
					Log.info("MinerService is running and mining and paused now begin resume pause");
					MinerService.getInstance().resumePause();
				} else {
					Log.info("MinerService is running: " + MinerService.getInstance().isRunning() + " and is mining: " + MinerService.getInstance().isMining.get() + " and is paused: " + MinerService.getInstance().isPausing.get());
				}
			}
			else {
				if(savepointSync != null) {
					try {
						Log.info("Begin rollback, Savepoint: " + savepointSync);
						Util.DB().getConnection().rollback(savepointSync);
						Log.info("Rollback successful");
					} catch (Exception e1) {
						Log.Error(e1.getMessage());
					}
				}
				// EQCHiveSyncNetwork just sleep then sync again
				try {
					sleeping(Util.getCurrentEQCHiveInterval().longValue());
				} catch (Exception e1) {
					Log.Error(e1.getMessage());
				}
			}
		}
	}

	private void onMiner(EQCServiceState state) {
		Log.info("onMiner");
		if (!MinerService.getInstance().isRunning()) {
			Log.info("Begin start new MinerService");
			MinerService.getInstance().start();
		} else {
			try {
				if(MinerService.getInstance().isMining.get()) {
					// Here need add synchronized lock to double avoid conflict with MinerService
					synchronized (EQCService.class) {
						Log.info("Begin synchronized (EQCService.class)");
						if (!MinerService.getInstance().getNewEQCHiveHeight().isNextID(Util.DB().getEQCHiveTailHeight())) {
							Log.info("Changed to new mining base begin stop current mining progress");
							MinerService.getInstance().stopMining();
							Log.info("Need restart just beginning new mining progress");
							MinerService.getInstance().startMining();
						}
						Log.info("End synchronized (EQCService.class)");
					}
					
					if (MinerService.getInstance().isPausing.get()) {
						Log.info("Still mining in the tail and Miner service was paused just resume it");
						MinerService.getInstance().resumePause();
					} else {
						Log.info("Still mining in the tail and Miner service is running now doesn't need do anything");
					}
				}
				else {
					Log.info("Miner service is running now but doesn't mining just start mining");
					MinerService.getInstance().startMining();
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
		offerState(new EQCServiceState(State.FIND));
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
			Log.info("Begin sync sp list");
			Util.syncSPList();
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
	}

	/**
	 * @param sp_mode the mode to set
	 */
	public EQCServiceProvider setSp(SP sp) {
		this.sp = sp;
		return this;
	}
	
	public class MaxTailValidException extends Exception {
		private static final long serialVersionUID = -421091230472072768L;
		public MaxTailValidException(String message) {
			super(message);
		}
	}
	
}
