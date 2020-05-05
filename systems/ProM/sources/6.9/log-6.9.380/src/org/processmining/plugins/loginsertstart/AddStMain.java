package org.processmining.plugins.loginsertstart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.collection.ComparablePair;
import org.processmining.plugins.log.logfilters.LogFilter;
import org.processmining.plugins.log.logfilters.LogFilterException;
import org.processmining.plugins.log.logfilters.XTraceEditor;

/**
 * Check the log and see if any start events exist or not in the event log and
 * then calls the class to add the add the missing start events.
 * 
 * @author jnakatumba
 * 
 */

public class AddStMain {

	private AddStEvents startEvents;

	private List<ComparablePair<String, Date>> resDateList;
	private ArrayList<Date> startDatesList;
	private ComparablePair<String, Date> resDate;
	private Set<String> resNamesList;

	private Integer addCount = 0, removeCount = 0;
	private String displaychoice, outlierChoice;

	private XLog log;

	/**
	 * Initialize the plugin by checking if any start events exist in the log
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * 
	 */

	public AddStMain(UIPluginContext context, XLog log, String displayChoice, String outLierChoice) {
		try {
			outlierChoice = outLierChoice;
			displaychoice = displayChoice;
			resDateList = new ArrayList<ComparablePair<String, Date>>();
			startDatesList = new ArrayList<Date>();
			resDate = new ComparablePair<String, Date>(null, null);
			resNamesList = new TreeSet<String>();
			getEditedLog(context, log);
		} catch (LogFilterException e) {
			e.printStackTrace();
		}

	}

	private XLog getEditedLog(final UIPluginContext context, XLog log) throws LogFilterException {
		/**
		 * check if the log has any start events at all
		 */
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				if (XLifecycleExtension.instance().extractTransition(event).equals("start")) {
					Date startDate = XTimeExtension.instance().extractTimestamp(event);
					startDatesList.add(startDate);

				} else if (XLifecycleExtension.instance().extractTransition(event).equals("complete")) {
					String rName = XOrganizationalExtension.instance().extractResource(event);
					Date cDate = XTimeExtension.instance().extractTimestamp(event);
					resDate = getResDate(rName, cDate);
					resDateList.add(resDate);
					resNamesList.add(rName);

				}
			}
			Collections.sort(resDateList);
		}
		/**
		 * For each trace per log, the start event is added per complete event
		 */
		return this.log = LogFilter.filter(context.getProgress(), 100, log, XLogInfoFactory.createLogInfo(log),
				new XTraceEditor() {
					public XTrace editTrace(XTrace trace) {
						startEvents = new AddStEvents(trace, resDateList, displaychoice, outlierChoice, addCount,
								removeCount);
						trace = startEvents.getTrace();
						addCount = startEvents.getAddevents();
						removeCount = startEvents.getRemoveEvents();
						return trace;
					}

				});

	}

	private ComparablePair<String, Date> getResDate(String rName, Date timeStamp) {
		return new ComparablePair<String, Date>(rName, timeStamp);
	}

	/**
	 * The edited log
	 * 
	 * @return XLog
	 */
	public XLog getLog() {
		return log;
	}

	/**
	 * Number of start events added
	 * 
	 * @return Start Events added
	 */

	public int getAddCounter() {
		return addCount;
	}

	/**
	 * Number of events removed
	 * 
	 * @return No of events removed
	 */
	public int getRemoveCounter() {
		return removeCount;
	}
}
