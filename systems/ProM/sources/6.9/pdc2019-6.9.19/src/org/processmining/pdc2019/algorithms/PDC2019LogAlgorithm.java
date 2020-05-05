package org.processmining.pdc2019.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.pdc2019.parameters.PDC2019Parameters;
import org.processmining.plugins.log.OpenLogFilePlugin;

public class PDC2019LogAlgorithm {

	private static ImportPlugin logImporter = new OpenLogFilePlugin();

	private Map<String, XLog> loadedLogs;

	public PDC2019LogAlgorithm() {
		loadedLogs = new HashMap<String, XLog>();
	}

	public XLog apply(PluginContext context, PDC2019Parameters parameters) throws Exception {
		String fileName = parameters.getSet().getFileName(parameters.getNr());
		if (!loadedLogs.containsKey(fileName)) {
			System.out.println("[PDC2019LogAlgorithm] Loading log from file " + "pdc2019logs/" + fileName + ".xes");
			InputStream logFile = ClassLoader.getSystemClassLoader().getResourceAsStream(
					"pdc2019logs/" + fileName + ".xes");
			File tmpFile = File.createTempFile("tmp" + fileName, ".xes");
			BufferedReader br = new BufferedReader(new InputStreamReader(logFile));
			String line = br.readLine();

			PrintStream out = new PrintStream(tmpFile);
			while (line != null) {
				out.println(line);
				line = br.readLine();
			}
			out.flush();
			out.close();
			loadedLogs.put(fileName, (XLog) logImporter.importFile(context, tmpFile.getAbsolutePath()));
		}
		return loadedLogs.get(fileName);
	}
}
