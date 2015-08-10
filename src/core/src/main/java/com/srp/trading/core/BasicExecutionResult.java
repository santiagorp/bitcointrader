/**
 * 
 */
package com.srp.trading.core;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 * Basic execution result containing boolean and message
 */
public class BasicExecutionResult implements Serializable {
	boolean success;
	String message;
	HashMap<String, Object> additionalData = new HashMap<String, Object>();
	
	public BasicExecutionResult(boolean success, String msg) {
		this.success = success;
		this.message = msg;
	}	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public HashMap<String, Object> getAdditionalData() {
		return additionalData;
	}
}
