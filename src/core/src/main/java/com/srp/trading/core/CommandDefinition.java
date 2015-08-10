/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class CommandDefinition implements Serializable {
	private String name;
	private String description;
	private String defaultValue;
	private HashMap<String, String> parameterDefinitions = new HashMap<String, String>();
	private HashMap<String, String> defaultValues = new HashMap<String, String>();
		
	public CommandDefinition(String name, String description) {
		this.name = name;
		this.description = description;		
	}
	
	/**
	 * Add a new parameter to the command definition
	 * @param name
	 * @param description
	 */
	public void addParameter(String name, String description, String defaultValue) {
		parameterDefinitions.put(name, description);
		defaultValues.put(name, defaultValue);
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public HashMap<String, String> getParameterDefinitions() {
		return parameterDefinitions;
	}

	public HashMap<String, String> getDefaultValues() {
		return defaultValues;
	}
}
