package org.processmining.log.parameters;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;

public class LowFrequencyFilterParameters extends AbstractLogFilterParameters {

	private int threshold;

	public LowFrequencyFilterParameters(XLog log) {
		super(XUtils.getDefaultClassifier(log));
		/*
		 * The least-occurring traces that make up at least 5% of the log will be removed.
		 */
		setThreshold(5); 
	}
	
	public LowFrequencyFilterParameters(LowFrequencyFilterParameters parameters) {
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
		if (object instanceof LowFrequencyFilterParameters) {
			LowFrequencyFilterParameters parameters = (LowFrequencyFilterParameters) object;
			return super.equals(parameters) &&
					getThreshold() == parameters.getThreshold();
		}
		return false;
	}
}
