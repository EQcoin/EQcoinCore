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
package com.eqcoin.services;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.avro.ipc.netty.NettyTransceiver;
import org.apache.avro.ipc.specific.SpecificRequestor;
import org.junit.jupiter.api.Test;

import com.eqcoin.avro.O;
import com.eqcoin.avro.SyncblockNetwork;
import com.eqcoin.rpc.Code;
import com.eqcoin.rpc.Cookie;
import com.eqcoin.rpc.Info;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Jun 13, 2019
 * @email 10509759@qq.com
 */
public class SyncblockNetworkServiceTest {

	@Test
	void ping() {
		long time = System.currentTimeMillis();
    	NettyTransceiver client = null;
    	try {
//    		Util.init();
    		Cookie cookie = new Cookie();
    		cookie.setIp(Util.SINGULARITY_IP);
    		cookie.setVersion(Util.PROTOCOL_VERSION);
    		Info info = new Info();
    		info.setCode(Code.OK);
    		info.setMessage("");
    		System.out.println("Begin link remote: " + time);
    		client = new NettyTransceiver(new InetSocketAddress(InetAddress.getByName("14.221.177.198"), 7997), 3000l);
    		System.out.println("End link remote: " + (System.currentTimeMillis() - time));
    		 // client code - attach to the server and send a message
    		SyncblockNetwork proxy = SpecificRequestor.getClient(SyncblockNetwork.class, client);
            System.out.println("Client built, got proxy");
//            Cookie cookie = new Cookie();
//            cookie.setIp(Util.getCookie().getIp().toString());
//            cookie.setVersion("0.01");
            System.out.println("Calling proxy.send with message:  " + cookie);
            System.out.println("Result: " + proxy.ping(cookie.getProtocol(O.class)));

//            // cleanup
//            client.close();
    	}
    	catch (Exception e) {
			// TODO: handle exception
    		System.out.println("Exception occur during link remote: " + (System.currentTimeMillis() - time));
    		System.out.println(e.getMessage());
		}
    	finally {
			if(client != null) {
				client.close();
			}
		}
	}
}
