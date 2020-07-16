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
package org.eqcoin.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.velocity.runtime.directive.Parse;
import org.eqcoin.lock.witness.Witness;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 26, 2020
 * @email 10509759@qq.com
 */
public abstract class EQCSerializable implements EQCTypable, EQCInheritable {
	
	public EQCSerializable() {
		init();
	}
	
	/**
	 * If derived class hasn't any sub class can override this constructor to create
	 * new class from byte[] otherwise need override @see
	 * com.eqcoin.serialization.EQCSerializable#Parse(byte[]) to support constructor
	 * different sub class
	 * 
	 * @param bytes
	 * @throws Exception
	 */
	public EQCSerializable(byte[] bytes) throws Exception {
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCType.assertNoRedundantData(is);
	}
	
	/**
	 * If the sub class has multiple sub class then shouldn't implement this
	 * constructor and use Parse(ByteArrayInputStream is) instead this.
	 * 
	 * @param is
	 * @throws Exception
	 */
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
	 * ByteArrayInputStream.
	 * 
	 * @param is
	 * @return
	 * @throws Exception
	 */
	public <T extends EQCSerializable> T Parse(ByteArrayInputStream is) throws Exception {
		return null;
	}
	
	/**
	 * When the object which extends from EQCSerializable have multiple sub classes
	 * need implement this to support parse different sub class from the
	 * byte[].
	 * @param bytes
	 * @return
	 * @throws Exception
	 */
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
		return null;
	}
	
}
