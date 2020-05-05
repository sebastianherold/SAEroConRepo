package org.processmining.contexts.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
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

/**
 * Utility class to collect all methods annotated with <code>@TestMethod</code>
 * from a given location of compiled Java classes.
 * 
 * @author dfahland
 */
public class AllInclassMethodTests {

	// the list fo all found methods
	private final List<Method> testMethods = new LinkedList<Method>();
	
	/**
	 * @return all found methods
	 */
	public List<Method> getAllTestMethods() {
		return testMethods;
	}
	
	/**
	 * Find all methods with annotation <code>@TestMethod</code> in the given
	 * loop up directory. The classes can be stored inside a JAR file. The list
	 * of found methods can be retrieved with {@link #getAllTestMethods()}.
	 * 
	 * This method can be run several times to scan classes from different
	 * directories.
	 * 
	 * @param lookUpDir
	 */
	public void collectAllTestMethods(String lookUpDir)  {
		
		System.out.println("Collecting inclass method tests from "+lookUpDir);

		try {
	
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
		} catch (MalformedURLException e) {
			System.err.println(lookUpDir+" gives an invalid URL.\n"+e);
		} catch (URISyntaxException e) {
			System.err.println(lookUpDir+" gives an invalid URI.\n"+e);
		} catch (IOException e) {
			System.err.println("Could not read "+lookUpDir+"\n"+e);
		}
	}
	
	private void register(URL url) {
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

		} catch (IOException e) {
			//fireError(url, e, null);
			System.err.println(e);
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
//		if (classFilename.indexOf(INNER_CLASS_MARKER) >= 0) {
//			// we're not going to load inner classes
//			return null;
//		}
		return loadClass(loader, url, classFilename.substring(0, classFilename.length() - PluginManager.CLASS_EXTENSION.length())
				.replace(URL_SEPARATOR, PACKAGE_SEPARATOR).replace(File.separatorChar, PACKAGE_SEPARATOR));
	}
	

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
				System.err.println("[Framework] ERROR while scanning for tests at: " + url + ":");
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
		if (!testExpectedFromFile(method) && !testExpectedFromOutputAnnotation(method)) {
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
	public static boolean testExpectedFromFile(Method method) {
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
	public static boolean testExpectedFromOutputAnnotation(Method method) {
		assert(method.isAnnotationPresent(TestMethod.class));
		return
			method.getAnnotation(TestMethod.class).output() != null
			&& !method.getAnnotation(TestMethod.class).output().isEmpty();
	}
	
	/**
	 * @param method
	 * @return <code>true</code> iff the method is annotated with
	 *         <code>{@link TestMethod#returnSystemOut()} == true</code>.
	 *         Then the return result of the method will be everything the
	 *         method wrote to {@link System#out}.
	 */
	public static boolean testResultFromSystemOut(Method method) {
		assert(method.isAnnotationPresent(TestMethod.class));
		return
			method.getAnnotation(TestMethod.class).returnSystemOut() == true;
	}
	
	public static String getTestName (Method m) {
		return m.getClass().toString()+"."+m.getName();
	}
	
	@TestMethod(output="correct output")
	public static String test_basicOutputTest() {
		return "correct output";
	}
	
	@TestMethod(filename="testresult_AllInclassMethodTests_basicFileTest.txt")
	public static String test_basicFileTest() {
		return "correct output (filetest)";
	}

	@TestMethod(filename="testresult_AllInclassMethodTests_basicFileTest.txt", output="correct output")
	public static String test_dualTest() {
		return "correct output";
	}
	
	@TestMethod(filename="testresult_AllInclassMethodTests_basicFileTest.txt", returnSystemOut=true)
	public static String test_basicFileTest_OutputStream() {
		System.out.print("correct output");
		System.out.print(" (filetest)");
		return null;
	}
}
