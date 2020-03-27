/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
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
package com.eqcoin.blockchain.changelog;

import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQCSeed;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.seed.EQcoinSeedRoot;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.ZionOPTransaction;
import com.eqcoin.blockchain.transaction.ZionTransaction;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 19, 2020
 * @email 10509759@qq.com
 */
public class Statistics {
	private ID totalSupply;
	private ID totalTransactionNumbers;
	private ID totalPublickeyNumbers;
	private ChangeLog changeLog;
	
	public Statistics(ChangeLog changeLog) {
		totalSupply = ID.ZERO;
		totalTransactionNumbers = ID.ZERO;
		totalPublickeyNumbers = ID.ZERO;
		this.changeLog = changeLog;
	}
	
	public boolean isValid(EQCSeed eQcoinSeed) throws Exception {
		// Check if total new lock numbers equal to total new passport numbers + total new updated Lock numbers
		EQcoinSeedRoot preEQcoinSeedRoot = changeLog.getEQCHive(changeLog.getHeight().getPreviousID(), true).getEQcoinSeed()
				.getEQcoinSeedRoot();
		ID totalNewLockNumbers = changeLog.getTotalLockNumbers().subtract(preEQcoinSeedRoot.getTotalLockNumbers());
		ID totalNewPassportNumbers = changeLog.getTotalPassportNumbers().subtract(preEQcoinSeedRoot.getTotalPassportNumbers());
		if(!totalNewLockNumbers.equals(totalNewPassportNumbers.add(changeLog.getTotalNewUpdatedLockNumbers()))) {
			Log.Error("TotalNewLockNumbers doesn't equal to totalNewPassportNumbers + totalNewUpdateLockNumbers. This is invalid.");
			throw new IllegalStateException("TotalNewLockNumbers doesn't equal to totalNewPassportNumbers + totalNewUpdateLockNumbers. This is invalid.");
		}
		
		// Check if total supply is valid
		if(!totalSupply.equals(Util.cypherTotalSupply(changeLog.getHeight()))) {
			Log.Error("TotalSupply is invalid expected: " + Util.cypherTotalSupply(changeLog.getHeight()) + " but actual: " + totalSupply);
			return false;
		}
		
		// Check if total transaction numbers is valid
		if(!totalTransactionNumbers.equals(preEQcoinSeedRoot.getTotalTransactionNumbers().add(new ID(eQcoinSeed.getNewTransactionList().size())))) {
			Log.Error("TotalTransactionNumbers is invalid expected: " + totalTransactionNumbers + " but actual: " + preEQcoinSeedRoot.getTotalTransactionNumbers().add(new ID(eQcoinSeed.getNewTransactionList().size())));
			return false;
		}
		
		// Check if total lock numbers is valid
		if(!changeLog.getTotalLockNumbers().equals(Util.DB().getTotalLockNumbers(changeLog.getHeight().getPreviousID()).add(Util.DB().getTotalNewLockNumbers(changeLog)))) {
			Log.Error("Total lock numbers is invalid.");
			return false;
		}
		
		// Check if total passport numbers is valid
		if(!changeLog.getTotalPassportNumbers().equals(Util.DB().getTotalPassportNumbers(changeLog.getHeight().getPreviousID()).add(Util.DB().getTotalNewPassportNumbers(changeLog)))) {
			Log.Error("Total passport numbers is invalid.");
			return false;
		}
		
		return true;
	}
	
	public void update(Passport passport) {
		totalSupply = totalSupply.add(passport.getBalance());
		totalTransactionNumbers = totalTransactionNumbers.add(passport.getNonce());
	}
	
	/**
	 * @return the totalSupply
	 */
	public ID getTotalSupply() {
		return totalSupply;
	}

	/**
	 * @return the totalTransactionNumbers
	 */
	public ID getTotalTransactionNumbers() {
		return totalTransactionNumbers;
	}

	public void generateStatistics(EQCSeed eQcoinSeed) throws Exception {
		Passport passport;
		for(long i=1; i<=changeLog.getTotalPassportNumbers().longValue(); ++i) {
			passport = changeLog.getFilter().getPassport(new ID(i), true);
			update(passport);
		}
		Lock lock;
		for(long i=1; i<=changeLog.getTotalLockNumbers().longValue(); ++i) {
			lock = changeLog.getFilter().getLock(new ID(i), true);
			if(lock.getPublickey() != null) {
				totalPublickeyNumbers = totalPublickeyNumbers.getNextID();
			}
		}
	}
	
}
