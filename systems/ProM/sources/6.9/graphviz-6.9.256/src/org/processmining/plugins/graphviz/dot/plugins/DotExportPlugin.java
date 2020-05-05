package org.processmining.plugins.graphviz.dot.plugins;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;

/**
 * Exports a Dot object
 * 
 * @author F. Mannhardt
 *
 */
@Plugin(name = "Export Dot", returnLabels = {}, returnTypes = {}, level = PluginLevel.Regular, parameterLabels = {
		"Dot", "File" }, userAccessible = true)
@UIExportPlugin(description = "Export Dot", extension = "dot")
public final class DotExportPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Dot File")
	public void export(PluginContext context, Dot dot, File file) throws IOException {
		try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file))) {
			fileWriter.write(dot.toString());
		}
	}
}