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
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Apr 10, 2020
 * @email 10509759@qq.com
 */
public class T1Lock extends Lock {
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		type = LockType.T1;
	}

	public T1Lock() {
		super();
	}
	
//	public T1Lock(byte[] bytes) throws Exception {
//		super(bytes);
//	}

	public T1Lock(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		proof = EQCCastle.parseNBytes(is, Util.SHA3_256_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(proof);
		return os;
	}
	
	public Value getProofLength() {
		return  new Value(Util.T1_LOCK_PROOF_SPACE_COST);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.passport.Lock#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(type == null) {
			Log.Error("lockType == null");
			return false;
		}
		if(type != LockType.T1) {
			Log.Error("lockType != LockType.T1");
			return false;
		}
		if(proof == null) {
			Log.Error("lockProof == null");
			return false;
		}
		if(proof.length != Util.SHA3_256_LEN) {
			Log.Error("lockProof.length != Util.SHA3_256_LEN");
			return false;
		}
		return true;
	}
	
	public String toInnerJson() {
		return "\"T1Lock\":" + "{\n" 
				+ "\"LockType\":" + type + ",\n"
				+ "\"PublickeyHash\":" + "\"" + Util.bytesTo512HexString(proof) + "\""
				+ "\n" + "}";
	}
	
}
