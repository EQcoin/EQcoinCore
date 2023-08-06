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

import org.eqcoin.avro.O;

/**
 * @author Xun Wang
 * @date Jul 7, 2019
 * @email 10509759@qq.com
 */
public class PendingTransactionState extends EQCServiceState {
	private byte[] transaction;
	
	public PendingTransactionState() {
		super(State.PENDINGTRANSACTION);
	}
	
	public PendingTransactionState(O transactionRPC) {
		super(State.PENDINGTRANSACTION);
		transaction = transactionRPC.getO().array();
	}

	/**
	 * @return the transaction
	 */
	public byte[] getTransaction() {
		return transaction;
	}

	/**
	 * @param transaction the transaction to set
	 */
	public void setTransaction(byte[] transaction) {
		this.transaction = transaction;
	}
	
}
