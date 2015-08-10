/**
 * 
 */
package com.srp.finance;

import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CandleStick {	
	private static Logger logger = Logger.getLogger(CandleStick.class.getName());
	
	private Calendar calendar;
	private double open;
	private double close;
	private double high;
	private double low;
	private double volumen;

	public CandleStick(MarketData data) {
		this.calendar = data.getTime();
		open = data.getPrice();
		close = data.getPrice();
		high = data.getPrice();
		low = data.getPrice();
		this.volumen = data.getVolumen();
	}
	
	public CandleStick(Calendar time, double open, double close, double high, double low, double vol) {
		this.calendar = time;
		this.open = open;
		this.close = close;
		this.high = high;
		this.low = low;
		this.volumen = vol;
	}
	
	/**
	 * Clone current candle stick
	 */
	public CandleStick copy() {
        CandleStick c = new CandleStick(calendar, open, close, high, low, volumen);
        return c;
    }
	
	/*
	 * Join the supplied candle into the current one
	 */
	public void sum(CandleStick c) {
		if (c.getCalendar().getTime().before(calendar.getTime())) {
			open = c.getOpen();
			calendar = c.getCalendar();
		} else {
			close = c.getClose();
		}
		
		high = Math.max(high, c.getHigh());
		low = Math.min(low, c.getLow());
		volumen = volumen + c.getVolumen();
	}
	
	/**
	 * Get the calendar in linux epoch calendar
	 * @return
	 */
	public long getEpoch() {
		return calendar.getTimeInMillis() / 1000;
	}
	
	/**
	 * Comparator by ctime
	 */
	public static Comparator<CandleStick> ComparatorByTime() {
		Comparator<CandleStick> comparator = new Comparator<CandleStick>() {
			@Override
			public int compare(CandleStick c1, CandleStick c2) {
				long diff = c1.getEpoch() - c2.getEpoch();
				return (int) diff;
			}
		};
		return comparator;
	}
	
	/**
	 * Create a list of candles from the specified market data and interval
	 * @param data Market data
	 * @param interval In seconds
	 * @return
	 */
	public static List<CandleStick> getCandles(List<MarketData> data, int interval) {
		List<CandleStick> candles = new ArrayList<CandleStick>();
		for (MarketData md: data) {
			CandleStick c = new CandleStick(md);
			candles.add(c);
		}
		
		return compressCandles(candles, interval);
	}
	
	/**
	 * Append the additional candles to the supplied ones. The period will be the same than the original ones.
	 * @param candles The original candles. The period will be calculated from here.
	 * @param additional Data to append
	 * @return A list of candles resulting of appending and compressing the additional ones, If the frequency cannot be calculated, throws and exception.
	 * @throws Exception 
	 */
	public static List<CandleStick> append(List<CandleStick> candles, List<CandleStick> additional, int candleFreq) {
		// Sort additional candles
		Collections.sort(additional,ComparatorByTime());
		
		// Calculate freq
		int freq = candleFreq;
		if (candles.size() > 1) {
			freq = (int) (candles.get(1).getEpoch() - candles.get(0).getEpoch());
		}
		
		List<CandleStick> tmp = new ArrayList<CandleStick>();
		
		for (CandleStick c: candles) {
			tmp.add(c.copy());
		}

		int n = candles.size();
		CandleStick last = candles.get(n - 1);
		for (CandleStick c: additional) {
			if (c.getEpoch() > last.getEpoch())
				tmp.add(c.copy());
		}
		
		List<CandleStick> result = compressCandles(tmp, freq);
		return result;
	}
	
	/**
	 * Compress the candles into a list of the specified interval
	 * @param candles
	 * @param interval In seconds
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public static List<CandleStick> compressCandles(List<CandleStick> candles, int interval) {
		List<CandleStick> result = new ArrayList<CandleStick>();
		CandleStick prev = candles.get(0).copy();
		prev.setVolumen(0);
		Calendar t = prev.getCalendar();
		Calendar nt = (Calendar) t.clone();
		nt.add(Calendar.SECOND, interval);
		CandleStick currentCandle = null;
		
		int i = 0;
		while (i < candles.size()) {
			currentCandle  = candles.get(i).copy();
			
			if (!currentCandle.getCalendar().getTime().before(t.getTime()) && currentCandle.getCalendar().getTime().before(nt.getTime())) {
				// 1. If is in range, add/replace prev
				if (prev.getVolumen() == 0) {					
					prev = currentCandle;
					prev.setCalendar(t);
				} else {
					prev.sum(currentCandle);
				}
				i++;
			} else {
				// 2. Add previous to result
				result.add(prev);

				// 3. If not in range, update range with dummy data
				t = (Calendar) nt.clone();
				nt.add(Calendar.SECOND,  interval);
				MarketData dummyData = new MarketData(t, prev.getClose(), 0);
				prev = new CandleStick(dummyData);				
			}
		}
		
		// Add the current candle to the total
		result.add(prev);
		
		return result;
	}
	
	
	/**
	 * Return the minimum of all the candles
	 * @param candles
	 * @return
	 */
	public static double getMin(List<CandleStick> candles) {
		double min = candles.get(0).getLow();
		for (CandleStick c: candles) {
			min = Math.min(min, c.getLow());
		}
		return min;
	}
	
	/**
	 * Return the maximum of all the candles
	 * @param candles
	 * @return
	 */
	public static double getMax(List<CandleStick> candles) {
		double max = candles.get(0).getHigh();
		for (CandleStick c: candles) {
			max = Math.max(max, c.getHigh());
		}
		return max;
	}
	
	/**
	 * Return a list of crosspoints between emas
	 * @param candles
	 * @param ema1
	 * @param ema2
	 * @return
	 */
	public static List<MarketData> findCrosses(List<CandleStick> candles, List<Double> ema1, List<Double> ema2) {
		List<MarketData> result = new ArrayList<MarketData>();
		boolean prev = ema1.get(0) - ema2.get(0) > 0;
		for (int i = 0; i < candles.size(); i++) {
			boolean current = ema1.get(i) - ema2.get(i) > 0;
			if (current != prev) {
				Long t1 = candles.get(i -1).getEpoch();
				Long t2 = candles.get(i).getEpoch();
				Point2D l1 = com.srp.geometry.Util.getSlopeIntercept(t1, ema1.get(i - 1), t2, ema1.get(i));
				Point2D l2 = com.srp.geometry.Util.getSlopeIntercept(t1, ema2.get(i - 1), t2, ema2.get(i));
				Point2D cross = com.srp.geometry.Util.getIntersection(l1, l2);
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis((long) cross.getX() * 1000);
				MarketData md = new MarketData(c, cross.getY(), 0);
				result.add(md);
			}
		}
		return result;
	}
	
	
	/**
	 * Return a list of values representing the close point of each candle 
	 * @param candles
	 * @return List of close points from the candle sticks
	 */
	public static List<Double> getValues(List<CandleStick> candles) {
		List<Double> result = new ArrayList<Double>();
		for (CandleStick c: candles) {			
			result.add(c.getClose());
		}
		return result;
	}
	
	/**
	 * Generate a gnu plot file with the specified title from the candles and ema supplied. Saved in the specified filename.
	 * @param filename Full path to the gnuplot to generate
	 * @param title Chart title
	 * @param candles CandleStick list (market data)
	 * @param ema1 Exponential Moving average to be plot (1st)
	 * @param ema2 Exponential Moving average to be plot (2nd)
	 */
	public static void generatePlot(String filename, String title, List<CandleStick> candles, List<Double> ema1, List<Double> ema2, String imageName) {
		double min = getMin(candles);
		double max = getMax(candles);
		double margin = (max - min) / 10.0;
		min = min - margin;
		max = max + margin;
		int n = candles.size();
		long dt = candles.get(n - 1).getEpoch() - candles.get(0).getEpoch();
		double width = dt / (n * 2) * 1.25;
		String format = "yyyy-MM-dd'T'HH:mm:ss";
		String gnuPlotDateFormat = "%Y-%m-%dT%H:%M:%S";
		SimpleDateFormat sdf =  new SimpleDateFormat(format); 
		
		Calendar start = Calendar.getInstance();
		start.setTimeInMillis((candles.get(0).getEpoch() - (dt / n)) * 1000);
		Calendar end = Calendar.getInstance();
		end.setTimeInMillis((candles.get(n -1).getEpoch() + (dt / n)) * 1000);
		
		
		
		try {
			PrintWriter f = new PrintWriter(filename, "UTF-8");
			
			f.println("set title '" + title + "' font 'arial, 32'");
		    f.println("set timefmt \"" + gnuPlotDateFormat + "\"");
		    f.println("set boxwidth " + width + "");
		    f.println("set xdata time");
		    if (!imageName.isEmpty()) {
		        f.println("set terminal pngcairo size 2560,1440");
		        f.println("set output '" + imageName + "'");
		    }

		    f.println("set grid ytics lc rgb '#cccccc' lw 1 lt 1");
		    f.println("unset key");
		    
		    f.println("set xrange['" + sdf.format(start.getTime()) + "':'" + sdf.format(end.getTime()) + "']");
		    f.println("set yrange[" + min + ':' + max + "]");
		    f.println("set y2range[" + min + ':' + max + "]");
		    f.println("set y2tics");
		    f.println("set style line 1 lw 1 lc rgb '#079900'");
		    f.println("set style line 2 lw 1 lc rgb '#ff0000'");
		    f.println("set style line 3 lw 1 lc rgb '#4275a9'");
		    f.println("set style line 4 lw 1 lc rgb '#0055bb'");
		    f.println("set style line 5 lw 1 lc rgb '#FF8000'");
		    f.println("#Date\tOpen\tLow\tHigh\tClose");
		    f.println("plot \t'-' using 1:2:3:4:5 with candlesticks ls 1, \\");
		    f.println("\t'-' using 1:2:3:4:5 with candlesticks ls 2, \\");
		    f.println("\t'-' using 1:2 with lines ls 4,\\");
		    f.println("\t'-' using 1:2 with lines ls 5");
			
		    // White candles (green)
		    for (int i = 0; i < n; i++) {
		        CandleStick c = candles.get(i);
		        if (c.isDown())
		            continue;
		        
		        f.println(sdf.format(c.getCalendar().getTime()) + "\t" + c.getOpen() + "\t" + c.getLow() + "\t" + c.getHigh() + "\t" + c.getClose());		            
		    }
		    f.println("e");
		    		   
		    // White candles (red)
		    for (int i = 0; i < n; i++) {
		        CandleStick c = candles.get(i);
		        if (c.isUp())
		            continue;
		        
		        f.println(sdf.format(c.getCalendar().getTime()) + "\t" + c.getOpen() + "\t" + c.getLow() + "\t" + c.getHigh() + "\t" + c.getClose());		            
		    }
		    f.println("e");

		    // EMA 1
		    for (int i = 0; i < n; i++) {
		        CandleStick c = candles.get(i);
		        double e1 = ema1.get(i);		        
		        f.println(sdf.format(c.getCalendar().getTime()) + "\t" + e1);		            
		    }
		    f.println("e");
		    
		    // EMA 2
		    for (int i = 0; i < n; i++) {
		        CandleStick c = candles.get(i);
		        double e2 = ema2.get(i);		        
		        f.println(sdf.format(c.getCalendar().getTime()) + "\t" + e2);		            
		    }
		    f.println("e");
		    
			f.close();
		} catch (Exception e) {
			logger.info(e);
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		long t = calendar.getTimeInMillis() / 1000;
		String s = String.format("T:%d O:%.8f H:%.8f L:%.8f C:%.8f V:%.4f", new Object[] {t, open, high, low, close, volumen} );
		return s;
	}
	
	/**
	 * Indicates if the current candlestick is a white candle (price increases)
	 * @return
	 */
	public boolean isUp() {
		return open < close;
	}
	
	/**
	 * Indicates if the current candlestick is a black candle (price decreases)
	 * @return
	 */
	public boolean isDown() {
		return open > close;
	}
	
	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar cal) {
		this.calendar = cal;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getVolumen() {
		return volumen;
	}

	public void setVolumen(double volumen) {
		this.volumen = volumen;
	}

}
