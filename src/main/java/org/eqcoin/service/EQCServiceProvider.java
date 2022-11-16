/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
* Wandering Earth Corporation reserves any and all current and future rights,
* titles and interests in any and all intellectual property rights of Wandering Earth
* Corporation, including but not limited to discoveries, ideas, marks, concepts,
* methods, formulas, processes, codes, software, inventions, compositions, techniques,
* information and data, whether or not protectable in trademark, copyrightable
* or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.service;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCHiveSyncNetworkClient;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TailInfo;
import org.eqcoin.rpc.service.avro.EQCHiveSyncNetworkService;
import org.eqcoin.rpc.service.avro.EQCMinerNetworkService;
import org.eqcoin.rpc.service.avro.EQCTransactionNetworkService;
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
	public class MaxTailValidException extends Exception {
		final long serialVersionUID = -421091230472072768L;
		public MaxTailValidException(final String message) {
			super(message);
		}
	}

	private static EQCServiceProvider instance;

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

	GlobalState globalState;

	private SP sp;

	private EQCServiceProvider() {
		super();
		try {
			globalState = new GlobalStateH2();
		} catch (ClassNotFoundException | SQLException e) {
			Log.Error(e.getMessage());
		}
	}

	private void onBootUp(final EQCServiceState state) {
		// 2021-03-02 Here need start up Ignite service first
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
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.
	 * EQCServiceState)
	 */
	@Override
	protected void onDefault(final EQCServiceState state) {
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

	private void onFind(final EQCServiceState state) {
		SPList minerList = null;
		SP maxTail = null;
		TailInfo minerTailInfo = null;
		TailInfo maxTailInfo = null;
		boolean isMaxTail = true;

		try {
			// Before here already do syncMinerList in Util.init during every time EQcoin core startup
			minerList = Util.MC().getSPList(SP_MODE.getFlag(SP_MODE.EQCMINERNETWORK));
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
			final Vector<TailInfo> minerTailList = new Vector<>();
			for (final SP sp : minerList.getSPList()) {
				try {
					// Doesn't need include current Node's ip
					if (!sp.equals(Util.LOCAL_SP)) {
						Log.info("Try to get miner tail: " + sp);
						minerTailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(sp);
					} else {
						Log.info("Current SP " + sp + " is the same as local SP " + Util.LOCAL_SP + " just ignore it");
						continue;
					}
				} catch (final Exception e) {
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
					sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue());
					return;
				}
			}

			final Comparator<TailInfo> reverseComparator = Collections.reverseOrder();
			Collections.sort(minerTailList, reverseComparator);
			// Retrieve the max Hive TailInfo
			Log.info("minerTailListSize: " + minerTailList.size());
			Log.info("MinerTailList: " + minerTailList.toString());
			maxTailInfo = minerTailList.get(0);
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			Log.info("LocalTail: " + globalState.getEQCHiveTailHeight());
			final EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
			if (maxTailInfo.getCheckPointHeight().compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0
					&& maxTailInfo.getHeight().compareTo(globalState.getEQCHiveTailHeight()) > 0) {
				isMaxTail = false;
				final SPList minerIpList = new SPList();
				for (final TailInfo tailInfo2 : minerTailList) {
					if (tailInfo2.equals(minerTailList.get(0))) {
						minerIpList.addSP(tailInfo2.getSp());
					}
				}
				maxTail = EQCMinerNetworkClient.getFastestServer(minerIpList);
				if (maxTail == null) {
					Log.info("MaxTail can't connect now just sleep then try find&sync again");
					sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue());
					return;
				}
			}

			if (!isMaxTail) {
				Log.info("Find max tail begin sync to: " + maxTail);
				// Begin sync to MaxTail
				final EQCHiveSyncState eqcHiveSyncState = new EQCHiveSyncState();
				eqcHiveSyncState.setSp(maxTail);
				offerState(eqcHiveSyncState);
			} else {
				if (sp.isEQCMinerNetwork()) {
					Log.info("Miner node just begin mining");
					offerState(new EQCServiceState(State.MINER));
				} else {
					// Full node just sleep then try to find&sync again
					Log.info("Full node just sleep then try to find&sync again");
					sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue());
				}
			}
		} catch (final Exception e) {
			Log.Error(name + e.getMessage());
			try {
				sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue() / 60);
			} catch (final Exception e1) {
				Log.Error(e1.getMessage());
			}
		}
	}

	private void onMiner(final EQCServiceState state) {
		Log.info("onMiner");
		if (!PlantService.getInstance().isRunning()) {
			Log.info("Begin start new MinerService");
			PlantService.getInstance().start();
		} else {
			try {
				if(PlantService.getInstance().isMining.get()) {
					// Here need add synchronized lock to double avoid conflict with MinerService
					synchronized (EQCService.class) {
						Log.info("Begin synchronized (EQCService.class)");
						if (!PlantService.getInstance().getNewEQCHiveHeight().isNextID(globalState.getEQCHiveTailHeight())) {
							Log.info("Changed to new mining base begin stop current mining progress");
							PlantService.getInstance().stopMining();
							Log.info("Need restart just beginning new mining progress");
							PlantService.getInstance().startMining();
						}
						Log.info("End synchronized (EQCService.class)");
					}

					if (PlantService.getInstance().isPausing.get()) {
						Log.info("Still mining in the tail and Miner service was paused just resume it");
						PlantService.getInstance().resumePause();
					} else {
						Log.info("Still mining in the tail and Miner service is running now doesn't need do anything");
					}
				}
				else {
					Log.info("Miner service is running now but doesn't mining just start mining");
					PlantService.getInstance().startMining();
				}
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
				try {
					sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue());
				} catch (final Exception e1) {
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
	protected void onSleep(final SleepState state) {
		Log.info("Sleep finished begin find");
		offerState(new EQCServiceState(State.FIND));
	}

	private void onSync(final EQCServiceState state) {
		final EQCHiveSyncState eqcHiveSyncState = (EQCHiveSyncState) state;
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
					final ID localTailHeight = globalState.getEQCHiveTailHeight();
					if (eqcHiveSyncState.getEQCHive().getRoot().getHeight().compareTo(localTailHeight) <= 0) {
						Log.info("New EQCHive's height: " + eqcHiveSyncState.getEQCHive().getRoot().getHeight()
								+ " not bigger than local tail: " + localTailHeight
								+ " just discard it");
						return;
					} else {
						final byte[] localTailRootProof = globalState.getEQCHiveRootProof(localTailHeight);
						if (eqcHiveSyncState.getEQCHive().getRoot().getHeight().isNextID(localTailHeight)) {
							if (Arrays.equals(eqcHiveSyncState.getEQCHive().getRoot().getPreProof(),
									localTailRootProof)) {
								Log.info("New EQCHive is current tail's next hive just begin verify it");
								eqcHiveSyncState.getEQCHive().setGlobalState(globalState);
								final Savepoint savepointNext = globalState.setSavepoint();
								if (eqcHiveSyncState.getEQCHive().isValid()) {
									try {
										globalState.updateGlobalState(eqcHiveSyncState.getEQCHive(), savepointNext, GlobalState.VALID_NEXT_HIVE);
										Log.info("New EQCHive is valid and saved successful");
										Log.info("Miner service isRunning: " + PlantService.getInstance().isRunning + " isPausing: " + PlantService.getInstance().isPausing + " isMining: " + PlantService.getInstance().isMining());
										if(PlantService.getInstance().isMining()) {
											PlantService.getInstance().stopMining();
											PlantService.getInstance().startMining();
										}
									} catch (final Exception e) {
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
							Log.info("MaxTail's height: " + eqcHiveSyncState.getEQCHive().getRoot().getHeight()
									+ " bigger than local tail: " + localTailHeight
									+ " begin sync to it");
						}
					}
					Log.info("End synchronized (EQCService.class)");
				}
			} else {
				Log.info("Received MaxTail from Find just begin sync");
			}

			final TailInfo maxTailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(eqcHiveSyncState.getSp());
			Log.info("MaxTail: " + maxTailInfo.getHeight());
			ID localTailHeight = null;
			synchronized (EQCService.class) {
				Log.info("Begin synchronized (EQCService.class)");
				if(eqcHiveSyncState.getEQCHive() != null) {
					if (PlantService.getInstance().isRunning() && PlantService.getInstance().isMining.get() && !PlantService.getInstance().isPausing.get()) {
						Log.info("MinerService is running and mining just begin pause it");
						PlantService.getInstance().pause();
						Log.info("MinerService paused now");
					} else {
						Log.info("MinerService isRunning:" + PlantService.getInstance().isRunning() + " and isMining: " + PlantService.getInstance().isMining.get() + " and isPausing: " + PlantService.getInstance().isPausing.get() + " now has nothing to do");
					}
				}

				localTailHeight = globalState.getEQCHiveTailHeight();
				Log.info("LocalTailHeight: " + localTailHeight);
				// 20200514 here need analysis if need retrieve the passport from current mining table if exists?
				final EQcoinRootPassport eQcoinSubchainAccount = (EQcoinRootPassport) globalState.getPassport(ID.ZERO);
				final EQCHiveRoot eQcoinSeedRoot = globalState.getEQCHiveRoot(localTailHeight);
				long base = localTailHeight.longValue();
				// Check if it is valid chain
				if (maxTailInfo.getHeight().compareTo(localTailHeight) > 0 && maxTailInfo.getCheckPointHeight()
						.compareTo(eQcoinSubchainAccount.getCheckPointHeight()) >= 0) {
					Log.info("Try to find local chain's which height match with max tail chain");
					byte[] maxProof = null;
					byte[] localProof = null;
					for (; base >= eQcoinSubchainAccount.getCheckPointHeight().longValue(); --base) {
						maxProof = EQCHiveSyncNetworkClient.getEQCHiveRootProof(new ID(base), eqcHiveSyncState.getSp());
						localProof = globalState.getEQCHiveRootProof(new ID(base));
						if (Arrays.equals(localProof, maxProof)) {
							Log.info("Current max tail chain's local base height is: " + base);
							isValidChain = true;
							break;
						}
					}

					// If is valid max chain and is EQCMinerNetwork need check if the POW is valid
					if(isValidChain && (eqcHiveSyncState.getEQCHive() != null)) {
						EQCHiveRoot maxEQCHiveRoot = null;
						final EQCHiveRoot localEQCHiveRoot = null;
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
							savepointBroadcastNewEQCHive = globalState.setSavepoint();
							Log.info("Savepoint: " + savepointBroadcastNewEQCHive);
						}
						Log.info("MaxTail is valid chain begin sync");
						Log.info("Begin recovery relevant global state to base height: " + base);
						// Remove fork EQCHive
						if (base < localTailHeight.longValue()) {
							Log.info("Begin delete EQCHive from " + (base + 1) + " to " + localTailHeight.longValue());
							for(long i=(base+1); i<=localTailHeight.longValue(); ++i) {
								globalState.deleteEQCHive(new ID(i));
							}
						} else {
							Log.info("Base " + base + " equal to local tail " + localTailHeight.longValue() + " do nothing");
						}

						if (new ID(base).compareTo(localTailHeight) < 0) {
							Log.info("Base height " + base + " less than local tail height " + localTailHeight.longValue() + " beginning recovery global state and remove snapshot");
							// Recovery relevant global state to base height
							Util.recoveryGlobalStateTo(new ID(base), globalState);
							// Remove relevant snapshot after base height to avoid exists wrong state
							globalState.deletePassportSnapshotFrom(new ID(base+1), true);
							globalState.deleteLockMateSnapshotFrom(new ID(base+1), true);
						}

						// Remove extra passport and lock here need remove passport and lock after base
						// Passport
						final ID originalPassportNumbers = eQcoinSeedRoot.getTotalPassportNumbers();
						final EQCHiveRoot eqcHiveRootBase = globalState.getEQCHiveRoot(new ID(base));
						// Here throw null pointer will cause savepoint invalid?
						if (eqcHiveRootBase.getTotalPassportNumbers().compareTo(originalPassportNumbers) < 0) {
							Log.info("Begin delete extra Passport from "
									+ eqcHiveRootBase.getTotalPassportNumbers().getNextID() + " to "
									+ originalPassportNumbers);
							for(long i=eqcHiveRootBase.getTotalPassportNumbers().getNextID().longValue(); i<=originalPassportNumbers.longValue(); ++i) {
								globalState.deletePassport(new ID(i));
							}
						} else {
							Log.info(
									"Base height's TotalPassportNumbers " + eqcHiveRootBase.getTotalPassportNumbers()
									+ " equal to local tail " + originalPassportNumbers + " do nothing");
						}
						// Lock
						final ID originalLockNumbers = eQcoinSeedRoot.getTotalLockMateNumbers();
						if (eqcHiveRootBase.getTotalLockMateNumbers().compareTo(originalLockNumbers) < 0) {
							Log.info("Begin delete extra Lock from "
									+ eqcHiveRootBase.getTotalLockMateNumbers().getNextID() + " to "
									+ originalLockNumbers);
							for(long i=eqcHiveRootBase.getTotalLockMateNumbers().getNextID().longValue(); i<=originalLockNumbers.longValue(); ++i) {
								globalState.deleteLockMate(new ID(i));
							}
						} else {
							Log.info(
									"Base height's TotalLockNumbers " + eqcHiveRootBase.getTotalLockMateNumbers()
									+ " equal to local tail " + originalLockNumbers + " do nothing");
						}
						globalState.saveEQCHiveTailHeight(new ID(base));

						// Begin sync to tail
						EQCHive maxTailHive = null;
						for (long i = base + 1; i <= maxTailInfo.getHeight().longValue(); ++i) {
							if(eqcHiveSyncState.getEQCHive() == null) {
								savepointSync = null;
								Log.info("onSync begin set savepoint");
								savepointSync = globalState.setSavepoint();
								Log.info("onSync end set savepoint: " + savepointSync);
							}
							Log.info("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp());
							maxTailHive = EQCHiveSyncNetworkClient.getEQCHive(new ID(i), eqcHiveSyncState.getSp());
							if (maxTailHive == null) {
								Log.info("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp() + " but which is null");
								throw new MaxTailValidException("Begin sync No. " + i + " EQCHive from " + eqcHiveSyncState.getSp() + " but which is null");
							}
							//							changeLog = new ChangeLog(new ID(i), new Filter(Mode.VALID));
							maxTailHive.setGlobalState(globalState);
							if (maxTailHive.isValid()) {
								Log.info("Verify No." + i + " hive passed");
								try {
									if(eqcHiveSyncState.getEQCHive() == null) {
										Log.info("onSync just commit it every EQCHive");
										globalState.updateGlobalState(maxTailHive, savepointSync, GlobalState.SYNC_MAX_TAIL);
									}
									else {
										globalState.updateGlobalState(maxTailHive, null, GlobalState.VALID_NEW_TAIL);
									}
								} catch (final Exception e) {
									Log.Error("During update No." + i + " hive's global state error occur: " + e.getMessage());
									throw new MaxTailValidException("During update No." + i + " hive's global state error occur: " + e.getMessage());
								}
								Log.info("Current new tail: " + globalState.getEQCHiveTailHeight());
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
						if(eqcHiveSyncState.getEQCHive() != null) {
							Log.info("Successful valid to current new tail just begin commit");
							globalState.commit(GlobalState.VALID_NEW_TAIL);
							Log.info("End commit");
							Log.info("Changed to max chain just stop mining");
							PlantService.getInstance().stopMining();
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
		} catch (final Exception e) {
			Log.Error(name + e.getMessage());
			if(e instanceof IOException) {
				Util.updateDisconnectSPStatus(eqcHiveSyncState.getSp());
			}
			if(eqcHiveSyncState.getEQCHive() != null) {
				if(savepointBroadcastNewEQCHive != null) {
					try {
						Log.info("Begin rollback, Savepoint: " + savepointBroadcastNewEQCHive);
						globalState.rollback(savepointBroadcastNewEQCHive);
						Log.info("Rollback successful");
					} catch (final Exception e1) {
						Log.Error(e1.getMessage());
					}
				}
				if (PlantService.getInstance().isRunning() && PlantService.getInstance().isMining.get() && PlantService.getInstance().isPausing.get()) {
					Log.info("MinerService is running and mining and paused now begin resume pause");
					PlantService.getInstance().resumePause();
				} else {
					Log.info("MinerService is running: " + PlantService.getInstance().isRunning() + " and is mining: " + PlantService.getInstance().isMining.get() + " and is paused: " + PlantService.getInstance().isPausing.get());
				}
			}
			else {
				if(savepointSync != null) {
					try {
						Log.info("Begin rollback, Savepoint: " + savepointSync);
						globalState.rollback(savepointSync);
						Log.info("Rollback successful");
					} catch (final Exception e1) {
						Log.Error(e1.getMessage());
					}
				}
				// EQCHiveSyncNetwork just sleep then sync again
				try {
					sleeping(Util.getCurrentEQCHiveInterval(globalState).longValue());
				} catch (final Exception e1) {
					Log.Error(e1.getMessage());
				}
			}
		}
		finally {
			if(savepointBroadcastNewEQCHive != null) {
				try {
					globalState.releaseSavepoint(savepointBroadcastNewEQCHive);
				} catch (final Exception e) {
					Log.Error(e.getMessage());
				}
			}
			if(savepointSync != null) {
				try {
					globalState.releaseSavepoint(savepointSync);
				} catch (final Exception e) {
					Log.Error(e.getMessage());
				}
			}
		}
	}

	/**
	 * @param sp_mode the mode to set
	 */
	public EQCServiceProvider setSp(final SP sp) {
		this.sp = sp;
		return this;
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
		if(globalState != null) {
			try {
				globalState.close();
			} catch (final Exception e) {
				Log.Error(e.getMessage());
			}
			globalState = null;
		}
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

}
