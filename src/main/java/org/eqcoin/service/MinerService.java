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
package org.eqcoin.service;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.changelog.Filter;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.passport.EQcoinRootPassport;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.rpc.NewEQCHive;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Oct 12, 2018
 * @email 10509759@qq.com
 */
public final class MinerService extends EQCService {
	private static MinerService instance;
	private static ChangeLog changeLog;
	private ID newEQCHiveHeight;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		super.start();
		worker.setPriority(Thread.MAX_PRIORITY);
		startMining();
		Log.info(name + "started");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		isRunning.set(false);
		resumeHalt();
		super.stop();
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
			if (!isRunning.get() || !isMining.get()) {
				Log.info("Exit from prepare minering");
				break;
			}
			try {
				miningOneEQCHive();
			} catch (Exception e) {
				Log.Error("Due to error occur have to exit mining: " + e.getMessage());
				break;
			}
		}
		Log.info("End of mining");
	}

	public void miningOneEQCHive() throws Exception {
		ID tailHeight = null;
		EQCHive newEQCHive = null;
		Savepoint savepoint = null;

		// Begin making new EQCHive
		tailHeight = Util.DB().getEQCHiveTailHeight();
		newEQCHiveHeight = tailHeight.getNextID();
		Log.info("Begin mining new EQCHive local tail: " + tailHeight + " work thread state: " + worker.getState());

		changeLog = new ChangeLog(newEQCHiveHeight, new Filter(Mode.MINING));
		Util.DB().getConnection().commit();
		savepoint = Util.DB().getConnection().setSavepoint();

		newEQCHive = new EQCHive(Util.DB().getEQCHiveRootProof(tailHeight), newEQCHiveHeight, changeLog);
		newEQCHive.plantingEQcoinSeed();

		Log.info("New EQCHive planting successful height: " + newEQCHiveHeight);
//		Log.info(newEQCBlock.toString());
		Log.info("Size: " + newEQCHive.getBytes().length);
		Log.info("New EQCHive have " + newEQCHive.getEQcoinSeed().getNewTransactionList().size() + " new transactions");
		Log.info("New EQCHive have " + changeLog.getForbiddenLockList().size() + " forbidden locks");

		// Use this only for debug when after test will remove this
		try {
			EQCHive eqcHive1 = new EQCHive(newEQCHive.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}

		// Beginning POW
		while (!newEQCHive.getEQCHiveRoot().isDifficultyValid()) {
			onPause("minering");
			if (!isRunning.get() || !isMining.get()) {
				Log.info("Exit from mining");
				return;
			}
			newEQCHive.getEQCHiveRoot().setNonce(newEQCHive.getEQCHiveRoot().getNonce().getNextID());
			if (newEQCHive.getEQCHiveRoot().getNonce().mod(ID.TWO).equals(ID.ZERO) && isRunning.get() && isMining.get()) {
				halt();
			}
		}

		// Mining successful
		synchronized (EQCService.class) {
			// Here add lock to avoid conflict with handle new received EQCHive in
			// EQCServiceProvider
			Log.info("Begin synchronized (EQCService.class)");
//						onPause("verify new EQCHive"); // Here can't pause which will cause deadlock
			if (!isRunning.get() || !isMining.get()) {
				// Here must check if it has been stopped because of already received valid new
				// EQCHive during mining
				Log.info("The POW was finished but because of already received new EQCHive so 绅士的 exit");
				return;
			}

			Log.info("EQCHive No." + newEQCHive.getHeight().longValue() + " Find use: "
					+ (System.currentTimeMillis() - newEQCHive.getEQCHiveRoot().getTimestamp().longValue()) + " ms");

			// When finished debug will remove this to save space
			Log.info("\n" + newEQCHive.getEQCHiveRoot().toString());

			// Check if current local tail is the mining base in case which has been changed
			// by EQCServiceProvider
			if (newEQCHiveHeight.isNextID(Util.DB().getEQCHiveTailHeight())) { // Here must retrieve the lively tail height
				Log.info("Still on the tail just broadcast it");
				try {
					// Send new EQCHive to EQCMinerNetwork and EQCHiveSyncNetwork if at here exists
					// any exception shouldn't block the mining
					NewEQCHiveState newEQCHiveState = new NewEQCHiveState();
					newEQCHiveState.getNewEQCHive().setEQCHive(newEQCHive);
					BroadcastNewEQCHiveService.getInstance().offerNewEQCHiveState(newEQCHiveState);
				} catch (Exception e) {
					Log.Error(e.getMessage());
				}
				
				changeLog.updateGlobalState(newEQCHive, savepoint);
				Log.info("EQCHive No." + newEQCHive.getHeight().longValue() + " saved successful.");
				
			} else {
				Log.Error("Current mining height is: " + newEQCHiveHeight + " but local tail height changed to: "
						+ Util.DB().getEQCHiveTailHeight() + " so have to discard this POW");
			}
			Log.info("End synchronized (EQCService.class)");
		}

	}

	private void halt() {
		synchronized (name) {
			try {
				name.wait(1000);//Util.getCurrentEQCHiveInterval().divide(BigInteger.TEN).intValue());// 1000);
			} catch (Exception e) {
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
	 * @return the newEQCHiveHeight
	 */
	public ID getNewEQCHiveHeight() {
		return newEQCHiveHeight;
	}

}
