/**
 * 
 */
package com.srp.trading.plugin.exchangeutil;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.Version;
import com.srp.trading.plugin.BasePlugin;
import com.srp.trading.plugin.PluginConfig;
import com.srp.trading.server.ExchangeFactory;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.Wallet;
import com.xeiam.xchange.service.polling.PollingAccountService;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class ExchangeUtilPlugin extends BasePlugin {
	private static Logger logger = Logger.getLogger(ExchangeUtilPlugin.class.getName());
	Future<?> future = null;
	ExchangeFactory exchangeFactory;
	Appender appender = null;
	String selectedSite = null;
	Exchange exchange = null;
	String currency = "USD";
	
	public ExchangeUtilPlugin() {
		super("ExchangeUtilPlugin", new Version("0.3"), "Exchange utilities plugin");
	}

	@Override
	public void Initialize(PluginConfig config) {
		super.Initialize(config);
		exchangeFactory = (ExchangeFactory) this.applicationContext.getBean("exchangeFactory");
		updatePackageAppender("exchangeUtil");
		logger.info("Initialized plugin: " + pluginDesc.toString());
	}

	@Override
	public List<CommandDefinition> getCommandDefinitions() {
		List<CommandDefinition> commands = new ArrayList<CommandDefinition>();

		if (selectedSite == null || selectedSite.isEmpty()) {
			commands.add(new CommandDefinition("Bitstamp", "Select Bitstamp as exchange site "));
			commands.add(new CommandDefinition("BTCChina", "Select BTCChina as exchange site"));
			commands.add(new CommandDefinition("BTC-e", "Select  BTC-e as exchange site"));
			commands.add(new CommandDefinition("MtGox", "Select MtGox as exchange site"));
		} else {
			commands.add(new CommandDefinition("Disconnect", "Disconnect from " + selectedSite));
			commands.add(new CommandDefinition("Wallet balance", "Get wallet balance"));
			commands.add(new CommandDefinition("Ticker", "Get exchange ticker"));
		}

		logger.info("Retrieving list of valid commands");
		return commands;
	}

	@Override
	public void shutdown() {
		logger.info("Plugin shutdown started.");
		super.shutdown();
		logger.info("Plugin shutdown finished.");
		removePackageAppender(); // Remove the log appender for the current
									// package
	}

	@Override
	public BasicExecutionResult executeCommand(Command cmd) {
		BasicExecutionResult result;
		// Check if it's a valid command
		if (!isValidCommand(cmd)) {
			return new BasicExecutionResult(false, "Command not valid");
		}

		switch (cmd.getName()) {		
		case "Bitstamp":
			selectedSite = "bitstamp";
			result = connectToExchange();
			break;
		case "BTCChina":
			selectedSite = "btcchina";
			currency = "CNY";
			result = connectToExchange();
			break;
		case "BTC-e":
			selectedSite = "btce";
			result = connectToExchange();
			break;
		case "MtGox":
			selectedSite = "mtgox";
			result = connectToExchange();
			break;
		case "Disconnect":
			result = new BasicExecutionResult(true, "Disconnected from " + selectedSite);
			selectedSite = null;
			exchange = null;
			break;
		case "Wallet balance":
			try {
				PollingAccountService pacs = exchange.getPollingAccountService();
				AccountInfo ai = pacs.getAccountInfo();
				List<Wallet> wallets = ai.getWallets();
				//List<Wallet> wallets = exchange.getPollingAccountService().getAccountInfo().getWallets();				
				StringBuilder sb = new StringBuilder();
				sb.append("Current wallets balance:\n");
				for (Wallet w: wallets) {
					String currency = w.getCurrency();
					String amount = w.getBalance().getAmount().setScale(4, RoundingMode.HALF_DOWN).toPlainString();					
					sb.append(currency).append(": ").append(amount).append(" ").append(currency).append("\n");
				}
				result = new BasicExecutionResult(true, "Success!");
				result.getAdditionalData().put("output", sb.toString());
			} catch (Exception e) {
				result = new BasicExecutionResult(false, "Error retrieving wallet balance: " + e.getMessage());
			}
			break;
		case "Ticker":
			try {
				Ticker ticker = exchange.getPollingMarketDataService().getTicker("BTC", currency);
				result = new BasicExecutionResult(true, "Ticker retrieved");
				result.getAdditionalData().put("output", ticker.toString());
			} catch (Exception e) {
				result = new BasicExecutionResult(false, "Error retrieving ticker: " + e.getMessage());
			}
			break;
		default:
			result = new BasicExecutionResult(false, "Command not valid");
			break;
		}

		return result;
	}

	/**
	 * Connect to the currently selected exchange. In case of error reset the selected site
	 * @return
	 */
	private BasicExecutionResult connectToExchange() {
		BasicExecutionResult result;
		exchange = exchangeFactory.createExchange(selectedSite);
		if (exchange == null) {
			selectedSite = null;
			result = new BasicExecutionResult(false, "Could not connect to exchange. Please check configuration.");
		} else {
			result = new BasicExecutionResult(true, "Connected to " + selectedSite);				
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

	@Override
	public String getStatusMessage() {
		if (selectedSite == null || selectedSite.isEmpty()) {
			return "Not initialized";
		}
		
		String msg = selectedSite + ": Ready";
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
