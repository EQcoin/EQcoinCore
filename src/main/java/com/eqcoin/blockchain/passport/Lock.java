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
import java.security.acl.Owner;
import java.util.Arrays;

import javax.print.attribute.standard.RequestingUserName;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.blockchain.transaction.CompressedPublickey;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.serialization.EQCLockShapeTypable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Lock implements EQCLockShapeTypable {
	/*
	 * LockShape enum which expressed three types of Lock: Readable, ID and AI.
	 * Readable Lock used for signature the Transaction and RPC for example sign the
	 * Transaction then send the Transaction's bytes to EQC Transaction network. AI
	 * Lock used for store the Lock in LockList in the blockchain. ID Lock used for
	 * EQC Passport for example save the Passport and relevant Lock into EQC
	 * blockchain.
	 */
	public enum LockShape {
		READABLE, AI, ID, FULL
	}

	private ID id;
	private String readableLock;
	private ID passportId;
	private byte[] publickey;
	private byte[] code;

	/**
	 * @param id
	 * @param readableLock
	 * @param passportId
	 * @param publickey
	 * @param code
	 */
	public Lock(ID id, String readableLock, ID passportId, byte[] publickey, byte[] code) {
		super();
		this.id = id;
		this.readableLock = readableLock;
		this.passportId = passportId;
		this.publickey = publickey;
		this.code = code;
	}

	public Lock() {
	}
	
	public Lock(String readableLock) {
		this.readableLock = readableLock;
	}

	/**
	 * Create Lock according to the bytes from EQC blockchain's Transactions' newHelixList.
	 * 
	 * @param bytes
	 * @throws IOException
	 * @throws NoSuchFieldException
	 */
	public Lock(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseLock(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public Lock(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		parseLock(is);
	}

	private void parseLock(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		// Parse Lock
		readableLock = Util.LockTool.AIToAddress(EQCType.parseBIN(is));

//		// Parse Code
//		data = null;
//		if ((data = EQCType.parseBIN(is)) != null) {
//			setCode(data);
//		}
	}
	
	/**
	 * Get the Lock' whole bytes include Address' AI value and relevant code(if
	 * any). For storage Lock in newHelixList.
	 * 
	 * @return byte[]
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(EQCType.bytesToBIN(LockTool.addressToAI(readableLock)));
			if (code != null) {
				os.write(EQCType.bytesToBIN(code));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/**
	 * Get the Lock' whole bin include Address' AI value and relevant code(if
	 * any). For storage Lock in newHelixList.
	 * 
	 * @return byte[]
	 */
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	public int getBillingSize() {
		int size = 0;
		size += EQCType.bytesToBIN(Util.LockTool.addressToAI(readableLock)).length;
		if (code != null) {
			size += EQCType.bytesToBIN(code).length;
		}
		size += EQCType.getEQCTypeOverhead(size);
		return size;
	}

	public LockType getAddressType() {
		return Util.LockTool.getAddressType(readableLock);
	}
	
	/**
	 * @return the ID
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param ID the ID to set
	 * @throws NoSuchFieldException 
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * @return the readableLock
	 */
	public String getReadableLock() {
		return readableLock;
	}

	/**
	 * @param readableLock the readableLock to set
	 */
	public void setReadableLock(String readableLock) {
		this.readableLock = readableLock;
	}

	/**
	 * @return the code
	 */
	public byte[] getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(byte[] code) {
		this.code = code;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readableLock == null) ? 0 : readableLock.hashCode());
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
		Lock other = (Lock) obj;
		if (readableLock == null) {
			if (other.readableLock != null)
				return false;
		} else if (!readableLock.equals(other.readableLock))
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
		return "\"Passport\":" + "{\n" 
				+ "\"ID\":" + ((id == null) ? null : "\"" + id + "\"") + ",\n"
				+ "\"readableLock\":" + ((readableLock == null) ? null : "\"" + readableLock + "\"") + ",\n" 
				+ "\"passportId\":" + ((passportId == null) ? null : "\"" + passportId + "\"") + ",\n" 
				+ "\"Publickey\":" + ((publickey == null) ? null : "\"" + Util.getHexString(publickey) + "\"") + ",\n" 
				+ "\"Code\":" + ((code == null)?null:"\"" + Util.getHexString(code) + "\"")
				+ "\n" + "}";
	}

	public boolean isGood() {
		return isGood(null);
	}

	public boolean isGood(CompressedPublickey compressedPublickey) {
		if (readableLock == null) {
			return false;
		}
		
		LockTool.LockType lockType = Util.LockTool.getAddressType(readableLock);

		// Check readableLock's format is valid
		if(LockTool.isAddressFormatValid(readableLock)) {
			return false;
		}

		// Check Lock type, CRC32 checksum and generated from Publickey is valid
		if (lockType == LockType.T1 || lockType == LockType.T2) {
			if (!LockTool.verifyAddressCRC32C(readableLock)) {
				return false;
			}
			if (compressedPublickey != null) {
				if (!LockTool.verifyAddressPublickey(readableLock, compressedPublickey.getCompressedPublickey())) {
					return false;
				}
			}
		}
		// The others Lock Type is invalid
		else {
			return false;
		}

		return true;
	}

	@Override
	public boolean isSanity(LockShape lockShape) {
		// Here exists one bug need check if code is null due to in mvp phase the code
		// should be null
		LockType lockType = getAddressType();
		if (lockType != LockType.T1 && lockType != LockType.T2) {
			return false;
		}
		if(code != null) {
			return false;
		}
		
		// Here maybe exists bugs in each case if need check the other fields is null?
		if (lockShape == LockShape.AI || lockShape == LockShape.READABLE) {
			if(id !=null | passportId != null || publickey != null || code != null) {
				return false;
			}
			if (readableLock == null) {
				return false;
			}
			// In the future also need check the code
		} else if (lockShape == LockShape.ID) {
			if(readableLock !=null | passportId != null || publickey != null || code != null) {
				return false;
			}
			if (id == null || !id.isSanity()) {
				return false;
			}
		}
		else if(lockShape == LockShape.FULL) {
			if (id == null || !id.isSanity() || readableLock == null || passportId == null || !passportId.isSanity()) {
				return false;
			}
			// In the future also need check the code
		}
		else {
			return false;
		}

		return true;
	}

	public byte[] getAddressAI() {
		return LockTool.addressToAI(readableLock);
	}
	
	/**
	 * Get the Address' bytes according to it's AddressShape(READABLE, AI, ID). For
	 * create the Transaction for storage it in the EQC block chain when
	 * lockShape is ID or for create the Transaction for send it to the EQC miner
	 * network when lockShape is READABLE.
	 * 
	 * @param lockShape
	 * @return byte[]
	 */
	public byte[] getBytes(LockShape lockShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if (lockShape == Lock.LockShape.ID) {
				os.write(id.getEQCBits());
			} else if (lockShape == Lock.LockShape.READABLE) {
//				Log.info(Util.dumpBytes(EQCType.stringToASCIIBytes(readableLock), 16));
				os.write(EQCType.stringToASCIIBytes(readableLock));
			} else if (lockShape == Lock.LockShape.AI) {
				os.write(LockTool.addressToAI(readableLock));
			}
			else if(lockShape == LockShape.FULL) {
				os.write(id.getEQCBits());
				os.write(EQCType.stringToBIN(readableLock));
				os.write(passportId.getEQCBits());
				if(publickey != null) {
					os.write(EQCType.bytesToBIN(publickey));
				}
				else {
					os.write(EQCType.NULL);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	/**
	 * Get the Address' bin according to it's AddressShape(READABLE, AI, ID). For
	 * create the Transaction for storage it in the EQC block chain when
	 * lockShape is ID or for create the Transaction for send it to the EQC miner
	 * network when lockShape is READABLE.
	 * 
	 * @param lockShape
	 * @return byte[]
	 */
	public byte[] getBin(LockShape lockShape) {
		byte[] bin = null;
		// Due to EQCBits bytes is BIN type also so here just get it
		if(lockShape == LockShape.ID) {
			bin = getBytes(lockShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(lockShape));
		}
		return bin;
	}
	
	@Override
	public boolean isValid(ChangeLog changeLog) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean compare(Lock lock) {
//		if(!id.equals(lock.getId())) {
//			return false;
//		}
		if(!readableLock.equals(lock.getReadableLock())) {
			return false;
		}
		if(code != null) {
			return false;
		}
		if(lock.getCode() != null) {
			return false;
		}
		return true;
	}

	/**
	 * @return the passportId
	 */
	public ID getPassportId() {
		return passportId;
	}

	/**
	 * @param passportId the passportId to set
	 */
	public void setPassportId(ID passportId) {
		this.passportId = passportId;
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
	
}
