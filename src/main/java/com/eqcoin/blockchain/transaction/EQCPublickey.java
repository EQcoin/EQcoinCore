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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Arrays;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.passport.Lock.LockShape;
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
public class EQCPublickey implements EQCTypable {
	/**
	 * The No. Passport relevant publickey which is BINX type.
	 */
	private byte[] publickey = null;
	private boolean isNew;
	private ChangeLog changeLog;

	/**
	 * Publickey relevant ID used to verify Publickey
	 */
	private ID id;
	
	private LockType lockType;
	
	public EQCPublickey() {
		super();
		isNew = false;
	}

	public EQCPublickey(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public EQCPublickey(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parse(is);
	}
	
	private void parse(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		// Parse publicKey
		publickey = EQCType.parseBIN(is);
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

	public int getBillingSize() {
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

	/**
	 * @return the isNew
	 */
	public boolean isNew() {
		return isNew;
	}

	/**
	 * @param isNew the isNew to set
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	
	@Override
	public boolean isSanity() {
		if (publickey == null || lockType == null) {
			return false;
		}
		if(lockType != LockType.T1 && lockType != LockType.T2) {
			return false;
		}
		if(lockType == LockType.T1 && publickey.length != Util.P256_PUBLICKEY_LEN) {
			return false;
		}
		else if(lockType == LockType.T2 && publickey.length != Util.P521_PUBLICKEY_LEN) {
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(publickey);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EQCPublickey other = (EQCPublickey) obj;
		if (!Arrays.equals(publickey, other.publickey))
			return false;
		return true;
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
		return "\"EQCPublickey\":" + "\"" + Util.dumpBytes(publickey, 16) + "\"";
	}

	@Override
	public boolean isValid() throws Exception {
		Lock lock = changeLog.getFilter().getLock(LockTool.AIToAddress(LockTool.publickeyToAI(publickey)), true);
		if(lock.getPublickey() == null) {
			if(!LockTool.verifyLockAndPublickey(lock.getReadableLock(), publickey)) {
				return false;
			}
		}
		else {
			if(!Arrays.equals(lock.getPublickey(), publickey)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isNULL() {
		return publickey == null;
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * @return the lockType
	 */
	public LockType getLockType() {
		return lockType;
	}

	/**
	 * @param lockType the lockType to set
	 */
	public void setLockType(LockType lockType) {
		this.lockType = lockType;
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public void setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
	}
	
}
