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
 */
package org.eqcoin.persistence.statedb;

/**
 * @author Xun Wang
 * @date 2022年1月16日
 * @email 10509759@qq.com
 */
public interface StateDB {
	
	public <O> O getPassportCacheName() throws Exception;
	
	public <O> O getEQCHiveCacheName() throws Exception;
	
	public <O> O getTransactionCacheName() throws Exception;
	
	public <O> void createCache(O cacheName) throws Exception;
	
	public <O> void delCache(O cacheName) throws Exception;
	
	public <O, K, V> void put(O cacheName, K key, V value) throws Exception;
	
	public <O, K, V> V get(O cacheName, K key) throws Exception;
	
	public <O, K> void del(O cacheName, K key) throws Exception;
	
	public void commit() throws Exception;
	
	public void rollback() throws Exception;
	
}
