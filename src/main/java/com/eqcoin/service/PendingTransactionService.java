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

import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.rpc.MaxNonce;
import com.eqcoin.rpc.Nest;
import com.eqcoin.service.state.EQCServiceState;
import com.eqcoin.service.state.PendingTransactionState;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 30, 2019
 * @email 10509759@qq.com
 */
public class PendingTransactionService extends EQCService {
	private static PendingTransactionService instance;
	
	private PendingTransactionService() {
		super();
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
		PendingTransactionState pendingTransactionState = null;
		Transaction transaction = null;
		Passport account = null;
		MaxNonce maxNonce = null;
		try {
			Log.info("Received new Transaction");
			pendingTransactionState = (PendingTransactionState) state;
			transaction = Transaction.parseRPC(pendingTransactionState.getTransaction());
			account = Util.DB().getPassport(transaction.getTxIn().getLock().getAddressAI(), Mode.GLOBAL);
			if(account == null) {
				Log.info("Transaction with readable address " + transaction.getTxIn().getLock().getReadableLock() + "'s relevant Account doesn't exists just discard it");
				return;
			}
			if(transaction.getNonce().compareTo(account.getNonce()) < 0) {
				Log.info("Transaction's nonce " + transaction.getNonce() + " less than relevant Account's Asset's nonce " + account.getNonce() + " just discard it");
				return;
			}
			transaction.getTxIn().getLock().setId(account.getId());
//			maxNonce = EQCBlockChainH2.getInstance().getTransactionMaxNonce(transaction.getNest());
//			// Here maybe exists one bug maybe need remove this
//			if(transaction.getNonce().compareTo(maxNonce.getNonce().getNextID()) > 0) {
//				Log.info("Transaction's nonce more than relevant Account's Asset's max nonce just discard it");
//				return;
//			}
			// Here doesn't extra check to make sure nonce is continuously due to EQCBlockChainH2.getInstance().getTransactionMaxNonce may not synchronized
			if(transaction.verifySignature()) {
//				if(transaction.getNonce().compareTo(maxNonce.getNonce().getNextID()) == 0) {
//					EQCBlockChainH2.getInstance().saveTransactionMaxNonce(transaction.getNest(), transaction.getMaxNonce());
//				}
				EQCBlockChainH2.getInstance().saveTransactionInPool(transaction);
				Log.info("Transaction with ID " + transaction.getTxIn().getLock().getId()  + " and nonce " + transaction.getNonce() + " is valid just save it");
			}
			else {
				Log.Error("Transaction is invalid just discard it");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	public void offerPendingTransactionState(PendingTransactionState pendingTransactionState) {
		pendingMessage.offer(pendingTransactionState);
	}

}
