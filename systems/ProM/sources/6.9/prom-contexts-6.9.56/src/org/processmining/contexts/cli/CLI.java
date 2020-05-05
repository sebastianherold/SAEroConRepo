package org.processmining.contexts.cli;

import jargs.gnu.CmdLineParser;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.processmining.contexts.scripting.ScriptExecutor;
import org.processmining.contexts.scripting.ScriptExecutor.ScriptExecutionException;
import org.processmining.contexts.scripting.Signature;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.boot.Boot.Level;
import org.processmining.framework.plugin.annotations.Bootable;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.AutoHelpCommandLineParser;
import org.processmining.framework.util.CommandLineArgumentList;
import org.processmining.framework.util.Pair;

public class CLI {
	@Plugin(name = "CLI", parameterLabels = {}, returnLabels = {}, returnTypes = {}, userAccessible = false)
	@Bootable
	public Object main(CommandLineArgumentList commandlineArguments) throws Throwable {
		//try {
		if (Boot.VERBOSE != Level.NONE) {
			System.out.println("Starting script execution engine...");
			System.out.println(commandlineArguments);
		}

		CLIContext globalContext = new CLIContext();
		ScriptExecutor executor = new ScriptExecutor(globalContext.getMainPluginContext());
		Pair<List<String>, List<String>> params = parseCommandLine(commandlineArguments, executor);

		if (params != null) {
			List<String> scripts = params.getFirst();
			List<String> scriptArguments = params.getSecond();
			try {
				executor.bind("arguments", scriptArguments);

				for (String script : scripts) {
					executor.execute(script);
				}
			} catch (ScriptExecutionException e) {
				System.err.println("Error while executing '"+commandlineArguments+"'");
				System.err.println(e);
				throw e;
			}
		}
		//} catch (Throwable t) {
		//	t.printStackTrace();
		//	System.err.println(t);
			// System.exit(1);
		//}
		/*
		 * try { File f = new File(OsUtil.getProMUserDirectory(),
		 * "testrepo.xml"); Repository repo = new Repository(f.toURI().toURL());
		 * PackageManager.getInstance().addRepository(repo);
		 * 
		 * PackageManager.getInstance().update();
		 * 
		 * PackageDescriptor toInstall = null; System.out.println("Enabled:");
		 * for (PackageDescriptor pack :
		 * PackageManager.getInstance().getEnabledPackages()) {
		 * System.out.println("  -  " + pack); }
		 * System.out.println("Installed:"); for (PackageDescriptor pack :
		 * PackageManager.getInstance().getInstalledPackages()) {
		 * System.out.println("  -  " + pack); } System.out.println("Latest:");
		 * for (PackageDescriptor pack :
		 * PackageManager.getInstance().getLatestPackages()) {
		 * System.out.println("  - " + pack); if
		 * (pack.getName().equals("holub")) { toInstall = pack; } }
		 * PackageManager.getInstance().installOrUpdate(toInstall);
		 * PackageManager.getInstance().uninstall(toInstall);
		 * System.out.println("Done.");
		 * System.out.println(PackageManager.getInstance()); } catch (Exception
		 * e) { e.printStackTrace(); }
		 */
		// System.exit(0);
		return null;
	}

	private Pair<List<String>, List<String>> parseCommandLine(CommandLineArgumentList arguments, ScriptExecutor executor)
			throws IOException {
		AutoHelpCommandLineParser parser = new AutoHelpCommandLineParser("java " + this.getClass().getCanonicalName());

		CmdLineParser.Option helpOption = parser.addHelp(parser.addBooleanOption('h', "help"),
				"Print this help message and exit");
		CmdLineParser.Option listOption = parser.addHelp(parser.addBooleanOption('l', "list"),
				"List all available plugins on standard output and exit");
		CmdLineParser.Option stdinOption = parser.addHelp(parser.addBooleanOption('i', "stdin"),
				"Read script from standard input");
		CmdLineParser.Option scriptOption = parser.addHelp(parser.addStringOption('s', "script"),
				"Execute the script given on the command line");
		CmdLineParser.Option fileOption = parser.addHelp(parser.addStringOption('f', "file"),
				"Read script from the given file");

		try {
			parser.parse(arguments.toStringArray());
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			parser.printUsage();
			return null;
		}

		if ((Boolean) parser.getOptionValue(listOption, Boolean.FALSE)) {
			for (Signature plugin : executor.getAvailablePlugins()) {
				System.out.println(plugin.toString(25, 1));
			}
			return null;
		}
		if ((Boolean) parser.getOptionValue(helpOption, Boolean.FALSE)) {
			parser.printUsage();
			return null;
		}

		List<String> scripts = new ArrayList<String>();
		if ((Boolean) parser.getOptionValue(stdinOption, Boolean.FALSE)) {
			scripts.add(readFromStdin());
		}
		for (Object script : parser.getOptionValues(scriptOption)) {
			scripts.add(script.toString());
		}
		for (Object scriptFile : parser.getOptionValues(fileOption)) {
			scripts.add(readFile(scriptFile.toString()));
		}

		return new Pair<List<String>, List<String>>(scripts, new ArrayList<String>(Arrays.asList(parser
				.getRemainingArgs())));
	}

	public static String readFile(String scriptFile) throws IOException {
		InputStream is = new FileInputStream(scriptFile);
		String result = readWholeStream(is);
		is.close();
		return result;
	}

	private static String readFromStdin() throws IOException {
		return readWholeStream(System.in);
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
	    Boot.boot(CLI.class, CLIPluginContext.class, args);
	  } catch (InvocationTargetException e) {
	    throw e.getCause();
	  }
	}
}
