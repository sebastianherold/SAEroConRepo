package org.processmining.logskeleton.models;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.annotations.AuthoredType;
import org.processmining.framework.annotations.Icon;
import org.processmining.framework.util.HTMLToString;
import org.processmining.logskeleton.parameters.LogSkeletonBrowser;
import org.processmining.logskeleton.parameters.LogSkeletonBrowserParameters;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotNode;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

@AuthoredType(typeName = "Log skeleton", affiliation = AuthoredType.TUE, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
@Icon(icon = "rotule_30x35.png")
public class LogSkeleton implements HTMLToString {

	/*
	 * Holds the counters and the directly-follows relation.
	 */
	private LogSkeletonCount countModel;

	/*
	 * The equivalence relation. If S is an element of sameCounts, then all
	 * elements of S are equivalent.
	 */
	private Collection<Collection<String>> sameCounts;
	private Map<Integer, Collection<Collection<String>>> sameCountsNoise;

	/*
	 * The precedence relation. If precedence.get(a).contains(b), then if a
	 * occurs, some b must occur before.
	 */
	private Map<String, ThresholdSet> precedences;

	/*
	 * The response relation. If response.get(a).contains(b), then if a occurs,
	 * some b must occur after.
	 */
	private Map<String, ThresholdSet> responses;

	private Map<String, ThresholdSet> notPrecedences;
	private Map<String, ThresholdSet> notResponses;

	/*
	 * The not co-existence relation. If notCoExistence.get(a).contains(b), then
	 * if a occurs, b may not occur (before or after).
	 */
	private Map<String, ThresholdSet> notCoExistences;

	private Set<String> required;
	private Set<String> forbidden;
	private List<List<String>> splitters;
	private String label;

	private int equivalenceThreshold;
	private int precedenceThreshold;
	private int responseThreshold;
	private int notCoExistenceeThreshold;

	//	private Map<List<String>, List<Integer>> distances;

	public LogSkeleton() {
		this(new LogSkeletonCount());
	}

	@SuppressWarnings("unchecked")
	public LogSkeleton(LogSkeletonCount countModel) {
		this.countModel = countModel;
		//		sameCounts = new HashSet<Collection<String>>();
		sameCountsNoise = new HashMap<Integer, Collection<Collection<String>>>();
		for (int noiseLevel = 0; noiseLevel < 21; noiseLevel++) {
			sameCountsNoise.put(noiseLevel, new HashSet<Collection<String>>());
		}
		sameCounts = sameCountsNoise.get(0);
		precedences = new HashMap<String, ThresholdSet>();
		responses = new HashMap<String, ThresholdSet>();
		notPrecedences = new HashMap<String, ThresholdSet>();
		notResponses = new HashMap<String, ThresholdSet>();
		notCoExistences = new HashMap<String, ThresholdSet>();
		required = new HashSet<String>();
		forbidden = new HashSet<String>();
		splitters = new ArrayList<List<String>>();
		label = null;
		setEquivalenceThreshold(100);
		setPrecedenceThreshold(100);
		setResponseThreshold(100);
		setNotCoExistenceThreshold(100);
	}

	public void addSameCount(Collection<String> activities) {
		List<String> orderedActivities = new ArrayList<String>(activities);
		Collections.sort(orderedActivities);
		sameCounts.add(orderedActivities);
	}

	public void addSameCount(int noiseLevel, Collection<String> activities) {
		List<String> orderedActivities = new ArrayList<String>(activities);
		Collections.sort(orderedActivities);
		sameCountsNoise.get(noiseLevel).add(orderedActivities);
	}

	public Collection<String> getSameCounts(String activity) {
		for (Collection<String> sameCount : sameCounts) {
			if (sameCount.contains(activity)) {
				return sameCount;
			}
		}
		return null;
	}

	public void addPrePost(String activity, Collection<String> pre, Collection<String> post) {
		Set<String> preset = new HashSet<String>(pre);
		Set<String> postset = new HashSet<String>(post);
		if (!precedences.containsKey(activity)) {
			precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
		}
		if (!responses.containsKey(activity)) {
			responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
		}
		if (!notPrecedences.containsKey(activity)) {
			notPrecedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
		}
		if (!notResponses.containsKey(activity)) {
			notResponses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
		}
		if (!notCoExistences.containsKey(activity)) {
			notCoExistences.put(activity, new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
		}
		precedences.get(activity).addAll(preset);
		responses.get(activity).addAll(postset);
		Set<String> negPreset = new HashSet<String>(countModel.getActivities());
		negPreset.removeAll(preset);
		notPrecedences.get(activity).addAll(negPreset);
		Set<String> negPostset = new HashSet<String>(countModel.getActivities());
		negPostset.removeAll(postset);
		notResponses.get(activity).addAll(negPostset);
		Set<String> prepostset = new HashSet<String>(countModel.getActivities());
		prepostset.removeAll(preset);
		prepostset.removeAll(postset);
		notCoExistences.get(activity).addAll(prepostset);
	}

	public void cleanPrePost() {
		for (String activity : precedences.keySet()) {
			precedences.get(activity).reset();
		}
		for (String activity : responses.keySet()) {
			responses.get(activity).reset();
		}
		for (String activity : notPrecedences.keySet()) {
			notPrecedences.get(activity).reset();
		}
		for (String activity : notResponses.keySet()) {
			notResponses.get(activity).reset();
		}
		for (String activity : notCoExistences.keySet()) {
			notPrecedences.get(activity).removeAll(notCoExistences.get(activity));
			notResponses.get(activity).removeAll(notCoExistences.get(activity));
		}
		Map<String, Set<String>> precedences2 = new HashMap<String, Set<String>>();
		Map<String, Set<String>> responses2 = new HashMap<String, Set<String>>();
		Map<String, Set<String>> negPrecedences2 = new HashMap<String, Set<String>>();
		Map<String, Set<String>> negResponses2 = new HashMap<String, Set<String>>();
		for (String activity : countModel.getActivities()) {
			precedences2.put(activity, new HashSet<String>(precedences.get(activity)));
			responses2.put(activity, new HashSet<String>(responses.get(activity)));
			negPrecedences2.put(activity, new HashSet<String>(notPrecedences.get(activity)));
			negResponses2.put(activity, new HashSet<String>(notResponses.get(activity)));
		}
		for (String activity : countModel.getActivities()) {
			cleanPrePost(activity, precedences, precedences2);
			cleanPrePost(activity, responses, responses2);
			cleanPrePost(activity, notPrecedences, negPrecedences2);
			cleanPrePost(activity, notResponses, negResponses2);
		}
	}

	private void cleanPrePost(String activity, Map<String, ThresholdSet> map, Map<String, Set<String>> map2) {
		Set<String> mappedActivities = map2.get(activity);
		Set<String> mappedMappedActivities = new HashSet<String>();
		for (String mappedActivity : mappedActivities) {
			for (String mappedMappedActivity : map2.get(mappedActivity)) {
				if (!map2.get(mappedMappedActivity).contains(mappedActivity)) {
					mappedMappedActivities.add(mappedMappedActivity);
				}
			}
		}
//		System.out.println("[LogSkeleton] " + activity + " " + mappedMappedActivities);
		map.get(activity).removeAll(mappedMappedActivities);
	}

	private boolean checkSameCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		boolean ok = true;
		for (Collection<String> sameCount : sameCounts) {
			Set<Integer> counts = new HashSet<Integer>();
			for (String activity : sameCount) {
				counts.add(model.get(activity));
			}
			if (counts.size() != 1) {
				messages.add("[LogSkeleton] Case " + caseId + ": Always Together fails for " + sameCount);
				ok = false;
			}
		}
		return ok;
	}

	private boolean checkTransitionCounts(LogSkeletonCount model, Set<String> messages, String caseId) {
		return countModel.checkTransitionCounts(model, messages, caseId);
	}

	private boolean checkCausalDependencies(XTrace trace, Set<String> messages) {
		String caseId = XConceptExtension.instance().extractName(trace);
		List<String> postset = new ArrayList<String>();
		postset.add(LogSkeletonCount.STARTEVENT);
		for (XEvent event : trace) {
			postset.add(XConceptExtension.instance().extractName(event));
		}
		postset.add(LogSkeletonCount.ENDEVENT);
		List<String> preset = new ArrayList<String>();
		String prevActivity = null;
		while (!postset.isEmpty()) {
			if (prevActivity != null) {
				preset.add(prevActivity);
			}
			String activity = postset.remove(0);
			if (precedences.containsKey(activity) && !preset.containsAll(precedences.get(activity))) {
				Set<String> missing = new HashSet<String>(precedences.get(activity));
				missing.removeAll(preset);
				messages.add("[LogSkeleton] Case " + caseId + ": Precedence fails for " + activity + ", missing are "
						+ missing);
				return false;
			}
			if (responses.containsKey(activity) && !postset.containsAll(responses.get(activity))) {
				Set<String> missing = new HashSet<String>(responses.get(activity));
				missing.removeAll(postset);
				messages.add("[LogSkeleton] Case " + caseId + ": Response fails for " + activity + ", missing are "
						+ missing);
				return false;
			}
			Set<String> notPreset = new HashSet<String>(countModel.getActivities());
			notPreset.removeAll(preset);
			if (notPrecedences.containsKey(activity) && !notPreset.containsAll(notPrecedences.get(activity))) {
				Set<String> present = new HashSet<String>(notPrecedences.get(activity));
				present.removeAll(notPreset);
				messages.add("[LogSkeleton] Case " + caseId + ": Not Precedence fails for " + activity + ", present are "
						+ present);
				return false;
			}
			Set<String> notPostset = new HashSet<String>(countModel.getActivities());
			notPostset.removeAll(postset);
			if (notResponses.containsKey(activity) && !notPostset.containsAll(notResponses.get(activity))) {
				Set<String> present = new HashSet<String>(notResponses.get(activity));
				present.removeAll(notPostset);
				messages.add("[LogSkeleton] Case " + caseId + ": Not Response fails for " + activity + ", present are "
						+ present);
				return false;
			}
			prevActivity = activity;
		}
		return true;
	}

	public boolean check(XTrace trace, LogSkeletonCount model, Set<String> messages, boolean[] checks) {
		boolean ok = true;
		if (checks[0]) {
			ok = ok && checkSameCounts(model, messages, XConceptExtension.instance().extractName(trace));
			if (!ok) {
				return false;
			}
		}
		if (checks[1]) {
			ok = ok && checkCausalDependencies(trace, messages);
			if (!ok) {
				return false;
			}
		}
		if (checks[2]) {
			ok = ok && checkTransitionCounts(model, messages, XConceptExtension.instance().extractName(trace));
			if (!ok) {
				return false;
			}
		}
		return ok;
	}

	public String toHTMLString(boolean includeHTMLTags) {
		StringBuffer buf = new StringBuffer();
		List<String> sorted;
		if (includeHTMLTags) {
			buf.append("<html>");
		}
		buf.append("<h1>Causal relations</h1><table>");
		buf.append(
				"<tr><th>Activity</th><th>Sibling activities</th><th>Count</th><th>Precedence</th><th>Response</th><th>Not co-occurrence</th></tr>");
		for (String activity : countModel.getActivities()) {
			// Activity
			buf.append("<tr><td>" + activity + "</td>");
			// Sibling activities and count
			for (Collection<String> siblings : sameCounts) {
				if (siblings.contains(activity)) {
					// Activities
					sorted = new ArrayList<String>(siblings);
					Collections.sort(sorted);
					sorted.remove(activity);
					buf.append("<td>" + sorted + "</td>");
					// Count
					buf.append("<td>" + countModel.get(activity) + "</td>");
				}
			}
			// Precedence
			sorted = new ArrayList<String>(precedences.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Response
			sorted = new ArrayList<String>(responses.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
			// Not co-occurrence
			sorted = new ArrayList<String>(countModel.getActivities());
			sorted.removeAll(notCoExistences.get(activity));
			Collections.sort(sorted);
			buf.append("<td>" + sorted + "</td>");
		}
		buf.append("</table>");
		if (includeHTMLTags) {
			buf.append("</html>");
		}
		return buf.toString();
	}

	public Dot visualize(LogSkeletonBrowserParameters parameters) {
		Map<String, DotNode> map = new HashMap<String, DotNode>();
		Dot graph = new Dot();
		//		graph.setOption("concentrate", "true");
		//		graph.setKeepOrderingOfChildren(true);
		// Set312 color scheme, with white as last resort.
		String[] set312Colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462",
				"#b3de69", "#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f" };
		//		String[] colors = new String[] { "#8dd3c7", "#ffffb3", "#bebada", "#fb8072", "#80b1d3", "#fdb462", "#b3de69",
		//				"#fccde5", "#d9d9d9", "#bc80bd", "#ccebc5", "#ffed6f", "#8dd3c7:#ffffb3", "#bebada:#fb8072",
		//				"#80b1d3:#fdb462", "#b3de69:#fccde5", "#d9d9d9:#bc80bd", "#ccebc5:#ffed6f", "#ffffb3:#bebada",
		//				"#fb8072:#80b1d3", "#fdb462:#b3de69", "#fccde5:#d9d9d9", "#bc80bd:#ccebc5", "#ffed6f:#8dd3c7", "white" };
		String[] colors = new String[100];
		for (int i = 0; i < 99; i++) {
			int m = i / 12;
			int d = i % 12;
			if (m == 0) {
				// Basic color, no gradient.
				colors[i] = set312Colors[i];
			} else {
				// Extended color, gradient.
				colors[i] = set312Colors[d] + ":" + set312Colors[(d + m) % 12];
			}
		}
		// Fall-back color
		colors[99] = "white";

		int colorIndex = 0;
		Map<String, String> colorMap = new HashMap<String, String>();

		Set<String> activities = new HashSet<String>(parameters.getActivities());

		setPrecedenceThreshold(parameters.getPrecedenceThreshold());
		setResponseThreshold(parameters.getResponseThreshold());
		setNotCoExistenceThreshold(parameters.getNotCoExistenceThreshold());

		if (parameters.isUseNeighbors()) {
			for (String fromActivity : countModel.getActivities()) {
				for (String toActivity : countModel.getActivities()) {
					if (parameters.getActivities().contains(fromActivity)
							|| parameters.getActivities().contains(toActivity)) {
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSAFTER)) {
							if (responses.get(fromActivity).contains(toActivity)) {
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSBEFORE)) {
							if (precedences.get(toActivity).contains(fromActivity)) {
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERAFTER)) {
							if (notResponses.get(fromActivity).contains(toActivity)) {
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERBEFORE)) {
							if (notPrecedences.get(toActivity).contains(fromActivity)) {
								activities.add(fromActivity);
								activities.add(toActivity);
							}
						}
						//						if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENNEXT)) {
						//							if (countModel.get(toActivity, fromActivity) == 0
						//									&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(fromActivity))) {
						//								activities.add(fromActivity);
						//								activities.add(toActivity);
						//							}
						//						}
						//						if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENPREVIOUS)) {
						//							if (countModel.get(toActivity, fromActivity) == 0
						//									&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(toActivity))) {
						//								activities.add(fromActivity);
						//								activities.add(toActivity);
						//							}
						//						}
						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERTOGETHER)) {
							if (!fromActivity.equals(toActivity)) {
								if (fromActivity.compareTo(toActivity) >= 0
										&& (!parameters.isUseEquivalenceClass()
												|| fromActivity.equals(getSameCounts(fromActivity).iterator().next()))
										&& (!parameters.isUseEquivalenceClass()
												|| toActivity.equals(getSameCounts(toActivity).iterator().next()))
										&& notCoExistences.get(fromActivity).contains(toActivity)) {
									activities.add(fromActivity);
									activities.add(toActivity);
								}
							}
						}
						//						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTONEWAY)) {
						//							if (countModel.get(fromActivity, toActivity) > 0
						//									&& countModel.get(toActivity, fromActivity) == 0) {
						//								activities.add(fromActivity);
						//								activities.add(toActivity);
						//							}
						//						}
						//						if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTBOTHWAYS)) {
						//							if (fromActivity.compareTo(toActivity) <= 0) {
						//								if (countModel.get(fromActivity, toActivity) > 0
						//										&& countModel.get(toActivity, fromActivity) > 0) {
						//									activities.add(fromActivity);
						//									activities.add(toActivity);
						//								}
						//							}
						//						}
					}
				}
			}
		}

		for (String activity : activities) {
			String colorActivity = getSameCounts(activity).iterator().next();
			String activityColor = colorMap.get(colorActivity);
			if (activityColor == null) {
				activityColor = colors[colorIndex];
				colorMap.put(colorActivity, activityColor);
				if (colorIndex < colors.length - 1) {
					colorIndex++;
				}
			}
			String interval = "" + countModel.getMin(activity);
			if (countModel.getMax(activity) > countModel.getMin(activity)) {
				interval += ".." + countModel.getMax(activity);
			}
			int border = 0;
			if (parameters.getActivities().contains(activity)) {
				border = 1;
			}

			DotNode node = graph.addNode("<<table align=\"center\" bgcolor=\"" + activityColor + "\" border=\"" + border
					+ "\" cellborder=\"0\" cellpadding=\"2\" columns=\"*\" style=\"rounded\"><tr><td colspan=\"3\"><font point-size=\"24\"><b>"
					+ encodeHTML(activity) + "</b></font></td></tr><hr/><tr><td>" + colorActivity + "</td><td>"
					+ countModel.get(activity) + "</td>" + "<td>" + interval + "</td>" + "</tr></table>>");
			node.setOption("shape", "none");
			//			DotNode node = graph.addNode(activity + "\n" + countModel.get(activity));
			//			node.setLabel("<" + encodeHTML(activity) + ">");
			map.put(activity, node);
		}

		String defaultColor = darker("#d9d9d9");
		String almostNeverColor = "#fdb462";
		String neverColor = darker(almostNeverColor);
		String almostAlwaysColor = "#80b1d3";
		String alwaysColor = darker(almostAlwaysColor);
		String almostAlwaysNotColor = "#fb8072";
		String alwaysNotColor = darker(almostAlwaysNotColor);

		for (String fromActivity : activities) {
			for (String toActivity : activities) {
				if (parameters.getActivities().contains(fromActivity)
						|| parameters.getActivities().contains(toActivity)) {
					String tailDecorator = null;
					String headDecorator = null;
					String tailLabel = null;
					String headLabel = null;
					String tailArrow = null;
					String headArrow = null;
					String headColor = null;
					String tailColor = null;
					boolean isAsymmetric = true;
					if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSAFTER)) {
						if (tailDecorator == null && responses.get(fromActivity).contains(toActivity)) {
							tailDecorator = "noneinv";
//							headArrow = "normal";
							tailColor = alwaysColor;
							int threshold = responses.get(fromActivity).getMaxThreshold(toActivity);
							if (threshold < 100) {
								tailLabel = "." + threshold;
								tailColor = almostAlwaysColor;
							}
							//							System.out.println("[LogSkeleton] tailLabel = " + tailLabel);
						}
					}
					if (parameters.getVisualizers().contains(LogSkeletonBrowser.ALWAYSBEFORE)) {
						if (headDecorator == null && precedences.get(toActivity).contains(fromActivity)) {
							headDecorator = "normal";
//							headArrow = "normal";
							headColor = alwaysColor;
							int threshold = precedences.get(toActivity).getMaxThreshold(fromActivity);
							if (threshold < 100) {
								headLabel = "." + threshold;
								headColor = almostAlwaysColor;
							}
							//							System.out.println("[LogSkeleton] headLabel = " + headLabel);
						}
					}
					//					if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENNEXT)) {
					//						if (tailDecorator == null && countModel.get(toActivity, fromActivity) == 0
					//								&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(fromActivity))) {
					//							tailDecorator = "odot";
					//							headArrow = "normal";
					//							headLabel = "" + countModel.get(fromActivity, toActivity);
					//						}
					//					}
					//					if (parameters.getVisualizers().contains(LogSkeletonBrowser.OFTENPREVIOUS)) {
					//						if (headDecorator == null && countModel.get(toActivity, fromActivity) == 0
					//								&& (5 * countModel.get(fromActivity, toActivity) > countModel.get(toActivity))) {
					//							headDecorator = "odot";
					//							headArrow = "normal";
					//							headLabel = "" + countModel.get(fromActivity, toActivity);
					//						}
					//					}
					if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERTOGETHER)) {
						if (!fromActivity.equals(toActivity)) {
							if (headDecorator == null && fromActivity.compareTo(toActivity) >= 0
									&& (!parameters.isUseEquivalenceClass()
											|| fromActivity.equals(getSameCounts(fromActivity).iterator().next()))
									&& (!parameters.isUseEquivalenceClass()
											|| toActivity.equals(getSameCounts(toActivity).iterator().next()))
									&& notCoExistences.get(toActivity).contains(fromActivity)) {
								headDecorator = "nonetee";
								//								dummy = true;
								isAsymmetric = false;
								headColor = neverColor;
								int threshold = notCoExistences.get(toActivity).getMaxThreshold(fromActivity);
								if (threshold < 100) {
									headLabel = "." + threshold;
									headColor = almostNeverColor;
								}
							}
							if (tailDecorator == null && fromActivity.compareTo(toActivity) >= 0
									&& (!parameters.isUseEquivalenceClass()
											|| fromActivity.equals(getSameCounts(fromActivity).iterator().next()))
									&& (!parameters.isUseEquivalenceClass()
											|| toActivity.equals(getSameCounts(toActivity).iterator().next()))
									&& notCoExistences.get(fromActivity).contains(toActivity)) {
								tailDecorator = "nonetee";
								//								dummy = true;
								isAsymmetric = false;
								tailColor = neverColor;
								int threshold = notCoExistences.get(fromActivity).getMaxThreshold(toActivity);
								if (threshold < 100) {
									tailLabel = "." + threshold;
									tailColor = almostNeverColor;
								}
							}
						}
					}
					if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERAFTER)) {
						if (!fromActivity.equals(toActivity) && headDecorator == null
								&& notResponses.get(toActivity).contains(fromActivity) 
								/*&& !notResponses.get(fromActivity).contains(toActivity)*/) {
							headDecorator = "noneinvtee";
//							tailArrow = "normal";
							headColor = alwaysNotColor;
							int threshold = notResponses.get(toActivity).getMaxThreshold(fromActivity);
							if (threshold < 100) {
								headLabel = "." + threshold;
								headColor = almostAlwaysNotColor;
							}
							//							System.out.println("[LogSkeleton] tailLabel = " + tailLabel);
						}
					}
					if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEVERBEFORE)) {
						if (!fromActivity.equals(toActivity) && tailDecorator == null
								&& notPrecedences.get(fromActivity).contains(toActivity)
								/*&& !notPrecedences.get(toActivity).contains(fromActivity)*/) {
							tailDecorator = "teenormal";
//							tailArrow = "normal";
							tailColor = alwaysNotColor;
							int threshold = notPrecedences.get(fromActivity).getMaxThreshold(toActivity);
							if (threshold < 100) {
								tailLabel = "." + threshold;
								tailColor = almostAlwaysNotColor;
							}
							//							System.out.println("[LogSkeleton] tailLabel = " + tailLabel);
						}
					}
					//					if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTONEWAY)) {
					//						if (tailDecorator == null && countModel.get(fromActivity, toActivity) > 0
					//								&& countModel.get(toActivity, fromActivity) == 0) {
					//							tailDecorator = "odot";
					//							if (headLabel == null) {
					//								headLabel = "" + countModel.get(fromActivity, toActivity);
					//							}
					//							if (headArrow == null) {
					//								headArrow = "normal";
					//							}
					//						}
					//					}
					//					if (parameters.getVisualizers().contains(LogSkeletonBrowser.NEXTBOTHWAYS)) {
					//						if (fromActivity.compareTo(toActivity) <= 0) {
					//							if (tailDecorator == null && countModel.get(fromActivity, toActivity) > 0
					//									&& countModel.get(toActivity, fromActivity) > 0) {
					//								tailDecorator = "odot";
					//								if (headLabel == null) {
					//									headLabel = "" + countModel.get(fromActivity, toActivity);
					//								}
					//								if (headArrow == null) {
					//									headArrow = "normal";
					//								}
					//							}
					//							if (headDecorator == null && countModel.get(fromActivity, toActivity) > 0
					//									&& countModel.get(toActivity, fromActivity) > 0) {
					//								headDecorator = "odot";
					//								if (tailLabel == null) {
					//									tailLabel = "" + countModel.get(toActivity, fromActivity);
					//								}
					//								if (tailArrow == null) {
					//									tailArrow = "vee";
					//								}
					//							}
					//						}
					//					}
					if (tailDecorator != null || headDecorator != null || tailArrow != null || headArrow != null) {
						DotEdge arc = graph.addEdge(map.get(fromActivity), map.get(toActivity));
						arc.setOption("dir", "both");
						if (tailDecorator == null) {
							tailDecorator = "";
						}
						if (tailArrow == null) {
							tailArrow = "none";
						}
						if (headDecorator == null) {
							headDecorator = "";
						}
						if (headArrow == null) {
							headArrow = "none";
						}
						arc.setOption("arrowtail", tailDecorator + tailArrow);
						arc.setOption("arrowhead", headDecorator + headArrow);
						if (parameters.isUseFalseConstraints() && !isAsymmetric) {
							arc.setOption("constraint", "false");
						}
						if (parameters.isUseEdgeColors() && (headColor != null || tailColor != null)) {
							String color = (tailColor == null ? defaultColor : tailColor) + ";0.5:"
									+ (headColor == null ? defaultColor : headColor) + ";0.5";
							arc.setOption("color", color);
						}
						//						arc.setOption("constraint", "true");
						if (parameters.isUseHeadTailLabels()) {
							if (headLabel != null) {
								arc.setOption("headlabel", headLabel);
							}
							if (tailLabel != null) {
								arc.setOption("taillabel", tailLabel);
							}
						} else if (headLabel != null || tailLabel != null) {
							String label = "";
							if (tailLabel != null) {
								label += tailLabel;
							}
							label += "&rarr;";
							if (headLabel != null) {
								label += headLabel;
							}
							arc.setLabel(label);
						}
					}
				}
			}
		}

		if (parameters.isUseHyperArcs())

		{
			/*
			 * Sort the arcs to get a (more) deterministic result.
			 */
			List<DotEdge> candidateArcs = new ArrayList<DotEdge>(graph.getEdges());
			Collections.sort(candidateArcs, new Comparator<DotEdge>() {

				public int compare(DotEdge o1, DotEdge o2) {
					int c = o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
					if (c == 0) {
						c = o1.getTarget().getLabel().compareTo(o2.getTarget().getLabel());
					}
					return c;
				}

			});

			/*
			 * Iterate over all arcs in the (current!) graph.
			 * 
			 * Note that the graph may change in the process.
			 */
			while (!candidateArcs.isEmpty()) {
				/*
				 * Get the next arc.
				 */
				DotEdge arc = candidateArcs.iterator().next();
				/*
				 * For now, only do this for always-arcs. Includes always-not (not response, not precendence) arcs.
				 */
				if (arc.getOption("arrowtail").contains("inv") || arc.getOption("arrowhead").contains("inv")
						|| arc.getOption("arrowtail").contains("inv") 
						|| arc.getOption("arrowhead").contains("normal")) {
					/*
					 * Get the cluster for this arc.
					 */
					DotNode sourceNode = arc.getSource();
					DotNode targetNode = arc.getTarget();
					Set<DotNode> sourceNodes = new HashSet<DotNode>();
					sourceNodes.add(sourceNode);
					Set<DotNode> targetNodes = new HashSet<DotNode>();
					targetNodes.add(targetNode);
					boolean changed = true;
					while (changed) {
						changed = false;
						for (DotEdge anotherArc : graph.getEdges()) {
							if (isEqual(arc, anotherArc)) {
								if (sourceNodes.contains(anotherArc.getSource())) {
									changed = changed || targetNodes.add(anotherArc.getTarget());
								}
								if (targetNodes.contains(anotherArc.getTarget())) {
									changed = changed || sourceNodes.add(anotherArc.getSource());
								}
							}
						}
					}

					/*
					 * Get a biggest maximal clique in the cluster.
					 */
					Set<DotEdge> arcs = getMaximalClique(graph, sourceNodes, targetNodes, arc.getOption("arrowtail"),
							arc.getOption("arrowhead"), arc.getLabel(), arc, new HashSet<List<Set<DotNode>>>());

					if (arcs != null) {
						/*
						 * A maximal clique was found. Update the sources and
						 * targets to this clique.
						 */
						sourceNodes.clear();
						targetNodes.clear();
						for (DotEdge a : arcs) {
							sourceNodes.add(a.getSource());
							targetNodes.add(a.getTarget());
						}
						//						System.out.println("[LogSkeleton] " + sourceNodes + " -> " + targetNodes);
						/*
						 * Add a connector node to the graph.
						 */
						DotNode connector = graph.addNode("");
						connector.setOption("shape", "point");
						/*
						 * Add arcs from and to the new connector node.
						 */
						for (DotNode node : sourceNodes) {
							DotEdge a = graph.addEdge(node, connector);
							a.setOption("dir", "both");
							a.setOption("arrowtail", arc.getOption("arrowtail"));
							a.setOption("arrowhead", "none");
							if (arc.getOption("taillabel") != null) {
								a.setOption("taillabel", arc.getOption("taillabel"));
							}
							if (arc.getLabel() != null) {
								String[] labels2 = arc.getLabel().split("&rarr;");
								if (labels2.length == 2) {
									System.out.println("[LogSkeleton] set label1 " + labels2[0]);
									a.setLabel(labels2[0]);
								} else {
									a.setLabel(arc.getLabel());
								}
							}
							if (arc.getOption("color") != null) {
								String[] colors2 = arc.getOption("color").split("[;:]");
								if (colors2.length == 4) {
									System.out.println("[LogSkeleton] set color1 " + colors[0]);
									a.setOption("color", colors2[0]);
								} else {
									a.setOption("color", arc.getOption("color"));
								}
							}
							candidateArcs.add(a);
						}
						for (DotNode node : targetNodes) {
							DotEdge a = graph.addEdge(connector, node);
							a.setOption("dir", "both");
							a.setOption("arrowtail", "none");
							a.setOption("arrowhead", arc.getOption("arrowhead"));
							if (arc.getOption("headlabel") != null) {
								a.setOption("headlabel", arc.getOption("headlabel"));
							}
							if (arc.getLabel() != null) {
								String[] labels2 = arc.getLabel().split("&rarr;");
								if (labels2.length == 2) {
									System.out.println("[LogSkeleton] set label2 " + labels2[1]);
									a.setLabel(labels2[1]);
								} else {
									a.setLabel(arc.getLabel());
								}
							}
							if (arc.getOption("color") != null) {
								String[] colors2 = arc.getOption("color").split("[;:]");
								if (colors2.length == 4) {
									System.out.println("[LogSkeleton] set color2 " + colors[2]);
									a.setOption("color", colors2[2]);
								} else {
									a.setOption("color", arc.getOption("color"));
								}
							}
							candidateArcs.add(a);
						}
						/*
						 * Remove the old arcs, they have now been replaced with
						 * the newly added connector node and arcs.
						 */
						for (DotEdge anotherArc : arcs) {
							graph.removeEdge(anotherArc);
						}
						candidateArcs.removeAll(arcs);
						/*
						 * Sort the arcs again, as some have been added.
						 */
						Collections.sort(candidateArcs, new Comparator<DotEdge>() {

							public int compare(DotEdge o1, DotEdge o2) {
								int c = o1.getSource().getLabel().compareTo(o2.getSource().getLabel());
								if (c == 0) {
									c = o1.getTarget().getLabel().compareTo(o2.getTarget().getLabel());
								}
								return c;
							}

						});
					} else {
						/*
						 * No maximal clique was found, leave the arc as-is.
						 */
						candidateArcs.remove(arc);
					}
				} else {
					/*
					 * Not an always-arc, leave the arc as-is.
					 */
					candidateArcs.remove(arc);
				}
			}
		}

		graph.setOption("labelloc", "b");
		graph.setOption("nodesep", "0.5");
		//		String label = "Event Log: " + (this.label == null ? "<not specified>" : this.label) + "\\l";
		//		if (!required.isEmpty()) {
		//			label += "Required Activities Filters: " + required + "\\l";
		//		}
		//		if (!forbidden.isEmpty()) {
		//			label += "Forbidden Activities Filters: " + forbidden + "\\l";
		//		}
		//		if (!splitters.isEmpty()) {
		//			label += "Activity Splitters: " + splitters + "\\l";
		//		}
		List<String> selectedActivities = new ArrayList<String>(parameters.getActivities());
		Collections.sort(selectedActivities);
		//		label += "Show Activities: " + activities + "\\l";
		//		label += "Show Constraints: " + parameters.getVisualizers() + "\\l";
		String label = "<table bgcolor=\"gold\" cellborder=\"0\" cellpadding=\"0\" columns=\"3\" style=\"rounded\">";
		label +=

				encodeHeader("Skeleton Configuration");
		label += encodeRow("Event Log", this.label == null ? "<not specified>" : this.label);
		if (!required.isEmpty()) {
			label += encodeRow("Required Activities Filter", required.toString());
		}
		if (!forbidden.isEmpty()) {
			label += encodeRow("Forbidden Activities Filter", forbidden.toString());
		}
		if (!splitters.isEmpty()) {
			label += encodeRow("Activity Splitters", splitters.toString());
		}
		label += encodeRow("View Activities", selectedActivities.toString());
		label += encodeRow("View Constraints", parameters.getVisualizers().toString());
		if (equivalenceThreshold < 100) {
			label += encodeRow("Noise Threshold", "" + (100 - equivalenceThreshold) + "%");
		}
		label += "</table>";
		graph.setOption("fontsize", "8.0");
		graph.setOption("label", "<" + label + ">");
		//		graph.setOption("labeljust", "l");
		return graph;
	}

	private String darker(String color) {
		Color darkerColor = Color.decode(color).darker();
		return "#" + Integer.toHexString(darkerColor.getRed()) + Integer.toHexString(darkerColor.getGreen())
				+ Integer.toHexString(darkerColor.getBlue());
	}

	private boolean isEqual(DotEdge e1, DotEdge e2) {
		if (!isEqual(e1.getOption("arrowtail"), e2.getOption("arrowtail"))) {
			return false;
		}
		if (!isEqual(e1.getOption("arrowhead"), e2.getOption("arrowhead"))) {
			return false;
		}
		if (!isEqual(e1.getOption("headlabel"), e2.getOption("headlabel"))) {
			return false;
		}
		if (!isEqual(e1.getOption("taillabel"), e2.getOption("taillabel"))) {
			return false;
		}
		if (!isEqual(e1.getLabel(), e2.getLabel())) {
			return false;
		}
		return true;
	}

	private boolean isEqual(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		}
		return s1.equals(s2);
	}

	private Set<DotEdge> getMaximalClique(Dot graph, Set<DotNode> sourceNodes, Set<DotNode> targetNodes,
			String arrowtail, String arrowhead, String label, DotEdge baseArc, Set<List<Set<DotNode>>> checkedNodes) {
		/*
		 * Make sure a clique is not too small.
		 */
		if (sourceNodes.size() < 2) {
			/*
			 * A single source. Do not look for a maximal clique.
			 */
			return null;
		}
		if (targetNodes.size() < 2) {
			/*
			 * A single target. Do not look for a maximal clique.
			 */
			return null;
		}
		/*
		 * Keep track of which combinations of sources and targets have already
		 * been checked. This prevents checking the same combinations many times
		 * over.
		 */
		List<Set<DotNode>> checked = new ArrayList<Set<DotNode>>();
		checked.add(new HashSet<DotNode>(sourceNodes));
		checked.add(new HashSet<DotNode>(targetNodes));
		checkedNodes.add(checked);
		/*
		 * Collect all matching arcs that go from some source to some target.
		 */
		Set<DotEdge> arcs = new HashSet<DotEdge>();
		for (DotEdge arc : graph.getEdges()) {
			if (isEqual(arc, baseArc)) {
				if (sourceNodes.contains(arc.getSource()) && targetNodes.contains(arc.getTarget())) {
					arcs.add(arc);
				}
			}
		}
		/*
		 * Check whether a maximal clique.
		 */
		if (arcs.size() == sourceNodes.size() * targetNodes.size()) {
			/*
			 * Yes.
			 */
			return arcs;
		}
		/*
		 * No, look for maximal cliques that have one node (source or target)
		 * less.
		 */
		Set<DotEdge> bestArcs = null; // Best solution so far.
		if (sourceNodes.size() > targetNodes.size()) {
			/*
			 * More sources than targets. Removing a source yields a possible
			 * bigger clique than removing a target. So, first try to remove a
			 * source, and only then try to remove a target.
			 */
			if (sourceNodes.size() > 2) {
				/*
				 * Try to find a maximal clique with one source removed. Sort
				 * the source nodes first to get a (more) deterministic result.
				 */
				List<DotNode> sortedSourceNodes = new ArrayList<DotNode>(sourceNodes);
				Collections.sort(sortedSourceNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode srcNode : sortedSourceNodes) {
					if (bestArcs == null || (sourceNodes.size() - 1) * targetNodes.size() > bestArcs.size()) {
						/*
						 * May result in a bigger clique than the best found so
						 * far. First, remove the node from the sources.
						 */
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						/*
						 * Check whether this combination of sources and targets
						 * was checked before.
						 */
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							/*
							 * No, it was not. Check now.
							 */
							arcs = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label, baseArc,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								/*
								 * Found a bigger maximal clique than the best
								 * found so far. Update.
								 */
								bestArcs = arcs;
							}
						}
					}
				}
			}
			if (targetNodes.size() > 2) {
				List<DotNode> sortedTargetNodes = new ArrayList<DotNode>(targetNodes);
				Collections.sort(sortedTargetNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode tgtNode : sortedTargetNodes) {
					if (bestArcs == null || sourceNodes.size() * (targetNodes.size() - 1) > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label, baseArc,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
							}
						}
					}
				}
			}
		} else {
			/*
			 * The other way around.
			 */
			if (targetNodes.size() > 2) {
				List<DotNode> sortedTargetNodes = new ArrayList<DotNode>(targetNodes);
				Collections.sort(sortedTargetNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode tgtNode : sortedTargetNodes) {
					if (bestArcs == null || sourceNodes.size() * (targetNodes.size() - 1) > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(targetNodes);
						nodes.remove(tgtNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(sourceNodes);
						checked.add(nodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, sourceNodes, nodes, arrowtail, arrowhead, label, baseArc,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
							}
						}
					}
				}
			}
			if (sourceNodes.size() > 2) {
				List<DotNode> sortedSourceNodes = new ArrayList<DotNode>(sourceNodes);
				Collections.sort(sortedSourceNodes, new Comparator<DotNode>() {

					public int compare(DotNode o1, DotNode o2) {
						return o1.getLabel().compareTo(o2.getLabel());
					}

				});
				for (DotNode srcNode : sortedSourceNodes) {
					if (bestArcs == null || (sourceNodes.size() - 1) * targetNodes.size() > bestArcs.size()) {
						Set<DotNode> nodes = new HashSet<DotNode>(sourceNodes);
						nodes.remove(srcNode);
						checked = new ArrayList<Set<DotNode>>();
						checked.add(nodes);
						checked.add(targetNodes);
						if (!checkedNodes.contains(checked)) {
							arcs = getMaximalClique(graph, nodes, targetNodes, arrowtail, arrowhead, label, baseArc,
									checkedNodes);
							if (bestArcs == null || (arcs != null && bestArcs.size() < arcs.size())) {
								bestArcs = arcs;
							}
						}
					}
				}
			}
		}
		/*
		 * Return the biggest maximal clique found. Equals null if none found.
		 */
		return bestArcs;
	}

	private String encodeHeader(String title) {
		return "<tr><td colspan=\"3\"><b>" + encodeHTML(title) + "</b></td></tr><hr/>";
	}

	private String encodeRow(String label, String value) {
		return encodeRow(label, value, 0);
	}

	private String encodeRow(String label, String value, int padding) {
		return "<tr><td align=\"right\"><i>" + label + "</i></td><td> : </td><td align=\"left\">" + encodeHTML(value)
				+ "</td></tr>";
	}

	private String encodeHTML(String s) {
		String s2 = s;
		if (s.length() > 2 && s.startsWith("[") && s.endsWith("]")) {
			s2 = s.substring(1, s.length() - 1);
		}
		return s2.replaceAll("&", "&amp;").replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
	}

	public Dot createGraph(LogSkeletonBrowser visualizer) {
		LogSkeletonBrowserParameters parameters = new LogSkeletonBrowserParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().add(visualizer);
		return visualize(parameters);
	}

	public Dot createGraph(Set<LogSkeletonBrowser> visualizers) {
		LogSkeletonBrowserParameters parameters = new LogSkeletonBrowserParameters();
		parameters.getActivities().addAll(countModel.getActivities());
		parameters.getVisualizers().addAll(visualizers);
		return visualize(parameters);
	}

	public Dot createGraph(LogSkeletonBrowserParameters parameters) {
		return visualize(parameters);
	}

	public Collection<String> getActivities() {
		return countModel.getActivities();
	}

	public Set<String> getRequired() {
		return required;
	}

	public void setRequired(Set<String> required) {
		this.required = required;
	}

	public Set<String> getForbidden() {
		return forbidden;
	}

	public void setForbidden(Set<String> forbidden) {
		this.forbidden = forbidden;
	}

	public List<List<String>> getSplitters() {
		return splitters;
	}

	public void setSplitters(List<List<String>> splitters) {
		this.splitters = splitters;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void exportToFile(CsvWriter writer) throws IOException {
		writer.write(label);
		writer.endRecord();
		countModel.exportToFile(writer);
		writer.write("equivalence");
		for (int noise = 0; noise < 21; noise++) {
			writer.write("" + sameCountsNoise.get(noise).size());
		}
		for (int noise = 0; noise < 21; noise++) {
			writer.endRecord();
			for (Collection<String> activities : sameCountsNoise.get(noise)) {
				for (String activity : activities) {
					writer.write(activity);
				}
				writer.endRecord();
			}
		}
		writer.write("precedence");
		writer.write("" + precedences.size());
		writer.endRecord();
		for (String activity : precedences.keySet()) {
			writer.write(activity);
			precedences.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("response");
		writer.write("" + responses.size());
		writer.endRecord();
		for (String activity : responses.keySet()) {
			writer.write(activity);
			responses.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not precedence");
		writer.write("" + notPrecedences.size());
		writer.endRecord();
		for (String activity : notPrecedences.keySet()) {
			writer.write(activity);
			notPrecedences.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not response");
		writer.write("" + notResponses.size());
		writer.endRecord();
		for (String activity : notResponses.keySet()) {
			writer.write(activity);
			notResponses.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("not co-occurrence");
		writer.write("" + notCoExistences.size());
		writer.endRecord();
		for (String activity : notCoExistences.keySet()) {
			writer.write(activity);
			notCoExistences.get(activity).exportToFile(writer);
			writer.endRecord();
		}
		writer.write("required");
		writer.write(required.isEmpty() ? "0" : "1");
		writer.endRecord();
		if (!required.isEmpty()) {
			for (String activity : required) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.write("forbidden");
		writer.write(forbidden.isEmpty() ? "0" : "1");
		writer.endRecord();
		if (!forbidden.isEmpty()) {
			for (String activity : forbidden) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.write("splitters");
		writer.write("" + splitters.size());
		writer.endRecord();
		for (List<String> splitter : splitters) {
			for (String activity : splitter) {
				writer.write(activity);
			}
			writer.endRecord();
		}
		writer.endRecord();
	}

	public void importFromStream(CsvReader reader) throws IOException {
		if (reader.readRecord()) {
			label = reader.get(0);
		}
		sameCountsNoise = new HashMap<Integer, Collection<Collection<String>>>();
		for (int noise = 0; noise < 21; noise++) {
			sameCountsNoise.put(noise, new HashSet<Collection<String>>());
		}
		sameCounts = sameCountsNoise.get(0);
		countModel.importFromStream(reader);
		if (reader.readRecord()) {
			if (reader.get(0).equals("always together")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						List<String> orderedActivities = new ArrayList<String>();
						for (int column = 0; column < reader.getColumnCount(); column++) {
							orderedActivities.add(reader.get(column));
						}
						Collections.sort(orderedActivities);
						for (int noise = 0; noise < 21; noise++) {
							sameCountsNoise.get(noise).add(orderedActivities);
						}
					}
				}
			} else if (reader.get(0).equals("equivalence")) {
				int rows[] = new int[21];
				for (int noise = 0; noise < 21; noise++) {
					rows[noise] = Integer.valueOf(reader.get(noise + 1));
				}
				for (int noise = 0; noise < 21; noise++) {
					for (int row = 0; row < rows[noise]; row++) {
						if (reader.readRecord()) {
							List<String> orderedActivities = new ArrayList<String>();
							for (int column = 0; column < reader.getColumnCount(); column++) {
								orderedActivities.add(reader.get(column));
							}
							Collections.sort(orderedActivities);
							sameCountsNoise.get(noise).add(orderedActivities);
						}
					}
				}
			}
		}
		precedences = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("always before")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						precedences.get(activity).addAll(activities);
					}
				}
			} else if (reader.get(0).equals("precedence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						precedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						precedences.get(activity).importFromFile(reader);
					}
				}
			}
		}
		responses = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("always after")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						responses.get(activity).addAll(activities);
					}
				}
			} else if (reader.get(0).equals("response")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						responses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						responses.get(activity).importFromFile(reader);
					}
				}
			}
		}
		notPrecedences = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("not precedence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notPrecedences.put(activity, new ThresholdSet(countModel.getActivities(), precedenceThreshold));
						notPrecedences.get(activity).importFromFile(reader);
					}
				}
			}
		}
		notResponses = new HashMap<String, ThresholdSet>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("not response")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notResponses.put(activity, new ThresholdSet(countModel.getActivities(), responseThreshold));
						notResponses.get(activity).importFromFile(reader);
					}
				}
			}
		}
		if (reader.readRecord()) {
			if (reader.get(0).equals("sometimes before")) {
				Map<String, Set<String>> anyPresets = new HashMap<String, Set<String>>();
				Map<String, Set<String>> anyPostsets = new HashMap<String, Set<String>>();
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						Set<String> activities = new HashSet<String>();
						for (int column = 1; column < reader.getColumnCount(); column++) {
							activities.add(reader.get(column));
						}
						anyPresets.put(activity, activities);
					}
				}
				if (reader.readRecord()) {
					if (reader.get(0).equals("sometimes after")) {
						rows = Integer.valueOf(reader.get(1));
						for (int row = 0; row < rows; row++) {
							if (reader.readRecord()) {
								String activity = reader.get(0);
								Set<String> activities = new HashSet<String>();
								for (int column = 1; column < reader.getColumnCount(); column++) {
									activities.add(reader.get(column));
								}
								anyPostsets.put(activity, activities);
							}
						}
					}
				}
				notCoExistences = new HashMap<String, ThresholdSet>();
				for (String activity : countModel.getActivities()) {
					Set<String> prepostset = new HashSet<String>();
					if (anyPresets.containsKey(activity)) {
						prepostset.addAll(anyPresets.get(activity));
					}
					if (anyPostsets.containsKey(activity)) {
						prepostset.addAll(anyPostsets.get(activity));
					}
					notCoExistences.put(activity,
							new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
					notCoExistences.get(activity).addAll(countModel.getActivities());
					notCoExistences.get(activity).removeAll(prepostset);
				}
			} else if (reader.get(0).equals("not co-occurrence")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						String activity = reader.get(0);
						notCoExistences.put(activity,
								new ThresholdSet(countModel.getActivities(), notCoExistenceeThreshold));
						notCoExistences.get(activity).importFromFile(reader);
					}
				}
			}
		}
		required = new HashSet<String>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("required")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						for (int column = 0; column < reader.getColumnCount(); column++) {
							required.add(reader.get(column));
						}
					}
				}
			}
		}
		forbidden = new HashSet<String>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("forbidden")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						for (int column = 0; column < reader.getColumnCount(); column++) {
							forbidden.add(reader.get(column));
						}
					}
				}
			}
		}
		splitters = new ArrayList<List<String>>();
		if (reader.readRecord()) {
			if (reader.get(0).equals("splitters")) {
				int rows = Integer.valueOf(reader.get(1));
				for (int row = 0; row < rows; row++) {
					if (reader.readRecord()) {
						List<String> splitter = new ArrayList<String>();
						for (int column = 0; column < reader.getColumnCount(); column++) {
							splitter.add(reader.get(column));
						}
						splitters.add(splitter);
					}
				}
			}
		}
	}

	public Set<String> getAlwaysBefore(String activity) {
		if (precedences.containsKey(activity)) {
			return new HashSet<String>(precedences.get(activity));
		}
		return new HashSet<String>();
	}

	public Set<String> getAlwaysAfter(String activity) {
		if (responses.containsKey(activity)) {
			return new HashSet<String>(responses.get(activity));
		}
		return new HashSet<String>();
	}

	public void setPrecedenceThreshold(int precedenceThreshold) {
		this.precedenceThreshold = precedenceThreshold;
		for (String activity : precedences.keySet()) {
			precedences.get(activity).setThreshold(precedenceThreshold);
		}
		for (String activity : notPrecedences.keySet()) {
			notPrecedences.get(activity).setThreshold(precedenceThreshold);
		}
	}

	public int getResponseThreshold() {
		return responseThreshold;
	}

	public void setResponseThreshold(int responseThreshold) {
		this.responseThreshold = responseThreshold;
		for (String activity : responses.keySet()) {
			responses.get(activity).setThreshold(responseThreshold);
		}
		for (String activity : notResponses.keySet()) {
			notResponses.get(activity).setThreshold(responseThreshold);
		}
	}

	public int getNotCoExistenceThreshold() {
		return notCoExistenceeThreshold;
	}

	public void setNotCoExistenceThreshold(int notCoOccurencethreshold) {
		this.notCoExistenceeThreshold = notCoOccurencethreshold;
		for (String activity : notCoExistences.keySet()) {
			notCoExistences.get(activity).setThreshold(notCoOccurencethreshold);
		}
	}

	public boolean hasManyNotCoExistenceArcs(LogSkeletonBrowserParameters parameters) {
		int nr = 0;
		for (String fromActivity : countModel.getActivities()) {
			for (String toActivity : countModel.getActivities()) {
				if (!fromActivity.equals(toActivity)) {
					if (fromActivity.compareTo(toActivity) >= 0
							&& (!parameters.isUseEquivalenceClass()
									|| fromActivity.equals(getSameCounts(fromActivity).iterator().next()))
							&& (!parameters.isUseEquivalenceClass()
									|| toActivity.equals(getSameCounts(toActivity).iterator().next()))
							&& notCoExistences.get(fromActivity).contains(toActivity)) {
						nr++;
					}
				}
			}
		}
		/*
		 * Return whether there are too many Not Co-Existence constraints to
		 * show by default. THe first visualization should be reasonably fast.
		 * In case of too many Not Co-Existence constraints, this first
		 * visualization takes ages.
		 */
		return nr > 100;
	}

	public int getEquivalenceThreshold() {
		return equivalenceThreshold;
	}

	public void setEquivalenceThreshold(int equivalenceThreshold) {
		this.equivalenceThreshold = equivalenceThreshold;
		sameCounts = sameCountsNoise.get(100 - equivalenceThreshold);
	}
}
