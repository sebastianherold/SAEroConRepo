/*
 * Copyright (c) 2014 F. Mannhardt (f.mannhardt@tue.nl)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package org.processmining.log.utils;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

/**
 * Fluent-style builder for create a XLog in an easy way. Get an instance by
 * calling {@link XLogBuilder#newInstance()}, then it can be used as follows: 

 * <pre>
 * {@code
 *  XLog log = XLogBuilder.newInstance()
 *	 	.startLog("logName")
 *		.addTrace("traceName", 2)
 *		.addAttribute("traceAttribute", "test")
 *			.addEvent("Event1")
 *			.addAttribute("eventAttribute", 21)
 *			.addEvent("Event2")
 *			.addEvent("Event3")
 *			.addEvent("Event4", 2)
 *		.build();
 * }
 * </pre>
 * Please note that a {@link XLogBuilder} instance is design to be used to 
 * create one log only. 
 * 
 * @author F. Mannhardt
 * 
 */
public class XLogBuilder {

	public static XLogBuilder newInstance() {
		return new XLogBuilder();
	}

	private XFactory factory = XFactoryRegistry.instance().currentDefault();
	private final XConceptExtension conceptInstance = XConceptExtension.instance();

	private XLog log = null;

	private XTrace currentTrace = null;
	private int currentTraceMultiplicity = 1;

	private XEvent currentEvent = null;
	private int currentEventMultiplicity;

	public XLogBuilder startLog(String name) {
		log = factory.createLog();
		if (log != null) {
			conceptInstance.assignName(log, name);
		}
		return this;
	}

	public XLogBuilder addTrace(String name) {
		return addTrace(name, 1);
	}

	public XLogBuilder addTrace(String name, int numberOfTraces) {
		if (log == null) {
			throw new IllegalStateException("Please call 'startLog' first!");
		}
		if (currentEvent != null) {
			addCurrentEventToTrace();
		}
		if (currentTrace != null) {
			addCurrentTraceToLog();
			currentEvent = null;
		}
		currentTrace = factory.createTrace();
		if (name != null) {
			conceptInstance.assignName(currentTrace, name);
		}
		currentTraceMultiplicity = numberOfTraces;
		return this;
	}

	private void addCurrentTraceToLog() {
		log.add(currentTrace);
		if (currentTraceMultiplicity > 1) {
			for (int i = 0; i < currentTraceMultiplicity - 1; i++) {
				XTrace clone = (XTrace) currentTrace.clone();
				String name = conceptInstance.extractName(clone);
				if (name != null) {
					conceptInstance.assignName(clone, name.concat("-").concat(String.valueOf(i+1)));
				}
				log.add(clone);
			}
		}
	}

	public XLogBuilder addEvent(String name) {
		addEvent(name, 1);
		return this;
	}

	public XLogBuilder addEvent(String name, int numberOfEvents) {
		if (currentTrace == null) {
			throw new IllegalStateException("Please call 'addTrace' first!");
		}
		if (currentEvent != null) {
			addCurrentEventToTrace();
		}
		currentEvent = factory.createEvent();
		conceptInstance.assignName(currentEvent, name);
		currentEventMultiplicity = numberOfEvents;
		return this;
	}

	private void addCurrentEventToTrace() {
		currentTrace.add(currentEvent);
		if (currentEventMultiplicity > 1) {
			for (int i = 0; i < currentEventMultiplicity - 1; i++) {
				currentTrace.add((XEvent) currentEvent.clone());
			}
		}
	}
	
	/**
	 * Add the given attribute
	 * 
	 * @param attribute
	 * @return {@link XLogBuilder}
	 */
	public XLogBuilder addAttribute(XAttribute attribute) {
		addAttributeInternal(attribute.getKey(), attribute);
		return this;
	}
	
	/**
	 * @param name
	 * @param value
	 * @return
	 */
	public XLogBuilder addAttribute(String name, boolean value) {
		XAttribute attribute = factory.createAttributeBoolean(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return the {@link XLogBuilder} itself
	 */
	public XLogBuilder addAttribute(String name, long value) {
		XAttribute attribute = factory.createAttributeDiscrete(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return the {@link XLogBuilder} itself
	 */
	public XLogBuilder addAttribute(String name, String value) {
		XAttribute attribute = factory.createAttributeLiteral(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return the {@link XLogBuilder} itself
	 */
	public XLogBuilder addAttribute(String name, Date value) {
		XAttribute attribute = factory.createAttributeTimestamp(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}

	/**
	 * @param name
	 * @param value
	 * @return the {@link XLogBuilder} itself
	 */
	public XLogBuilder addAttribute(String name, double value) {
		XAttribute attribute = factory.createAttributeContinuous(name, value, null);
		addAttributeInternal(name, attribute);
		return this;
	}
	
	public XLogBuilder setFactory(XFactory factory) {
		this.factory = factory;
		return this;
	}

	private void addAttributeInternal(String name, XAttribute attribute) {
		if (currentEvent == null && currentTrace == null) {
			throw new IllegalStateException("Please call 'addEvent' or 'addTrace' first!");
		}

		if (currentEvent == null) {
			// Trace Attributes			
			currentTrace.getAttributes().put(name, attribute);
		} else {
			// Event Attributes
			currentEvent.getAttributes().put(name, attribute);
		}
	}

	/**
	 * Builds and returns the XLog. This is only to be used once! 
	 * 
	 * @return the final XLog
	 */ 
	public XLog build() {
		if (currentEvent != null) {
			addCurrentEventToTrace();
		}
		if (currentTrace != null) {
			addCurrentTraceToLog();
		}
		return log;
	}

}
