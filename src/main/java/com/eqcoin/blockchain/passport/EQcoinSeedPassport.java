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
package com.eqcoin.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Passport.PassportType;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Jun 22, 2019
 * @email 10509759@qq.com
 */
public class EQcoinSeedPassport extends Passport {
	private ID totalSupply;
	private ID totalLockNumbers;
	private ID totalPassportNumbers;
	/**
	 * Record all transaction's numbers include EQcoin transaction,
	 * SharedSmartContractTransaction and MixedSmartContractTion.
	 */
	private ID totalTransactionNumbers;
	/**
	 * Body field include TxFeeRate
	 */
	// Here maybe need more design only record the txFeeRate isn't enough? If need record the history ?
	private byte txFeeRate;
	// Here maybe need more design only record the height and hash isn't enough If need record the history ?
	private ID checkPointHeight;
	private byte[] checkPointHash;

	public EQcoinSeedPassport() {
		super(PassportType.EQCOINSEED);
	}
	
	public EQcoinSeedPassport(byte[] bytes) throws NoSuchFieldException, IOException {
		super(bytes);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// TODO Auto-generated method stub
		super.parseBody(is);
		// Parse TxFeeRate
		txFeeRate = EQCType.parseBIN(is)[0];
		// Parse CheckPoint Height
		checkPointHeight = EQCType.parseID(is);
		// Parse CheckPoint Hash
		checkPointHash = EQCType.parseBIN(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#getBodyBytes()
	 */
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(super.getBodyBytes());
			os.write(EQCType.bytesToBIN(new byte[]{txFeeRate}));
			os.write(checkPointHeight.getEQCBits());
			os.write(EQCType.bytesToBIN(checkPointHash));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"EQcoinSubchainAccount\":" + 
				"\n{\n" +
					super.toInnerJson() + ",\n" +
					"\"TxFeeRate\":" + "\"" + txFeeRate + "\"" +
				"\n}";
	}
	
	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(!super.isSanity()) {
			return false;
		}
		if(checkPointHeight == null || checkPointHash == null) {
			return false;
		}
		if(!checkPointHeight.isSanity() || checkPointHash.length != 64) {
			return false;
		}
		if(txFeeRate < 1 || txFeeRate >10) {
			return false;
		}
		return true;
	}

	/**
	 * @return the txFeeRate
	 */
	public byte getTxFeeRate() {
		return txFeeRate;
	}

	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(byte txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}

	/**
	 * @return the checkPointHash
	 */
	public byte[] getCheckPointHash() {
		return checkPointHash;
	}

	/**
	 * @param checkPointHash the checkPointHash to set
	 */
	public void setCheckPointHash(byte[] checkPointHash) {
		this.checkPointHash = checkPointHash;
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
	 * @return the totalTransactionNumbers
	 */
	public ID getTotalTransactionNumbers() {
		return totalTransactionNumbers;
	}

	/**
	 * @param totalTransactionNumbers the totalTransactionNumbers to set
	 */
	public void setTotalTransactionNumbers(ID totalTransactionNumbers) {
		this.totalTransactionNumbers = totalTransactionNumbers;
	}
	
}
