package org.processmining.plugins.log.logfilters.impl;

import java.util.Calendar;
import java.util.Date;

import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XEventEditor;

@Plugin(name = "Week Abstraction Log Editor", categories = { PluginCategory.Filtering }, parameterLabels = { "Log" }, returnLabels = { "Log (edited)" }, returnTypes = { XLog.class })
public class WeekAbstractionLogEditor {

	XTimeExtension timeExtension = XTimeExtension.instance();
	Calendar calendar = Calendar.getInstance();

	//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(requiredParameterLabels = { 0 }, variantLabel = "Day Abstraction Log Editor")
	public XLog editor(PluginContext context, XLog log) throws LogFilterException {

		return LogFilter.filter(log, new XEventEditor() {

			public XEvent editEvent(XEvent event) {
				// TODO Auto-generated method stub
				XEvent editedEvent = (XEvent) event.clone();

				Date date = timeExtension.extractTimestamp(event);
				calendar.setTime(date);
				/*
				 * Abstract from the day.
				 */
				calendar.set(Calendar.YEAR, 2008);
				calendar.set(Calendar.MONTH, 0);
				calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, 1);

				timeExtension.assignTimestamp(editedEvent, calendar.getTime());
				return editedEvent;
			}
		});
	}
}
