package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityFilterParameters;

public class LogCentralityFilterConnection  extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String LOGCENTRALITY = "Log Centrality";

	private LogCentralityFilterParameters parameters;

	public LogCentralityFilterConnection(XLog log, LogCentrality logCentrality,
			LogCentralityFilterParameters parameters) {
		super("Log Centrality Filter Connection");
		put(LOG, log);
		put(LOGCENTRALITY, logCentrality);
		this.parameters = new LogCentralityFilterParameters(parameters);
	}

	public LogCentralityFilterParameters getParameters() {
		return parameters;
	}
}