/**
 * 
 */
package com.srp.finance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class Utils {
	/***
	 * Return the exponential moving average for the spcified data and index
	 * @param data
	 * @param index
	 * @return
	 */
	public static List<Double> getEMA(List<Double> data, int n) {
		List<Double> s = new ArrayList<Double>();
		for (double d: data) {
			s.add(d);
		}
		
		List<Double> ema = new ArrayList<Double>();
		int j = 1;
		
	    // get n sma first and calculate the next n period ema
	    double sma = 0;
	    for (int i = 0; i < n; i++) {
	    	sma += s.get(i);
	    }
	    sma = sma / n;
	    
	    double multiplier = 2.0 / (1 + n);
	    ema.add(sma);
		
	    // EMA(current) = ( (Price(current) - EMA(prev) ) x Multiplier) + EMA(prev)
	    ema.add(((s.get(n) - sma) * multiplier) + sma);
	    
	    // now calculate the rest of the values
	    for (int i = n + 1; i < s.size(); i++) {
	    	double tmp = ((s.get(i) - ema.get(j)) * multiplier) + ema.get(j);	    
	        j++;
	        ema.add(tmp);
	    }
	    
	    List<Double> result = new ArrayList<Double>();
	    for (int i = 0; i < n - 1; i++) {
	    	result.add(data.get(0));
	    }
		
	    for (double x: ema) {
	    	result.add(x);
	    }
	    
	    return result;
	}
}
