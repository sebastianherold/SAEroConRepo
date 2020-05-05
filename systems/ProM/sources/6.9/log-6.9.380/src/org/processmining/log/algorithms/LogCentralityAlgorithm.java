package org.processmining.log.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityParameters;

public class LogCentralityAlgorithm {

	public LogCentrality apply(PluginContext context, XLog log, LogCentrality centrality, LogCentralityParameters parameters) {
		context.getProgress().setMaximum(log.size());
		centrality.setClassifier(context, parameters);
		return centrality;
	}
}
