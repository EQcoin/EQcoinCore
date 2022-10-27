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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.SleepState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * EQC message queue service for relevant Service for examples:
 * EQCServiceProvider and RPC service. EQCService will handle the message in the
 * relevant message queue one bye one according to it's priority.
 * 
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCService implements Runnable {
	protected PriorityBlockingQueue<EQCServiceState> pendingMessage;
	protected Thread worker;
	protected AtomicBoolean isRunning;
	protected AtomicBoolean isPausing;
	private Object isPaused;
	protected AtomicBoolean isSleeping;
	protected AtomicBoolean isWaiting;
//	protected AtomicBoolean isStopped;
	protected AtomicReference<State> state;
	protected String name;
	
	public EQCService() {
		pendingMessage = new PriorityBlockingQueue<>(Util.HUNDREDPULS);
		isRunning = new AtomicBoolean(false);
		isPausing = new AtomicBoolean(false);
		isPaused = new Object();
		isSleeping = new AtomicBoolean(false);
		isWaiting = new AtomicBoolean(false);
//		isStopped = new AtomicBoolean(true);
		state = new AtomicReference<>();
		name = this.getClass().getSimpleName() + " ";
	}
	
	public synchronized void start() {
		Log.info(name+ " Begin starting " + ((worker == null)?" previous worker thread is null":" previous worker thread is't null thread ID: " + worker.getId()));
//		synchronized (isStopped) {
//			if(!isStopped.get()) {
//				try {
//					Log.info(name + "waiting for previous thread stop state: " + worker.getState());
//					isStopped.wait();
//					Log.info(name + "previous thread stopped state:" + worker.getState());
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.info(name + e.getMessage());
//				}
//			}
//		}
		
		if (worker == null || !worker.isAlive() || !isPausing.get()) {
			Log.info(name + " old worker thread's ID: " + ((worker==null)?worker:worker.getId()));
			worker = new Thread(this);
			Log.info(name + " create new thread successful thread ID: " + worker.getId());
			worker.setPriority(Thread.NORM_PRIORITY);
			worker.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
				public void uncaughtException(Thread t, Throwable e) {
					Log.Error(name + "thread state: " + worker.getState() + " Uncaught Exception occur: " + e.getMessage());
					Log.info(name + "beginning stop " + name);
					stop();
					Log.info(name + "beginning start " + name);
					start();
				}
			});
			name = this.getClass().getSimpleName() + " thread ID: " + worker.getId() + " ";
			worker.start();
//			synchronized (isRunning) {
//				try {
//					Log.info(name + "waiting for thread start");
//					isRunning.wait();
//					Log.info(name + "thread started");
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					Log.Error(e.getMessage());
//				}
//			}
		}
		else {
				Log.info(name + "previous thread's worker still exists&running and it's state: " + worker.getState());
		}
	}

	public synchronized boolean isRunning() {
		return  (worker != null) && worker.isAlive();
	}

	public synchronized void stop() {
		Log.info(name + "begining stop thread state: " + worker.getState());
		isRunning.set(false);
		if(isSleeping.get()) {
			resumeSleeping();
		}
		if (isPausing.get()) {
			resumePause();
		}
		if(isWaiting.get()) {
			resumeWaiting();
		}
		offerState(new EQCServiceState(State.STOP));
	}

	// 20200516 here isn't synchronized exists bug need waiting till the service is really paused
	public void pause() {
		synchronized (isPausing) {
			Log.info(name + "Begining pause() thread state: " + worker.getState());
			isPausing.set(true);
		}
		synchronized (isPaused) {
			try {
				isPaused.wait();
				Log.info(name + "End of pause(), isPausing: " + isPausing.get());
			} catch (InterruptedException e) {
				Log.Error(e.getMessage());
			}
		}
	}

	public void onPause(String ...phase) {
		synchronized (isPausing) {
			if (isPausing.get()) {
				try {
					if(!isRunning.get()) {
						Log.info(name + "received pause request at " + ((phase==null)?phase:phase[0]) + " but due to is already stop running so here have nothing to do");
						return;
					}
					if(phase != null) {
						Log.info(name + "paused at " + phase[0]);
					}
					Log.info(name + "is pausing now thread state: " + worker.getState());
					synchronized (isPaused) {
						isPaused.notify();
					}
					isPausing.wait();
					isPausing.set(false);
					Log.info(name + "End of onPause(), isPausing: " + isPausing.get());
				} catch (InterruptedException e) {
					Log.Error(e.getMessage());
				}
			}
		}
	}

	public void resumePause() {
		synchronized (isPausing) {
			isPausing.set(false);
			Log.info(name + "resumePause() thread state: " + worker.getState() + " isPausing: " + isPausing);
			isPausing.notify();
		}
	}

	public void waiting() {
		synchronized (isWaiting) {
			Log.info(name + "begin waiting thread state: " + worker.getState());
			isWaiting.set(true);
			offerState(new EQCServiceState(State.WAIT));
		}
	}

	public void resumeWaiting() {
		synchronized (isWaiting) {
			Log.info(name + "resumeWaiting thread state: " + worker.getState());
			isWaiting.notify();
		}
	}

	public void sleeping(long sleepTime) {
		synchronized (isSleeping) {
			isSleeping.set(true);
			offerState(new SleepState(sleepTime));
		}
	}

	public void resumeSleeping() {
		synchronized (isSleeping) {
			Log.info(name + "resumeSleeping thread state: " + worker.getState());
			isSleeping.notify();
		}
	}

	@Override
	public void run() {
		isRunning.set(true);
//		synchronized (isRunning) {
//			isRunning.notify();
//		}
//		isStopped.set(false);
		state.set(State.RUNNING);
		Log.info(name + "new worker thread is running now... thread id: " + worker.getId());
		EQCServiceState state = null;
		while (isRunning.get()) {
			try {
				Log.info(name + "Waiting for new message... pending message size: " + pendingMessage.size() + " pending message: "+ pendingMessage + " thread state: " + worker.getState());
				state = pendingMessage.take();
				Log.info(name + "take new message: " + state + " thread state: " + worker.getState());
				this.state.set(State.TAKE);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error(name + e.getMessage());
				this.state.set(State.ERROR);
				continue;
			}
			switch (state.getState()) {
			case SLEEP:
//				Log.info(name + "Begin onSleep");
				onSleep(state);
//				Log.info(name + "End onSleep");
				break;
			case WAIT:
				this.state.set(State.WAIT);
//				Log.info(name + "Begin onWaiting");
				onWaiting(state);
//				Log.info(name + "End onWaiting");
				break;
			case STOP:
//				Log.info(name + "Begin onStop");
				onStop(state);
//				Log.info(name + "End onStop");
				break;
			default:
//				Log.info(name + "Begin onDefault");
				onDefault(state);
//				Log.info(name + "End onDefault");
				break;
			}
		}
		Log.info(name + "worker thread stopped... thread state: " + worker.getState());
//		synchronized (isStopped) {
//			isStopped.set(true);
//			isStopped.notify();
//		}
	}

	private void onSleep(EQCServiceState state) {
		synchronized (isSleeping) {
			this.state.set(State.SLEEP);
			SleepState sleepState = (SleepState) state;
			Log.info(name + "onSleep time: " + sleepState.getSleepTime());
			if (isSleeping.get()) {
				try {
					if(!isRunning.get()) {
						Log.info(name + "received sleep request but due to is already stop running so here have nothing to do");
						return;
					}
					if (sleepState.getSleepTime() == 0) {
						isSleeping.wait();
					} else {
						isSleeping.wait(sleepState.getSleepTime());
					}
					onSleep(sleepState);
					isSleeping.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(e.getMessage());
				}
			}
		}
	}

	protected void onSleep(SleepState state) {
		
	}

	protected void onWaiting(EQCServiceState state) {
		synchronized (isWaiting) {
			if (isWaiting.get()) {
				if(!isRunning.get()) {
					Log.info(name + "received wait request but due to is already stop running so here have nothing to do");
					return;
				}
				Log.info(name + "onWaiting");
				try {
					isWaiting.wait();
					isWaiting.set(false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.Error(name + e.getMessage());
				}
			}
		}
	}

	protected void onStop(EQCServiceState state) {
		Log.info(name + "Received stop message need stop now thread state: " + worker.getState());
	}

	protected void onDefault(EQCServiceState state) {

	}

	public void offerState(EQCServiceState state) {
			pendingMessage.offer(state);
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state.get();
	}

}
