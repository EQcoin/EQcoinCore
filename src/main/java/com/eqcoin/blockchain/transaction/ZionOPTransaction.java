/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import com.eqcoin.blockchain.lock.EQCLockMate;
import com.eqcoin.blockchain.passport.AssetPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.blockchain.seed.EQcoinSeed;
import com.eqcoin.blockchain.transaction.Transaction.TransactionType;
import com.eqcoin.blockchain.transaction.operation.Operation;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.blockchain.transaction.operation.ChangeLockOP;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

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

	public ZionOPTransaction(byte[] bytes)
			throws Exception {
		super(bytes);
	}

	public ZionOPTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public boolean isValid()
			throws NoSuchFieldException, IllegalStateException, IOException, Exception {
		return (super.isValid() && operation.isMeetPreconditions() && operation.isValid());
	}

	public String toInnerJson() {
		return
		"\"ZionOPTransaction\":" + "\n{\n" 
				+ txIn.toInnerJson() + ",\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"TxOutList\":" + "\n{\n" + "\"Size\":" + "\"" + txOutList.size() + "\"" + ",\n"
				+ "\"List\":" + "\n" + getTxOutString() + "\n},\n"
				+ operation.toInnerJson() + ",\n"
				+ eqcWitness.toInnerJson()
				+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		// TODO Auto-generated method stub
		return transactionType == TransactionType.ZIONOP;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isDerivedSanity(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public boolean isDerivedSanity() throws Exception {
		return (super.isDerivedSanity() && operation != null && operation.isSanity());
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
		operation = new Operation(this).Parse(is);
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
