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
package org.eqcoin.passport.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;

/**
 * @author Xun Wang
 * @date May 4, 2020
 * @email 10509759@qq.com
 */
public class UpdateHeight extends StateVariable {
	private ID updateHeight;
	
	public UpdateHeight() {}
	
	public UpdateHeight(ByteArrayInputStream is) throws Exception {
		super(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.storage.StateVariable#isSanity()
	 */
	@Override
	public boolean isSanity() throws Exception {
		if(!super.isSanity()) {
			return false;
		}
		if(updateHeight == null) {
			Log.Error("updateHeight == null");
			return false;
		}
		if(!updateHeight.isSanity()) {
			Log.Error("!updateHeight.isSanity()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.storage.StateVariable#isMeetPreconditions()
	 */
	@Override
	public boolean isMeetPreconditions() throws Exception {
		if (!(passport.getId().equals(ID.ZERO))) {
			Log.Error("Passport isn't EQcoinRootPassport or relevant lock doesn't support update height op");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.passport.storage.StateVariable#planting()
	 */
	@Override
	public void planting() throws Exception {
		updateHeight = passport.getChangeLog().getHeight();
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#parseBody(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		updateHeight = EQCType.parseID(is);
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#getBodyBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBodyBytes(ByteArrayOutputStream os) throws Exception {
		os.write(updateHeight.getEQCBits());
		return os;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		state = STATE.UPDATEHEIGHT;
	}

	/* (non-Javadoc)
	 * @see org.eqcoin.serialization.EQCSerializable#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return "\"UpdateHeight\":" + "{\n" + 
				super.toInnerJson() + ",\n" +
				"\"UpdateHeight\":" + "\"" + updateHeight + "\"" +
				"\n}";
	}
	
}
