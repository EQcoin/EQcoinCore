/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * The copyright of all works released by Xun Wang or jointly released by
 * Xun Wang with cooperative partners are owned by Xun Wang and entitled
 * to protection available from copyright law by country as well as international
 * conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Xun Wang reserves any and all current and future rights, titles and interests
 * in any and all intellectual property rights of Xun Wang including but not limited
 * to discoveries, ideas, marks, concepts, methods, formulas, processes, codes,
 * software, inventions, compositions, techniques, information and data, whether
 * or not protectable in trademark, copyrightable or patentable, and any trademarks,
 * copyrights or patents based thereon. For the use of any and all intellectual
 * property rights of Xun Wang without prior written permission, Xun Wang reserves
 * all rights to take any legal action and pursue any rights or remedies under
 * applicable law.
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
public class SECP521R1Curve extends ECDSACurve {
	
	private static SECP521R1Curve secp521r1Curve;
	
	static {
		secp521r1Curve = new SECP521R1Curve();
	}
	
	private SECP521R1Curve() {
		CURVE_PARAMS = SECNamedCurves.getByName(Keystore.SECP521R1);
		HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
		CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(),
				CURVE_PARAMS.getH());
		spec = ECNamedCurveTable.getParameterSpec(Keystore.SECP521R1);
		ecParams = new ECNamedCurveSpec(Keystore.SECP521R1, spec.getCurve(), spec.getG(), spec.getN());
	}
	
	public static SECP521R1Curve getInstance() {
		return secp521r1Curve;
	}
	
}
