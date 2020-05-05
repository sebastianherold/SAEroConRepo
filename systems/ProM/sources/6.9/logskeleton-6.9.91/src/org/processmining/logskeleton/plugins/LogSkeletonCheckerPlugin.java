package org.processmining.logskeleton.plugins;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonCheckerAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.models.LogSkeleton;

@Plugin(name = "Filter Event Log on Log Skeleton", icon = "prom_duck_300.png", url = "http://www.win.tue.nl/~hverbeek", parameterLabels = { "Log Skeleton", "Event log"}, returnLabels = { "Filtered Event Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "Filter Event Log on Log Skeleton")
public class LogSkeletonCheckerPlugin extends LogSkeletonCheckerAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0, 1 })
	public XLog run(PluginContext context, LogSkeleton model, XLog log) {
		return run(context, model, log, new LogSkeletonClassifier());
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, XEventClassifier classifier) {
		boolean[] checks = new boolean[] { true, true, true };
		return run(context, model, log, classifier, new HashSet<String>(), checks);
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, Set<String> messages, boolean[] checks) {
		XEventClassifier classifier = new LogSkeletonClassifier();
		return run(context, model, log, classifier, messages, checks);
	}

	public XLog run(PluginContext context, LogSkeleton model, XLog log, XEventClassifier classifier, Set<String> messages, boolean[] checks) {
		return apply(model, log, classifier, messages, checks);
	}
}
