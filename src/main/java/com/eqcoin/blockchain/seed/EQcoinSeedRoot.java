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
package com.eqcoin.blockchain.seed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.TransferCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date July 30, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSeedRoot extends EQCSeedRoot {
	private ID totalSupply;
	/**
	 * Calculate this according to newHelixList ARRAY's length which equal to the
	 * new Locks in ZionTransaction and UpdateLockOP
	 */
	private ID totalLockNumbers;
	/**
	 * Calculate this according to ZionTransaction.
	 */
	private ID totalPassportNumbers;
	/**
	 * Calculate this according to newPublickeyList ARRAY's length which equal to
	 * the total numbers of Lock which have the Publickey.
	 */
	private ID totalPublickeyNumbers;
	/**
	 * Save the root of Lock Merkel Tree.
	 */
	private byte[] lockMerkelTreeRoot;
	/**
	 * Save the root of Passport Merkel Tree.
	 */
	private byte[] passportMerkelTreeRoot;
	private TransferCoinbaseTransaction coinbaseTransaction;
	private EQcoinSeed eQcoinSeed;
	private final int SIZE64 = 64;
	private ChangeLog changeLog;
	
	public EQcoinSeedRoot(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public EQcoinSeedRoot(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public EQcoinSeedRoot() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		totalSupply = EQCType.parseID(is);
		totalLockNumbers = EQCType.parseID(is);
		totalPassportNumbers = EQCType.parseID(is);
		lockMerkelTreeRoot = EQCType.parseBIN(is);
		passportMerkelTreeRoot = EQCType.parseBIN(is);
		coinbaseTransaction = (TransferCoinbaseTransaction) Transaction.parseTransaction(EQCType.parseBIN(is));
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(super.getBodyBytes());
		os.write(totalSupply.getEQCBits());
		os.write(totalLockNumbers.getEQCBits());
		os.write(totalPassportNumbers.getEQCBits());
		os.write(EQCType.bytesToBIN(lockMerkelTreeRoot));
		os.write(EQCType.bytesToBIN(passportMerkelTreeRoot));
		os.write(coinbaseTransaction.getBin());
		return os.toByteArray();
	}

	/**
	 * @return the totalPassportNumbers
	 */
	public ID getTotalPassportNumbers() {
		return totalPassportNumbers;
	}

	/**
	 * @param totalPassportNumbers the totalPassportNumbers to set
	 */
	public void setTotalPassportNumbers(ID totalPassportNumbers) {
		this.totalPassportNumbers = totalPassportNumbers;
	}

	/**
	 * @return the coinbaseTransaction
	 */
	public TransferCoinbaseTransaction getCoinbaseTransaction() {
		return coinbaseTransaction;
	}

	/**
	 * @param coinbaseTransaction the coinbaseTransaction to set
	 */
	public void setCoinbaseTransaction(TransferCoinbaseTransaction coinbaseTransaction) {
		this.coinbaseTransaction = coinbaseTransaction;
	}

	protected String toInnerJson() {
		return "\"EQcoinSeedHeader\":" + "{\n"
				+ "\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\"" + ",\n"
				+ "\"TotalLockNumbers\":" + "\"" + totalLockNumbers + "\"" + ",\n" 
				+ "\"TotalPassportNumbers\":" + "\"" + totalPassportNumbers + "\"" + ",\n" 
				+ "\"LockMerkelTreeRoot\":" + "\"" + Util.getHexString(lockMerkelTreeRoot) + "\"" + ",\n" 
				+ "\"PassportMerkelTreeRoot\":" + "\"" + Util.getHexString(passportMerkelTreeRoot) + "\"" + ",\n" 
				+ coinbaseTransaction.toInnerJson()
				+ "\n" + "}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if (!super.isSanity()) {
			return false;
		}
		if (totalLockNumbers == null || !totalLockNumbers.isSanity() || totalPassportNumbers == null
				|| !totalPassportNumbers.isSanity() || lockMerkelTreeRoot == null || lockMerkelTreeRoot.length != SIZE64
				|| passportMerkelTreeRoot == null || passportMerkelTreeRoot.length != SIZE64) {
			return false;
		}

		if(changeLog.getHeight().compareTo(Util.getMaxCoinbaseHeight(changeLog.getHeight())) < 0) {
			if(coinbaseTransaction == null || !coinbaseTransaction.isSanity()) {
				return false;
			}
		}
		else {
			if(coinbaseTransaction != null) {
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public boolean isValid() throws Exception {
		changeLog.buildLockMerkleTreeBase();
		changeLog.generateLockMerkleTreeRoot();
		changeLog.buildPassportMerkleTreeBase();
		changeLog.generatePassportMerkleTreeRoot();
		if(!totalTransactionNumbers.equals(changeLog.getStatistics().getTotalTransactionNumbers())) {
			Log.Error("TotalTransactionNumbers is invalid expected: " + totalTransactionNumbers + " but actual: " + changeLog.getStatistics().getTotalTransactionNumbers());
			return false;
		}
		
		return true;
	}

	/**
	 * @return the totalLockNumbers
	 */
	public ID getTotalLockNumbers() {
		return totalLockNumbers;
	}

	/**
	 * @param totalLockNumbers the totalLockNumbers to set
	 */
	public void setTotalLockNumbers(ID totalLockNumbers) {
		this.totalLockNumbers = totalLockNumbers;
	}

	/**
	 * @param EQcoinSeed the EQcoinSeed to set
	 */
	public void setEQcoinSeed(EQcoinSeed eQcoinSeed) {
		this.eQcoinSeed = eQcoinSeed;
	}

	/**
	 * @return the lockMerkelTreeRoot
	 */
	public byte[] getLockMerkelTreeRoot() {
		return lockMerkelTreeRoot;
	}

	/**
	 * @param lockMerkelTreeRoot the lockMerkelTreeRoot to set
	 */
	public void setLockMerkelTreeRoot(byte[] lockMerkelTreeRoot) {
		this.lockMerkelTreeRoot = lockMerkelTreeRoot;
	}

	/**
	 * @return the passportMerkelTreeRoot
	 */
	public byte[] getPassportMerkelTreeRoot() {
		return passportMerkelTreeRoot;
	}

	/**
	 * @param passportMerkelTreeRoot the passportMerkelTreeRoot to set
	 */
	public void setPassportMerkelTreeRoot(byte[] passportMerkelTreeRoot) {
		this.passportMerkelTreeRoot = passportMerkelTreeRoot;
	}

	/**
	 * @return the totalSupply
	 */
	public ID getTotalSupply() {
		return totalSupply;
	}

	/**
	 * @param totalSupply the totalSupply to set
	 */
	public void setTotalSupply(ID totalSupply) {
		this.totalSupply = totalSupply;
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * @return the totalPublickeyNumbers
	 */
	public ID getTotalPublickeyNumbers() {
		return totalPublickeyNumbers;
	}

	/**
	 * @param totalPublickeyNumbers the totalPublickeyNumbers to set
	 */
	public void setTotalPublickeyNumbers(ID totalPublickeyNumbers) {
		this.totalPublickeyNumbers = totalPublickeyNumbers;
	}
	
}
