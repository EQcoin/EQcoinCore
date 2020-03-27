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
package com.eqcoin.rpc.service;

import java.nio.ByteBuffer;

import org.apache.avro.AvroRemoteException;

import com.eqcoin.avro.O;
import com.eqcoin.avro.SyncblockNetwork;
import com.eqcoin.blockchain.changelog.Filter.Mode;
import com.eqcoin.blockchain.hive.EQCHiveRoot;
import com.eqcoin.blockchain.hive.EQCHive;
import com.eqcoin.blockchain.passport.EQcoinRootPassport;
import com.eqcoin.blockchain.passport.Passport;
import com.eqcoin.persistence.EQCBlockChainH2;
import com.eqcoin.persistence.EQCBlockChainH2.NODETYPE;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.TailInfo;
import com.eqcoin.service.PossibleNodeService;
import com.eqcoin.service.state.PossibleNodeState;
import com.eqcoin.util.ID;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 29, 2019
 * @email 10509759@qq.com
 */
public class SyncblockNetworkServiceImpl implements SyncblockNetwork {

	@Override
	public O ping(O cookie) {
		O info = null;
		try {
			Cookie cookie1 = new Cookie(cookie);
			PossibleNodeState possibleNode = new PossibleNodeState();
			possibleNode.setIp(cookie1.getIp());
			possibleNode.setNodeType(NODETYPE.FULL);
			possibleNode.setTime(System.currentTimeMillis());
			PossibleNodeService.getInstance().offerNode(possibleNode);
			info = Util.getDefaultInfo().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return info;
	}

	@Override
	public O getMinerList() {
		O minerList = null;
		try {
			minerList = EQCBlockChainH2.getInstance().getMinerList().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return minerList;
	}

	@Override
	public O getFullNodeList() {
		O fullNodeList = null;
		try {
			fullNodeList = EQCBlockChainH2.getInstance().getFullNodeList().getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return fullNodeList;
	}

	@Override
	public O getBlockTail() {
		O io = null;
		TailInfo<O> europa = null;
		EQcoinRootPassport eQcoinSubchainAccount = null;
		try {
			europa = new TailInfo();
			europa.setHeight(Util.DB().getEQCHiveTailHeight());
			eQcoinSubchainAccount = (EQcoinRootPassport) Util.DB().getPassport(ID.ONE, Mode.GLOBAL);
			europa.setCheckPointHeight(eQcoinSubchainAccount.getCheckPointHeight());
			europa.setBlockTailProof(Util.DB().getEQCHive(europa.getHeight(), true).getEqcHeader().getProof());
			io = europa.getProtocol();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return io;
	}

	@Override
	public O getBlock(O height) {
		O block = null;
		EQCHive eqcHive = null;
		try {
			eqcHive = Util.DB().getEQCHive(new ID(height.getO().array()), false);
			if(eqcHive != null) {
				block = Util.bytes2O(eqcHive.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return block;
	}

	@Override
	public O getEQCHeaderHash(O height) {
		O eqcHeaderHash = null;
		byte[] eqcHeaderHash1 = null;
		try {
			eqcHeaderHash1 = Util.DB().getEQCHeaderHash(new ID(height.getO().array()));
			if(eqcHeaderHash1 != null) {
				eqcHeaderHash = new O(ByteBuffer.wrap(eqcHeaderHash1));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return eqcHeaderHash;
	}

	@Override
	public O getEQCHeader(O height) {
		O eqcHeader = null;
		EQCHiveRoot eqcHeader1 = null;
		try {
			eqcHeader1 = Util.DB().getEQCHive(new ID(height.getO().array()), true).getEqcHeader();
			if(eqcHeader1 != null) {
				eqcHeader = Util.bytes2O(eqcHeader1.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.Error(e.getMessage());
		}
		return eqcHeader;
	}
	
}
