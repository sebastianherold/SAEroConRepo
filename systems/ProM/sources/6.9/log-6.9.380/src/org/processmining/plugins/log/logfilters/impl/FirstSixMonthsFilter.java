package org.processmining.plugins.log.logfilters.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;

public class FirstSixMonthsFilter {

	@Plugin(name = "Filter log on first six months per trace", categories = { PluginCategory.Filtering }, parameterLabels = { "Log" }, returnLabels = { "Log (filtered)" }, returnTypes = { XLog.class })
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	public XLog filter(PluginContext context, XLog log) {
		XLog filtered = (XLog) log.clone();
		Iterator<XTrace> itTrace = filtered.iterator();
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			Iterator<XEvent> itEvent = trace.iterator();
			Date startDate = XTimeExtension.instance().extractTimestamp(itEvent.next());
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			cal.add(Calendar.MONTH, 6);
			Date filterDate = cal.getTime();
			while (itEvent.hasNext()) {
				if (XTimeExtension.instance().extractTimestamp(itEvent.next()).after(filterDate)) {
					itEvent.remove();
				}
			}
			if (trace.isEmpty()) {
				itTrace.remove();
			}
		}
		return filtered;
	}

}
