/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 * @copyright 2018-present EQcoin Federation All rights reserved...
 * Copyright of all works released by EQcoin Federation or jointly released by
 * EQcoin Federation with cooperative partners are owned by EQcoin Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Federation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqcoin.org
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.passport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eqcoin.changelog.ChangeLog;
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
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#init()
	 */
	@Override
	protected void init() {
		super.init();
		storage = new Storage();
	}

	public ExpendablePassport() throws Exception {
		super();
	}
	
	public ExpendablePassport(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public ExpendablePassport(ResultSet resultSet) throws Exception {
		super(resultSet);
		derivedParse(resultSet);
	}
	
	protected void derivedParse(ResultSet resultSet) throws Exception {
		storage = new Storage(resultSet.getBytes("storage"));
	}
	
	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws NoSuchFieldException, IOException, Exception {
		super.parseBody(is);
		storage = new Storage(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#getBodyBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		super.getBodyBytes(os);
		os.write(storage.getBytes());
		return os;
	}
	
	public void parseStorage(byte [] bytes) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		parseStorage(is);
	}
	
	public void parseStorage(ByteArrayInputStream is) throws Exception {
		storage = new Storage(is);
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

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#planting()
	 */
	@Override
	public void planting() throws Exception {
		storage.planting();
		super.planting();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.Passport#setChangeLog(org.eqcoin.changelog.ChangeLog)
	 */
	@Override
	public Passport setChangeLog(ChangeLog changeLog) {
		storage.setPassport(this);
		return super.setChangeLog(changeLog);
	}

	/**
	 * @return the storage
	 */
	public Storage getStorage() {
		return storage;
	}
	
	/**
	 * @return the isStorageUpdate
	 */
	public boolean isStorageUpdate() {
		return isStorageUpdate;
	}
	
	public void sync() {
		super.sync();
		isStorageUpdate = true;
	}
	
	public byte[] getStorageState() throws Exception {
		return storage.getBytes();
	}
	
}
