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
package com.eqcoin.blockchain.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.acl.Owner;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.transaction.CompressedPublickey;
import com.eqcoin.rpc.TailInfo;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.serialization.EQCType.ARRAY;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * Passport table's schema after refactor meet 3NF now //does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public abstract class Passport implements EQCTypable, EQCInheritable {
	/**
	 * Header field include PassportType
	 * Passport type can also used to represent different passport type's version
	 */
	private PassportType passportType;
	/**
	 * Body field include ID, LockID, balance, nonce and updateHeight
	 */
	private ID id;
	private ID lockID;
	private ID balance;
	private ID nonce;
	private ID updateHeight;
	
	/**
	 * PassportType include EQCOINSEED Passport and ASSET Passport.
	 * 
	 * @author Xun Wang
	 * @date May 19, 2019
	 * @email 10509759@qq.com
	 */
	public enum PassportType {
		EQCOINSEED, ASSET;
		public static PassportType get(int ordinal) {
			PassportType passportTypeType = null;
			switch (ordinal) {
			case 0:
				passportTypeType = PassportType.EQCOINSEED;
				break;
			case 1:
				passportTypeType = PassportType.ASSET;
				break;
			}
			return passportTypeType;
		}
		public byte[] getEQCBits() {
			return EQCType.intToEQCBits(this.ordinal());
		}
	}
	
	public static PassportType parsepassportType(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		PassportType passportType = null;
		passportType = PassportType.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
		return passportType;
	}

	public static Passport parsePassport(byte[] bytes) throws NoSuchFieldException, IllegalStateException, IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Passport account = null;
		PassportType passportType = parsepassportType(is);

		try {
			if (passportType == PassportType.ASSET) {
				account = new AssetPassport(bytes);
			} 
			else if (passportType == passportType.EQCOINSEED) {
				account = new EQcoinSeedPassport(bytes);
			} 
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return account;
	}
	
//	public static Account parseAccount(O o) throws NoSuchFieldException, IllegalStateException, IOException {
//		return parseAccount(o.getProtocol().array());
//	}
	
	public static PassportType parsePassportType(Lock lock) {
		PassportType passportType = null;
		if(lock.getAddressType() == LockType.T1 || lock.getAddressType() == LockType.T2) {
			passportType = PassportType.ASSET;
		}
		return passportType;
	}
	
	public static Passport createAccount(Lock key) {
		Passport passport = null;
		PassportType passportTypeType = parsePassportType(key);

		try {
			if (passportTypeType == PassportType.ASSET) {
				passport = new AssetPassport();
//				passport.setKey(key);
			} else if (passportTypeType == passportTypeType.EQCOINSEED) {
				passport = null;
			} 
		} catch (UnsupportedOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return passport;
	}

	public Passport(byte[] bytes) throws NoSuchFieldException, IOException {
		EQCType.assertNotNull(bytes);
		init();
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		// Parse Header
		parseHeader(is);
		// Parse Body
 		parseBody(is);
		EQCType.assertNoRedundantData(is);
	}
	
	@Override
	public void parseHeader(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
 		passportType = PassportType.get(EQCType.parseID(is).intValue());
	}
	
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		// Parse LockID
		lockID = EQCType.parseID(is);
		// Parse Balance
		balance = EQCType.parseID(is);
		// Parse Nonce
		nonce = EQCType.parseID(is);
		// Parse Update Height
		updateHeight = EQCType.parseID(is);
	}
	
	private void init() {
		balance = ID.ZERO;
		nonce = ID.ZERO;
		updateHeight = ID.ZERO;
	}
	
	public Passport(PassportType passportTypeType) {
		super();
		this.passportType = passportTypeType;
		init();
	}
	
	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(getHeaderBytes());
			os.write(getBodyBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}
	@Override
	public byte[] getHeaderBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(passportType.getEQCBits());
		} catch (IOException e) {
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	@Override
	public byte[] getBodyBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			os.write(id.getEQCBits());
			os.write(lockID.getEQCBits());
			os.write(balance.getEQCBits());
			os.write(nonce.getEQCBits());
			os.write(updateHeight.getEQCBits());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}
	
	public byte[] getHash() throws Exception {
//		if(hash == null) {
//			hash = Util.EQCCHA_MULTIPLE_DUAL(getHashBytes(soleUpdate), Util.HUNDREDPULS, true, false);
//		}
		return null;
	}
	
	/**
	 * @return the passportType
	 */
	public PassportType getpassportType() {
		return passportType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{\n" +
				toInnerJson() +
				"\n}";
	}

	public String toInnerJson() {
		return 
					"\"PassportType\":" + "\"" + passportType + "\"" + ",\n" +
					"\"ID\":" + "\"" + id + "\"" + ",\n" +
					"\"LockID\":" + "\"" + lockID + "\"" + ",\n" +
					"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
					"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
					"\"UpdateHeight\":" + "\"" + updateHeight + "\"" + 
					"\n}";
	}
	
	/**
	 * Body field include ID, LockID, totalIncome, totalCost, balance, nonce
	 */
	
	@Override
	public boolean isSanity() {
		if(passportType == null || id == null || lockID == null || balance == null || nonce == null || updateHeight == null) {
			return false;
		}
		if(id.isSanity() || lockID.isSanity() || balance.isSanity() || nonce.isSanity() || updateHeight.isSanity()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid(ChangeLog changeLog) {
		// TODO Auto-generated method stub
		return false;
	}

	public O getO() {
		return new O(ByteBuffer.wrap(getBytes()));
	}
	
	/**
	 * For system's security here need check if balance is enough for example SmartContract maybe provide wrong input amount
	 * @param amount
	 */
	public void withdraw(ID amount) {
		EQCType.assertNotBigger(amount, balance);
		balance = balance.subtract(amount);
	}
	
	public void deposit(ID amount) {
		EQCType.assertNotBigger(amount, Util.MAX_EQcoin);
		balance = balance.add(amount);
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * @return the lockID
	 */
	public ID getLockID() {
		return lockID;
	}

	/**
	 * @param lockID the lockID to set
	 */
	public void setLockID(ID lockID) {
		this.lockID = lockID;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

//	/**
//	 * @param nonce the nonce to set
//	 */
//	public void setNonce(ID nonce) {
//		this.nonce = nonce;
//	}

	public void increaseNonce() {
		nonce = nonce.getNextID();
	}
	
	/**
	 * @return the balance
	 */
	public ID getBalance() {
		return balance;
	}
	
}
