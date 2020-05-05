package org.processmining.plugins.log.logfilters.ui;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Filter Log using Simple Heuristics", level= PluginLevel.PeerReviewed, categories = { PluginCategory.Filtering }, parameterLabels = { "Log" }, returnLabels = { "Log" }, returnTypes = { XLog.class }, userAccessible = true)
public class LogFilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack = "Log")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static XLog main(final UIPluginContext context, XLog log) {
		LogFilterUI filterUI = new LogFilterUI(context);
		XLog filteredLog = filterUI.filter(log);
		if (filteredLog != null) {
			XConceptExtension.instance().assignName(filteredLog, filterUI.getName());
			context.getFutureResult(0).setLabel(filterUI.getName());
		} else {
			context.log("Canceled by user.");
			context.getFutureResult(0).cancel(true);
		}
		return filteredLog;
	}
}
