package org.processmining.framework.util;

import java.net.URL;
import java.net.URLClassLoader;

public class ProMClassLoader extends URLClassLoader {

	public ProMClassLoader(ClassLoader loader) {
		super(new URL[] { }, loader);
	}
	
//	protected Class<?> findClass(String name) throws ClassNotFoundException {
//		System.err.println("[ProMClassLoader] Find class " + name);
//		return super.findClass(name);
//	}

//    @Override public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("[ProMClassLoader] Load class " + name);
//        return super.loadClass(name, resolve);
//    }
    
}
