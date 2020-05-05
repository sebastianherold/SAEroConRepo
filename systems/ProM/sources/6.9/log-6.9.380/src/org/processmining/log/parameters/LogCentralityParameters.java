package org.processmining.log.parameters;

import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XUtils;

public class LogCentralityParameters extends AbstractLogFilterParameters {

	public LogCentralityParameters(XLog log) {
		super(XUtils.getDefaultClassifier(log));
	}

	public LogCentralityParameters(LogCentralityParameters parameters) {
		super(parameters);
	}

	public boolean equals(Object object) {
		if (object instanceof LogCentralityParameters) {
			LogCentralityParameters parameters = (LogCentralityParameters) object;
			return super.equals(parameters);
		}
		return false;
	}
}
