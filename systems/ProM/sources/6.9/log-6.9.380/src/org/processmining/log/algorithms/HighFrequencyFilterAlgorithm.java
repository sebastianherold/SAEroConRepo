package org.processmining.log.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.HighFrequencyFilterParameters;
import org.processmining.log.parameters.LogFilterParameters;

public class HighFrequencyFilterAlgorithm implements LogFilterAlgorithm {

	private Map<Set<List<String>>, Integer> cache;

	public HighFrequencyFilterAlgorithm() {
		cache = new HashMap<Set<List<String>>, Integer>();
	}
	
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

		List<Integer> occurrences = new ArrayList<Integer>(traceOccurrenceMap.values());
		Collections.sort(occurrences);

//		SummaryStatistics statistics = new SummaryStatistics();
//		for (int occurrence : occurrences) {
//			for (int i = 0; i < occurrence; i++) {
//				statistics.addValue(occurrence);
//			}
//		}
//		int threshold = (int) Math.round(statistics.getMean() - 1.0 * statistics.getStandardDeviation());
		int threshold = ((100 - ((HighFrequencyFilterParameters) parameters).getFrequencyThreshold()) * clonedLog
				.size()) / 100;
		int sum = 0;
		int index = -1;
		while (sum < threshold) {
			sum += occurrences.get(++index);
		}
		/*
		 * The 'traces' occurrences[index]...occurrences[occurrences.size()-1]
		 * cover more than X% if the log, where X = parameters.getThreshold().
		 */
		threshold = occurrences.get(index);
		/*
		 * If we take all traces that occur at least as many times as threshold
		 * times, we cover at least X% of the log.
		 */
		if (threshold == occurrences.get(occurrences.size() - 1) + 1) {
			/*
			 * We're about to remove all traces. That seems to be undesirable.
			 */
			threshold--;
		}
		System.out.println("Threshold = " + threshold);

		Collection<XTrace> tracesToRemove = new HashSet<XTrace>();

		for (XTrace trace : clonedLog) {
			if (traceOccurrenceMap.get(traceActivitiesMap.get(trace)) >= threshold) {
				/*
				 * Trace occurs often enough. Filter it in.
				 */
			} else {
				/*
				 * Trace does not occur often enough by itself. Check whether it
				 * matches one that does.
				 */
				boolean retain = false;
				for (XTrace otherTrace : clonedLog) {
					if (traceOccurrenceMap.get(traceActivitiesMap.get(otherTrace)) >= threshold) {
						if (getTraceDistance(traceActivitiesMap.get(trace), traceActivitiesMap.get(otherTrace)) < ((HighFrequencyFilterParameters) parameters)
								.getDistanceThreshold()) {
							/*
							 * Yes, it matches one that does. Filter this trace
							 * in as well.
							 */
							retain = true;
							continue;
						}
					}
				}
				if (!retain) {
					/*
					 * This trace does not occur frequent enough, an dit does
					 * not match any other traces that does. Filter it out.
					 */
					tracesToRemove.add(trace);
				}
			}
		}

		clonedLog.removeAll(tracesToRemove);
		/*
		 * At least X% of the log is retained.
		 */

		return clonedLog;
	}

	private int getTraceDistance(List<String> activities1, List<String> activities2) {
		return getTraceDistance(activities1, activities2, 0, 0);
	}

	private int getTraceDistance(List<String> activities1, List<String> activities2, int index1, int index2) {
		int distance = 0;
		if (index1 >= activities1.size()) {
			distance = activities2.size() - index2;
		} else if (index2 >= activities2.size()) {
			distance = activities1.size() - index1;
		} else {
			Set<List<String>> activities = new HashSet<List<String>>();
			activities.add(activities1);
			activities.add(activities2);
			if (cache.containsKey(activities)) {
				distance = cache.get(activities);
			} else {
				if (activities1.get(index1).equals(activities2.get(index2))) {
					distance = getTraceDistance(activities1, activities2, index1 + 1, index2 + 1);
				} else {
					int distance1 = 1 + getTraceDistance(activities1, activities2, index1 + 1, index2);
					int distance2 = 1 + getTraceDistance(activities1, activities2, index1, index2 + 1);
					if (distance1 < distance2) {
						distance = distance1;
					} else {
						distance = distance2;
					}
				}
				cache.put(activities, distance);
			}
		}
		return distance;
	}
}
