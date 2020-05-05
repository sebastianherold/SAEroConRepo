package org.processmining.log.plugins;

import javax.swing.JComponent;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.log.algorithms.LogCentralityVisualizerAlgorithm;
import org.processmining.log.models.LogCentrality;

public class LogCentralityVisualizerPlugin extends LogCentralityVisualizerAlgorithm {

	@Plugin(name = "Log Summary", parameterLabels = "Happifiable Log", returnTypes = JComponent.class, returnLabels = "Log Visualization", userAccessible = false, mostSignificantResult = 1, help = "Provides an overview of the centralized log")
	@Visualizer(name = "Log Summary")
	public JComponent visualizeLog(PluginContext context, LogCentrality centrality) throws ConnectionCannotBeObtained {
		XLogInfo info = XLogInfoFactory.createLogInfo(centrality.getLog(), centrality.getClassifier());
		JComponent component = context.tryToFindOrConstructFirstNamedObject(JComponent.class, "  Log Summary", null,
				null, centrality.getLog(), info);
		return component;
	}

	@Plugin(name = "Trace Happiness", parameterLabels = "Happifiable Log", returnTypes = JComponent.class, returnLabels = "Log Visualization", userAccessible = false, mostSignificantResult = 1, help = "Provides an overview of the centralized log")
	@Visualizer(name = "Trace Happiness Visualizer")
	public JComponent visualize(PluginContext context, LogCentrality centrality) {
		return apply(centrality, null);
	}

}
