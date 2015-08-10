/**
 * 
 */
package com.srp.trading.finance;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.srp.finance.CandleFrequency;
import com.srp.finance.CandleStick;
import com.srp.finance.Utils;
import com.srp.trading.plugin.BitcoinWisdomDataExtractor;
import com.srp.trading.plugin.DataExchange;
import com.srp.trading.plugin.ExchangeSymbol;
import com.srp.trading.plugin.IMarketDataExtractor;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CandleStickTest {

	/**
	 * Test method for {@link com.srp.finance.CandleStick#generatePlot(java.lang.String, java.lang.String, java.util.List, java.util.List, java.util.List, java.lang.String)}.
	 */
	@Test
	public final void testGeneratePlot() {
		try {
			// Retrieve candles from server
			CandleFrequency freq = CandleFrequency.m1;
			IMarketDataExtractor de = new BitcoinWisdomDataExtractor();
			DataExchange ex = DataExchange.BTCe; 
			de.Initialize(ex, freq, ExchangeSymbol.LTCBTC);
			List<CandleStick> candles = de.getCandles();
						
			// Generate emas
			List<Double> data = CandleStick.getValues(candles);
			List<Double> ema1 = Utils.getEMA(data, 7);
			List<Double> ema2 = Utils.getEMA(data, 30);
			
			// Generate plot
			String currentDir = System.getProperty("user.dir");
			String filePath = new File(currentDir, "chart.plot").toString();
			String title = ex.name() + " " + freq.value() + "secs EMA(7,30)";
			CandleStick.generatePlot(filePath, title, candles, ema1, ema2, "chart.png");			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
