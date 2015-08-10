package com.srp.trading.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.srp.trading.client.ui.IDinamicMenu;
import com.srp.trading.client.ui.IMenuAction;
import com.srp.trading.client.ui.MenuActionBase;
import com.srp.trading.client.ui.MenuItem;
import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.IServerAPI;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.PluginExecInfo;

public class MenuHelper {
	private static Logger logger = Logger.getLogger(MenuHelper.class.getName());
	IServerAPI server = null;
	
	/**
	 * Creates a new menu helper instance
	 * @param s
	 */
	public MenuHelper(IServerAPI s) {
		server = s;
	}
	
	/**
	 * Get a main menu to setup the exchanges
	 * 
	 * @return
	 * @throws RemoteException 
	 */
	public MenuItem getMainMenu() throws RemoteException {		
		MenuItem mainMenu = new MenuItem("Main menu", "Bitocoin trader client", new MainMenuDinamic());
		return mainMenu;
	}
	
	/**
	 * Creates the main menu dinamically
	 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
	 *
	 */
	public class MainMenuDinamic extends MenuActionBase implements IDinamicMenu {
		@Override
		public List<MenuItem> getGeneratedItems() throws RemoteException {
			List<MenuItem> items = new ArrayList<MenuItem>();
			
			List<MenuItem> loadedPluginsItems = new ArrayList<MenuItem>();
			List<MenuItem> unloadPluginSubmenu = new ArrayList<MenuItem>();
			
			List<PluginExecInfo> loadedPlugins = server.getExecPluginsInfo();			
			for (PluginExecInfo p : loadedPlugins) {			
				String name = p.getDescription().getCompoundName() + " (" + p.getId() + ": " + p.getStatusMessage() + ")";
				String desc = p.getDescription().getDescription()  + " (" + p.getId() + ")";				
				loadedPluginsItems.add(new MenuItem(name, desc, new PluginCommandsMenu(p.getId())));
				unloadPluginSubmenu.add(new MenuItem(name, new UnloadPluginAction(p.getId())));				
			}
			
			List<PluginDescription> availablePlugins = server.getAvailablePlugins();
			List<MenuItem> loadPluginSubmenu = new ArrayList<MenuItem>();
			MenuItem refreshAvailablePlugins = new MenuItem("Refresh available plugins", new RefreshAvailablePluginsAction());
			loadPluginSubmenu.add(refreshAvailablePlugins);
			for (PluginDescription p : availablePlugins) {
				MenuItem item = new MenuItem(p.getName(), new LoadPluginAction(p.getCompoundName()));
				loadPluginSubmenu.add(item);				
			}
			
			MenuItem loadPlugin = new MenuItem("Load plugin", "Load a plugin", loadPluginSubmenu);
			MenuItem unloadPlugin = new MenuItem("Unload plugin", "Unload a plugin", unloadPluginSubmenu);
			
			
			items.add(loadPlugin);
			if (unloadPluginSubmenu.size() > 0) {
				items.add(unloadPlugin);
			}
			items.addAll(loadedPluginsItems);
								
			return items;
		}		
	}
		
	public class LoadPluginAction extends MenuActionBase implements IMenuAction {
		String pluginName;
		boolean renderParent = false;
		
		/**
		 * Action to load the specified plugin by name
		 * @param pluginName
		 */
		public LoadPluginAction(String pluginName) {
			this.pluginName = pluginName;
		}
		
		@Override
		public void execute() throws RemoteException {
			BasicExecutionResult excecResult = server.loadPlugin(pluginName);
			if (excecResult.isSuccess()) {
				System.out.println("Plugin loaded succesfully!");
			} else {
				System.out.println("Error loading the plugin:");
				System.out.println(excecResult.getMessage());
			}
			renderParent =  excecResult.isSuccess();
		}

		@Override
		public boolean isBackToParent() {
			return renderParent;
		}
	}

	public class UnloadPluginAction extends MenuActionBase implements IMenuAction {
		int pluginId;
		boolean renderParent = false;
		
		/**
		 * Action to load the specified plugin by name
		 * @param pluginName
		 */
		public UnloadPluginAction(int pluginId) {
			this.pluginId = pluginId;
		}
		
		@Override
		public void execute() throws RemoteException {
			BasicExecutionResult excecResult = server.unloadPlugin(pluginId);
			if (excecResult.isSuccess()) {
				System.out.println("Plugin unloaded succesfully!");
			} else {
				System.out.println("Error unloading the plugin:");
				System.out.println(excecResult.getMessage());
			}
			renderParent =  excecResult.isSuccess();
		}

		@Override
		public boolean isBackToParent() {
			return renderParent;
		}
	}

	
	/**
	 * Display plugin commands dinamic menu
	 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
	 */
	public class PluginCommandsMenu extends MenuActionBase implements IDinamicMenu {
		private int id;
		
		public PluginCommandsMenu(int pluginId) {
			this.id = pluginId;
		}
		
		@Override
		public List<MenuItem> getGeneratedItems() throws RemoteException {
			List<MenuItem> items = new ArrayList<MenuItem>();
			BasicExecutionResult execRes = server.getAvailableCommands(id);
			if (execRes.isSuccess()) {
				List<CommandDefinition> commands = (List<CommandDefinition>) execRes.getAdditionalData().get("commands");
				for (CommandDefinition cDef : commands) {
					Command c = new Command();
					c.setName(cDef.getName());
					MenuItem item = new MenuItem(cDef.getName(), new ExecPluginCommandAction(id, c));
					items.add(item);
				}
			}
			return items;
		}
		
	}
	
	/**
	 * Executes a command from a plugin
	 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
	 *
	 */
	public class ExecPluginCommandAction extends MenuActionBase implements IMenuAction {
		private Command cmd;
		private int pluginId;
		boolean renderParent = false;
			
		public ExecPluginCommandAction(int pluginId, Command cmd) {
			this.pluginId = pluginId;
			this.cmd = cmd;
		}
		
		@Override
		public void execute() throws RemoteException {
			BasicExecutionResult result = server.sendCommandToPlugin(pluginId, cmd);
			if (!result.isSuccess()) {
				System.out.println("Error executing command: " + result.getMessage());
			} else {
				if (result.getAdditionalData().containsKey("output")) {
					String output = (String) result.getAdditionalData().get("output");
					System.out.println(output);
				} else {
					System.out.println("Success!");
				}
			}			
		}
		
		@Override
		public boolean isBackToParent() {
			return renderParent;
		}		
	}
	
	public class LoadPlugin extends MenuActionBase implements IDinamicMenu {
		@Override
		public List<MenuItem> getGeneratedItems() throws RemoteException {
			List<MenuItem> result = new ArrayList<MenuItem>();
			List<PluginDescription> plugins = server.getAvailablePlugins();
			for (PluginDescription p: plugins) {
				MenuItem item = new MenuItem(p.getDescription(), new LoadPluginAction(p.getCompoundName()));
				result.add(item);
			}
			
			return result;
		}
	}
	
	public class RefreshAvailablePluginsAction extends MenuActionBase implements IMenuAction {
		@Override
		public void execute() throws RemoteException {
			BasicExecutionResult result = server.refreshAvailablePlugins();			
		}

		@Override
		public boolean isBackToParent() {
			return false;
		}
		
	}
}
