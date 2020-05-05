package org.processmining.log.models.impl;

import org.processmining.log.models.EventLogArray;

public class EventLogArrayFactory {

	public static EventLogArray createEventLogArray() {
		return new EventLogArrayImpl();
	}
	
}
