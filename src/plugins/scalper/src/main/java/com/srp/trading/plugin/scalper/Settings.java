/**
 * 
 */
package com.srp.trading.plugin.scalper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.joda.money.CurrencyUnit;

import com.srp.trading.core.PluginDescription;
import com.srp.trading.plugin.PluginConfig;
import com.srp.trading.server.ExchangeFactory;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeException;
import com.xeiam.xchange.NotAvailableFromExchangeException;
import com.xeiam.xchange.NotYetImplementedForExchangeException;
import com.xeiam.xchange.currency.Currencies;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.service.streaming.ExchangeStreamingConfiguration;
import com.xeiam.xchange.utils.Base64.OutputStream;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class Settings {
	private Logger logger = Logger.getLogger(Settings.class.getName());

    private Trend trend = Trend.Undefined;
	private String exchangeSite;
	private Exchange exchange;
	private ExchangeFactory exchangeFactory;
	private String tradableIdentifier;
	private String transactionCurrency;
	private Ticker lastTicker;
	private Date timeOfLastOrder = new Date();
	private int orderDelay;
	private Date lastOpenOrdersRetrieval = new Date();
	private int retrieveOpenOrdersDelay;
	private BigDecimal targetProfit;
	private BigDecimal profitOrderRatio;
	private BigDecimal fee;
	private BigDecimal scalpAmount;
	private boolean forceMinProfit;  // if true, the target profit of operations is always the minimum targetProfit, and profitOrderRatio is ignored
	private int maxPendingOperations;
	private int pollingDelay;
	private int numDecimalsAmount = 8; // Number of decimals to be used placing orders for the BTC amount. Bitcoin supports till 5
	private int numDecimalsPrice = 5; // Number of decimals to be used in the order price (for the transaction currency/fiat). Usually 5
	private BigDecimal cancelOrdersPerc; 
	private ExchangeStreamingConfiguration exchangeStreamConfig = null;
	
	public Settings(ExchangeFactory exchangeFactory, String exchangeSite) {
		this.exchangeFactory = exchangeFactory;
		this.exchangeSite = exchangeSite;
	}
	
	/**
	 * Initialize exchange specific settings
	 * @throws Exception
	 */
	public void initialize(PluginConfig config) throws Exception {
		Properties prop = getConfigProperties(config);
		parseSettings(prop);
				
		String profitPercent = targetProfit.multiply(new BigDecimal(100)).toPlainString();
		String msg = String.format("Current exchange: %s, Fees: %s %%, Scalp amount: %s, target profit: %s, Max num operations: %d, Trend: %s",
				new Object[] {exchangeSite, fee.toPlainString(), scalpAmount.toPlainString(), profitPercent, maxPendingOperations, trend.toString()});
		logger.info(msg);
		this.exchange = exchangeFactory.createExchange(exchangeSite);
	}
	
	/**
	 * Get the current config properties
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public Properties getConfigProperties(PluginConfig pluginConfig) throws Exception{
		String configFileName = new File(pluginConfig.getConfigPath(), exchangeSite + ".properties").toString();		
		Properties prop = new Properties();
		prop.load(new FileInputStream(configFileName));
		
		return prop;
	}
	
	/**
	 * Save the config properties into the file
	 * @param prop
	 * @return
	 */
	public boolean saveConfigProperties(Properties prop, PluginConfig pluginConfig, PluginDescription pluginDesc) {		
		StringBuilder sb = new StringBuilder();
		sb.append("Configuraton properties for the plugin ").append(pluginDesc.getName()).append(", v").append(pluginDesc.getVersion().toString());
		File confFile = new File(pluginConfig.getConfigPath(), exchangeSite + ".properties");		
		try {
			Writer writer = new FileWriter(confFile);
			prop.store(writer, sb.toString());
		} catch (IOException e) {
			logger.info(e);
			return false;
		}
		return true;
	}
	
	/**
	 * Parse the settings from the specified config file
	 * @param configFileName
	 * @throws Exception
	 */
	private void parseSettings(Properties prop) {
		String placeOrdersDelayStr = prop.getProperty("placeOrderDelay");
		orderDelay = Integer.parseInt(placeOrdersDelayStr);

		String getOpenOrdersDelayStr = prop.getProperty("getOpenOrdersDelay");
		retrieveOpenOrdersDelay = Integer.parseInt(getOpenOrdersDelayStr);
		
		String maxPendingOperationsStr = prop.getProperty("maxPendingOperations");
		maxPendingOperations = Integer.parseInt(maxPendingOperationsStr);
		
		String scalpAmountStr = prop.getProperty("scalpAmount");
		scalpAmount = new BigDecimal(scalpAmountStr);
		
		String minProfitStr = prop.getProperty("minProfit");
		targetProfit = new BigDecimal(minProfitStr).divide(new BigDecimal(100), 8, RoundingMode.FLOOR);
		
		String cancelOrderPercStr = prop.getProperty("cancelOrderPerc");
		if (cancelOrderPercStr == null || cancelOrderPercStr.isEmpty()) {
			cancelOrderPercStr = "2"; // Default value (2%)
		}
		cancelOrdersPerc = new BigDecimal(cancelOrderPercStr).divide(new BigDecimal(100), 8, RoundingMode.FLOOR).add(new BigDecimal("1"));				
		
		String forceMinProfitStr = prop.getProperty("forceMinProfit");
		if (forceMinProfitStr != null) {
			forceMinProfit = Boolean.parseBoolean(forceMinProfitStr);
		} else {
			forceMinProfit = false;
		}
		String profitOrderRatioStr = prop.getProperty("profitOrderRatio");
		if (profitOrderRatioStr != null) {
			profitOrderRatio = new BigDecimal(profitOrderRatioStr);
		} else {
			profitOrderRatio = new BigDecimal(1);
		}
		
		String numDecimalsAmountStr = prop.getProperty("numDecimalsAmount");
		if (numDecimalsAmountStr != null) {
			numDecimalsAmount = Integer.parseInt(numDecimalsAmountStr);
		}
		
		String numDecimalsPriceStr = prop.getProperty("numDecimalsPrice");
		if (numDecimalsPriceStr != null) {
			numDecimalsPrice = Integer.parseInt(numDecimalsPriceStr);;
		}

        String trendStr = prop.getProperty("trend");
        if (trendStr != null) {
        	trendStr = trendStr.toLowerCase();
            switch (trendStr) {
                case "up":
                    this.trend = Trend.Up;
                 break;
                case "down":
                    this.trend = Trend.Down;
                    break;
                default:
                    this.trend = Trend.Undefined;
                    break;
            }
        } else {
            this.trend = Trend.Undefined;
        }
        
		
		tradableIdentifier = Currencies.BTC;
		
		String fiatCurrencyString = prop.getProperty("fiatCurrency");
		if (fiatCurrencyString != null) {
		  transactionCurrency = fiatCurrencyString;	
		} else {
		  transactionCurrency = Currencies.USD;	
		}

		String feeStr = prop.getProperty("fee");
		this.fee = (new BigDecimal(feeStr)).divide(new BigDecimal(100), 8, RoundingMode.FLOOR);

		String pollingDelayStr = prop.getProperty("pollingDelay");
		if (pollingDelayStr != null) {
			pollingDelay = Integer.parseInt(pollingDelayStr);
		}
	}
		
	/**
	 * Update the ticker using the polling market service
	 * @throws IOException 
	 */
	public void updateTicker() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
		this.lastTicker = this.getExchange().getPollingMarketDataService().getTicker(tradableIdentifier, transactionCurrency);		
	}

    public Trend getTrend() {
        return this.trend;
    }

    public void setTrend(Trend trend) {
        this.trend = trend;
    }   

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange mtgoxAPI) {
		this.exchange = mtgoxAPI;
	}

	public String getTradableIdentifier() {
		return tradableIdentifier;
	}

	public String getTransactionCurrency() {
		return transactionCurrency;
	}

	public CurrencyUnit getCurrencyUnit() {
		return CurrencyUnit.of(transactionCurrency);
	}

	public Ticker getLastTicker() {
		return lastTicker;
	}

	public void setLastTicker(Ticker lastTicker) {
		this.lastTicker = lastTicker;
	}

	public Date getTimeOfLastOrder() {
		return timeOfLastOrder;
	}

	public void setTimeOfLastOrder(Date timeOfLastOrder) {
		this.timeOfLastOrder = timeOfLastOrder;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public BigDecimal getTargetProfit() {
		return targetProfit;
	}

	public BigDecimal getProfitOrderRatio() {
		return profitOrderRatio;
	}

	public BigDecimal getScalpAmount() {
		return scalpAmount;
	}
	
	public boolean getForceMinProfit() {
		return forceMinProfit;
	}

	public int getMaxPendingOperations() {
		return maxPendingOperations;
	}

	/**
	 * Retrieve the minimum order delay in seconds for placing orders
	 * 
	 * @return
	 */
	public int getOrderDelay() {
		return orderDelay;
	}

	public Date getLastOpenOrdersRetrieval() {
		return lastOpenOrdersRetrieval;
	}

	public void setLastOpenOrdersRetrieval(Date lastOpenOrdersRetrieval) {
		this.lastOpenOrdersRetrieval = lastOpenOrdersRetrieval;
	}

	/**
	 * @return Time that we should wait between getting open orders (in seconds)
	 */
	public int getRetrieveOpenOrdersDelay() {
		return retrieveOpenOrdersDelay;
	}

	public String getExchangeSite() {
		return exchangeSite;
	}
	
	/**
	 * @return Delay that we should wait for polling (in milliseconds)
	 */
	public int getPollingDelay() {
		return pollingDelay;
	}

	/**
	 * Returns the stream configuration for the curent exchange. Null if not supported streaming.
	 * @return
	 */
	public ExchangeStreamingConfiguration getExchangeStreamConfig () {
		return exchangeStreamConfig ;
	}

	public int getNumDecimalsAmount() {
		return numDecimalsAmount;
	}

	public int getNumDecimalsPrice() {
		return numDecimalsPrice;
	}

	public BigDecimal getCancelOrdersPerc() {
		return cancelOrdersPerc;
	}
}
