/**
 * 
 */
package com.srp.trading.plugin.scalper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.srp.trading.common.JarClassLoader;
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
public class ScalperPlugin extends BasePluginDB {
	private static Logger logger = Logger.getLogger(ScalperPlugin.class.getName());
	private Settings settings = null;
	private String selectedSite = null;
	private ExecutorService executorService = null;
	private Future<?> future = null;
	private ExchangeFactory exchangeFactory;
	private Runnable runnable = null;
	private Appender appender = null;
	private String siteName;
	private String menuPosition = "main";
	Properties parametersDescription = null;

	public ScalperPlugin() {
		super("ScalperPlugin", new Version("2.4"), "Scalping plugin");
	}

	@Override
	public void Initialize(PluginConfig config) {
		super.Initialize(config);
		exchangeFactory = (ExchangeFactory) this.applicationContext.getBean("exchangeFactory");		
		logger.info("Initialized plugin: " + pluginDesc.toString());
	}

	@Override
	public List<CommandDefinition> getCommandDefinitions() {
		List<CommandDefinition> commands = new ArrayList<CommandDefinition>();
		logger.info("Retrieving list of valid commands");

		if (menuPosition == "main") {
				if (selectedSite == null || selectedSite.isEmpty()) {
				commands.add(new CommandDefinition("Bitstamp", "Select Bitstamp as exchange site "));
				commands.add(new CommandDefinition("BTC-e", "Select  BTC-e as exchange site"));
				commands.add(new CommandDefinition("MtGox", "Select MtGox as exchange site"));
				commands.add(new CommandDefinition("BTCChina", "Select BTCChina as exchange site"));
			} else {
				if (future != null && !future.isCancelled() && !future.isDone()) {
					commands.add(new CommandDefinition("Stop scalping", "Stop the scalping trader"));
				} else {
					commands.add(new CommandDefinition("Start scalping", "Start the scalping trader"));					
					commands.add(new CommandDefinition("Settings", "Modify settings for " + siteName));
					commands.add(new CommandDefinition("Disconnect", "Disconnect from " + siteName));
				}
			}
		} else  if (menuPosition == "config") {
			commands.add(new CommandDefinition("Go back", "Go back to the main menu"));
			CommandDefinition pluginParametersCmd = getConfigCommand();
			if (pluginParametersCmd != null) {
				commands.add(pluginParametersCmd);
			}			
		}

		return commands;
	}

	/**
	 * Get the plugin configuration settings
	 * 
	 * @return
	 */
	private CommandDefinition getConfigCommand() {
		CommandDefinition cmdDef = new CommandDefinition("Scalper configuration", "Modify the plugin parameters for the " + siteName + " scalper");
		
		try {
			// Get parameters descriptions
			if (parametersDescription == null) {
				JarClassLoader cl = new JarClassLoader(pluginConfig.getJarPath());
				InputStream descsStream =  cl.getResourceAsStream("parametersDescription.properties");
				parametersDescription = new Properties();
				parametersDescription.load(descsStream);
			}
			
			// Load the parameters
			Properties prop = settings.getConfigProperties(pluginConfig);
			for (Object key : prop.keySet()) {
				String name = key.toString();
				String value = prop.getProperty(name);
				String description = name;
				if (parametersDescription != null && parametersDescription.containsKey(name)) {
					description = parametersDescription.getProperty(name);
				}
				cmdDef.addParameter(name, description, value);
			}
		} catch (Exception e) {
			logger.info(e);
			return null;
		}

		return cmdDef;
	}

	@Override
	public void shutdown() {
		logger.info("Plugin shutdown started.");
		super.shutdown();
		forceStop();
		logger.info("Plugin shutdown finished.");
		removePackageAppender();
	}

	/**
	 * Force the stop of the plugin (scalping)
	 */
	private void forceStop() {
		if (future != null) {
			future.cancel(true);
			try {
				logger.info("Finishing plugin thread");
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.info("Error finishing plugin thread.");
				logger.info(e);
			}
			executorService = null;
			future = null;
		}
	}

	/**
	 * Connect to the currently selected exchange. In case of error reset the
	 * selected site
	 * 
	 * @return
	 */
	private BasicExecutionResult connectToExchange() {
		BasicExecutionResult result;
		settings = new Settings(exchangeFactory, selectedSite);
		try {
			settings.initialize(pluginConfig);
			result = new BasicExecutionResult(true, "Selected site: " + selectedSite);
			dbName = selectedSite;
			updatePackageAppender(selectedSite);
		} catch (Exception e) {
			selectedSite = null;
			result = new BasicExecutionResult(false, e.getMessage());
			logger.info("Exchange site could not be established");
			logger.info(e);
		}
		return result;
	}

	@Override
	public BasicExecutionResult executeCommand(Command cmd) {
		BasicExecutionResult result = null;
		logger.info("Received command: " + cmd.getName());

		// Check if it's a valid command
		if (!isValidCommand(cmd)) {
			result = new BasicExecutionResult(false, "Command not valid");
			return result;
		}

		switch (cmd.getName()) {
		case "Bitstamp":
			siteName = cmd.getName();
			selectedSite = "bitstamp";
			result = connectToExchange();
			break;
		case "BTC-e":
			siteName = cmd.getName();
			selectedSite = "btce";
			result = connectToExchange();
			break;
		case "MtGox":
			siteName = cmd.getName();
			selectedSite = "mtgox";
			result = connectToExchange();
			break;
		case "BTCChina":
			siteName = cmd.getName();
			selectedSite = "btcchina";
			result = connectToExchange();
			break;
		case "Start scalping":
			// Check that no other scalpers on the same stock are running
			boolean canScalp = true;
			Server server = (Server) applicationContext.getBean("server");
			for (IPluginAPI plugin : server.getLoadedPlugins().values()) {
				if (plugin instanceof ScalperPlugin == false)
					continue;

				ScalperPlugin scalper = (ScalperPlugin) plugin;
				if (scalper.selectedSite != null && scalper.selectedSite.equals(this.selectedSite) && scalper.future != null) {
					canScalp = false;
					break;
				}
			}

			if (!canScalp) {
				result = new BasicExecutionResult(false, "Scalping not allowed. Another plugin scalping in the same site.");
			} else {
				executorService = Executors.newSingleThreadExecutor();
				runnable = getRunnable();
				future = executorService.submit(runnable);
				result = new BasicExecutionResult(true, "Scalping started!");
				logger.info("Scalping started!");
			}
			break;
		case "Stop scalping":
			forceStop();
			result = new BasicExecutionResult(true, "scalping stopped!");
			logger.info("Scalping stopped!");
			break;
		case "Disconnect":
			forceStop();			
			result = new BasicExecutionResult(true, "Disconnected from " + siteName);
			selectedSite = null;
			siteName = null;
			settings = null;			
			break;
		case "Scalper configuration":
			boolean saved = false;
			try {
				Properties prop = settings.getConfigProperties(pluginConfig);
				for (String key : cmd.getParameters().keySet()) {
					String value = cmd.getParameters().get(key);
					if (prop.containsKey(key)) {
						prop.setProperty(key, value);
					}
				}
				saved = settings.saveConfigProperties(prop, pluginConfig, pluginDesc);
			} catch (Exception e) {
				logger.info("Error saving the settings.");
				logger.info(e);
			}

			if (saved) {
				result = new BasicExecutionResult(true, siteName + " scalper settings updated");
				result.getAdditionalData().put("output", siteName + " Scalper settings updated");
			} else {
				result = new BasicExecutionResult(false, "Error saving " + siteName + " scalper settings");
			}
			break;
		case "Settings":
			menuPosition = "config";
			result = new BasicExecutionResult(true, "");
			break;
		case "Go back":
			menuPosition = "main";
			result = new BasicExecutionResult(true, "");
			break;
		default:
			result = new BasicExecutionResult(false, "Command not found");
			logger.info("Command not found.");
			break;
		}

		return result;
	}

	/**
	 * Returns ture if the command is accepted in the current plugin state
	 * 
	 * @param cmd
	 * @return
	 */
	private boolean isValidCommand(Command cmd) {
		boolean valid = false;
		for (CommandDefinition accepted : getCommandDefinitions()) {
			if (accepted.getName().equals(cmd.getName())) {
				valid = true;
				break;
			}
		}

		return valid;
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

	/**
	 * Retrieve the appropiate runnable depending of the streamming support
	 * 
	 * @return
	 */
	private Runnable getRunnable() {
		Runnable result = null;
		if (settings.getExchangeStreamConfig() == null) {
			result = new PollingRunnable(settings, pluginConfig);
		} else {
			result = new StreamingRunnable(settings, pluginConfig);
		}

		return result;
	}

	@Override
	public String getStatusMessage() {
		if (selectedSite == null || selectedSite.isEmpty()) {
			return "Not initialized";
		}
		String msg = selectedSite;
		if (future != null && !future.isCancelled() && !future.isDone()) {
			msg += ", Scalping";
		} else {
			msg += ", Stopped";
		}
		return msg;
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
	}
}
