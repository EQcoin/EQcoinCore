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
package org.eqcoin.lock;

import java.io.ByteArrayOutputStream;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class LockMate extends EQCSerializable {
	
	private ID id;
	private Lock lock;
	private Publickey publickey;
	private ChangeLog changeLog;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		publickey = new Publickey();
	}

	public LockMate() {}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getHeaderBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBodyBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(lock.getBytes());
		os.write(publickey.getBytes());
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(id == null) {
			Log.Error("id == null");
			return false;
		}
		if(!id.isSanity()) {
			Log.Error("!id.isSanity()");
			return false;
		}
		if(lock == null) {
			Log.Error("eqcLock == null");
			return false;
		}
		if(!lock.isSanity()) {
			Log.Error("!eqcLock.isSanity()");
			return false;
		}
		if(publickey == null) {
			Log.Error("eqcPublickey == null");
			return false;
		}
		if(!publickey.isSanity()) {
			Log.Error("!eqcPublickey.isSanity()");
			return false;
		}
		return true;
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
	 * @return the lock
	 */
	public Lock getLock() {
		return lock;
	}

	/**
	 * @param lock the lock to set
	 */
	public void setLock(Lock lock) {
		this.lock = lock;
	}

	/**
	 * @return the publickey
	 */
	public Publickey getPublickey() {
		return publickey;
	}

	/**
	 * @param publickey the publickey to set
	 */
	public void setPublickey(Publickey publickey) {
		this.publickey = publickey;
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

	public String toInnerJson() {
		return "\"LockMate\":" + "{\n" + 
				"\"ID\":" + "\"" + id + "\"" + ",\n" +
				lock.toInnerJson() + ",\n" +
				publickey.toInnerJson() +
				"\n}";
	}

	/**
	 * @param changeLog the changeLog to set
	 */
	public LockMate setChangeLog(ChangeLog changeLog) {
		this.changeLog = changeLog;
		return this;
	}
	
	public void planting() throws Exception {
		changeLog.getFilter().saveLock(this);
	}
	
}
