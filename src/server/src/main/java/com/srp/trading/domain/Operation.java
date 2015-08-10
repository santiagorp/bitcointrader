/**
 * 
 */
package com.srp.trading.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;

import com.srp.finance.ProfitHelper;
import com.xeiam.xchange.dto.Order.OrderType;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
@Entity
@Table(name = "TBLOPERATION")
public class Operation extends DomainBase {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer operationId;
	
	@Column(name="askId",updatable = false, insertable = false)
	private int askId;
	
	@OneToOne
	@JoinColumn(name = "askId", nullable = false)
	private Order ask;

	
	@Column(name="bidId", updatable = false, insertable = false)
	private int bidId;
	
	@OneToOne
	@JoinColumn(name = "bidId", nullable = false)
	private Order bid;
	
	private OperationType operationType;

	private Integer symbolId;
	
	public Operation() {
	}

	public Operation(OperationType opType, BigDecimal askPrice, BigDecimal askAmount, BigDecimal bidPrice, BigDecimal bidAmount) {
		this.ask = new Order(OrderType.ASK, askPrice, askAmount, null);
		this.bid = new Order(OrderType.BID, bidPrice, bidAmount, null);		
		this.operationType = opType;
		OrderHistory askStatus = new OrderHistory(OrderStatus.Unknown);
		OrderHistory bidStatus = new OrderHistory(OrderStatus.Unknown);
		askStatus.setOrder(ask);
		bidStatus.setOrder(bid);		
	}
	/**
	 * Save the specified operation in the db
	 * 
	 * @param op
	 */
	public boolean Save() {
		Integer scopedSymbolId  = getSessionWrapper().getSymbolId();
		if (this.symbolId == null) {
			this.symbolId = scopedSymbolId;
		}
		return Save(this);
	}
	
	/**
	 * Save the current operation, the bid and the ask objects to DB.
	 * Useful on creation of new objects.
	 * @return
	 */
	public boolean SaveAll() {
		OrderHistory bidStatus = bid.getHistory().iterator().next();
		OrderHistory askStatus = ask.getHistory().iterator().next();
		Integer scopedSymbolId  = getSessionWrapper().getSymbolId();
		if (this.symbolId == null) {
			symbolId = scopedSymbolId;
			ask.setSymbolId(scopedSymbolId);
			bid.setSymbolId(scopedSymbolId);
		}
		Object[] data = new Object[] { bid, ask, bidStatus, askStatus, this };
		return Save(data);
	}
		
	/**
	 * Load an operation by its id
	 * 
	 * @param id
	 * @return
	 */
	public static Operation find(Integer id) {
		if (id == null)
			return null;

		Session session = getSession();	
		Operation op = (Operation) session.get(Operation.class, id);

		return op;
	}
	
	/**
	 * Retrieve all the operations having bid and/or ask in the specified status
	 * 
	 * @param status
	 * @return
	 */
	public static List<Operation> find(OrderStatus status) {
		Session session = getSession();	
		
		String hql = "select distinct op from Operation op, LastOrderStatus los " +
				   " where ((op.bidId = los.orderId and los.status = :status) " +
				   " or (op.askId = los.orderId and los.status = :status)) ";

		
		
		Query q = createScoppedQuery(hql, "op");
		q.setParameter("status", status);
		List<Operation> result = q.list();

		return result;
	}
	
	/**
	 * Retrieve all the operations having bid and/or ask in the specified statuses
	 * 
	 * @param statuses Array with the desired statuses to check
	 * @return
	 */
	public static List<Operation> find(OrderStatus statuses[]) {
		if (statuses.length == 0) {
			return new ArrayList<Operation>();
		}
		
		Session session = getSession();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct op from Operation op, LastOrderStatus los ");
		sb.append(" where ((op.bidId = los.orderId and (");
		for (int i=0; i<statuses.length; i++) {
			String param = ":status" + i;
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(" los.status = ").append(param);			
		}
		sb.append("))");
		
		sb.append(" or (op.askId = los.orderId and (");
		for (int i=0; i<statuses.length; i++) {
			String param = ":status" + i;
			if (i > 0) {
				sb.append(" or ");
			}
			sb.append(" los.status = ").append(param);			
		}
		sb.append(")))");
				
		String hql = sb.toString();
			
		Query q = createScoppedQuery(hql, "op");
		for (int i = 0; i < statuses.length; i++) {
			q.setParameter("status" + i, statuses[i]);
		}
		
		List<Operation> result = q.list();

		return result;
	}
	
	/**
	 * Retrieve the operation containing a bid or ask with the specified orderId
	 * @param orderId
	 * @return
	 */
	public static Operation findByOrderId(int orderId) {
		Session session = getSession();	
		String hql = " from Operation op where (op.bidId = :orderId or op.askId = :orderId) ";
		Query q = createScoppedQuery(hql, "op");
		q.setParameter("orderId", orderId);
		List<Operation> list = q.list();		
		Operation result = list != null && list.size() > 0 ? list.get(0) : null;		
	
		return result;
	}
	
	/**
	 * Return the profit of the current operation for the specified fees
	 * @param fee
	 * @return
	 */
	public BigDecimal getProfit(BigDecimal fee) {
		BigDecimal profit = ProfitHelper.getProfit(getAsk().getPrice(), getBid().getPrice(), fee);
		return profit;
	}
	
	/*
	 * Returns true if the bid and ask orders are in finished or cancelled status
	 */
	public boolean hasFinished() {
		OrderStatus bidStatus = bid.getLatestStatus();
		OrderStatus askStatus = ask.getLatestStatus();
		boolean bidFinished = bidStatus == OrderStatus.Finished || bidStatus == OrderStatus.Cancelled;
		boolean askFinished = askStatus == OrderStatus.Finished || askStatus == OrderStatus.Cancelled;
				
		return bidFinished && askFinished;
	}
	
	public Integer getOperationId() {
		return operationId;
	}

	public void setOperationId(Integer operationId) {
		this.operationId = operationId;
	}

	public Order getAsk() {
		if (ask == null) {
			// First try to get it in the session
			Session session = getSession();
			ask = (Order) session.get(Order.class, new Long(askId));
			
			if (ask == null) {
				ask = Order.find(askId);
			}
		}
		return ask;
	}

	public void setAsk(Order ask) {
		this.ask = ask;
	}

	public Order getBid() {
		if (bid == null) {
			// First try to get it in the session
			Session session = getSession();
			bid = (Order) session.get(Order.class, new Long(bidId));
			
			bid = Order.find(bidId);
		}
		return bid;
	}

	public void setBid(Order bid) {
		this.bid = bid;
	}

	public OperationType getOperationType() {
		return operationType;
	}

	public void setOpType(OperationType operationType) {
		this.operationType = operationType;
	}
	public int getBidId() {
		return bidId;
	}

	public void setBidId(int bidId) {
		this.bidId = bidId;
	}

	public int getAskId() {
		return askId;
	}

	public void setAskId(int askId) {
		this.askId = askId;
	}

	public Integer getSymbolId() {
		return symbolId;
	}

	public void setSymbolId(Integer symbolId) {
		this.symbolId = symbolId;
	}
}
