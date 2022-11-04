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
