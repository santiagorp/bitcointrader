/**
 * 
 */
package com.srp.finance;

import java.util.Calendar;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class MarketData {
	private Calendar time;
	private double price;
	private double volumen;
	
	public MarketData(Calendar time, double price, double vol) {
		this.time = time;
		this.price = price;
		this.volumen = vol;
	}
	
	/**
	 * Create a copy of the current market data object
	 * @return
	 */
	public MarketData copy() {
		MarketData result = new MarketData(time, price, volumen);
		return result;
	}
	
	public Calendar getTime() {
		return time;
	}
	public void setTime(Calendar time) {
		this.time = time;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getVolumen() {
		return volumen;
	}
	public void setVolumen(double volumen) {
		this.volumen = volumen;
	}
}
