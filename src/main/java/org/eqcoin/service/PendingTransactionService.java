/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.service;

import org.eqcoin.service.state.EQCServiceState;
import org.eqcoin.service.state.PendingTransactionState;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class PendingTransactionService extends EQCService {
	private static PendingTransactionService instance;
	private Value txFeeRate;
	
	private PendingTransactionService() {
		super();
//		EQcoinRootPassport eQcoinRootPassport;
//		try {
//			eQcoinRootPassport = (EQcoinRootPassport) Util.GS().getPassport(ID.ZERO);
//			txFeeRate = new Value(eQcoinRootPassport.getTxFeeRate());
//		} catch (Exception e) {
//			Log.Error(e.getMessage());
//		}
	}
	
	public static PendingTransactionService getInstance() {
		if (instance == null) {
			synchronized (PendingTransactionService.class) {
				if (instance == null) {
					instance = new PendingTransactionService();
				}
			}
		}
		return instance;
	}
	
    /* (non-Javadoc)
	 * @see com.eqchains.service.EQCService#onDefault(com.eqchains.service.state.EQCServiceState)
	 */
	@Override
	protected synchronized void onDefault(EQCServiceState state) {
		// Here change to new method to calculate the nonce - the client should keep the nonce is continuously so if the nonce isn't correctly just discard it
		PendingTransactionState pendingTransactionState = null;
		Transaction transaction = null;
		Passport passport = null;
		try {
			Log.info("Received new Transaction");
			pendingTransactionState = (PendingTransactionState) state;
			transaction = new Transaction().setTransactionShape(TransactionShape.RPC).Parse(pendingTransactionState.getTransaction());
			transaction.setTxFeeRate(txFeeRate);
			if(!transaction.getWitness().isMeetPreCondition()) {
				Log.info("Doesn't meet pre condition just discard it");
			}
			passport = transaction.getWitness().getPassport();
			if(transaction.getNonce().compareTo(passport.getNonce()) < 0) {
				Log.info("Transaction's nonce " + transaction.getNonce() + " less than relevant Account's Asset's nonce " + passport.getNonce() + " just discard it");
				return;
			}
//			transaction.getTxIn().getLock().setId(account.getId());
//			maxNonce = EQCBlockChainH2.getInstance().getTransactionMaxNonce(transaction.getNest());
//			// Here maybe exists one bug maybe need remove this
//			if(transaction.getNonce().compareTo(maxNonce.getNonce().getNextID()) > 0) {
//				Log.info("Transaction's nonce more than relevant Account's Asset's max nonce just discard it");
//				return;
//			}
			// Here doesn't extra check to make sure nonce is continuously due to EQCBlockChainH2.getInstance().getTransactionMaxNonce may not synchronized
			
			if(!transaction.isSanity()) {
				Log.info("Transaction with ID " + passport.getId() + " isn't sanity just discard it");
				return;
			}
			
			Util.MC().saveTransactionInPool(transaction);
			Log.info("Transaction with ID " + passport.getId()  + " and nonce " + transaction.getNonce() + " is sanity just save it");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public void offerPendingTransactionState(PendingTransactionState pendingTransactionState) {
		pendingMessage.offer(pendingTransactionState);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#stop()
	 */
	@Override
	public synchronized void stop() {
		super.stop();
		instance = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.service.EQCService#start()
	 */
	@Override
	public synchronized void start() {
		getInstance();
		super.start();
	}
	
}
