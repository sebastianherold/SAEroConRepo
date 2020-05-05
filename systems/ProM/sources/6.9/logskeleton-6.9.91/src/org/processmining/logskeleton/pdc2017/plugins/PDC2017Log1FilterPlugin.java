package org.processmining.logskeleton.pdc2017.plugins;

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

@Plugin(name = "PDC 2017 Log 1 Filter", parameterLabels = { "Event Log 1" }, returnLabels = { "Filtered Log 1" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log1FilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		LogSkeletonBuilderAlgorithm skeletonBuilder = new LogSkeletonBuilderAlgorithm();
		XLog filteredLog = XFactoryRegistry.instance().currentDefault()
				.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | filter: f=j, f+d=1, b=l, c=s, d=o, d=h");
		XLog traceLog = XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		for (XTrace trace : log) {
			traceLog.clear();
			traceLog.add(trace);
			LogSkeletonCount count = skeletonBuilder.count(traceLog, classifier);
			if (count.get("f") != count.get("j")) {
				continue;
			}
			if (count.get("f") + count.get("d") != 1) {
				continue;
			}
			if (count.get("b") != count.get("l")) {
				continue;
			}
			if (count.get("c") != count.get("s")) {
				continue;
			}
			if (count.get("d") != count.get("o") || count.get("d") != count.get("h")) {
				continue;
			}
			//			if (count.get("d") != count.get("o")) {
			//				continue;
			//			}
			filteredLog.add(trace);
		}
		return filteredLog;
	}
}
