package org.eqcoin.rpc.object;
///**
// * EQcoin core
// * @copyright 2018-present Wandering Earth Corporation All rights reserved...
// * Copyright of all works released by Wandering Earth Corporation or jointly released by
// * Wandering Earth Corporation with cooperative partners are owned by Wandering Earth Corporation
// * and entitled to protection available from copyright law by country as well as
// * international conventions.
// * Attribution — You must give appropriate credit, provide a link to the license.
// * Non Commercial — You may not use the material for commercial purposes.
// * No Derivatives — If you remix, transform, or build upon the material, you may
// * not distribute the modified material.
// * For any use of above stated content of copyright beyond the scope of fair use
// * or without prior written permission, Wandering Earth Corporation reserves all rights to
// * take any legal action and pursue any right or remedy available under applicable
// * law.
// * http://www.eqcoin.org
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//package org.eqcoin.rpc;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//
//import org.eqcoin.avro.O;
//import org.eqcoin.serialization.EQCInheritable;
//import org.eqcoin.serialization.EQCTypable;
//import org.eqcoin.serialization.EQCType;
//import org.eqcoin.util.ID;
//import org.eqcoin.util.Util;
//
///**
// * @author Xun Wang
// * @date Jun 25, 2019
// * @email 10509759@qq.com
// */
//@Deprecated // Instead use SP
//public class Cookie extends IO {
//	private SP sp;
//	private ID version;
//	
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#init()
//	 */
//	@Override
//	protected void init() {
//		sp = Util.LOCAL_SP;
//		version = Util.PROTOCOL_VERSION;
//	}
//
//	public Cookie() {
//		super();
//	}
//	
//	public Cookie(ByteArrayInputStream is) throws Exception {
//		super(is);
//	}
//	
//	public <T> Cookie(T type) throws Exception {
//		super(type);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.serialization.EQCTypable#isSanity()
//	 */
//	@Override
//	public boolean isSanity() {
//		if(version == null || !version.equals(Util.PROTOCOL_VERSION)) {
//			return false;
//		}
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
//	 */
//	@Override
//	public boolean isValid() throws Exception {
//		return false;
//	}
//
//	/**
//	 * @return the sp
//	 */
//	public SP getSp() {
//		return sp;
//	}
//
//	/**
//	 * @param sp the sp to set
//	 */
//	public void setSp(SP sp) {
//		this.sp = sp;
//	}
//
//	/**
//	 * @return the version
//	 */
//	public ID getVersion() {
//		return version;
//	}
//
//	/**
//	 * @param version the version to set
//	 */
//	public void setVersion(ID version) {
//		this.version = version;
//	}
//
//	public boolean isIPNull() {
//		// Here need do more job to check if the sp format is valid
//		return sp == null || sp.getIp().isEmpty();
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
//	 */
//	@Override
//	public void parse(ByteArrayInputStream is) throws Exception {
//		sp = new SP(is);
//		version = EQCType.parseID(is);
//	}
//
//	/* (non-Javadoc)
//	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
//	 */
//	@Override
//	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
//		os.write(sp.getBytes());
//		os.write(version.getEQCBits());
//		return os;
//	}
//
//	/* (non-Javadoc)
//	 * @see java.lang.Object#toString()
//	 */
//	@Override
//	public String toString() {
//		return "{\"Cookie\":{" + sp + "\", \"version\":\"" + version + "\"}}";
//	}
//
//}
