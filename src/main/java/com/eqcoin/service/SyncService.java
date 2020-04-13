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

import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.IP;
import com.eqcoin.rpc.IPList;
import com.eqcoin.rpc.TransactionIndex;
import com.eqcoin.rpc.TransactionIndexList;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.rpc.client.MinerNetworkClient;
import com.eqcoin.rpc.client.TransactionNetworkClient;
import com.eqcoin.service.state.SleepState;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Aug 6, 2019
 * @email 10509759@qq.com
 */
public class SyncService extends EQCService {
	private static SyncService instance;

	private SyncService() {
		super();
	}
	
	public static SyncService getInstance() {
		if (instance == null) {
			synchronized (SyncService.class) {
				if (instance == null) {
					instance = new SyncService();
				}
			}
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onSleep(com.eqchains.service.state.SleepState)
	 */
	@Override
	protected void onSleep(SleepState state) {
		try {
//			Log.info("Begin onSync");
			// Check if miner in miner list is online doesn't need check at here move to the ohter's place
//			IPList ipList = EQCBlockChainH2.getInstance().getMinerList();
//			for(String ip:ipList.getIpList()) {
//				if(MinerNetworkClient.ping(ip) == -1) {
//					Util.updateDisconnectIPStatus(ip);
//				}
//			}
			
			// Sync Transaction
			IPList<O> ipList = EQCBlockChainH2.getInstance().getMinerList();
			ipList.addIP(Util.SINGULARITY_IP);
			TransactionIndexList<O> transactionIndexList = null;
			TransactionIndexList<O> needSyncList = null;
			TransactionList<O> transactionList = null;
			for(IP ip:ipList.getIpList()) {
				Log.info("Begin get Transaction list");
				transactionIndexList = MinerNetworkClient.getTransactionIndexList(ip);
				needSyncList = new TransactionIndexList();
				if(transactionIndexList != null) {
					Log.info("Begin sync Transaction");
					for(TransactionIndex<O> transactionIndex:transactionIndexList.getTransactionIndexList()) {
						if(!EQCBlockChainH2.getInstance().isTransactionExistsInPool(transactionIndex)) {
							needSyncList.addTransactionIndex(transactionIndex);
						}
					}
				}
				transactionList = MinerNetworkClient.getTransactionList(needSyncList, ip);
				if(transactionList != null) {
					for(Transaction transaction:transactionList.getTransactionList()) {
						EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
					}
				}
			}
			sleeping(Util.getCurrentBlockInterval().longValue()/10);
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		// TODO Auto-generated method stub
		super.start();
		try {
			sleeping(Util.getCurrentBlockInterval().longValue()/10);
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

}
