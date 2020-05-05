package org.processmining.plugins.log.logclassifiers;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class EventAndClassifier {

	@Plugin(name = "And event classifier", parameterLabels = { "Event classifiers" }, returnLabels = { "Event classifier" }, returnTypes = { XEventAndClassifier.class }, userAccessible = true)
	public static XEventAndClassifier getEventNameClassifier(final PluginContext context, XEventClassifier[] classifiers) {
		return new XEventAndClassifier(classifiers);
	}

}
