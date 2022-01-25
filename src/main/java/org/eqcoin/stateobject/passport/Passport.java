/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection 
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
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
package org.eqcoin.stateobject.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.sql.ResultSet;

import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.lock.Lock;
import org.eqcoin.lock.LockTool.LockType;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.StateObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * Passport table's schema after refactor meet 3NF now //does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public class Passport extends StateObject {// implements Externalizable {
	/**
	 * PassportType include EQCOINSEED Passport and ASSET Passport.
	 *
	 * @author Xun Wang
	 * @date May 19, 2019
	 * @email 10509759@qq.com
	 */
	public enum PassportType {
		ASSET, EXTENDABLEASSET;// , SMARTCONTRACT, EXTENDABLESMARTCONTRACT;
		public static PassportType get(final int ordinal) {
			PassportType passportTypeType = null;
			switch (ordinal) {
			case 0:
				passportTypeType = PassportType.ASSET;
				break;
			case 1:
				passportTypeType = PassportType.EXTENDABLEASSET;
				break;
				// case 2:
				//				passportTypeType = PassportType.SMARTCONTRACT;
				//				break;
				// case 3:
				//				passportTypeType = PassportType.EXTENDABLESMARTCONTRACT;
				//				break;
			}
			if(passportTypeType == null) {
				throw new IllegalStateException("Invalid passport type: " + ordinal);
			}
			return passportTypeType;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	public static Passport parsePassport(final byte[] bytes) throws Exception {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Passport passport = null;
		final PassportType passportType = parsepassportType(is);
		try {
			if (passportType == PassportType.ASSET) {
				passport = new AssetPassport(bytes);
			}
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return passport;
	}
	public static Passport parsePassport(final ResultSet resultSet) throws Exception {
		Passport passport = null;
		final PassportType passportType = PassportType.get(resultSet.getByte("type"));

		try {
			if (passportType == PassportType.ASSET) {
				passport = new AssetPassport(resultSet);
			}
			else if(passportType == PassportType.EXTENDABLEASSET) {
				passport = new ExpendablePassport(resultSet);
			}
		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
			Log.Error(e.getMessage());
		}
		return passport;
	}
	public static PassportType parsepassportType(final ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		PassportType passportType = null;
		passportType = PassportType.get(EQCCastle.eqcBitsToInt(EQCCastle.parseEQCBits(is)));
		return passportType;
	}
	public static PassportType parsePassportType(final Lock lock) {
		PassportType passportType = null;
		if(lock.getType() == LockType.T1 || lock.getType() == LockType.T2) {
			passportType = PassportType.ASSET;
		}
		return passportType;
	}
	/**
	 * Header field include PassportType
	 * Passport type can also used to represent different passport type's version
	 */
	protected PassportType type;
	private boolean isTypeUpdate;
	/**
	 * Body field include ID, LockID, balance, nonce and updateHeight
	 */
	private ID id;
	private boolean isIDUpdate;
	private ID lockID;
	private boolean isLockIDUpdate;
	private Value balance;

	private boolean isBalanceUpdate;

	private ID nonce;

	private boolean isNonceUpdate;

	private ID updateHeight;

	private boolean isUpdateHeightUpdate;

	protected EQCHive eqcHive;

	public Passport() throws Exception {
		super();
	}

	public Passport(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public Passport(final ResultSet resultSet) throws Exception {
		super();
		type = PassportType.get(resultSet.getByte("type"));
		// Parse PassportID
		id = new ID(resultSet.getLong("id"));
		// Parse LockID
		lockID = new ID(resultSet.getLong("lock_id"));
		// Parse Balance
		balance = new Value(resultSet.getLong("balance"));
		// Parse Nonce
		nonce = new ID(resultSet.getLong("nonce"));
		// Parse UpdateHeight
		updateHeight = EQCCastle.parseID(resultSet.getLong("update_height"));
	}

	public void deposit(final Value value) {
		//		EQCCastle.assertNotBigger(value, Util.MAX_BALANCE);
		if(balance == null) {
			//			EQCCastle.assertNotLess(value, Util.MIN_BALANCE);
			balance = value;
		}
		else {
			balance = balance.add(value);
		}
		isBalanceUpdate = true;
	}

	/**
	 * @return the balance
	 */
	public Value getBalance() {
		return balance;
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		os.write(lockID.getEQCBits());
		os.write(balance.getEQCBits());
		os.write(nonce.getEQCBits());
		os.write(updateHeight.getEQCBits());
		return os;
	}

	public EQCHive getEQCHive() {
		return eqcHive;
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		return os;
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	@Override
	public byte[] getKey() throws Exception {
		return getHeaderBytes();
	}

	/**
	 * @return the lockID
	 */
	public ID getLockID() {
		return lockID;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

	public O getO() throws Exception {
		return new O(ByteBuffer.wrap(getBytes()));
	}

	public byte[] getProof() throws Exception {
		//		if(hash == null) {
		//			hash = Util.EQCCHA_MULTIPLE_DUAL(getHashBytes(soleUpdate), Util.HUNDREDPULS, true, false);
		//		}
		return null;
	}

	/**
	 * @return the type
	 */
	public PassportType getType() {
		return type;
	}

	/**
	 * @return the updateHeight
	 */
	public ID getUpdateHeight() {
		return updateHeight;
	}

	@Override
	public <V> V getValue() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public void increaseNonce() {
		nonce = nonce.getNextID();
		isNonceUpdate = true;
	}

	@Override
	protected void init() {
		nonce = ID.ZERO;
	}

	/**
	 * @return the isBalanceUpdate
	 */
	public boolean isBalanceUpdate() {
		return isBalanceUpdate;
	}

	/**
	 * @return the isIDUpdate
	 */
	public boolean isIDUpdate() {
		return isIDUpdate;
	}

	/**
	 * @return the isLockIDUpdate
	 */
	public boolean isLockIDUpdate() {
		return isLockIDUpdate;
	}

	//	/**
	//	 * @param nonce the nonce to set
	//	 */
	//	public void setNonce(ID nonce) {
	//		this.nonce = nonce;
	//	}

	/**
	 * @return the isNonceUpdate
	 */
	public boolean isNonceUpdate() {
		return isNonceUpdate;
	}

	/**
	 * Body field include ID, LockID, totalIncome, totalCost, balance, nonce
	 * @throws Exception
	 */

	@Override
	public boolean isSanity() throws Exception {
		if(type == null) {
			Log.Error("passportType == null");
			return false;
		}
		if(id == null) {
			Log.Error("id == null");
			return false;
		}
		if(!id.isSanity()) {
			Log.Error("!id.isSanity()");
			return false;
		}
		if(lockID == null) {
			Log.Error("lockID == null");
			return false;
		}
		if(!lockID.isSanity()) {
			Log.Error("!lockID.isSanity()");
			return false;
		}
		if(balance == null) {
			Log.Error("balance == null");
			return false;
		}
		if(!balance.isSanity()) {
			Log.Error("!balance.isSanity()");
			return false;
		}
		if(balance.compareTo(Util.MIN_BALANCE) < 0) {
			Log.Error("balance.compareTo(Util.MIN_BALANCE) < 0");
			return false;
		}
		if(balance.compareTo(Util.MAX_BALANCE) > 0) {
			Log.Error("balance.compareTo(Util.MAX_BALANCE) > 0");
			return false;
		}
		if(nonce == null) {
			Log.Error("nonce == null");
			return false;
		}
		if(!nonce.isSanity()) {
			Log.Error("!nonce.isSanity()");
			return false;
		}
		if(updateHeight == null) {
			Log.Error("updateHeight == null");
			return false;
		}
		if(!updateHeight.isSanity()) {
			Log.Error("!updateHeight.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * @return the isTypeUpdate
	 */
	public boolean isTypeUpdate() {
		return isTypeUpdate;
	}

	/**
	 * @return the isUpdateHeightUpdate
	 */
	public boolean isUpdateHeightUpdate() {
		return isUpdateHeightUpdate;
	}

	@Override
	public void parseBody(final ByteArrayInputStream is) throws NoSuchFieldException, IOException, Exception {
		// Parse PassportID
		id = EQCCastle.parseID(is);
		// Parse LockID
		lockID = EQCCastle.parseID(is);
		// Parse Balance
		balance = EQCCastle.parseValue(is);
		// Parse Nonce
		nonce = EQCCastle.parseID(is);
		// Parse UpdateHeight
		updateHeight = EQCCastle.parseID(is);
	}

	@Override
	public void parseHeader(final ByteArrayInputStream is) throws NoSuchFieldException, IOException {
		type = PassportType.get(EQCCastle.parseID(is).intValue());
	}

	public void planting() throws Exception {
		eqcHive.getGlobalState().savePassport(this);
	}

	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		final int len = in.readInt();
		final byte[] bytes = new byte[len];
		in.read(bytes);
		try {
			parse(new ByteArrayInputStream(bytes));
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Passport setEQCHive(final EQCHive eqcHive) {
		this.eqcHive = eqcHive;
		return this;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(final ID id) {
		this.id = id;
		isIDUpdate = true;
	}

	/**
	 * @param lockID the lockID to set
	 */
	public void setLockID(final ID lockID) {
		this.lockID = lockID;
		isLockIDUpdate = true;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final PassportType type) {
		this.type = type;
		isTypeUpdate = true;
	}

	/**
	 * @param updateHeight the updateHeight to set
	 */
	public void setUpdateHeight(final ID updateHeight) {
		this.updateHeight = updateHeight;
	}

	public void sync() {
		isTypeUpdate = true;
		isLockIDUpdate = true;
		isBalanceUpdate = true;
		isNonceUpdate = true;
		isUpdateHeightUpdate = true;
	}

	@Override
	public String toInnerJson() {
		return
				"\"Type\":" + "\"" + type + "\"" + ",\n" +
				"\"ID\":" + "\"" + id + "\"" + ",\n" +
				"\"LockID\":" + "\"" + lockID + "\"" + ",\n" +
				"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
				"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
				"\"UpdateHeight\":" + "\"" + updateHeight + "\"" +
				"\n}";
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

	/**
	 * For system's security here need check if balance is enough for example
	 * SmartContract maybe provide wrong input amount
	 *
	 * @param Value
	 */
	public void withdraw(final Value value) {
		EQCCastle.assertNotBigger(value, balance);
		balance = balance.subtract(value);
		isBalanceUpdate = true;
	}

	public void writeExternal(final ObjectOutput out) throws IOException {
		byte[] bytes;
		try {
			bytes = getBytes();
			out.writeInt(bytes.length);
			out.write(bytes);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
