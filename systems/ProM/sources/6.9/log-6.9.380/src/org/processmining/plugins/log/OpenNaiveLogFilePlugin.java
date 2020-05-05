package org.processmining.plugins.log;

import java.io.InputStream;

import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;

@Plugin(name = "Open XES Log File (Naive)", level = PluginLevel.PeerReviewed, parameterLabels = { "Filename" }, returnLabels = { "Log (single process)" }, returnTypes = { XLog.class })
@UIImportPlugin(description = "ProM log files (Naive)", extensions = { "mxml", "xml", "gz", "zip", "xes", "xez" })
public class OpenNaiveLogFilePlugin extends OpenLogFilePlugin {
	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
	throws Exception {
		return importFromStream(context, input, filename, fileSizeInBytes, new XFactoryNaiveImpl());
	}
}
