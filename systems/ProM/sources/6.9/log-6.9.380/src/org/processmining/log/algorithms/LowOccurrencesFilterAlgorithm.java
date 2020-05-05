package org.processmining.log.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.LogFilterParameters;
import org.processmining.log.parameters.LowOccurrencesFilterParameters;

public class LowOccurrencesFilterAlgorithm implements LogFilterAlgorithm {

	public XLog apply(PluginContext context, XLog log, LogFilterParameters parameters) {
		final Map<List<String>, Integer> traceOccurrenceMap = new HashMap<List<String>, Integer>();
		final Map<XTrace, List<String>> traceActivitiesMap = new HashMap<XTrace, List<String>>();
		XLog clonedLog = (XLog) log.clone();

		for (XTrace trace : clonedLog) {
			List<String> activities = new ArrayList<String>();
			for (XEvent event : trace) {
				activities.add(parameters.getClassifier().getClassIdentity(event));
			}
			traceActivitiesMap.put(trace, activities);
			if (traceOccurrenceMap.keySet().contains(activities)) {
				traceOccurrenceMap.put(activities, traceOccurrenceMap.get(activities) + 1);
			} else {
				traceOccurrenceMap.put(activities, 1);
			}
		}

		Collection<XTrace> tracesToRemove = new HashSet<XTrace>();

		for (XTrace trace : clonedLog) {
			if (traceOccurrenceMap.get(traceActivitiesMap.get(trace)) < ((LowOccurrencesFilterParameters) parameters).getThreshold()) {
				/*
				 * Trace does not occur often enough. Have it removed.
				 */
				tracesToRemove.add(trace);
			}
		}

		clonedLog.removeAll(tracesToRemove);

		return clonedLog;
	}

}
