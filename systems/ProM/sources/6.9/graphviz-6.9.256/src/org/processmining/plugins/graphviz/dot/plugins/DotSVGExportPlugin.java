package org.processmining.plugins.graphviz.dot.plugins;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.export.ExporterSVG;

/**
 * Exports a Dot object
 * 
 * @author F. Mannhardt
 *
 */
@Plugin(name = "Export Dot as SVG", returnLabels = {}, returnTypes = {}, level = PluginLevel.Regular, parameterLabels = {
		"Dot", "File" }, userAccessible = true)
@UIExportPlugin(description = "Export SVG", extension = "svg")
public final class DotSVGExportPlugin extends DotImageExportPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Export Dot as SVG")
	public void exportAsSVG(PluginContext context, Dot dot, File file) throws IOException {
		export(dot, file, new ExporterSVG());
	}
}