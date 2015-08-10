/**
 * 
 */
package com.srp.trading.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srp.finance.CandleFrequency;
import com.srp.finance.CandleStick;
import com.srp.finance.MarketData;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class BitcoinWisdomDataExtractor implements IMarketDataExtractor {
	private Logger logger = Logger.getLogger(BitcoinWisdomDataExtractor.class.getName());
	private String candlesUrl;
	private String tradesSinceUrl;
	private String tickersUrl;
	private Long lastTickerId = null;
	private CandleFrequency candleFreq;
	private final String fromStr = "%EPOCHSINCE%";
	private String exchangeAndSymbol;
	private DataExchange dataExchange;
	private ExchangeSymbol symbol;
	
	@Override
	public void Initialize(DataExchange exchange, CandleFrequency freq, ExchangeSymbol symbol) {
		this.candleFreq = freq;
		this.dataExchange = exchange;
		this.symbol = symbol;
		this.exchangeAndSymbol = exchange.toString() + symbol.toString();
		this.candlesUrl = "http://s1.bitcoinwisdom.com:8080/period?step=" + freq.value() + "&sid=f7e2f42d&symbol=" + exchangeAndSymbol; 
		this.tradesSinceUrl = "http://s1.bitcoinwisdom.com:8080/trades?since=" + fromStr + "&symbol=" + exchangeAndSymbol;
		this.tickersUrl = "http://s1.bitcoinwisdom.com:8080/ticker";
	}
	
	/* (non-Javadoc)
	 * @see com.srp.trading.plugin.IMarketDataExtractor#getCandles(java.lang.String)
	 */
	@Override
	public List<CandleStick> getCandles() {
		List<CandleStick> result = new ArrayList<CandleStick>();
				
		try {
			String jsonString = HttpUtil.sendGet(candlesUrl);
			lastTickerId = getTicketId();
			ArrayList candles = new ObjectMapper().readValue(jsonString, ArrayList.class);
			for (Object o : candles) {
				ArrayList c = (ArrayList) o;
				long epoch = Long.parseLong(c.get(0).toString());
				double open = Double.parseDouble(c.get(3).toString()); 
				double close = Double.parseDouble(c.get(4).toString());
				double high = Double.parseDouble(c.get(5).toString());
				double low = Double.parseDouble(c.get(6).toString());
				double volumen = Double.parseDouble(c.get(7).toString());
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(epoch * 1000);
				CandleStick cs = new CandleStick(time, open, close, high, low, volumen);
				result.add(cs);
			}
		} catch (Exception e) {			
			logger.info(e);
			result = null;
		}	
		
		result = CandleStick.compressCandles(result, candleFreq.value());
		
		return result;		
	}
	
	/**
	 * Get the candles since the last request
	 * @param since
	 * @return
	 */
	@Override
	public List<CandleStick> getRecentCandles() {
		if (lastTickerId == null)
			return getCandles();
		
		List<CandleStick> result = new ArrayList<CandleStick>();
		Long lastId = null;
		try {
			List<CandleStick> candles = new ArrayList<CandleStick>();
			String url = tradesSinceUrl.replaceFirst(fromStr, Long.toString(lastTickerId));
			String jsonString = HttpUtil.sendGet(url);
			ArrayList trades = new ObjectMapper().readValue(jsonString, ArrayList.class);			
			for (Object o : trades) {
				HashMap<String, Object> c = (HashMap<String,Object>) o;
				lastId = Long.parseLong(c.get("tid").toString());
				long epoch = Long.parseLong(c.get("date").toString());
				double price = Double.parseDouble(c.get("price").toString()); 
				double volumen = Double.parseDouble(c.get("amount").toString());
				Calendar time = Calendar.getInstance();
				time.setTimeInMillis(epoch * 1000);
				MarketData md = new MarketData(time, price, volumen);
				CandleStick cs = new CandleStick(md);
				candles.add(cs);
			}
									
			// Sort the candles by date
			Collections.sort(candles, CandleStick.ComparatorByTime());
			if (lastId != null) {
				lastTickerId = lastId;
			}
			result = candles;
		} catch (Exception e) {			
			logger.info(e);
			result = null;
		}	
		
		return result;
	}
	
	/**
	 * Get the latest tickers in the exchanges
	 * @return
	 */
	public HashMap getTickers() {
		HashMap result = new HashMap();
		try {
			String jsonString = HttpUtil.sendGet(tickersUrl);
			result = new ObjectMapper().readValue(jsonString, HashMap.class);			
		} catch (Exception e) {
			logger.info(e);
			result = null;
		}
		return result;
	}
	
	/**
	 * Get the current ticket id of the specified exchange and currency
	 * @param exchangeAndSymbol
	 * @return
	 */
	public Long getTicketId() {
		Long result = null;
		try {
			HashMap tickers = getTickers();
			HashMap exchangeTicker = (HashMap) tickers.get(exchangeAndSymbol);
			result = Long.parseLong(exchangeTicker.get("tid").toString());
		} catch (Exception e) {
			logger.info(e);
			result = null;
		}
		return result;
	}
	
	public CandleFrequency getCandleFreq() {
		return candleFreq;
	}

	/* (non-Javadoc)
	 * @see com.srp.trading.plugin.IMarketDataExtractor#getExchange()
	 */
	@Override
	public DataExchange getExchange() {
		return dataExchange;
	}

	/* (non-Javadoc)
	 * @see com.srp.trading.plugin.IMarketDataExtractor#getFrequency()
	 */
	@Override
	public CandleFrequency getFrequency() {
		return candleFreq;
	}

	/* (non-Javadoc)
	 * @see com.srp.trading.plugin.IMarketDataExtractor#getSymbol()
	 */
	@Override
	public ExchangeSymbol getSymbol() {
		return symbol;
	}	
}
