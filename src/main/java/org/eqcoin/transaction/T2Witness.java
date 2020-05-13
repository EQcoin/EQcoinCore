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
package org.eqcoin.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import org.eqcoin.crypto.ECDSASignature;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public class T2Witness extends Witness {
	
	public T2Witness() {
	}
	
	public T2Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public T2Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#isSanity()
	 */
	@Override
	public boolean isSanity() {
		if(signature == null) {
			Log.Error("signature == null");
			return false;
		}
		if(signature.length != Util.P521_SIGNATURE_LEN.intValue()) {
			Log.Error("signature.length != Util.P521_SIGNATURE_LEN.intValue()");
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"T2Witness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(signature) + "\"\n" +
				"}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		signature = EQCType.parseNBytes(is, Util.P521_SIGNATURE_LEN.intValue());
	}

	public static byte[] DERToEQCSignature(byte[] derSignature) throws Exception {
		ECDSASignature ecdsaSignature = null;
		byte[] r = null, s = null;
		
		ecdsaSignature = ECDSASignature.decodeFromDER(derSignature);
		r = ecdsaSignature.getR().toByteArray();
		s = ecdsaSignature.getS().toByteArray();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(r.length < Util.P521_POINT_LEN.intValue()) {
			os.write(new byte[Util.P521_POINT_LEN.intValue() - r.length]);
		}
		os.write(r);
		if(s.length < Util.P521_POINT_LEN.intValue()) {
			os.write(new byte[Util.P521_POINT_LEN.intValue() - s.length]);
		}
		os.write(s);
		return os.toByteArray();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#getDERSignature()
	 */
	@Override
	public byte[] getDERSignature() throws Exception {
		byte[] r = null, s = null;
		ByteArrayInputStream is = new ByteArrayInputStream(signature);
		r = EQCType.parseNBytes(is, Util.P521_POINT_LEN.intValue());
		s = EQCType.parseNBytes(is, Util.P521_POINT_LEN.intValue());
		ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
		return ecdsaSignature.encodeToDER();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#setDERSignature(byte[])
	 */
	@Override
	public void setDERSignature(byte[] derSignature) throws Exception {
		signature = DERToEQCSignature(derSignature);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#getProof()
	 */
	@Override
	public byte[] getProof() {
		byte[] bytes = new byte[Util.PROOF_SIZE];
		bytes[0] = signature[0];
		bytes[1] = signature[21];
		bytes[2] = signature[42];
		bytes[3] = signature[63];
		return bytes;
	}
	
}
