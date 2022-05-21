/**
 * EQchains core - EQchains Federation's EQchains core library
 * @copyright 2018-present EQchains Federation All rights reserved...
 * Copyright of all works released by EQchains Federation or jointly released by
 * EQchains Federation with cooperative partners are owned by EQchains Federation
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQchains Federation reserves all rights to
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
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTON) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eqcoin.rpc.service.avro;

import org.eqcoin.avro.EQCHiveSyncNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHiveRoot;
import org.eqcoin.rpc.gateway.Gateway;
import org.eqcoin.rpc.object.TailInfo;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCHiveSyncNetworkServiceImpl extends EQCRPCServiceImpl implements EQCHiveSyncNetwork {

	@Override
	public O getEQCHiveTail() {
		O io = null;
		TailInfo tailInfo = null;
//		EQcoinRootPassport eQcoinSubchainPassport = null;
//		try {
//			tailInfo = new TailInfo();
//			tailInfo.setHeight(Util.GS().getEQCHiveTailHeight());
//			eQcoinSubchainPassport = (EQcoinRootPassport) Util.GS().getPassport(ID.ZERO);
//			tailInfo.setCheckPointHeight(eQcoinSubchainPassport.getCheckPointHeight());
//			tailInfo.setTailProof(Util.GS().getEQCHiveRootProof(tailInfo.getHeight()));
//			tailInfo.setSp(Util.LOCAL_SP);
//			io = tailInfo.getProtocol(O.class);
//		} catch (Exception e) {
//			Log.Error(e.getMessage());
//		}
		return io;
	}

	@Override
	public O getEQCHive(O h) {
		O hive = null;
		byte[] eqcHive = null;
		try {
			eqcHive = Util.GS().getEQCHive(new ID(h.getO().array()));
			if(eqcHive != null) {
				hive = Gateway.getProtocol(O.class, eqcHive);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return hive;
	}

	/**
	 * @param S
	 */
	@Override
	public O getLockInfo(O S) {
		return null;
	}

	@Override
	public O getEQCHiveRootProof(O h) {
		O rootProof = null;
		byte[] eqcRootProof = null;
		try {
			eqcRootProof = Util.GS().getEQCHiveRootProof(new ID(h.getO().array()));
			if(eqcRootProof != null) {
				rootProof = Gateway.getProtocol(O.class, eqcRootProof);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return rootProof;
	}

	@Override
	public O getEQCHiveRoot(O H) {
		O eqcHiveRoot = null;
		EQCHiveRoot eqcHiveRoot2 = null;
		try {
			eqcHiveRoot2 = Util.GS().getEQCHiveRoot(new ID(H.getO().array()));
			if(eqcHiveRoot2 != null) {
				eqcHiveRoot = eqcHiveRoot2.getProtocol(O.class);
			}
		} catch (Exception e) {
			Log.Error(e.getMessage());
		}
		return eqcHiveRoot;
	}
	
}
