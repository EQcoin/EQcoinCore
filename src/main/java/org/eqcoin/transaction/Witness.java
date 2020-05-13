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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.lock.Publickey;
import org.eqcoin.lock.T1Publickey;
import org.eqcoin.lock.T2Publickey;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCInheritable;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * Witness contains the transaction relevant witness parts for example signature.
 * 
 * @author Xun Wang
 * @date Mar 5, 2020
 * @email 10509759@qq.com
 */
public class Witness extends EQCSerializable {
	
	protected byte[] signature;
	protected Transaction transaction;

	public Witness() {
	}
	
	public Witness(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Witness Parse(ByteArrayInputStream is) throws Exception {
		Witness witness = null;
		if (transaction.getLockType() ==LockType.T1) {
			witness = new T1Witness(is);
		} else if (transaction.getLockType() == LockType.T2) {
			witness = new T2Witness(is);
		} else {
			throw new IllegalStateException("Bad lock type: " + transaction.getTxInLockMate().getLock().getLockType());
		}
		return witness;
	}

	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws IOException {
		os.write(signature);
		return os;
	}

	/**
	 * @return the DERSignature
	 * @throws Exception 
	 */
	public byte[] getDERSignature() throws Exception {
		return null;
	}

	/**
	 * @param derSignature the derSignature to set
	 * @throws Exception 
	 */
	public void setDERSignature(byte[] derSignature) throws Exception {
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return 
		
				"{\n" +
				toInnerJson() +
				"\n}";
		
	}
	
	public String toInnerJson() {
		return 
		
				"\"Witness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(signature) + "\"\n" +
				"}";
	}

	public boolean isNull() {
		return signature == null;
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}
	
	public byte[] getProof() {
		return null;
	}
	
}
