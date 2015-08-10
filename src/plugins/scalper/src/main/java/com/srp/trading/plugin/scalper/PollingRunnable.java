package com.srp.trading.plugin.scalper;

import java.io.File;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.srp.trading.common.Util;
import com.srp.trading.domain.DomainBase;
import com.srp.trading.domain.SessionWrapper;
import com.srp.trading.domain.Symbol;
import com.srp.trading.plugin.PluginConfig;

public class PollingRunnable implements Runnable {
	static final Logger logger = Logger.getLogger(PollingRunnable.class.getName());
	private final Settings settings;
	private PluginConfig config;

	public PollingRunnable(Settings settings, PluginConfig config) {
		this.settings = settings;
		this.config = config;
	}
	
	@Override
	public void run() {
		// Create session
		String path = new File(config.getDataPath(), settings.getExchangeSite() + ".sqlite").toString();
		Session session = null;		
		long threadId = Thread.currentThread().getId();
		try {
			session = DomainBase.createSession(path);
			SessionWrapper sw = new SessionWrapper(session);
			DomainBase.getActiveSessions().put(threadId, sw);
			
			while (true) {
				boolean createNewOperations = true;
				try {
					settings.updateTicker();					
				} catch (Exception e) {
					createNewOperations = false;
					String msg = "Error retrieving ticker.";
					Util.printOutput(logger, msg);
					logger.info(e);		
				}
				
				try {
					ScalpLogic logic = new ScalpLogic(settings);
					logic.execute(createNewOperations);
				} catch (Exception e) {
					String msg = "Error executing scalping logic.";
					Util.printOutput(logger, msg);
					logger.info(e);					
				} 
				
				try {
					// Wait for polling
					Thread.sleep(settings.getPollingDelay());
				} catch (InterruptedException e) {
					String msg = "Stopped scalping!";
					Util.printOutput(logger, msg);
					logger.info(e);
					break;
				}
			}
		} catch (Exception e) {
			String msg = "Error setting session.";
			Util.printOutput(logger, msg);
			logger.info(e);
		}		
		
		if (session != null) {
			session.close();
		}
		if (DomainBase.getActiveSessions().containsKey(threadId)) {
			DomainBase.getActiveSessions().remove(threadId);
		}
		return;
	}
}
