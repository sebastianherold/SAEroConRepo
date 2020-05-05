package org.processmining.logskeleton.pdc2017.plugins;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.logskeleton.algorithms.SplitterAlgorithm;
import org.processmining.logskeleton.classifiers.LogSkeletonClassifier;
import org.processmining.logskeleton.parameters.SplitterParameters;

@Plugin(name = "PDC 2017 Log 2 Splitter", parameterLabels = { "Event Log 2" }, returnLabels = { "Split Log 2" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log2SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XLog filteredLog = log;
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		// Split a over itself
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("a");
		parameters.setDuplicateActivity("a");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split s over itself
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("s");
		parameters.setDuplicateActivity("s");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Done
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | split: [a, a], [s, s]");
		return filteredLog;
	}
}
