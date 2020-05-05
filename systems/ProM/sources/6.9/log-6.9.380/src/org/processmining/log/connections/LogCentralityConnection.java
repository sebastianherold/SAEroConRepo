package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityParameters;

public class LogCentralityConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String LOGCENTRALITY = "Log Centrality";

	private LogCentralityParameters parameters;

	public LogCentralityConnection(XLog log, LogCentrality logCentrality,
			LogCentralityParameters parameters) {
		super("Log Centrality Connection");
		put(LOG, log);
		put(LOGCENTRALITY, logCentrality);
		this.parameters = new LogCentralityParameters(parameters);
	}

	public LogCentralityParameters getParameters() {
		return parameters;
	}
}