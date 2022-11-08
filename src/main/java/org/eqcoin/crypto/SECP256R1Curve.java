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

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.eqcoin.keystore.Keystore;

/**
 * @author Xun Wang
 * @date Apr 14, 2020
 * @email 10509759@qq.com
 */
public class SECP256R1Curve extends ECDSACurve {
	
	private static SECP256R1Curve secp256r1Curve;
	
	static {
		secp256r1Curve = new SECP256R1Curve();
	}
	
	private SECP256R1Curve() {
		CURVE_PARAMS = SECNamedCurves.getByName(Keystore.SECP256R1);
		HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
		CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH());
		spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP256R1);
		ecParams = new ECNamedCurveSpec(Keystore.SECP256R1, spec.getCurve(), spec.getG(), spec.getN());
	}
	
	public static SECP256R1Curve getInstance() {
		return secp256r1Curve;
	}
	
}
