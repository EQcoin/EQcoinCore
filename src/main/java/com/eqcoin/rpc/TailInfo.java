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
package com.eqcoin.rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import com.eqcoin.avro.O;
import com.eqcoin.blockchain.changelog.ChangeLog;
import com.eqcoin.serialization.EQCTypable;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.ID;

/**
 * @author Xun Wang
 * @date Jun 24, 2019
 * @email 10509759@qq.com
 */
public class TailInfo<T> extends IO<T> implements Comparable<TailInfo> {
	private ID height;
	private ID checkPointHeight;
	private byte[] blockTailProof;
	private String ip;

	public TailInfo() {
	}
	
	public TailInfo(T type) throws Exception {
		parse(type);
	}
	
	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(height == null || checkPointHeight == null) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isValid(com.eqchains.blockchain.accountsmerkletree.AccountsMerkleTree)
	 */
	@Override
	public boolean isValid(ChangeLog changeLog) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @return the height
	 */
	public ID getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(ID height) {
		this.height = height;
	}

	/**
	 * @return the checkPointHeight
	 */
	public ID getCheckPointHeight() {
		return checkPointHeight;
	}

	/**
	 * @param checkPointHeight the checkPointHeight to set
	 */
	public void setCheckPointHeight(ID checkPointHeight) {
		this.checkPointHeight = checkPointHeight;
	}

	@Override
	public void parseHeader(ByteArrayInputStream is) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parseBody(ByteArrayInputStream is) throws Exception {
		height = new ID(EQCType.parseEQCBits(is));
		checkPointHeight = new ID(EQCType.parseEQCBits(is));
		blockTailProof = EQCType.parseBIN(is);
	}

	@Override
	public byte[] getHeaderBytes() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBodyBytes() throws Exception {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(height.getEQCBits());
		os.write(checkPointHeight.getEQCBits());
		os.write(EQCType.bytesToBIN(blockTailProof));
		return os.toByteArray();
	}

	/**
	 * @return the blockTailProof
	 */
	public byte[] getBlockTailProof() {
		return blockTailProof;
	}

	/**
	 * @param blockTailProof the blockTailProof to set
	 */
	public void setBlockTailProof(byte[] blockTailProof) {
		this.blockTailProof = blockTailProof;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		TailInfo tailInfo = (TailInfo) obj;
		return height.equals(tailInfo.height) && checkPointHeight.equals(tailInfo.checkPointHeight) && Arrays.equals(blockTailProof, tailInfo.blockTailProof);
	}

	@Override
	public int compareTo(TailInfo o) {
		if(!checkPointHeight.equals(o.checkPointHeight)) {
			return checkPointHeight.intValue() - o.checkPointHeight.intValue();
		}
		else {
			return height.intValue() - o.height.intValue();
		}
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TailInfo [height=" + height + ", checkPointHeight=" + checkPointHeight + ", blockTailProof="
				+ Arrays.toString(blockTailProof) + ", ip=" + ip + "]";
	}
	
}
