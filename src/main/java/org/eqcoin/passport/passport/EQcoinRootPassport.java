///**
// * EQcoin core
// *
// * http://www.eqcoin.org
// *
// * @Copyright 2018-present Xun Wang All Rights Reserved...
// * The copyright of all works released by Xun Wang or jointly released by
// * Xun Wang with cooperative partners are owned by Xun Wang and entitled
// * to protection available from copyright law by country as well as international
// * conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * Xun Wang reserves any and all current and future rights, titles and interests
// * in any and all intellectual property rights of Xun Wang including but not limited
// * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
// * software, inventions, compositions, techniques, information and data, whether
// * or not protectable in trademark, copyrightable or patentable, and any trademarks,
// * copyrights or patents based thereon. For the use of any and all intellectual
// * property rights of Xun Wang without prior written permission, Xun Wang reserves
// * all rights to take any legal action and pursue any rights or remedies under
// * applicable law.
// */
//package org.eqcoin.passport;
////
////import java.io.ByteArrayInputStream;
////import java.io.ByteArrayOutputStream;
////import java.sql.ResultSet;
////
//import org.eqcoin.passport.passport.ExpendablePassport;
//import org.eqcoin.passport.storage.Storage;
////import org.eqcoin.serialization.EQCType;
////import org.eqcoin.util.ID;
////import org.eqcoin.util.Log;
////import org.eqcoin.util.Util;
////
/////**
//// * @author Xun Wang
//// * @date Jun 22, 2019
//// * @email 10509759@qq.com
//// */
//@Deprecated
//public class EQcoinRootPassport extends ExpendablePassport {
////	/**
////	 * Body field include ProtocolVersion, MaxBlockSize, BlockInterval, TxFeeRate, CheckPoint
////	 */
////	// Here maybe need more design only record the height and hash isn't enough If need record the history ?
////	private ID protocolVersion;
////	// height of protocol when reached this height the relevant protocol version should equal to protocolVersion
////	private byte maxBlockSize;
////	private byte blockInterval;
////	// Here maybe need more design only record the txFeeRate isn't enough? If need record the history ?
////	private byte txFeeRate;
////	private ID checkPointHeight;
////	private byte[] checkPointHash;
////
////	public EQcoinRootPassport() throws Exception {
////		super();
////	}
////
//	public EQcoinRootPassport(final byte[] bytes) throws Exception {
//		super(bytes);
//	}
////
////	public EQcoinRootPassport(final ResultSet resultSet) throws Exception {
////		super(resultSet);
////	}
////
////	@Override
////	protected void derivedParse(final ResultSet resultSet) throws Exception {
////		final ByteArrayInputStream is = new ByteArrayInputStream(resultSet.getBytes("storage"));
////		storage = new Storage(is);
////		// Parse ProtocolVersion
////		protocolVersion = EQCType.parseID(is);
////		// Parse MaxBlockSize
////		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
////		// Parse BlockInterval
////		blockInterval = EQCType.parseNBytes(is, 1)[0];
////		// Parse TxFeeRate
////		txFeeRate = EQCType.parseNBytes(is, 1)[0];
////		// Parse CheckPoint Height
////		checkPointHeight = EQCType.parseID(is);
////		// Parse CheckPoint Hash
////		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
////	}
////
////	/**
////	 * @return the blockInterval
////	 */
////	public byte getBlockInterval() {
////		return blockInterval;
////	}
////
////	/* (non-Javadoc)
////	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#getBodyBytes()
////	 */
////	@Override
////	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
////		super.getBodyBytes(os);
////		os.write(protocolVersion.getEQCBits());
////		os.write(new byte[] {maxBlockSize});
////		os.write(new byte[] {blockInterval});
////		os.write(new byte[]{txFeeRate});
////		os.write(checkPointHeight.getEQCBits());
////		os.write(checkPointHash);
////		return os;
////	}
////
////	/**
////	 * @return the checkPointHash
////	 */
////	public byte[] getCheckPointHash() {
////		return checkPointHash;
////	}
////
////	/**
////	 * @return the checkPointHeight
////	 */
////	public ID getCheckPointHeight() {
////		return checkPointHeight;
////	}
////
////	/**
////	 * @return the maxBlockSize
////	 */
////	public byte getMaxBlockSize() {
////		return maxBlockSize;
////	}
////
////	/**
////	 * @return the protocolVersion
////	 */
////	public ID getProtocolVersion() {
////		return protocolVersion;
////	}
////
////	@Override
////	public byte[] getStorageState() throws Exception {
////		final ByteArrayOutputStream os = new ByteArrayOutputStream();
////		os.write(super.getStorageState());
////		os.write(protocolVersion.getEQCBits());
////		os.write(new byte[] {maxBlockSize});
////		os.write(new byte[] {blockInterval});
////		os.write(new byte[]{txFeeRate});
////		os.write(checkPointHeight.getEQCBits());
////		os.write(checkPointHash);
////		return os.toByteArray();
////	}
////	/**
////	 * @return the txFeeRate
////	 */
////	public byte getTxFeeRate() {
////		return txFeeRate;
////	}
////
////
////
////	/* (non-Javadoc)
////	 * @see com.eqcoin.blockchain.passport.Passport#init()
////	 */
////	@Override
////	protected void init() {
////		super.init();
////		type = PassportType.EQCOINROOT;
////	}
////
////	/* (non-Javadoc)
////	 * @see com.eqchains.blockchain.account.SmartContractAccount#isSanity()
////	 */
////	@Override
////	public boolean isSanity() throws Exception {
////		if(!super.isSanity()) {
////			return false;
////		}
////		if(protocolVersion == null) {
////			Log.Error("protocolVersion == null");
////			return false;
////		}
////		if(!protocolVersion.isSanity()) {
////			Log.Error("!protocolVersion.isSanity()");
////			return false;
////		}
////		if(maxBlockSize != 1) {
////			Log.Error("maxBlockSize != 1");
////			return false;
////		}
////		if(blockInterval != 198) {
////			Log.Error("blockInterval != 198");
////			return false;
////		}
////		if(checkPointHeight == null) {
////			Log.Error("checkPointHeight == null");
////			return false;
////		}
////		if(checkPointHash == null) {
////			Log.Error("checkPointHash == null");
////			return false;
////		}
////		if(!checkPointHeight.isSanity()) {
////			Log.Error("!checkPointHeight.isSanity()");
////			return false;
////		}
////		if(checkPointHash.length != Util.SHA3_512_LEN) {
////			Log.Error("checkPointHash.length != Util.SHA3_512_LEN");
////			return false;
////		}
////		if(txFeeRate < 1) {
////			Log.Error("txFeeRate < 1 ");
////			return false;
////		}
////		if(txFeeRate >10) {
////			Log.Error("txFeeRate >10");
////			return false;
////		}
////		return true;
////	}
////
////	/* (non-Javadoc)
////	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#parseBody(java.io.ByteArrayInputStream)
////	 */
////	@Override
////	public void parseBody(final ByteArrayInputStream is) throws Exception {
////		super.parseBody(is);
////		// Parse ProtocolVersion
////		protocolVersion = EQCType.parseID(is);
////		// Parse MaxBlockSize
////		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
////		// Parse BlockInterval
////		blockInterval = EQCType.parseNBytes(is, 1)[0];
////		// Parse TxFeeRate
////		txFeeRate = EQCType.parseNBytes(is, 1)[0];
////		// Parse CheckPoint Height
////		checkPointHeight = EQCType.parseID(is);
////		// Parse CheckPoint Hash
////		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
////	}
////
////	/* (non-Javadoc)
////	 * @see org.eqcoin.passport.ExpendablePassport#parseStorage(byte[])
////	 */
////	@Override
////	public void parseStorage(final byte[] bytes) throws Exception {
////		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
////		parseStorage(is);
////	}
////
////	/* (non-Javadoc)
////	 * @see org.eqcoin.passport.ExpendablePassport#parseStorage(java.io.ByteArrayInputStream)
////	 */
////	@Override
////	public void parseStorage(final ByteArrayInputStream is) throws Exception {
////		super.parseStorage(is);
////		// Parse ProtocolVersion
////		protocolVersion = EQCType.parseID(is);
////		// Parse MaxBlockSize
////		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
////		// Parse BlockInterval
////		blockInterval = EQCType.parseNBytes(is, 1)[0];
////		// Parse TxFeeRate
////		txFeeRate = EQCType.parseNBytes(is, 1)[0];
////		// Parse CheckPoint Height
////		checkPointHeight = EQCType.parseID(is);
////		// Parse CheckPoint Hash
////		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
////	}
////
////	/**
////	 * @param blockInterval the blockInterval to set
////	 */
////	public void setBlockInterval(final byte blockInterval) {
////		this.blockInterval = blockInterval;
////	}
////
////	/**
////	 * @param checkPointHash the checkPointHash to set
////	 */
////	public void setCheckPointHash(final byte[] checkPointHash) {
////		this.checkPointHash = checkPointHash;
////	}
////
////	/**
////	 * @param checkPointHeight the checkPointHeight to set
////	 */
////	public void setCheckPointHeight(final ID checkPointHeight) {
////		this.checkPointHeight = checkPointHeight;
////	}
////
////	/**
////	 * @param maxBlockSize the maxBlockSize to set
////	 */
////	public void setMaxBlockSize(final byte maxBlockSize) {
////		this.maxBlockSize = maxBlockSize;
////	}
////
////	/**
////	 * @param protocolVersion the protocolVersion to set
////	 */
////	public void setProtocolVersion(final ID protocolVersion) {
////		this.protocolVersion = protocolVersion;
////	}
////
////	/**
////	 * @param txFeeRate the txFeeRate to set
////	 */
////	public void setTxFeeRate(final byte txFeeRate) {
////		this.txFeeRate = txFeeRate;
////	}
////
////	/* (non-Javadoc)
////	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#toInnerJson()
////	 */
////	@Override
////	public String toInnerJson() {
////		return
////				"\"EQcoinSubchainAccount\":" +
////				"\n{\n" +
////				super.toInnerJson() + ",\n" +
////				"\"TxFeeRate\":" + "\"" + txFeeRate + "\"" +
////				"\n}";
////	}
////
////	/* (non-Javadoc)
////	 * @see java.lang.Object#toString()
////	 */
////	@Override
////	public String toString() {
////		return "{\n" +
////				toInnerJson() +
////				"\n}";
////	}
////
//}
