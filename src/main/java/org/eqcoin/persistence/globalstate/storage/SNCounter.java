/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * Copyright of all works released by Wandering Earth Corporation or jointly
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
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.persistence.globalstate.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date Aug 26, 2020
 * @email 10509759@qq.com
 */
public class SNCounter extends GSStateVariable {
	private ID snCounter;

	public SNCounter() {
		super();
		state = GSState.SNCOUNTER;
	}

	public SNCounter(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		os.write(snCounter.getEQCBits());
		return os;
	}

	public ID getSnCounter() {
		return snCounter;
	}

	public ID getUpdateHeight() {
		return height;
	}

	@Override
	public boolean isSanity() throws Exception {
		if (!super.isSanity()) {
			return false;
		}
		if (state != GSState.TAILHEIGHT) {
			Log.Error("state != GSState.TAILHEIGHT");
			return false;
		}
		if (snCounter == null) {
			Log.Error("snCounter == null");
			return false;
		}
		if (!snCounter.isSanity()) {
			Log.Error("!snCounter.isSanity()");
			return false;
		}
		return true;
	}

	@Override
	public void parseBody(final ByteArrayInputStream is) throws Exception {
		snCounter = EQCCastle.parseID(is);
	}

	public void setSnCounter(final ID snCounter) {
		this.snCounter = snCounter;
	}

	public void setUpdateHeight(final ID updateHeight) {
		this.height = updateHeight;
	}

}
