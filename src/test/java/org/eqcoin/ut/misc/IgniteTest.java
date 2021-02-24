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
package org.eqcoin.ut.misc;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.WALMode;
import org.eqcoin.passport.AssetPassport;
import org.eqcoin.passport.Passport;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Xun Wang
 * @date Aug 26, 2020
 * @email 10509759@qq.com
 */
class IgniteTest {
	private static Ignite ignite;
	private static IgniteCache<Long, Passport> cache;

	private static IgniteCache<Long, byte[]> cacheBinary;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
		final DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
		dataStorageConfiguration.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
		dataStorageConfiguration.getDefaultDataRegionConfiguration().setMaxSize(251 * 1024 * 1024);
		dataStorageConfiguration.setWalMode(WALMode.NONE);
		igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
		igniteConfiguration.setConsistentId("abc");
		ignite = Ignition.start(igniteConfiguration);
		if (ignite.cluster().state() != ClusterState.ACTIVE) {
			ignite.cluster().state(ClusterState.ACTIVE);
		}
		final CacheConfiguration cacheCfg = new CacheConfiguration();
		cacheCfg.setName("passportCache");
		cacheCfg.setCacheMode(CacheMode.PARTITIONED);
		cache = ignite.getOrCreateCache(cacheCfg);
		final CacheConfiguration cacheCfgBinary = new CacheConfiguration();
		cacheCfgBinary.setName("passportCacheBinary");
		cacheCfgBinary.setCacheMode(CacheMode.PARTITIONED);
		//		cacheCfgBinary.setWriteBehindEnabled(false);
		cacheBinary = ignite.getOrCreateCache(cacheCfgBinary);
		ignite.cluster().disableWal("passportCacheBinary");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		if (ignite.cluster().state() == ClusterState.ACTIVE) {
			ignite.cluster().state(ClusterState.INACTIVE);
		}
		ignite.close();
	}

	@Test
	void crudPassport() {
		Passport passport;
		try {
			passport = new AssetPassport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			//			final Map<Long, Passport> map = new HashMap<>();
			//			//			cache.putAll(map);
			//			for (long i = 0; i < 100000l; ++i) {
			//				map.put(i, passport);
			//				//				cache.put(i, passport);
			//				//				Log.info("" + i);
			//			}
			Log.info("" + time);
			cache.put(2l, passport);

			//			cache.putAllAsync(map);
			Log.info("" + (System.currentTimeMillis() - time));
			passport = null;
			passport = cache.get(2l);
			final IgniteCache<Long, BinaryObject> binaryCache = ignite.cache("passportCache").withKeepBinary();
			final BinaryObject aBinaryObject = binaryCache.get(2l);
			aBinaryObject.clone();
			//					passport = cache.get(2l);
			//			assertNotNull(passport);
			// Insert 1000000's passport used time [2020-08-26
			// 15:26:22:022][org.eqcoin.ut.misc.IgniteTest.crudPassport
			// 97]18609 and the disk space is about 1gb
			// 100000's 4240 putall 3558 putAllAsync 2775
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void readPassport() {
		Passport passport;
		try {
			final IgniteCache<Long, BinaryObject> binaryCache = ignite.cache("passportCache").withKeepBinary();
			final BinaryObject aBinaryObject = binaryCache.get(2l);
			for (long i = 0; i < 100000l; ++i) {
				if(i%1000==0) {
					passport = cache.get(i);
					Log.info("" + i + passport.toString());
				}
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void savePassport() {
		Passport passport;
		try {
			passport = new AssetPassport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final BinaryObject binaryObject = ignite.binary().toBinary(passport);
			binaryObject.
			final long time = System.currentTimeMillis();
			// final Map<Long, Passport> map = new HashMap<>();
			// // cache.putAll(map);
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 100000l; ++i) {
				cache.put(i, passport);
				//				Log.info("" + i);
			}
			// cache.putAllAsync(map);
			Log.info("" + (System.currentTimeMillis() - time));
			//			passport = null;
			//			passport = cache.get(2l);
			//			final IgniteCache<Long, BinaryObject> binaryCache = ignite.cache("passportCache").withKeepBinary();
			//			final BinaryObject aBinaryObject = binaryCache.get(2l);
			//			aBinaryObject.clone();
			// passport = cache.get(2l);
			// assertNotNull(passport);
			// Insert 100000's passport used time:
			// No.1
			// [2020-08-28 16:24:34:034][org.eqcoin.ut.misc.IgniteTest.savePassport 163]9630
			// File size: 708 MB (742,493,403 字节)
			// No.2 覆盖了一次
			// [2020-08-28 16:31:34:034][org.eqcoin.ut.misc.IgniteTest.savePassport 163]5295
			// File size: 844 MB (885,153,419 字节)
			// No.3 又覆盖了一次
			// [2020-08-28 16:34:38:038][org.eqcoin.ut.misc.IgniteTest.savePassport 163]5366
			// File size: 972 MB (1,019,367,145 字节)
			// No.4 开启了Externalizable重新写入一次
			// [2020-08-28 16:38:35:035][org.eqcoin.ut.misc.IgniteTest.savePassport 163]8850
			// File size: 644 MB (675,383,686 字节)
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void savePassportBinary() {
		try {
			Passport passport;
			passport = new AssetPassport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final byte[] passport1 = new byte[100 * 1024 * 1024];// passport.getBytes();
			final long time = System.currentTimeMillis();
			// final Map<Long, Passport> map = new HashMap<>();
			// // cache.putAll(map);
			Log.info("" + time);
			for (long i = 0; i < 40l; ++i) {
				cacheBinary.put(i, passport1);
				// Log.info("" + i);
			}
			// cache.putAllAsync(map);
			Log.info("" + (System.currentTimeMillis() - time));
			// passport = null;
			// passport = cache.get(2l);
			// final IgniteCache<Long, BinaryObject> binaryCache =
			// ignite.cache("passportCache").withKeepBinary();
			// final BinaryObject aBinaryObject = binaryCache.get(2l);
			// aBinaryObject.clone();
			// passport = cache.get(2l);
			// assertNotNull(passport);
			// Insert 100000's passport used time:
			// No.1
			// [2020-08-28 16:24:34:034][org.eqcoin.ut.misc.IgniteTest.savePassport 163]9630
			// File size: 708 MB (742,493,403 字节)
			// No.2 覆盖了一次
			// [2020-08-28 16:31:34:034][org.eqcoin.ut.misc.IgniteTest.savePassport 163]5295
			// File size: 844 MB (885,153,419 字节)
			// No.3 又覆盖了一次
			// [2020-08-28 16:34:38:038][org.eqcoin.ut.misc.IgniteTest.savePassport 163]5366
			// File size: 972 MB (1,019,367,145 字节)
			// No.4 开启了Externalizable重新写入一次
			// [2020-08-28 16:38:35:035][org.eqcoin.ut.misc.IgniteTest.savePassport 163]8850
			// File size: 644 MB (675,383,686 字节)
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	final void test() {
		fail("Not yet implemented"); // TODO
	}

}
