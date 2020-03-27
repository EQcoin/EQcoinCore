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
package com.eqcoin.blockchain.transaction.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.blockchain.transaction.TransferOPTransaction;
import com.eqcoin.blockchain.transaction.operation.Operation.OP;
import com.eqcoin.serialization.EQCInheritable;
import com.eqcoin.serialization.EQCLockShapeInheritable;
import com.eqcoin.serialization.EQCLockShapeTypable;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
// Due to the Lock's state need represent 3 styles(ID, AI, Readable) so here must use EQCLockShapeTypable & EQCLockShapeInheritable
// Due to the expandability so here need use isMeetPreconditions
public abstract class Operation implements EQCTypable, EQCInheritable {
	public enum OP {
		PUBLICKEY, LOCK, CHECKPOINT, BLOCKINTERVAL, MAXBLOCKSIZE, TXFEERATE, UPDATESCRIPT;
		public static OP get(int ordinal) {
			OP op = null;
			switch (ordinal) {
			case 0:
				op = OP.PUBLICKEY;
				break;
			case 1:
				op = OP.LOCK;
				break;
			case 3:
				op = OP.CHECKPOINT;
				break;
			case 4:
				op = OP.BLOCKINTERVAL;
				break;
			case 5:
				op = OP.MAXBLOCKSIZE;
				break;
			case 6:
				op = OP.TXFEERATE;
				break;
			case 7:
				op = OP.UPDATESCRIPT;
				break;
			}
			return op;
		}
	}

	protected OP op;
	protected Transaction transaction;

	public Operation() {
	}
	
	public Operation(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCInheritable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		parseHeader(is);
		parseBody(is);
	}

	public void execute() throws Exception {
	}
	
	/**
	 * @return the op
	 */
	public OP getOP() {
		return op;
	}

	/**
	 * @param op the op to set
	 */
	public void setOP(OP op) {
		this.op = op;
	}

	public boolean isMeetPreconditions() throws Exception {
		return true;
	}

	public static OP parseOP(ByteArrayInputStream is) throws NoSuchFieldException, IllegalStateException, IOException {
		is.mark(0);
		OP op = null;
		int opCode = -1;
		opCode = EQCType.eqcBitsToInt(EQCType.parseEQCBits(is));
		op = OP.get(opCode);
		is.reset();
		return op;
	}

	public static Operation parseOperation(ByteArrayInputStream is) throws Exception {
		Operation operation = null;
		OP op = parseOP(is);

		if (op == OP.LOCK) {
			operation = new UpdateLockOP(is);
		} else if (op == OP.CHECKPOINT) {
			
		} else if (op == OP.TXFEERATE) {
			operation = new UpdateTxFeeRateOP();
		} 
		return operation;
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

	public String toInnerJson() {
		return null;
	}

	@Override
	public byte[] getBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization Header
			os.write(getHeaderBytes());
			// Serialization Body
			os.write(getBodyBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBin() {
		return EQCType.bytesToBIN(getBytes());
	}

	@Override
	public boolean isSanity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseBody(ByteArrayInputStream is)
			throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] getHeaderBytes() {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			// Serialization OP
			os.write(EQCType.longToEQCBits(op.ordinal()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return os.toByteArray();
	}

	@Override
	public byte[] getBodyBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is)
			throws NoSuchFieldException, IOException, IllegalArgumentException {
		// Parse OP
		op = OP.get(EQCType.eqcBitsToInt(EQCType.parseEQCBits(is)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((op == null) ? 0 : op.hashCode());
		result = prime * result + ((transaction == null) ? 0 : transaction.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Operation other = (Operation) obj;
		if (op != other.op) {
			return false;
		}
		if (transaction == null) {
			if (other.transaction != null) {
				return false;
			}
		} else if (!transaction.equals(other.transaction)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the transaction
	 */
	public Transaction getTransaction() {
		return transaction;
	}

	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * @return the op
	 */
	public OP getOp() {
		return op;
	}
	
}
