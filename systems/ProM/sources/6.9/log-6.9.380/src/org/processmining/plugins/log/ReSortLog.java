package org.processmining.plugins.log;

import java.util.Date;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

public class ReSortLog {

	@Plugin(name = "Resort Log Based on Time", parameterLabels = { "log" }, returnLabels = { "log" }, returnTypes = { XLog.class }, userAccessible = true)
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl")
	public static XLog removeEdgePoints(PluginContext context, XLog log) {

		XLog result = XFactoryRegistry.instance().currentDefault().createLog(log.getAttributes());

		for (XTrace t : log) {
			XTrace copy = XFactoryRegistry.instance().currentDefault().createTrace(t.getAttributes());
			result.add(copy);

			for (XEvent e : t) {
				XEvent copyEvent = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
				Date insertAt = XTimeExtension.instance().extractTimestamp(e);
				if (insertAt == null || copy.size() == 0) {
					copy.add(copyEvent);
					continue;
				}
				for (int i = copy.size() - 1; i >= 0; i--) {
					XEvent e2 = copy.get(i);
					Date d2 = XTimeExtension.instance().extractTimestamp(e2);
					if (d2 == null || d2.before(insertAt)) {
						copy.add(i+1, copyEvent);
						break;
					}
					if (i == 0) {
						copy.add(0, copyEvent);
					}
				}

			}

		}

		return result;

	}
}
