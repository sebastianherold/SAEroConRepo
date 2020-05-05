package org.processmining.log.parameters;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;

public class LowOccurrencesFilterParameters extends AbstractLogFilterParameters {

	private int threshold;

	public LowOccurrencesFilterParameters(XLog log) {
		super(XUtils.getDefaultClassifier(log));
		/*
		 * Traces that occur at least 2 times will be retained.
		 */
		setThreshold(2);
	}
	
	public LowOccurrencesFilterParameters(LowOccurrencesFilterParameters parameters) {
		super(parameters);
		setThreshold(parameters.getThreshold());
	}
	
	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}
	
	public boolean equals(Object object) {
		if (object instanceof LowOccurrencesFilterParameters) {
			LowOccurrencesFilterParameters parameters = (LowOccurrencesFilterParameters) object;
			return super.equals(parameters) &&
					getThreshold() == parameters.getThreshold();
		}
		return false;
	}
}
