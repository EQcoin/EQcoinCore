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
import java.security.acl.Owner;
import java.util.Arrays;
import javax.print.attribute.standard.RequestingUserName;

import com.eqcoin.blockchain.lock.LockTool.LockType;
import com.eqcoin.blockchain.transaction.Value;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class EQCLock extends EQCSerializable {
	/*
	 * LockShape enum which expressed three types of Lock: Readable, ID and AI.
	 * Readable Lock used for signature the Transaction and RPC for example sign the
	 * Transaction then send the Transaction's bytes to EQC Transaction network. AI
	 * Lock used for store the Lock in LockList in the blockchain. ID Lock used for
	 * EQC Passport for example save the Passport and relevant Lock into EQC
	 * blockchain.
	 */
//	public enum LockShape {
//		READABLE, AI, ID, FULL
//	}

//	/**
//	 * The id represent Passport's ID in Lock which retrieve from Transaction's TxOut.
//	 * The id represent Lock's ID in Lock which retrieve from Lock table.
//	 */
//	private ID id;
//	private String readableLock;
//	private ID passportId;
//	private byte[] publickey;
	protected LockType lockType;
	protected byte[] lockCode;

	public EQCLock() {
		super();
	}

	public EQCLock(byte[] bytes) throws Exception {
		super(bytes);
	}

	public EQCLock(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub
		return super.Parse(is);
	}

	public static EQCLockMate parseLock(String readableLock) throws Exception {
		if(!LockTool.isReadableLockSanity(readableLock)) {
			throw	new IllegalStateException("Readable lock isn't sanity: " + readableLock);
		}
		EQCLockMate lock = null;
		byte[] aiLock = LockTool.readableLockToAI(readableLock);
//		lockType = LockTool.getLockTypeFromAI(aiLock);
		return lock;
	}
	
	public static EQCLock parseEQCLock(ByteArrayInputStream is) throws Exception {
		EQCLock lock = null;
		LockType lockType = parseLockType(is);
		if(lockType == LockType.T1) {
			lock = new T1Lock(is);
		}
		else if(lockType == LockType.T2) {
			lock = new T2Lock(is);
		}
		else {
			throw new IllegalStateException("Wrong lock type: " + lockType);
		}
		return lock;
	}
	
	public static LockType parseLockType(ByteArrayInputStream is) throws Exception {
		LockType lockType = null;
		is.mark(0);
		lockType = LockType.get(EQCType.parseID(is).intValue());
		is.reset();
		return lockType;
	}
	
//	public int getBillingSize() throws Exception {
//		int size = 0;
//		size += EQCType.bytesToBIN(Util.LockTool.readableLockToAI(readableLock)).length;
//		if (code != null) {
//			size += EQCType.bytesToBIN(code).length;
//		}
//		size += EQCType.getEQCTypeOverhead(size);
//		return size;
//	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		lockType = lockType.get(EQCType.parseID(is).intValue());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(lockType.getEQCBits());
		return os;
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
		return null;
//		return "\"Passport\":" + "{\n" 
//				+ "\"ID\":" + ((id == null) ? null : "\"" + id + "\"") + ",\n"
//				+ "\"readableLock\":" + ((readableLock == null) ? null : "\"" + readableLock + "\"") + ",\n" 
//				+ "\"passportId\":" + ((passportId == null) ? null : "\"" + passportId + "\"") + ",\n" 
//				+ "\"Publickey\":" + ((publickey == null) ? null : "\"" + Util.getHexString(publickey) + "\"") + ",\n" 
//				+ "\"Code\":" + ((code == null)?null:"\"" + Util.getHexString(code) + "\"")
//				+ "\n" + "}";
	}

	public boolean isGood() {
		return isGood(null);
	}

	public boolean isGood(EQCPublickey eqcPublickey) {
//		if (aiLock == null) {
//			return false;
//		}
//		
//		LockTool.LockType lockType = Util.LockTool.getLockType(readableLock);
//
//		// Check readableLock's format is valid
//		if(!LockTool.isAddressFormatValid(readableLock)) {
//			return false;
//		}
//
//		// Check Lock type, CRC32 checksum and generated from Publickey is valid
//		if (lockType == LockType.T1 || lockType == LockType.T2) {
//			if (!LockTool.verifyAddressCRC32C(readableLock)) {
//				return false;
//			}
//			if (eqcPublickey != null) {
//				if (!LockTool.verifyLockAndPublickey(readableLock, eqcPublickey.getPublickey())) {
//					return false;
//				}
//			}
//		}
//		// The others Lock Type is invalid
//		else {
//			return false;
//		}

		return true;
	}

	@Override
	public boolean isSanity() {
//		// Here exists one bug need check if code is null due to in mvp phase the code
//		// should be null
//		LockType lockType = getLockType();
//		if (lockType != LockType.T1 && lockType != LockType.T2) {
//			return false;
//		}
//		if(code != null) {
//			return false;
//		}
//		
//		// Here maybe exists bugs in each case if need check the other fields is null?
//		if (lockShape == LockShape.AI || lockShape == LockShape.READABLE) {
//			if(id !=null | passportId != null || publickey != null || code != null) {
//				return false;
//			}
//			if (readableLock == null) {
//				return false;
//			}
//			// In the future also need check the code
//		} else if (lockShape == LockShape.ID) {
//			if(readableLock !=null | passportId != null || publickey != null || code != null) {
//				return false;
//			}
//			if (id == null || !id.isSanity()) {
//				return false;
//			}
//		}
//		else if(lockShape == LockShape.FULL) {
//			if (id == null || !id.isSanity() || readableLock == null || passportId == null || !passportId.isSanity()) {
//				return false;
//			}
//			// In the future also need check the code
//		}
//		else {
//			return false;
//		}

		return true;
	}

	/**
	 * @return the lockCode
	 */
	public byte[] getLockCode() {
		return lockCode;
	}

	/**
	 * @param lockCode the lockCode to set
	 */
	public void setLockCode(byte[] lockCode) {
		this.lockCode = lockCode;
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
	
	public Value getProofLength() {
		return  Value.ZERO;
	}

	public String getReadableLock() throws Exception {
		return LockTool.EQCLockToReadableLock(this);
	}
	
}
