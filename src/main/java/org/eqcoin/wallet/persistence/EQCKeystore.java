/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 * 
 * @Copyright 2018-present Xun Wang All Rights Reserved...
 * Copyright of all works released by Xun Wang or jointly released by Xun Wang
 * with cooperative partners are owned by Xun Wang and entitled to protection 
 * available from copyright law by country as well as international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, Xun Wang reserves all rights to take 
 * any legal action and pursue any right or remedy available under applicable
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
package org.eqcoin.wallet.persistence;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.eqcoin.keystore.Keystore.ECCTYPE;

/**
 * @author Xun Wang
 * @date May 5, 2020
 * @email 10509759@qq.com
 */
public interface EQCKeystore {
	
//	public Connection getConnection() throws Exception;
//	
//	// Release the relevant database resource
//	public boolean close() throws Exception;
//
//	public boolean saveLock(LockMate lockMate) throws Exception;
//
//	public LockMate getLock(ID id) throws Exception;
//	
////	public LockMate getLock(Lock eqcLock, Mode mode) throws Exception;
//	
//	public Vector<LockMate> getLockList() throws Exception;
//	
//	public boolean isLockExists(ID id) throws Exception;
//	
//	public ID isLockExists(Lock lock) throws Exception;
//
//	public boolean deleteLock(ID id) throws Exception;
//
//	public boolean savePassport(Passport passport) throws Exception;
//	
//	public Passport getPassport(ID id) throws Exception;
//
////	public Passport getPassportFromLockId(ID lockId, Mode mode) throws Exception;
//	
//	public boolean isPassportExists(ID id) throws Exception;
//	
//	public boolean deletePassport(ID id) throws Exception;
//	
//	boolean isTransactionExistsInPool(Transaction transaction) throws Exception;
//
//	public boolean saveTransactionInPool(Transaction transaction) throws Exception;
//
//	public boolean deleteTransactionInPool(Transaction transaction) throws Exception;
//	
//	public Vector<Transaction> getPendingTransactionListInPool(ID id) throws Exception;
	
	public <T>  boolean generateKeyPair(T alais, ECCTYPE eccType) throws Exception;
	
	public <T> PrivateKey getPrivateKey(T alais) throws Exception;
	
	public <T> PublicKey getPublicKey(T alais) throws Exception;
	
	public <T> T generateAlais() throws Exception;

}
