/**
 * 
 */
package com.srp.trading.core;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class Util {
	/**
	 * Get the rmi registry specified host:port string
	 * 
	 * @param route
	 * @return
	 * @throws RemoteException
	 */
	public static Registry getRegistry(String route) throws RemoteException {
		Registry registry;
		String host = null;
		Integer port = null;

		if (route != null) {
			String[] ss = route.split(":");
			host = ss[0];
			if (ss.length > 0) {
				port = Integer.decode(ss[1]);
			}
		}

		if (port != null) {
			registry = LocateRegistry.getRegistry(host, port);
		} else if (host != null) {
			registry = LocateRegistry.getRegistry(host);
		} else {
			registry = LocateRegistry.getRegistry();
		}

		return registry;
	}
}
