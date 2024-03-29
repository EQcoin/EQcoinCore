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
