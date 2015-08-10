/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Contains the basic
 */
public class PluginExecInfo implements Serializable {
	Integer id;
	PluginDescription description;
	String statusMessage;
	
	public PluginExecInfo(int id, PluginDescription description, String statusMessage) {
		this.id = id;
		this.description = description;
		this.statusMessage = statusMessage;
	}
	
	public int getId() {
		return id;
	}
	
	public PluginDescription getDescription() {
		return description;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
}
