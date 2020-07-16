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
package org.eqcoin.transaction.txout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.lock.LockMate;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 30, 2020
 * @email 10509759@qq.com
 */
public class TransferTxOut extends EQCSerializable {
	private ID passportId;
	private Value value;
	
	public TransferTxOut(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public TransferTxOut(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public TransferTxOut() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		// Parse Passport ID
		passportId = EQCType.parseID(is);
		// Parse Value
		value = EQCType.parseValue(is);
	}
	
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
			os.write(passportId.getEQCBits());
			os.write(value.getEQCBits());
		return os;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public TransferTxOut Parse(ByteArrayInputStream is) throws Exception {
		return new TransferTxOut(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(passportId == null) {
			Log.Error("passportId == null");
			return false;
		}
		if(!passportId.isSanity()) {
			Log.Error("!passportId.isSanity()");
			return false;
		}
		if(value == null) {
			Log.Error("value == null");
			return false;
		}
		if(!value.isSanity()) {
			Log.Error("!value.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * @return the passportId
	 */
	public ID getPassportId() {
		return passportId;
	}

	/**
	 * @param passportId the passportId to set
	 */
	public void setPassportId(ID passportId) {
		this.passportId = passportId;
	}

	/**
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 * @throws NoSuchFieldException 
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	public String toInnerJson() {
		return 
		"\"TransferTxOut\":" + 
		"\n{" +
		"\"PassportId\":" + passportId + ",\n" +
		"\"Value\":" + "\"" +  Long.toString(value.longValue()) + "\"" + "\n" +
		"}";
	}
	
}
