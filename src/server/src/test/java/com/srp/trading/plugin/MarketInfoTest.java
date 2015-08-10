/**
 * 
 */
package com.srp.trading.plugin;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.srp.finance.CandleFrequency;
import com.srp.finance.CandleStick;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class MarketInfoTest {

	/**
	 * Test method for {@link com.srp.trading.plugin.MarketInfo#getCandles()}.
	 */
	@Test
	public final void testGetCandles() {
		CandleFrequency freq = CandleFrequency.m30;
		IMarketDataExtractor de = new BitcoinWisdomDataExtractor();
		de.Initialize(DataExchange.BtcChina, freq, ExchangeSymbol.BTCCNY);
		List<CandleStick> candles = null;
		try {
			candles = de.getCandles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(candles != null && candles.size() > 0);
	}

	/**
	 * Test method for {@link com.srp.trading.plugin.MarketInfo#getRecentCandles()}.
	 */
	@Test
	public final void testGetRecentCandles() {
		//performTestGetRecentCandles();
	}

	/**
	 * Real test
	 */
	private void performTestGetRecentCandles() {
		CandleFrequency freq = CandleFrequency.m1;
		List<CandleStick> candles = null;
		List<Double> ema1;
		List<Double> ema2;
		List<Double> values;
		try {
			IMarketDataExtractor de = new BitcoinWisdomDataExtractor();
			de.Initialize(DataExchange.BTCe, freq, ExchangeSymbol.BTCUSD);
			int e1 = 10;
			int e2 = 30;
			candles = de.getRecentCandles();
			values = CandleStick.getValues(candles);
			ema1 = com.srp.finance.Utils.getEMA(values, 10);
			ema2 = com.srp.finance.Utils.getEMA(values, 30);
			String name = "recentCandles_1";
			CandleStick.generatePlot(name + ".plot", "BTC-e recent candles 1", candles, ema1, ema2, name + ".png");
			Thread.sleep(60000);
			List<CandleStick> candles2 = de.getRecentCandles();
			candles = CandleStick.append(candles, candles2, freq.value());
			values = CandleStick.getValues(candles);
			ema1 = com.srp.finance.Utils.getEMA(values, 10);
			ema2 = com.srp.finance.Utils.getEMA(values, 30);
			name = "recentCandles_2";
			CandleStick.generatePlot(name + ".plot", "BTC-e recent candles 2", candles, ema1, ema2, name + ".png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(candles != null && candles.size() > 0);
	}

	@Test
	public final void testGetTickers() {
		CandleFrequency freq = CandleFrequency.m30;
		BitcoinWisdomDataExtractor de = new BitcoinWisdomDataExtractor();
		de.Initialize(DataExchange.BtcChina, freq, ExchangeSymbol.BTCCNY);
		HashMap tickers = null;
		try {
			tickers = de.getTickers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(tickers != null && tickers.size() > 0);
	}

	/**
	 * Test method for {@link java.lang.Object#sendGet()}.
	 */
	@Test
	public final void testSendGet() {
		String url = "http://s1.bitcoinwisdom.com:8080/period?step=900&sid=f7e2f42d&symbol=btcchinabtccny";
		String response = null;
		try {
			response = HttpUtil.sendGet(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(response != null && !response.isEmpty());
	}
}
