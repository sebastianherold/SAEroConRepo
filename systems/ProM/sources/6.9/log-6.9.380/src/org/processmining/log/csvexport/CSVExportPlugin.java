package org.processmining.log.csvexport;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.csv.CSVFile;

import com.google.common.io.Files;

/**
 * Exports a CSVFile
 * 
 * @author F. Mannhardt
 *
 */
@Plugin(name = "Export CSV", returnLabels = {}, returnTypes = {},
		level = PluginLevel.Regular, parameterLabels = { "CSVFile", "File" }, userAccessible = true)
@UIExportPlugin(description = "Export CSV", extension = "csv")
public final class CSVExportPlugin  {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export CSV File")
	public void export(PluginContext context, CSVFile csvFile, File file) throws IOException {
		Files.copy(csvFile.getFile().toFile(), file);
	}
}
