/**
 * 
 */
package com.srp.trading.plugin;

import java.util.HashMap;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class PluginContainer {
	private HashMap<Integer, IPluginAPI> loadedPlugins = new HashMap<Integer, IPluginAPI>();

	public HashMap<Integer, IPluginAPI> getLoadedPlugins() {
		return loadedPlugins;
	}	
}
