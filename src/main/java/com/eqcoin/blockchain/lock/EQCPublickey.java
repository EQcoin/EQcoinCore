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
package com.eqcoin.blockchain.lock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.transaction.ModerateOPTransaction;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.TransferTransaction;
import com.eqcoin.blockchain.transaction.ZeroZionCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.ZionCoinbaseTransaction;
import com.eqcoin.blockchain.transaction.ZionOPTransaction;
import com.eqcoin.blockchain.transaction.ZionTransaction;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * The SegWit contains the compressed public key corresponding to the
 * specific Passport. The public key can be verified with any transaction
 * corresponding to the relevant lock's readableLock.
 * SegWit could contains the SmartContract's receipt&log also.
 * 
 * @author Xun Wang
 * @date Sep 28, 2018
 * @email 10509759@qq.com
 */
@Deprecated
public class EQCPublickey extends EQCSerializable {
	/**
	 * The No. Passport relevant publickey which is BINX type.
	 */
	protected byte[] publickey;
	protected Transaction transaction;
	
	public EQCPublickey() {
	}

	public EQCPublickey(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public EQCPublickey(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public EQCPublickey(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	public void parse(ByteArrayInputStream is) throws Exception {
		throw new IllegalStateException("Shouldn't call this to constructor new EQCPublickey");
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		EQCPublickey eqcPublickey = null;
		if (transaction.getTxInLockMate().getLock().getLockType() ==LockType.T1) {
			eqcPublickey = new T1Publickey(is);
		} else if (transaction.getTxInLockMate().getLock().getLockType() == LockType.T2) {
			eqcPublickey = new T2Publickey(is);
		} else {
			throw new IllegalStateException("Bad lock type: " + transaction.getTxInLockMate().getLock().getLockType());
		}
		return (T) eqcPublickey;
	}
	
	/**
	 * Get the PublicKey's bytes for storage it in the EQC block chain.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(publickey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	public int getBillingLength() {
		int size = 0;
		size += publickey.length;
		size += EQCType.getEQCTypeOverhead(size);
	    return size;
	}

	/**
	 * @return the publickey
	 */
	public byte[] getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(byte[] publickey) {
		this.publickey = publickey;
	}

	/**
	 * Get the PublicKey's BIN for storage it in the EQC block chain.
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 */
	@Override
	public byte[] getBin() {
		// TODO Auto-generated method stub
		return EQCType.bytesToBIN(getBytes());
	}

	@Override
	public boolean isSanity() {
//		if (publickey == null || lockType == null) {
//			return false;
//		}
//		if(lockType != LockType.T1 && lockType != LockType.T2) {
//			return false;
//		}
//		if(lockType == LockType.T1 && publickey.length != Util.P256_PUBLICKEY_LEN) {
//			return false;
//		}
//		else if(lockType == LockType.T2 && publickey.length != Util.P521_PUBLICKEY_LEN) {
//			return false;
//		}
		return false;
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
		return "\"EQCPublickey\":" + "\"" + Util.getHexString(publickey) + "\"";
	}

	@Override
	public boolean isValid() throws Exception {
		return (transaction.getTxInLockMate().getEqcPublickey().isNULL() && LockTool.verifyEQCLockAndPublickey(transaction.getTxInLockMate().getLock(), publickey));
	}
	
	public boolean isNULL() {
		return publickey == null;
	}

}
