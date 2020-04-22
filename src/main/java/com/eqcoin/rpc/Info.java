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
package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.avro.O;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;

/**
 * @author Xun Wang
 * @date Jun 25, 2019
 * @email 10509759@qq.com
 */
public class Info extends IO {
	private Cookie cookie;
	private Code code;
	private String message;
	
	public <T> Info(T type) throws Exception {
		super(type);
	}
	
	public Info() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(cookie == null || code == null) {
			return false;
		}
		if(!cookie.isSanity()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the cookie
	 */
	public Cookie getCookie() {
		return cookie;
	}

	/**
	 * @param cookie the cookie to set
	 */
	public void setCookie(Cookie cookie) {
		this.cookie = cookie;
	}

	/**
	 * @return the code
	 */
	public Code getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(Code code) {
		this.code = code;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		cookie = new Cookie(is);
		code = Code.get(EQCType.parseID(is).intValue());
		message = EQCType.bytesToASCIISting(EQCType.parseBIN(is));
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(cookie.getBytes());
		os.write(code.getEQCBits());
		os.write(EQCType.stringToBIN(message));
		return os;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Info [cookie=" + cookie + ", code=" + code + ", message=" + message + "]";
	}

}
