/**
 * 
 */
package com.srp.trading.plugin.template;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.Version;
import com.srp.trading.plugin.BasePluginDB;
import com.srp.trading.plugin.IPluginAPI;
import com.srp.trading.plugin.PluginConfig;
import com.srp.trading.server.ExchangeFactory;
import com.srp.trading.server.Server;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class TemplatePlugin extends BasePluginDB {
	private static Logger logger = Logger.getLogger(TemplatePlugin.class.getName());
	Future<?> future = null;
	ExchangeFactory exchangeFactory;
	Appender appender = null;
	
	public TemplatePlugin() {
		super("TemplatePlugin", new Version("1.0"), "Template plugin");
	}
	
	@Override
	public void Initialize(PluginConfig config) {
		super.Initialize(config);
		exchangeFactory = (ExchangeFactory) this.applicationContext.getBean("exchangeFactory");
		updatePackageAppender("template");
		logger.info("Initialized plugin: " + pluginDesc.toString());
	}	
	
	@Override
	public List<CommandDefinition> getCommandDefinitions() {
		List<CommandDefinition> commands = new ArrayList<CommandDefinition>();
		logger.info("Retrieving list of valid commands");
				
		return commands;
	}
	
	@Override
	public void shutdown() {
		logger.info("Plugin shutdown started.");
		super.shutdown();
		logger.info("Plugin shutdown finished.");
		removePackageAppender(); // Remove the log appender for the current package
	}
	
	@Override
	public BasicExecutionResult executeCommand(Command cmd) {
		return new BasicExecutionResult(false, "Command not valid");
	}

	@Override
	public PluginDescription getPluginDescription() {
		return pluginDesc;
	}

	@Override
	public BasicExecutionResult canBeLoaded() {
		BasicExecutionResult result = new BasicExecutionResult(true, null);
		return result;		
	}
	
	@Override
	public String getStatusMessage() {
		return "Ready";		
	}
	
	/**
	 * Add the log appender for the specified site in the current plugin package
	 */
	private void updatePackageAppender(String baseName) {
		Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
		removePackageAppender();
		appender = createAppender(baseName);
		logger.addAppender(appender);
	}

	/**
	 * Remove the log appender for the specified site in the current plugin
	 * package
	 */
	private void removePackageAppender() {
		Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
		if (appender != null) {
			logger.removeAppender(appender);
		}
	}}
