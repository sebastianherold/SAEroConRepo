package org.processmining.log.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.parameters.LogCentralityFilterParameters;
import org.processmining.log.parameters.LogCentralityParameters;

public class LogCentrality {

	private XLog log;
	private XEventClassifier classifier;
	private Map<List<String>, Double> centralityMap;
	private List<Double> centralities;
	private Map<Set<List<String>>, Integer> cache;

	public LogCentrality(XLog log) {
		this.log = log;
		this.classifier = null;
		this.cache = null;
	}

	public XLog getLog() {
		return log;
	}
	
	public XEventClassifier getClassifier() {
		return classifier;
	}
	
	public List<Double> getCentralities() {
		return centralities;
	}
	
	public void setClassifier(PluginContext context, LogCentralityParameters parameters) {
		if (context != null) {
			context.getProgress().setValue(0);
		}
		if (this.classifier == null || !this.classifier.equals(parameters.getClassifier())) {
			this.classifier = parameters.getClassifier();
			this.centralityMap = new HashMap<List<String>, Double>();
			this.centralities = new ArrayList<Double>();
			this.cache = new HashMap<Set<List<String>>, Integer>();

			for (XTrace trace : log) {
				List<String> traceId = getTraceId(trace, classifier);
				if (!centralityMap.containsKey(traceId)) {
					centralityMap.put(traceId, getTraceCentrality(trace, log, classifier));
				}
				centralities.add(centralityMap.get(traceId));
				if (context != null) {
					context.getProgress().inc();
				}
			}
			Collections.sort(centralities);
			this.cache = null;
		}
	}

	public int size() {
		return log.size();
	}
	
	public XLog filter(PluginContext context, LogCentralityFilterParameters parameters) {
		XLog log = (XLog) this.log.clone();
		int index = (parameters.getPercentage() * centralities.size()) / 100;
		if (index >= centralities.size()) {
			index = centralities.size() - 1;
		}
		Double threshold = centralities.get(index);
		Set<XTrace> removedTraces = new HashSet<XTrace>();
		for (XTrace trace : log) {
			if (parameters.isFilterIn() == (centralityMap.get(getTraceId(trace, classifier)) > threshold)) {
				removedTraces.add(trace);
			}
			if (context != null) {
				context.getProgress().inc();
			}
		}
		for (XTrace trace : removedTraces) {
			log.remove(trace);
		}
		return log;
	}

	private List<String> getTraceId(XTrace trace, XEventClassifier classifier) {
		List<String> traceId = new ArrayList<String>();
		for (XEvent event : trace) {
			traceId.add(classifier.getClassIdentity(event));
		}
		return traceId;
	}

	private double getTraceCentrality(XTrace trace, XLog log, XEventClassifier classifier) {
		int totalSquareDistance = 0;
		for (XTrace otherTrace : log) {
			int distance = getTraceDistance(trace, otherTrace, classifier);
			totalSquareDistance += (distance * distance);
		}
		return Math.sqrt(totalSquareDistance);
	}

	private int getTraceDistance(XTrace trace1, XTrace trace2, XEventClassifier classifier) {
		List<String> activities1 = new ArrayList<String>();
		List<String> activities2 = new ArrayList<String>();
		for (XEvent event : trace1) {
			activities1.add(classifier.getClassIdentity(event));
		}
		for (XEvent event : trace2) {
			activities2.add(classifier.getClassIdentity(event));
		}
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
