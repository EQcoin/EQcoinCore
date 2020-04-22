package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.eqcoin.avro.O;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;

public class Nest extends IO {
	private ID id;
	
	public <T> Nest(T type) throws Exception {
		super(type);
	}
	
	public Nest() {
		super();
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
	public ByteArrayOutputStream getHeaderBytes(ByteArrayOutputStream os) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		return os;
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
