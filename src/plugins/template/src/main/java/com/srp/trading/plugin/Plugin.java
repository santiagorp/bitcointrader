/**
 * 
 */
package com.srp.trading.plugin;

import com.srp.trading.plugin.template.TemplatePlugin;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class Plugin implements IPluginData {
	/**
	 * Get the load class of the plugin
	 */
	public String getPluginClass() {
		return TemplatePlugin.class.getCanonicalName();
	}

}
