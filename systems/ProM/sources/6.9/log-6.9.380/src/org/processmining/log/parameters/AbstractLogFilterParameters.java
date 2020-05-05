package org.processmining.log.parameters;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;

public abstract class AbstractLogFilterParameters extends PluginParametersImpl implements LogFilterParameters {

	private XEventClassifier classifier;

	public AbstractLogFilterParameters(XEventClassifier classifier) {
		super();
		setClassifier(classifier);
	}

	public AbstractLogFilterParameters(AbstractLogFilterParameters parameters) {
		super(parameters);
		setClassifier(parameters.getClassifier());
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public boolean equals(Object object) {
		if (object instanceof AbstractLogFilterParameters) {
			AbstractLogFilterParameters parameters = (AbstractLogFilterParameters) object;
			return super.equals(parameters) 
					&& getClassifier().equals(parameters.getClassifier());
		}
		return false;
	}
}
