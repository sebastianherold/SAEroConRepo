package org.processmining.log.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.LogFrequency;
import org.processmining.log.models.impl.LogFrequencyFactory;
import org.processmining.log.parameters.LogFrequencyParameters;

public class LogFrequencyAlgorithm {

	public LogFrequency apply(PluginContext context, XLog log, LogFrequencyParameters parameters) {
		LogFrequency frequency = LogFrequencyFactory.createLogFrequency(log);
		final Map<List<String>, Integer> traceOccurrenceMap = new HashMap<List<String>, Integer>();

		for (XTrace trace : log) {
			List<String> activities = new ArrayList<String>();
			for (XEvent event : trace) {
				activities.add(parameters.getClassifier().getClassIdentity(event));
			}
			if (traceOccurrenceMap.keySet().contains(activities)) {
				traceOccurrenceMap.put(activities, traceOccurrenceMap.get(activities) + 1);
			} else {
				traceOccurrenceMap.put(activities, 1);
			}
		}

		for (List<String> activities : traceOccurrenceMap.keySet()) {
			frequency.add(traceOccurrenceMap.get(activities));
		}
		return frequency;
	}
}
