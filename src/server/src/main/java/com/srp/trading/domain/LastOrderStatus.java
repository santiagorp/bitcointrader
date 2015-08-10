/**
 * 
 */
package com.srp.trading.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
@Entity
@Table(name = "VWLASTORDERSTATUS")
public class LastOrderStatus extends DomainBase {
	@Id
	private Integer orderId;
	
	@ManyToOne
	@JoinColumn(name = "orderId", nullable = false, insertable=false, updatable=false)
	private Order order;
	
	private OrderStatus status;
	
	private Date time;
	
	private Integer symbolId;
			
	public LastOrderStatus() {
	}
	
	/**
	 * Count the number of orders with the specified status
	 * @param status
	 * @return
	 */
	public static Long count(OrderStatus status) {
		Session session = getSession();		
		String hql = "select count(*) from LastOrderStatus WHERE status = :status";
		Query q = session.createQuery(hql);
		q.setParameter("status",  status);
		Long result = (Long) q.uniqueResult();
		
		return result;
	}
	
	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getSymbolId() {
		return symbolId;
	}

	public void setSymbolId(Integer symbolId) {
		this.symbolId = symbolId;
	}	
}
