package org.processmining.logskeleton.pdc2019.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.LogSkeletonBuilderAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.models.LogSkeletonCount;

@Plugin(name = "PDC 2019 Log 2 Filter", parameterLabels = { "Event Log 1" }, returnLabels = { "Filtered Log 2" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2019 Plug-in")
public class PDC2019Log2FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		LogSkeletonBuilderAlgorithm skeletonBuilder = new LogSkeletonBuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | filter: ai=1");
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog, classifier);
			if (count.get("ai") != 1) {
				continue;
			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}
