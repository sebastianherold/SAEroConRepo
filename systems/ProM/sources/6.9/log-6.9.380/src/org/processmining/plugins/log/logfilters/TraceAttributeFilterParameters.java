package org.processmining.plugins.log.logfilters;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;

public class TraceAttributeFilterParameters extends AttributeFilterParameters {

	public TraceAttributeFilterParameters(PluginContext context, XLog log) {
		super(context);
		filter = new HashMap<String, Set<String>>();
		for (XTrace trace : log) {
			for (String key : trace.getAttributes().keySet()) {
				if (!filter.containsKey(key)) {
					filter.put(key, new HashSet<String>());
				}
				filter.get(key).add(trace.getAttributes().get(key).toString());
			}
			context.getProgress().inc();
		}
		for (XAttribute attribute : log.getGlobalTraceAttributes()) {
			mustHaves.add(attribute.getKey());
		}
		name = XConceptExtension.instance().extractName(log);
	}
}
