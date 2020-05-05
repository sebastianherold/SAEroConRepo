package org.processmining.log.parameters;

import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.log.models.LogCentrality;


public class LogCentralityFilterParameters extends PluginParametersImpl {

	private int percentage;
	private boolean filterIn;

	public LogCentralityFilterParameters(LogCentrality centrality) {
		super();
		setPercentage(80);
		setFilterIn(true);
	}

	public LogCentralityFilterParameters(LogCentralityFilterParameters parameters) {
		super(parameters);
		setPercentage(parameters.getPercentage());
		setFilterIn(parameters.isFilterIn());
	}
	
	public void setPercentage(int percentage) {
		this.percentage = percentage;
	}

	public int getPercentage() {
		return percentage;
	}

	public void setFilterIn(boolean filterIn) {
		this.filterIn = filterIn;
	}

	public boolean isFilterIn() {
		return filterIn;
	}

	public boolean equals(Object object) {
		if (object instanceof LogCentralityFilterParameters) {
			LogCentralityFilterParameters parameters = (LogCentralityFilterParameters) object;
			return super.equals(parameters) &&
					getPercentage() == parameters.getPercentage() &&
					isFilterIn() == parameters.isFilterIn();
		}
		return false;
	}

}
