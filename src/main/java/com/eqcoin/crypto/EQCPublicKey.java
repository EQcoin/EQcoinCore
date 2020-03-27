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
package com.eqcoin.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import com.eqcoin.keystore.Keystore;
import com.eqcoin.keystore.Keystore.ECCTYPE;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * EQCPublicKey is an EQC tool you can use it as PublicKey to verify the
 * signature or get the PublicKey's compressed encode
 * 
 * @author Xun Wang
 * @date Sep 26, 2018
 * @email 10509759@qq.com
 */
public class EQCPublicKey implements PublicKey {
	private static final long serialVersionUID = 1303765568188200263L;
	/**
	 * The parameters of the secp256r1 or secp521r1 curve that EQCOIN uses.
	 */
	private ECDomainParameters CURVE;
	// All clients must agree on the curve to use by agreement. EQCOIN uses
	// secp256r1 or secp521r1.
	private X9ECParameters params;
	private ECPoint ecPoint;

	// For java standard EC
	ECNamedCurveParameterSpec spec;
	ECNamedCurveSpec ecParams;
	ECPublicKey pk;

	public EQCPublicKey(ECCTYPE type) {
		if (type == ECCTYPE.P256) {
			params = SECNamedCurves.getByName(Keystore.SECP256R1);
			CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP256R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP256R1, spec.getCurve(), spec.getG(), spec.getN());
		} else if (type == ECCTYPE.P521) {
			params = SECNamedCurves.getByName(Keystore.SECP521R1);
			CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
			spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP521R1);
			ecParams = new ECNamedCurveSpec(Keystore.SECP521R1, spec.getCurve(), spec.getG(), spec.getN());
		}
	}

	/**
	 * Construct EQPublicKey using the official ECPublicKey of java then you can use
	 * EQPublicKey get the compressed public key
	 * 
	 * @param ecPublicKey The interface to an elliptic curve (EC) public key
	 */
	public void setECPoint(final ECPublicKey ecPublicKey) {
		final java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
		final BigInteger xCoord = publicPointW.getAffineX();
		final BigInteger yCoord = publicPointW.getAffineY();
		ecPoint = CURVE.getCurve().createPoint(xCoord, yCoord);
	}

	/**
	 * Construct an EQPublicKey with a compressed public key then you can use it to
	 * verify the signature
	 * 
	 * @param bytes Compressed public key
	 */
	public void setECPoint(final byte[] compressedPublicKey) {
		ecPoint = CURVE.getCurve().decodePoint(compressedPublicKey);
		ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true));
		KeyFactory kf = null;
		try {
			kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
				ECPointUtil.decodePoint(ecParams.getCurve(), ecPoint.getEncoded(true)), ecParams);
		try {
			pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
	}

	/**
	 * @return
	 */
	public byte[] getCompressedPublicKeyEncoded() {
		return ecPoint.getEncoded(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getAlgorithm()
	 */
	@Override
	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return "EC";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		return "X.509";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.security.Key#getEncoded()
	 */
	@Override
	public byte[] getEncoded() {
		return pk.getEncoded();
	}

//	public byte[] getPublicEncodedFromSignature(byte[] signature) {
//		
//	}
	
//	  public  byte[] recoverPubBytesFromSignature(int recId, byte[] sig, byte[] messageHash) {
////	        check(recId >= 0, "recId must be positive");
////	        check(sig.r.signum() >= 0, "r must be positive");
////	        check(sig.s.signum() >= 0, "s must be positive");
////	        check(messageHash != null, "messageHash must not be null");
//	        // 1.0 For j from 0 to h   (h == recId here and the loop is outside this function)
//	        //   1.1 Let x = r + jn
//		  	BigInteger r , s;
//		  	r = extractR(sig);
//		  	s = extractS(sig);
//	        BigInteger n = CURVE.getN();  // Curve order.
//	        BigInteger i = BigInteger.valueOf((long) recId / 2);
//	        BigInteger x = r.add(i.multiply(n));
//	        //   1.2. Convert the integer x to an octet string X of length mlen using the conversion routine
//	        //        specified in Section 2.3.7, where mlen = ⌈(log2 p)/8⌉ or mlen = ⌈m/8⌉.
//	        //   1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve point R using the
//	        //        conversion routine specified in Section 2.3.4. If this conversion routine outputs “invalid”, then
//	        //        do another iteration of Step 1.
//	        //
//	        // More concisely, what these points mean is to use X as a compressed public key.
//	        ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
//	        BigInteger prime = curve.getQ();  // Bouncy Castle is not consistent about the letter it uses for the prime.
//	        if (x.compareTo(prime) >= 0) {
//	            // Cannot have point co-ordinates larger than this as everything takes place modulo Q.
//	            return null;
//	        }
//	        // Compressed keys require you to know an extra bit of data about the y-coord as there are two possibilities.
//	        // So it's encoded in the recId.
//	        ECPoint R = decompressKey(x, (recId & 1) == 1);
//	        //   1.4. If nR != point at infinity, then do another iteration of Step 1 (callers responsibility).
//	        if (!R.multiply(n).isInfinity())
//	            return null;
//	        //   1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
//	        BigInteger e = new BigInteger(1, messageHash);
//	        //   1.6. For k from 1 to 2 do the following.   (loop is outside this function via iterating recId)
//	        //   1.6.1. Compute a candidate public key as:
//	        //               Q = mi(r) * (sR - eG)
//	        //
//	        // Where mi(x) is the modular multiplicative inverse. We transform this into the following:
//	        //               Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
//	        // Where -e is the modular additive inverse of e, that is z such that z + e = 0 (mod n). In the above equation
//	        // ** is point multiplication and + is point addition (the EC group operator).
//	        //
//	        // We can find the additive inverse by subtracting e from zero then taking the mod. For example the additive
//	        // inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
//	        BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
//	        BigInteger rInv = r.modInverse(n);
//	        BigInteger srInv = rInv.multiply(s).mod(n);
//	        BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
//	        ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
//	        // result sanity check: point must not be at infinity
//	        if (q.isInfinity())
//	            return null;
//	        return q.getEncoded(/* compressed */ false);
//	    }
	  
//	  /**
//	     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
//	     *
//	     * @param xBN -
//	     * @param yBit -
//	     * @return -
//	     */
//	    private  ECPoint decompressKey(BigInteger xBN, boolean yBit) {
//	        X9IntegerConverter x9 = new X9IntegerConverter();
//	        byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
//	        compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
//	        return CURVE.getCurve().decodePoint(compEnc);
//	    }
//	    
//	    public static BigInteger extractR(byte[] signature) {
//	        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
//	        int lengthR = signature[startR + 1];
//	        return new BigInteger(Arrays.copyOfRange(signature, startR + 2, startR + 2 + lengthR));
//	    }
//
//	    public static BigInteger extractS(byte[] signature) {
//	        int startR = (signature[1] & 0x80) != 0 ? 3 : 2;
//	        int lengthR = signature[startR + 1];
//	        int startS = startR + 2 + lengthR;
//	        int lengthS = signature[startS + 1];
//	        return new BigInteger(Arrays.copyOfRange(signature, startS + 2, startS + 2 + lengthS));
//	    }
//	    
}
