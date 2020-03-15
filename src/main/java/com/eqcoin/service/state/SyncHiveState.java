package com.eqcoin.service.state;

import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.service.state.EQCServiceState.State;

public class SyncHiveState extends EQCServiceState {
	private String ip;
	private EQCHive eqcHive;
	
	public SyncHiveState() {
		super(State.SYNC);
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the eqcHive
	 */
	public EQCHive getEqcHive() {
		return eqcHive;
	}

	/**
	 * @param eqcHive the eqcHive to set
	 */
	public void setEqcHive(EQCHive eqcHive) {
		this.eqcHive = eqcHive;
	}
	
}
