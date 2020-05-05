package org.processmining.plugins.graphviz.visualisation;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.graphviz.dot.Dot;

@Plugin(name = "Dot visualisation", returnLabels = { "Dot visualisation" }, returnTypes = { JComponent.class }, parameterLabels = { "Dot" }, userAccessible = true, level = PluginLevel.Regular)
@Visualizer
public class DotVisualisation {

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Dot dot) {

		return new DotPanel(dot);
	}
}
