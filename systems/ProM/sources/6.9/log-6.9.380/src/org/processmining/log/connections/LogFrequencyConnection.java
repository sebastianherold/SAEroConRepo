package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.models.LogFrequency;
import org.processmining.log.parameters.LogFrequencyParameters;

public class LogFrequencyConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String LOGFREQUENCY = "Log Frequency";

	private LogFrequencyParameters parameters;

	public LogFrequencyConnection(XLog log, LogFrequency logFrequency,
			LogFrequencyParameters parameters) {
		super("Log Frequency Connection");
		put(LOG, log);
		put(LOGFREQUENCY, logFrequency);
		this.parameters = new LogFrequencyParameters(parameters);
	}

	public LogFrequencyParameters getParameters() {
		return parameters;
	}
}