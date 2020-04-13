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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;

import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.transaction.Transaction.TransactionShape;
import com.eqcoin.serialization.EQCSerializable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class Tx extends EQCSerializable {
	
	protected ID passportId;
	protected Value value;
	
	public Tx() {
		super();
	}

	public Tx(byte[] bytes) throws Exception {
		super(bytes);
	}

	public Tx(ByteArrayInputStream is) throws Exception {
		parse(is);
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

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		passportId = ID.ZERO;
		value = new Value();
	}
	
	@Override
	public byte[] getBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(passportId.getEQCBits());
			os.write(value.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		return ((passportId != null) && passportId.isSanity() && (value != null) && value.isSanity());
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
	
}
