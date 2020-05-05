package org.processmining.plugins.loginsertstart;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * 
 * Insert Start Visualizer Class
 * 
 * Visualise the edited log
 * 
 * @author jnakatumba
 * 
 */

@Plugin(name = "Log visualizer", returnLabels = { "Visualized Log" }, returnTypes = { XLog.class }, parameterLabels = "log")
@Visualizer
public class AddStVisualizer {

	private XLog xlog;

	public XLog setLog(XLog log) {
		xlog = log;
		return xlog;

	}

	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Default Visualization")
	public static XLog open(PluginContext context, AddStVisualizer output) throws Exception {
		return output.xlog;
	}

}
