/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
//package org.eqcoin.transaction.operation;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.sql.SQLException;
//
//import org.eqcoin.lock.LockMate;
//import org.eqcoin.serialization.EQCCastle;
//import Passport;
//import org.eqcoin.transaction.ModerateOPTransaction;
//import org.eqcoin.transaction.Transaction;
//import org.eqcoin.transaction.TransferOPTransaction;
//import org.eqcoin.transaction.ZionOPTransaction;
//import org.eqcoin.transaction.operation.Operation.OP;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Log;
//
///**
// * @author Xun Wang
// * @date Jun 22, 2019
// * @email 10509759@qq.com
// */
//public class ChangeTxFeeRate extends Operation {
//	private byte txFeeRate;
//	
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#init()
//	 */
//	@Override
//	protected void init() {
//		op = OP.TXFEERATE;
//	}
//
//	public ChangeTxFeeRate() throws Exception {
//		super();
//	}
//	
//	public ChangeTxFeeRate(ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//
//	@Override
//	public void planting() throws Exception {
//		EQcoinRootPassport eQcoinSeedPassport = (EQcoinRootPassport) transaction.getEQCHive().getGlobalState().getPassport(ID.ZERO);
//		eQcoinSeedPassport.setTxFeeRate(txFeeRate);
//		transaction.getEQCHive().getGlobalState().savePassport(eQcoinSeedPassport);
//	}
//
//	@Override
//	public boolean isMeetConstraint() throws Exception {
//		if(!(transaction instanceof ModerateOPTransaction)) {
//			Log.Error("Only ModerateOPTransaction can execute ChangeTxFeeRateOP");
//			return false;
//		}
//		if(!transaction.getWitness().getPassport().getId().equals(ID.ONE)) {
//			Log.Error("Only Passport one can execute ChangeTxFeeRateOP");
//			return false;
//		}
//		return true;
//	}
//
//	@Override
//	public boolean isSanity() {
//		if(op == null) {
//			Log.Error("op == null");
//			return false;
//		}
//		if(op != OP.TXFEERATE) {
//			Log.Error("op != OP.TXFEERATE");
//			return false;
//		}
//		if(!(txFeeRate >=1 && txFeeRate<=10)) {
//			Log.Error("TxFeeRate is invalid: " + txFeeRate);
//			return false;
//		}
//		return true;
//	}
//	
//	@Override
//	public String toInnerJson() {
//		return 
//		"\"ChangeTxFeeRateOP\":" + 
//		"{\n" +
//		super.toInnerJson() + ",\n"  + 
//		"\"TxFeeRate\":" + "\"" + txFeeRate + "\""  + 
//		"\n}\n";
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.transaction.operation.Operation#parseBody(java.io.ByteArrayInputStream, com.eqchains.blockchain.transaction.Address.AddressShape)
//	 */
//	@Override
//	public void parseBody(ByteArrayInputStream is)
//			throws NoSuchFieldException, IOException, IllegalArgumentException {
//		// Parse TxFeeRate
//		txFeeRate = EQCCastle.parseBIN(is)[0];
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.blockchain.transaction.operation.Operation#getBodyBytes(com.eqchains.blockchain.transaction.Address.AddressShape)
//	 */
//	@Override
//	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
//		// Serialization TxFeeRate
//		os.write(EQCCastle.bytesToBIN(new byte[] { txFeeRate }));
//		return os;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#hashCode()
//	 */
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + txFeeRate;
//		return result;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj) {
//			return true;
//		}
//		if (!super.equals(obj)) {
//			return false;
//		}
//		if (getClass() != obj.getClass()) {
//			return false;
//		}
//		ChangeTxFeeRate other = (ChangeTxFeeRate) obj;
//		if (txFeeRate != other.txFeeRate) {
//			return false;
//		}
//		return true;
//	}
//
//	/**
//	 * @return the txFeeRate
//	 */
//	public byte getTxFeeRate() {
//		return txFeeRate;
//	}
//
//	/**
//	 * @param txFeeRate the txFeeRate to set
//	 */
//	public void setTxFeeRate(byte txFeeRate) {
//		this.txFeeRate = txFeeRate;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.transaction.operation.Operation#isValid(com.eqcoin.blockchain.changelog.ChangeLog)
//	 */
//	@Override
//	public boolean isValid() throws Exception {
//		return true;
//	}
//	
//}
