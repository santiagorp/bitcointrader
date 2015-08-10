/**
 * 
 */
package com.srp.trading.client.ui;

import java.rmi.RemoteException;
import java.util.List;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public interface IDinamicMenu {
	List<MenuItem> getGeneratedItems() throws RemoteException;
}
