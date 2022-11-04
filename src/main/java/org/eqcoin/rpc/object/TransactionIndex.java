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
package org.eqcoin.rpc.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eqcoin.avro.O;
import org.eqcoin.serialization.EQCCastle;
import org.eqcoin.serialization.EQCObject;
import org.eqcoin.transaction.Transaction.TransactionShape;
import org.eqcoin.util.ID;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 27, 2019
 * @email 10509759@qq.com
 */
public class TransactionIndex extends IO {
	private ID id;
	private ID nonce;
	private byte[] proof;
	private TransactionIndexShape transactionIndexShape;
	
	public enum TransactionIndexShape {
		FULL, ID;
		public static TransactionIndexShape get(int ordinal) {
			TransactionIndexShape transactionIndexShape = null;
			switch (ordinal) {
			case 0:
				transactionIndexShape = TransactionIndexShape.FULL;
				break;
			case 1:
				transactionIndexShape = TransactionIndexShape.ID;
				break;
			}
			return transactionIndexShape;
		}

		public byte[] getEQCBits() {
			return EQCCastle.intToEQCBits(this.ordinal());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#init()
	 */
	@Override
	protected void init() {
		transactionIndexShape = TransactionIndexShape.FULL;
	}

	public TransactionIndex() {
		super();
	}
	
	public TransactionIndex(TransactionIndexShape transactionIndexShape) {
		super();
		this.transactionIndexShape = transactionIndexShape;
	}
	
	public TransactionIndex(ByteArrayInputStream is) throws Exception {
		super(is);
	}
	
	public <T> TransactionIndex(T type) throws Exception {
		super(type);
	}

	/* (non-Javadoc)
	 * @see com.eqchains.serialization.EQCTypable#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(transactionIndexShape == TransactionIndexShape.FULL) {
			if(proof == null || proof.length != Util.PROOF_SIZE) {
				return false;
			}
		}
		return (id != null && id.isSanity() && nonce != null && nonce.isSanity());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#Parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public <T extends EQCObject> T Parse(ByteArrayInputStream is) throws Exception {
		TransactionIndex transactionIndex = new TransactionIndex(transactionIndexShape);
		transactionIndex.parse(is);
		return (T) transactionIndex;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		id = EQCCastle.parseID(is);
		nonce = EQCCastle.parseID(is);
		if(transactionIndexShape == TransactionIndexShape.FULL) {
			proof = EQCCastle.parseNBytes(is, Util.PROOF_SIZE);
		}
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#getBytes(java.io.ByteArrayOutputStream)
	 */
	@Override
	public ByteArrayOutputStream getBytes(ByteArrayOutputStream os) throws Exception {
		os.write(id.getEQCBits());
		os.write(nonce.getEQCBits());
		if(transactionIndexShape == TransactionIndexShape.FULL) {
			os.write(proof);
		}
		return os;
	}

	/**
	 * @return the id
	 */
	public ID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(ID id) {
		this.id = id;
	}

	/**
	 * @return the nonce
	 */
	public ID getNonce() {
		return nonce;
	}

	/**
	 * @param nonce the nonce to set
	 */
	public void setNonce(ID nonce) {
		this.nonce = nonce;
	}

	/**
	 * @return the proof
	 */
	public byte[] getProof() {
		return proof;
	}

	/**
	 * @param proof the proof to set
	 */
	public void setProof(byte[] proof) {
		this.proof = proof;
	}

	/**
	 * @param transactionIndexShape the transactionIndexShape to set
	 */
	public void setTransactionIndexShape(TransactionIndexShape transactionIndexShape) {
		this.transactionIndexShape = transactionIndexShape;
	}
	
}
