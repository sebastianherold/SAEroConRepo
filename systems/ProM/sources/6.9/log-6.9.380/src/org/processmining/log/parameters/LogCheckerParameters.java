package org.processmining.log.parameters;

import java.util.EnumSet;

import org.processmining.log.logchecks.LogCheckType;

public class LogCheckerParameters {

	private EnumSet<LogCheckType> logChecks;

	public EnumSet<LogCheckType> getLogChecks() {
		return logChecks;
	}

	public void setLogChecks(EnumSet<LogCheckType> logChecks) {
		this.logChecks = logChecks;
	}
	
}
