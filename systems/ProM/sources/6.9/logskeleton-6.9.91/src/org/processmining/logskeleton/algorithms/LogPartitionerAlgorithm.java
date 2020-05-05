package org.processmining.logskeleton.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;

public class LogPartitionerAlgorithm {

	private void partition(EventLogArray logs, Set<String> positiveFilters, Set<String> negativeFilters, XEventClassifier classifier) {
		XLog log = logs.getLog(logs.getSize() - 1);
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		XLog discardedLog = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());
		for (XTrace trace : log) {
			boolean ok = true;
			Set<String> toMatch = new HashSet<String>(positiveFilters);
			for (XEvent event : trace) {
				String activity = classifier.getClassIdentity(event);
				if (negativeFilters.contains(activity)) {
					ok = false;
					;
				}
				toMatch.remove(activity);
			}
			if (ok && toMatch.isEmpty()) {
				filteredLog.add(trace);
			} else {
				discardedLog.add(trace);
			}
		}
		logs.removeLog(logs.getSize() - 1);

		logs.addLog(filteredLog);
		logs.addLog(discardedLog);
	}

	public EventLogArray apply(XLog log, XEventClassifier classifier) {
		String name = XConceptExtension.instance().extractName(log);
		Set<String> activities = new HashSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				activities.add(classifier.getClassIdentity(event));
			}
		}
		List<String> activityList = new ArrayList<String>(activities);
		Collections.sort(activityList);
		String line= "";
		for (String activity : activityList) {
			line = line + activity + ",";
		}
		line = line + "#";
		System.out.println(line);
		EventLogArray logs = EventLogArrayFactory.createEventLogArray();
		logs.addLog(log);
		Set<String> positiveFilters = new HashSet<String>();
		while (!log.isEmpty()) {
			Map<Set<String>, Double> scores = new HashMap<Set<String>, Double>();
			double maxScore = 0;
			Set<String> negativeFilters = new HashSet<String>();
			for (XTrace trace : log) {
				Set<String> score = new HashSet<String>();
				for (XEvent event : trace) {
					score.add(classifier.getClassIdentity(event));
				}
				if (!scores.containsKey(score)) {
					scores.put(score, 1.0 / (trace.size() + 1));
				} else {
					scores.put(score, scores.get(score) + (1.0 / (trace.size() + 1)));
				}
				if (scores.get(score) > maxScore) {
					maxScore = scores.get(score);
				}
			}
			List<String> bestScore = null;
			XAttributeList list = XFactoryRegistry.instance().currentDefault().createAttributeList("activities", null);
			for (Set<String> score : scores.keySet()) {
				if (bestScore == null && scores.get(score) == maxScore) {
					bestScore = new ArrayList<String>(score);
					Collections.sort(bestScore);
					negativeFilters.addAll(activityList);
					negativeFilters.removeAll(score);
					for (String s : bestScore) {
						list.addToCollection(XFactoryRegistry.instance().currentDefault()
								.createAttributeLiteral(XConceptExtension.KEY_NAME, s, XConceptExtension.instance()));
					}
				}
			}
			partition(logs, positiveFilters, negativeFilters, classifier);
			log = logs.getLog(logs.getSize() - 2);
			XConceptExtension.instance().assignName(log, name + " @" + (logs.getSize() - 1) + " |" + log.size() + "| " + bestScore);
			line= "";
			for (String activity : activityList) {
				if (bestScore.contains(activity)) {
					line = line + "1,";
				} else {
					line = line + "0,";
				}
			}
			line = line + log.size();
			System.out.println(line);
			log.getAttributes().put("activities", list);
			log = logs.getLog(logs.getSize() - 1);
		}
		return logs;
	}

}
