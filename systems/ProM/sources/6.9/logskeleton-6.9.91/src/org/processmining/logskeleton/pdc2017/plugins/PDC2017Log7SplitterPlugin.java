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

@Plugin(name = "PDC 2017 Log 7 Splitter", parameterLabels = { "Event Log 7"}, returnLabels = { "Split Log 7" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017Log7SplitterPlugin extends SplitterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitterParameters parameters = new SplitterParameters();
		XEventClassifier classifier = new LogSkeletonClassifier(new XEventNameClassifier());

		// Split n over f
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("f");
		parameters.setDuplicateActivity("n");
		XLog filteredLog = apply(log, classifier, parameters);
		// Split h over i
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("i");
		parameters.setDuplicateActivity("h");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split c over i
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("i");
		parameters.setDuplicateActivity("c");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split h.0 over c.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("c.0");
//		parameters.getMilestoneActivities().add("h.0");
		parameters.setDuplicateActivity("h.0");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split p over e
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("e");
		parameters.setDuplicateActivity("p");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Split c.0 over h.0.0
		parameters.getMilestoneActivities().clear();
		parameters.getMilestoneActivities().add("h.0.0");
		parameters.setDuplicateActivity("c.0");
		filteredLog = apply(filteredLog, classifier, parameters);
		// Done, except for b...
		XConceptExtension.instance().assignName(
				filteredLog,
				XConceptExtension.instance().extractName(log)
						+ " | split: [n, f], [h, i], [c,i], [h.0, c.0], [p, e], [c.0, h.0.0], 7B");
		return apply7B(filteredLog, classifier);
	}

}
