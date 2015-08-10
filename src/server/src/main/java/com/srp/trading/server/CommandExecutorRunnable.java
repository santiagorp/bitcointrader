/**
 * 
 */
package com.srp.trading.server;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.plugin.BasePluginDB;
import com.srp.trading.plugin.IPluginAPI;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CommandExecutorRunnable implements Runnable {
	private IPluginAPI plugin;
	private Command cmd;
	BasicExecutionResult result = new BasicExecutionResult(false,  "Command not executed");
	
	public CommandExecutorRunnable(IPluginAPI plugin, Command cmd) {
		this.plugin = plugin;
		this.cmd = cmd;
	}
		
	@Override
	public void run() {
		BasePluginDB dbPlugin = null;
		if (plugin instanceof BasePluginDB) {
			dbPlugin =(BasePluginDB) plugin; 
		}
		if (dbPlugin != null && dbPlugin.getDbName() != null && !dbPlugin.getDbName().isEmpty()) {
			result = dbPlugin.wrappedDBexecuteCommand(cmd);
		} else {
			result = plugin.executeCommand(cmd);
		}
	}

	public BasicExecutionResult getResult() {
		return result;
	}
}
