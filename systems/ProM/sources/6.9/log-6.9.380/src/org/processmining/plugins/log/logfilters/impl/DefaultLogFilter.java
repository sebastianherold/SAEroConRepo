package org.processmining.plugins.log.logfilters.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.XEventCondition;
import org.processmining.plugins.log.logfilters.XTraceCondition;

@Plugin(name = "Default Log Filter", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Remove", "Skip instance" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
public class DefaultLogFilter {
	/**
	 * This method filters a log by 1) removing all XEvents of which the
	 * XLifeCycleExtension
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed.
	 * @param log
	 *            The log that needs to be filtered.
	 * @param event
	 *            The event label of the event to be added
	 * @return the filtered log
	 */
	@PluginVariant(requiredParameterLabels = { 0, 1, 2 }, variantLabel = "Keep given events")
	public XLog filter(PluginContext context, XLog log, String[] lifeCycleObjectsToIgnore,
			String[] lifeCycleObjectsToRemoveCase) {
//		long time = -System.currentTimeMillis();
		final Set<?> remove = new HashSet<Object>(Arrays.asList(lifeCycleObjectsToRemoveCase));
		XLog filtered = log;
		
		if (!remove.isEmpty()) {
			// First, remove all cases containing an event of which the lifecycle extension is such
			// that the trace should be removed.
			filtered = LogFilter.filter((context != null ? context.getProgress() : null), 100, filtered,
					(context != null ? XLogInfoFactory.createLogInfo(filtered) : null), new XTraceCondition() {

						public boolean keepTrace(XTrace trace) {
							for (XEvent event : trace) {
								if (remove.contains(XLifecycleExtension.instance().extractTransition(event))) {
									return false;
								}
							}
							return true;
						}
					});
		}
//		time += System.currentTimeMillis();
//		System.err.println("[DefaultLogFilter] remove time = " + time);

//		time = -System.currentTimeMillis();
		final Set<?> ignore = new HashSet<Object>(Arrays.asList(lifeCycleObjectsToIgnore));

		if (!ignore.isEmpty()) {
			// Finally, remove all events of which the lifecycle extension is such
			// that it should be ignored.
			filtered = LogFilter.filter((context != null ? context.getProgress() : null), 100, filtered,
					(context != null ? XLogInfoFactory.createLogInfo(filtered) : null), new XEventCondition() {

						public boolean keepEvent(XEvent event) {
							if (ignore.contains(XLifecycleExtension.instance().extractTransition(event))) {
								return false;
							}
							return true;
						}
					});
		}
//		time += System.currentTimeMillis();
//		System.err.println("[DefaultLogFilter] ignore time = " + time);
		return filtered;
	}
}
