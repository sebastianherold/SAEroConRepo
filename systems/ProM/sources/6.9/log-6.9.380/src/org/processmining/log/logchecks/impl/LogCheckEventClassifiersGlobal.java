package org.processmining.log.logchecks.impl;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.logchecks.LogCheck;
import org.processmining.log.models.LogCheckerReport;

public class LogCheckEventClassifiersGlobal implements LogCheck {

	private static LogCheck instance;
	
	private LogCheckEventClassifiersGlobal() {
		
	}
	
	public static LogCheck getInstance() {
		if (instance == null) {
			instance = new LogCheckEventClassifiersGlobal();
		}
		return instance;
	}
	
	public boolean check(PluginContext context, XLog log, LogCheckerReport report) {
		boolean checkOk = true;
		for (XEventClassifier classifier : log.getClassifiers()) {
			for (String key : classifier.getDefiningAttributeKeys()) {
				boolean keyFound = false;
				for (XAttribute attribute : log.getGlobalEventAttributes()) {
					keyFound = attribute.getKey().equals(key);
					if (keyFound) {
						break;
					}
				}
				if (!keyFound) {
					String message = checkOk ? "<h2>Event classifier uses non-global attribute</h2><ul>" : "";
					checkOk = false;
					message += "<li>Classifier " + classifier.name() + " uses key " + key + ", but key " + key + " is not declared as being global.</li>"; 
					report.add(message);
				}
			}
		}
		if (!checkOk) {
			report.add("</ul>");
		}
		return checkOk;
	}

}
