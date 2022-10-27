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
package org.eqcoin.ut.misc;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.eqcoin.passport.passport.AssetPassport;
import org.eqcoin.passport.passport.Passport;
import org.eqcoin.util.ID;
import org.eqcoin.util.Log;
import org.eqcoin.util.Util;
import org.eqcoin.util.Value;
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
	private static String passportCache = "passportCache";
	private static String passportCacheBinary = "passportCacheBinary";
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
		// dataStorageConfiguration.setWalMode(WALMode.NONE);
		igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
		igniteConfiguration.setConsistentId("abc");
		ignite = Ignition.start(igniteConfiguration);
		if (ignite.cluster().state() != ClusterState.ACTIVE) {
			ignite.cluster().state(ClusterState.ACTIVE);
		}
		final CacheConfiguration cacheCfg = new CacheConfiguration();
		cacheCfg.setName(passportCache);
		cacheCfg.setCacheMode(CacheMode.PARTITIONED);
		cache = ignite.getOrCreateCache(cacheCfg);
		final CacheConfiguration cacheCfgBinary = new CacheConfiguration();
		cacheCfgBinary.setName(passportCacheBinary);
		cacheCfgBinary.setCacheMode(CacheMode.PARTITIONED);
		// cacheCfgBinary.setWriteBehindEnabled(false);
		cacheBinary = ignite.getOrCreateCache(cacheCfgBinary);
		// ignite.cluster().disableWal(passportCache);
		// ignite.cluster().disableWal(passportCacheBinary);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		//		if (ignite.cluster().state() == ClusterState.ACTIVE) {
		//			ignite.cluster().state(ClusterState.INACTIVE);
		//		}
		//		ignite.close();
	}

	@Test
	void crudPassport() {
		Passport passport;
		try {
			passport = new Passport();
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
	void passportDeserializationCost() {
		try {
			Passport passport;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final byte[] passport1 = passport.getBytes();
			Passport passport2 = null;
			final long time = System.currentTimeMillis();
			Log.info("" + time);
			for (long i = 1; i < 1000000l; ++i) {
				passport2 = new Passport(passport1);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport2.toString());
			// Test result
			// [2021-02-25 10:36:33:033] 1096
			// [2021-02-25 10:37:23:023] 1018
			// [2021-02-25 10:38:15:015] 1040
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void passportSerializationCost() {
		try {
			Passport passport;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			byte[] passport1 = passport.getBytes();
			final Passport passport2 = null;
			final long time = System.currentTimeMillis();
			Log.info("" + time);
			for (long i = 1; i < 1000000l; ++i) {
				passport1 = passport.getBytes();
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 10:40:37:037] 1045
			// [2021-02-25 10:41:29:029] 988
			// [2021-02-25 10:42:19:019] 1009
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void readMultiplePassportBytes() {
		try {
			byte[] passport = null;
			final long time = System.currentTimeMillis();
			Log.info("" + time);
			for (long i = 0; i < 1000000l; ++i) {
				passport = cacheBinary.get(i);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 12:39:01:001] 12732
			// [2021-02-25 12:40:02:002] 12573
			// [2021-02-25 12:41:09:009] 13021
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void readMultiplePassportObject() {
		try {
			Passport passport = null;
			final long time = System.currentTimeMillis();
			Log.info("" + time);
			for (long i = 0; i < 1000000l; ++i) {
				passport = cache.get(i);
				//				Log.info(passport.toString());
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 16:38:00:000] 6046
			// [2021-02-25 16:38:41:041] 5603
			// [2021-02-25 16:39:23:023] 5607
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
	void readSinglePassportBinaryObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			cache.put(0l, passport);
			final IgniteCache<Long, BinaryObject> bIgniteCache = cache.withKeepBinary();
			BinaryObject binaryObject = null;
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				binaryObject = bIgniteCache.get(0l);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(binaryObject.toString());
			// Test result
			// [2021-02-25 12:55:22:022] 2792
			// [2021-02-25 12:56:05:005] 2895
			// [2021-02-25 12:56:48:048] 2893
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void readSinglePassportBytes() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			byte[] bytes = null;
			for (long i = 0; i < 1000000l; ++i) {
				bytes = cacheBinary.get(0l);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info("" + bytes.length);
			// Test result
			// [2021-02-25 16:08:29:029] 2836
			// [2021-02-25 16:09:11:011] 2749
			// [2021-02-25 16:09:59:059] 2827
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void readSinglePassportObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			cache.put(0l, passport);
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				passport = cache.get(0l);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 11:15:02:002] 7555
			// [2021-02-25 11:16:06:006] 7068
			// [2021-02-25 11:18:27:027] 6820
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveMultiplePassportBinaryObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final IgniteCache<Long, BinaryObject> bIgniteCache = cache.withKeepBinary();
			final BinaryObject binaryObject = bIgniteCache.get(0l);
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				bIgniteCache.put(i, binaryObject);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(cache.get(999999l).toString());
			// Test result
			// [2021-02-25 15:36:03:003] 50717
			// [2021-02-25 15:37:48:048] 45891
			// [2021-02-25 15:39:22:022] 43921
			// [2021-02-25 15:43:15:015] 42292
			// 开启WAL之后
			// 33571
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveMultiplePassportBytes() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				cacheBinary.put(i, passport.getBytes());
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(cacheBinary.get(999999l).toString());
			// Test result
			// [2021-02-25 16:32:22:022] 14154
			// [2021-02-25 16:33:17:017] 13806
			// [2021-02-25 16:34:12:012] 15728
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveMultiplePassportObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				cache.put(i, passport);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(cache.get(999999l).toString());
			// Test result
			// [2021-02-25 11:28:48:048] 18900
			// [2021-02-25 11:29:57:057] 18759
			// [2021-02-25 11:31:27:027] 19086
			// 开启WAL之后
			// 33571
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void savePassport() {
		Passport passport;
		try {

			final TcpDiscoverySpi spi = new TcpDiscoverySpi();
			final TcpDiscoveryMulticastIpFinder tcMp = new TcpDiscoveryMulticastIpFinder();
			tcMp.setAddresses(Arrays.asList("localhost"));
			spi.setIpFinder(tcMp);
			final IgniteConfiguration cfg = new IgniteConfiguration();
			cfg.setClientMode(true);
			cfg.setDiscoverySpi(spi);
			final Ignite ignite = Ignition.start(cfg);

			final CacheConfiguration cacheCfg = new CacheConfiguration();
			cacheCfg.setName(passportCache);
			cacheCfg.setCacheMode(CacheMode.PARTITIONED);
			cache = ignite.getOrCreateCache(cacheCfg);

			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final BinaryObject binaryObject = ignite.binary().toBinary(passport);
			final long time = System.currentTimeMillis();
			// final Map<Long, Passport> map = new HashMap<>();
			// // cache.putAll(map);
			//			Log.info("" + time + " Len: " + passport.getBytes().length);
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
	void savePassport1() {
		Passport passport;
		try {
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			cache.put(0l, passport);
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 1; i < 100000l; ++i) {
				passport = cache.get(0l);
				passport.increaseNonce();
				passport.deposit(new Value(i));
				cache.put(0l, passport);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(cache.get(0l).toString());
			// Test result
			// [2021-02-25 10:25:13:013] 3161
			// [2021-02-25 10:26:06:006] 3241
			// [2021-02-25 10:27:08:008] 3226
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

	@Test
	void savePassportBinary1() {
		try {
			Passport passport;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final byte[] passport1 = new byte[100 * 1024 * 1024];// passport.getBytes();
			cacheBinary.put(0l, passport.getBytes());
			byte[] bytes = null;
			Passport passport2 = null;
			final long time = System.currentTimeMillis();
			Log.info("" + time);
			for (long i = 1; i < 100000l; ++i) {
				bytes = cacheBinary.get(0l);
				passport2 = new Passport(bytes);
				passport2.increaseNonce();
				passport2.deposit(new Value(i));
				cacheBinary.put(0l, passport2.getBytes());
				// Log.info("" + i);
			}
			// cache.putAllAsync(map);
			Log.info("" + (System.currentTimeMillis() - time));
			passport2 = new Passport(cacheBinary.get(0l));
			Log.info(passport2.toString());
			// Test result
			// [2021-02-25 10:12:01:001] 3912
			// [2021-02-25 10:16:12:012] 3300
			// [2021-02-25 10:17:48:048] 3172
			// [2021-02-25 10:28:21:021] 3118
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveSinglePassportBinaryObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final IgniteCache<Long, BinaryObject> bIgniteCache = cache.withKeepBinary();
			final BinaryObject binaryObject = bIgniteCache.get(0l);
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				bIgniteCache.put(0l, binaryObject);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(binaryObject.toString());
			// Test result
			// [2021-02-25 15:26:35:035] 15576
			// [2021-02-25 15:27:33:033] 15690
			// [2021-02-25 15:28:28:028] 15332
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveSinglePassportBytes() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			final byte[] bytes = passport.getBytes();
			for (long i = 0; i < 1000000l; ++i) {
				cacheBinary.put(0l, bytes);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 16:00:30:030] 6596
			// [2021-02-25 16:01:22:022] 5976
			// [2021-02-25 16:02:15:015] 6013
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	void saveSinglePassportObject() {
		try {
			Passport passport = null;
			passport = new Passport();
			passport.setId(ID.ZERO);
			passport.setLockID(ID.ZERO);
			passport.setUpdateHeight(ID.ZERO);
			passport.deposit(Util.MIN_BALANCE);
			passport.increaseNonce();
			final long time = System.currentTimeMillis();
			Log.info("" + time + " Len: " + passport.getBytes().length);
			for (long i = 0; i < 1000000l; ++i) {
				cache.put(0l, passport);
			}
			Log.info("" + (System.currentTimeMillis() - time));
			Log.info(passport.toString());
			// Test result
			// [2021-02-25 11:20:55:055] 11499
			// [2021-02-25 11:21:55:055] 10163
			// [2021-02-25 11:22:52:052] 9800
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
