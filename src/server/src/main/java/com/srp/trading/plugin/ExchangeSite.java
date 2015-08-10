/**
 * 
 */
package com.srp.trading.plugin;

import org.apache.log4j.Logger;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class ExchangeSite {
	private Logger logger = Logger.getLogger(ExchangeSite.class.getName());
	
	String id;
	String name;
	
	/**
	 * Create a new exchange site
	 * @param id The id of the exchange. Used to find the appropiate properties file
	 * @param name The descriptive name of the exchange (UI friendly)
	 */
	public ExchangeSite(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
