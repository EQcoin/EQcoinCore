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
import java.util.Comparator;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.passport.Lock.LockShape;
import com.eqcoin.serialization.EQCLockShapeInheritable;
import com.eqcoin.serialization.EQCLockShapeTypable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class Tx implements Comparator<Tx>, Comparable<Tx>, EQCLockShapeTypable, EQCLockShapeInheritable {
	/**
	 * Due to also need use Tx deploy smartcontract so here need use lock to keep the code field.
	 */
	protected Lock lock;
//	protected ID passportId;
	protected long value;
	
	public Tx() {
		lock = new Lock();
	}

	public Tx(byte[] bytes, LockShape lockShape) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseBody(is, lockShape);
		EQCType.assertNoRedundantData(is);
	}

	public Tx(ByteArrayInputStream is, LockShape lockShape) throws Exception {
		parseBody(is, lockShape);
	}
	
	/**
	 * @return the Lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @param Lock the Lock to set
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
	}

//	/**
//	 * @return the passportId
//	 */
//	public ID getPassportId() {
//		return passportId;
//	}
//
//	/**
//	 * @param passportId the passportId to set
//	 */
//	public void setPassportId(ID passportId) {
//		this.passportId = passportId;
//	}

	/**
	 * @return the value
	 */
	public long getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 * @throws NoSuchFieldException 
	 */
	public void setValue(long value) {
		this.value = value;
	}
	
	public void addValue(long value) {
		this.value += value;
	}
	
	public void subtractValue(long value) {
		this.value -= value;
	}

	/**
	 * When AddressShpae is ID
	 * Get the Txin's bytes for storage it in the EQC block chain
	 * For save the space the Address' shape is Serial Number which is the EQCBits type.
	 * 
	 * @return byte[]
	 * @throws Exception 
	 */
	public byte[] getBytes(LockShape lockShape) throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getBodyBytes(lockShape));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((address == null) ? 0 : address.hashCode());
//		result = prime * result + ((value == null) ? 0 : value.hashCode());
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Tx other = (Tx) obj;
//		if (address == null) {
//			if (other.address != null)
//				return false;
//		} else if (!address.equals(other.address))
//			return false;
//		if (value == null) {
//			if (other.value != null)
//				return false;
//		} else if (!value.equals(other.value))
//			return false;
//		return true;
//	}

	@Override
	public byte[] getBin(LockShape lockShape) throws Exception {
		byte[] bin = null;
		// Due to the EQCBits itself is embedded in the key-value pair so here need use getBytes
		if(lockShape == LockShape.ID) {
			bin = getBytes(lockShape);
		}
		else {
			bin = EQCType.bytesToBIN(getBytes(lockShape));
		}
		return bin;
	}
	
	@Override
	public boolean isSanity(LockShape lockShape) {
		if(lockShape == LockShape.ID) {
			if(lock == null || !lock.getId().isSanity()) {
				return false;
			}
		}
		else if(lockShape == LockShape.READABLE) {
			if(lock == null || !lock.isSanity(LockShape.READABLE) || !lock.isGood()) {
				return false;
			}
		}
		
		if(value <= 0 || value >= Util.MAX_EQC) {
			return false;
		}
		
		return true;
	}

	@Override
	public int compareTo(Tx o) {
		// TODO Auto-generated method stub
		return lock.getReadableLock().compareTo(o.getLock().getReadableLock());
	}

	@Override
	public int compare(Tx o1, Tx o2) {
		// TODO Auto-generated method stub
		return o1.getLock().getReadableLock().compareTo(o2.getLock().getReadableLock());
	}

	@Override
	public void parseBody(ByteArrayInputStream is, LockShape lockShape) throws NoSuchFieldException, IOException, NoSuchFieldException, IllegalStateException {
		// Parse Address
		if(lockShape == LockShape.ID) {
			lock = new Lock();
			lock.setId(new ID(EQCType.parseEQCBits(is)));
		}
		else 	if(lockShape == LockShape.READABLE) {
			lock = new Lock(EQCType.bytesToASCIISting(EQCType.parseBIN(is)));
		}
		// Parse Value
		value = EQCType.eqcBitsToLong(EQCType.parseEQCBits(is));
	}

	@Override
	public byte[] getHeaderBytes(LockShape lockShape) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes(LockShape lockShape) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			if(lockShape == LockShape.ID) {
				os.write(lock.getBin(LockShape.ID));
			}
			else {
				os.write(lock.getBin(LockShape.READABLE));
			}
			os.write(EQCType.longToEQCBits(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is, LockShape lockShape)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	public boolean compare(Tx tx) {
		if(!lock.compare(tx.getLock())) {
			return false;
		}
		if(value != tx.getValue()) {
			return false;
		}
		return true;
	}
	
}
