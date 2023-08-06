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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.protocol.EQCProtocol;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Mar 11, 2020
 * @email 10509759@qq.com
 */
public class ZionOPTransaction extends ZionTransaction {

	public ZionOPTransaction() {
		super();
		transactionType = TransactionType.ZIONOP;
	}

	public ZionOPTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public boolean isValid() throws Exception {
		if(!super.isValid()) {
			return false;
		}
		if(operation == null) {
			Log.Error("operation == null");
			return false;
		}
		EQCProtocol eqcProtocol = null;
		if(!operation.isMeetConstraint(eqcProtocol)) {
			Log.Error("!operation.isMeetPreconditions()");
			return false;
		}
		if(!operation.isValid()) {
			Log.Error("!operation.isValid()");
			return false;
		}
		return true;
	}

	public String toInnerJson() {
		return
		"\"ZionOPTransaction\":" + "\n{\n" 
				+ statusInnerJson() + ",\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ operation.toInnerJson() + ",\n"
				+ witness.toInnerJson()
				+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		if(transactionType == null) {
			Log.Error("transactionType == null");
			return false;
		}
		if(transactionType != TransactionType.ZIONOP) {
			Log.Error("transactionType != TransactionType.ZIONOP");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isDerivedSanity(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public boolean isDerivedSanity() throws Exception {
		if(!super.isDerivedSanity()) {
			return false;
		}
		if(operation == null) {
			Log.Error("operation == null");
			return false;
		}
		if(!operation.isSanity()) {
			Log.Error("!operation.isSanity()");
			return false;
		}
		return true;
	}
	
	@Override
	protected void derivedPlanting() throws Exception {
		super.derivedPlanting();
		operation.planting();
	}

	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
			// Serialization Super body
			super.getBodyBytes(os);
			os.write(operation.getBytes());
		return os;
	}
	
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse Super body
		super.parseBody(is);
		// Parse Operation
		operation = new Operation().setTransaction(this).Parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedTxOutPlanting()
	 */
	@Override
	protected void derivedTxOutPlanting() throws Exception {
		super.derivedTxOutPlanting();
		operation.planting();
	}

//	// Here exists one bug still doesn't know how to fix it
//	/* (non-Javadoc)
//	 * @see com.eqcoin.blockchain.transaction.ZionTransaction#getProofLength()
//	 */
//	@Override
//	protected Value getProofLength() throws Exception  {
//		Value value = null;
////		try {
//			value = new Value(EQCType.eqcSerializableListToArray(operationList).length);
//			value = super.getProofLength().add(value);
////		} catch (Exception e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//		return value;
//	}
	
}
