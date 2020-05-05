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

@Plugin(name = "PDC 2019 Log 1 Filter", parameterLabels = { "Event Log 1" }, returnLabels = { "Filtered Log 1" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2019 Plug-in")
public class PDC2019Log1FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		LogSkeletonBuilderAlgorithm skeletonBuilder = new LogSkeletonBuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | filter: ai=o, ai+x+f=1, an+aj=1, ab+n+ah=an, ac+ak=1, ag=1, ad+ap+m=v");
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog, classifier);
			if (count.get("ai") != count.get("o")) {
				continue;
			}
			if (count.get("ai") + count.get("x") + count.get("f") != 1) {
				continue;
			}
			if (count.get("an") + count.get("aj") != 1) {
				continue;
			}
			if (count.get("ab") + count.get("n") + count.get("ah") != count.get("an")) {
				continue;
			}
			if (count.get("ac") + count.get("ak") != 1) {
				continue;
			}
			if (count.get("ag") != 1) {
				continue;
			}
			if (count.get("ad") + count.get("ap") + count.get("m") != count.get("v")) {
				continue;
			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}
