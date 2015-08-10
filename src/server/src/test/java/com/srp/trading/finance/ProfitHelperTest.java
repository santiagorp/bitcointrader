package com.srp.trading.finance;
import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Test;

import com.srp.finance.AskBid;
import com.srp.finance.ProfitHelper;

/**
 * 
 */

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class ProfitHelperTest {

	/**
	 * Test method for {@link com.srp.finance.ProfitHelper#isProfitable(java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal)}.
	 */
	@Test
	public final void testIsProfitable() {
		BigDecimal ask = new BigDecimal("114.70");
		BigDecimal bid = new BigDecimal("113.81");
		BigDecimal fee = new BigDecimal("0.002");
		BigDecimal minProfit = new BigDecimal("0.002");
		boolean isProfitable = ProfitHelper.isProfitable(ask, bid, fee, minProfit);
		
		assertTrue("Operation is profitable!", isProfitable);
		
		bid = new BigDecimal("114.60");
		isProfitable = ProfitHelper.isProfitable(ask, bid, fee, minProfit);
		
		assertFalse("Operation not profitable!", isProfitable);
	}

	/**
	 * Test method for {@link com.srp.finance.ProfitHelper#getProfit(java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal)}.
	 */
	@Test
	public final void testGetProfit() {
		BigDecimal ask = new BigDecimal("114.70");
		BigDecimal bid = new BigDecimal("113.81");
		BigDecimal fee = new BigDecimal("0.002");
		
		BigDecimal profit = ProfitHelper.getProfit(ask, bid, fee);
		String valueStr = profit.setScale(8, RoundingMode.FLOOR).toPlainString();
		
		boolean ok = valueStr.equals("0.00379280");
		
		assertTrue("Profit calculation ok!", ok);
	}

	/**
	 * Test method for {@link com.srp.finance.ProfitHelper#getBtcToBuy(java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal)}.
	 */
	@Test
	public final void testGetBtcToBuy() {
		BigDecimal ask = new BigDecimal("114.70");
		BigDecimal bid = new BigDecimal("113.81");
		BigDecimal fee = new BigDecimal("0.002");
		BigDecimal scalpAmount = new BigDecimal("0.02");
		
		BigDecimal btcToBuy = ProfitHelper.getBtcToBuy(ask, bid, fee, scalpAmount);
		String valueStr = btcToBuy.setScale(8, RoundingMode.FLOOR).toPlainString();
		
		boolean ok = valueStr.equals("0.02011608");
		
		assertTrue("BTC amount calculation ok!", ok);

	}

	/**
	 * Test method for {@link com.srp.finance.ProfitHelper#getOrderRatioAskBid(java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal)}.
	 */
	@Test
	public final void testGetOrderRatioAskBid() {
		BigDecimal ask = new BigDecimal("116.47");
		BigDecimal bid = new BigDecimal("115.03");
		BigDecimal fee = new BigDecimal("0.002");
		BigDecimal ratio = new BigDecimal("0.5");
		
		AskBid ab = ProfitHelper.getOrderRatioAskBid(ask,  bid,  ratio,  fee);		
		String askStr = ab.getAsk().setScale(5, RoundingMode.FLOOR).toPlainString();
		String bidStr = ab.getBid().setScale(5, RoundingMode.FLOOR).toPlainString();
		
		boolean ok = askStr.equals("116.22638") && bidStr.equals("115.27361") ;
		
		assertTrue("Ratio calculations ok!", ok);
	}

}

