package org.processmining.plugins.log.logfilters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;

public class AttributeFilterParameters {

	protected Map<String,Set<String>> filter;
	protected Set<String> mustHaves;
	protected String name;
	private boolean removeEmptyTraces;
	
	public AttributeFilterParameters(PluginContext context) {
		filter = new HashMap<String,Set<String>>();
		mustHaves = new HashSet<String>();
		name = "";
		setRemoveEmptyTraces(false);
	}
	
	public AttributeFilterParameters(PluginContext context, XLog log) {
		filter = new HashMap<String,Set<String>>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (String key : event.getAttributes().keySet()) {
					if (!filter.containsKey(key)) {
						filter.put(key, new HashSet<String>());
					}
					filter.get(key).add(event.getAttributes().get(key).toString());
				}
			}
			context.getProgress().inc();
		}
		mustHaves = new HashSet<String>();
		for (XAttribute attribute : log.getGlobalEventAttributes()) {
			mustHaves.add(attribute.getKey());
		}
		name = XConceptExtension.instance().extractName(log);
	}
	
	public void setFilter(Map<String,Set<String>> filter) {
		this.filter = filter;
	}

	public Map<String,Set<String>> getFilter() {
		return filter;
	}
	
	public void setMustHave(Set<String> mustHaves) {
		this.mustHaves = mustHaves;
	}
	
	public Set<String> getMustHaves() {
		return mustHaves;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public boolean isRemoveEmptyTraces() {
		return removeEmptyTraces;
	}

	public void setRemoveEmptyTraces(boolean removeEmptyTraces) {
		this.removeEmptyTraces = removeEmptyTraces;
	}
}
