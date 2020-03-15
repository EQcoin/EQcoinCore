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
package com.eqcoin.blockchain.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Nov 12, 2018
 * @email 10509759@qq.com
 */
public class EQCRoot implements EQCTypable {
	/**
	 * Save the root of Passport Merkel Tree.
	 */
	private byte[] passportMerkelTreeRoot;
	/**
	 * Save the root of EQCSubchain' Merkel Tree. Including
	 * the root of TransactionList, SignatureList, PublickeyList
	 * and PassportList's Merkel Tree.
	 */
	private byte[] subchainsMerkelTreeRoot;

	public EQCRoot() {
	}

	public EQCRoot(byte[] bytes) throws NoSuchFieldException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// Parse Accounts hash
		passportMerkelTreeRoot = EQCType.parseBIN(is);

		// Parse TransactionsMerkelTreeRoot hash
		subchainsMerkelTreeRoot = EQCType.parseBIN(is);
	}

	public byte[] getHash() {
		// Reactor Here need use MerkelTree.
		return Util.EQCCHA_MULTIPLE_DUAL(getBytes(), Util.HUNDREDPULS, true, false);
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(passportMerkelTreeRoot));
			os.write(EQCType.bytesToBIN(subchainsMerkelTreeRoot));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	/**
	 * @return the accountsMerkelTreeRoot
	 */
	public byte[] getAccountsMerkelTreeRoot() {
		return passportMerkelTreeRoot;
	}

	/**
	 * @return the subchainsMerkelTreeRoot
	 */
	public byte[] getSubchainsMerkelTreeRoot() {
		return subchainsMerkelTreeRoot;
	}

	/**
	 * @param subchainsMerkelTreeRoot the subchainsMerkelTreeRoot to set
	 */
	public void setSubchainsMerkelTreeRoot(byte[] subchainsMerkelTreeRoot) {
		this.subchainsMerkelTreeRoot = subchainsMerkelTreeRoot;
	}

	/**
	 * @param accountsMerkelTreeRoot the accountsMerkelTreeRoot to set
	 */
	public void setAccountsMerkelTreeRoot(byte[] accountsMerkelTreeRoot) {
		this.passportMerkelTreeRoot = accountsMerkelTreeRoot;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" + toInnerJson() + "\n}";
	}

	public String toInnerJson() {
		return "\"Root\":" + "\n{\n"
				+ "\"EQCAccountsMerkelTreeRoot\":" + "\""
				+ Util.dumpBytes(passportMerkelTreeRoot, 16) + "\"" + ",\n" + "\"EQCSubchainsMerkelTreeRoot\":" + "\""
				+ Util.dumpBytes(subchainsMerkelTreeRoot, 16) + "\"" + "\n" + "}";
	}

	@Override
	public boolean isSanity() {
		if (passportMerkelTreeRoot == null || subchainsMerkelTreeRoot == null) {
			return false;
		}
//		if (totalSupply.compareTo(Util.MIN_EQC) < 0 || totalSupply.compareTo(Util.MAX_EQC) > 0) {
//			return false;
//		}
		if (passportMerkelTreeRoot.length != Util.HASH_LEN || subchainsMerkelTreeRoot.length != Util.HASH_LEN) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(ChangeLog changeLog) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
