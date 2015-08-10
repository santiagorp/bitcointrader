/**
 * 
 */
package com.srp.trading.common;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class Util {
	private static Logger logger = Logger.getLogger(Util.class.getName());
	
	/**
	 * Unify the output printing the date/time
	 * @param msg
	 */
	public static void printOutput(Logger logger, String msg) {		
		Date now = new Date();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		System.out.println(df.format(now) + ": " + msg);
		
		if (logger != null) {
			logger.info(msg);
		}
	}
	
	/**
	 * Log the memory stats
	 * @param logger
	 * @param msg
	 */
	public static void logMemStats(Logger logger, String msg) {
        Runtime runtime = Runtime.getRuntime();
        int mb = 1024*1024;
        logger.info("##### Heap utilization statistics [MB] #####: " + msg);
        logger.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb); 
        logger.info("Free Memory:" + runtime.freeMemory() / mb);
        logger.info("Total Memory:" + runtime.totalMemory() / mb);
        logger.info("Max Memory:" + runtime.maxMemory() / mb);
    }
}
