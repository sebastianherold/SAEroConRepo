package org.processmining.log.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.log.parameters.LowOccurrencesFilterParameters;

public class LowOccurrencesFilterConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String FILTEREDLOG = "Filtered log";

	private LowOccurrencesFilterParameters parameters;

	public LowOccurrencesFilterConnection(XLog log, XLog filteredLog,
			 LowOccurrencesFilterParameters parameters) {
		super("Low Occurrences Filter Connection");
		put(LOG, log);
		put(FILTEREDLOG, filteredLog);
		this.parameters = new LowOccurrencesFilterParameters(parameters);
	}

	public  LowOccurrencesFilterParameters getParameters() {
		return parameters;
	}
}
