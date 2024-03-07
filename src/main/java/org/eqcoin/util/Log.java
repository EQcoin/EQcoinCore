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
package org.eqcoin.util;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author Xun Wang
 * @date 9-11-2018
 * @email 10509759@qq.com
 */
public final class Log {

	private static Logger log;
	private static FileHandler fileHandler;
	private static ConsoleHandler consoleHandler;
	private static boolean DEBUG = true;

	private Log() {}

	private static void instance() {
		if (log == null) {
			try {
				log = Logger.getLogger(MethodHandles.lookup().lookupClass().getSimpleName());
				log.setLevel(Level.ALL);
				log.setUseParentHandlers(false);
				fileHandler = new FileHandler(Util.LOG_PATH, Util.ONE_MB, 1000, true);
				fileHandler.setFormatter(new EQCFormatter());
				log.addHandler(fileHandler);
				consoleHandler = new ConsoleHandler();
				consoleHandler.setFormatter(new EQCFormatter());
				log.addHandler(consoleHandler);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println(e.getMessage());
			} 
		}
	}

	public static void info(String info) {
		if (DEBUG) {
			instance();
			log.info(info);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			fileHandler.flush();
		}
	}
	
	public static void Error(String error) {
		if (DEBUG) {
			instance();
			log.info("[ERROR]" + error);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			fileHandler.flush();
		}
	}
	
	public static void Warn(String warn) {
		if (DEBUG) {
			instance();
			log.info("[WARN]" + warn);
			// flush buffer immediately otherwise the log data in the buffer maybe missing
			fileHandler.flush();
		}
	}
	
	public static String getStringDate(long timestamp) {
		   Date currentTime = new Date(timestamp);
		   SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss:sss]");
		   String dateString = formatter.format(currentTime);
		   return dateString;
		}
	
	public static class EQCFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			return getStringDate(record.getMillis()) + "[" + Thread.currentThread().getStackTrace()[9].getClassName() + "."
					+ Thread.currentThread().getStackTrace()[9].getMethodName() + " "
					+ Thread.currentThread().getStackTrace()[9].getLineNumber() + "]" + record.getMessage() + "\r\n";
		}
	}

}
