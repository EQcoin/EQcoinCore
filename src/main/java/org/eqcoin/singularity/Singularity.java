/**
 * EQcoin core
 *
 * http://www.eqcoin.org
 *
 * @copyright 2018-present EQcoin Planet All rights reserved...
 * Copyright of all works released by EQcoin Planet or jointly released by
 * EQcoin Planet with cooperative partners are owned by EQcoin Planet
 * and entitled to protection available from copyright law by country as well as
 * international conventions.
 * Attribution — You must give appropriate credit, provide a link to the license.
 * Non Commercial — You may not use the material for commercial purposes.
 * No Derivatives — If you remix, transform, or build upon the material, you may
 * not distribute the modified material.
 * For any use of above stated content of copyright beyond the scope of fair use
 * or without prior written permission, EQcoin Planet reserves all rights to take
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
package org.eqcoin.singularity;


import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.eqcoin.stateobject.passport.Passport;
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

			Ignite ignite;
			final String passportCache = "passportCache";
			final String passportCacheBinary = "passportCacheBinary";
			IgniteCache<Long, Passport> cache;
			IgniteCache<Long, byte[]> cacheBinary;
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
