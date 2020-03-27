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
package com.eqchains.misc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.eqcoin.blockchain.passport.Lock;
import com.eqcoin.keystore.Keystore;
import com.eqcoin.util.Base58;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;
import com.eqcoin.util.Util.LockTool;
import com.eqcoin.util.Util.LockTool.LockType;

/**
 * @author Xun Wang
 * @date May 13, 2019
 * @email 10509759@qq.com
 */
public class LockTest {
	 @Test
	    void verifyAddressCRC32C() {
		   String readableAddress = Keystore.getInstance().getUserAccounts().get(0).getReadableLock();
		   Log.info(readableAddress);
	        assertTrue(LockTool.verifyAddressCRC32C(readableAddress));
	    }
	   
	   @Test
	   void base58AndCrc32c() {
		   byte[] bytes = Util.getSecureRandomBytes();
		   String address = Base58.encode(bytes);
		   Log.info(address);
		   try {
			byte[] bytes1 = Base58.decode(address);
			assertArrayEquals(bytes, bytes1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
	   
	   @Test
	   void generateAddress() {
		   byte[] publickey =  Util.AESDecrypt(Keystore.getInstance().getUserAccounts().get(1).getPublicKey(), "abc");
		   String address = LockTool.generateAddress(publickey, LockType.T1);
		   Log.info(address);
		   assertTrue(LockTool.verifyLockAndPublickey(Keystore.getInstance().getUserAccounts().get(1).getReadableLock(), publickey));
		   assertTrue(LockTool.verifyLockAndPublickey(address, publickey));
	   }
	   
	   @Test
	   void verifyAI2Address() {
		   Lock lock = new Lock();
		   lock.setReadableLock(Keystore.getInstance().getUserAccounts().get(0).getReadableLock());
		   try {
			assertEquals(LockTool.AIToAddress(lock.getAddressAI()), lock.getReadableLock());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   }
}
