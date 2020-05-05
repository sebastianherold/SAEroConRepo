package org.processmining.logskeleton.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.models.LogSkeleton;

@Plugin(name = "Build Log Skeleton from Event Log", icon = "prom_duck_300.png", url = "http://www.win.tue.nl/~hverbeek", parameterLabels = { "Event log"}, returnLabels = { "Log Skeleton" }, returnTypes = { LogSkeleton.class }, userAccessible = true, help = "Create Log Skeleton from Event Log")
public class LogSkeletonBuilderPlugin extends LogSkeletonBuilderAlgorithm  {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public LogSkeleton run(PluginContext context, XLog log) {
		return apply(log, new LogSkeletonClassifier());
	}

	public LogSkeleton run(PluginContext context, XLog log, XEventClassifier classifier) {
		return apply(log, classifier);
	}
}
