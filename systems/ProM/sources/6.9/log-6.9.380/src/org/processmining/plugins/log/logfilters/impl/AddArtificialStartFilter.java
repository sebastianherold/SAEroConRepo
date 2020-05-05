package org.processmining.plugins.log.logfilters.impl;

import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XTraceEditor;

@Plugin(name = "Add Artificial Start Event Filter", parameterLabels = { "Log", "Event Label" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class AddArtificialStartFilter {
	/**
	 * This method filters a log by adding an artificial start event to each
	 * trace in the log.
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed.
	 * @param log
	 *            The log that needs to be filtered.
	 * @param event
	 *            The event label of the event to be added
	 * @return the filtered log
	 * @throws LogFilterException
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1 }, variantLabel = "Keep given events")
	public XLog filter(PluginContext context, XLog log, final XEvent event) throws LogFilterException {
		// Construct a sorted set of events for easy lookup

		return LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XTraceEditor() {

					public XTrace editTrace(XTrace trace) {
						// Keep the trace if the first event is contained in the
						// given set.
						trace.add(0, event);
						return trace;
					}
				});
	}

}
