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
public abstract class Adjacent extends GSStateVariable {
	private ID preSN;
	private ID initialHeight;

	public Adjacent() {
		super();
	}

	public Adjacent(final ByteArrayInputStream is) throws Exception {
		super(is);
	}

	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		super.getBodyBytes(os);
		os.write(preSN.getEQCBits());
		os.write(initialHeight.getEQCBits());
		return os;
	}

	public ID getCreateHeight() {
		return height;
	}

	public ID getInitialHeight() {
		return initialHeight;
	}

	public ID getPreSN() {
		return preSN;
	}

	@Override
	public boolean isSanity() throws Exception {
		if (!super.isSanity()) {
			return false;
		}
		if (preSN == null) {
			Log.Error("preSN == null");
			return false;
		}
		if (!preSN.isSanity()) {
			Log.Error("!preSN.isSanity()");
			return false;
		}
		if (initialHeight == null) {
			Log.Error("initialHeight == null");
			return false;
		}
		if (!initialHeight.isSanity()) {
			Log.Error("!initialHeight.isSanity()");
			return false;
		}
		return true;
	}

	@Override
	public void parseBody(final ByteArrayInputStream is) throws Exception {
		super.parseBody(is);
		preSN = EQCCastle.parseID(is);
		initialHeight = EQCCastle.parseID(is);
	}

	public void setCreateHeight(final ID createHeight) {
		this.height = createHeight;
	}

	public void setInitialHeight(final ID initialHeight) {
		this.initialHeight = initialHeight;
	}

	public void setPreSN(final ID preHeight) {
		this.preSN = preHeight;
	}

}
