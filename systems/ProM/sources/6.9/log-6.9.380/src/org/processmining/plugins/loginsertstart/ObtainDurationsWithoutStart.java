package org.processmining.plugins.loginsertstart;

import java.util.ArrayList;
import java.util.Collections;
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
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.collection.ComparablePair;

/**
 * Check the log and see if any start events exist or not in the event log.
 * 
 * @author jnakatumba
 * 
 */

public class ObtainDurationsWithoutStart {

	private final Set<String> resNamesList;
	private final List<ComparablePair<String, Date>> resDateList;
	private final List<ComparablePair<String, Date>> cidDateList;

	private Long serviceTime = 0L, currentTimeS = 0L, prevResTimeS = 0L;
	private ComparablePair<String, Date> resDate;
	private ComparablePair<String, Date> cidDate;
	private ComparablePair<String, Date> prevPair, prevPairCase;

	private List<Pair<String, Long>> resDurationList;
	private final List<Pair<String, Long>> resDurationListRes;
	private final List<Pair<String, Long>> resDurationListCase;
	private final List<Pair<String, Long>> resDurationListResAver;
	private Pair<String, Long> resDurPair;
	private String caseId;

	private XEvent myEvent;
	private Date cDate, prevCasDate;;
	private String rName;
	private final String displayChoice;

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

	public ObtainDurationsWithoutStart(XLog log, String choice) {
		displayChoice = choice;

		resDurationList = new ArrayList<Pair<String, Long>>();
		resDurationListRes = new ArrayList<Pair<String, Long>>();
		resDurationListCase = new ArrayList<Pair<String, Long>>();
		resDurationListResAver = new ArrayList<Pair<String, Long>>();

		resNamesList = new TreeSet<String>();
		resDateList = new ArrayList<ComparablePair<String, Date>>();
		cidDateList = new ArrayList<ComparablePair<String, Date>>();

		resDate = new ComparablePair<String, Date>(null, null);
		cidDate = new ComparablePair<String, Date>(null, null);

		for (XTrace trace : log) {
			for (XEvent event : trace) {
				if (XLifecycleExtension.instance().extractTransition(event).equals("complete")) {
					String rName = XOrganizationalExtension.instance().extractResource(event);
					Date cDate = XTimeExtension.instance().extractTimestamp(event);
					resDate = getDate(rName, cDate);
					resDateList.add(resDate);
					resNamesList.add(rName);

				}
			}
		}
		Collections.sort(resDateList);

		for (XTrace trace : log) {
			for (XEvent event : trace) {
				if (XLifecycleExtension.instance().extractTransition(event).equals("complete")) {
					insertStartEvents(trace, event);
				}
			}
		}

	}

	private void insertStartEvents(XTrace trace, XEvent event) {
		myEvent = event;
		cDate = XTimeExtension.instance().extractTimestamp(myEvent);
		caseId = XConceptExtension.instance().extractName(trace);
		rName = XOrganizationalExtension.instance().extractResource(myEvent);
		getResourceDetails();

		cidDate = getDate(caseId, cDate);
		cidDateList.add(cidDate);

	}

	/**
	 * Check to see if the current resource executed any event before
	 */
	private void getResourceDetails() {
		String rname;
		Date rDate;
		if (resDateList.size() != 0) {
			search: for (int i = 0; i < resDateList.size(); i++) {
				prevPair = resDateList.get(i);
				rname = prevPair.getFirst();
				rDate = prevPair.getSecond();
				if (rname.equals(rName) == true) {
					if (rDate.compareTo(cDate) == 0) {
						if (i != 0) {
							ComparablePair<String, Date> currentP = resDateList.get(i - 1);
							rname = currentP.getFirst();
							if (rname.equals(rName)) {
								rDate = currentP.getSecond();
								prevResTimeS = rDate.getTime();
								currentTimeS = cDate.getTime();
								serviceTime = currentTimeS - prevResTimeS;
								serviceTime = serviceTime / 1000;
								serviceTime = serviceTime / 60;

								long st = serviceTime.longValue();

								resDurPair = new Pair<String, Long>(rName, st);
								resDurationListRes.add(resDurPair);
								getServiceTimesOnCase();
								break search;

							}
							getServiceTimesOnCase();
							break search;
						}
					}
				}
			}
		}

	}

	/**
	 * Checks the information based on the case perspective and then edits the
	 * event with the new information obtained.
	 */

	private void getServiceTimesOnCase() {
		int listSize = cidDateList.size();
		if ((listSize != 0) && (listSize > 1)) {
			prevPairCase = cidDateList.get(listSize - 1);
			prevCasDate = prevPairCase.getSecond();
			if (prevCasDate.compareTo(cDate) < 0) {
				currentTimeS = cDate.getTime();
				Long prevCasTimeS = prevCasDate.getTime();

				serviceTime = currentTimeS - Math.max(prevCasTimeS, prevResTimeS);
				serviceTime = serviceTime / 1000;
				serviceTime = serviceTime / 60;

				long st = serviceTime.longValue();
				resDurPair = new Pair<String, Long>(rName, st);
				resDurationListResAver.add(resDurPair);

				serviceTime = currentTimeS - prevCasTimeS;
				serviceTime = serviceTime / 1000;
				serviceTime = serviceTime / 60;

				long stC = serviceTime.longValue();
				resDurPair = new Pair<String, Long>(rName, stC);
				resDurationListCase.add(resDurPair);

			}
		}

	}

	public List<Pair<String, Long>> getResDateDetails() {

		if (displayChoice.equals("Resource perspective")) {
			resDurationList = resDurationListRes;

		} else if (displayChoice.equals("Case perspective")) {
			resDurationList = resDurationListCase;

		} else if (displayChoice.equals("Resource/Case perspective")) {
			resDurationList = resDurationListResAver;
		}
		return resDurationList;
	}

	public Set<String> getResNames() {
		return resNamesList;
	}

	private ComparablePair<String, Date> getDate(String rName, Date timeStamp) {
		return new ComparablePair<String, Date>(rName, timeStamp);
	}

}
