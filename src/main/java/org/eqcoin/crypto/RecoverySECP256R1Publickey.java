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

/**
 * @author Xun Wang
 * @date Apr 14, 2020
 * @email 10509759@qq.com
 */
public class RecoverySECP256R1Publickey extends RecoveryECCPublickey {

	static {
		recoverySECP256R1Publickey = new RecoverySECP256R1Publickey();
	}

	public static RecoverySECP256R1Publickey getInstance() {
		return recoverySECP256R1Publickey;
	}

	private static RecoverySECP256R1Publickey recoverySECP256R1Publickey;

	private RecoverySECP256R1Publickey() {
		ecdsaCurve = SECP256R1Curve.getInstance();
	}

}
