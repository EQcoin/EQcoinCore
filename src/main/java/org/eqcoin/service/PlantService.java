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
 * Wandering Earth Corporation retains all current and future right, title and interest
 * in all of Wandering Earth Corporation’s intellectual property, including, without
 * limitation, inventions, ideas, concepts, code, discoveries, processes, marks,
 * methods, software, compositions, formulae, techniques, information and data,
 * whether or not patentable, copyrightable or protectable in trademark, and
 * any trademarks, copyright or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
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

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Oct 12, 2018
 * @email 10509759@qq.com
 */
public final class PlantService extends EQCService {
	public static PlantService getInstance() {
		if (instance == null) {
			synchronized (PlantService.class) {
				if (instance == null) {
					instance = new PlantService();
				}
			}
		}
		return instance;
	}
	private static PlantService instance;
	GlobalState globalState;
	private ID newEQCHiveHeight;

	protected AtomicBoolean isMining;

	private PlantService() {
		super();
		isMining = new AtomicBoolean(false);
		try {
			globalState = new GlobalStateH2();
		} catch (ClassNotFoundException | SQLException e) {
			Log.Error(e.getMessage());
		}
	}

	/**
	 * @return the newEQCHiveHeight
	 */
	public ID getNewEQCHiveHeight() {
		return newEQCHiveHeight;
	}

	private void halt() {
		synchronized (name) {
			try {
				name.wait(1000);//Util.getCurrentEQCHiveInterval().divide(BigInteger.TEN).intValue());// 1000);
			} catch (final Exception e) {
				Log.Error(e.getMessage());
			}
		}
	}

	public boolean isMining() {
		return isMining.get();
	}

	public void plantingNewEQCHive() throws Exception {
		ID tailHeight = null;
		EQCHive newEQCHive = null;
		Savepoint savepoint = null;
		// Begin making new EQCHive
		tailHeight = globalState.getEQCHiveTailHeight();
		newEQCHiveHeight = tailHeight.getNextID();
		savepoint = globalState.setSavepoint();
		Log.info("Begin mining new EQCHive local tail: " + tailHeight + " work thread state: " + worker.getState() + " savepoint: " + savepoint);

		newEQCHive = new EQCHive(globalState.getEQCHiveRootProof(tailHeight), newEQCHiveHeight, globalState);
		newEQCHive.planting();

		Log.info("New EQCHive planting successful height: " + newEQCHiveHeight);
//		Log.info(newEQCBlock.toString());
		Log.info("Size: " + newEQCHive.getBytes().length);
		Log.info("New EQCHive have " + newEQCHive.getEQCoinSeeds().getNewTransactionList().size() + " new transactions");

		// Use this only for debug when after test will remove this
		try {
			final EQCHive eqcHive1 = new EQCHive(newEQCHive.getBytes());
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}

		// Beginning POW
		while (!newEQCHive.getRoot().isDifficultyValid()) {
			onPause("minering");
			if (!isRunning.get() || !isMining.get()) {
				Log.info("Exit from mining");
				return;
			}
			newEQCHive.getRoot().setNonce(newEQCHive.getRoot().getNonce().getNextID());
			if (newEQCHive.getRoot().getNonce().mod(ID.TWO).equals(ID.ZERO) && isRunning.get() && isMining.get()) {
				halt();
			}
		}

		// Planting successful
		synchronized (EQCService.class) {
			// Here add lock to avoid conflict with handle new received EQCHive in
			// EQCServiceProvider
			Log.info("Begin synchronized (EQCService.class)");
//						onPause("verify new EQCHive"); // Here can't pause which will cause deadlock
			if (!isRunning.get() || !isMining.get()) {
				// Here must check if it has been stopped because of already received valid new
				// EQCHive during mining
				Log.info("The POW was finished but because of already received new EQCHive so gentle exit");
				return;
			}

			Log.info("EQCHive No." + newEQCHive.getRoot().getHeight().longValue() + " Find use: "
					+ (System.currentTimeMillis() - newEQCHive.getRoot().getTimestamp().longValue()) + " ms");

			// When finished debug will remove this to save space
			Log.info("\n" + newEQCHive.getRoot().toString());

			// Check if current local tail is the mining base in case which has been changed
			// by EQCServiceProvider
			if (newEQCHiveHeight.isNextID(globalState.getEQCHiveTailHeight())) { // Here must retrieve the lively tail height
				Log.info("Still on the tail just broadcast it");
				try {
					// Send new EQCHive to EQCMinerNetwork and EQCHiveSyncNetwork if at here exists
					// any exception shouldn't block the mining
					final NewEQCHiveState newEQCHiveState = new NewEQCHiveState();
					newEQCHiveState.getNewEQCHive().setEQCHive(newEQCHive);
					BroadcastNewEQCHiveService.getInstance().offerNewEQCHiveState(newEQCHiveState);
				} catch (final Exception e) {
					Log.Error(e.getMessage());
				}
				
				Log.info("Begin commit EQCHive No." + newEQCHive.getRoot().getHeight().longValue());
				globalState.updateGlobalState(newEQCHive, savepoint, GlobalState.MINING);
				Log.info("EQCHive No." + newEQCHive.getRoot().getHeight().longValue() + " committed successful");
				
			} else {
				Log.Error("Current mining height is: " + newEQCHiveHeight + " but local tail height changed to: "
						+ globalState.getEQCHiveTailHeight() + " so have to discard this POW");
			}
			Log.info("End synchronized (EQCService.class)");
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
		case MINING:
			this.state.set(State.MINING);
			isMining.set(true);
			onMinering(state);
			break;
		default:
			break;
		}
	}

	private void onMinering(final EQCServiceState state) {
		Log.info("Begin minering...");
		this.state.set(State.MINER);
		while (isRunning.get() && isMining.get()) {
			onPause("prepare minering");
			if (!isRunning.get() || !isMining.get()) {
				Log.info("Exit from prepare minering");
				break;
			}
			try {
				plantingNewEQCHive();
			} catch (final Exception e) {
				Log.Error("Due to error occur have to restart MinerService: " + e.getMessage());
				stop();
				EQCServiceProvider.getInstance().offerState(new EQCServiceState(State.FIND));
				break;
			}
		}
		Log.info("End of mining");
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#pause()
	 */
	@Override
	public void pause() {
		synchronized (isPausing) {
			Log.info(name + "Begining pause() thread state: " + worker.getState());
			isPausing.set(true);
		}
		resumeHalt();
		super.pause();
	}

	private void resumeHalt() {
		synchronized (name) {
			Log.info("Begin resumeHalt");
			name.notify();
			Log.info("End resumeHalt");
		}
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
		worker.setPriority(Thread.MAX_PRIORITY);
//		startMining();
		isMining.set(true);
		Log.info(name + "started");
	}

	public void startMining() {
		offerState(new EQCServiceState(State.MINING));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqchains.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		isRunning.set(false);
		stopMining();
		super.stop();
		if(globalState != null) {
			try {
				globalState.close();
			} catch (final Exception e) {
				Log.Error(e.getMessage());
			}
			globalState = null;
		}
		instance = null;
	}
	
	public void stopMining() {
		Log.info(name + "begin stop mining progress");
		isMining.set(false);
		if (isPausing.get()) {
			resumePause();
		}
		resumeHalt();
	}
	
}
