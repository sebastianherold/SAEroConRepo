package org.processmining.log.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityFilterParameters;

public class LogCentralityFilterAlgorithm {

	public XLog apply(PluginContext context, LogCentrality centrality, LogCentralityFilterParameters parameters) {
		context.getProgress().setMaximum(centrality.size());
		return centrality.filter(context, parameters);
	}
}
