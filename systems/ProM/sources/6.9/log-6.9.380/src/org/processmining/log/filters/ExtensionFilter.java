package org.processmining.log.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.Progress;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XEventEditor;
import org.processmining.plugins.log.logfilters.XTraceEditor;

/**
 * 
 * 
 * 
 * @author jvdwerf
 *
 */
public class ExtensionFilter implements XEventEditor, XTraceEditor {

	public static XLog removeExtensions(XLog log, XExtension extension) {		
		return removeExtensions(null, log, extension);
	}
	
	public static XLog removeExtensions(XLog log, String extension) {		
		return removeExtensions(null, log, extension);
	}
	
	
	public static XLog removeExtensions(Progress progress, XLog log, XExtension extension) {
		return removeExtensions(progress, log, extension.getName());
	}
	
	public static XLog removeExtensions(Progress progress, XLog log, String extension) {
		Set<String> names = new HashSet<String>();
		names.add(extension);
		
		return removeExtensions(progress, log, names);
	}
	
	public static XLog removeExtensions(Progress progress, XLog log, String... extensions) {
		return removeExtensions(progress, log, Arrays.asList(extensions));
	}
	
	public static XLog removeExtensions(Progress progress, XLog log, Collection<String> extensions) {

		ExtensionFilter ef = new ExtensionFilter((XLog) log.clone());
		
		try {
			return ef.filter(progress, extensions);
			
		} catch (LogFilterException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	//////
	///
	
	private Set<String> attrKeys;
	private Set<String> logKeys;
	private Set<String> traceKeys;
	private Set<String> eventKeys;
	
	private XLog log;
	
	public ExtensionFilter(XLog log) {
		this.log = log;
	}
	
	public XLog getLog() {
		return log;
	}
	
	public XLog filter(Progress progress, Collection<String> extensions) throws LogFilterException {
		
		//set the attribute sets, and remove extensions from log
		prepareExtensionList(extensions);
		
		removeAttributes(log, logKeys);
		
		return LogFilter.filter(progress, log.size(), log, null, this, this);
	}
	
	private void prepareExtensionList(Collection<String> extensions) {
		attrKeys = new HashSet<String>();
		logKeys = new HashSet<String>();
		traceKeys = new HashSet<String>();
		eventKeys = new HashSet<String>();
		
		Collection<XExtension> tbr = new ArrayList<XExtension>();
		
		for(XExtension ext : log.getExtensions()) {
			if (extensions.contains(ext.getName())) {
				for(XAttribute attr : ext.getLogAttributes()) {
					logKeys.add(attr.getKey());
				}
				for(XAttribute attr : ext.getTraceAttributes()) {
					traceKeys.add(attr.getKey());
				}
				for(XAttribute attr : ext.getEventAttributes()) {
					eventKeys.add(attr.getKey());
				}
				for(XAttribute attr : ext.getMetaAttributes()) {
					attrKeys.add(attr.getKey());
				}
				
				tbr.add(ext);
			}
		}
		
		log.getExtensions().removeAll(tbr);
	}
	
	public XTrace editTrace(XTrace trace) {
		
		removeAttributes(trace, traceKeys);
		
		return trace;
	}

	public XEvent editEvent(XEvent event) {
		XEvent editedEvent = (XEvent) event.clone();
		
		removeAttributes(editedEvent, eventKeys);
		
		return editedEvent;
	}
	
	private void removeAttributes(XAttributable target, Set<String> items) {
		for(String key : items) {
			target.getAttributes().remove(key);
		}
		for(Entry<String,XAttribute> attr : target.getAttributes().entrySet()) {
			removeAttributes(attr.getValue(), attrKeys);
		}
	}
	
	
	
	
}
