package org.processmining.log.parameters;

import org.deckfour.xes.classification.XEventClassifier;

public interface ClassifierParameter {

	public void setClassifier(XEventClassifier classifier);
	public XEventClassifier getClassifier();
}
