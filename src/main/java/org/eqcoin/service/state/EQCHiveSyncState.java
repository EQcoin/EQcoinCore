package org.eqcoin.service.state;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.rpc.SP;
import org.eqcoin.service.state.EQCServiceState.State;

public class EQCHiveSyncState extends EQCServiceState {
	private SP sp;
	private EQCHive eqcHive;
	
	public EQCHiveSyncState() {
		super(State.SYNC);
	}

	/**
	 * @return the ip
	 */
	public SP getSp() {
		return sp;
	}

	/**
	 * @param sp the sp to set
	 */
	public void setSp(SP sp) {
		this.sp = sp;
	}

	/**
	 * @return the eqcHive
	 */
	public EQCHive getEQCHive() {
		return eqcHive;
	}

	/**
	 * @param eqcHive the eqcHive to set
	 */
	public void setEQCHive(EQCHive eqcHive) {
		this.eqcHive = eqcHive;
	}
	
}
