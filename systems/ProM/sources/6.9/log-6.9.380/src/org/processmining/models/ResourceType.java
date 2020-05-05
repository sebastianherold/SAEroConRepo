package org.processmining.models;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.model.ProMResourceTypeInformation;

public class ResourceType {
	static {
		ProMResourceTypeInformation.getInstance().setInfoFor(XLog.class, "Event Log",
				"Eindhoven University of Technology", "h.m.w.verbeek@tue.nl", "Eric Verbeek",
				"http://www.processmining.org", "resourcetype_log_30x35.png");

	}
}
