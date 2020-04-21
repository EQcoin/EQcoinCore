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
import java.util.Arrays;

import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSASigner.noneDSA;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.transaction.TransferCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.Value;
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
	private Value totalSupply;
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
	 * Save the root of lively lock proof Merkel Tree.
	 */
	private byte[] livelyLockProofRoot;
	/**
	 * Save the root of Passport Merkel Tree.
	 */
	private byte[] passportProofRoot;
	/**
	 * Save the root of forbidden lock proof Merkel Tree.
	 */
	private byte[] forbiddenLockProofRoot;
	private Transaction coinbaseTransaction;
	
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
	public void parse(ByteArrayInputStream is) throws Exception {
		super.parse(is);
		totalSupply = EQCType.parseValue(is);
		totalLockNumbers = EQCType.parseID(is);
		totalPassportNumbers = EQCType.parseID(is);
		livelyLockProofRoot = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
		passportProofRoot = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
		byte[] bytes = null;
		bytes = EQCType.parseBIN(is);
		if(EQCType.isNULL(bytes)) {
			forbiddenLockProofRoot = null;
		}
		else {
			forbiddenLockProofRoot = bytes;
		}
		coinbaseTransaction = Transaction.class.getDeclaredConstructor().newInstance().Parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.subchain.EQCSubchainHeader#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		super.getBytes(os);
		os.write(totalSupply.getEQCBits());
		os.write(totalLockNumbers.getEQCBits());
		os.write(totalPassportNumbers.getEQCBits());
		os.write(livelyLockProofRoot);
		os.write(passportProofRoot);
		os.write(EQCType.bytesToBIN(forbiddenLockProofRoot));
		os.write(coinbaseTransaction.getBytes());
		return os;
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
	public Transaction getCoinbaseTransaction() {
		return coinbaseTransaction;
	}

	/**
	 * @param coinbaseTransaction the coinbaseTransaction to set
	 */
	public void setCoinbaseTransaction(Transaction coinbaseTransaction) {
		this.coinbaseTransaction = coinbaseTransaction;
	}

	protected String toInnerJson() {
		return "\"EQcoinSeedRoot\":" + "{\n"
				+ "\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\"" + ",\n"
				+ "\"TotalSupply\":" + "\"" + totalSupply.longValue() + "\"" + ",\n" 
				+ "\"TotalLockNumbers\":" + "\"" + totalLockNumbers.longValue() + "\"" + ",\n" 
				+ "\"TotalPassportNumbers\":" + "\"" + totalPassportNumbers.longValue() + "\"" + ",\n" 
				+ "\"LivelyLockProofRoot\":" + "\"" + Util.getHexString(livelyLockProofRoot) + "\"" + ",\n" 
				+ "\"PassportProofRoot\":" + "\"" + Util.getHexString(passportProofRoot) + "\"" + ",\n" 
				+ "\"ForbiddenLockProofRoot\":" + "\"" + Util.getHexString(forbiddenLockProofRoot) + "\"" + ",\n" 
				+ coinbaseTransaction.toInnerJson()
				+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		return super.isSanity() && totalSupply != null && totalSupply.isSanity()
				&& totalLockNumbers != null && totalLockNumbers.isSanity() && totalPassportNumbers != null && totalPassportNumbers.isSanity()
				&& livelyLockProofRoot != null && livelyLockProofRoot.length == Util.SHA3_512_LEN && passportProofRoot != null 
				&& passportProofRoot.length == Util.SHA3_512_LEN && (forbiddenLockProofRoot == null || (forbiddenLockProofRoot != null && forbiddenLockProofRoot.length == Util.SHA3_512_LEN)) 
				&& coinbaseTransaction != null && coinbaseTransaction.isSanity();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.seed.EQCSeedHeader#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
	 */
	@Override
	public boolean isValid() throws Exception {
		// Check if CoinbaseTransaction is need
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
		if(!super.isValid()) {
			return false;
		}
		if(!totalSupply.equals(changeLog.getStatistics().getTotalSupply())) {
			Log.Error("TotalSupply is invalid expected: " + changeLog.getStatistics().getTotalSupply() + " but actual: " + totalSupply);
			return false;
		}
		if(!totalLockNumbers.equals(changeLog.getTotalLockNumbers())) {
			Log.Error("TotalLockNumbers is invalid expected: " + changeLog.getTotalLockNumbers() + " but actual: " + totalLockNumbers);
			return false;
		}
		if(!totalPassportNumbers.equals(changeLog.getTotalPassportNumbers())) {
			Log.Error("TotalLockNumbers is invalid expected: " + changeLog.getTotalPassportNumbers() + " but actual: " + totalPassportNumbers);
			return false;
		}
		changeLog.buildProofBase();
		changeLog.generateProofRoot();
		if (!Arrays.equals(livelyLockProofRoot, changeLog.getLockProofRoot())) {
			Log.Error("LivelyLockProofRoot is invalid expected: " + Util.getHexString(changeLog.getLockProofRoot()) + " but actual: " + Util.getHexString(livelyLockProofRoot));
			return false;
		}
		if (!Arrays.equals(passportProofRoot, changeLog.getPassportProofRoot())) {
			Log.Error("PassportProofRoot is invalid expected: " + Util.getHexString(changeLog.getPassportProofRoot()) + " but actual: " + Util.getHexString(passportProofRoot));
			return false;
		}
		if (!Arrays.equals(forbiddenLockProofRoot, changeLog.getForbiddenLockProofRoot())) {
			Log.Error("ForbiddenLockProofRoot is invalid expected: " + Util.getHexString(changeLog.getForbiddenLockProofRoot()) + " but actual: " + Util.getHexString(forbiddenLockProofRoot));
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
	 * @return the livelyLockProofRoot
	 */
	public byte[] getLivelyLockProofRoot() {
		return livelyLockProofRoot;
	}

	/**
	 * @param livelyLockProofRoot the livelyLockProofRoot to set
	 */
	public void setLivelyLockProofRoot(byte[] livelyLockProofRoot) {
		this.livelyLockProofRoot = livelyLockProofRoot;
	}

	/**
	 * @return the passportProofRoot
	 */
	public byte[] getPassportProofRoot() {
		return passportProofRoot;
	}

	/**
	 * @param passportProofRoot the passportProofRoot to set
	 */
	public void setPassportProofRoot(byte[] passportProofRoot) {
		this.passportProofRoot = passportProofRoot;
	}

	/**
	 * @return the totalSupply
	 */
	public Value getTotalSupply() {
		return totalSupply;
	}

	/**
	 * @param totalSupply the totalSupply to set
	 */
	public void setTotalSupply(Value totalSupply) {
		this.totalSupply = totalSupply;
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
	}

	/**
	 * @return the forbiddenLockProofRoot
	 */
	public byte[] getForbiddenLockProofRoot() {
		return forbiddenLockProofRoot;
	}

	/**
	 * @param forbiddenLockProofRoot the forbiddenLockProofRoot to set
	 */
	public void setForbiddenLockProofRoot(byte[] forbiddenLockProofRoot) {
		this.forbiddenLockProofRoot = forbiddenLockProofRoot;
	}
	
}
