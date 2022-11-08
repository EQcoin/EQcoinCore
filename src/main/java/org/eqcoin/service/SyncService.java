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
 */
package org.eqcoin.service;

import java.util.Vector;

import org.eqcoin.avro.O;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.client.avro.EQCMinerNetworkClient;
import org.eqcoin.rpc.client.avro.EQCTransactionNetworkClient;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.SPList;
import org.eqcoin.rpc.object.TransactionIndex;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.service.state.SleepState;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Util.SP_MODE;

/**
 * For sync various tasks for example transaction sync
 * Need do more job to support different sleep interval
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
			SPList spList = Util.MC().getSPList(SP_MODE.getFlag(SP_MODE.EQCTRANSACTIONNETWORK));
			spList.addSP(Util.SINGULARITY_SP);
			TransactionIndexList transactionIndexList = null;
			TransactionIndexList needSyncList = null;
			TransactionList transactionList = null;
			for(SP sp:spList.getSPList()) {
				Log.info("Begin get Transaction list");
				transactionIndexList = EQCTransactionNetworkClient.getTransactionIndexList(sp);
				needSyncList = new TransactionIndexList();
				if(transactionIndexList != null) {
					Log.info("Begin sync Transaction");
					for(TransactionIndex transactionIndex:transactionIndexList.getTransactionIndexList()) {
						if(!Util.MC().isTransactionExistsInPool(transactionIndex)) {
							needSyncList.addTransactionIndex(transactionIndex);
						}
					}
				}
				transactionList = EQCTransactionNetworkClient.getTransactionList(needSyncList, sp);
				if(transactionList != null) {
					for(Transaction transaction:transactionList.getTransactionList()) {
						Util.MC().saveTransactionInPool(transaction);
					}
				}
			}
			sleeping(Util.getCurrentEQCHiveInterval(Util.GS()).longValue()/10);
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
		getInstance();
		super.start();
		try {
			sleeping(Util.getCurrentEQCHiveInterval(Util.GS()).longValue()/10);
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		instance = null;
	}

}
