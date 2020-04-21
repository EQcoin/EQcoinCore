package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.changelog.Filter;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Util;

public class TransactionList<T> extends IO<T> {
	private Vector<Transaction> transactionList;
	
	public TransactionList() {
		transactionList = new Vector<>();
	}
	
	public TransactionList(T type) throws Exception {
		transactionList = new Vector<>();
		parse(type);
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

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		transactionList = EQCType.parseArray(is, new Transaction());
	}

	@Override
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(EQCType.eqcSerializableListToArray(transactionList));
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
