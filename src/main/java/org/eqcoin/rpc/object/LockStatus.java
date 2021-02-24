/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
 * law.
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
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date May 10, 2020
 * @email 10509759@qq.com
 */
public class LockStatus extends IO {
	private LOCKSTATUS status;
	private String readableLock;
	private ID id;
	
	public enum LOCKSTATUS {
		READABLELOCK, ID;
		public static LOCKSTATUS get(int ordinal) {
			LOCKSTATUS lockStatus = null;
			switch (ordinal) {
			case 0:
				lockStatus = LOCKSTATUS.READABLELOCK;
				break;
			case 1:
				lockStatus = LOCKSTATUS.ID;
				break;
			}
			if(lockStatus == null) {
				throw new IllegalStateException("Invalid lock status: " + lockStatus);
			}
			return lockStatus;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
	public LockStatus() {
		super();
	}
	
	public LockStatus(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public <T> LockStatus(T type) throws Exception {
		super(type);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		status = LOCKSTATUS.get(EQCCastle.parseID(is).intValue());
		if(status == LOCKSTATUS.READABLELOCK) {
			readableLock = EQCCastle.parseString(is);
		}
		else {
			id = EQCCastle.parseID(is);
		}
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(status.getEQCBits());
		if(status == LOCKSTATUS.READABLELOCK) {
			os.write(EQCCastle.stringToBIN(readableLock));
		}
		else {
			os.write(id.getEQCBits());
		}
		return os;
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(status == null) {
			Log.Error("status == null");
			return false;
		}
		if(status == LOCKSTATUS.READABLELOCK && readableLock == null) {
			Log.Error("status == LOCKSTATUS.READABLELOCK && readableLock == null");
			return false;
		}
		else if(status == LOCKSTATUS.ID && id == null) {
			Log.Error("status == LOCKSTATUS.ID && id == null");
			return false;
		}
		return true;
	}

	/**
	 * @return the type
	 */
	public LOCKSTATUS getType() {
		return status;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LOCKSTATUS type) {
		this.status = type;
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
	
}
