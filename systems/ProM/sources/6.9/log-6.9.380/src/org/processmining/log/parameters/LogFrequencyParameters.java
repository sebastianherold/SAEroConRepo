package org.processmining.log.parameters;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;

public class LogFrequencyParameters extends AbstractLogFilterParameters {

	public LogFrequencyParameters(XLog log) {
		super(XUtils.getDefaultClassifier(log));
	}
	
	public LogFrequencyParameters(LogFrequencyParameters parameters) {
		super(parameters);
	}

	public boolean equals(Object object) {
		if (object instanceof LogFrequencyParameters) {
			LogFrequencyParameters parameters = (LogFrequencyParameters) object;
			return super.equals(parameters);
		}
		return false;
	}
}
