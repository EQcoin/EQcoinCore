package org.eqcoin.stateobject;

import org.eqcoin.serialization.EQCObject;

public abstract class StateObject extends EQCObject {

	public StateObject() {
	}

	public StateObject(final byte[] bytes) throws Exception {
		super(bytes);
	}

	abstract public <K> K getKey() throws Exception;

	abstract public <V> V getValue() throws Exception;
	
	abstract public byte[] getHash() throws Exception;

}
