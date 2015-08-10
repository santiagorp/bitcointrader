/**
 * 
 */
package com.srp.finance;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class ProfitHelper {
	/***
	 * Returns if a combined bid/ask operation is profitable for the specified fees and desired profit
	 * @param ask The ask price
	 * @param bid The bid price
	 * @param fee The fees charged in the bid and ask
	 * @param minProfit Minimum desired profit
	 * @return
	 */
	public static boolean isProfitable(BigDecimal ask, BigDecimal bid, BigDecimal fee, BigDecimal minProfit) {
		BigDecimal profit = getProfit(ask, bid, fee);		
		boolean result = profit.compareTo(minProfit) >= 0;		
		return result;
	}
	
	/**
	 * Get the profit of an operation from its ask/bid and fees values
	 * @param ask The ask price
	 * @param bid The bid price
	 * @param fee The fee
	 * 
	 *  
	 * P = (A/B) * (1-F)^2 - 1
	 * 
	 * Where:
	 * 
	 * A = Ask 
	 * F = Fees
	 * B = Bid
	 * 
	 * @return
	 */
	public static BigDecimal getProfit(BigDecimal ask, BigDecimal bid, BigDecimal fee) {
		BigDecimal one = new BigDecimal(1);
        BigDecimal oneMinusFee = one.subtract(fee);
        BigDecimal omfPow = oneMinusFee.multiply(oneMinusFee);
        BigDecimal profit = ask.divide(bid, 8, RoundingMode.FLOOR).multiply(omfPow).subtract(one);
		return profit;
	}
	
	/**
	 * Get the amount of BTC that needs to be bought after selling the scalpped amount:
	 * 
	 * x = A*N*(1-F) / B
	 * 
	 * Where:
	 *  
	 * A = Ask
	 * N = Scalp amount
	 * F = Transaction fees
	 * B = Bid
	 * 
	 * @return
	 */
	public static BigDecimal getBtcToBuy(BigDecimal ask, BigDecimal bid, BigDecimal fee, BigDecimal scalpAmount) {
		BigDecimal one = new BigDecimal(1);
        BigDecimal oneMinusFee = one.subtract(fee);        
        BigDecimal btcToBuy = ask.multiply(scalpAmount).multiply(oneMinusFee).divide(bid, 8, RoundingMode.FLOOR);
        
        return btcToBuy;
	}
	
	
	/**
	 * Calculate the final ask and bid from a original ask/bid and fees. This returned ask will have a profit scaled to the ratio parameter.
	 * 
	 * Formulas:
	 * (A + B)/ 2 = (a + b) / 2
	 * P * r = (a / b) * (1 - F)^2 - 1
	 * 
	 * After resolving:
	 * a = A + B - b
	 * b = (A + B) / ((( P * r + 1) / (1 - F)^2)) + 1) 
	 * 
	 * Where:
	 * 
	 * A = Original ask price (usually from the ticker)
	 * B = Original bid price (usually from the ticker)
	 * F = Transaction fees
	 * P = Profit for the A, B and F parameters
	 * a = the ask price to calculate
	 * b = the bid price to calculate
	 * r = The ratio to multiply the current profit
	 * 
	 * @return The ask price to calculate (a)
	 */
	public static AskBid getOrderRatioAskBid(BigDecimal Ask, BigDecimal Bid, BigDecimal ratio, BigDecimal fee) {
		BigDecimal one = new BigDecimal(1);
		BigDecimal oneMinusFee = one.subtract(fee);
		BigDecimal omfPow = oneMinusFee.multiply(oneMinusFee);		
		BigDecimal P = getProfit(Ask,  Bid,  fee);
		BigDecimal b = Ask.add(Bid).divide(P.multiply(ratio).add(one).divide(omfPow, 8, RoundingMode.FLOOR).add(one), 8, RoundingMode.FLOOR);
		BigDecimal a = Ask.add(Bid).subtract(b);		
		AskBid result = new AskBid(a, b);
		return result;
	}
	
}
