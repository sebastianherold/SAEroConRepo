package org.processmining.plugins.log.filter;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class AttributeLogFilter {

	public static final String NONE = "none";
	public static final String TRACE_ATTRIBUTE = "trace attribute";
	public static final String EVENT_ATTRIBUTE = "trace with an event having this attribute";

	public String attribute_filterOn = TRACE_ATTRIBUTE;

	public boolean attribute_include = true;
	public String attribute_key = null;
	public Set<String> attribute_values = new HashSet<String>();

	public int length_min_value = 0;
	public int length_max_value = Integer.MAX_VALUE;

	public XLog log;

	public AttributeLogFilter(XLog log) {
		this.log = log;
		setDefaultValues();
	}

	public void setDefaultValues() {
		this.attribute_filterOn = NONE;
		int min_length = Integer.MAX_VALUE;
		int max_length = Integer.MIN_VALUE;
		for (XTrace t : log) {
			if (t.size() < min_length)
				min_length = t.size();
			if (t.size() > max_length)
				max_length = t.size();
		}
		length_min_value = min_length;
		length_max_value = max_length;
	}

	public boolean satisfies(XAttributeMap attributes) {
		if (!attributes.containsKey(attribute_key)) {
			return false;
		}
		XAttribute attr = attributes.get(attribute_key);
		// the only way to get the value consistently out of all the attribute subclasses
		String attr_value = attr.toString();

		return attribute_values.contains(attr_value);
	}

	public boolean keepTraceOnAttributes(XTrace trace) {

		if (attribute_filterOn == TRACE_ATTRIBUTE) {
			XAttributeMap attributes = trace.getAttributes();
			if (satisfies(attributes)) {
				return attribute_include;
			} else {
				return !attribute_include;
			}

		} else if (attribute_filterOn == EVENT_ATTRIBUTE) {
			for (XEvent e : trace) {
				XAttributeMap attributes = e.getAttributes();
				if (satisfies(attributes)) {
					if (attribute_include)
						return true;
					else
						return false;
				}
			}
			if (attribute_include)
				return false;
			else
				return true;
		}
		return false;
	}

	public boolean keepTraceOnLength(XTrace trace) {
		if (length_min_value <= trace.size() && trace.size() <= length_max_value)
			return true;
		else
			return false;
	}

	public boolean keepTrace(XTrace trace) {

		if (attribute_filterOn != NONE) {
			if (!keepTraceOnAttributes(trace))
				return false;
		}
		if (!keepTraceOnLength(trace))
			return false;

		return true;
	}

}