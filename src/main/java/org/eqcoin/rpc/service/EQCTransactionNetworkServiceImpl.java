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
package org.eqcoin.rpc.service;

import java.nio.ByteBuffer;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;
import org.eqcoin.avro.EQCTransactionNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.changelog.Filter.Mode;
import org.eqcoin.lock.LockMate;
import org.eqcoin.lock.LockTool;
import org.eqcoin.passport.Passport;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.rpc.Code;
import org.eqcoin.rpc.LockInfo;
import org.eqcoin.rpc.LockStatus;
import org.eqcoin.rpc.Protocol;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.rpc.LockStatus.LOCKSTATUS;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.service.PendingTransactionService;
import org.eqcoin.service.state.PendingTransactionState;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.wallet.WalletStatus.Status;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class EQCTransactionNetworkServiceImpl implements EQCTransactionNetwork {

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

	@Override
	public O getSPList(O m) {
		O minerList = null;
		try {
			minerList = EQCHiveH2.getInstance().getSPList(EQCType.parseID(m.o.array())).getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
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
			transactions = Util.DB().getPendingTransactionListInPool(EQCType.parseID(i));
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
				LockMate lockMate = Util.DB().getLock(LockTool.readableLockToEQCLock(lockStatus.getReadableLock()), Mode.GLOBAL);
				if(lockMate != null) {
					passport = Util.DB().getPassportFromLockId(lockMate.getId(), Mode.GLOBAL);
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
			transactionIndexList = EQCHiveH2.getInstance().getTransactionIndexListInPool(syncTime, System.currentTimeMillis());
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
			transactionList = EQCHiveH2.getInstance().getTransactionListInPool(new TransactionIndexList(transactionIndexList));
			io = transactionList.getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	@Override
	public O registerSP(O S) {
		// TODO Auto-generated method stub
		return null;
	}

}
