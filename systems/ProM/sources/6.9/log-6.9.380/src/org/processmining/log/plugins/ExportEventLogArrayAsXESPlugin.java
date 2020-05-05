package org.processmining.log.plugins;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.out.XesXmlSerializer;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;

@Plugin(name = "ELA export (Event Log Array)", returnLabels = {}, returnTypes = {}, parameterLabels = {
		"Event Log Array (XES)", "File" }, userAccessible = true)
@UIExportPlugin(description = "Event Log Array (XES)", extension = "ela")
public class ExportEventLogArrayAsXESPlugin {

	@PluginVariant(variantLabel = "ELA export to XES files (Event Log Array)", requiredParameterLabels = { 0, 1 })
	public void exportXesGz(PluginContext context, EventLogArray eventLogs, File file) throws IOException {
		eventLogs.exportToFile(context, file, new XesXmlSerializer());
	}

}
