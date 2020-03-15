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
package com.eqcoin.configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.eqcoin.keystore.Keystore;
import com.eqcoin.serialization.EQCType;
import com.eqcoin.util.Log;
import com.eqcoin.util.Util;

/**
 * @author Xun Wang
 * @date Oct 9, 2018
 * @email 10509759@qq.com
 */
public class Configuration {
	private final String CONFIGURATION_PATH = Util.PATH + "/EQchains.config";
	private static Configuration instance;
	private boolean boolIsInitH2;
	private boolean boolIsInitSingularityBlock;
	
	private Configuration() {
		loadConfig();
	}

	public static Configuration getInstance() {
		if (instance == null) {
			synchronized (Keystore.class) {
				if (instance == null) {
					instance = new Configuration();
				}
			}
		}
		return instance;
	}
	
	public void loadConfig() {
		File file = new File(CONFIGURATION_PATH);
		if (file.exists()) {
			if (file.length() == 0) {
				Log.info("EQchains.config exists but haven't any confuguration just return.");
				return;
			}
			Log.info("EQchains.config exists and not empty just load it.");
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				ByteArrayInputStream bis = new ByteArrayInputStream(is.readAllBytes());
				
				// Parse boolIsInitH2
				boolIsInitH2 = EQCType.eqcBitsToBoolean(EQCType.parseEQCBits(bis));
				
				// Parse boolIsInitSingularityBlock
				boolIsInitSingularityBlock = EQCType.eqcBitsToBoolean(EQCType.parseEQCBits(bis));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("EQchains.config not found: " + e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("Load confuguration failed: " + e.getMessage());
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.Error("Load confuguration failed: " + e.getMessage());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.Error(e.getMessage());
					}
				}
			}
		}
	}
	
	public boolean saveConfiguration() {
		boolean bool = true;
		try {
			File file = new File(CONFIGURATION_PATH);

			// Save all configuration to EQchains.config
			OutputStream os = new FileOutputStream(file);
			// Save boolIsInitH2
			os.write(EQCType.booleanToEQCBits(boolIsInitH2));
			// Save boolIsInitSingularityBlock
			os.write(EQCType.booleanToEQCBits(boolIsInitSingularityBlock));
			os.flush();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			bool = false;
			Log.Error(e.getMessage());
		}
		return bool;
	}

	public void updateIsInitH2(boolean isInitH2) {
		boolIsInitH2 = isInitH2;
		saveConfiguration();
	}
	
	public boolean isInitH2() {
		return boolIsInitH2;
	}
	
	public void updateIsInitSingularityBlock(boolean isInitSingularityBlock) {
		boolIsInitSingularityBlock = isInitSingularityBlock;
		saveConfiguration();
	}
	
	public boolean isInitSingularityBlock() {
		return boolIsInitSingularityBlock;
	}
	
}
