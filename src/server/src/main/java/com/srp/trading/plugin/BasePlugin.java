/**
 * 
 */
package com.srp.trading.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.rolling.RollingFileAppender;
import org.apache.log4j.rolling.RollingPolicy;
import org.apache.log4j.rolling.TimeBasedRollingPolicy;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.srp.trading.core.BasicExecutionResult;
import com.srp.trading.core.Command;
import com.srp.trading.core.CommandDefinition;
import com.srp.trading.core.PluginDescription;
import com.srp.trading.core.Version;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Base clase used for all the plugin implementations
 */
public class BasePlugin implements IPluginAPI, ApplicationContextAware {
	protected ApplicationContext applicationContext;	
	protected PluginDescription pluginDesc = new PluginDescription();
	protected PluginConfig pluginConfig = null;
		
	public BasePlugin(String name, Version version, String description) {
		this.pluginDesc.setName(name);
		this.pluginDesc.setVersion(version);
		this.pluginDesc.setDescription(description);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public List<CommandDefinition> getCommandDefinitions() {
		return new ArrayList<CommandDefinition>();
	}

	@Override
	public BasicExecutionResult executeCommand(Command cmd) {
		return new BasicExecutionResult(false, "Command not defined");
	}

	@Override
	public PluginDescription getPluginDescription() {
		return pluginDesc;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public BasicExecutionResult canBeLoaded() {
		BasicExecutionResult result = new BasicExecutionResult(false, "Plugin could not be loaded: not load conditions defined.");
		return result;
	}

	@Override
	public void Initialize(PluginConfig config) {
		this.pluginConfig = config;
	}

	@Override
	public String getStatusMessage() {
		return "No status info";
	}
	
	/**
	 * Create a new log4j appender in the plugin log directory with the specified basename
	 * @param baseName
	 * @return
	 */
	protected Appender createAppender(String baseName) {
		RollingFileAppender appender = new RollingFileAppender();
		TimeBasedRollingPolicy rollPolicy = new TimeBasedRollingPolicy();		
		String logFile = new File(pluginConfig.getLogPath(), baseName + "-%d{yyyyMMdd}.log.gz").toString();		
		rollPolicy.setFileNamePattern(logFile);
		appender.setRollingPolicy(rollPolicy);
		rollPolicy.activateOptions();
		appender.activateOptions();		
		
		Layout layout = new PatternLayout("%d{yyyy/MM/dd HH:mm:ss} %-5p %c{1}:%L - %m%n");
		appender.setLayout(layout);
		return appender;
	}
}
