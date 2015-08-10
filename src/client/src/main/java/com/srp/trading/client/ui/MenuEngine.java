package com.srp.trading.client.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Deal with the menus and actions
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class MenuEngine {
	private MenuItem menu;
	private MenuItem current;
	
	/**
	 * Create a menu engine 
	 * @param menu
	 */
	public MenuEngine(MenuItem menu) {
		this.menu = menu;
		this.current = this.menu;
	}
	
	public void execute() {
		boolean finished = false;
		
		while (!finished) {
			renderCurrent();
			int index = getValidOption();
			if (index == 0) {
				if (current.getParent() == null) {
					finished = true;
					continue;
				} else {
					current = current.getParent();
					continue;
				}
			} else {
				MenuItem item = current.getSubMenu().get(index - 1);
				if (item.getMenuItemType() == MenuItemType.Action) {
					try {
						item.getAction().execute();
						boolean goToParent = item.getAction().isBackToParent();						
						if (goToParent) {
							current = item.getParent().getParent();
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					continue;
				} else {
					current = item;
					continue;
				}
			}
		}
		
		System.out.println("Finished OK!");
	}	
	
	/**
	 * Render the current menu
	 */
	private void renderCurrent() {
		if (current.getMenuItemType() == MenuItemType.Action)
			return;
		
		if (current.isDinamicSubmenu()) {
			List<MenuItem> items;
			try {
				items = current.getDinamicMenuGeneration().getGeneratedItems();
			} catch (RemoteException e) {
				items = new ArrayList<MenuItem>();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (MenuItem item : items) {
				item.setParent(current);
				current.getSubMenu().clear();
				current.getSubMenu().addAll(items);
			}
		}
		
		int i = 1;
		
		// Print header
		System.out.println(current.getHeader());

		// All entries
		for (MenuItem item : current.getSubMenu()) {
			renderItem(item.getName(), i++);
		}

		// Go back/exit entry
		String text;
		if (current.getParent() == null) {
			text = "Exit";
		} else {
			text = "Go back";
		}		
		renderItem(text, 0);
		
		System.out.println();
	}
	
	private void renderItem(String name, int id) {
		String text = String.format("[%d]\t%s", new Object[] {id, name });
		System.out.println(text);
	}
	
	/**
	 * Get a valid menu option
	 */
	private int getValidOption() {		
		while (true) {
			try{
				System.out.print("Enter option: ");
			    BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			    String s = bufferRead.readLine();			    
			    int option = Integer.parseInt(s);
			    if (option >= 0 && option <= current.getSubMenu().size()) {
					System.out.println();
			    	return option;
			    }		    			    
			} catch(Exception e) {				
			}			
			System.out.println("Option not valid. Try again.");
		}
	}
}
