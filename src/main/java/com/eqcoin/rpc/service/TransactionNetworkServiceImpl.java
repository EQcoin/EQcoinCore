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
package com.eqcoin.rpc.service;

import java.nio.ByteBuffer;
import java.util.Vector;

import org.apache.avro.AvroRemoteException;

import com.eqcoin.avro.O;
import com.eqcoin.avro.TransactionNetwork;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.Balance;
import com.eqcoin.rpc.Code;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.rpc.TransactionList;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.service.PendingTransactionService;
import com.eqcoin.service.state.PendingTransactionState;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class TransactionNetworkServiceImpl implements TransactionNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public O ping(O cookie) {
		O info = null;
		Cookie cookie1 = null;
		try {
			cookie1 = new Cookie(cookie);
			if(cookie1.isSanity()) {
				info = Util.getDefaultInfo().getProtocol();
			}
			else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null).getProtocol();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMinerList()
	 */
	@Override
	public O getMinerList() {
		O minerList = null;
		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList().getProtocol();
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
			info = Util.getDefaultInfo().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getID(com.eqchains.avro.IO)
	 */
	@Override
	// 2020-04-05 Here need do more job
	public O getID(O readableAddress) {
		O id = null;
//		Passport account = null;
//		try {
//			account = Util.DB().getPassport(LockTool.readableLockToAI(EQCType.bytesToASCIISting(readableAddress.getO().array())), Mode.GLOBAL);
//			if(account != null) {
//				id = Util.bytes2O(account.getId().getEQCBits());
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return id;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getAccount(com.eqchains.avro.IO)
	 */
	@Override
	// 2020-04-05 Here need do more job
	public O getAccount(O a) {
		O io = null;
//		Passport account = null;
//		try {
//			account = Util.DB().getPassport(a.o.array(), Mode.GLOBAL);
//			if(account != null) {
//				io = Util.bytes2O(account.getBytes());
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getMaxNonce(com.eqchains.avro.IO)
	 */
	@Override
	public O getMaxNonce(O nest) {
		O io = null;
		MaxNonce<O> maxNonce = null;
		try {
			maxNonce = EQCBlockChainH2.getInstance().getTransactionMaxNonce(new Nest(nest));
			io = maxNonce.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getBalance(com.eqchains.avro.IO)
	 */
	@Override
	public O getBalance(O nest) {
		O io = null;
		Balance<O> balance = null;
		try {
			balance = Util.DB().getBalance(new Nest(nest));
			io = balance.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getSignHash(com.eqchains.avro.IO)
	 */
	@Override
	public O getSignHash(O id) {
		O io = null;
		byte[] signHash = null;
		try {
//			signHash = Util.DB().getPassport(new ID(id.getO().array()), Mode.GLOBAL).getSignatureHash();
			if(signHash != null) {
				io = Util.bytes2O(signHash);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.avro.TransactionNetwork#getTransactionList(com.eqchains.avro.IO)
	 */
	@Override
	public O getPendingTransactionList(O nest) {
		O io = null;
		TransactionList<O> transactionList = new TransactionList();
		Vector<Transaction> transactions = null;
		try {
			transactions = EQCBlockChainH2.getInstance().getPendingTransactionListInPool(new Nest(nest));
			if(!transactions.isEmpty()) {
				for(Transaction transaction:transactions) {
					transactionList.addTransaction(transaction);
				}
				io = transactionList.getProtocol();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return io;
	}

}
