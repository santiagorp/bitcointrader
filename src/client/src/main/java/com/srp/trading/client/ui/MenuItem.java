package com.srp.trading.client.ui;

import java.util.ArrayList;
import java.util.List;


public class MenuItem {
	private String name;
	private MenuItemType menuItemType;
	private IMenuAction action;
	private IDinamicMenu dinamicMenuGeneration;
	public IDinamicMenu getDinamicMenuGeneration() {
		return dinamicMenuGeneration;
	}

	private List<MenuItem> subMenu;
	private String header;
	private MenuItem parent;
	private Object tag;
	private boolean isDinamicSubmenu = false;
	
	/**
	 * Create a final menu item
	 * @param name
	 * @param action
	 */
	public MenuItem(String name, IMenuAction action) {
		this.name = name;
		this.action = action;
		action.setMenuItem(this);
		menuItemType = MenuItemType.Action;		
	}
	
	/**
	 * Create a submenu
	 * @param name
	 * @param subMenu
	 */
	public MenuItem(String name, String header, List<MenuItem> subMenu) {
		this.name = name;
		this.subMenu = subMenu;
		menuItemType = MenuItemType.SubMenu;
		this.header = header;
		for (MenuItem item : subMenu) {
			item.setParent(this);
		}
	}
	
	/**
	 * Create a dinamic submenu. Its items are generated on renderTime.
	 * @param name
	 * @param header
	 * @param dinamicMenu
	 */
	public MenuItem(String name, String header, IDinamicMenu dinamicMenu) {
		this.name = name;
		this.header = header;
		this.subMenu = new ArrayList<MenuItem>();
		this.dinamicMenuGeneration = dinamicMenu;
		this.isDinamicSubmenu = true;
	}
	
	/**
	 * Replace subitems
	 * @param items
	 */
	public void SetMenuItems(List<MenuItem> items) {
		subMenu.clear();
		subMenu.addAll(items);
		for (MenuItem item : items) {
			item.setParent(this);
		}
	}

	public String getName() {
		return name;
	}

	public MenuItemType getMenuItemType() {
		return menuItemType;
	}

	public IMenuAction getAction() {
		return action;
	}

	public List<MenuItem> getSubMenu() {
		return subMenu;
	}

	public MenuItem getParent() {
		return parent;
	}

	public void setParent(MenuItem parent) {
		this.parent = parent;
	}

	public String getHeader() {
		return header;
	}
	
	public void setHeader(String header) {
		this.header = header;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

	public boolean isDinamicSubmenu() {
		return isDinamicSubmenu;
	}
}
