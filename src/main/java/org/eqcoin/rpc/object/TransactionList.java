package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.transaction.Transaction;

public class TransactionList extends IO {
	private Vector<Transaction> transactionList;
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		transactionList = new Vector<>();
	}

	public TransactionList() {
		super();
	}
	
	public <T> TransactionList(T type) throws Exception {
		super(type);
	}
	
	@Override
	public boolean isSanity() {
		if(transactionList == null) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isValid() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		transactionList = EQCCastle.parseArray(is, new Transaction());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCCastle.eqcSerializableListToArray(transactionList));
		return os;
	}

	public void addTransaction(Transaction transaction) throws Exception {
		if(transaction != null) {
			transactionList.add(transaction);
		}
	}

	public void addAll(Vector<Transaction> transactionList) {
		this.transactionList.addAll(transactionList);
	}

	/**
	 * @return the transactionList
	 */
	public Vector<Transaction> getTransactionList() {
		return transactionList;
	}
	
}
