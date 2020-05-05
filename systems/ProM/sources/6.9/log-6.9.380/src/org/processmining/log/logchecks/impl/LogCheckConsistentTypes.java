package org.processmining.log.logchecks.impl;

import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeID;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.logchecks.LogCheck;
import org.processmining.log.models.LogCheckerReport;

public class LogCheckConsistentTypes implements LogCheck {

	private enum Type {
		LITERAL("string"), //
		DISCRETE("int"), //
		CONTINUOUS("double"), //
		TIMESTAMP("date"), // 
		BOOLEAN("boolean"), //
		ID("id"), //
		LIST("list"), //
		UNKNOWN("unknown"); //

		private String label;

		Type(String label) {
			this.label = label;
		}

		public String toString() {
			return label;
		}
	};

	private static LogCheck instance;

	private LogCheckConsistentTypes() {

	}

	public static LogCheck getInstance() {
		if (instance == null) {
			instance = new LogCheckConsistentTypes();
		}
		return instance;
	}

	public boolean checkTraces(PluginContext context, XLog log, LogCheckerReport report) {
		boolean allOk = true;
		Map<String, Type> traceAttributeTypes = new HashMap<String, Type>();
		for (XAttribute attribute : log.getGlobalTraceAttributes()) {
			Type existingType = traceAttributeTypes.get(attribute.getKey());
			Type type = getType(attribute);
			if (existingType == null) {
				traceAttributeTypes.put(attribute.getKey(), type);
			} else if (type != existingType) {
				report.add(allOk ? "<h2>Inconsistent types for trace attribute</h2><ul>" : "");
				report.add("<li>Global attribute key " + attribute.getKey() + ": " + type + " vs. " + existingType + "</li>");
				allOk = false;
			}
		}
		int traceCtr = 0;
		for (XTrace trace : log) {
			for (XAttribute attribute : trace.getAttributes().values()) {
				Type existingType = traceAttributeTypes.get(attribute.getKey());
				Type type = getType(attribute);
				if (existingType == null) {
					traceAttributeTypes.put(attribute.getKey(), type);
				} else if (type != existingType) {
					String traceId = " at position " + traceCtr;
					if (trace.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
						traceId = XConceptExtension.instance().extractName(trace) + " " + traceId;
					} else {
						traceId = "<i>unknown</i> " + traceId;
					}
					report.add(allOk ? "<h2>Inconsistent types for trace attribute</h2><ul>" : "");
					report.add("<li>Trace " + traceId + ", attribute key " + attribute.getKey() + ": " + type + " vs. " + existingType + "</li>");
					allOk = false;
				}
			}
			traceCtr++;
		}
		report.add(allOk ? "" : "</ul>");
		return allOk;
	}

	public boolean checkEvents(PluginContext context, XLog log, LogCheckerReport report) {
		boolean allOk = true;
		Map<String, Type> eventAttributeTypes = new HashMap<String, Type>();
		for (XAttribute attribute : log.getGlobalEventAttributes()) {
			Type existingType = eventAttributeTypes.get(attribute.getKey());
			Type type = getType(attribute);
			if (existingType == null) {
				eventAttributeTypes.put(attribute.getKey(), type);
			} else if (type != existingType) {
				report.add(allOk ? "<h2>Inconsistent types for event attribute</h2><ul>" : "");
				report.add("<li>Attribute key " + attribute.getKey() + ": " + type + " vs. " + existingType + "</li>");
				allOk = false;
			}
		}
		int traceCtr = 0;
		for (XTrace trace : log) {
			int eventCtr = 0;
			for (XEvent event : trace) {
				for (XAttribute attribute : event.getAttributes().values()) {
					Type existingType = eventAttributeTypes.get(attribute.getKey());
					Type type = getType(attribute);
					if (existingType == null) {
						eventAttributeTypes.put(attribute.getKey(), getType(attribute));
					} else if (type != existingType) {
						String traceId = " at position " + traceCtr;
						if (trace.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
							traceId = XConceptExtension.instance().extractName(trace) + " " + traceId;
						} else {
							traceId = "<i>unknown</i> " + traceId;
						}
						String eventId = " at position " + eventCtr;
						if (event.getAttributes().containsKey(XConceptExtension.KEY_NAME)) {
							eventId = XConceptExtension.instance().extractName(event) + " " + eventId;
						} else {
							eventId = "<i>unknown</i> " + eventId;
						}
						report.add(allOk ? "<h2>Inconsistent types for event attribute</h2><ul>" : "");
						report.add("<li>Trace " + traceId + ", event " + eventId + ", attribute key " + attribute.getKey() + ": " + type + " vs. " + existingType + "</li>");
						allOk = false;
					}
				}
				eventCtr++;
			}
			traceCtr++;
		}
		report.add(allOk ? "" : "</ul>");
		return allOk;
	}

	private Type getType(XAttribute attribute) {
		if (attribute instanceof XAttributeLiteral) {
			return Type.LITERAL;
		} else if (attribute instanceof XAttributeDiscrete) {
			return Type.DISCRETE;
		} else if (attribute instanceof XAttributeTimestamp) {
			return Type.TIMESTAMP;
		} else if (attribute instanceof XAttributeContinuous) {
			return Type.CONTINUOUS;
		} else if (attribute instanceof XAttributeID) {
			return Type.ID;
		} else if (attribute instanceof XAttributeBoolean) {
			return Type.BOOLEAN;
		} else if (attribute instanceof XAttributeList) {
			return Type.LIST;
		}
		return Type.UNKNOWN;
	}

	public boolean check(PluginContext context, XLog log, LogCheckerReport report) {
		boolean okTraces = checkTraces(context, log, report);
		boolean okEvents = checkEvents(context, log, report);
		return okTraces && okEvents;
	}

}
