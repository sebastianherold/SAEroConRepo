package org.processmining.plugins.loginsertstart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;

/**
 * Check the log and see if any start events exist or not in the event log.
 * 
 * @author jnakatumba
 * 
 */

public class ObtainDurationWithStart {
	private final ArrayList<Date> startDatesList;
	private final List<Pair<String, Long>> resDurationList;
	private final Set<String> resNamesList;
	private Pair<String, Long> resDurPair;
	private final XLog log;

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

	public ObtainDurationWithStart(PluginContext context, XLog log) {
		this.log = log;
		resDurationList = new ArrayList<Pair<String, Long>>();
		startDatesList = new ArrayList<Date>();
		resNamesList = new TreeSet<String>();
		getEditedLog();

	}

	private void getEditedLog() {
		String rNamest = null;
		String caseId = null;
		String aName = null;
		Date startDate = null;
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				String eventType = XLifecycleExtension.instance().extractTransition(event);
				if (eventType.equals("start")) {
					startDate = XTimeExtension.instance().extractTimestamp(event);
					caseId = XConceptExtension.instance().extractName(trace);
					aName = XConceptExtension.instance().extractName(event);
					rNamest = XOrganizationalExtension.instance().extractResource(event);

				} else if (eventType.equals("complete")) {
					String cid = XConceptExtension.instance().extractName(trace);
					String aNameC = XConceptExtension.instance().extractName(event);
					Date compDate = XTimeExtension.instance().extractTimestamp(event);
					if (caseId != null) {
						if (cid.equals(caseId)) {
							if (aName != null) {
								if (aName.equals(aNameC)) {
									startDatesList.add(startDate);
									long stTime = startDate.getTime();
									long compsTime = compDate.getTime();
									long serviceTimes = compsTime - stTime;
									serviceTimes = serviceTimes / 1000;
									serviceTimes = serviceTimes / 60;

									resDurPair = new Pair<String, Long>(rNamest, serviceTimes);
									resDurationList.add(resDurPair);
									resNamesList.add(rNamest);

								}

							}

						}
					}

				}
			}
		}

	}

	public List<Pair<String, Long>> getResDateDetails() {
		return resDurationList;
	}

	public Set<String> getResNames() {
		return resNamesList;
	}

}
