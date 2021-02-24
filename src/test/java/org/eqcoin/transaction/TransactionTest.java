/**
 * EQcoin core - EQcoin Federation's EQcoin core library
 *
 * http://www.eqcoin.org
 *
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
package org.eqcoin.transaction;

import org.eqcoin.transaction.Transaction.TRANSACTION_PRIORITY;
import org.eqcoin.ut.misc.TransTest;
import org.eqcoin.util.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date Jul 26, 2019
 * @email 10509759@qq.com
 */
public class TransactionTest {
	
	private static boolean isRpc;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Log.info("setUpBeforeClass");
		isRpc = false;
	}
	
	@Test
	void regresstionTest() {
//		TransTest.Zion(0, 2300, TRANSACTION_PRIORITY.ASAP, 2);
//		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, 3, 4, 5);
		TransTest.Tranfer(2, 1, TRANSACTION_PRIORITY.ASAP, false, 5);
//		TransTest.TranferChangeLock(2, 1, 13, TRANSACTION_PRIORITY.ASAP, 3);
//		TransTest.TranferChangeLock(3, 1, 12, TRANSACTION_PRIORITY.ASAP, 4);
	}
	
	@Test
	void regresstionChangeLockTest() {
		TransTest.TranferChangeLock(2, 1, 4, TRANSACTION_PRIORITY.ASAP, isRpc, 1);
	}
	
	@Test
	void regresstionZeroZionTest() {
		TransTest.Zion(0, 2000, TRANSACTION_PRIORITY.ASAP, isRpc, 2);
	}
	
	@Test
	void regresstionZionTest() {
		TransTest.Zion(2, 51, TRANSACTION_PRIORITY.ASAP, isRpc, 3);
	}
	
	@Test
	void regresstionTransferTest() {
		TransTest.Tranfer(0, 1, TRANSACTION_PRIORITY.ASAP, isRpc, 1);
	}
	
}
