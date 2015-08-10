/**
 * 
 */
package com.srp.finance;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
/**
 * Supported candle frequency
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public enum CandleFrequency {
    m1 (60),
    m3 (180),
    m5 (300),
    m15 (900),
    m30 (1800),
    h1 (3600),
    h2 (7200),
    h4 (14400),
    h6 (21600),
    h12 (43200),
    d1 (86400),
    d3 (259200),
    w1 (604800);

    private final int seconds;       

    private CandleFrequency(int seconds) {
        this.seconds = seconds;
    }

    public int value(){
       return seconds;
    }
    
    /**
     * Create the enumeration from a string
     * @param str
     * @return
     */
    public static CandleFrequency fromString(String str) {
    	CandleFrequency freq = null;
    	switch (str.toLowerCase()) {
    	case "m1":
			freq = CandleFrequency.m1;
			break;
    	case "m3":
			freq = CandleFrequency.m3;
			break;
		case "m5":
			freq = CandleFrequency.m5;
			break;
		case "m15":
			freq = CandleFrequency.m15;
			break;
		case "m30":
			freq = CandleFrequency.m30;
			break;
		case "h1":
			freq = CandleFrequency.h1;
			break;
		case "h2":
			freq = CandleFrequency.h2;
			break;
		case "h4":
			freq = CandleFrequency.h4;
			break;
		case "h6":
			freq = CandleFrequency.h6;
			break;
		case "h12":
			freq = CandleFrequency.h12;
			break;
		case "d1":
			freq = CandleFrequency.d1;
			break;
		case "d3":
			freq = CandleFrequency.d3;
			break;
		case "w1":
			freq = CandleFrequency.w1;
			break;
		default:
			freq = null;
			break;
    	}
    	return freq;
    }
}