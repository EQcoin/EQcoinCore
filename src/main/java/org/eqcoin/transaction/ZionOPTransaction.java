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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import org.eqcoin.lock.LockMate;
import org.eqcoin.seeds.EQCSeeds;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.stateobject.passport.AssetPassport;
import org.eqcoin.stateobject.passport.Passport;
import org.eqcoin.transaction.Transaction.TransactionType;
import org.eqcoin.transaction.operation.ChangeLock;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.transaction.operation.Operation.OP;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

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
		if(!operation.isMeetConstraint()) {
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
