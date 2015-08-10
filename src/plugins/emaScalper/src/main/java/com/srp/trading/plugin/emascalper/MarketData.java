/**
 * 
 */
package com.srp.trading.plugin.emascalper;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.srp.finance.CandleStick;
import com.srp.finance.Trend;
import com.srp.trading.common.Util;
import com.srp.trading.plugin.BitcoinWisdomDataExtractor;
import com.srp.trading.plugin.ExchangeSymbol;
import com.srp.trading.plugin.IMarketDataExtractor;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class MarketData implements Runnable {
	static final Logger logger = Logger.getLogger(MarketData.class.getName());
	
	private IMarketDataExtractor dataGetter;
	private List<Double> ema1 = new ArrayList<Double>();
	private List<Double> ema2 = new ArrayList<Double>();
	private int emaIndex1;
	private int emaIndex2;
	private List<CandleStick> candles;
	private long updateDelay;
	private ExchangeSymbol symbol;
	
	public MarketData(IMarketDataExtractor dataGetter, int emaIndex1, int emaIndex2) {
		this.dataGetter = dataGetter;
		this.emaIndex1 = emaIndex1;
		this.emaIndex2 = emaIndex2;
		this.symbol = dataGetter.getSymbol();
		updateDelay = dataGetter.getFrequency().value() / 10;
	}

	/**
	 * Initialize with candles
	 */
	public void initialize() {		
		while (candles == null) {
			candles = dataGetter.getCandles();
		}
		calculateEmas();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		initialize();
		
		while (true) {
			try {
				Thread.sleep(updateDelay * 1000);
				refreshFromService();				
			} catch (InterruptedException e) {
				String msg = "Stopped market data extractor!";
				Util.printOutput(logger, msg);
				logger.info(e);
				break;
			} catch (Exception e) {
				logger.info(e);
			}
		}		
	}	
	
	/**
	 * Calculate EMAs
	 */
	public void calculateEmas() {
		List<Double> values = CandleStick.getValues(candles);
		ema1 = com.srp.finance.Utils.getEMA(values, emaIndex1);
		ema2 = com.srp.finance.Utils.getEMA(values, emaIndex2);
	}
	
	/**
	 * Refresh with new candles and recalculate emas
	 */
	private void refreshFromService() {
		int maxSize = candles.size();
		logger.debug("Refreshing candles from market data extractor");		
		List<CandleStick> recent = dataGetter.getRecentCandles();
		if (recent == null) {
			logger.debug("Error retrieving candles from market data extractor");
		}
		List<CandleStick> total = CandleStick.append(candles, recent, dataGetter.getFrequency().value());

		// Delete extra candles
		while (total.size() > maxSize) {
			total.remove(0);
		}
		
		candles = total;
		
		logger.debug("Calculating EMAs");
		calculateEmas();
	}
	
	/**
	 * Retrieve the current trend from the last ema values
	 * @return The current trend
	 */
	public Trend calculateTrendEMACrosses() {
		Trend result = Trend.Undefined;
		double lastEma1 = ema1.get(ema1.size() - 1);
		double lastEma2 = ema2.get(ema2.size() - 1);		
		
		// ema2 is delayed respect ema1, so:
		// - when is ascending ema2 is lower than ema1
		// - when is descending ema2 is above ema1 
		double diff = lastEma1 - lastEma2; 
		if (diff > 0) {
			result = Trend.Up;
		} else if (diff < 0) {
			result = Trend.Down;
		}
		return result;
	}
	
	
	/*
	 * Calculate the trend based on the ema1 slope
	 */
	public Trend calculateTrendEMA1Slope() {
		Trend result = Trend.Undefined;
		int i = candles.size();
		double t0 = candles.get(i - 2).getEpoch();
		double x1 = 0;
		double y1 = ema1.get(i - 2);
		double x2 = (candles.get(i - 1).getEpoch() - t0) / (double) dataGetter.getFrequency().value();
		double y2 = ema1.get(i - 1);
		
		double slope = com.srp.geometry.Util.getSlopeIntercept(x1, y1, x2, y2).getX();
		
		String msg = String.format("P1(%.2f, %.5f), P2(%.2f, %.5f). Slope: %.8f", new Object[] {x1, y1, x2, y2, slope });
		logger.debug(msg);

		if (slope >= 0) {
			result = Trend.Up;
		} else {
			result = Trend.Down;
		}
		return result;
	}
	
	/**
	 * Returns true if there are candles in the list
	 * @return
	 */
	public boolean hasValidData() {
		return this.candles != null && this.candles.size() > 0;
	}
	
	public List<Double> getEma1() {
		return ema1;
	}
	public List<Double> getEma2() {
		return ema2;
	}
	public List<CandleStick> getCandles() {
		return candles;
	}

	public int getEmaIndex1() {
		return emaIndex1;
	}

	public int getEmaIndex2() {
		return emaIndex2;
	}

	public ExchangeSymbol getSymbol() {
		return symbol;
	}
}
