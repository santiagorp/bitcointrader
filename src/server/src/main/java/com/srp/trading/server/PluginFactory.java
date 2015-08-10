/**
 * 
 */
package com.srp.trading.server;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.srp.trading.common.IOHelper;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.plugin.BasePlugin;
import com.srp.trading.plugin.IPluginAPI;
import com.srp.trading.plugin.IPluginData;
import com.srp.trading.plugin.PluginConfig;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class PluginFactory implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private Logger logger = Logger.getLogger(PluginFactory.class.getName());
	private boolean initialized = false;
	
	// Contains all the valid plugin description indexced by plugin name
	private HashMap<String, PluginDescription> availablePlugins = new HashMap<String, PluginDescription>();

	// Contains all the valid jars for plugins indexed by plugin name
	private HashMap<String, PluginLoadInfo> loadInfo = new HashMap<String, PluginLoadInfo>(); 
	private String pluginsPath;
	
	public PluginFactory() {		
	}
	
	/**
	 * Initialize the factory reading all available plugins
	 */
	public void Initialize(String pluginsPath) {
		if (initialized)
			return;
	
		this.pluginsPath = pluginsPath;
		RefreshAvailablePlugins();
		this.initialized = true;
		logger.info("Initialized plugin factory");
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Get all available plugins
	 */
	public void RefreshAvailablePlugins() {
		availablePlugins.clear();
		loadInfo.clear();
		
		logger.info("Refreshing available plugins list.");
		// Get all the subdirectories in the plugins path
		String[] subDirs = IOHelper.getDirs(pluginsPath);
		
		// Load all the classes from the plugins path
		for (String pluginDir : subDirs) {
			String[] jarPaths = IOHelper.getJarNames(pluginDir);
			for (String jarPath : jarPaths) {
				IPluginData pd = (IPluginData) IOHelper.getInstance(jarPath, "com.srp.trading.plugin.Plugin");
				if (pd == null) {					
					logger.info("Plugin entry point not found in " + jarPath);
					continue;
				}
				
				String classToLoad = pd.getPluginClass();
				IPluginAPI plugin = (IPluginAPI) IOHelper.getInstance(jarPath,  classToLoad);
				if (plugin == null) {
					logger.info("IPluginAPI in class " +  classToLoad + " could not be loaded from " + jarPath);
					continue;
				}
				
				PluginDescription pluginDesc = plugin.getPluginDescription();
				if (pluginDesc == null) {
					logger.info("PluginDescription not found in class " +  classToLoad + " from " + jarPath);
					continue;
				}

				PluginLoadInfo pli = new PluginLoadInfo();
				pli.className = classToLoad;
				pli.jarPath = jarPath;
				loadInfo.put(pluginDesc.getCompoundName(), pli);
				availablePlugins.put(pluginDesc.getCompoundName(),  pluginDesc);
				logger.info("Found plugin: " + pluginDesc.toString());
			}
		}				
	}
	
	/**
	 * Create a plugin using reflection to load the appropiate class
	 * @param exchangeSiteId
	 * @return
	 */
	public IPluginAPI createPlugin(String pluginName) {
		logger.info("Requested plugin load: " + pluginName);
		PluginLoadInfo pli = loadInfo.get(pluginName);
		if (pli == null)
			return null;
		
		BasePlugin plugin = (BasePlugin) IOHelper.getInstance(pli.jarPath, pli.className);
		logger.info("Created new plugin instance: " + plugin.getPluginDescription().toString());
		plugin.setApplicationContext(applicationContext);

		// Initialize plugin paths
		File jarPath = new File(pli.jarPath);
		String pluginPath = jarPath.getParent();
		String confPath = new File(pluginPath, "conf").toString();
		String dataPath = new File(pluginPath, "data").toString();
		String logPath = new File(pluginPath, "log").toString();
		PluginConfig config = new PluginConfig(pli.jarPath, pluginPath, confPath, dataPath, logPath);
		plugin.Initialize(config);		
		logger.info("Plugin initialization finished!");
		
		return plugin;
	}

	public HashMap<String, PluginDescription> getAvailablePlugins() {
		return availablePlugins;
	}
	
	public class PluginLoadInfo {
		public String className;
		public String jarPath;
	}
}
