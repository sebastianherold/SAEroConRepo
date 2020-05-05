package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.parameters.HighFrequencyFilterParameters;

public class HighFrequencyFilterConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String FILTEREDLOG = "Filtered log";

	private HighFrequencyFilterParameters parameters;

	public HighFrequencyFilterConnection(XLog log, XLog filteredLog,
			HighFrequencyFilterParameters parameters) {
		super("High Frequency Filter Connection");
		put(LOG, log);
		put(FILTEREDLOG, filteredLog);
		this.parameters = new HighFrequencyFilterParameters(parameters);
	}

	public HighFrequencyFilterParameters getParameters() {
		return parameters;
	}
}
