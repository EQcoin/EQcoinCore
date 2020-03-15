/**
 * EQZIPWallet - EQchains Federation's EQZIPWallet
 * @copyright 2018-present EQCOIN Foundation All rights reserved...
 * Copyright of all works released by EQCOIN Foundation or jointly released by 
 * EQCOIN Foundation with cooperative partners are owned by EQCOIN Foundation 
 * and entitled to protection available from copyright law by country as well as 
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQCOIN Foundation reserves all rights to 
 * take any legal action and pursue any right or remedy available under applicable 
 * law.
 * https://www.eqzip.com
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
/**
* EQchains core - EQchains Federation's EQchains core library
* @copyright 2018-present EQchains Federation All rights reserved...
* Copyright of all works released by EQchains Federation or jointly released by
* EQchains Federation with cooperative partners are owned by EQchains Federation
* and entitled to protection available from copyright law by country as well as
* international conventions.
* Attribution — You must give appropriate credit, provide a link to the license.
* Non Commercial — You may not use the material for commercial purposes.
* No Derivatives — If you remix, transform, or build upon the material, you may
* not distribute the modified material.
* For any use of above stated content of copyright beyond the scope of fair use
* or without prior written permission, EQchains Federation reserves all rights to
* take any legal action and pursue any right or remedy available under applicable
* law.
* https://www.eqchains.com
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
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.passport.EQcoinSeedPassport;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.NewHive;
import com.eqcoin.service.state.EQCServiceState;
import com.eqcoin.service.state.NewHiveState;
import com.eqcoin.service.state.EQCServiceState.State;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Oct 12, 2018
 * @email 10509759@qq.com
 */
public final class MinerService extends EQCService {
	private static MinerService instance;
	private static ChangeLog changeLog;
	private ID newHiveHeight;
	private AtomicBoolean isMining;
	
	private MinerService() {
		super();
		isMining = new AtomicBoolean(false);
	}

	public static MinerService getInstance() {
		if (instance == null) {
			synchronized (MinerService.class) {
				if (instance == null) {
					instance = new MinerService();
				}
			}
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		super.start();
		worker.setPriority(Thread.MIN_PRIORITY);
		startMining();
		Log.info(name + "started");
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		isRunning.set(false);
		resumeHalt();
		super.stop();
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
		case MINING:
			this.state.set(State.MINING);
			isMining.set(true);
			onMinering(state);
			break;
		default:
			break;
		}
	}
	
	public void stopMining() {
		Log.info(name + "begin stop mining progress");
		isMining.set(false);
		if (isPausing.get()) {
			resumePause();
		}
		resumeHalt();
	}
	
	public void startMining() {
		offerState(new EQCServiceState(State.MINING));
	}
	
	private void onMinering(EQCServiceState state) {
		Log.info("Begin minering...");
		this.state.set(State.MINER);
		while (isRunning.get() && isMining.get()) {
 			onPause("prepare minering");
			if(!isRunning.get() || !isMining.get()) {
				Log.info("Exit from prepare minering");
				break;
			}
			// Get current EQCBlock's tail
			ID blockTailHeight;
			try {
				blockTailHeight = Util.DB().getEQCBlockTailHeight();
				/////////////////////////////////////////////////
//				if(blockTailHeight.compareTo(ID.valueOf(2)) == 0) {
//					break;
//				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(e.getMessage());
				break;
			}
			EQCHive blockTail;
			try {
				Log.info("Begin mining new hive local blockTailHeight: " + blockTailHeight + " work thread state: " + worker.getState());
				blockTail = Util.DB().getEQCHive(blockTailHeight, false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.Error(e.getMessage());
				break;
			}

			// Begin making new EQCBlock
			newHiveHeight = blockTailHeight.getNextID();
			// If create AccountsMerkleTree just create it
			try {
				changeLog = new ChangeLog(newHiveHeight, new Filter(Mode.MINING));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.Error(e1.getMessage());
				break;
			}
			EQCHive newEQCHive;
			try {
				newEQCHive = new EQCHive(newHiveHeight, blockTail.getEqcHeader().getHash());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				Log.Error(e1.getMessage());
				break;
			}

			// Initial new EQCBlock
			try {
				// Build Transactions and initial Root
				newEQCHive.plantingEQCHive(changeLog);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("Warning during Build Transactions error occur have to exit need check to find the reason: "
						+ e.getMessage());
				break;
			}
			
			Log.info("New hive height: " + newHiveHeight);
//			Log.info(newEQCBlock.toString());
			Log.info("Size: " + newEQCHive.getBytes().length);
			Log.info("EQcoin new transaction numbers: " + newEQCHive.geteQcoinSeed().getNewTransactionList().size());
			Log.info("EQcoin new passport numbers: " + newEQCHive.geteQcoinSeed().getNewHelixList().size());
			Log.info("EQcoin new compressed publickey numbers: " + newEQCHive.geteQcoinSeed().getNewCompressedPublickeyList().size());
			try {
				EQCHive eqcHive = new EQCHive(newEQCHive.getBytes(), false);
			} catch (Exception e) {
				e.printStackTrace();
				Log.Error(e.getMessage());
			}
			
			// Send locked transactions to EQC network haven't implement, doesn't implement in MVP phase...
			
			// Beginning calculate new EQCBlock's hash
			BigInteger hash;
			ID nonce = ID.ZERO;
			BigInteger difficulty = Util.targetBytesToBigInteger(newEQCHive.getEqcHeader().getTarget());
			while (true) {
				onPause("minering");
				if(!isRunning.get() || !isMining.get()) {
					Log.info("Exit from mining");
					break;
				}
				newEQCHive.getEqcHeader().setNonce(nonce);
				hash = new BigInteger(1, newEQCHive.getHash());
				if (hash.compareTo(difficulty) <= 0) { 
					try {
						synchronized (EQCService.class) {
							// Here add synchronized to avoid conflict with Sync block service handle new received block
							Log.info("Begin synchronized (EQCService.class)");
//							onPause("verify new block"); // Here can't pause which will cause deadlock
							if(!isRunning.get() || !isMining.get()) {
								// Here need check if it has been stopped
								Log.info("Exit from verify new block");
								break;
							}
							
							Log.info(Util.getHexString(newEQCHive.getHash()));
							Log.info("EQC Block No." + newEQCHive.getHeight().longValue() + " Find use: "
									+ (System.currentTimeMillis() - newEQCHive.getEqcHeader().getTimestamp().longValue())
									+ " ms, details:");

							Log.info(newEQCHive.getEqcHeader().toString());
//							Log.info(newEQCBlock.getRoot().toString());
							
//							try {
//								PendingNewBlockService.getInstance().pause();
//							}
//							catch (Exception e) {
//								Log.Error(e.getMessage());
//							}
							// Check if current local tail is the mining base in case which has been changed by SyncBlockService
							if (newHiveHeight.isNextID(Util.DB().getEQCBlockTailHeight())) {
								Log.info("Still on the tail just save it");
								Util.DB().saveEQCHive(newEQCHive);
								changeLog.updateGlobalState();
								Util.DB().saveEQCBlockTailHeight(newEQCHive.getHeight());
								try {
									// Send new block to EQC Miner network
									NewHiveState newBlockState = new NewHiveState(State.BROADCASTNEWHIVE);
									EQcoinSeedPassport eQcoinSubchainAccount = (EQcoinSeedPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
									NewHive newBlock = new NewHive();
									newBlock.setEqcHive(newEQCHive);
									newBlock.setCheckPointHeight(eQcoinSubchainAccount.getCheckPointHeight());
									newBlockState.setNewBlock(newBlock);
									BroadcastNewHiveService.getInstance().offerNewBlockState(newBlockState);
								}
								catch (Exception e) {
									Log.Error(e.getMessage());
								}
								EQCBlockChainH2.getInstance().deleteTransactionsInPool(newEQCHive);
//								// Here exists one bug before delete the old history snapshot need recovery the checkpoint's height's status first
//								EQcoinSubchainAccount eQcoinSubchainAccount = (EQcoinSubchainAccount) Util.DB()
//										.getAccount(ID.ONE);
//								EQCBlockChainH2.getInstance()
//										.deleteAccountSnapshotFrom(eQcoinSubchainAccount.getCheckPointHeight(), false);
//								try {
//									PendingNewBlockService.getInstance().resumePause();
//								}
//								catch (Exception e) {
//									Log.Error(e.getMessage());
//								}
							}
							else {
								Log.Error("Current mining height is: " + newHiveHeight + " but local tail height changed to: " + Util.DB().getEQCBlockTailHeight() + 
										" so have to discard this block");
							}
							Log.info("End synchronized (EQCService.class)");
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
					break;
				}
				nonce = nonce.getNextID();
				if(nonce.mod(ID.TWO).equals(ID.ZERO) && isRunning.get() && isMining.get()) {
					halt();
				}
			}
		}
		Log.info("End of mining");
	}

	private void halt() {
		synchronized (name) {
			try {
				name.wait(1000);
			} catch (InterruptedException e) {
				Log.Error(e.getMessage());
			}
		}
	}
	
	private void resumeHalt() {
		synchronized (name) {
			name.notify();
		}
	}
	
	/**
	 * @return the newBlockHeight
	 */
	public ID getNewBlockHeight() {
		return newHiveHeight;
	}
	
}
