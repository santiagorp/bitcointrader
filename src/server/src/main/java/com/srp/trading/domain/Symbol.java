/**
 * 
 */
package com.srp.trading.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
@Entity
@Table(name = "TBLSYMBOL")
public class Symbol extends DomainBase {
	private static Logger logger = Logger.getLogger(Symbol.class.getName());
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int symbolId;

	private String name;

	/**
	 * Save the current symbol in the db
	 * 
	 * @param op
	 */
	public boolean Save() {
		logger.debug("Saving symbol");
		return Save(this);
	}
	
	/**
	 * Load a symbol by its id
	 * 
	 * @param id
	 * @return
	 */
	public static Symbol find(Integer id) {
		logger.debug("Invoked find symbol by id");
		if (id == null)
			return null;

		Session session = getSession();
		Symbol o = (Symbol) session.get(Symbol.class, id);

		return o;
	}
	
	/***
	 * Load a symbol by its symbol name
	 * 
	 * @param symbolName
	 * @return
	 */
	public static Symbol find(String symbolName) {
		logger.debug("Invoked find symbol by name");
		if (symbolName == null)
			return null;

		Session session = getSession();	
		Query q = session.createQuery("from Symbol s WHERE s.name = :name");
		q.setParameter("name", symbolName);
		List<Symbol> ss = (List<Symbol>) q.list();
		if (ss == null || ss.size() == 0) {
			return null;
		} else {
			return ss.get(0);
		}
	}
	
	public int getSymbolId() {
		return symbolId;
	}

	public void setSymbolId(int symbolId) {
		this.symbolId = symbolId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
