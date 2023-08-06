/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by Xun
 * Wang with cooperative partners are owned by Xun Wang and entitled to
 * protection available from copyright law by country as well as international
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
 * property rights of Xun Wang without prior written permission, Xun Wang
 * reserves all rights to take any legal action and pursue any rights or remedies
 * under applicable law.
 */
package org.eqcoin.hive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

import org.eqcoin.avro.O;
import org.eqcoin.persistence.globalstate.GlobalState;
import org.eqcoin.persistence.globalstate.GlobalState.Statistics;
import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;

/**
 * @author Xun Wang
 * @date 9-12-2018
 * @email 10509759@qq.com
 */
public class EQCHiveRoot extends EQCObject implements Gateway {
	private final static int MAX_NONCE_LEN = 8;
	private final static int MIN_TIMESTAMP_LEN = 6;
	private final static int TARGET_LEN = 4;
	/**
	 * Current EQCHive's height
	 *  lengthen >= 1 byte
	 */
	private ID height;
	/**
	 * Current EQCHive's protocol version
	 *  lengthen >= 1 byte
	 */
	private ID protocolVersion;
	/**
	 * lengthen = 64 bytes
	 */
	private byte[] preProof;
	/**
	 * lengthen = 4 bytes
	 */
	private byte[]	target;
	/**
	 *  lengthen >= 1 byte
	 */
	/**
	 * lengthen = 64 bytes
	 */
	private byte[] eqCoinSeedsProof;

	/**
	 * Calculate this according to newTransactionList ARRAY's length
	 */
	private ID totalTransactionNumbers;
	//	/**
	//	 * Calculate this according to newHelixList ARRAY's length which equal to the
	//	 * new Locks in ZionTransaction and UpdateLockOP
	//	 */
	private ID totalLockMateNumbers;
	//	/**
	//	 * Calculate this according to ZionTransaction.
	//	 */
	private ID totalPassportNumbers;
	private Value totalSupply;
	private ID timestamp;
	/**
	 * lengthen <= 8 bytes
	 */
	private ID nonce;
	private EQCHive eqcHive;
	// The min EQCHiveRoot's size
	private final int min_size = 139; // Here exists bug need do more job to fix this

	public EQCHiveRoot() {}

	/**
	 * @param header
	 * @throws Exception
	 */
	public EQCHiveRoot(final byte[] bytes) throws Exception {
		super(bytes);
	}

	public EQCHiveRoot(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	public <T> EQCHiveRoot(final T type) throws Exception {
		super();
		parse(type);
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(protocolVersion.getEQCBits());
		os.write(preProof);
		os.write(target);
		os.write(eqCoinSeedsProof);
		os.write(totalTransactionNumbers.getEQCBits());
		os.write(totalLockMateNumbers.getEQCBits());
		os.write(totalPassportNumbers.getEQCBits());
		os.write(totalSupply.getEQCBits());
		os.write(timestamp.getEQCBits());
		os.write(nonce.getEQCBits());
		return os;
	}

	public byte[] getEqCoinSeedsProof() {
		return eqCoinSeedsProof;
	}

	/**
	 * @return the EQCoinSeedsProof
	 */
	public byte[] getEQCoinSeedsProof() {
		return eqCoinSeedsProof;
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(height.getEQCBits());
		return os;
	}

	/**
	 * @return the height
	 */
	public ID getHeight() {
		return height;
	}

	public int getMinSize() {
		// Here exists bug
		return min_size;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}
	/**
	 * @return the preProof
	 */
	public byte[] getPreProof() {
		return preProof;
	}
	/**
	 * @return byte[] the eqcHeader's EQCCHA hash
	 * @throws Exception
	 */
	public byte[] getProof() throws Exception {
		return MessageDigest.getInstance("SHA3-512").digest(Util.multipleExtendMix(getBytes(), Util.TARGET_INTERVAL.intValue()));
	}
	@Override
	public <T> T getProtocol(final Class<T> type) throws Exception {
		T protocol = null;
		if(type.equals(O.class)) {
			protocol = (T) new O(ByteBuffer.wrap(getBytes()));
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		return protocol;
	}
	public ID getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @return the target
	 */
	public byte[] getTarget() {
		return target;
	}

	/**
	 * @return the timestamp
	 */
	public ID getTimestamp() {
		return timestamp;
	}

	public ID getTotalLockMateNumbers() {
		return totalLockMateNumbers;
	}

	public ID getTotalPassportNumbers() {
		return totalPassportNumbers;
	}

	public Value getTotalSupply() {
		return totalSupply;
	}

	public ID getTotalTransactionNumbers() {
		return totalTransactionNumbers;
	}

	// 20200514 here need check if the target is valid
	public boolean isDifficultyValid() throws Exception {
		return (new BigInteger(1, getProof()).compareTo(Util.targetBytesToBigInteger(target)) <= 0);
	}

	public boolean isDifficultyValid(final GlobalState globalState) throws Exception {
		if (!Arrays.equals(target, Util.cypherTarget(globalState))) {
			Log.Error("target isn't valid");
			return false;
		}
		if (!isDifficultyValid()) {
			Log.info("Difficulty is invalid");
			return false;
		}
		return true;
	}

	@Override
	public boolean isSanity() {
		if(preProof == null) {
			Log.Error("preProof is null: " + preProof);
			return false;
		}
		if(target == null) {
			Log.Error("target is null: " + target);
			return false;
		}
		if(eqCoinSeedsProof == null) {
			Log.Error("eqCoinSeedProof is null: " + eqCoinSeedsProof);
			return false;
		}
		if(height == null) {
			Log.Error("height is null: " + height);
			return false;
		}
		if(timestamp == null) {
			Log.Error("timestamp is null: " + timestamp);
			return false;
		}
		if(nonce == null) {
			Log.Error("nonce is null: " + nonce);
			return false;
		}
		if(preProof.length != Util.SHA3_512_LEN) {
			Log.Error("preProof.length != Util.SHA3_512_LEN(64): " + preProof.length);
			return false;
		}
		if(target.length != TARGET_LEN) {
			Log.Error("target.length != TARGET_LEN(4): " + target.length);
			return false;
		}
		if(eqCoinSeedsProof.length != Util.SHA3_512_LEN) {
			Log.Error("eqCoinSeedProof.length != Util.SHA3_512_LEN(64): " + eqCoinSeedsProof.length);
			return false;
		}
		if(height.equals(ID.ZERO)) {
			if(!timestamp.equals(ID.ZERO)) {
				Log.Error("ZKP: " + timestamp);
				return false;
			}
		}
		else {
			if(timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN) {
				Log.Error("timestamp.getEQCBits().length < MIN_TIMESTAMP_LEN(6): " + timestamp.getEQCBits().length);
				return false;
			}
		}
		if(nonce.getEQCBits().length > MAX_NONCE_LEN) {
			Log.Error("nonce.getEQCBits().length > MAX_NONCE_LEN(8): " + nonce.getEQCBits().length);
			return false;
		}
		return true;
	}

	public boolean isStatisticsValid(final Statistics statistics, final boolean isCheckRoot) throws Exception {
		// Check if total new lock numbers equal to total new passport numbers + total new updated Lock numbers
		ID totalNewLockMateNumbers = null;
		ID totalNewPassportNumbers = null;
		ID preTotalLockMateNumbers = null;
		ID preTotalPassportNumbers = null;
		ID preTotalTransactionNumbers = null;
		totalNewLockMateNumbers = eqcHive.getGlobalState().getTotalNewLockMateNumbers();
		totalNewPassportNumbers = eqcHive.getGlobalState().getTotalNewPassportNumbers();
		if(height.equals(ID.ZERO)) {
			preTotalLockMateNumbers = ID.ZERO;
			preTotalPassportNumbers = ID.ZERO;
			preTotalTransactionNumbers = ID.ZERO;
		}
		else {
			preTotalLockMateNumbers = eqcHive.getPreRoot().getTotalLockMateNumbers();
			preTotalPassportNumbers = eqcHive.getPreRoot().getTotalPassportNumbers();
			preTotalTransactionNumbers = eqcHive.getPreRoot().getTotalTransactionNumbers();
		}

		// 20200530 here need do more job
		//		if(!totalNewLockNumbers.equals(totalNewPassportNumbers.add(new ID(changeLog.getForbiddenLockList().size())))) {
		//			Log.Error("TotalNewLockNumbers doesn't equal to totalNewPassportNumbers + totalNewUpdateLockNumbers. This is invalid.");
		//			return false;
		//		}

		// Check if the last EQCHive's height is valid
		if(!height.isNextID(eqcHive.getGlobalState().getLastEQCHiveHeight())) {
			Log.Error("!eqcHive.getRoot().getHeight().isNextID(eqcHive.getGlobalState().getLastEQCHiveHeight())");
			return false;
		}

		// Check if the last LockMate's ID is valid
		if(!statistics.getTotalLockMateNumbers().isNextID(eqcHive.getGlobalState().getLastLockMateId())) {
			Log.Error("!totalLockMateNumbers.isNextID(eqcHive.getGlobalState().getLastLockMateId())");
			return false;
		}

		// Check if the last Passport's ID is valid
		if(!statistics.getTotalPassportNumbers().isNextID(eqcHive.getGlobalState().getLastPassportId())) {
			Log.Error("!totalPassportNumbers.isNextID(eqcHive.getGlobalState().getLastPassportId())");
			return false;
		}

		// Check if total supply is valid
		if (!statistics.getTotalSupply().equals(Util.cypherTotalSupply(eqcHive))) {
			Log.Error("TotalSupply is invalid expected: " + Util.cypherTotalSupply(eqcHive) + " but statistics: "
					+ statistics.getTotalSupply());
			return false;
		}
		if(isCheckRoot && !totalSupply.equals(statistics.getTotalSupply())) {
			Log.Error("TotalSupply is invalid expected: " + statistics.getTotalSupply() + " but in EQCHiveRoot: " + totalSupply);
			return false;
		}

		// Check if total transaction numbers is valid
		if(!statistics.getTotalTransactionNumbers().equals(preTotalTransactionNumbers.add(new ID(eqcHive.getEQCoinSeeds().getNewTransactionList().size())))) {
			Log.Error("TotalTransactionNumbers is invalid expected: " + preTotalTransactionNumbers.add(new ID(eqcHive.getEQCoinSeeds().getNewTransactionList().size())) + " but statistics: " + statistics.getTotalTransactionNumbers());
			return false;
		}
		if(isCheckRoot && totalTransactionNumbers.equals(statistics.getTotalTransactionNumbers())) {
			Log.Error("TotalTransactionNumbers is invalid expected: " + statistics.getTotalTransactionNumbers() + " but in EQCHiveRoot: " + totalTransactionNumbers);
			return false;
		}

		// Check if total lock numbers is valid
		if(!statistics.getTotalLockMateNumbers().equals(totalNewLockMateNumbers.add(preTotalLockMateNumbers))) {
			Log.Error("Total lock mate numbers is invalid expected: " + statistics.getTotalLockMateNumbers() + " but actual: " + totalNewLockMateNumbers.add(preTotalLockMateNumbers));
			return false;
		}
		if(isCheckRoot && totalLockMateNumbers.equals(totalNewLockMateNumbers.add(preTotalLockMateNumbers))) {
			Log.Error("Total lock mate numbers is invalid expected: " + statistics.getTotalLockMateNumbers() + " but in EQCHiveRoot: " + totalLockMateNumbers);
			return false;
		}

		// Check if total passport numbers is valid
		if(!statistics.getTotalPassportNumbers().equals(totalNewPassportNumbers.add(preTotalPassportNumbers))) {
			Log.Error("Total passport numbers is invalid expected: " + statistics.getTotalPassportNumbers() + " but actual: " + totalNewPassportNumbers.add(preTotalPassportNumbers));
			return false;
		}
		if(isCheckRoot && !totalPassportNumbers.equals(totalNewPassportNumbers.add(preTotalPassportNumbers))) {
			Log.Error("Total passport numbers is invalid expected: " + statistics.getTotalPassportNumbers() + " but in EQCHiveRoot: " + totalNewPassportNumbers.add(preTotalPassportNumbers));
			return false;
		}

		return true;
	}
	@Override
	public boolean isValid() throws Exception {
		if(!isSanity()) {
			Log.info("Sanity test failed.");
			return false;
		}
		if(!Arrays.equals(preProof, eqcHive.getPreRoot().getProof())) {
			Log.Error("PreProof is invalid.");
			return false;
		}
		if(!Arrays.equals(eqCoinSeedsProof, eqcHive.getEQCoinSeeds().getProof())) {
			Log.Error("eqCoinSeedHash is invalid.");
			return false;
		}
		if(!height.isNextID(eqcHive.getPreRoot().getHeight())) {
			Log.Error("Height should be the previous EQCHive's next Height.");
			return false;
		}
		if(timestamp.compareTo(eqcHive.getPreRoot().getTimestamp()) <= 0) {
			Log.Error("Timestamp should bigger than previous EQCHive's timestamp.");
			return false;
		}
		if(timestamp.compareTo(new ID(System.currentTimeMillis())) > 0) {
			Log.Error("Timestamp should less than current GMT time.");
			return false;
		}
		return true;
	}
	@Override
	public <T> void parse(final T type) throws Exception {
		Objects.requireNonNull(type);
		byte[] bytes = null;
		if(type instanceof O) {
			bytes = ((O) type).getO().array();
		}
		else {
			throw new IllegalStateException("Invalid Protocol type");
		}
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parse(is);
		EQCCastle.assertNoRedundantData(is);
	}
	@Override
	public void parseBody(final ByteArrayInputStream is) throws Exception {
		protocolVersion = EQCCastle.parseID(is);
		preProof = EQCCastle.parseNBytes(is, Util.HASH_LEN);
		target = EQCCastle.parseNBytes(is, TARGET_LEN);
		eqCoinSeedsProof = EQCCastle.parseNBytes(is, Util.HASH_LEN);
		totalTransactionNumbers = EQCCastle.parseID(is);
		totalLockMateNumbers = EQCCastle.parseID(is);
		totalPassportNumbers = EQCCastle.parseID(is);
		totalSupply = EQCCastle.parseValue(is);
		timestamp = EQCCastle.parseID(is);
		nonce = EQCCastle.parseID(is);
	}
	@Override
	public void parseHeader(final ByteArrayInputStream is) throws Exception {
		height = new ID(EQCCastle.parseEQCBits(is));
	}
	public EQCHiveRoot setEQCHive(final EQCHive eqcHive) {
		this.eqcHive = eqcHive;
		return this;
	}

	public void setEqCoinSeedsProof(final byte[] eqCoinSeedsProof) {
		this.eqCoinSeedsProof = eqCoinSeedsProof;
	}

	/**
	 * @param EQCoinSeedsProof the EQCoinSeedsProof to set
	 */
	public void setEQCoinSeedsProof(final byte[] eqCoinSeedsProof) {
		this.eqCoinSeedsProof = eqCoinSeedsProof;
	}

	/**
	 * @param height the height to set
	 */
	public EQCHiveRoot setHeight(final ID height) {
		this.height = height;
		return this;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public EQCHiveRoot setNonce(final ID nonce) {
		this.nonce = nonce;
		return this;
	}

	/**
	 * @param preProof the preProof to set
	 */
	public void setPreProof(final byte[] preProof) {
		this.preProof = preProof;
	}

	public void setProtocolVersion(final ID protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(final byte[] target) {
		this.target = target;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(final ID timestamp) {
		this.timestamp = timestamp;
	}

	public void setTotalLockMateNumbers(final ID totalLockMateNumbers) {
		this.totalLockMateNumbers = totalLockMateNumbers;
	}

	public void setTotalPassportNumbers(final ID totalPassportNumbers) {
		this.totalPassportNumbers = totalPassportNumbers;
	}

	public void setTotalSupply(final Value totalSupply) {
		this.totalSupply = totalSupply;
	}

	//	public byte[] getSnapshot() {
	//		byte[] bytes = new byte[5];
	//		bytes[0] = preProof[33];
	//		bytes[1] = preProof[51];
	//		bytes[2] = eqCoinSeedProof[33];
	//		bytes[3] = eqCoinSeedProof[51];
	//		if(timestamp.equals(ID.ZERO)) {
	//			bytes[4] = 0;
	//		}
	//		else {
	//			bytes[4] = timestamp.getEQCBits()[3];
	//		}
	//		return bytes;
	//	}

	public void setTotalTransactionNumbers(final ID totalTransactionNumbers) {
		this.totalTransactionNumbers = totalTransactionNumbers;
	}

	@Override
	public String toInnerJson() {
		return "\"EQCHeader\":" +
				"{\n" +
				"\"PreProof\":" + "\"" + Util.bytesTo512HexString(preProof) + "\"" + ",\n" +
				"\"Target\":" + "\"" + Util.bytesTo512HexString(Util.targetBytesToBigInteger(target).toByteArray()) + "\"" + ",\n" +
				"\"TargetBytes\":" + "\"" + Integer.toHexString((int) Util.bytesToLong(target)).toUpperCase() + "\"" + ",\n" +
				"\"EQCoinSeedProof\":" + "\"" + Util.dumpBytes(eqCoinSeedsProof, 16) + "\"" + ",\n" +
				"\"Height\":" + "\"" + height + "\"" + ",\n" +
				"\"Timestamp\":" + "\"" + Util.getGMTTime(timestamp.longValue()) + "\"" + ",\n" +
				"\"Nonce\":" + "\"" + Integer.toHexString(nonce.intValue()).toUpperCase() + "\"" + "\n" +
				"}";
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

}
