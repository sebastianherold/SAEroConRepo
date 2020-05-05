package org.processmining.log.logchecks;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.LogCheckerReport;

public interface LogCheck {

	public boolean check(PluginContext context, XLog log, LogCheckerReport report);
	
}
