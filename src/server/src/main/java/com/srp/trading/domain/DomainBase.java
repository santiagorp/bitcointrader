package com.srp.trading.domain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class DomainBase {
	private static Logger logger = Logger.getLogger(DomainBase.class.getName());
	
	// List of sessions indexed by thread
	private static HashMap<Long, SessionWrapper> activeSessions = new HashMap<Long, SessionWrapper>();

	private static SessionFactory sessionFactory = null; 
    private static ServiceRegistry serviceRegistry = null;
    private static String baseConnectionName = "";
       
    public static SessionFactory getSessionFactory() {
    	if (sessionFactory == null) {
    		Configuration configuration = new Configuration();    		
            configuration.configure(); 
             
            Properties properties = configuration.getProperties();            
            String connString = properties.getProperty("connection.url");
            connString = connString.replace("$BASENAME$", baseConnectionName);
            properties.setProperty("hibernate.connection.url", connString);
             
            serviceRegistry = new ServiceRegistryBuilder().applySettings(properties).buildServiceRegistry();         
            sessionFactory = configuration.buildSessionFactory(serviceRegistry); 
             
            return sessionFactory;
    	}
    	return sessionFactory;
    }
    
    /**
     * Create connection for the specified path
     * @return
     */
    public static Connection createSqliteConnection(String dbPath) {
    	String connURL = "jdbc:sqlite:" + dbPath;
    	Connection conn = null;
        try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection(connURL);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
        return conn;
    }
    
    /**
     * Open a session for a spcified db path
     * @param dbPath
     * @return
     */
    public static Session createSession(String dbPath) {
    	Connection conn = createSqliteConnection(dbPath);
    	Session session = getSessionFactory().withOptions().connection(conn).openSession();
    	return session;
    }
    
    /**
     * Get the session for the current thread
     * @return
     */
    public static Session getSession() {
    	long threadId = Thread.currentThread().getId();
    	return activeSessions.get(threadId).getSession();
    }

    /**
     * Get the session wrapper for the current thread
     * @return
     */
    public static SessionWrapper getSessionWrapper() {
    	long threadId = Thread.currentThread().getId();
    	return activeSessions.get(threadId);
    }
    
	/**
	 * Save the specified operation in the db
	 * 
	 * @param op
	 */
	public boolean Save(Object o) {
		return Save(new Object[] {o});
	}
	
	/**
	 * Save all the objects
	 * @param data
	 * @return
	 */
	public boolean Save(Object[] data) {
		boolean result = false;
		Session session = null;
		Transaction tx = null;

		try {
			session = getSession();
			tx = session.beginTransaction();

			// Saving to the database
			for (Object o: data) {
				try {
					session.saveOrUpdate(o);
				} catch (NonUniqueObjectException nuoEx) {
					session.merge(o);
				}
			}

			// Committing the change in the database.
			session.flush();
			tx.commit();
			for (Object o: data) {
				session.refresh(o);
			}

			result = true;
		} catch (Exception ex) {
			logger.info(ex);
			logger.info("Rolling back transacion...");
			tx.rollback();
		}
		return result;

	}
	
	/**
	 * Add scopped parameters clauses to the query
	 * @param hql
	 * @return
	 */
	protected static Query createScoppedQuery(String hql, String prefix) {
		Integer scopedSymbolId  = getSessionWrapper().getSymbolId();
		Query q;

		if (scopedSymbolId != null) {
			String scopped = hql + " AND " + prefix + ".symbolId = :symbolId";
			q = getSession().createQuery(scopped);
			q.setParameter("symbolId", scopedSymbolId);
		} else {
			q = getSession().createQuery(hql);
		}
		
		return q;		
	}

	public String getBaseConnectionName() {
		return baseConnectionName;
	}

	public void setBaseConnectionName(String baseConnectionName) {
		this.baseConnectionName = baseConnectionName;
	}
	
	public static HashMap<Long, SessionWrapper> getActiveSessions() {
		return activeSessions;
	}
}
