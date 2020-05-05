package org.processmining.contexts.scripting;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.annotations.CLI;
import org.processmining.framework.util.StringUtils;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

/**
 * Script interpretor for ProM. The class provides a wrapper around
 * the Java BSH {@link Interpreter} to initialize all available plugins
 * from the .ProM user directory in an {@link Interpreter}. This
 * interpreter can the be used to interpret Java code at run-time
 * against the plugins, which allows for scripted execution of ProM plugins.
 * 
 * @author dfahland and others
 */
public class ScriptExecutor {

	// the interpreter used to run a script
	private Interpreter interpreter;
	// the plugin context in which the interpreter is executed, it
	// allows to retrieve the available plugins
	private final PluginContext context;

	// set of all method signatures announced by available plugins
	private Set<Signature> availablePlugins;
	// set of all method signatures announced by plugins that could not
	// be loaded for one reason or the other
	private Set<Signature> failedPlugins;

	/**
	 * Meant to provide diagnostic information in case a script fails.
	 */
	public class ScriptExecutionException extends Exception {

		private static final long serialVersionUID = -4777627419215658865L;

		public ScriptExecutionException(EvalError e) {
			super(e);
		}
		
		public ScriptExecutionException(String e) {
			super(e);
		}
	}

	/**
	 * Initialize a new interpreter with all available plugins.
	 * 
	 * @param context
	 * @throws ScriptExecutionException
	 */
	public ScriptExecutor(PluginContext context) throws ScriptExecutionException {
		this.context = context;
		
		availablePlugins = new HashSet<Signature>();
		failedPlugins = new HashSet<Signature>();
		
		init();
	}

	/**
	 * Execute a script in the interpreter.
	 * 
	 * @param script
	 * @throws ScriptExecutionException
	 */
	public void execute(String script) throws Throwable {
		try {
			interpreter.eval(script);
		} catch (EvalError e) {
		  
			// provide more detailed error messages
			if (e instanceof TargetError) {
				TargetError e2 = (TargetError)e;
				
				if (e2.getTarget() instanceof ExecutionException) {
					ExecutionException e3 = (ExecutionException)e2.getTarget();
					
					if (e3.getCause() instanceof FileNotFoundException) {
						System.err.println("Error. The script tries to access a non-existing file path:\n  "+
								((FileNotFoundException)e3.getCause()).getMessage());
					}
					
				  throw e3.getCause();
				} else if (e2.getTarget() instanceof java.util.concurrent.CancellationException) {
				  System.err.println("Script execution was cancelled. Message:\n"+e);
				  throw e2.getTarget();
				} else if (e2.getTarget() instanceof java.lang.AssertionError) {
				  throw e2.getTarget();
				}
			}
			throw new ScriptExecutionException(e);
		}
	}
	
	/**
	 * Iterate over all registered plugins, find their declared methods
	 * and try to create an invocation context for each. Return the list
	 * of plugins that succeeded, and save the list of methods that failed.
	 * 
	 * @return
	 */
	private LinkedList<PluginDescriptor> workingPlugins() {
		LinkedList<PluginDescriptor> workingPlugins = new LinkedList<PluginDescriptor>();

		Set<Signature> foundSignatures = new HashSet<Signature>();
		String nl = System.getProperty("line.separator");
		int pluginIndex = 0;

		for (PluginDescriptor plugin : context.getPluginManager().getAllPlugins())
		{
			if (Boot.VERBOSE == Level.ALL) System.out.println("checking "+plugin.getName());

			// method signatures of this plugin
			Set<Signature> thisPluginSignatures = new HashSet<Signature>();
			
			try {
				
				StringBuffer init = new StringBuffer();
				Interpreter pluginInterpreter = new Interpreter();
				pluginInterpreter.set("__main_context", context);
				
				// the right context type is checked at start by the
				// pluginmanager
				// if
				// (plugin.getContextType().isAssignableFrom(context.getClass()))
				// {
				for (int j = 0; j < plugin.getParameterTypes().size(); j++) {
					Signature signature = getSignature(plugin, j);
					thisPluginSignatures.add(signature);

					if (!foundSignatures.contains(signature)) {
						foundSignatures.add(signature);
						pluginIndex++;

						pluginInterpreter.set("__plugin_descriptor" + pluginIndex, plugin);
						pluginInterpreter.set("__plugin_method_index" + pluginIndex, j);

						if (signature.getReturnTypes().size() == 1) {
							init.append(Object.class.getCanonicalName());
						} else {
							init.append(Object[].class.getCanonicalName());
						}
						init.append(" " + signature.getName() + "(");

						int index = 0;
						for (Class<?> cl : signature.getParameterTypes()) {
							if (index > 0) {
								init.append(", ");
							}
							init.append(cl.getCanonicalName());
							init.append(" p" + index++);
						}
						init.append(") {" + nl);
						init.append("    " + PluginContext.class.getCanonicalName()
								+ " context = __main_context.createChildContext(\"Result of ");
						init.append(signature.getName() + "\");" + nl);

						init.append("    __plugin_descriptor" + pluginIndex + ".invoke(__plugin_method_index"
								+ pluginIndex + ", context, new " + (Object[].class.getCanonicalName()) + " { ");
						for (int i = 0; i < signature.getParameterTypes().size(); i++) {
							if (i > 0) {
								init.append(", ");
							}
							init.append("p" + i);
						}
						init.append(" });" + nl);

						if (signature.getReturnTypes().size() > 1) {
							init.append("    context.getResult().synchronize();" + nl);
							init.append("    " + Object[].class.getCanonicalName() + " result = new "
									+ Object.class.getCanonicalName() + "[context.getResult().getSize()];" + nl);
							init
									.append("    for (int i = 0; i < result.length; i++) { result[i] = context.getResult().getResult(i); }"
											+ nl);
							init.append("    return result;" + nl);
						} else {
							init.append("    return context.getFutureResult(0).get();" + nl);
						}
						init.append("}" + nl);
					}
				}

				//pluginInterpreter.eval(init.toString());
				workingPlugins.addLast(plugin);		// this plugin works, remeber it
			
			} catch (EvalError e) {
				System.err.println("Failed to load plugin "+plugin.getName());
				failedPlugins.addAll(thisPluginSignatures);
				
			} catch (NoClassDefFoundError e) {
				System.err.println("Failed to load plugin "+plugin.getName());
				System.err.println("Missing class "+e.getMessage());
				failedPlugins.addAll(thisPluginSignatures);
			}
			workingPlugins.addLast(plugin);
		}
		return workingPlugins;
	}

	private void init() throws ScriptExecutionException {
		String nl = System.getProperty("line.separator");
		int pluginIndex = 0;

		try {
		StringBuffer init = new StringBuffer();
		interpreter = new Interpreter();
		interpreter.set("__main_context", context);
		
		System.out.println("initializing all plugins");
	
    	for (PluginDescriptor plugin : context.getPluginManager().getAllPlugins()) {
    	// skip scanning for working plugins. question: why does scanning for working plugins
    	// take so much more time?
		//for (PluginDescriptor plugin : workingPlugins()) {
			
			if (Boot.VERBOSE == Level.ALL) System.out.println("initializing "+plugin.getName());
			// the right context type is checked at start by the pluginmanager
			// if (plugin.getContextType().isAssignableFrom(context.getClass()))
			// {
			for (int j = 0; j < plugin.getParameterTypes().size(); j++) {
				Signature signature = getSignature(plugin, j);

				if (!availablePlugins.contains(signature)) {
					availablePlugins.add(signature);
					pluginIndex++;

					interpreter.set("__plugin_descriptor" + pluginIndex, plugin);
					interpreter.set("__plugin_method_index" + pluginIndex, j);

					// The following code generates a piece of Java code which declares a Java method.
					// This Java method is a wrapper for the plugin method we are currently initializing.
					// The name of the new Java method is a transcription of the name that was declared in
					// the @PluginVariant{} annotation of the plugin (or the name of the owning @Plugin class).
					// The body of the wrapper method uses ProM's invocation context and plugin descriptor
					// to correctly call the plugin with the given parameters, it also returns the results
					// after the plugin had been executed.

					// A script can then use the transcribed name of the Java plugin to call the plugin.

					// signature of the wrapper method: return type, name, ...
					if (signature.getReturnTypes().size() == 1) {
						init.append(Object.class.getCanonicalName());
					} else {
						init.append(Object[].class.getCanonicalName());
					}
					init.append(" " + signature.getName() + "(");

					// signature of the wrapper method: ... parameters ...
					int index = 0;
					for (Class<?> cl : signature.getParameterTypes()) {
						if (index > 0) {
							init.append(", ");
						}
						init.append(cl.getCanonicalName());
						init.append(" p" + index++);
					}

					// body of the wrapper method: get execution context and invoke plugin
					init.append(") {" + nl);
					init.append("    " + PluginContext.class.getCanonicalName()
							+ " context = __main_context.createChildContext(\"Result of ");
					init.append(signature.getName() + "\");" + nl);

					init.append("    __plugin_descriptor" + pluginIndex + ".invoke(__plugin_method_index"
							+ pluginIndex + ", context, new " + (Object[].class.getCanonicalName()) + " { ");
					for (int i = 0; i < signature.getParameterTypes().size(); i++) {
						if (i > 0) {
							init.append(", ");
						}
						init.append("p" + i);
					}
					init.append(" });" + nl);

					// body of the wrapper method: wait for plugin method to complete and return result
					if (signature.getReturnTypes().size() > 1) {
						init.append("    context.getResult().synchronize();" + nl);
						init.append("    " + Object[].class.getCanonicalName() + " result = new "
								+ Object.class.getCanonicalName() + "[context.getResult().getSize()];" + nl);
						init
						.append("    for (int i = 0; i < result.length; i++) { result[i] = context.getResult().getResult(i); }"
								+ nl);
						init.append("    return result;" + nl);
					} else {
						init.append("    return context.getFutureResult(0).get();" + nl);
					}
					init.append("}" + nl);
					// finished declaring wrapper method 
				}
			} // for each plugin variant
    	} // for each plugin

    	// compile the Java code for all wrapper methods
    	interpreter.eval(init.toString());
				
		} catch (EvalError e) {
			System.err.println("Failed to load one of the plugins.");
			throw new ScriptExecutionException(e);
		} catch (NoClassDefFoundError e) {
			System.err.println("Failed to load one of the plugins.");
			throw new ScriptExecutionException("Missing class "+e.getMessage());
		}
	}

	private Signature getSignature(PluginDescriptor plugin, int index) {
		String name;

		if (plugin.hasAnnotation(CLI.class)) {
			name = plugin.getAnnotation(CLI.class).functionName();
		} else {
			name = plugin.getName();
		}
		return new Signature(plugin.getReturnTypes(), StringUtils.getJavaIdentifier(name), plugin
				.getParameterTypes(index));
	}

	public void bind(String name, Object value) throws ScriptExecutionException {
		try {
			interpreter.set(name, value);
		} catch (EvalError e) {
			throw new ScriptExecutionException(e);
		}
	}

	public List<Signature> getAvailablePlugins() {
		List<Signature> result = new ArrayList<Signature>(availablePlugins);

		Collections.sort(result, new Comparator<Signature>() {
			public int compare(Signature a, Signature b) {
				return a.getName().compareTo(b.getName());
			}
		});
		return result;
	}
}
