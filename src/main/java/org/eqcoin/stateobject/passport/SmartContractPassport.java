/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take 
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
import java.security.acl.Owner;
import java.util.Collections;

import org.apache.avro.io.parsing.Symbol;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.passport.Passport.PassportType;
import org.eqcoin.transaction.Transaction.TransactionType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date May 13, 2019
 * @email 10509759@qq.com
 */
public abstract class SmartContractPassport extends ExpendablePassport {
	/**
	 * Body field include LanguageType
	 */
	private ID founderID;
	private ID leasePeriod;
	private LanguageType languageType;
	/**
	 * Use ID is better than use long type.
	 * Here can just ignore the totalStateSize's itself size or find a way to count it.
	 */
	private ID totalStateSize;
	
	public enum LanguageType {
		JAVA, INTELLIGENT, MOVE;
		public static LanguageType get(int ordinal) {
			LanguageType languageType = null;
			switch (ordinal) {
			case 0:
				languageType = LanguageType.JAVA;
				break;
			case 1:
				languageType = LanguageType.INTELLIGENT;
				break;
			case 2:
				languageType = LanguageType.MOVE;
				break;
			}
			return languageType;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
	
	/**
	 * @author Xun Wang
	 * @date Jun 18, 2019
	 * @email 10509759@qq.com
	 * 
	 * When SmartContract inactive we can achieve it's state DB to GitHub which is free. 
	 * After this all the full node doesn't need store it's state DB in local just keep the relevant Account's state.
	 * When it become active again all the full node can recovery it's state DB from GitHub. 
	 */
	public enum State {
		ACTIVE, OVERDUE, TERMINATE;
		public static State get(int ordinal) {
			State state = null;
			switch (ordinal) {
			case 0:
				state = State.ACTIVE;
				break;
			case 1:
				state = State.OVERDUE;
				break;
			case 2:
				state = State.TERMINATE;
				break;
			}
			return state;
		}
		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
//	public enum SmartContractType {
//		SUBCHAIN, MISC, INVALID;
//		public static SmartContractType get(int ordinal) {
//			SmartContractType smartContractType = null;
//			switch (ordinal) {
//			case 0:
//				smartContractType = SmartContractType.SUBCHAIN;
//				break;
//			case 1:
//				smartContractType = SmartContractType.MISC;
//				break;
//			default:
//				smartContractType = SmartContractType.INVALID;
//				break;
//			}
//			return smartContractType;
//		}
//		public boolean isSanity(changeLog) {
//			if((this.ordinal() < SUBCHAIN.ordinal()) || (this.ordinal() > INVALID.ordinal())) {
//				return false;
//			}
//			return true;
//		}
//		public byte[] getEQCBits() {
//			return EQCType.intToEQCBits(this.ordinal());
//		}
//	}
	
	protected SmartContractPassport() throws Exception {
		super();
	}
	
//	protected SmartContractAccount(byte[] bytes) throws NoSuchFieldException, IOException {
//		super(AccountType.SMARTCONTRACT);
//	}
	
	
//	public static SmartContractType parseSubchainType(ByteArrayInputStream is) {
//		SmartContractType subchainType = SmartContractType.INVALID;
//		byte[] data = null;
//		try {
//			if ((data = EQCType.parseEQCBits(is)) != null) {
//				subchainType = SmartContractType.get(EQCType.eqcBitsToInt(data));
//			}
//		} catch (NoSuchFieldException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Log.Error(e.getMessage());
//		}
//		return subchainType;
//	}

	public SmartContractPassport(byte[] bytes) throws Exception {
		super(bytes);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse Super Body
		super.parseBody(is);
		// Parse Sub Body
		// Parse FounderID
		founderID = new ID(EQCCastle.parseEQCBits(is));
		// Parse LanguageType
		languageType = LanguageType.get(EQCCastle.eqcBitsToInt(EQCCastle.parseEQCBits(is)));
		// Parse TotalStateSize
		totalStateSize = EQCCastle.parseID(is);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#getBodyBytes()
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
			super.getBodyBytes(os);
			os.write(founderID.getEQCBits());
			os.write(languageType.getEQCBits());
			os.write(totalStateSize.getEQCBits());
		return os;
	}

	/**
	 * @return the languageType
	 */
	public LanguageType getLanguageType() {
		return languageType;
	}

	/**
	 * @param languageType the languageType to set
	 */
	public void setLanguageType(LanguageType languageType) {
		this.languageType = languageType;
	}
	
	public String toInnerJson() {
		return 
					super.toInnerJson() + ",\n" +
					"\"FounderID\":" + "\"" + founderID + "\"" + ",\n" +
					"\"LanguageType\":" + "\"" + languageType + "\"" + ",\n" +
					"\"TotalStateSize\":" + "\"" + totalStateSize + "\"" + ",\n";
	}

	/* (non-Javadoc)
	 * @see com.eqchains.blockchain.account.Account#isSanity(changeLog)
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(!super.isSanity()) {
			return false;
		}
		if(founderID == null) {
			Log.Error("founderID == null");
			return false;
		}
		if(!founderID.isSanity()) {
			Log.Error("!founderID.isSanity()");
			return false;
		}
		if(languageType == null) {
			Log.Error("languageType == null");
			return false;
		}
		if(totalStateSize == null) {
			Log.Error("totalStateSize == null");
			return false;
		}
		if(!totalStateSize.isSanity()) {
			Log.Error("!totalStateSize.isSanity()");
			return false;
		}
		// Need do more job about language type
		return true;
	}

	/**
	 * @return the founderID
	 */
	public ID getFounderID() {
		return founderID;
	}

	/**
	 * @param founderID the founderID to set
	 */
	public void setFounderID(ID founderID) {
		this.founderID = founderID;
	}

	/**
	 * @return the totalStateSize
	 */
	public ID getTotalStateSize() {
		return totalStateSize;
	}

	/**
	 * @param totalStateSize the totalStateSize to set
	 */
	public void setTotalStateSize(ID totalStateSize) {
		this.totalStateSize = totalStateSize;
	}
	
}
