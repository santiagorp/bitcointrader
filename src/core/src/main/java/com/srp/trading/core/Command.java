/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Command to be sent to a plugin
 */
public class Command implements Serializable {
	String name;
	HashMap<String, String> parameters = new HashMap<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public HashMap<String, String> getParameters() {
		return parameters;
	}
	public void setParameters(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}	
}
