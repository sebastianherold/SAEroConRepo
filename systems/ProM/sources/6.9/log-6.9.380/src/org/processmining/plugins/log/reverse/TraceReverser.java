package org.processmining.plugins.log.reverse;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Reverses all events per trace (leaving the timestamps as they are)
 * 
 * @author T. van der Wiel
 * 
 */
@Plugin(name = "Reverse Log", parameterLabels = { "Log" }, returnLabels = { "Reversed log" }, returnTypes = { XLog.class }, userAccessible = true)
public class TraceReverser {
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "T. van der Wiel", email = "t.v.d.wiel@student.tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Reverse Log")
	public XLog reverse(UIPluginContext context, XLog log) {
		context.getFutureResult(0).setLabel("Reversed " + XConceptExtension.instance().extractName(log));
		XTimeExtension te = XTimeExtension.instance();
		XLog revLog = (XLog) log.clone();
		Date first = getFirstDate(log), last = getLastDate(log);
		revLog.clear();
		XConceptExtension.instance().assignName(revLog, "Reversed " + XConceptExtension.instance().extractName(log));
		for (XTrace t : log) {
			XTrace revT = new XTraceImpl(t.getAttributes());
			for (int i = t.size() - 1; i >= 0; i--) {
				XEvent e = (XEvent) t.get(i).clone();
				te.assignTimestamp(e, reverseTime(first, te.extractTimestamp(e), last));
				revT.add(e);
			}
			revLog.add(revT);
		}
		return revLog;
	}

	private Date reverseTime(Date first, Date date, Date last) {
		long f = first.getTime(), v = date.getTime(), l = last.getTime();
		return new Date((l - v) + f);
	}

	private Date getLastDate(XLog log) {
		XTimeExtension te = XTimeExtension.instance();
		Date last = te.extractTimestamp(log.get(0).get(0));
		for (XTrace t : log) {
			for (XEvent e : t) {
				Date current = te.extractTimestamp(e);
				if (current.after(last)) {
					last = current;
				}
			}
		}
		return last;
	}

	private Date getFirstDate(XLog log) {
		XTimeExtension te = XTimeExtension.instance();
		Date last = te.extractTimestamp(log.get(0).get(0));
		for (XTrace t : log) {
			for (XEvent e : t) {
				Date current = te.extractTimestamp(e);
				if (current.before(last)) {
					last = current;
				}
			}
		}
		return last;
	}
}
