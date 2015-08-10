/**
 * 
 */
package com.srp.trading.plugin;

import java.util.Calendar;
import java.util.List;

import com.srp.finance.CandleFrequency;
import com.srp.finance.CandleStick;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public interface IMarketDataExtractor {

	/**
	 * Initialize the exchange data getter with the specified parameters
	 * 
	 * @param exchange
	 * @param freq
	 * @param symbol
	 */
	public abstract void Initialize(DataExchange exchange, CandleFrequency freq, ExchangeSymbol symbol);

	/**
	 * Get the candles from an exchange
	 * 
	 * @param jsonString
	 * @return
	 */
	public abstract List<CandleStick> getCandles();

	/**
	 * Get the latest candles since the previous invocation to getCandles or
	 * getRecentCandles
	 * 
	 * @return
	 */
	public abstract List<CandleStick> getRecentCandles();

	/**
	 * Retrieve the current exchange used for data retrieval
	 * @return
	 */
	public abstract DataExchange getExchange();

	/**
	 * Get the candle frequency parameter used in the initalization
	 * @return
	 */
	public abstract CandleFrequency getFrequency();

	/**
	 * Get the exchange symbol used in the initialization
	 * @return
	 */
	public abstract ExchangeSymbol getSymbol();
}