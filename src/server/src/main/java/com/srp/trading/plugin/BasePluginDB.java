/**
 * 
 */
package com.srp.trading.plugin;

import java.io.File;

import org.hibernate.Session;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.Version;
import com.srp.trading.domain.DomainBase;
import com.srp.trading.domain.SessionWrapper;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class BasePluginDB extends BasePlugin {
	protected String dbName;
	
	/**
	 * @param name
	 * @param version
	 * @param description
	 */
	public BasePluginDB(String name, Version version, String description) {
		super(name, version, description);
	}
	
	/***
	 * Set the session for the current thread before executing a command 
	 * @param cmd
	 * @return
	 */
	public BasicExecutionResult wrappedDBexecuteCommand(Command cmd) {
		BasicExecutionResult result = new BasicExecutionResult(false, "Undefined error.");
		Session session = null;
		long threadId = Thread.currentThread().getId();
		try {
			String path = new File(pluginConfig.getDataPath(), dbName + ".sqlite").toString();
			session = DomainBase.createSession(path);
			SessionWrapper sw = new SessionWrapper(session);
			DomainBase.getActiveSessions().put(threadId, sw);
			result = executeCommand(cmd);		
			DomainBase.getActiveSessions().remove(threadId);
			session.close();
		} catch (Exception e) {
			if (session != null) {
				session.close();
			}
			if (DomainBase.getActiveSessions().containsKey(threadId)) {
				DomainBase.getActiveSessions().remove(threadId);
			}
		}
		return result;
	}

	public String getDbName() {
		return dbName;
	}
}
