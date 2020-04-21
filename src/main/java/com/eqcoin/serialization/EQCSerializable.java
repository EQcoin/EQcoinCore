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
package com.eqcoin.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.velocity.runtime.directive.Parse;

import com.eqcoin.blockchain.transaction.EQCWitness;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Mar 26, 2020
 * @email 10509759@qq.com
 */
public abstract class EQCSerializable implements EQCTypable, EQCInheritable {
	
	public EQCSerializable() {
		init();
	}
	
	public EQCSerializable(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	public EQCSerializable(ByteArrayInputStream is) throws Exception {
		init();
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parse(java.io.ByteArrayInputStream, java.lang.Object[])
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		parseHeader(is);
		parseBody(is);
	}

	/**
	 * When the object which extends from EQCSerializable have multiple sub classes
	 * need implement this to support parse different sub class from the
	 * ByteArrayInputStream
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		return null;
	}
	
	public <T extends EQCSerializable> T Parse(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		T t = null;
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		t = Parse(is);
		EQCType.assertNoRedundantData(is);
		return t;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parseHeader(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#getHeaderBytes()
	 */
	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		return os;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		return os;
	}

	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
 		getHeaderBytes(os);
		getBodyBytes(os);
		return os;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#getBytes()
	 */
	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		return getBytes(os).toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#getBin()
	 */
	@Override
	public byte[] getBin() throws Exception {
		return EQCType.bytesToBIN(getBytes());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCTypable#isValid()
	 */
	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected void init() {
	}

}
