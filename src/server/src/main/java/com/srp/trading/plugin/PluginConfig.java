/**
 * 
 */
package com.srp.trading.plugin;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class PluginConfig {
	private String configPath;
	private String dataPath;
	private String pluginPath;
	private String logPath;
	private String jarPath;
	
	/**
	 * Initialize the base paths required for the plugins
	 * @param configPath Path to the directory where the config files will be located
	 * @param dataPath Path to the directory where the data base files or other data files will be located
	 * @param pluginPath Path to the directory where the plugins will be loaded
	 */
	public PluginConfig(String jarPath, String pluginPath, String configPath, String dataPath, String logPath) {
		this.jarPath = jarPath;
		this.configPath = configPath;
		this.dataPath = dataPath;
		this.pluginPath = pluginPath;
		this.logPath = logPath;
	}
	
	public String getConfigPath() {
		return configPath;
	}

	public String getJarPath() {
		return jarPath;
	}
	
	public String getDataPath() {
		return dataPath;
	}

	public String getPluginPath() {
		return pluginPath;
	}

	public String getLogPath() {
		return logPath;
	}
}
