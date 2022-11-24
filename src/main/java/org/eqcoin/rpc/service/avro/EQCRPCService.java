/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.rpc.service.avro;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.avro.ipc.Server;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public abstract class EQCRPCService {
	protected Server server;
	protected final AtomicBoolean isRunning = new AtomicBoolean(false);

	public synchronized void start() {
		Log.info("Starting " + this.getClass().getSimpleName());
		if (server != null) {
			server.close();
		}
	}
	
	public synchronized void stop() {
		Log.info("Begin stop " + this.getClass().getSimpleName() + "'s NettyServer");
		close();
		Log.info(this.getClass().getSimpleName() + "'s NettyServer stopped...");
	}
	
	private void close() {
		if(server != null) {
			server.close();
			server = null;
			isRunning.set(false);
		}
	}
	
	public final boolean isRunning() {
		return isRunning.get();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		close();
	}
	
}
