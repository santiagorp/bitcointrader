/**
 * 
 */
package com.srp.finance;

import java.math.BigDecimal;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class AskBid {	
	private BigDecimal ask;
	private BigDecimal bid;
	
	public AskBid(BigDecimal ask, BigDecimal bid) {
		this.ask = ask;
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}
}

