package org.processmining.logskeleton.pdc2019.plugins;

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

@Plugin(name = "PDC 2019 Log 9 Splitter", parameterLabels = { "Event Log 9"}, returnLabels = { "Split Log 9" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2019Log9SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		// Split t over v
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("s");
		parameters.setDuplicateActivity("s");
		XLog filteredLog = apply(log, classifier, parameters);
		// Split ad over k
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("o");
		parameters.setDuplicateActivity("w");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split ad.0 over ad.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("o");
		parameters.setDuplicateActivity("al");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split k over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ai");
		parameters.setDuplicateActivity("ai");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split z over ad.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ai.1");
		parameters.setDuplicateActivity("z");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split h over z.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("z.0");
		parameters.setDuplicateActivity("d");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split p over b
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q");
		parameters.setDuplicateActivity("q");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ai.1");
		parameters.setDuplicateActivity("ai.1");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("aj");
		parameters.setDuplicateActivity("aj");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("ac");
		parameters.setDuplicateActivity("ac");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("n");
		parameters.setDuplicateActivity("n");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o over o
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("z.1");
		parameters.setDuplicateActivity("c");
		filteredLog = apply(filteredLog, classifier, parameters);
		XConceptExtension.instance().assignName(
				filteredLog,
				XConceptExtension.instance().extractName(log)
						+ " | split: ...");
		return filteredLog;
	}

}
