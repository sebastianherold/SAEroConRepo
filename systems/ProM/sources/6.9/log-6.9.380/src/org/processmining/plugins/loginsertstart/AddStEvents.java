package org.processmining.plugins.loginsertstart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.collection.ComparablePair;

/**
 * Add Start Events
 * 
 * This is the class where the start events are actually added to the event log
 * 
 * @author jnakatumba
 * 
 */

public class AddStEvents {
	private final XTrace trace;
	private final String transition = "start";

	private final List<ComparablePair<String, Date>> cidDateList;
	private final List<ComparablePair<String, Date>> resDateList;
	private ComparablePair<String, Date> cidDate;
	private ComparablePair<String, Date> prevPair, prevPairCase;
	private final List<Pair<String, String>> startEventsList;
	private final List<Pair<String, String>> completeEventsList;
	private final List<Pair<String, String>> startEventsList1;
	private final List<Pair<String, String>> completeEventsList1;

	private final ArrayList<XEvent> eventList;
	private final List<XEvent> starts;
	private final List<XEvent> removeEvents;
	private Pair<String, String> typeNamePair;
	private Pair<String, String> startNamePair;
	private Integer removeCount = 0;

	private Date prevCasDate;

	private Long serviceTime = 0L, currentTimeS = 0L;
	private Long prevResTimeS = 0L, prevCasTimeS = 0L;

	private XEvent myEvent;
	private Date cDate, newDate;
	private String rName, caseId;
	private Integer count = 0;
	private final String displayChoice, outlierChoice;
	private String eventType, actName;

	/**
	 * Add start events
	 * 
	 * @param traces
	 *            The trace that is to be edited
	 * @param resDateList
	 *            The list of all the resources in the log and the particular
	 *            dates at which an event execution occurred in the log that was
	 *            done by the resources.
	 */

	public AddStEvents(XTrace trace, List<ComparablePair<String, Date>> resDateList, String displaychoice,
			String outlierChoice, Integer addcounter, Integer rCount) {

		this.resDateList = resDateList;
		displayChoice = displaychoice;
		this.outlierChoice = outlierChoice;
		count = addcounter;
		removeCount = rCount;

		startEventsList = new ArrayList<Pair<String, String>>();
		completeEventsList = new ArrayList<Pair<String, String>>();
		startEventsList1 = new ArrayList<Pair<String, String>>();
		completeEventsList1 = new ArrayList<Pair<String, String>>();
		eventList = new ArrayList<XEvent>();
		starts = new ArrayList<XEvent>();
		removeEvents = new ArrayList<XEvent>();

		cidDateList = new ArrayList<ComparablePair<String, Date>>();
		cidDate = new ComparablePair<String, Date>(null, null);

		/**
		 * Iterate through each trace in the log and see if there are duplicate
		 * events. For example if we have a start event followed by another
		 * start event, and then a complete event, we match the second start
		 * event with the complete event and then discard the first start event
		 */
		for (XEvent event : trace) {
			cDate = XTimeExtension.instance().extractTimestamp(event);
			caseId = XConceptExtension.instance().extractName(trace);
			rName = XOrganizationalExtension.instance().extractResource(event);
			eventType = XLifecycleExtension.instance().extractTransition(event);
			actName = XConceptExtension.instance().extractName(event);
			myEvent = event;
			typeNamePair = new Pair<String, String>(eventType, actName);
			starts.add(myEvent);

			if (eventType.equals("start")) {
				if (startEventsList1.contains(typeNamePair) == false) {
					startEventsList1.add(typeNamePair);
				} else {
					for (int i = 0; i < starts.size(); i++) {
						XEvent prevEvent = starts.get(i);
						String actvName = XConceptExtension.instance().extractName(prevEvent);
						if (actvName.equals(actName)
								&& XLifecycleExtension.instance().extractTransition(prevEvent).equals("start")) {
							removeEvents.add(prevEvent);
							starts.remove(prevEvent);
						}

					}
				}

			} else if (eventType.equals("complete")) {

				if (completeEventsList1.contains(typeNamePair) == false) {
					completeEventsList1.add(typeNamePair);

				} else {
					for (int i = 0; i < starts.size(); i++) {
						XEvent prevEvent = starts.get(i);
						String actvName = XConceptExtension.instance().extractName(prevEvent);
						if (actvName.equals(actName)
								&& XLifecycleExtension.instance().extractTransition(prevEvent).equals("complete")) {
							removeEvents.add(prevEvent);
							starts.remove(prevEvent);

						}
					}

				}
			}

		}

		/**
		 * Discard the events that are of the same event type per activity
		 * occurring in a trace
		 */
		for (int i = 0; i < removeEvents.size(); i++) {
			XEvent prevEvent = removeEvents.get(i);
			trace.remove(prevEvent);
			removeCount++;

		}

		/**
		 * Obtain when to insert the start events to the log
		 */
		for (XEvent event : trace) {
			cDate = XTimeExtension.instance().extractTimestamp(event);
			caseId = XConceptExtension.instance().extractName(trace);
			rName = XOrganizationalExtension.instance().extractResource(event);
			eventType = XLifecycleExtension.instance().extractTransition(event);
			actName = XConceptExtension.instance().extractName(event);
			myEvent = event;
			typeNamePair = new Pair<String, String>(eventType, actName);
			init();
			cidDate = getIdDate(caseId, cDate);
			cidDateList.add(cidDate);

		}

		/**
		 * Add the new start events to the event log. These are added before
		 * each complete event observed in the log.
		 */
		for (int i = 0; i < eventList.size(); i++) {
			XEvent events = eventList.get(i);
			String actNames = XConceptExtension.instance().extractName(events);
			int sn = 0;
			now: for (ListIterator<XEvent> eventListIt = trace.listIterator(); eventListIt.hasNext();) {
				if (eventListIt.hasNext() == true) {
					XEvent event = eventListIt.next();
					sn++;
					if (XLifecycleExtension.instance().extractTransition(event).equals("complete")) {
						String actName = XConceptExtension.instance().extractName(event);
						if (actNames.equals(actName)) {
							trace.add(sn - 1, events);
							count++;
							break now;
						}

					}
				}

			}
		}

		this.trace = trace;

	}

	/**
	 * Check if a start event in the log can be matched with a corresponding
	 * event and if a start event is missing,i.e., we observe only a complete
	 * event, then we insert it into the log.
	 */
	private void init() {
		if (eventType.equals("start")) {
			if (startEventsList.contains(typeNamePair) == false) {
				startEventsList.add(typeNamePair);
				starts.add(myEvent);
			}

		} else if (eventType.equals("complete")) {
			if (completeEventsList.contains(typeNamePair) == false) {
				startNamePair = new Pair<String, String>("start", actName);

				if (startEventsList.contains(startNamePair) == false) {
					start();

				}
			}

		}
	}

	/**
	 * The durations for each event can be checked based on either the resource
	 * perspective, case of the maximum of both the resource and case
	 * perspective.
	 */

	private void start() {
		if (displayChoice.equals("Resource perspective")) {
			getResourceDetails();

		} else if (displayChoice.equals("Case perspective")) {
			getServiceTimesOnCase();

		} else if (displayChoice.equals("Resource/Case perspective")) {
			getServiceTimesOnAverage();
		}

	}

	/**
	 * Check to see if the current resource executed any event before this one.
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

								long value = serviceTime.longValue();
								double dValue = Double.valueOf(value);

								if (outlierChoice.equals("Estimate Values")) {
									estimateDates(serviceTime);
									break search;

								} else {
									Double outlierRange = Double.valueOf(outlierChoice);
									if (dValue < outlierRange) {
										Date pDate = estimateStartDates(serviceTime);
										XTimeExtension.instance().assignTimestamp(myEvent, pDate);
										eventList.add(myEvent);
										break search;

									} else {
										XTimeExtension.instance().assignTimestamp(myEvent, cDate);
										eventList.add(myEvent);
										break search;

									}

								}

							}
							if (outlierChoice.equals("Estimate Values")) {
								estimateDates(serviceTime);
								break search;
							} else {
								XTimeExtension.instance().assignTimestamp(myEvent, cDate);
								eventList.add(myEvent);
								break search;
							}

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

	private void getServiceTimesOnAverage() {
		String rname;
		Date rDate;
		int listSize = cidDateList.size();
		XOrganizationalExtension.instance().assignResource(myEvent, rName);
		XLifecycleExtension.instance().assignTransition(myEvent, transition);

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

								if (listSize != 0) {
									prevPairCase = cidDateList.get(listSize - 1);
									prevCasDate = prevPairCase.getSecond();

									if (prevCasDate.compareTo(cDate) < 0) {
										currentTimeS = cDate.getTime();
										prevCasTimeS = prevCasDate.getTime();

										serviceTime = currentTimeS - Math.max(prevCasTimeS, prevResTimeS);
										serviceTime = serviceTime / 1000;
										serviceTime = serviceTime / 60;

										if (outlierChoice.equals("Estimate Values")) {
											estimateDates(serviceTime);
											break search;

										} else {
											Double outlierRange = Double.valueOf(outlierChoice);
											long value = serviceTime.longValue();
											double dValue = Double.valueOf(value);
											if (dValue < outlierRange) {
												Date pDate = estimateStartDates(serviceTime);
												XTimeExtension.instance().assignTimestamp(myEvent, pDate);
												eventList.add(myEvent);
												break search;

											} else {
												XTimeExtension.instance().assignTimestamp(myEvent, cDate);
												eventList.add(myEvent);
												break search;

											}

										}

									} else {
										XTimeExtension.instance().assignTimestamp(myEvent, cDate);
										eventList.add(myEvent);
										break search;
									}

								}
							}
							XTimeExtension.instance().assignTimestamp(myEvent, cDate);
							eventList.add(myEvent);
							break search;
						}
					}
				}
			}
		}

	}

	private void getServiceTimesOnCase() {
		int listSize = cidDateList.size();
		if (listSize != 0) {
			prevPairCase = cidDateList.get(listSize - 1);
			prevCasDate = prevPairCase.getSecond();
			XOrganizationalExtension.instance().assignResource(myEvent, rName);
			XLifecycleExtension.instance().assignTransition(myEvent, transition);
			if (prevCasDate.compareTo(cDate) < 0) {
				currentTimeS = cDate.getTime();
				prevCasTimeS = prevCasDate.getTime();

				serviceTime = currentTimeS - prevCasTimeS;
				serviceTime = serviceTime / 1000;
				serviceTime = serviceTime / 60;

				if (outlierChoice.equals("Estimate Values")) {
					estimateDates(serviceTime);

				} else {

					Double outlierRange = Double.valueOf(outlierChoice);
					long value = serviceTime.longValue();
					double dValue = Double.valueOf(value);
					if (dValue < outlierRange) {
						Date pDate = estimateStartDates(serviceTime);
						XTimeExtension.instance().assignTimestamp(myEvent, pDate);
						eventList.add(myEvent);

					} else {
						XTimeExtension.instance().assignTimestamp(myEvent, cDate);
						eventList.add(myEvent);

					}
				}
			}

		} else {
			XTimeExtension.instance().assignTimestamp(myEvent, cDate);
			eventList.add(myEvent);

		}

	}

	/**
	 * We obtain a start date based on a duration and a complete date per event.
	 * If the complete date occurred before 13hrs we assume it was started at
	 * 8hrs or else it was started at 13hrs.
	 * 
	 * @param serviceT
	 *            Service time
	 */

	private void estimateDates(Long serviceT) {
		long st = serviceT.longValue();

		Calendar myCalendar = Calendar.getInstance();
		myCalendar.setTime(cDate);
		int currentDay = myCalendar.get(Calendar.DAY_OF_WEEK);
		int currentMonth = myCalendar.get(Calendar.MONTH);
		int currentTime = myCalendar.get(Calendar.HOUR_OF_DAY);
		int currentDayMonth = myCalendar.get(Calendar.DAY_OF_MONTH);
		int currentYear = myCalendar.get(Calendar.YEAR);

		int a = (int) st;
		myCalendar.add(Calendar.MINUTE, -a);
		newDate = myCalendar.getTime();

		if (outlierChoice.equals("Estimate Values")) {

			Calendar newCalendars = Calendar.getInstance();
			newCalendars.setTime(newDate);

			int prevDay = newCalendars.get(Calendar.DAY_OF_WEEK);
			int prevMonth = newCalendars.get(Calendar.MONTH);
			int prevDayMonth = newCalendars.get(Calendar.DAY_OF_MONTH);
			int prevYear = myCalendar.get(Calendar.YEAR);

			if ((prevDay < currentDay) || (prevMonth < currentMonth) || (prevDayMonth < currentDayMonth)
					|| (prevYear < currentYear)) {
				Calendar newCalendar = Calendar.getInstance();
				newCalendar.setTime(cDate);

				int cTime = newCalendar.get(Calendar.HOUR_OF_DAY);
				if (cTime < 12) {
					newCalendar.add(Calendar.HOUR_OF_DAY, -(currentTime - 8));
					newDate = newCalendar.getTime();
					XTimeExtension.instance().assignTimestamp(myEvent, newDate);
					eventList.add(myEvent);

				} else {
					newCalendar.add(Calendar.HOUR_OF_DAY, -(currentTime - 13));

					newDate = newCalendar.getTime();
					XTimeExtension.instance().assignTimestamp(myEvent, newDate);
					eventList.add(myEvent);

				}
			} else {
				XTimeExtension.instance().assignTimestamp(myEvent, newDate);
				eventList.add(myEvent);
			}
		}

	}

	/**
	 * Based on the complete date and a certain duration, we obtain a start date
	 * 
	 * @param servT
	 *            Service Time
	 * @return Start Date
	 */

	private Date estimateStartDates(Long servT) {
		long st = servT.longValue();
		Calendar myCalendar = Calendar.getInstance();
		myCalendar.setTime(cDate);

		int a = (int) st;
		myCalendar.add(Calendar.MINUTE, -a);
		Date cDate = myCalendar.getTime();

		return cDate;

	}

	private ComparablePair<String, Date> getIdDate(String caseId, Date timeStamp) {
		return new ComparablePair<String, Date>(caseId, timeStamp);
	}

	public XTrace getTrace() {
		return trace;
	}

	public int getAddevents() {
		return count;
	}

	public int getRemoveEvents() {
		return removeCount;
	}

}