package org.processmining.log.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.LogFilterParameters;
import org.processmining.log.parameters.LowFrequencyFilterParameters;

public class LowFrequencyFilterAlgorithm implements LogFilterAlgorithm {

	public XLog apply(PluginContext context, XLog log, LogFilterParameters parameters) {
		final Map<List<String>, Integer> traceOccurrenceMap = new HashMap<List<String>, Integer>();
		final Map<XTrace, List<String>> traceActivitiesMap = new HashMap<XTrace, List<String>>();
		
		XLog clonedLog = (XLog) log.clone();
		
		for (XTrace trace : clonedLog) {
			List<String> activities = new ArrayList<String>();
			for (XEvent event : trace) {
				activities.add(parameters.getClassifier().getClassIdentity(event));
			}
			traceActivitiesMap.put(trace,  activities);
			if (traceOccurrenceMap.keySet().contains(activities)) {
				traceOccurrenceMap.put(activities, traceOccurrenceMap.get(activities) + 1);
			} else {
				traceOccurrenceMap.put(activities, 1);
			}
		}
		
		List<Integer> occurrences = new ArrayList<Integer>(traceOccurrenceMap.values());
		Collections.sort(occurrences);
		int threshold = (((LowFrequencyFilterParameters) parameters).getThreshold() * clonedLog.size()) / 100;
		int sum = 0;
		int index = -1;
		while (sum < threshold) {
			sum += occurrences.get(++index);
		}
		/*
		 * The low-frequency 'traces' (occurrences[0]...occurrences[index]) counted so far make up for X% of the log, 
		 * where X = parameters.getThreshold().
		 */
		threshold = (index == -1 ? 0 : occurrences.get(index)) + 1;
		/*
		 * If we take all traces that occur fewer than threshold times, we cover at least X% of the log.
		 */
		
		if (threshold == occurrences.get(occurrences.size() - 1) + 1) {
			/*
			 * We're about to remove all traces. That seems to be undesirable.
			 */
			threshold--;
		}
		
		Collection<XTrace> tracesToRemove = new HashSet<XTrace>();

		for (XTrace trace : clonedLog) {
			/*
			 * Trace does not occur often enough. Have it removed.
			 */
			if (traceOccurrenceMap.get(traceActivitiesMap.get(trace)) < threshold) {
				tracesToRemove.add(trace);
			}
		}
		
		/*
		 * tracesToRemove holds at least X% of the log.
		 */
		clonedLog.removeAll(tracesToRemove);
		
		return clonedLog;
	}

}
