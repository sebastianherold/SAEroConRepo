package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.parameters.LowFrequencyFilterParameters;

public class LowFrequencyFilterConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String FILTEREDLOG = "Filtered log";

	private LowFrequencyFilterParameters parameters;

	public LowFrequencyFilterConnection(XLog log, XLog filteredLog,
			LowFrequencyFilterParameters parameters) {
		super("Low Frequency Filter Connection");
		put(LOG, log);
		put(FILTEREDLOG, filteredLog);
		this.parameters = new LowFrequencyFilterParameters(parameters);
	}

	public LowFrequencyFilterParameters getParameters() {
		return parameters;
	}
}
