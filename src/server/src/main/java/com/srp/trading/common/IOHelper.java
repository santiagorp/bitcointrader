/**
 * 
 */
package com.srp.trading.common;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class IOHelper {
	private static Logger logger = Logger.getLogger(IOHelper.class.getName());
	
	/**
	 * Get all the jar files in the specified directory
	 * @return
	 */
	public static String[] getJarNames(String path) {
		File dir = new File(path);
		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.endsWith(".jar")) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		String[] jarFiles = dir.list(jarFilter);

		for (int i = 0; i < jarFiles.length; i++) {
			jarFiles[i] = new File(path,  jarFiles[i]).toString();
		}
					
		return jarFiles;
	}
	
	/**
	 * Return full path to directories inside of the specified path
	 * @param path
	 * @return
	 */
	public static String[] getDirs(String path) {
		File dir = new File(path);
		FilenameFilter dirFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (dir.isDirectory()) {
					return true;
				} else {
					return false;
				}
			}
		};
		
		String[] dirPaths = dir.list(dirFilter);

		for (int i = 0; i < dirPaths.length; i++) {
			dirPaths[i] = new File(path,  dirPaths[i]).toString();
		}
					
		return dirPaths;		
	}
	
	/**
	 * Load a class from the specified jar file
	 * @param jarPath
	 * @param className
	 * @return
	 */
	public static Object getInstance(String jarPath, String className) {
		Object result;
		try {
			String jarfile = new File(jarPath).toString();
			JarClassLoader jcl = new JarClassLoader(jarfile);			
			result = jcl.loadClass(className).newInstance();
		} catch (Exception e) {
			logger.debug(e);
			result = null;
		}		
		
		return result;		
	}
}
