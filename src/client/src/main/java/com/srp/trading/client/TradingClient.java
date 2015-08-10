/**
 * 
 */
package com.srp.trading.client;

import java.util.List;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import com.srp.trading.client.ui.MenuEngine;
import com.srp.trading.client.ui.MenuItem;
import com.srp.trading.core.IServerAPI;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.Util;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class TradingClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
        try {
        	String route = args.length > 0 ? args[0] : null;			        
            Registry registry = Util.getRegistry(route);
            IServerAPI sa = (IServerAPI) registry.lookup("btcTradingServer");
            
            MenuHelper menuHelper = new MenuHelper(sa);
            MenuItem menu = menuHelper.getMainMenu();
            MenuEngine engine = new MenuEngine(menu);
            engine.execute();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }


	}

}
