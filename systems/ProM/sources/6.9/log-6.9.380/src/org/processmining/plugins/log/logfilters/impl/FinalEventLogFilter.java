package org.processmining.plugins.log.logfilters.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XTraceCondition;

@Plugin(name = "Final Event Log Filter", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Event Classes", "Classifier", "Start IDs" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class FinalEventLogFilter {
	/**
	 * This method filters a log by removing all traces from the log which do
	 * not end with one of the given events
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed
	 * @param log
	 *            The log that needs to be filtered.
	 * @param events
	 *            The event classes that can serve as end events of a trace
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Keep given events")
	public XLog filterWithNames(PluginContext context, XLog log, final XEventClasses events) {
		// Construct a sorted set of events for easy lookup

		return LogFilter.filter((context != null ? context.getProgress() : null), 100, log, (context != null ? XLogInfoFactory.createLogInfo(log) : null),
				new XTraceCondition() {

					public boolean keepTrace(XTrace trace) {
						// Keep the trace if the first event is contained in the
						// given set.
						return !trace.isEmpty() && events.getClasses().contains(events.getClassOf(trace.get(trace.size() - 1)));
					}

				});
	}

	@PluginVariant(requiredParameterLabels = { 0, 2, 3 }, variantLabel = "Keep given events")
	public XLog filterWithClassifier(PluginContext context, XLog log, final XEventClassifier classifier,
			final String[] finalIds) {
		// Construct a sorted set of events for easy lookup
		final Collection<String> ids = new HashSet<String>(Arrays.asList(finalIds));

		return LogFilter.filter((context != null ? context.getProgress() : null), 100, log, (context != null ? XLogInfoFactory.createLogInfo(log) : null),
				new XTraceCondition() {

					public boolean keepTrace(XTrace trace) {
						// Keep the trace if the first event is contained in the
						// given set.
						return !trace.isEmpty() && ids.contains(classifier.getClassIdentity(trace.get(trace.size() - 1)));
					}

				});
	}
}
