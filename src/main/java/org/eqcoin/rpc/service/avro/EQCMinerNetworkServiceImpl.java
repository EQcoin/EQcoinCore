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
package org.eqcoin.rpc.service.avro;

import org.apache.avro.AvroRemoteException;
import org.eqcoin.avro.EQCMinerNetwork;
import org.eqcoin.avro.O;
import org.eqcoin.hive.EQCHive;
import org.eqcoin.persistence.globalstate.h2.GlobalStateH2;
import org.eqcoin.rpc.object.Code;
import org.eqcoin.rpc.object.Info;
import org.eqcoin.rpc.object.NewEQCHive;
import org.eqcoin.rpc.object.SP;
import org.eqcoin.rpc.object.TransactionIndexList;
import org.eqcoin.rpc.object.TransactionList;
import org.eqcoin.serialization.EQCCastle;
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
public class EQCMinerNetworkServiceImpl extends EQCRPCServiceImpl implements EQCMinerNetwork {

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
