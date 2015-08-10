/**
 * 
 */
package com.srp.trading.domain;

import com.xeiam.xchange.dto.Order.OrderType;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class EnumHelper {
	/**
	 * Return a string representation of the specified enum
	 * @param orderType
	 * @return
	 */
	public static String getString(OrderType orderType) {
		switch (orderType) {
		case ASK:
			return "ASK";
		case BID:
			return "BID";
		default:
			return null;
		}				
	}
	
	/**
	 * Return a string representation of the specified enum
	 * @param operationType
	 * @return
	 */
	public static String getString(OperationType operationType) {
		switch (operationType) {
		case Scalping:
			return "Scalping";
		case Unknown:
			return "Unknown";
		default:
			return null;
		}
	}
}
