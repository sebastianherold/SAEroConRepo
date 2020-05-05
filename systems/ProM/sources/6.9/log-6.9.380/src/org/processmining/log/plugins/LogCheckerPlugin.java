package org.processmining.log.plugins;

import java.util.EnumSet;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.log.algorithms.LogCheckerAlgorithm;
import org.processmining.log.help.LogCheckerHelp;
import org.processmining.log.logchecks.LogCheckType;
import org.processmining.log.models.LogCheckerReport;
import org.processmining.log.parameters.LogCheckerParameters;

@Plugin(name = "Check Log", categories = {}, parameterLabels = { "Log", "Parameters" }, returnLabels = {
		"Log Check Report" }, returnTypes = { HTMLToString.class }, help = LogCheckerHelp.TEXT)
public class LogCheckerPlugin extends LogCheckerAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Check Log, default", requiredParameterLabels = { 0 })
	public LogCheckerReport runDefault(PluginContext context, XLog log) {
		LogCheckerParameters parameters = new LogCheckerParameters();
		parameters.setLogChecks(EnumSet.of(
				LogCheckType.LOG_CHECK_EVENT_CLASSIFIERS_GLOBAL,
				LogCheckType.LOG_CHECK_GLOBAL_ATTRIBUTE, 
				LogCheckType.LOG_CHECK_CONSISTENT_TYPES));
		return apply(context, log, parameters);
	}
}
