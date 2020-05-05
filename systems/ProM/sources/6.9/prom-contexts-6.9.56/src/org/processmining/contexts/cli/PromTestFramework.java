package org.processmining.contexts.cli;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.processmining.framework.annotations.TestMethod;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.plugin.PluginManager;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.impl.PluginCacheEntry;
import org.processmining.framework.util.CommandLineArgumentList;

public class PromTestFramework {

	@Plugin(name = "ProMTest", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		System.out.println("Entering ProM Test Framework");
		
		// from where do we read the tests
		String classesToTestDir = null;			// default
		
		if (commandlineArguments.size() != 2)
			throw new Exception("Error. The ProM Test Framework requires 2 arguments: (1) location of classes that contain tests, (2) location of test files");
		
		// directory where test cases are stored
		classesToTestDir = commandlineArguments.get(0); 
		// read location where test input files and expected test outputs are stored
		final String testFileRoot = commandlineArguments.get(1);
		
		// scan directory for tests
		getAllTestMethods(classesToTestDir);
		
		// and run tests, collect all failed tests
		List<PromTestException.ResultMismatch> failedTest = new LinkedList<PromTestException.ResultMismatch>();
		List<PromTestException.WrappedException> errorTest = new LinkedList<PromTestException.WrappedException>();
		
		System.out.println("Running "+testMethods.size()+" tests:");
		for (Method test : testMethods) {
			
			try {
				System.out.println(test);
				
				// run test and get test result
				String result = (String)test.invoke(null);
				// load expected result
				String expected = null;
				if (testResultFromOutputAnnotation(test)) {
					expected = test.getAnnotation(TestMethod.class).output();
				} else if (testResultFromFile(test)) {
					expected = readFile(testFileRoot+"/"+test.getAnnotation(TestMethod.class).filename());
				}
				// compare result and expected
				if (!result.equals(expected)) {
					// test failed, store for reporting
					failedTest.add(
						new PromTestException.ResultMismatch(test, expected, result));
				}
			} catch (Throwable e) {
				// test crashed, store exception for reporting
				errorTest.add(
						new PromTestException.WrappedException(test, e));
			}
		}
		
		if (!failedTest.isEmpty() || ! errorTest.isEmpty()) {
			throw new PromTestException(failedTest, errorTest);
		}
		
    	return null;
	}
	
	private void getAllTestMethods(String lookUpDir) throws Exception {
		
		URL[] defaultURLs;
		
		if (lookUpDir == null) {
			URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			defaultURLs = sysloader.getURLs();
		} else {
			File f = new File(lookUpDir);
			defaultURLs = new URL[] { f.toURI().toURL()	};
		}
		
		File f = new File("." + File.separator + Boot.LIB_FOLDER);
		String libPath = f.getCanonicalPath();

		for (URL url : defaultURLs) {
			if (Boot.VERBOSE == Level.ALL) {
				System.out.println("Processing url: " + url);
			}
			if (!(new File(url.toURI()).getCanonicalPath().startsWith(libPath))) {
				if (Boot.VERBOSE == Level.ALL) {
					System.out.println("Scanning for tests: " + url);
				}
				register(url);
			} else {
				if (Boot.VERBOSE == Level.ALL) {
					System.out.println("Skipping: " + url.getFile() + " while scanning for tests.");
				}
			}
		}
	}
	
	/**
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.plugin.PluginManager#register(java.net.URL)
	 */
	public void register(URL url) {
		if (url.getProtocol().equals(PluginManager.FILE_PROTOCOL)) {
			try {
				File file = new File(url.toURI());

				if (file.isDirectory()) {
					scanDirectory(file);
					return;
				}
				// we ignore: PluginManager.MCR_EXTENSION
				else if (file.getAbsolutePath().endsWith(PluginManager.JAR_EXTENSION)) {
					scanUrl(url);
				}
			} catch (URISyntaxException e) {
				// fireError(url, e, null);
				System.err.println(e);
			}
		} else {
			// scanUrl(url);
			System.err.println("Loading tests from "+url+" not supported.");
		}
	}
	
	private void scanDirectory(File file) {
		try {
			URL url = file.toURI().toURL();
			URLClassLoader loader = new URLClassLoader(new URL[] { url });

			Queue<File> todo = new LinkedList<File>();
			FileFilter filter = new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory() || pathname.getPath().endsWith(PluginManager.CLASS_EXTENSION);
				}
			};

			todo.add(file);
			while (!todo.isEmpty()) {
				File dir = todo.remove();

				for (File f : dir.listFiles(filter)) {
					if (f.isDirectory()) {
						todo.add(f);
					} else {
						if (f.getAbsolutePath().endsWith(PluginManager.CLASS_EXTENSION)) {
							loadClassFromFile(loader, url,
									makeRelativePath(file.getAbsolutePath(), f.getAbsolutePath()));
						}
					}
				}
			}
		} catch (MalformedURLException e) {
			//fireError(null, e, null);
			System.err.println(e);
		}
	}
	
	private void scanUrl(URL url) {
		URLClassLoader loader = new URLClassLoader(new URL[] { url });
		PluginCacheEntry cached = new PluginCacheEntry(url, Boot.VERBOSE);

		if (cached.isInCache()) {
			for (String className : cached.getCachedClassNames()) {
				loadClass(loader, url, className);
			}
		} else {
			try {
				InputStream is = url.openStream();
				JarInputStream jis = new JarInputStream(is);
				JarEntry je;
				List<String> loadedClasses = new ArrayList<String>();

				while ((je = jis.getNextJarEntry()) != null) {
					if (!je.isDirectory() && je.getName().endsWith(PluginManager.CLASS_EXTENSION)) {
						String loadedClass = loadClassFromFile(loader, url, je.getName());
						loadedClasses.add(loadedClass);
					}
				}
				jis.close();
				is.close();

				cached.update(loadedClasses);
			} catch (IOException e) {
				//fireError(url, e, null);
				System.err.println(e);
			}
		}
	}


	private String makeRelativePath(String root, String absolutePath) {
		String relative = absolutePath;

		if (relative.startsWith(root)) {
			relative = relative.substring(root.length());
			if (relative.startsWith(File.separator)) {
				relative = relative.substring(File.separator.length());
			}
		}
		return relative;
	}
	
	private static final char PACKAGE_SEPARATOR = '.';
	private static final char URL_SEPARATOR = '/';
	private static final char INNER_CLASS_MARKER = '$';

	
	private String loadClassFromFile(URLClassLoader loader, URL url, String classFilename) {
		if (classFilename.indexOf(INNER_CLASS_MARKER) >= 0) {
			// we're not going to load inner classes
			return null;
		}
		return loadClass(loader, url, classFilename.substring(0, classFilename.length() - PluginManager.CLASS_EXTENSION.length())
				.replace(URL_SEPARATOR, PACKAGE_SEPARATOR).replace(File.separatorChar, PACKAGE_SEPARATOR));
	}
	

	private final List<Method> testMethods = new LinkedList<Method>();
	
	/**
	 * Returns the name of the class, if it is annotated, or if any of its
	 * methods carries a plugin annotation!
	 * 
	 * @param loader
	 * @param url
	 * @param className
	 * @return
	 */
	private String loadClass(URLClassLoader loader, URL url, String className) {
		boolean isAnnotated = false;

		if ((className == null) || className.trim().equals("")) {
			return null;
		}

		className = className.trim();
		try {
			Class<?> pluginClass = Class.forName(className, false, loader);
			/*
			// Check if plugin annotation is present
			if (pluginClass.isAnnotationPresent(Plugin.class) && isGoodPlugin(pluginClass)) {
				PluginDescriptorImpl pl = new PluginDescriptorImpl(pluginClass, pluginContextType);
				addPlugin(pl);
			}*/

			for (Method method : pluginClass.getMethods()) {
				
				if (method.isAnnotationPresent(TestMethod.class) && isGoodTest(method)) {
					testMethods.add(method);
				}
			}
		} catch (Throwable t) {
			// fireError(url, t, className);
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("[Framework] ERROR while scanning for testable plugins at: " + url + ":");
				System.err.println("   in file :" + className);
				System.err.println("   " + t.getMessage());
				t.printStackTrace();
			}
		}
		return isAnnotated ? className : null;
	}
	
	private boolean isGoodTest(Method method) {
		
		assert(method.isAnnotationPresent(TestMethod.class));
		
		// check annotations
		if (!testResultFromFile(method) && !testResultFromOutputAnnotation(method)) {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("Test " + method.toString() + " could not be loaded. "
						+ "No expected test result specified.");
			}
			return false;
		}

		// check return type: must be String
		if ((method.getModifiers() & Modifier.STATIC) == 0) {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("Test " + method.toString() + " could not be loaded. "
						+ "Test must be static.");
			}
			return false;
		}

		// check return type: must be String
		if (!method.getReturnType().equals(String.class)) {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("Test " + method.toString() + " could not be loaded. "
						+ "Return result must be java.lang.String");
			}
			return false;
		}

		// check parameter types: must be empty
		Class<?>[] pars = method.getParameterTypes();
		if (pars != null && pars.length > 0) {
			if (Boot.VERBOSE != Level.NONE) {
				System.err.println("Test " + method.toString() + " could not be loaded. "
						+ "A test must not take any parameters.");
			}
			return false;			
		}
		return true;
	}

	/**
	 * @param method
	 * @return <code>true</code> iff the method is annotated with
	 *         {@link TestMethod#filename()}. Then the result of the test will
	 *         be compared to the contents of a file.
	 */
	private static boolean testResultFromFile(Method method) {
		assert(method.isAnnotationPresent(TestMethod.class));		
		return 
			method.getAnnotation(TestMethod.class).filename() != null
			&& !method.getAnnotation(TestMethod.class).filename().isEmpty();
	}

	/**
	 * @param method
	 * @return <code>true</code> iff the method is annotated with
	 *         {@link TestMethod#output()}. Then the result of the test will
	 *         be compared to the specified string.
	 */
	private static boolean testResultFromOutputAnnotation(Method method) {
		assert(method.isAnnotationPresent(TestMethod.class));
		return
			method.getAnnotation(TestMethod.class).output() != null
			&& !method.getAnnotation(TestMethod.class).output().isEmpty();
	}
	
	private static String readFile(String scriptFile) throws IOException {
		InputStream is = new FileInputStream(scriptFile);
		String result = readWholeStream(is);
		is.close();
		return result;
	}

	private static String readWholeStream(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(new BufferedInputStream(is));
		StringBuffer result = new StringBuffer();
		int c;

		while ((c = reader.read()) != -1) {
			result.append((char) c);
		}
		return result.toString();
	}
	
	public static void main(String[] args) throws Throwable {
	  try {
	    Boot.boot(PromTestFramework.class, CLIPluginContext.class, args);
	  } catch (InvocationTargetException e) {
	    throw e.getCause();
	  }
	}
}
