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
package org.eqcoin.passport.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;

import org.eqcoin.hive.EQCHive;
import org.eqcoin.passport.storage.Storage;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class ExpendablePassport extends Passport {
	protected Storage storage;
	private boolean isStorageUpdate;
	
	public ExpendablePassport() throws Exception {
		super();
	}

	public ExpendablePassport(final byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public ExpendablePassport(final ResultSet resultSet) throws Exception {
		super(resultSet);
		derivedParse(resultSet);
	}
	
	protected void derivedParse(final ResultSet resultSet) throws Exception {
		storage = new Storage(resultSet.getBytes("storage"));
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#getBodyBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(final ByteArrayOutputStream os) throws Exception {
		super.getBodyBytes(os);
		os.write(storage.getBytes());
		return os;
	}
	
	/**
	 * @return the storage
	 */
	public Storage getStorage() {
		return storage;
	}

	public byte[] getStorageState() throws Exception {
		return storage.getBytes();
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#init()
	 */
	@Override
	protected void init() {
		super.init();
		storage = new Storage();
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if (!super.isSanity()) {
			return false;
		}
		if (storage == null) {
			Log.Error("storage == null");
			return false;
		}
		if (!storage.isSanity()) {
			Log.Error("!storage.isSanity()");
			return false;
		}
		return true;
	}
	
	/**
	 * @return the isStorageUpdate
	 */
	public boolean isStorageUpdate() {
		return isStorageUpdate;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(final ByteArrayInputStream is) throws NoSuchFieldException, IOException, Exception {
		super.parseBody(is);
		storage = new Storage(is);
	}
	
	public void parseStorage(final byte [] bytes) throws Exception {
		final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseStorage(is);
	}

	public void parseStorage(final ByteArrayInputStream is) throws Exception {
		storage = new Storage(is);
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#planting()
	 */
	@Override
	public void planting() throws Exception {
		super.planting();
		storage.planting();
	}
	
	@Override
	public Passport setEQCHive(final EQCHive eqcHive) {
		storage.setPassport(this);
		return super.setEQCHive(eqcHive);
	}
	
	@Override
	public void sync() {
		super.sync();
		isStorageUpdate = true;
	}
	
}
