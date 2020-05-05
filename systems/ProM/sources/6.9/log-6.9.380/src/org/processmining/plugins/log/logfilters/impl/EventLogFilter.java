package org.processmining.plugins.log.logfilters.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;

@Plugin(name = "Event Log Filter", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "All Event Classes", "Event Classes to keep",
		"Minimal Occurrence frequency", "Minimal Occurrence in cases", "Satisfy both" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class EventLogFilter {

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the XEventClass belonging to this XEvent is
	 * not provided in the given XEventClasses object
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param allEventClasses
	 *            All event classes
	 * @param events
	 *            The event classes that should be kept in the log. Events not
	 *            belonging to these classes are removed, after which emtpy
	 *            traces are also removed
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2 }, variantLabel = "Filter on Event Class")
	public XLog filterWithNames(PluginContext context, XLog log, final XEventClasses allEventClasses,
			XEventClass[] eventClassesToKeep) {
		// Construct a sorted set of names for easy lookup
		final HashSet<XEventClass> toKeep = new HashSet<XEventClass>(Arrays.asList(eventClassesToKeep));

		return LogFilter.filter((context != null ? context.getProgress() : null), 100, log, (context != null ? XLogInfoFactory.createLogInfo(log) : null),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// only keep the event if:
						// 1) its name is in toKeep
						XEventClass c = allEventClasses.getClassOf(event);
						if (!toKeep.contains(c)) {
							return false;
						}
						return true;
					}

				});
	}

	public XLog filterWithClassifier(PluginContext context, XLog log, final XEventClassifier classifier,
			final String[] selectedIds) {
		final Collection<String> ids = new HashSet<String>(Arrays.asList(selectedIds));

		return LogFilter.filter((context != null ? context.getProgress() : null), 100, log, (context != null ? XLogInfoFactory.createLogInfo(log) : null),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						return ids.contains(classifier.getClassIdentity(event));
					}

				});
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the getName() of XEvent is not contained in
	 * the given set of labels. Note that the percentages are based on the
	 * original log, not on the log with the unnecessary events removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param allEventClasses
	 *            All event classes
	 * @param eventClassesToKeep
	 *            The event classes that should be kept in the log. Events not
	 *            belonging to these classes are removed, after which emtpy
	 *            traces are also removed
	 * @param minOccurrence
	 *            All events with an event class which represent less than this
	 *            percentage of the events in the log are removed. Value should
	 *            be 0 <= minOccurrence <= 1. If 0, then all events are kept, if
	 *            1, then no events are kept.
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3 }, variantLabel = "Filter on Frequencies names")
	public XLog filterWithMinOccFreq(PluginContext context, XLog log, final XEventClasses allEventClasses,
			final XEventClass[] eventClassesToKeep, final Double minOccurrence) {

		final HashSet<XEventClass> toKeep = new HashSet<XEventClass>(Arrays.asList(eventClassesToKeep));
		final Map<XEventClass, Double> count = new HashMap<XEventClass, Double>();// allEventClasses
		// .
		// getOccurrenceFrequency
		// (log);
		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// only keep the event if:
						// 1) it's name is in eventClassesToKeep
						// 2) the frequency is >= minOccurrence
						XEventClass c = allEventClasses.getClassOf(event);
						if (!toKeep.contains(c)) {
							return false;
						}

						double percentage = count.get(c);
						return percentage >= minOccurrence;
					}

				});
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the getName() of XEvent is not contained in
	 * the given set of labels. Note that the percentages are based on the
	 * original log, not on the log with the unnecessary events removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param allEventClasses
	 *            All event classes
	 * @param eventClassesToKeep
	 *            The event classes that should be kept in the log. Events not
	 *            belonging to these classes are removed, after which emtpy
	 *            traces are also removed
	 * @param minCases
	 *            All events which occur in less than minCasses percent of the
	 *            cases are removed. Value should be 0 <= minCases <= 1. If 0,
	 *            then all events are kept, if 1, then only events appearing in
	 *            all cases are kept.
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 4 }, variantLabel = "Filter on Frequencies names")
	public XLog filterWithMinCases(PluginContext context, XLog log, final XEventClasses allEventClasses,
			XEventClass[] eventClassesToKeep, final Double minCases) {

		final Map<XEventClass, Double> count = new HashMap<XEventClass, Double>();// allEventClasses
		// .
		// getCaseFrequency(log);
		final HashSet<XEventClass> toKeep = new HashSet<XEventClass>(Arrays.asList(eventClassesToKeep));

		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// only keep the event if:
						// 1) it's name is in eventClassesToKeep
						// 2) the frequency is >= minCases
						XEventClass c = allEventClasses.getClassOf(event);
						if (!toKeep.contains(c)) {
							return false;
						}

						double percentage = count.get(c);
						return percentage >= minCases;
					}

				});
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the getName() of XEvent is not contained in
	 * the given set of labels. Note that the percentages are based on the
	 * original log, not on the log with the unnecessary events removed.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param allEventClasses
	 *            All event classes
	 * @param eventClassesToKeep
	 *            The event classes that should be kept in the log. Events not
	 *            belonging to these classes are removed, after which emtpy
	 *            traces are also removed
	 * @param minOccurrence
	 *            All events with an event class which represent less than this
	 *            percentage of the events in the log are removed. Value should
	 *            be 0 <= minOccurrence <= 1. If 0, then all events are kept, if
	 *            1, then no events are kept.
	 * @param minCases
	 *            All events which occur in less than minCasses percent of the
	 *            cases are removed. Value should be 0 <= minCases <= 1. If 0,
	 *            then all events are kept, if 1, then only events appearing in
	 *            all cases are kept.
	 * @param fullfillBoth
	 *            A boolean indicating wheter both minimal frequencies should be
	 *            obeyed (true) or whether one is enough (false)
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2, 3, 4, 5 }, variantLabel = "Filter on Frequencies names")
	public XLog filterWithBoth(PluginContext context, XLog log, final XEventClasses allEventClasses,
			XEventClass[] eventClassesToKeep, final Double minOccurrence, final Double minCases,
			final Boolean fullfillBoth) {

		final Map<XEventClass, Double> countFreq = new HashMap<XEventClass, Double>();// allEventClasses
		// .
		// getCaseFrequency
		// (log);
		final Map<XEventClass, Double> countCase = new HashMap<XEventClass, Double>();// allEventClasses
		// .
		// getCaseFrequency
		// (log);
		final HashSet<XEventClass> toKeep = new HashSet<XEventClass>(Arrays.asList(eventClassesToKeep));

		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XEventCondition() {

					public boolean keepEvent(XEvent event) {
						// only keep the event if:
						// 1) it's name is in eventClassesToKeep
						// 2) the frequency is >= minCases
						XEventClass c = allEventClasses.getClassOf(event);
						if (!toKeep.contains(c)) {
							return false;
						}
						double casePercentage = countFreq.get(c);
						double freqPercentage = countCase.get(c);
						if (fullfillBoth) {
							return (casePercentage >= minCases) && (freqPercentage >= minOccurrence);
						} else {
							return (casePercentage >= minCases) || (freqPercentage >= minOccurrence);
						}
					}

				});
	}
}
