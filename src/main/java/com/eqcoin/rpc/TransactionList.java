package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.blockchain.transaction.Transaction;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.serialization.EQCType.ARRAY;

public class TransactionList<T> extends IO<T> {
	private Vector<byte[]> transactionList;
	
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
	public boolean isValid(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		ARRAY array = EQCType.parseARRAY(is);
		ByteArrayInputStream iStream = new ByteArrayInputStream(array.elements);
		for(int i=0; i<array.size; ++i) {
			transactionList.add(EQCType.parseBIN(iStream));
		}
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		if(transactionList.size() == 0) {
			return EQCType.bytesArrayToARRAY(null);
		}
		else {
			return EQCType.bytesArrayToARRAY(transactionList);
		}
	}
	
	public void addTransaction(Transaction transaction) {
		if(transaction != null) {
			transactionList.add(EQCType.bytesToBIN(transaction.getRPCBytes()));
		}
	}

	public void addAll(Vector<byte[]> transactionList) {
		this.transactionList.addAll(transactionList);
	}

	/**
	 * @return the transactionList
	 */
	public Vector<byte[]> getTransactionList() {
		return transactionList;
	}
	
}
