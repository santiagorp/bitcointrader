/**
 * 
 */
package com.srp.trading.plugin;

import java.util.List;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.PluginDescription;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public interface IPluginAPI {
	/**
	 * Get a list of available commands for the current plugin
	 * @return
	 */
	List<CommandDefinition> getCommandDefinitions();
	
	/**
	 * Invoke the execution of the specified command in the plugin.
	 * Returns the execution result.
	 * @param cmd
	 * @return
	 */
	BasicExecutionResult executeCommand(Command cmd);
	
	/**
	 * Retrieve the plugin description
	 * @return
	 */
	PluginDescription getPluginDescription();
	
	/**
	 * Returns success = true if the plugin can be loaded.
	 * @return
	 */
	BasicExecutionResult canBeLoaded();
	
	/**
	 * Method executed after the plugin object has been constructed
	 * @param config The basic config settings for the current plugin
	 */
	void Initialize(PluginConfig config);
	
	/**
	 * Shutdown the plugin (in case that contain runable threads/activities)
	 */
	void shutdown();
	
	/**
	 * Text with information regarding the current plugin status 
	 * @return
	 */
	String getStatusMessage();
}
