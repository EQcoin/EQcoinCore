/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
 */
package org.eqcoin.passport.passport;

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
import org.eqcoin.lock.publickey.PublicKey;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCStateObject;
import org.eqcoin.util.*;

/**
 * Passport table's schema after refactor meet 3NF now //does not match 3NF but very blockchain.
 * @author Xun Wang
 * @date Nov 5, 2018
 * @email 10509759@qq.com
 */
public class Passport extends EQCStateObject {// implements Externalizable {

	public static Passport parsePassport(final byte[] bytes) throws Exception {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Passport passport = null;
//		final PassportType passportType = parsepassportType(is);
//		try {
//			if (passportType == PassportType.ASSET) {
//				passport = new AssetPassport(bytes);
//			}
//		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
		return passport;
	}
	public static Passport parsePassport(final ResultSet resultSet) throws Exception {
		Passport passport = null;
//		final PassportType passportType = PassportType.get(resultSet.getByte("type"));
//
//		try {
//			if (passportType == PassportType.ASSET) {
//				passport = new AssetPassport(resultSet);
//			}
//			else if(passportType == PassportType.EXTENDABLEASSET) {
//				passport = new ExpendablePassport(resultSet);
//			}
//		} catch (NoSuchFieldException | UnsupportedOperationException | IOException e) {
//			Log.Error(e.getMessage());
//		}
		return passport;
	}
//	public static PassportType parsepassportType(final ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
//		PassportType passportType = null;
//		passportType = PassportType.get(EQCCastle.eqcBitsToInt(EQCCastle.parseEQCBits(is)));
//		return passportType;
//	}
//	public static PassportType parsePassportType(final Lock lock) {
//		PassportType passportType = null;
//		if(lock.getType() == LockType.T1 || lock.getType() == LockType.T2) {
//			passportType = PassportType.ASSET;
//		}
//		return passportType;
//	}

	/**
	 * The Passport has a Status, an ID, a Balance, a Nonce, a LockNonce, a Lock, a PublicKey, and a SmartContract.
	 */
	private Status status;
	private ID id;
	private boolean isIDUpdate;
	private Value balance;

	private boolean isBalanceUpdate;

	private ID nonce;

	private boolean isNonceUpdate;

	private ID lockNonce;

	private boolean isLockNonceUpdate;

	private Lock lock;

	private PublicKey publicKey;

	protected EQCHive eqcHive;

	public Passport() throws Exception {
		super();
	}

	public Passport(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public Passport(final ResultSet resultSet) throws Exception {
		super();
		// Parse Status
		status = new Status(resultSet.getLong("status"));
		// Parse ID
		id = new ID(resultSet.getLong("id"));
		// Parse Balance
		balance = new Value(resultSet.getLong("balance"));
		// Parse Nonce
		nonce = new ID(resultSet.getLong("nonce"));
		// Parse LockNonce
		lockNonce = new ID(resultSet.getLong("lock_nonce"));
		// Parse Lock
		lock = new Lock(resultSet.getBytes("lock"));
		// Parse PublicKey
		publicKey = new PublicKey(resultSet.getBytes("public_key"));
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
		os.write(status.getEQCBits());
		os.write(id.getEQCBits());
		os.write(balance.getEQCBits());
		os.write(nonce.getEQCBits());
		os.write(lockNonce.getEQCBits());
		os.write(lock.getBytes());
		os.write(publicKey.getBytes());
		return os;
	}

	public EQCHive getEQCHive() {
		return eqcHive;
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
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
		return id.getEQCBits();
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
	 * @return the lockNonce
	 */
	public ID getLockNonce() {
		return lockNonce;
	}

	@Override
	public <V> V getValue() throws Exception {
		return null;
	}

	@Override
	public byte[] getHash() throws Exception {
		return new byte[0];
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

	@Override
	public boolean isSanity() throws Exception {
		if(status == null) {
			Log.Error("status == null");
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
		if(lockNonce == null) {
			Log.Error("updateHeight == null");
			return false;
		}
		if(!lockNonce.isSanity()) {
			Log.Error("!updateHeight.isSanity()");
			return false;
		}
		if(lock == null) {
			Log.Error("lock == null");
			return false;
		}
		if(!lock.isSanity()) {
			Log.Error("!lock.isSanity()");
			return false;
		}
		if(publicKey == null) {
			Log.Error("publicKey == null");
			return false;
		}
		if(!publicKey.isSanity()) {
			Log.Error("!publicKey.isSanity()");
			return false;
		}
		return true;
	}

	/**
	 * @return the isUpdateHeightUpdate
	 */
	public boolean isLockNonceUpdate() {
		return isLockNonceUpdate;
	}

	@Override
	public void parseBody(final ByteArrayInputStream is) throws NoSuchFieldException, IOException, Exception {
		// Parse Status
		status = new Status(EQCCastle.parseID(is));
		// Parse ID
		id = EQCCastle.parseID(is);
		// Parse Balance
		balance = EQCCastle.parseValue(is);
		// Parse Nonce
		nonce = EQCCastle.parseID(is);
		// Parse lockNonce
		lockNonce = EQCCastle.parseID(is);
		// Parse lock
		lock = new Lock(is);
		// Parse publicKey
		publicKey = new PublicKey(is);
	}

	@Override
	public void parseHeader(final ByteArrayInputStream is) throws NoSuchFieldException, IOException {
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
	 * @param lockNonce the lockNonce to set
	 */
	public void setLockNonce(final ID lockNonce) {
		this.lockNonce = lockNonce;
	}

	//	a Status, an ID, a Balance, a Nonce, a LockNonce, a Lock, a PublicKey,
	// Hibernate? Need do more job here
	public void sync() {
		isBalanceUpdate = true;
		isNonceUpdate = true;
		isLockNonceUpdate = true;
	}

	//	a Status, an ID, a Balance, a Nonce, a LockNonce, a Lock, a PublicKey,
	@Override
	public String toInnerJson() {
		return
				"\"Status\":" + "\"" + status + "\"" + ",\n" +
				"\"ID\":" + "\"" + id + "\"" + ",\n" +
				"\"Balance\":" + "\"" + balance + "\"" + ",\n" +
				"\"Nonce\":" + "\"" + nonce + "\"" + ",\n" +
						"\"LockNonce\":" + "\"" + lockNonce + "\"" + ",\n" +
						"\"Lock\":" + "\"" + lock + "\"" + ",\n" +
				"\"PublicKey\":" + "\"" + publicKey + "\"" + "\n";
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
