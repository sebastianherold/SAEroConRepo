package org.processmining.log.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.LogFilterParameters;

public interface LogFilterAlgorithm {
	
	public XLog apply(PluginContext context, XLog log, LogFilterParameters parameters);

}
