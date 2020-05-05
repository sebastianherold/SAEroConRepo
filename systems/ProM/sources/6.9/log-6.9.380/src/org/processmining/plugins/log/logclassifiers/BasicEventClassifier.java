package org.processmining.plugins.log.logclassifiers;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.classification.XEventResourceClassifier;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Basic event classifier", parameterLabels = {}, returnLabels = { "Event classifier" }, returnTypes = { XEventAttributeClassifier.class }, userAccessible = true)
public class BasicEventClassifier {

	@PluginVariant(variantLabel = "Activity name classifier", requiredParameterLabels = {})
	public static XEventAttributeClassifier getEventNameClassifier(final PluginContext context) {
		return new XEventNameClassifier();
	}

	@PluginVariant(variantLabel = "Resource classifier", requiredParameterLabels = {})
	public static XEventAttributeClassifier getEventResourceClassifier(final PluginContext context) {
		return new XEventResourceClassifier();
	}

	@PluginVariant(variantLabel = "Lifecycle transition classifier", requiredParameterLabels = {})
	public static XEventAttributeClassifier getEventLifeTransClassifier(final PluginContext context) {
		return new XEventLifeTransClassifier();
	}

}
