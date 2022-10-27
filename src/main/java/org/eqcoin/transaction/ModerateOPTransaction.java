/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
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

import org.eqcoin.protocol.EQCProtocol;
import org.eqcoin.transaction.operation.Operation;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Mar 21, 2020
 * @email 10509759@qq.com
 */
public class ModerateOPTransaction extends Transaction {
	private Operation operation;

	public ModerateOPTransaction() {
		super();
		transactionType = TransactionType.ZIONOP;
	}

	public ModerateOPTransaction(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	@Override
	public boolean isValid() throws Exception {
		if(operation == null) {
			Log.Error("operation == null");
			return false;
		}
		EQCProtocol eqcProtocol = null;
		if (!operation.isMeetConstraint(eqcProtocol)) {
			Log.Error("Operation " + operation + " doesn't meet preconditions.");
			return false;
		}
		if (!operation.isValid()) {
			Log.Error("Operation " + operation + " isn't valid.");
			return false;
		}
		if (!super.isValid()) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModerateOPTransaction other = (ModerateOPTransaction) obj;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		return true;
	}
	
	public String toInnerJson() {
		return
		"\"ModerateOPTransaction\":" + "\n{\n" + statusInnerJson() + ",\n"
				+ operation.toInnerJson() + ",\n"
				+ "\"Nonce\":" + "\"" + nonce + "\"" + ",\n"
				+ "\"EQCWitness\":" + witness.toInnerJson() 
				+ "\n" + "}";
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isTransactionTypeSanity()
	 */
	@Override
	protected boolean isTransactionTypeSanity() {
		if(transactionType != TransactionType.MODERATEOP) {
			Log.Error("transactionType != TransactionType.MODERATEOP");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#isDerivedSanity(com.eqcoin.blockchain.transaction.Transaction.TransactionShape)
	 */
	@Override
	public boolean isDerivedSanity() throws Exception {
		if(operation == null) {
			Log.Error("operation == null");
			return false;
		}
//		if(!(operation instanceof ChangeCheckPoint)) {
//			Log.Error("!(operation instanceof ChangeCheckPointOP)");
//			return false;
//		}
		if (!operation.isSanity()) {
			Log.Error("!operation.isSanity()");
			return false;
		}
		if(!getTxFee().isSanity()) {
			Log.Error("!getTxFee().isSanity()");
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.TransferTransaction#derivedPlanting()
	 */
	@Override
	protected void derivedPlanting() throws Exception {
		super.derivedPlanting();
		operation.planting();
	}

	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		// Serialization Super body
		super.getBodyBytes(os);
		// Serialization Operation
		os.write(operation.getBytes());
		return os;
	}
	
	public void parseBody(ByteArrayInputStream is) throws Exception {
		// Parse Super body
		super.parseBody(is);
		// Parse Operation
		operation = new Operation().setTransaction(this).Parse(is);
	}

}
