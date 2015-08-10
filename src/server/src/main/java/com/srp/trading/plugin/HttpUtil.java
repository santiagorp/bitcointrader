/**
 * 
 */
package com.srp.trading.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class HttpUtil {
	static final Logger logger = Logger.getLogger(HttpUtil.class.getName());
	
	private static final String USER_AGENT = "Mozilla/5.0";	
	/**
	 * Perfrom http get request with a default timeout of 10 seconds
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static String sendGet(String url) throws Exception {
		return sendGet(url, 10000);	
	}
	
	
	/**
	 * Perform a http get request
	 * @param url
	 * @param timeout The timeout in ms
	 * @return
	 * @throws Exception
	 */
	public static String sendGet(String url, int timeout) throws Exception {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
		// optional default is GET
		con.setRequestMethod("GET");
		
		// Set timeout
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);
 
		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);
 
		int responseCode = con.getResponseCode();
		logger.debug("\nSending 'GET' request to URL : " + url);
		logger.debug("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
 
		return response.toString(); 
	}
}
