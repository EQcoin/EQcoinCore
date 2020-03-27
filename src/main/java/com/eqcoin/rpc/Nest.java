package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqcoin.avro.O;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;

public class Nest<T> extends IO<T> {
	private ID id;
	
	public Nest(T type) throws Exception {
		parse(type);
	}
	
	public Nest() {
	}

	@Override
	public boolean isSanity() {
		if(id == null) {
			return false;
		}
		if(!id.isSanity()) {
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
		id = EQCType.parseID(is);
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(id.getEQCBits());
		return os.toByteArray();
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}
	
}
