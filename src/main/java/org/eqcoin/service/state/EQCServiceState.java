/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by Xun
 * Wang with cooperative partners are owned by Xun Wang and entitled to
 * protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang
 * reserves all rights to take any legal action and pursue any rights or remedies
 * under applicable law.
 */
package org.eqcoin.service.state;

import java.sql.Date;

import org.eqcoin.transaction.operation.Operation.OP;

/**
 * @author Xun Wang
 * @date Jul 6, 2019
 * @email 10509759@qq.com
 */
public class EQCServiceState implements Comparable<EQCServiceState> {
	protected State state;
	protected long time;
	
	public EQCServiceState(State state) {
		this.state = state;
		time = System.currentTimeMillis();
	}
	
	public enum State {
		STOP, BOOTUP,  WAIT, RUNNING, DEFAULT, ERROR, TAKE, MINER, MINING, SLEEP, PAUSE, FIND, SYNC, POSSIBLENODE, PENDINGTRANSACTION, PENDINGNEWEQCHIVE, BROADCASTNEWEQCHIVE  
	}
	
	@Override
	public int compareTo(EQCServiceState o) {
		if(state == o.state) {
			return (int) (o.time - time);
		}
		return o.state.ordinal() - state.ordinal();
	}

	/**
	 * @return the state
	 */
	public State getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(State state) {
		this.state = state;
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}
	
	public static EQCServiceState getDefaultState() {
		return new EQCServiceState(State.DEFAULT);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return state.name() + " occur at: " + new Date(time).toGMTString();
	}
	
}
