/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class PluginDescription implements Serializable {
	String name;
	String description;
	Version version;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Version getVersion() {
		return version;
	}
	public String getCompoundName() {
		return name + "-" + version.toString();
	}
	public void setVersion(Version version) {
		this.version = version;
	}
	@Override
	public String toString() {
		String s = name + ", " + getVersion().toString() + ": " + description;
		return s;
	}
}
