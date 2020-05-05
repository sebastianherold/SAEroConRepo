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

@Plugin(name = "PDC 2019 Log 3 Filter", parameterLabels = { "Event Log 1" }, returnLabels = { "Filtered Log 3" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2019 Plug-in")
public class PDC2019Log3FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		LogSkeletonBuilderAlgorithm skeletonBuilder = new LogSkeletonBuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | filter: ...");
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog, classifier);
			if (count.get("al") != count.get("l")) {
				continue;
			}
			if (count.get("ap") != count.get("j")) {
				continue;
			}
			if (count.get("ad") != count.get("i")) {
				continue;
			}
			if (count.get("b") != count.get("u")) {
				continue;
			}
			if (count.get("aq") != count.get("ar")) {
				continue;
			}
			if (count.get("e") != count.get("t")) {
				continue;
			}
			if (count.get("d") != count.get("o")) {
				continue;
			}
			if (count.get("w") != count.get("ag")) {
				continue;
			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}
