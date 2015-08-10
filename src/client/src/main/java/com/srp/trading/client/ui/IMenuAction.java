package com.srp.trading.client.ui;

import java.rmi.RemoteException;

public interface IMenuAction {
	/**
	 * Action to be executed when the entry is selected
	 * @throws RemoteException
	 */
	void execute() throws RemoteException;
	
	/**
	 * If true, indicates that after executing the action the next rendered item should be the parent
	 * @return
	 */
	boolean isBackToParent();
	
	/**
	 * Get menuitem where the action belongs
	 * @return
	 */
	MenuItem getMenuItem();
	
	/**
	 * Set the menuitem where the action belongs
	 * @param item
	 */
	void setMenuItem(MenuItem item);
}
