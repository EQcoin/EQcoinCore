/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth 0 Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth 0 Corporation or jointly
 * released by Wandering Earth 0 Corporation with cooperative partners are owned
 * by Wandering Earth 0 Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth 0 Corporation reserves any and all current and future rights,
 * titles and interests in any and all intellectual property rights of Wandering Earth
 * 0 Corporation including but not limited to discoveries, ideas, marks, concepts,
 * methods, formulas, processes, codes, software, inventions, compositions, techniques,
 * information and data, whether or not protectable in trademark, copyrightable
 * or patentable, and any trademarks, copyrights or patents based thereon. For
 * the use of any and all intellectual property rights of Wandering Earth 0 Corporation
 * without prior written permission, Wandering Earth 0 Corporation reserves all
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.transaction.txout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date Mar 30, 2020
 * @email 10509759@qq.com
 */
public class TransferTxOut extends EQCObject {
	protected ID passportId;
	protected Value value;

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
		// Parse Value
		value = new Value(EQCCastle.parseEQCLight(is));
		// Parse Passport ID
		passportId = new ID(EQCCastle.parseEQCQuantum(is));
	}
	
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
			byte[] passportBytes = passportId.toByteArray();
			byte[] valueBytes = value.toByteArray();
			os.write(value.getEQCLight());
			os.write(passportBytes);
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
		if(passportId.compareTo(Util.MAX_PASSPORT_ID) == 1){
			Log.Error("The Passport'ID must less than " + Util.MAX_PASSPORT_ID);
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
		if(value.mod(BigInteger.valueOf(100000)) != BigInteger.ZERO){
			Log.Error("The transfer value in TransferTxOut must divisible by 100000.");
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
		"\"TransferTxOutQuantum\":" +
		"\n{" +
		"\"PassportId\":" + passportId + ",\n" +
		"\"Value\":" + "\"" +  Long.toString(value.longValue()) + "\"" + "\n" +
		"}";
	}

}
