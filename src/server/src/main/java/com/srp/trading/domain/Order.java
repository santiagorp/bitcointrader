/**
 * 
 */
package com.srp.trading.domain;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;

import com.xeiam.xchange.dto.Order.OrderType;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
@Entity
@Table(name = "TBLORDER")
public class Order extends DomainBase {
	private static Logger logger = Logger.getLogger(Order.class.getName());
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int orderId;

	private String ref;

	private OrderType orderType;

	@Column(name = "price")
	private String priceStr;

	@Transient
	private BigDecimal price;

	@Column(name = "amount")
	private String amountStr;

	@Transient
	private BigDecimal amount;

	@OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
	private Set<OrderHistory> history = new HashSet<OrderHistory>();
	
	private Integer symbolId;
	
	@JoinColumn(name="bidId")
	@OneToOne(mappedBy = "bid")
	private Operation bidOperation;

	@JoinColumn(name="askId")
	@OneToOne(mappedBy = "ask")
	private Operation askOperation;

	public Order() {
	}

	public Order(OrderType ot, BigDecimal price, BigDecimal amount, String ref) {
		logger.debug("Created new order entity");
		this.orderType = ot;
		this.setPrice(price);
		this.setAmount(amount);
		this.ref = ref;
	}

	/**
	 * Save the current order in the db
	 * 
	 * @param op
	 */
	public boolean Save() {
		logger.debug("Saving order");
		Integer scopedSymbolId  = getSessionWrapper().getSymbolId();
		if (this.symbolId == null) {
			this.symbolId = scopedSymbolId;
		}
		return Save(this);
	}

	/**
	 * Retrieve all the orders which last status is the specified one
	 * 
	 * @param status
	 *            The desired status to find orders
	 * @return The list of orders with the specified status (bid and/or asks)
	 */
	public static List<Order> find(OrderStatus status) {		
		return find(status, null);
	}

	/**
	 * Retrieve all the orders which last status is the specified one
	 * 
	 * @param status
	 *            The status of the orders to be retrieved
	 * @param oerderType
	 *            The desired order type to be retrieved
	 * @return The list of orders with the specified status and type (ask or
	 *         bid)
	 */
	public static List<Order> find(OrderStatus status, OrderType orderType) {
		logger.debug("Invoked find order by status and ordertype");		
		
		Session session = getSession();		
		
		String hql = " SELECT o.orderId as orderId, o.ref as ref, o.priceStr as priceStr, o.amountStr as amountStr, o.orderType as orderType, oh.time, oh.status, o.symbolId "
				+ " FROM Order o, LastOrderStatus oh " + " WHERE o.orderId = oh.orderId AND oh.status = :status ";
		
		if (orderType != null) {
			hql = hql + " AND o.orderType = :orderType";
		}
		
		Query q = createScoppedQuery(hql, "o");
		 
		q.setParameter("status", status);
		
		if (orderType != null) {
			q.setParameter("orderType", orderType);
		}
		
		q.setResultTransformer(new OrderByStatusResultTransformer(false));
		List<Order> result = q.list();
		
		return result;
	}

	/**
	 * Load an order by its id
	 * 
	 * @param id
	 * @return
	 */
	public static Order find(Integer id) {
		logger.debug("Invoked find order by id");
		if (id == null)
			return null;

		Session session = getSession();
		Order o = (Order) session.get(Order.class, id);

		return o;
	}

	/***
	 * Load an order by its order reference
	 * 
	 * @param orderReference
	 * @return
	 */
	public static List<Order> find(String orderReference) {
		logger.debug("Invoked find order by reference");
		if (orderReference == null)
			return null;

		Session session = getSession();
		
		String hql = "from Order o WHERE o.ref = :reference ";
		
		Query q = createScoppedQuery(hql, "o");
		
		q.setParameter("reference", orderReference);
		
		List<Order> result = (List<Order>) q.list();

		return result;
	}

	/**
	 * Get the latest operation status
	 * 
	 * @return
	 */
	public OrderStatus getLatestStatus() {
		logger.debug("Invoked getlateststatus");
		if (history.size() == 0) {
			return OrderStatus.Unknown;
		}
		Iterator<OrderHistory> it = history.iterator();
		OrderHistory latest = it.next();
		while (it.hasNext()) {
			OrderHistory oh = it.next();
			if (oh.getTime().getTime() > latest.getTime().getTime()) {
				latest = oh;
			}
		}

		return latest.getStatus();
	}

	/**
	 * Add new operation status to the history
	 * 
	 * @param status
	 * @return
	 */
	public boolean saveLatestStatus(OrderStatus status) {
		logger.debug("Invoked save latest status");
		OrderHistory oh = new OrderHistory(status);
		oh.setOrder(this);
		if (oh.Save()) {
			return true;
		} else {
			history.remove(oh);
		}

		return false;
	}

	/**
	 * Returns true if the order exists in the specified collection Comparision
	 * done by order ref
	 * 
	 * @param col
	 *            The collection to check against
	 * @return True if the reference was found in the collection
	 */
	public boolean existsByReferenceIn(Collection<Order> col) {
		for (Order o : col) {
			if (o.getRef().equals(this.ref) && o.getOrderType() == this.orderType)
				return true;
		}
		return false;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public String getPriceStr() {
		return priceStr;
	}

	public void setPriceStr(String priceStr) {
		this.priceStr = priceStr;
		this.price = new BigDecimal(priceStr);
	}

	public BigDecimal getPrice() {
		if (price == null && priceStr != null) {
			price = new BigDecimal(priceStr);
		}
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
		this.priceStr = price.toPlainString();
	}

	public String getAmountStr() {
		return amountStr;
	}

	public void setAmountStr(String amountStr) {
		this.amountStr = amountStr;
		this.amount = new BigDecimal(amountStr);
	}

	public BigDecimal getAmount() {
		if (amount == null && amountStr != null) {
			amount = new BigDecimal(amountStr);
		}
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
		this.amountStr = amount.toPlainString();
	}

	public Set<OrderHistory> getHistory() {
		return history;
	}

	public Operation getOperation() {
		if (bidOperation != null && askOperation == null) {
			return bidOperation;
		}

		if (askOperation != null && bidOperation == null) {
			return askOperation;
		}

		// Try to retrieve from DB if we have the id
		Operation op = Operation.findByOrderId(orderId);
		if (op != null && op.getBidId() == orderId) {
			bidOperation = op;
			op.setBid(this);
			return op;
		}

		if (op != null && op.getAskId() == orderId) {
			askOperation = op;
			op.setAsk(this);
			return op;
		}

		return null;
	}

	public Integer getSymbolId() {
		return symbolId;
	}

	public void setSymbolId(Integer symbolId) {
		this.symbolId = symbolId;
	}
}

/**
 * Transformer for the order status find function
 * 
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
class OrderByStatusResultTransformer implements ResultTransformer {

	private static final long serialVersionUID = 1L;
	private boolean reverseOrder = false;

	public OrderByStatusResultTransformer(boolean reverseOrder) {
		this.reverseOrder = reverseOrder;
	}

	/**
	 * Create an object out of each row data. In this scenario we have created a
	 * company short info object from row data.
	 */
	@Override
	public Object transformTuple(Object[] rowData, String[] aliasNames) {
		Order o = new Order();
		for (int index = 0; index < aliasNames.length; index++) {
			switch (aliasNames[index]) {
			case "orderId":
				o.setOrderId((Integer) rowData[index]);
				break;
			case "priceStr":
				o.setPriceStr((String) rowData[index]);
				break;
			case "amountStr":
				o.setAmountStr((String) rowData[index]);
				break;
			case "ref":
				o.setRef((String) rowData[index]);
				break;
			case "orderType":
				o.setOrderType((OrderType) rowData[index]);
				break;
			case "symbolId":
				o.setSymbolId((Integer) rowData[index]);
				break;
			default:
				break;
			}
		}
		return o;
	}

	/**
	 * Final result list transformation.
	 */
	@Override
	public List transformList(List paramList) {

		if (reverseOrder) {
			Collections.reverse(paramList);
		}

		return paramList;
	}
}
