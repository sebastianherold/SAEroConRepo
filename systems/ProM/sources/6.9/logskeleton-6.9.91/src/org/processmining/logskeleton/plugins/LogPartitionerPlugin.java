package org.processmining.logskeleton.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.models.EventLogArray;
import org.processmining.logskeleton.algorithms.LogPartitionerAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;

@Plugin(name = "Partition Log on Activity Sets", icon = "prom_duck_300.png", url = "http://www.win.tue.nl/~hverbeek", parameterLabels = {
		"Event log" }, returnLabels = { "Event Log Array" }, returnTypes = {
				EventLogArray.class }, userAccessible = true, help = "Filter Event Log on Log Skeleton")
public class LogPartitionerPlugin extends LogPartitionerAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public EventLogArray run(PluginContext context, XLog log) {
		return run(context, log, new LogSkeletonClassifier());
	}

	public EventLogArray run(PluginContext context, XLog log, XEventClassifier classifier) {
		EventLogArray logs = apply(log, classifier);
		for (int i = 0; i < logs.getSize(); i++) {
			context.getProvidedObjectManager().createProvidedObject(
					XConceptExtension.instance().extractName(logs.getLog(i)), logs.getLog(i), XLog.class, context);
		}
		return logs;
	}

}
