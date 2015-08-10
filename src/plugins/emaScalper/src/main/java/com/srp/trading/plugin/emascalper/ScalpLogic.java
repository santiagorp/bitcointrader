/**
 * 
 */
package com.srp.trading.plugin.emascalper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.money.BigMoney;

import com.srp.finance.AskBid;
import com.srp.finance.ProfitHelper;
import com.srp.finance.Trend;
import com.srp.trading.common.Util;
import com.srp.trading.domain.EnumHelper;
import com.srp.trading.domain.Operation;
import com.srp.trading.domain.OperationType;
import com.srp.trading.domain.Order;
import com.srp.trading.domain.OrderStatus;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.PollingTradeService;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class ScalpLogic {
	private static Logger logger = Logger.getLogger(ScalpLogic.class.getName());

	private Settings settings;
	private Ticker lastTicker;
	private PollingTradeService tradeService;
	private Trend currentTrend = Trend.Undefined;

	public ScalpLogic(Settings settings) {
		this.settings = settings;
		this.lastTicker = settings.getLastTicker();
		this.tradeService = settings.getExchange().getPollingTradeService();
	}

	public void execute(boolean createNewOperations) {
		try {
			// PRE: We need to have valid market data before doing anything
			if (!settings.getMarketData().hasValidData())
				return;

			Trend oldTrend = currentTrend;
			currentTrend = settings.getMarketData().calculateTrendEMA1Slope();
			boolean trendChanged = currentTrend != Trend.Undefined && oldTrend != Trend.Undefined && currentTrend != oldTrend;

			// A. Place orders with errors before creating new ones
			logger.debug("Place orders with errors before creating new ones");
			List<Order> unknownSellOrders = Order.find(OrderStatus.Unknown, OrderType.ASK);
			List<Order> unknownBuyOrders = Order.find(OrderStatus.Unknown, OrderType.BID);
			for (Order o : unknownSellOrders) {
				if (canPlaceOrder()) {
					switch (currentTrend) {
					case Up:
						if (o.getOperation().getBid().getLatestStatus() == OrderStatus.Finished) {
							placeOrder(o);
						}
						break;
					case Down:
						if (o.getOperation().getBid().getLatestStatus() == OrderStatus.Unknown) {
							placeOrder(o);
						}
						break;
					default:
						break;
					}
				}
			}

			for (Order o : unknownBuyOrders) {
				if (canPlaceOrder()) {
					switch (currentTrend) {
					case Down:
						if (o.getOperation().getAsk().getLatestStatus() == OrderStatus.Finished) {
							placeOrder(o);
						}
						break;
					case Up:
						if (o.getOperation().getAsk().getLatestStatus() == OrderStatus.Unknown) {
							placeOrder(o);
						}
						break;
					default:
						break;
					}
				}
			}

			// B. Create new operations if all the conditions are satisfied:
			// - there is room for it
			// - the last ticker is profitable and valid
			// - the two trends are aligned
			boolean trendsAligned = settings.getMarketData().calculateTrendEMA1Slope() == settings.getMarketData().calculateTrendEMACrosses();
			if (createNewOperations && trendsAligned) {
				logger.debug("Create new operations if there is room for it and the last ticker is profitable and valid");
				boolean isProfitable = isProfitable(lastTicker, settings);
				logger.info("Trend: " + currentTrend.toString());
				if (canPlaceOrder() && isProfitable) {
					List<Operation> pendingOps = Operation.find(OrderStatus.Pending);
					if (pendingOps.size() < settings.getMaxPendingOperations()) {
						createOperation();
					}
				}
			}

			// C. Update orders status in DB
			logger.debug("Update orders status in DB");
			if (canGetOpenOrders()) {
				List<Order> openOrders = getStockOpenOrders();
				if (openOrders != null) {
					closePendingOrders(openOrders);
				}
			}

			// D. Cancel pending operations if they are too far from current ticker
			List<Operation> operations = Operation.find(OrderStatus.Pending);
			for (Operation o : operations) {
				if (currentTrend == Trend.Up) {
					if (o.getBid().getLatestStatus() == OrderStatus.Pending && o.getAsk().getLatestStatus() != OrderStatus.Finished) {
						BigDecimal limit = o.getBid().getPrice().multiply(settings.getCancelOrdersPerc());
						BigDecimal tickerBid = lastTicker.getBid().getAmount();
						if (limit.compareTo(tickerBid) <= 0) {
							// Cancel order and operation
							if (cancelOrder(o.getBid(), false)) {
								cancelOrder(o.getAsk(), true);
							}
						}
					}
				} else if (currentTrend == Trend.Down) {
					if (o.getAsk().getLatestStatus() == OrderStatus.Pending && o.getBid().getLatestStatus() != OrderStatus.Finished) {
						BigDecimal limit = o.getAsk().getPrice().divide(settings.getCancelOrdersPerc(), RoundingMode.FLOOR);
						BigDecimal tickerAsk = lastTicker.getAsk().getAmount();
						if (limit.compareTo(tickerAsk) >= 0) {
							// Cancel order and operation
							if (cancelOrder(o.getAsk(), false)) {
								cancelOrder(o.getBid(), true);
							}
						}
					}
				}
			}

		} catch (Exception ex) {
			String msg = "Error processing operations";			
			Util.printOutput(logger, msg);
			ex.printStackTrace();
			logger.info(ex);
		}
	}

	/**
	 * Create a new operation Save the operation into the DB and place the order in the market
	 */
	private void createOperation() {
		BigDecimal askAmount = settings.getScalpAmount();
		BigDecimal orderRatio = settings.getProfitOrderRatio();
		BigDecimal targetProfit = settings.getTargetProfit();
		BigDecimal possibleProfit = ProfitHelper.getProfit(lastTicker.getAsk().getAmount(), lastTicker.getBid().getAmount(), settings.getFee());

		// when forcing operations to minimum profit, adjust ratio
		if (settings.getForceMinProfit() && (possibleProfit.compareTo(targetProfit) > 0)) {
			orderRatio = targetProfit.divide(possibleProfit, RoundingMode.HALF_EVEN);
		}

		AskBid ab = ProfitHelper.getOrderRatioAskBid(lastTicker.getAsk().getAmount(), lastTicker.getBid().getAmount(), orderRatio, settings.getFee());
		BigDecimal bidAmount = ProfitHelper.getBtcToBuy(ab.getAsk(), ab.getBid(), settings.getFee(), settings.getScalpAmount());

		Operation op = new Operation(OperationType.Scalping, ab.getAsk(), askAmount, ab.getBid(), bidAmount);

		if (op.SaveAll()) {
			BigDecimal profit = op.getProfit(settings.getFee());
			// Print operation information
			String profitPercentage = profit.multiply(new BigDecimal(100)).setScale(3, RoundingMode.FLOOR).toPlainString();
			System.out.println();
			String msg = String.format("New %s operation created. Id: %d, target profit: %s %%",
					new Object[] { EnumHelper.getString(op.getOperationType()), op.getOperationId(), profitPercentage });
			Util.printOutput(logger, msg);

			// Place the order in the stock
			switch (currentTrend) {
			case Up:
				placeOrder(op.getBid());
				break;
			case Down:
				placeOrder(op.getAsk());
				break;
			default:
				// Do not place any order otherwise
				break;
			}
		} else {
			String msg = "Error saving new operation in DB. Orders not placed";
			Util.printOutput(logger, msg);
		}
	}

	/**
	 * Update the status of the open orders in the DB to finished if they are not in the provided openOrders.
	 * 
	 * @param openOrders
	 */
	private void closePendingOrders(List<Order> openOrders) {
		List<Order> pendingOrdersDB = Order.find(OrderStatus.Pending);
		for (Order o : pendingOrdersDB) {
			if (!o.existsByReferenceIn(openOrders)) {
				o.saveLatestStatus(OrderStatus.Finished);

				// Print/log all order information
				String msg = String.format("%s order finished. Ref: %s", new Object[] { EnumHelper.getString(o.getOrderType()), o.getRef() });
				Util.printOutput(logger, msg);

				Operation op = o.getOperation();
				if (op.hasFinished()) {
					BigDecimal profit = op.getProfit(settings.getFee());
					String profitPercentage = profit.multiply(new BigDecimal(100)).setScale(3, RoundingMode.FLOOR).toPlainString();
					System.out.println();
					msg = String.format("Operation %d finished. Profit: %s%%", new Object[] { op.getOperationId(), profitPercentage });
					Util.printOutput(logger, msg);
				}
			}
		}
	}

	/**
	 * Place the specified order in the market and set its status to pending
	 * 
	 * @param order
	 */
	private void placeOrder(Order order) {
		try {
			BigDecimal amount = order.getAmount().setScale(settings.getNumDecimalsAmount(), RoundingMode.FLOOR);
			BigDecimal price = order.getPrice().setScale(settings.getNumDecimalsPrice(), RoundingMode.FLOOR);
			BigMoney priceMoney = BigMoney.of(settings.getCurrencyUnit(), price);

			LimitOrder lo = new LimitOrder(order.getOrderType(), amount, settings.getTradableIdentifier(), settings.getTransactionCurrency(), priceMoney);
			String ref = tradeService.placeLimitOrder(lo);
			
			if (ref == null || ref.isEmpty()) {
				throw new Exception("Service returned empty reference placing limit order.");
			}

			String msg = String.format(
					"Placed new %s order [orderId: %d, operationId: %d]. Price: %s %s, Amount: %s, Reference: %s",
					new Object[] { order.getOrderType(), order.getOrderId(), order.getOperation().getOperationId(), price.toPlainString(),
							settings.getTransactionCurrency(), amount.toPlainString(), ref });
			Util.printOutput(logger, msg);

			order.setRef(ref);
			order.Save();

			// Update time of last operation
			Date now = new Date();
			settings.setTimeOfLastOrder(now);

			// Update to pending
			order.saveLatestStatus(OrderStatus.Pending);
		} catch (Exception e) {
			String msg = String.format("Error placing order id %s, operation id: %d",
					new Object[] { order.getOrderId(), order.getOperation().getOperationId() });
			Util.printOutput(logger, msg);
			logger.info(e);
		}
	}

	/**
	 * Cancel an order. If it has a reference, cancel it from the server. Otherwise just change the status to cancelled in the DB.
	 * 
	 * @param o
	 *            The order to cancel
	 * @param force
	 *            Force the cancellation in the DB (even if the exchange returned false)
	 * @return True if the order could be cancelled or it was already cancelled or finished. Otherwise false.
	 */
	private boolean cancelOrder(Order o, boolean forceDBCancel) {
		OrderStatus latestStatus = o.getLatestStatus();
		String msg;
		boolean result = false;
		OrderStatus statusToSave = OrderStatus.Cancelled;

		if (latestStatus == OrderStatus.Finished || latestStatus == OrderStatus.Cancelled) {
			return true;
		}

		try {
			if (o.getRef() != null && !o.getRef().isEmpty()) {
				result = tradeService.cancelOrder(o.getRef());
				String preMsg = result ? "Cancelled " : "Could not cancel ";
				msg = String.format("%s %s order id %d from operation id: %d", new Object[] { preMsg, EnumHelper.getString(o.getOrderType()), o.getOrderId(),
						o.getOperation().getOperationId() });
				Util.printOutput(logger, msg);
				// TODO: Review the messages and print that the order could not be canceled because it was finished if required *see down*
				// If the order could not be cancelled, check if the reason is that it does not exist in the server anymore
				if (!result) {			
					List<Order> openOrders = getStockOpenOrders();
					boolean found = false;
					for (Order eo: openOrders) {
						if (eo.getRef().equals(o.getRef())) {
							found = true;
							break;
						}
					}
					if (!found) {
						statusToSave = OrderStatus.Finished;
					}
				}
			}			
		} catch (Exception e) {
			msg = String.format("Error cancelling %s order id %d from operation id: %d", new Object[] { EnumHelper.getString(o.getOrderType()), o.getOrderId(),
					o.getOperation().getOperationId() });
			Util.printOutput(logger, msg);
			logger.info(e);
		}
		
		if (result || forceDBCancel) {
			o.saveLatestStatus(statusToSave);
		}

		return result;
	}

	/**
	 * Get the orders in the DB which are still open in the server
	 * 
	 * @return The matching open orders in the DB or null if error retrieving them from the service
	 */
	private List<Order> getStockOpenOrders() {
		List<Order> result = null;
		try {
			OpenOrders tradeOpenOrders = tradeService.getOpenOrders();
			List<LimitOrder> limitOrders = tradeOpenOrders.getOpenOrders();
			result = new ArrayList<Order>();
			for (LimitOrder lo : limitOrders) {
				List<Order> orders = Order.find(lo.getId());
				if (orders == null)
					continue;
				for (Order o : orders) {
					result.add(o);
				}
			}
		} catch (Exception e) {
			String msg = "Error processing open orders from service";
			logger.info(msg);
			logger.error(e);
		}

		Date now = new Date();
		settings.setLastOpenOrdersRetrieval(now);
		return result;
	}

	/**
	 * Returns true if we can place an order due to the timeout since the last one
	 * 
	 * @return
	 */
	private boolean canPlaceOrder() {
		Calendar now = Calendar.getInstance();
		Calendar delayTill = Calendar.getInstance();
		Calendar lastOrderTime = Calendar.getInstance();

		lastOrderTime.setTime(settings.getTimeOfLastOrder());
		delayTill.setTime(settings.getTimeOfLastOrder());
		delayTill.add(Calendar.SECOND, settings.getOrderDelay());

		boolean timeoutExpired = now.after(delayTill);
		return timeoutExpired;
	}

	/**
	 * Returns true if we can ask for open orders to the service
	 * 
	 * @return
	 */
	private boolean canGetOpenOrders() {
		Calendar now = Calendar.getInstance();
		Calendar delayTill = Calendar.getInstance();
		Calendar lastRetrievalOrderTime = Calendar.getInstance();

		lastRetrievalOrderTime.setTime(settings.getLastOpenOrdersRetrieval());
		delayTill.setTime(settings.getLastOpenOrdersRetrieval());
		delayTill.add(Calendar.SECOND, settings.getRetrieveOpenOrdersDelay());

		boolean timeoutExpired = now.after(delayTill);
		return timeoutExpired;
	}

	/**
	 * Returns true if the scalping is profitable with the current ticket
	 * 
	 * @param ticker
	 * @return
	 */
	public static boolean isProfitable(Ticker ticker, Settings settings) {
		BigDecimal profit = getProfit(ticker, settings);

		boolean result = profit.compareTo(settings.getTargetProfit()) >= 0;
		String profitPercentage = profit.multiply(new BigDecimal(100)).setScale(3, RoundingMode.FLOOR).toPlainString();
		String msg = String.format("Analyze: Buy: %3$s %1$s, Ask: %2$s %1$s, Profit: %4$s%%, Profitable: %5$b",
				new Object[] { settings.getTransactionCurrency(), ticker.getAsk().getAmount().toPlainString(), ticker.getBid().getAmount().toPlainString(),
						profitPercentage, result });
		logger.info(msg);

		return result;
	}

	/**
	 * Return the profit for the current settings and the specified ticker
	 * 
	 * @param ticker
	 * @return
	 */
	public static BigDecimal getProfit(Ticker ticker, Settings settings) {
		BigDecimal profit = ProfitHelper.getProfit(ticker.getAsk().getAmount(), ticker.getBid().getAmount(), settings.getFee());
		return profit;
	}
}
