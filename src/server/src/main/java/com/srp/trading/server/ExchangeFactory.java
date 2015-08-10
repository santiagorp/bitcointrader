/**
 * 
 */
package com.srp.trading.server;

import java.io.File;
import java.io.FileInputStream;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import com.srp.trading.plugin.ExchangeSite;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import com.xeiam.xchange.btce.BTCEExchange;
import com.xeiam.xchange.mtgox.v2.MtGoxExchange;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class ExchangeFactory {
	private Logger logger = Logger.getLogger(ExchangeFactory.class.getName());
	HashMap<String, ExchangeSite> sites = new HashMap<String, ExchangeSite>();
	private boolean initialized = false;

	public ExchangeFactory() {
	}

	/**import 
	 * Register available exchanges
	 */
	public void Initialize() {
		if (initialized)
			return;
		
		ExchangeSite btce = new ExchangeSite("btce", "BTC-e");
		ExchangeSite bitstamp = new ExchangeSite("bitstamp", "Bitstamp");
		ExchangeSite mtgox = new ExchangeSite("mtgox", "MtGox");
		
		sites.put(btce.getId(), btce);
		sites.put(bitstamp.getId(), bitstamp);
		sites.put(mtgox.getId(), mtgox);
		
		initialized = true;
		logger.info("Initialized exchange factory");
	}

	/**
	 * Create a new exchange to operate from its exchange id
	 * @param exchangeId
	 * @return
	 */
	public Exchange createExchange(String exchangeId) {
		String apiKey, apiSecret, username, password;
		ExchangeSpecification exSpec = null;
		String homeDir = System.getProperty("user.home");
		String confDir = new File(homeDir, ".bitcointrader").toString();
		confDir = new File(confDir, "conf").toString();
		String apiKeysFilename = new File(confDir, exchangeId + ".properties").toString();

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(apiKeysFilename));
			switch (exchangeId) {
			case "mtgox":
				apiKey = prop.getProperty("apiKey");
				apiSecret = prop.getProperty("apiSecret");
				exSpec = new ExchangeSpecification(MtGoxExchange.class);
				exSpec.setSecretKey(apiSecret);
				exSpec.setApiKey(apiKey);
				exSpec.setSslUri("https://data.mtgox.com");
				exSpec.setPlainTextUriStreaming("ws://websocket.mtgox.com");
				exSpec.setSslUriStreaming("wss://websocket.mtgox.com");
				break;
			case "bitstamp":
				username = prop.getProperty("username");
				password = prop.getProperty("password");
				exSpec = new BitstampExchange().getDefaultExchangeSpecification();
				exSpec.setUserName(username);
				exSpec.setPassword(password);
				break;
			case "btce":
				apiKey = prop.getProperty("apiKey");
				apiSecret = prop.getProperty("apiSecret");
				exSpec = new ExchangeSpecification(BTCEExchange.class);
				exSpec.setSecretKey(apiSecret);
				exSpec.setApiKey(apiKey);
				exSpec.setSslUri("https://btc-e.com");
				break;
			case "btcchina":
				apiKey = prop.getProperty("apiKey");
				apiSecret = prop.getProperty("apiSecret");
			    // Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

					public java.security.cert.X509Certificate[] getAcceptedIssuers() {

						return null;
					}

					@Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] chain,
							String authType) throws CertificateException {
						
					}
					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] chain,
							String authType) throws CertificateException {

					}

				} };

			    // Install the all-trusting trust manager
			    SSLContext sc = SSLContext.getInstance("SSL");
			    sc.init(null, trustAllCerts, new java.security.SecureRandom());
			    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			    // Create all-trusting host name verifier
			    HostnameVerifier allHostsValid = new HostnameVerifier() {

					@Override
					public boolean verify(String arg0, SSLSession arg1) {
						return true;
					}

			    };

			    // Install the all-trusting host verifier
			    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
				
				exSpec = new ExchangeSpecification(BTCChinaExchange.class);
				exSpec.setSecretKey(apiSecret);
				exSpec.setApiKey(apiKey);
				break;	
			}
			
			Exchange exchange = com.xeiam.xchange.ExchangeFactory.INSTANCE.createExchange(exSpec);
			logger.info("Initialized exchange: " + exchangeId);
			
			return exchange;			
		} catch (Exception e) {
			logger.info("Exchange could not be created. Please verify your API key and API secret");
			logger.info(e);
			return null;
		}
	}
	
	/**
	 * Get a list of available sites
	 * @return
	 */
	public HashMap<String, ExchangeSite> getAvailableSites() {
		return sites;
	}
}
