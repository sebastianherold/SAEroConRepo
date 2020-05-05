package org.processmining.log.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.logchecks.LogCheckType;
import org.processmining.log.models.LogCheckerReport;
import org.processmining.log.parameters.LogCheckerParameters;

public class LogCheckerAlgorithm {

	public LogCheckerReport apply(PluginContext context, final XLog log, final LogCheckerParameters parameters) {
		LogCheckerReport report = new LogCheckerReport();
		boolean allOk = true;
		for (LogCheckType logCheckType: parameters.getLogChecks()) {
			allOk = logCheckType.getLogCheck().check(context, log, report) && allOk;
		}
		if (allOk) {
			report.add("<h2>No problems detected, congratulations!</h2>");
		}
		return report;
	}
}
