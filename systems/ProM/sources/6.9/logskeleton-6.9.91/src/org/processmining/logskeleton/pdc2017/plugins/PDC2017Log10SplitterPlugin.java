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

@Plugin(name = "PDC 2017 Log 10 Splitter", parameterLabels = { "Event Log 10" }, returnLabels = { "Split Log 10" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log10SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		// Split o over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("o");
		XLog filteredLog = apply(log, classifier, parameters);
		// Split i over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("i");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split q over q
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q");
		parameters.setDuplicateActivity("q");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split j over j
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("j");
		parameters.setDuplicateActivity("j");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split g over q.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q.1");
		parameters.setDuplicateActivity("g");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split o.1 over o.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("o.1");
		parameters.setDuplicateActivity("o.1");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split q.1 over q.1
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("q.1");
		parameters.setDuplicateActivity("q.1");
		filteredLog = apply(filteredLog, classifier, parameters);
		XConceptExtension.instance().assignName(
				filteredLog,
				XConceptExtension.instance().extractName(log)
						+ " | split: [o, j], [i, j], [q, q], [j, j], [g, q.1], [o.1, o.1], [q.1, q.1]");
		return filteredLog;
	}

}
