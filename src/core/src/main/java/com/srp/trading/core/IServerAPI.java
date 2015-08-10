/**
 * 
 */
package com.srp.trading.core;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public interface IServerAPI extends Remote { 
	/**
	 * Get a list of available plugins on the server
	 * @return
	 */
	public List<PluginDescription> getAvailablePlugins() throws RemoteException;
	
	/**
	 * Refresh the plugin list by scanning the current plugin directory list
	 * @return
	 * @throws RemoteException
	 */
	public BasicExecutionResult refreshAvailablePlugins() throws RemoteException;
	
	/**
	 * Retrieve information about the current running plugins
	 * @return
	 */
	public List<PluginExecInfo> getExecPluginsInfo() throws RemoteException;
	
	/**
	 * Load a plugin to operate in the specified exchange site
	 * @param name
	 * @return The execution result.
	 * In case of success, the String 'pluginId' will be added to the additional data
	 */
	public BasicExecutionResult loadPlugin(String name) throws RemoteException;
	
	/**
	 * Unload the specified plugin by its id
	 * @param id
	 * @return
	 */
	public BasicExecutionResult unloadPlugin(int id) throws RemoteException;
	
	/**
	 * Retrieve a list of available commands for the specified plugin.
	 * If the operation is successfull the data is located in
	 * the additional data under the key "commands" as List<CommandDefinition>
	 * @param pluginId
	 * @return
	 */
	public BasicExecutionResult getAvailableCommands(int pluginId) throws RemoteException;
	
	/**
	 * Send a command to the specified loaded plugin
	 * @param pluginId
	 * @param cmd
	 * @return
	 */
	public BasicExecutionResult sendCommandToPlugin(int pluginId, Command cmd) throws RemoteException;
}
