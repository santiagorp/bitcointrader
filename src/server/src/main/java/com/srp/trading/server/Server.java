/**
 * 
 */
package com.srp.trading.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.IServerAPI;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.PluginExecInfo;
import com.srp.trading.plugin.IPluginAPI;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 * 
 */
public class Server implements IServerAPI {
	private Logger logger = Logger.getLogger(Server.class.getName());
	private PluginFactory pluginFactory = null;
	private HashMap<Integer, IPluginAPI> loadedPlugins = new HashMap<Integer, IPluginAPI>();
	private Integer lastPluginId = 0;

	public Server() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.srp.trading.core.IServerAPI#getAvailablePlugins()
	 */
	@Override
	public List<PluginDescription> getAvailablePlugins() {
		logger.info("Retrieving list of available plugins");
		List<PluginDescription> result = new ArrayList<PluginDescription>();
		result.addAll(pluginFactory.getAvailablePlugins().values());
		return result;
	}

	@Override
	public BasicExecutionResult loadPlugin(String name) {
		BasicExecutionResult result;
		IPluginAPI plugin = pluginFactory.createPlugin(name);
		if (plugin == null) {
			result = new BasicExecutionResult(false, "Plugin not found: " + name);
			return result;
		}

		BasicExecutionResult canLoad = plugin.canBeLoaded();
		if (!canLoad.isSuccess()) {
			result = new BasicExecutionResult(false, "Plugin could not be loaded: " + canLoad.getMessage());
			return result;
		}

		lastPluginId++;

		loadedPlugins.put(lastPluginId, plugin);
		result = new BasicExecutionResult(true, "Plugin loaded succesfully");
		result.getAdditionalData().put("pluginId", lastPluginId);
		logger.info(plugin.getPluginDescription().toString() + ", Identifier: " + lastPluginId);

		return result;
	}

	@Override
	public BasicExecutionResult unloadPlugin(int id) {
		if (!loadedPlugins.containsKey(id)) {
			return new BasicExecutionResult(false, "Plugin not found with the specified id.");
		}

		IPluginAPI p = loadedPlugins.get(id);
		p.shutdown();
		loadedPlugins.remove(id);
		logger.info("Unloaded plugin id " + id);
		BasicExecutionResult result = new BasicExecutionResult(true, "Plugin unloaded");

		return result;
	}

	@Override
	public List<PluginExecInfo> getExecPluginsInfo() {
		List<PluginExecInfo> result = new ArrayList<PluginExecInfo>();
		for (int id : loadedPlugins.keySet()) {
			IPluginAPI p = loadedPlugins.get(id);
			PluginExecInfo pei = new PluginExecInfo(id, p.getPluginDescription(), p.getStatusMessage());
			result.add(pei);
		}

		return result;
	}

	@Override
	public BasicExecutionResult sendCommandToPlugin(int id, Command cmd) {
		if (!loadedPlugins.containsKey(id)) {
			return new BasicExecutionResult(false, "Plugin not found with the specified id.");
		}
		BasicExecutionResult result;
		logger.info("Sending command [" + cmd.getName() + "] to plugin id " + id);
		IPluginAPI p = loadedPlugins.get(id);
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {			
			CommandExecutorRunnable runnable = new CommandExecutorRunnable(p, cmd);
			Future<?> marketDataFuture = executorService.submit(runnable);
			marketDataFuture.get(); // Execute in thread and wait
			result = runnable.getResult();
		} catch (Exception e) {
			result = new BasicExecutionResult(false, "Error executing command: " + e.getMessage());
			logger.info(e);
		}
		executorService.shutdown();

		return result;
	}

	@Override
	public BasicExecutionResult getAvailableCommands(int pluginId) {
		if (!loadedPlugins.containsKey(pluginId)) {
			return new BasicExecutionResult(false, "Plugin not found with the specified id.");
		}
		IPluginAPI p = loadedPlugins.get(pluginId);
		List<CommandDefinition> commands = p.getCommandDefinitions();
		BasicExecutionResult result = new BasicExecutionResult(true, null);
		result.getAdditionalData().put("commands", commands);
		return result;
	}

	public PluginFactory getPluginFactory() {
		return pluginFactory;
	}

	public void setPluginFactory(PluginFactory pluginFactory) {
		this.pluginFactory = pluginFactory;
	}

	public HashMap<Integer, IPluginAPI> getLoadedPlugins() {
		return loadedPlugins;
	}

	@Override
	public BasicExecutionResult refreshAvailablePlugins() throws RemoteException {
		pluginFactory.RefreshAvailablePlugins();
		BasicExecutionResult result = new BasicExecutionResult(true, "Plugins refreshed");
		return result;
	}
}
