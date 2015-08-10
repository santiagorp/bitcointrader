/**
 * 
 */
package com.srp.trading.server;

import java.io.File;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.srp.trading.core.IServerAPI;
import com.srp.trading.core.Util;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class TradingServer {
	// Attributes
	private static ApplicationContext applicationContext;
	private static Logger logger = Logger.getLogger(TradingServer.class.getName());
	private int serverPort = 0;

	public static void main(String[] args) {
		String msg = "Starting server...";			
		logger.info(msg);
		System.out.println(msg);
					
		System.setProperty("logfile.pattern", "tradeConsole-%d{yyyyMMdd}.log.gz");
		applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/applicationContext.xml");
		
		TradingServer ts = (TradingServer) applicationContext.getBean("TradingServer");
		ts.start(args);
	}
	
	public void start(String[] args) {
		try {
			InitializeDependencies();
			
			Server server = (Server) applicationContext.getBean("server");
			IServerAPI stub = (IServerAPI) UnicastRemoteObject.exportObject(server, serverPort);			
			String route = args.length > 0 ? args[0] : null;
            Registry registry = Util.getRegistry(route);
            registry.rebind("btcTradingServer", stub);
            
            String msg = "Server ready!";
            System.out.println(msg);
            logger.info(msg);
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
			logger.info(e);
		}
	}
	

	/**
	 * Initialize dependencies in the container
	 */
	private static void InitializeDependencies() {
		logger.info("Initializing exchange factory...");
		ExchangeFactory exFact = (ExchangeFactory) applicationContext.getBean("exchangeFactory");
		exFact.Initialize();
		
		logger.info("Initializing plugin factory...");
		String pluginsPath = new File(System.getProperty("user.dir"),  "plugins").toString(); 
		PluginFactory plugFact = (PluginFactory) applicationContext.getBean("pluginFactory");
		plugFact.Initialize(pluginsPath);
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
}
