package org.processmining.log.parameters;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class SplitLogParameters {

	private String key;

	public SplitLogParameters(XLog log) {
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (XAttribute attribute : event.getAttributes().values()) {
					if (attribute instanceof XAttributeLiteral) {
						setKey(attribute.getKey());
						break;
					}
				}
			}
		}
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
