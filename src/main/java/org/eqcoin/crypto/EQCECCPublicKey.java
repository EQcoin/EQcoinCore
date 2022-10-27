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
package org.eqcoin.crypto;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;

import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.eqcoin.keystore.Keystore.ECCTYPE;
import org.eqcoin.util.Log;

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
	final long serialVersionUID = 1303765568188200263L;

	private ECDSACurve ecdsaCurve;

	/**
	 * ECC publickey relevant variable which depend on different ECC publickey object
	 */
	private ECPoint ecPoint;
	private ECPublicKey pk;

	public EQCECCPublicKey(final ECCTYPE type) {
		if(type == ECCTYPE.P256) {
			ecdsaCurve = SECP256R1Curve.getInstance();
		}
		else if(type == ECCTYPE.P521) {
			ecdsaCurve = SECP521R1Curve.getInstance();
		}
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

	/**
	 * @return
	 */
	public byte[] getCompressedPublicKeyEncoded() {
		return ecPoint.getEncoded(true);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.security.Key#getFormat()
	 */
	@Override
	public String getFormat() {
		return "X.509";
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
		} catch (final NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		final ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(
				ECPointUtil.decodePoint(ecdsaCurve.getEcParams().getCurve(), ecPoint.getEncoded(true)), ecdsaCurve.getEcParams());
		try {
			pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
		} catch (final InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
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

}
