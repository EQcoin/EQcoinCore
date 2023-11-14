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
package org.eqcoin.passport.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

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
