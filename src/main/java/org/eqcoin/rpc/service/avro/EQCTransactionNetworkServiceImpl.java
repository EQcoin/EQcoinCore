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
package org.eqcoin.rpc.service.avro;

import java.util.Vector;

import org.eqcoin.avro.EQCTransactionNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.rpc.object.LockInfo;
import org.eqcoin.rpc.object.LockStatus;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.rpc.object.LockStatus.LOCKSTATUS;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.service.PendingTransactionService;
import org.eqcoin.service.state.PendingTransactionState;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.wallet.WalletStatus.Status;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class EQCTransactionNetworkServiceImpl extends EQCRPCServiceImpl implements EQCTransactionNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public O ping() {
		O info = null;
		try {
				info = Util.getDefaultInfo().getProtocol(O.class);
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#sendTransaction(com.eqchains.avro.IO)
	 */
	@Override
	public O sendTransaction(O transactionRPC) {
		O info = null;
		PendingTransactionState pendingTransactionState = null;
		try {
			pendingTransactionState = new PendingTransactionState(transactionRPC);
			PendingTransactionService.getInstance().offerPendingTransactionState(pendingTransactionState);
			info = Util.getDefaultInfo().getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public O getPendingTransactionList(O i) {
		O io = null;
		TransactionList transactionList = new TransactionList();
		Vector<Transaction> transactions = null;
		try {
			transactions = Util.MC().getPendingTransactionListInPool(EQCCastle.parseID(i));
			if(!transactions.isEmpty()) {
				for(Transaction transaction:transactions) {
					transactionList.addTransaction(transaction);
				}
				io = transactionList.getProtocol(O.class);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

	@Override
	public O getLockInfo(O s) {
		O o = null;
		LockInfo lockInfo = new LockInfo();
		Passport passport = null;
		LockStatus lockStatus = null;
		try {
			lockStatus = new LockStatus(s);
			if(lockStatus.getType() == LOCKSTATUS.READABLELOCK) {
				LockMate lockMate = Util.GS().getLockMate(LockTool.readableLockToEQCLock(lockStatus.getReadableLock()));
				if(lockMate != null) {
					passport = Util.GS().getPassportFromLockMateId(lockMate.getId());
					if(passport != null) {
						lockInfo.setStatus(Status.LIVELY);
						lockInfo.setPassport(passport);
					}
					else {
						lockInfo.setStatus(Status.FORBIDDEN);
					}
				}
			}
			else {
				lockInfo.setStatus(Status.UNUSED);
			}
			o = lockInfo.getProtocol(O.class);
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return o;
	}
	
	@Override
	public O getTransactionIndexList(O synctime) {
		O io = null;
		long syncTime = Util.bytesToLong(synctime.getO().array());
		TransactionIndexList transactionIndexList = null;
		try {
			transactionIndexList = Util.MC().getTransactionIndexListInPool(syncTime, System.currentTimeMillis());
			io = transactionIndexList.getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public O getTransactionList(O transactionIndexList) {
		O io = null;
		TransactionList transactionList = null;
		try {
			transactionList = Util.MC().getTransactionListInPool(new TransactionIndexList(transactionIndexList));
			io = transactionList.getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

}
