package org.processmining.logskeleton.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public class LogSkeletonCount {

	/*
	 * The names to use for the start and end events.
	 */
	public final static String STARTEVENT = "|>";
	public final static String ENDEVENT = "[]";

	/*
	 * Counts how many times an activity occurred in the entire log. 
	 * If activityCounts.get(a) == 4, then activity a occurred 4 times in the log.
	 */
	private Map<String, Integer> activityCounts;
	
	/*
	 * Counts the minimal number of times an activity occurs in any trace. 
	 * If activityMinCounts.get(a) == 1, then a occurs at least once in every trace in the log.
	 */
	private Map<String, Integer> activityMinCounts;
	
	/*
	 * Counts the maximal number of times an activity occurs in any trace. 
	 * If activityMinCounts.get(a) == 2, then a occurs at most twice in every trace in the log.
	 */
	private Map<String, Integer> activityMaxCounts;
	
	/*
	 * The directly follows relation. If ab is a list containing first a and then b, and if transitionCounts.get(ab) == 5, 
	 * then a was 5 times directly followed by b in the log. Note that the lists (like ab) have always length 2. 
	 */
	private Map<List<String>, Integer> transitionCounts;

	public LogSkeletonCount() {
		activityCounts = new HashMap<String, Integer>();
		activityMinCounts = new HashMap<String, Integer>();
		activityMaxCounts = new HashMap<String, Integer>();
		transitionCounts = new HashMap<List<String>, Integer>();
	}

	public boolean checkTransitionCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		for (List<String> transition : model.transitionCounts.keySet()) {
			if (!transitionCounts.keySet().contains(transition)) {
				messages.add("[LogSkeletonCount] Case " + caseId + ": Next fails for " + transition);
				return false;
			}
			if (transitionCounts.get(transition) < model.transitionCounts.get(transition)) {
				messages.add("[LogSkeletonCount] Case " + caseId + ": Next fails for " + transition);
				return false;
			}
		}
		return true;
	}

	public Integer get(String activity) {
		return activityCounts.containsKey(activity) ? activityCounts.get(activity) : 0;
	}

	public Integer getMin(String activity) {
		return activityMinCounts.containsKey(activity) ? activityMinCounts.get(activity) : 0;
	}

	public Integer getMax(String activity) {
		return activityMaxCounts.containsKey(activity) ? activityMaxCounts.get(activity) : 0;
	}

	public Integer get(String fromActivity, String toActivity) {
		List<String> transition = getTransition(fromActivity, toActivity);
		return transitionCounts.containsKey(transition) ? transitionCounts.get(transition) : 0;
	}

	public Collection<String> getTo(String fromActivity) {
		Collection<String> toActivities = new HashSet<String>();
		for (List<String> transition : transitionCounts.keySet()) {
			if (transition.get(0).equals(fromActivity)) {
				toActivities.add(transition.get(1));
			}
		}
		return toActivities;
	}

	public Collection<String> getFrom(String toActivity) {
		Collection<String> fromActivities = new HashSet<String>();
		for (List<String> transition : transitionCounts.keySet()) {
			if (transition.get(1).equals(toActivity)) {
				fromActivities.add(transition.get(0));
			}
		}
		return fromActivities;
	}

	public void add(String activity, Integer number) {
		if (activityCounts.containsKey(activity)) {
			activityCounts.put(activity, activityCounts.get(activity) + number);
		} else {
			activityCounts.put(activity, number);
		}
	}

	public void add(String fromActivity, String toActivity, Integer number) {
		List<String> transition = getTransition(fromActivity, toActivity);
		if (transitionCounts.containsKey(transition)) {
			transitionCounts.put(transition, transitionCounts.get(transition) + number);
		} else {
			transitionCounts.put(transition, number);
		}
	}

	private Map<String, Integer> traceActivities = new HashMap<String, Integer>();

	private void updateMinMax() {
		if (!activityMinCounts.isEmpty()) {
			for (String activity : traceActivities.keySet()) {
				if (!activityMinCounts.containsKey(activity)) {
					activityMinCounts.put(activity, 0);
				}
			}
			for (String activity : activityMinCounts.keySet()) {
				if (!traceActivities.containsKey(activity)) {
					activityMinCounts.put(activity, 0);
				}
			}
		}
		for (String activity : traceActivities.keySet()) {
			if (activityMinCounts.containsKey(activity)) {
				activityMinCounts.put(activity,
						Math.min(activityMinCounts.get(activity), traceActivities.get(activity)));
			} else {
				activityMinCounts.put(activity, traceActivities.get(activity));
			}
			if (activityMaxCounts.containsKey(activity)) {
				activityMaxCounts.put(activity,
						Math.max(activityMaxCounts.get(activity), traceActivities.get(activity)));
			} else {
				activityMaxCounts.put(activity, traceActivities.get(activity));

			}
		}
		traceActivities.clear();
	}

	public void inc(String activity) {
		add(activity, 1);
		if (traceActivities.containsKey(activity)) {
			traceActivities.put(activity, traceActivities.get(activity) + 1);
		} else {
			traceActivities.put(activity, 1);
		}
		if (activity.equals(ENDEVENT)) {
			updateMinMax();
		}
	}

	public void inc(String fromActivity, String toActivity) {
		add(fromActivity, toActivity, 1);
	}

	public Collection<String> getActivities() {
		List<String> ordered = new ArrayList<String>(activityCounts.keySet());
		Collections.sort(ordered);
		return ordered;
	}

	private List<String> getTransition(String fromActivity, String toActivity) {
		List<String> transition = new ArrayList<String>(2);
		transition.add(0, fromActivity);
		transition.add(1, toActivity);
		return transition;
	}

	public void print(String name) {
		//		System.out.println("[PDC2017CountModel] Activity counts for " + name);
		for (String activity : activityCounts.keySet()) {
			//			System.out.println("[LogSkeletonCount] " + activity + ": " + activityCounts.get(activity));
		}
		//		System.out.println("[PC2017CountModel] Transitions counts for " + name);
		for (List<String> transition : transitionCounts.keySet()) {
			//			System.out.println("[LogSkeletonCount] " + transition + ": " + transitionCounts.get(transition));
		}
	}

	public void exportToFile(CsvWriter writer) throws IOException {
		writer.write("activity counts");
		writer.write("" + activityCounts.keySet().size());
		writer.endRecord();
		for (String activity : activityCounts.keySet()) {
			writer.write(activity);
			writer.write("" + activityCounts.get(activity));
			writer.write("" + activityMinCounts.get(activity));
			writer.write("" + activityMaxCounts.get(activity));
			writer.endRecord();
		}
		writer.write("transition counts");
		writer.write("" + transitionCounts.keySet().size());
		writer.endRecord();
		for (List<String> transitionList : transitionCounts.keySet()) {
			for (String transition : transitionList) {
				writer.write(transition);
			}
			writer.write("" + transitionCounts.get(transitionList));
			writer.endRecord();
		}
	}

	public void importFromStream(CsvReader reader) throws IOException {
		activityCounts = new HashMap<String, Integer>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("activity counts")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						activityCounts.put(reader.get(0), Integer.valueOf(reader.get(1)));
						activityMinCounts.put(reader.get(0), Integer.valueOf(reader.get(2)));
						activityMaxCounts.put(reader.get(0), Integer.valueOf(reader.get(3)));
					}
				}
			}
		}
		transitionCounts = new HashMap<List<String>, Integer>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("transition counts")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						List<String> transitions = new ArrayList<String>();
						for (int column = 0; column < reader.getColumnCount() - 1; column++) {
							transitions.add(reader.get(column));
						}
						transitionCounts.put(transitions, Integer.valueOf(reader.get(reader.getColumnCount() - 1)));
					}
				}
			}
		}
	}
}
