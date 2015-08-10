/**
 * 
 */
package com.srp.trading.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
@Entity
@Table(name = "TBLORDERHISTORY")
public class OrderHistory extends DomainBase {	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer orderHistoryId;
		
	@ManyToOne
	@JoinColumn(name = "orderId", nullable = false)
	private Order order;
	
	private OrderStatus status;
		
	private Date time;

	public OrderHistory() {
		this.time = new Date();
	}
	
	public OrderHistory(OrderStatus status) {
		this.status = status;
		this.time = new Date();
	}
	
	/**
	 * Save the specified current order history in the db
	 * @param op
	 */
	public boolean Save() {
		return Save(this);
	}

	public Integer getOrderHistoryId() {
		return orderHistoryId;
	}
	public void setOrderHistoryId(Integer ordernHistoryId) {
		this.orderHistoryId = ordernHistoryId;
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

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
		this.order.getHistory().add(this);
	}
}
