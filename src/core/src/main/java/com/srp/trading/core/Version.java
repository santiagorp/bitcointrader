/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Represents a version defined by a major.minor naming convention
 */
public class Version implements Serializable, Comparable<Version> {
	private int major = 0;
	private int minor = 0;
	
	public Version() {
	}
	
	/**
	 * Initialize with the specified values
	 * @param major
	 * @param minor
	 */
	public Version(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	/**
	 * Initializes with a major version and minor = 0
	 * @param major
	 */
	public Version(int major) {
		this.major = major;
		this.minor = 0;
	}
	
	/**
	 * Initializes with the specified version.
	 * The string on the format "x" or "x.y" being x and y integers
	 * @param versionStr
	 */
	public Version(String versionStr) {
		String[] ss = versionStr.split("\\.");
		if (versionStr.startsWith(".") && ss.length > 0) {
			this.minor = Integer.parseInt(ss[1]);
		} else {
			this.major = ss.length > 0 ? Integer.parseInt(ss[0]) : 0;
			this.minor = ss.length > 1 ? Integer.parseInt(ss[1]) : 0;
		}
	}
	

	/**
	 * Compares the current version with another one.
	 * Returns:
	 * 	< 0 if current object has lower version than the compared one
	 *  = 0 if both versions are the same.
	 *  > 0 if current object has greater version than the compared one
	 */
	@Override
	public int compareTo(Version v) {
		int result = this.major == v.major ? this.minor - v.getMinor() : this.major - v.getMajor();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return this.major + "." + this.minor;
	}
	
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	}
