package org.processmining.log.logchecks.impl;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.logchecks.LogCheck;
import org.processmining.log.models.LogCheckerReport;

public class LogCheckGlobalAttributes implements LogCheck {

	private static LogCheck instance;

	private LogCheckGlobalAttributes() {

	}

	public static LogCheck getInstance() {
		if (instance == null) {
			instance = new LogCheckGlobalAttributes();
		}
		return instance;
	}

	public boolean check(PluginContext context, XLog log, LogCheckerReport report) {
		boolean allOk = true;
		int traceCtr = 0;
		int eventCtr = 0;
		for (XTrace trace : log) {
			traceCtr++;
			eventCtr = 0;
			for (XAttribute attribute : log.getGlobalTraceAttributes()) {
				if (!trace.getAttributes().containsKey(attribute.getKey())) {
					if (allOk) {
						report.add("<h2>Trace/event misses global attributes</h2><ul>");
					}
					allOk = false;
					String traceId = " at position " + traceCtr;
					if (trace.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
						traceId = XConceptExtension.instance().extractName(trace) + " " + traceId;
					} else {
						traceId = "<i>unknown</i> " + traceId;
					}
					report.add("<li>Trace " + traceId + " misses global attribute " + attribute.getKey() + ".</li>");
				}
			}
			for (XEvent event : trace) {
				eventCtr++;
				for (XAttribute attribute : log.getGlobalEventAttributes()) {
					if (!event.getAttributes().containsKey(attribute.getKey())) {
						if (allOk) {
							report.add("<h2>Trace/event misses global attributes</h2><ul>");
						}
						allOk = false;
						String eventId = " at position " + eventCtr;
						if (event.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
							eventId = XConceptExtension.instance().extractName(event) + " " + eventId;
						} else {
							eventId = "<i>unknown</i> " + eventId;
						}
						String traceId = " at position " + traceCtr;
						if (trace.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
							traceId = XConceptExtension.instance().extractName(trace) + " " + traceId;
						} else {
							traceId = "<i>unknown</i> " + traceId;
						}
						report.add(
								"<li>Event " + eventId + " in trace " + traceId + " misses global attribute " + attribute.getKey() + ".</li>");
					}
				}
			}
		}
		if (!allOk) {
			report.add("</ul>");
		}
		return allOk;
	}

}
