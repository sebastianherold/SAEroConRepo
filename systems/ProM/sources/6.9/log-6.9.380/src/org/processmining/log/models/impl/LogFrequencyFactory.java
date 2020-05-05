package org.processmining.log.models.impl;

import org.deckfour.xes.model.XLog;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.LogFrequency;
import org.processmining.log.models.LogFrequencyArray;


public class LogFrequencyFactory {

	public static LogFrequency createLogFrequency(XLog log) {
		return new LogFrequencyImpl(log);
	}

	public static LogFrequencyArray createLogFrequencyArray(EventLogArray logs) {
		return new LogFrequencyArrayImpl(logs);
	}
}
