package org.processmining.plugins.log;

import java.io.InputStream;

import org.deckfour.xes.factory.XFactoryBufferedImpl;
import org.processmining.framework.plugin.PluginContext;

//@Plugin(name = "Open XES Log File (Buffered)", parameterLabels = { "Filename" }, returnLabels = { "Log (single process)" }, returnTypes = { XLog.class })
//@UIImportPlugin(description = "ProM log files (Buffered)", extensions = { "mxml", "xml", "gz", "zip", "xes", "xez" })
public class OpenBufferedLogFilePlugin extends OpenLogFilePlugin {
	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
	throws Exception {
		return importFromStream(context, input, filename, fileSizeInBytes, new XFactoryBufferedImpl());
	}
}
