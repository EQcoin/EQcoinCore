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
 * Wandering Earth Corporation reserves any and all current and future rights, 
 * titles and interests in any and all intellectual property rights of Wandering Earth 
 * Corporation, including but not limited to discoveries, ideas, marks, concepts, 
 * methods, formulas, processes, codes, software, inventions, compositions, techniques, 
 * information and data, whether or not protectable in trademark, copyrightable 
 * or patentable, and any trademarks, copyrights or patents based thereon.
 * For any use of above stated content of copyright beyond the scope of fair
 * use or without prior written permission, Wandering Earth Corporation reserves
 * all rights to take any legal action and pursue any right or remedy available
 * under applicable law.
 */
package org.eqcoin.singularity;


import org.eqcoin.util.Log;
import org.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public class Singularity {
	public static void main(final String[] args) {
		Thread.currentThread().setPriority(10);
		try {
			//			Log.info(Util.DB().getLock(ID.THREE, Mode.GLOBAL).toString());
			//			Log.info(Util.DB().getPassport(ID.ZERO, Mode.GLOBAL).toString());
			//			EQCHive eqcHive = new EQCHive(Util.DB().getEQCHive(ID.ZERO));
			//			Log.info(Util.DB().getEQCHive(ID.ZERO).toString());
			//			Util.init();
			//			Util.GS().getConnection().commit();
			//			for(int i=0; i<30; ++i)
			//			Log.info(ID.THREE.toString());
			//			EQCDesktopWalletH2.getInstance().generateKeyPair(EQCDesktopWalletH2.getInstance().generateAlais().toString(), ECCTYPE.P521);
			//			EQCHiveH2.getInstance().saveEQCHiveFile(new EQCHive(Util.DB().getEQCHive(ID.ZERO)));
			//			Log.info("TH: " + Util.DB().getEQCHiveTailHeight());
			Util.IsDeleteTransactionInPool = true;

			long n = 100000000;
			double p = 0.001;
			if (p == 0) {
				p = Double.MIN_VALUE;
			}
			Log.info("" + (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2))));
		

//			Ignite ignite;
//			final String passportCache = "passportCache";
//			final String passportCacheBinary = "passportCacheBinary";
//			IgniteCache<Long, Passport> cache;
//			IgniteCache<Long, byte[]> cacheBinary;
//			final IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
//			final DataStorageConfiguration dataStorageConfiguration = new DataStorageConfiguration();
//			dataStorageConfiguration.getDefaultDataRegionConfiguration().setPersistenceEnabled(true);
//			dataStorageConfiguration.getDefaultDataRegionConfiguration().setMaxSize(251 * 1024 * 1024);
//			// dataStorageConfiguration.setWalMode(WALMode.NONE);
//			igniteConfiguration.setDataStorageConfiguration(dataStorageConfiguration);
//			igniteConfiguration.setConsistentId("abc");
//			ignite = Ignition.start(igniteConfiguration);
//			if (ignite.cluster().state() != ClusterState.ACTIVE) {
//				ignite.cluster().state(ClusterState.ACTIVE);
//			}
//			final CacheConfiguration cacheCfg = new CacheConfiguration();
//			cacheCfg.setName(passportCache);
//			cacheCfg.setCacheMode(CacheMode.PARTITIONED);
//			cache = ignite.getOrCreateCache(cacheCfg);
//			final CacheConfiguration cacheCfgBinary = new CacheConfiguration();
//			cacheCfgBinary.setName(passportCacheBinary);
//			cacheCfgBinary.setCacheMode(CacheMode.PARTITIONED);
//			// cacheCfgBinary.setWriteBehindEnabled(false);
//			cacheBinary = ignite.getOrCreateCache(cacheCfgBinary);
			// ignite.cluster().disableWal(passportCache);
			// ignite.cluster().disableWal(passportCacheBinary);
			//			EQCDesktopWalletH2.getInstance().setUserName("nju2006");
			//			Util.LOCAL_SP.setIp(args[0]);
			////			Util.SINGULARITY_SP.setIp(args[0]);
			////			Util.LOCAL_SP.setFlag(SP_MODE.getFlag(SP_MODE.EQCHIVESYNCNETWORK));
			//			EQCServiceProvider.getInstance().setSp(Util.LOCAL_SP).start();
			//			TransTest.Tranfer(0, 1, TRANSACTION_PRIORITY.ASAP, true, 1);
			//			TailInfo tailInfo = EQCHiveSyncNetworkClient.getEQCHiveTail(Util.SINGULARITY_SP);
			//			Thread.sleep(100000000000000000L);
			//			MinerService.getInstance().start();
			//////			for(int i=0; i<10; ++i)
			//			MinerService.getInstance().miningOneEQCHive();
		} catch (final Exception e) {
			Log.Error(e.getMessage());
		}
	}
}
