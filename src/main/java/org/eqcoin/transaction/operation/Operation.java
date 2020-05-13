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
package org.eqcoin.transaction.operation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eqcoin.lock.LockMate;
import org.eqcoin.serialization.EQCInheritable;
import org.eqcoin.serialization.EQCSerializable;
import org.eqcoin.serialization.EQCTypable;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.transaction.Transaction;
import org.eqcoin.transaction.TransferOPTransaction;
import org.eqcoin.transaction.Value;
import org.eqcoin.transaction.operation.Operation.OP;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Mar 27, 2019
 * @email 10509759@qq.com
 */
public class Operation  extends EQCSerializable {
	public enum OP {
		LOCK, CHECKPOINT, BLOCKINTERVAL, MAXBLOCKSIZE, TXFEERATE, UPDATESCRIPT;
		public static OP get(int ordinal) {
			OP op = null;
			switch (ordinal) {
			case 0:
				op = OP.LOCK;
				break;
			case 1:
				op = OP.CHECKPOINT;
				break;
			case 2:
				op = OP.BLOCKINTERVAL;
				break;
			case 3:
				op = OP.MAXBLOCKSIZE;
				break;
			case 4:
				op = OP.TXFEERATE;
				break;
			case 5:
				op = OP.UPDATESCRIPT;
				break;
			}
			return op;
		}
	}

	protected OP op;
	protected Transaction transaction;

	public Operation() throws Exception {
		super();
	}
	
	public Operation(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public Operation(ByteArrayInputStream is) throws Exception {
		parse(is);
	}
	
	public void planting() throws Exception {
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

	// Due to the expand ability so here need use isMeetPreconditions
	public boolean isMeetPreconditions() throws Exception {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public Operation Parse(ByteArrayInputStream is) throws Exception {
		Operation operation = null;
		OP op = parseOP(is);
		if (op == OP.LOCK) {
			operation = new ChangeLock();
		} else if (op == OP.CHECKPOINT) {
			operation = new ChangeCheckPoint();
		} 
		operation.setTransaction(transaction);
		operation.parse(is);
		return operation;
	}

	public OP parseOP(ByteArrayInputStream is) throws Exception {
		OP op = null;
		int opCode = -1;
		try {
			is.mark(0);
			opCode = EQCType.eqcBitsToInt(EQCType.parseEQCBits(is));
			op = OP.get(opCode);
			if(op == null) {
				throw new IllegalStateException("OP shouldn't null.");
			}
		} finally {
			is.reset();
		}
		return op;
	}

	@Override
	public String toInnerJson() {
		return 
		"\"OP\":" + "\"" + op + "\"";
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
			// Serialization OP
			os.write(EQCType.longToEQCBits(op.ordinal()));
		return os;
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
	 * @return the op
	 */
	public OP getOp() {
		return op;
	}
	
	public Value getProofLength() {
		return Value.ZERO;
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
	public Operation setTransaction(Transaction transaction) {
		this.transaction = transaction;
		return this;
	}
	
}
