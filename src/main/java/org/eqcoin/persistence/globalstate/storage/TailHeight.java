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
 * Wandering Earth Corporation retains all current and future right, title and interest
 * in all of Wandering Earth Corporation’s intellectual property, including, without
 * limitation, inventions, ideas, concepts, code, discoveries, processes, marks,
 * methods, software, compositions, formulae, techniques, information and data,
 * whether or not patentable, copyrightable or protectable in trademark, and
 * any trademarks, copyright or patents based thereon.
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
 * @date Aug 11, 2020
 * @email 10509759@qq.com
 */
public class TailHeight extends GSStateVariable {
	private ID tailHeight;

	public TailHeight() {
		super();
		state = GSState.TAILHEIGHT;
	}

	public TailHeight(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		super.getBodyBytes(os);
		os.write(tailHeight.getEQCBits());
		return os;
	}

	public ID getTailHeight() {
		return tailHeight;
	}

	public ID getUpdateHeight() {
		return height;
	}

	@Override
	public boolean isSanity() throws Exception {
		if(!super.isSanity()) {
			return false;
		}
		if(state != GSState.TAILHEIGHT) {
			Log.Error("state != GSState.TAILHEIGHT");
			return false;
		}
		if(tailHeight == null) {
			Log.Error("tailHeight == null");
			return false;
		}
		if (!tailHeight.isSanity()) {
			Log.Error("!tailHeight.isSanity()");
			return false;
		}
		return true;
	}

	@Override
	public void parseBody(final ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		tailHeight = EQCCastle.parseID(is);
	}

	public void setTailHeight(final ID tailHeight) {
		this.tailHeight = tailHeight;
	}

	public void setUpdateHeight(final ID updateHeight) {
		this.height = updateHeight;
	}

}
