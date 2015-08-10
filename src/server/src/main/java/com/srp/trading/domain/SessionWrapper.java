/**
 * 
 */
package com.srp.trading.domain;

import org.hibernate.Session;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Wrapper for the hibernate session. Contains session related data.
 */
public class SessionWrapper {
	private Session session;
	private Integer symbolId;
	
	public SessionWrapper(Session s) {
		this.session = s;
	}
	
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public Integer getSymbolId() {
		return symbolId;
	}
	public void setSymbolId(Integer symbolId) {
		this.symbolId = symbolId;
	}	
}
