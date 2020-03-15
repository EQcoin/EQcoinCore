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
import java.io.IOException;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date July 30, 2019
 * @email 10509759@qq.com
 */
public class EQCSeedHeader implements EQCTypable, EQCInheritable {
	protected ID totalTxFee;
	/**
	 * Calculate this according to newTransactionList ARRAY's length
	 */
	protected ID totalTransactionNumbers;
	
	public EQCSeedHeader() {
		totalTxFee = ID.ZERO;
	}
	
	public EQCSeedHeader(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is);
	}
	
	public EQCSeedHeader(ByteArrayInputStream is) throws Exception {
		parseBody(is);
	}
	
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		totalTxFee = EQCType.parseID(is);
		totalTransactionNumbers = EQCType.parseID(is);
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(totalTxFee.getEQCBits());
		os.write(totalTransactionNumbers.getEQCBits());
		return os.toByteArray();
	}

	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getBodyBytes());
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}

	@Override
	public boolean isSanity() {
		if(totalTxFee == null || totalTransactionNumbers == null) {
			return false;
		}
		if(!totalTxFee.isSanity() || !totalTransactionNumbers.isSanity()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the totalTxFee
	 */
	public ID getTotalTxFee() {
		return totalTxFee;
	}

//	/**
//	 * @param totalTxFee the totalTxFee to set
//	 */
//	public void setTotalTxFee(ID totalTxFee) {
//		this.totalTxFee = totalTxFee;
//	}

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
	
	public void depositTxFee(long txFee) {
		totalTxFee = totalTxFee.add(ID.valueOf(txFee));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return

		"{\n" + toInnerJson() + "\n}";

	}
	
	protected String toInnerJson() {
		return "\"EQCSubchainHeader\":" + "{\n"
				+ "\"TotalTxFee\":" + "\"" + totalTxFee + "\"" + ",\n" + "\"TotalTransactionNumbers\":" + "\"" + totalTransactionNumbers + "\""
				+ "\n" + "}";
	}
	
}
