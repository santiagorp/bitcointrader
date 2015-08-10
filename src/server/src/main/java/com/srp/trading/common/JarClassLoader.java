/**
 * 
 */
package com.srp.trading.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import com.sun.org.apache.bcel.internal.util.ClassLoader;

/**
 * @author Santiago Rodríguez Pozo <santiagorp@gmail.com>
 *
 */
public class JarClassLoader extends ClassLoader {
	private static Logger logger = Logger.getLogger(JarClassLoader.class.getName());
	
	private String jarFileName;
    private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
    private Hashtable<String, Package> packages = new Hashtable<String, Package>();
        
  
    public JarClassLoader(String jarFileName) {  
        super(JarClassLoader.class.getClassLoader());
        this.jarFileName = jarFileName;
    }  
  
    public Class loadClass(String className) throws ClassNotFoundException {  
        return findClass(className);  
    }
    
    
    @Override
    public InputStream getResourceAsStream(String name) {    	
		try {
			JarFile jar = new JarFile(jarFileName);
	        JarEntry entry = jar.getJarEntry(name);
	        InputStream is = jar.getInputStream(entry);
	        return is;
		} catch (Exception e) {
		}
        
    	return super.getResourceAsStream(name);
    }
        
    @Override
    protected Package getPackage(String name) {
    	Package result = null;
    	
    	result = packages.get(name);
    	if (result != null) {
    		return result;
    	}
    	
    	result = super.getPackage(name);
    	
    	return result;
    }
  
    public Class findClass(String className) {  
        byte classByte[];  
        Class result = null;  
  
        result = classes.get(className); //checks in cached classes  
        if (result != null) {  
            return result;  
        }  
  
        try {  
            return findSystemClass(className);  
        } catch (Exception e) {  
        }  
        
  
        try {  
            JarFile jar = new JarFile(jarFileName);
            String packageName = className.substring(0, className.lastIndexOf('.'));
            String classPath = className.replace('.', '/') + ".class";            
            JarEntry entry = jar.getJarEntry(classPath);
            InputStream is = jar.getInputStream(entry);  
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
            int nextValue = is.read();  
            while (-1 != nextValue) {  
                byteStream.write(nextValue);  
                nextValue = is.read();  
            }  
  
            classByte = byteStream.toByteArray();  
            result = defineClass(className, classByte, 0, classByte.length, null);                        
            classes.put(className, result); 
            
            File jarFile = new File(jarFileName);
            URL jarUrl = jarFile.toURI().toURL();
            Package pkg = getPackage(packageName);            
            if (pkg == null) {
            	pkg = definePackage(packageName, "", "", "", "", "", "", jarUrl);
            	packages.put(packageName, pkg);
            }
            
            
            return result;  
        } catch (Exception e) {
        	logger.info(e);
            return null;  
        }  
    }  
}
