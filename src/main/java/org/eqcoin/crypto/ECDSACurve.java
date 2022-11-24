/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Wandering Earth Corporation All Rights Reserved...
 * The copyright of all works released by Wandering Earth Corporation or jointly
 * released by Wandering Earth Corporation with cooperative partners are owned
 * by Wandering Earth Corporation and entitled to protection available from
 * copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For the use of any and all intellectual property rights of Wandering Earth Corporation 
 * without prior written permission, Wandering Earth Corporation reserves all 
 * rights to take any legal action and pursue any rights or remedies under applicable law.
 */
package org.eqcoin.crypto;

import java.math.BigInteger;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECPoint;

/**
 * @author Xun Wang
 * @date Apr 13, 2020
 * @email 10509759@qq.com
 */
public abstract class ECDSACurve {
	/**
	 * The parameters of ECDSA curve that EQcoin uses.
	 */
	protected ECDomainParameters CURVE;
	protected X9ECParameters CURVE_PARAMS;
	protected BigInteger HALF_CURVE_ORDER;

	// For java standard EC
	protected ECNamedCurveParameterSpec spec;
	protected ECNamedCurveSpec ecParams;
	
	public ECDSACurve() {}
	
	/**
	 * @return the cURVE
	 */
	public ECDomainParameters getCURVE() {
		return CURVE;
	}
	/**
	 * @param cURVE the cURVE to set
	 */
	public void setCURVE(ECDomainParameters cURVE) {
		CURVE = cURVE;
	}
	/**
	 * @return the cURVE_PARAMS
	 */
	public X9ECParameters getCURVE_PARAMS() {
		return CURVE_PARAMS;
	}
	/**
	 * @param cURVE_PARAMS the cURVE_PARAMS to set
	 */
	public void setCURVE_PARAMS(X9ECParameters cURVE_PARAMS) {
		CURVE_PARAMS = cURVE_PARAMS;
	}
	/**
	 * @return the hALF_CURVE_ORDER
	 */
	public BigInteger getHALF_CURVE_ORDER() {
		return HALF_CURVE_ORDER;
	}
	/**
	 * @param hALF_CURVE_ORDER the hALF_CURVE_ORDER to set
	 */
	public void setHALF_CURVE_ORDER(BigInteger hALF_CURVE_ORDER) {
		HALF_CURVE_ORDER = hALF_CURVE_ORDER;
	}
	/**
	 * @return the spec
	 */
	public ECNamedCurveParameterSpec getSpec() {
		return spec;
	}
	/**
	 * @param spec the spec to set
	 */
	public void setSpec(ECNamedCurveParameterSpec spec) {
		this.spec = spec;
	}
	/**
	 * @return the ecParams
	 */
	public ECNamedCurveSpec getEcParams() {
		return ecParams;
	}
	/**
	 * @param ecParams the ecParams to set
	 */
	public void setEcParams(ECNamedCurveSpec ecParams) {
		this.ecParams = ecParams;
	}
	
}
