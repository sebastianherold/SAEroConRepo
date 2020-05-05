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

@Plugin(name = "PDC 2017 Log 4 Splitter", parameterLabels = { "Event Log 4" }, returnLabels = { "Split Log 4" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log4SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		// Split t over l
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("l");
		parameters.setDuplicateActivity("t");
		XLog filteredLog = apply(log, classifier, parameters);
		// Split r over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("r");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split m over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("b");
		parameters.setDuplicateActivity("m");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Done
		XConceptExtension.instance().assignName(filteredLog,
				XConceptExtension.instance().extractName(log) + " | split: [t, l], [r, b], [m, b]");
		return filteredLog;
	}

}
