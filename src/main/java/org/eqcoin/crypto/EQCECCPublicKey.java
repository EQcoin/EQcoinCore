/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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
package org.eqcoin.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Objects;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLTaggedObject;
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
import org.bouncycastle.math.ec.ECCurve.Fp;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve;
import org.bouncycastle.util.Properties;
import org.eqcoin.keystore.Keystore;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.lock.Lock;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * EQCPublicKey is an EQC tool you can use it as PublicKey to verify the
 * signature or get the PublicKey's compressed encode
 * 
 * @author Xun Wang
 * @date Sep 26, 2018
 * @email 10509759@qq.com
 */
/**
 * @author Xun Wang
 * @date Apr 14, 2020
 * @email 10509759@qq.com
 */
public class EQCECCPublicKey implements PublicKey {
	private static final long serialVersionUID = 1303765568188200263L;

	private ECDSACurve ecdsaCurve;
	
	/**
	 * ECC publickey relevant variable which depend on different ECC publickey object
	 */
	private ECPoint ecPoint;
	private ECPublicKey pk;

	public EQCECCPublicKey(ECCTYPE type) {
		if(type == ECCTYPE.P256) {
			ecdsaCurve = SECP256R1Curve.getInstance();
		}
		else if(type == ECCTYPE.P521) {
			ecdsaCurve = SECP521R1Curve.getInstance();
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
		ecPoint = ecdsaCurve.getCURVE().getCurve().createPoint(xCoord, yCoord);
	}

	/**
	 * Construct an EQPublicKey with a compressed public key then you can use it to
	 * verify the signature
	 * 
	 * @param bytes Compressed public key
	 */
	public void setECPoint(final byte[] compressedPublicKey) {
		ecPoint = ecdsaCurve.getCURVE().getCurve().decodePoint(compressedPublicKey);
		ECPointUtil.decodePoint(ecdsaCurve.getEcParams().getCurve(), ecPoint.getEncoded(true));
		KeyFactory kf = null;
		try {
			kf = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
				ECPointUtil.decodePoint(ecdsaCurve.getEcParams().getCurve(), ecPoint.getEncoded(true)), ecdsaCurve.getEcParams());
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

}
