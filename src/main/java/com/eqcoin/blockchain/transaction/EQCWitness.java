/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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

import com.eqcoin.blockchain.lock.EQCPublickey;
import com.eqcoin.blockchain.lock.T1Publickey;
import com.eqcoin.blockchain.lock.T2Publickey;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * EQCWitness contains the transaction relevant witness parts for example signature.
 * 
 * @author Xun Wang
 * @date Mar 5, 2020
 * @email 10509759@qq.com
 */
public class EQCWitness extends EQCSerializable {
	
	protected byte[] eqcSignature;
	protected Transaction transaction;

	public EQCWitness() {
	}
	
	public EQCWitness(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public EQCWitness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public EQCWitness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		EQCWitness eqcWitness = null;
		if (transaction.getLockType() ==LockType.T1) {
			eqcWitness = new T1Witness(is);
		} else if (transaction.getLockType() == LockType.T2) {
			eqcWitness = new T2Witness(is);
		} else {
			throw new IllegalStateException("Bad lock type: " + transaction.getTxInLockMate().getLock().getLockType());
		}
		return (T) eqcWitness;
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(eqcSignature);
		return os.toByteArray();
	}

	@Override
	public boolean isSanity() {
		return eqcSignature != null;
	}

	/**
	 * @return the eqcSignature
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
		
				"\"EQCWitness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(eqcSignature) + "\"\n" +
				"}";
	}

	public boolean isNull() {
		return eqcSignature == null;
	}

	/**
	 * @return the eqcSignature
	 */
	public byte[] getEqcSignature() {
		return eqcSignature;
	}
	
}
