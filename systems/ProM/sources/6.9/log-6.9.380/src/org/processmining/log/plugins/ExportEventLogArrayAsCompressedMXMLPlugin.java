package org.processmining.log.plugins;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.out.XMxmlGZIPSerializer;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;

@Plugin(name = "ELA export (Event Log Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Event Log Array (Compressed MXML)", "File" }, userAccessible = true)
@UIExportPlugin(description = "Event Log Array (Compressed MXML)", extension = "ela")
public class ExportEventLogArrayAsCompressedMXMLPlugin {

	@PluginVariant(variantLabel = "ELA export to compressed MXML files (Event Log Array)", requiredParameterLabels = { 0, 1 })
	public void exportMxmlGz(PluginContext context, EventLogArray eventLogs, File file) throws IOException {
		eventLogs.exportToFile(context, file, new XMxmlGZIPSerializer());
	}

}
