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
package com.eqcoin.blockchain.seed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Util;

/**
 * EQCWitness contains the transaction relevant witness parts for example signature.
 * 
 * @author Xun Wang
 * @date Mar 5, 2020
 * @email 10509759@qq.com
 */
public class EQCWitness implements EQCTypable, EQCInheritable {
	
	private byte[] signature;

	public EQCWitness() {
	}
	
	public EQCWitness(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public EQCWitness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		signature = EQCType.parseBIN(is);
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws IOException  {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(signature);
		return os.toByteArray();
	}

	@Override
	public byte[] getBytes() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(getBodyBytes());
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() throws IOException {
		return EQCType.bytesToBIN(getBytes());
	}

	@Override
	public boolean isSanity() {
		if(signature == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the signature
	 */
	public byte[] getSignature() {
		return signature;
	}

	/**
	 * @param signature the signature to set
	 */
	public void setSignature(byte[] signature) {
		this.signature = signature;
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
		
				"\"EQCSegWit\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.getHexString(signature) + "\"\n" +
				"}";
		
	}

	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		parseBody(is);
	}

}
