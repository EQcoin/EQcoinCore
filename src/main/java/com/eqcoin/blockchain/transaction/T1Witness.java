/**
 * EQchains core - EQchains Foundation's EQchains core library
 * @copyright 2018-present EQchains Foundation All rights reserved...
 * Copyright of all works released by EQchains Foundation or jointly released by
 * EQchains Foundation with cooperative partners are owned by EQchains Foundation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Foundation reserves all rights to
 * take any legal action and pursue any right or remedy available under applicable
 * law.
 * https://www.eqchains.com
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
package com.eqcoin.blockchain.transaction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import com.eqcoin.crypto.ECDSASignature;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public class T1Witness extends EQCWitness {
	
	public T1Witness() {
	}
	
	public T1Witness(byte[] bytes) throws Exception {
		super(bytes);
	}
	
	public T1Witness(ByteArrayInputStream is) throws Exception {
		parse(is);
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#isSanity()
	 */
	@Override
	public boolean isSanity() {
		return (super.isSanity() && eqcSignature.length == Util.P256_SIGNATURE_LEN.intValue());
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#toInnerJson()
	 */
	@Override
	public String toInnerJson() {
		return 
				"\"T1Witness\":" + 
				"\n{\n" +
					"\"Signature\":\"" + Util.bytesToHexString(eqcSignature) + "\"\n" +
				"}";
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.serialization.EQCSerializable#parse(java.io.ByteArrayInputStream)
	 */
	@Override
	public void parse(ByteArrayInputStream is) throws Exception {
		eqcSignature = EQCType.parseNBytes(is, Util.P256_POINT_LEN.intValue());
	}
	
	public static byte[] DERToEQCSignature(byte[] derSignature) throws Exception {
		ECDSASignature ecdsaSignature = null;
		byte[] r = null, s = null;
		
		ecdsaSignature = ECDSASignature.decodeFromDER(derSignature);
		r = ecdsaSignature.getR().toByteArray();
		s = ecdsaSignature.getS().toByteArray();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		if(r.length < Util.P256_POINT_LEN.intValue()) {
			os.write(new byte[Util.P256_POINT_LEN.intValue() - r.length]);
		}
		os.write(r);
		if(s.length < Util.P256_POINT_LEN.intValue()) {
			os.write(new byte[Util.P256_POINT_LEN.intValue() - s.length]);
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
		ByteArrayInputStream is = new ByteArrayInputStream(eqcSignature);
		r = EQCType.parseNBytes(is, Util.P256_POINT_LEN.intValue());
		s = EQCType.parseNBytes(is, Util.P256_POINT_LEN.intValue());
		ECDSASignature ecdsaSignature = new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
		return ecdsaSignature.encodeToDER();
	}

	/* (non-Javadoc)
	 * @see com.eqcoin.blockchain.transaction.EQCWitness#setDERSignature(byte[])
	 */
	@Override
	public void setDERSignature(byte[] derSignature) throws Exception {
		eqcSignature = DERToEQCSignature(derSignature);
	}
	
}
