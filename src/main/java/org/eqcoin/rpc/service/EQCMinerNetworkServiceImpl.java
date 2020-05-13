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
package org.eqcoin.rpc.service;

import org.apache.avro.AvroRemoteException;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.hive.EQCHiveH2;
import org.eqcoin.rpc.Code;
import org.eqcoin.rpc.Info;
import org.eqcoin.rpc.NewEQCHive;
import org.eqcoin.rpc.SP;
import org.eqcoin.rpc.TransactionIndexList;
import org.eqcoin.rpc.TransactionList;
import org.eqcoin.serialization.EQCType;
import org.eqcoin.service.PendingNewEQCHiveService;
import org.eqcoin.service.PossibleSPService;
import org.eqcoin.service.state.NewEQCHiveState;
import org.eqcoin.service.state.PossibleSPState;
import org.eqcoin.service.state.EQCServiceState.State;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class EQCMinerNetworkServiceImpl implements EQCMinerNetwork {

	/* (non-Javadoc)
	 * @see com.eqchains.avro.MinerNetwork#ping(com.eqchains.avro.IO)
	 */
	@Override
	public O registerSP(O sp) {
		Info info = null;
		O o = null;
		SP sp1 = null;
		try {
			sp1 = new SP(sp1);
			Log.info("Received ping from: " + sp1);
			if (sp1.isSanity()) {
				PossibleSPState possibleNodeState = new PossibleSPState();
				possibleNodeState.setSp(sp1);
				possibleNodeState.setTime(System.currentTimeMillis());
				PossibleSPService.getInstance().offerState(possibleNodeState);
				info = Util.getDefaultInfo();
			} else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null);
			}
			o = info.getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		Log.info("Give ping response to " + sp1 + " info: " + info);
		return o;
	}

	@Override
	public O getSPList(O m) {
		O minerList = null;
		try {
			minerList = Util.DB().getSPList(EQCType.parseID(m.o.array())).getProtocol(O.class);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
	}

	@Override
	public O broadcastNewEQCHive(O e) {
		O info = null;
		NewEQCHive newEQCHive = null;
		NewEQCHiveState newHiveState = null;
		try {
			newEQCHive = new NewEQCHive(e);
			Log.info("MinerNetworkServiceImpl received new EQCHive");
			if(newEQCHive.getSp().isSanity()) {
				info = Util.getDefaultInfo().getProtocol(O.class);
				newHiveState = new NewEQCHiveState();
				newHiveState.setState(State.PENDINGNEWEQCHIVE);
				newHiveState.setNewEQCHive(newEQCHive);
				PendingNewEQCHiveService.getInstance().offerState(newHiveState);
				Log.info("Call PendingNewBlockService handle the new block");
			}
			else {
				info = Util.getInfo(Code.WRONGPROTOCOL, null).getProtocol(O.class);
			}
		} catch (Exception exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
			Log.Error(exception.getMessage());
		}
		return info;
	}

}
