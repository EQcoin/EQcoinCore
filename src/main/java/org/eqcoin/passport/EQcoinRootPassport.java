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
package org.eqcoin.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;

import org.eqcoin.changelog.ChangeLog;
import org.eqcoin.passport.Passport.PassportType;
import org.eqcoin.passport.storage.Storage;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 22, 2019
 * @email 10509759@qq.com
 */
public class EQcoinRootPassport extends ExpendablePassport {
	/**
	 * Body field include ProtocolVersion, MaxBlockSize, BlockInterval, TxFeeRate, CheckPoint
	 */
	// Here maybe need more design only record the height and hash isn't enough If need record the history ?
	private ID protocolVersion;
	// height of protocol when reached this height the relevant protocol version should equal to protocolVersion
	private byte maxBlockSize;
	private byte blockInterval;
	// Here maybe need more design only record the txFeeRate isn't enough? If need record the history ?
	private byte txFeeRate;
	private ID checkPointHeight;
	private byte[] checkPointHash;

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.passport.Passport#init()
	 */
	@Override
	protected void init() {
		super.init();
		type = PassportType.EQCOINROOT;
	}

	public EQcoinRootPassport() throws Exception {
		super();
	}
	
	public EQcoinRootPassport(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public EQcoinRootPassport(ResultSet resultSet) throws Exception {
		super(resultSet);
	}
	
	protected void derivedParse(ResultSet resultSet) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(resultSet.getBytes("storage"));
		storage = new Storage(is);
		// Parse ProtocolVersion
		protocolVersion = EQCType.parseID(is);
		// Parse MaxBlockSize
		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
		// Parse BlockInterval
		blockInterval = EQCType.parseNBytes(is, 1)[0];
		// Parse TxFeeRate
		txFeeRate = EQCType.parseNBytes(is, 1)[0];
		// Parse CheckPoint Height
		checkPointHeight = EQCType.parseID(is);
		// Parse CheckPoint Hash
		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		// Parse ProtocolVersion
		protocolVersion = EQCType.parseID(is);
		// Parse MaxBlockSize
		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
		// Parse BlockInterval
		blockInterval = EQCType.parseNBytes(is, 1)[0];
		// Parse TxFeeRate
		txFeeRate = EQCType.parseNBytes(is, 1)[0];
		// Parse CheckPoint Height
		checkPointHeight = EQCType.parseID(is);
		// Parse CheckPoint Hash
		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
			super.getBodyBytes(os);
			os.write(protocolVersion.getEQCBits());
			os.write(new byte[] {maxBlockSize});
			os.write(new byte[] {blockInterval});
			os.write(new byte[]{txFeeRate});
			os.write(checkPointHeight.getEQCBits());
			os.write(checkPointHash);
		return os;
	}
	
	@Override
	public byte[] getStorageState() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(super.getStorageState());
		os.write(protocolVersion.getEQCBits());
		os.write(new byte[] {maxBlockSize});
		os.write(new byte[] {blockInterval});
		os.write(new byte[]{txFeeRate});
		os.write(checkPointHeight.getEQCBits());
		os.write(checkPointHash);
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.ExpendablePassport#parseStorage(byte[])
	 */
	@Override
	public void parseStorage(byte[] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseStorage(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.ExpendablePassport#parseStorage(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseStorage(ByteArrayInputStream is) throws Exception {
		super.parseStorage(is);
		// Parse ProtocolVersion
		protocolVersion = EQCType.parseID(is);
		// Parse MaxBlockSize
		maxBlockSize = EQCType.parseNBytes(is, 1)[0];
		// Parse BlockInterval
		blockInterval = EQCType.parseNBytes(is, 1)[0];
		// Parse TxFeeRate
		txFeeRate = EQCType.parseNBytes(is, 1)[0];
		// Parse CheckPoint Height
		checkPointHeight = EQCType.parseID(is);
		// Parse CheckPoint Hash
		checkPointHash = EQCType.parseNBytes(is, Util.SHA3_512_LEN);
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
	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.AssetSubchainAccount#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"EQcoinSubchainAccount\":" + 
				"\n{\n" +
					super.toInnerJson() + ",\n" +
					"\"TxFeeRate\":" + "\"" + txFeeRate + "\"" +
				"\n}";
	}
	
	

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.SmartContractAccount#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(!super.isSanity()) {
			return false;
		}
		if(protocolVersion == null) {
			Log.Error("protocolVersion == null");
			return false;
		}
		if(!protocolVersion.isSanity()) {
			Log.Error("!protocolVersion.isSanity()");
			return false;
		}
		if(maxBlockSize != 1) {
			Log.Error("maxBlockSize != 1");
			return false;
		}
		if(blockInterval != 198) {
			Log.Error("blockInterval != 198");
			return false;
		}
		if(checkPointHeight == null) {
			Log.Error("checkPointHeight == null");
			return false;
		}
		if(checkPointHash == null) {
			Log.Error("checkPointHash == null");
			return false;
		}
		if(!checkPointHeight.isSanity()) {
			Log.Error("!checkPointHeight.isSanity()");
			return false;
		}
		if(checkPointHash.length != Util.SHA3_512_LEN) {
			Log.Error("checkPointHash.length != Util.SHA3_512_LEN");
			return false;
		}
		if(txFeeRate < 1) {
			Log.Error("txFeeRate < 1 ");
			return false;
		}
		if(txFeeRate >10) {
			Log.Error("txFeeRate >10");
			return false;
		}
		return true;
	}

	/**
	 * @return the txFeeRate
	 */
	public byte getTxFeeRate() {
		return txFeeRate;
	}

	/**
	 * @param txFeeRate the txFeeRate to set
	 */
	public void setTxFeeRate(byte txFeeRate) {
		this.txFeeRate = txFeeRate;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}

	/**
	 * @return the checkPointHash
	 */
	public byte[] getCheckPointHash() {
		return checkPointHash;
	}

	/**
	 * @param checkPointHash the checkPointHash to set
	 */
	public void setCheckPointHash(byte[] checkPointHash) {
		this.checkPointHash = checkPointHash;
	}

	/**
	 * @return the maxBlockSize
	 */
	public byte getMaxBlockSize() {
		return maxBlockSize;
	}

	/**
	 * @param maxBlockSize the maxBlockSize to set
	 */
	public void setMaxBlockSize(byte maxBlockSize) {
		this.maxBlockSize = maxBlockSize;
	}

	/**
	 * @return the blockInterval
	 */
	public byte getBlockInterval() {
		return blockInterval;
	}

	/**
	 * @param blockInterval the blockInterval to set
	 */
	public void setBlockInterval(byte blockInterval) {
		this.blockInterval = blockInterval;
	}

	/**
	 * @return the protocolVersion
	 */
	public ID getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @param protocolVersion the protocolVersion to set
	 */
	public void setProtocolVersion(ID protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
	
}
