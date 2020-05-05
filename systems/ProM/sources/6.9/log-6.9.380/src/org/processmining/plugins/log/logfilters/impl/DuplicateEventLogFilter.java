package org.processmining.plugins.log.logfilters.impl;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XTraceEditor;

@Plugin(name = "Duplicate Event Log Filter", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Event Classes" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class DuplicateEventLogFilter {
	/**
	 * This method filters a log by removing events from a trace, if the event
	 * is of the same XEventClass as the previous event
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed.
	 * @param log
	 *            The log that needs to be filtered.
	 * @param events
	 *            The event classes that are distinguished.
	 * @return the filtered log
	 * @throws LogFilterException
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Keep given events")
	public XLog filter(PluginContext context, XLog log, final XEventClasses events) throws LogFilterException {
		// Construct a sorted set of events for easy lookup

		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XTraceEditor() {

					public XTrace editTrace(XTrace trace) {
						// Keep the trace if the first event is contained in the
						// given set.
						for (int i = 1; i < trace.size(); i++) {
							if (events.getClassOf(trace.get(i)).equals(events.getClassOf(trace.get(i - 1)))) {
								trace.remove(i);
								i--;
							}
						}

						// No check is necessary for empty traces for 2 reasons:
						// 1) the trace cannot become empty, as the first element is
						// never removed
						// 2) even if the trace becomes empty, the LogFilter code
						// removes the trace
						return trace;
					}
				});
	}
}
