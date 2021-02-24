/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Sep 27, 2018
 * @email 10509759@qq.com
 */
public class Lock extends EQCObject {
	protected LockType type;
	protected byte[] proof;

	public Lock() {
		super();
	}

	public Lock(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Lock Parse(ByteArrayInputStream is) throws Exception {
		Lock lock = null;
		LockType lockType = parseLockType(is);
		if(lockType == LockType.T1) {
			lock = new T1Lock(is);
		}
		else if(lockType == LockType.T2) {
			lock = new T2Lock(is);
		}
		return lock;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(byte[])
	 */
	@Override
	public Lock Parse(byte[] bytes) throws Exception {
		EQCCastle.assertNotNull(bytes);
		Lock lock = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		lock = Parse(is);
		EQCCastle.assertNoRedundantData(is);
		return lock;
	}

	private final LockType parseLockType(ByteArrayInputStream is) throws Exception {
		LockType lockType = null;
		try {
			is.mark(0);
			lockType = LockType.get(EQCCastle.parseID(is).intValue());
		} finally {
			is.reset();
		}
		return lockType;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		type = LockType.get(EQCCastle.parseID(is).intValue());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		os.write(type.getEQCBits());
		return os;
	}

	public String toInnerJson() {
		return null;
	}

	@Override
	public boolean isSanity() {
		return false;
	}

	/**
	 * @return the proof
	 */
	public byte[] getProof() {
		return proof;
	}

	/**
	 * @param proof the proof to set
	 */
	public void setProof(byte[] proof) {
		this.proof = proof;
	}

	/**
	 * @return the type
	 */
	public LockType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LockType lockType) {
		this.type = lockType;
	}
	
	public Value getGlobalStateLength() {
		return  null;
	}

	public String getReadableLock() throws Exception {
		return LockTool.EQCLockToReadableLock(this);
	}
	
}
