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
 */
package org.eqcoin.crypto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequenceGenerator;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public class ECDSASignature {
	/** The two components of the signature. */
	private BigInteger r, s;

	/**
	 * Constructs a signature with the given components. Does NOT automatically
	 * canonicalise the signature.
	 */
	public ECDSASignature(BigInteger r, BigInteger s) {
		this.r = r;
		this.s = s;
	}

	/**
	 * ASN.1 DER is an international standard for serializing data structures which is
	 * widely used in cryptography. It's somewhat like protocol buffers but less
	 * convenient. This method returns a standard DER encoding of the signature, as
	 * recognized by OpenSSL and other libraries.
	 * @throws IOException 
	 */
	public byte[] encodeToDER() throws IOException {
		ByteArrayOutputStream os = null;
		DERSequenceGenerator derSequenceGenerator = null;
		/**
		 * Compressed publickey and ASN.1 DER signature's length specification ECC curve
		 * compressed publickey length(bytes) signature length(bytes) P256 33 70、71、72
		 * P521 67 137、138、139
		 */
		os = new ByteArrayOutputStream(139);
		derSequenceGenerator = new DERSequenceGenerator(os);
		derSequenceGenerator.addObject(new ASN1Integer(r));
		derSequenceGenerator.addObject(new ASN1Integer(s));
		derSequenceGenerator.close();
		return os.toByteArray();
	}

	public static ECDSASignature decodeFromDER(byte[] derSignature) throws Exception {
		ASN1InputStream asnInputStream = null;
		try {
			// BouncyCastle by default is strict about parsing ASN.1 integers. We relax this
			// check, because some
			// Bitcoin signatures would not parse.
//			Properties.setThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer", true);

			asnInputStream = new ASN1InputStream(new ByteArrayInputStream(derSignature));
			ASN1Primitive asn1 = asnInputStream.readObject();
			ASN1Integer r = null, s = null;
			if (asn1 instanceof ASN1Sequence) {
				ASN1Sequence asn1Sequence = (ASN1Sequence) asn1;
				ASN1Encodable[] asn1Encodables = asn1Sequence.toArray();
				r = (ASN1Integer) asn1Encodables[0].toASN1Primitive();
				s = (ASN1Integer) asn1Encodables[1].toASN1Primitive();
			}

			// OpenSSL deviates from the DER spec by interpreting these values as unsigned,
			// though they should not be
			// Thus, we always use the positive versions. See:
			// http://r6.ca/blog/20111119T211504Z.html
			return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
		} catch (IOException e) {
			throw e;
		} finally {
//			Properties.removeThreadOverride("org.bouncycastle.asn1.allow_unsafe_integer");
			asnInputStream.close();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ECDSASignature other = (ECDSASignature) o;
		return r.equals(other.r) && s.equals(other.s);
	}

	@Override
	public int hashCode() {
		return Objects.hash(r, s);
	}

	/**
	 * @return the r
	 */
	public BigInteger getR() {
		return r;
	}

	/**
	 * @param r the r to set
	 */
	public void setR(BigInteger r) {
		this.r = r;
	}

	/**
	 * @return the s
	 */
	public BigInteger getS() {
		return s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(BigInteger s) {
		this.s = s;
	}
	
}
