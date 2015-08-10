/**
 * 
 */
package com.srp.trading.plugin;

import com.srp.trading.plugin.exchangeutil.ExchangeUtilPlugin;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class Plugin implements IPluginData {
	/**
	 * Get the load class of the plugin
	 */
	public String getPluginClass() {
		return ExchangeUtilPlugin.class.getCanonicalName();
	}
}
