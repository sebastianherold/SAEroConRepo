package org.processmining.plugins.log.logfilters;

import java.util.Iterator;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.Progress;

/**
 * Class containing some static methods that are easy to use when implementing a
 * new filter on a log.
 * 
 * @author bfvdonge
 * 
 */
public class LogFilter {

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the given XEventCondition returns false.
	 * After that, the given XTrace condition is checked on the filtered trace
	 * and the trace is removed if this condition returns false.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param eventCondition
	 *            The condition that is checked for all events in the log.
	 * @param traceCondition
	 *            The condition that is checked for all traces in the log. Note
	 *            that on each trace, the eventCondition is first checked on all
	 *            events, and then the trace condition is checked on the
	 *            filtered trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 */
	public static XLog filter(XLog log, XEventCondition eventCondition, XTraceCondition traceCondition) {
		return filter(null, 1, log, null, eventCondition, traceCondition);

	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the given XEventCondition returns false.
	 * After that, the given XTrace condition is checked on the filtered trace
	 * and the trace is removed if this condition returns false.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param eventCondition
	 *            The condition that is checked for all events in the log.
	 * @param traceCondition
	 *            The condition that is checked for all traces in the log. Note
	 *            that on each trace, the eventCondition is first checked on all
	 *            events, and then the trace condition is checked on the
	 *            filtered trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XEventCondition eventCondition,
			XTraceCondition traceCondition) {

		// set the progress indicator to the maximum number of events / 100
		if ((summary == null) && (progress != null)) {
			summary = XLogInfoFactory.createLogInfo(log);
		}
		int barSize = 100;
		if (summary != null) {
			steps = Math.min(steps, summary.getNumberOfTraces());
			barSize = summary.getNumberOfEvents() / (steps > 0 ? steps : 1);
			if (progress != null) {
				progress.setMaximum(progress.getMaximum() + steps);
			}
		}

		// Go through the log to remove all XEvents, of which the name is not
		// contains in
		// labels
		XLog filtered = (XLog) log.clone();
		int i = 0;
		Iterator<XTrace> itTrace = filtered.iterator();
//		long time = -System.currentTimeMillis();
//		long progressTime = 0;
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			Iterator<XEvent> itEvent = trace.iterator();
			while (itEvent.hasNext()) {
				if (!eventCondition.keepEvent(itEvent.next())) {
					// The XEvent should be removed
					itEvent.remove();
				}
				// Check for progress and signal if necessary
//				progressTime -= System.currentTimeMillis();
				if (progress != null) {
					if (i % barSize == 0) {
						progress.inc();
					}
					i++;
				}
//				progressTime += System.currentTimeMillis();
			}
			if (trace.isEmpty() || !traceCondition.keepTrace(trace)) {
				itTrace.remove();
			}
		}
//		time += System.currentTimeMillis();
//		System.err.println("[LogFilter] time = " + time + ", progressTime = " + progressTime);
		return filtered;
	}

	/**
	 * This method filters a log by checking the given XTrace condition on each
	 * trance. The trace is removed if this condition returns false.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param traceCondition
	 *            The condition that is checked for all traces in the log. Note
	 *            that on each trace, the eventCondition is first checked on all
	 *            events, and then the trace condition is checked on the
	 *            filtered trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 */
	public static XLog filter(XLog log, XTraceCondition traceCondition) {
		return filter(null, 1, log, null, traceCondition);
	}

	/**
	 * This method filters a log by checking the given XTrace condition on each
	 * trance. The trace is removed if this condition returns false.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param traceCondition
	 *            The condition that is checked for all traces in the log. Note
	 *            that on each trace, the eventCondition is first checked on all
	 *            events, and then the trace condition is checked on the
	 *            filtered trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XTraceCondition traceCondition) {

		// set the progress indicator to the maximum number of events / 100
		if ((summary == null) && (progress != null)) {
			summary = XLogInfoFactory.createLogInfo(log);
		}
		int barSize = 100;
		if (summary != null) {
			steps = Math.max(1, Math.min(steps, summary.getNumberOfTraces()));
			barSize = summary.getNumberOfTraces() / steps;
			if (progress != null) {
				progress.setMaximum(progress.getMaximum() + steps);
			}
		}

		// Go through the log to remove all XEvents, of which the name is not
		// contains in
		// labels
		XLog filtered = (XLog) log.clone();
		int i = 0;
		Iterator<XTrace> itTrace = filtered.iterator();
//		long time = -System.currentTimeMillis();
//		long progressTime = 0;
		while (itTrace.hasNext()) {
			XTrace trace = itTrace.next();
			if (!traceCondition.keepTrace(trace)) {
				itTrace.remove();
			}
//			progressTime -= System.currentTimeMillis();
			if (progress != null) {
				if (i % barSize == 0) {
					progress.inc();
				}
				i++;
			}
//			progressTime += System.currentTimeMillis();
		}
//		time += System.currentTimeMillis();
//		System.err.println("[LogFilter] time = " + time + ", progressTime = " + progressTime);
		return filtered;
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the given XEventCondition returns false.
	 * After that, the filtered trace is removed if it remains empty.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param eventCondition
	 *            The condition that is checked for all events in the log.
	 * @return the filtered log
	 */
	public static XLog filter(XLog log, XEventCondition eventCondition) {
		return filter(null, 1, log, null, eventCondition);
	}

	/**
	 * This method filters a log by removing XEvent objects from all XTrace
	 * object in the given XLog, if the given XEventCondition returns false.
	 * After that, the filtered trace is removed if it remains empty.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param eventCondition
	 *            The condition that is checked for all events in the log.
	 * @return the filtered log
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XEventCondition eventCondition) {

		return filter(progress, steps, log, summary, eventCondition, new XTraceCondition() {

			public boolean keepTrace(XTrace trace) {
				// Keep the trace
				return true;
			}
		});
	}

	/**
	 * This method filters a log by editing the XEvent objects from all XTrace
	 * object in the given XLog. XEvent objects are removed if the given
	 * XEventEditor returns null. If a new XEvent object is returned by the
	 * XEventEditor, then the original XEvent is replaced by the new XEvent.
	 * After editing all events, the filtered trace is removed if it remains
	 * empty. If it is not empty, then it is provided to the given XTraceEditor
	 * for editing. If this editor returns null, the trace is removed. If this
	 * editor returns a new XTrace object, the original XTrace is replaces.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param eventEditor
	 *            The editor that is applied to all XEvents in the log
	 * @param traceEditor
	 *            The editor that is applied to each trace. Note that first all
	 *            events are edited and that the trace editor is then applied to
	 *            the filtered trace, if it is not empty.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 * @throws LogFilterException
	 */
	public static XLog filter(XLog log, XEventEditor eventEditor, XTraceEditor traceEditor) throws LogFilterException {
		return filter(null, 1, log, null, eventEditor, traceEditor);
	}

	/**
	 * This method filters a log by editing the XEvent objects from all XTrace
	 * object in the given XLog. XEvent objects are removed if the given
	 * XEventEditor returns null. If a new XEvent object is returned by the
	 * XEventEditor, then the original XEvent is replaced by the new XEvent.
	 * After editing all events, the filtered trace is removed if it remains
	 * empty. If it is not empty, then it is provided to the given XTraceEditor
	 * for editing. If this editor returns null, the trace is removed. If this
	 * editor returns a new XTrace object, the original XTrace is replaces.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param eventEditor
	 *            The editor that is applied to all XEvents in the log
	 * @param traceEditor
	 *            The editor that is applied to each trace. Note that first all
	 *            events are edited and that the trace editor is then applied to
	 *            the filtered trace, if it is not empty.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 * @throws LogFilterException
	 *             if an XTraceEditor returns a new trace.
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XEventEditor eventEditor,
			XTraceEditor traceEditor) throws LogFilterException {

		int barSize = 100;
		// set the progress indicator to the maximum number of events / 100
		if (progress != null) {
			if (summary == null) {
				summary = XLogInfoFactory.createLogInfo(log);
			}
			steps = Math.min(steps, summary.getNumberOfTraces());
			barSize = summary.getNumberOfEvents() / steps;
			progress.setMaximum(progress.getMaximum() + steps);
		}

		// Go through the log to remove all XEvents, of which the name is not
		// contains in
		// labels
		XLog filtered = (XLog) log.clone();

		int progressStep = 0;

		Iterator<XTrace> it = filtered.iterator();
		while (it.hasNext()) {
			XTrace oldTrace = it.next();

			for (int eventIndex = 0; eventIndex < oldTrace.size(); eventIndex++) {
				XEvent oldEvent = oldTrace.get(eventIndex);
				XEvent newEvent = eventEditor.editEvent(oldEvent);
				if (newEvent == null) {
					// The editor returned null, so remove the event
					oldTrace.remove(eventIndex);
					eventIndex--;
				} else if (newEvent != oldEvent) {
					// The editor returned a new event, so replace the old one
					oldTrace.set(eventIndex, newEvent);
				}
				// Check for progress and signal if necessary
				if (progress != null) {
					if (progressStep % barSize == 0) {
						progress.inc();
					}
					progressStep++;
				}
			}
			if (oldTrace.isEmpty()) {
				// All events of this trace were removed, remove the trace
				it.remove();
			} else {
				// Edit the trace
				XTrace newTrace = traceEditor.editTrace(oldTrace);
				if (newTrace == null) {
					// The editor returned null, so remove the trace
					it.remove();
				} else if (newTrace != oldTrace) {
					// The editor returned a new trace, so this is wrong!
					throw new LogFilterException("New traces cannot be produced by XTraceEditors.");
				}
			}
		}
		return filtered;
	}

	/**
	 * This method filters a log by editing all XTrace objects in the given
	 * XLog. Each XTrace object is provided to the given XTraceEditor for
	 * editing. If this editor returns null, the trace is removed. If this
	 * editor returns a new XTrace object, the original XTrace is replaces.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param traceEditor
	 *            The editor that is applied to each trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 * @throws LogFilterException
	 */
	public static XLog filter(XLog log, XTraceEditor traceEditor) throws LogFilterException {
		return filter(null, 1, log, null, traceEditor);
	}

	/**
	 * This method filters a log by editing all XTrace objects in the given
	 * XLog. Each XTrace object is provided to the given XTraceEditor for
	 * editing. If this editor returns null, the trace is removed. If this
	 * editor returns a new XTrace object, the original XTrace is replaces.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param traceEditor
	 *            The editor that is applied to each trace.
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XTraceEditor traceEditor)
			throws LogFilterException {

		// set the progress indicator to the maximum number of events / 100
		if ((summary == null) && (progress != null)) {
			summary = XLogInfoFactory.createLogInfo(log);
		}
		int barSize = 100;
		if (summary != null) {
			steps = Math.min(steps, summary.getNumberOfTraces());
			barSize = summary.getNumberOfTraces() / steps;
			if (progress != null) {
				progress.setMaximum(progress.getMaximum() + steps);
			}
		}

		// Go through the log to remove all XEvents, of which the name is not
		// contains in
		// labels
		XLog filtered = (XLog) log.clone();
		int progressStep = 0;
		Iterator<XTrace> it = filtered.iterator();
		while (it.hasNext()) {
			XTrace oldTrace = it.next();

			// Edit the trace
			XTrace newTrace = traceEditor.editTrace(oldTrace);
			if (newTrace == null) {
				// The editor returned null, so remove the trace
				it.remove();
			} else if (newTrace != oldTrace) {
				// The editor returned a new trace, so this is wrong
				throw new LogFilterException("New traces cannot be produced by XTraceEditors.");
			}
			// Check for progress and signal if necessary
			if (progress != null) {
				if (progressStep % barSize == 0) {
					progress.inc();
				}
				progressStep++;
			}
		}
		return filtered;
	}

	/**
	 * This method filters a log by editing the XEvent objects from all XTrace
	 * object in the given XLog. XEvent objects are removed if the given
	 * XEventEditor returns null. If a new XEvent object is returned by the
	 * XEventEditor, then the original XEvent is replaced by the new XEvent.
	 * After editing, the filtered trace is removed if it remains empty.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @param eventEditor
	 *            The editor that is applied to all XEvents in the log
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 * @throws LogFilterException
	 */
	public static XLog filter(XLog log, XEventEditor eventEditor) throws LogFilterException {
		return filter(null, 1, log, null, eventEditor);
	}

	/**
	 * This method filters a log by editing the XEvent objects from all XTrace
	 * object in the given XLog. XEvent objects are removed if the given
	 * XEventEditor returns null. If a new XEvent object is returned by the
	 * XEventEditor, then the original XEvent is replaced by the new XEvent.
	 * After editing, the filtered trace is removed if it remains empty.
	 * 
	 * @param progress
	 *            A Progress object which is used for showing progress. steps
	 *            steps are added to the maximum and progress is increased in
	 *            steps steps. May be null.
	 * @param steps
	 *            The number of steps in which progress is provided. Should be >
	 *            0
	 * @param log
	 *            The log that needs to be filtered.
	 * @param summary
	 *            The summary of the log. Is used to determine the number of
	 *            events in the log. If null, it will be constructed if progress
	 *            is not null.
	 * @param eventEditor
	 *            The editor that is applied to all XEvents in the log
	 * @return the filtered log. The result is a clone of the input log, i.e.
	 *         the given log object does not change
	 * @throws LogFilterException
	 */
	public static XLog filter(Progress progress, int steps, XLog log, XLogInfo summary, XEventEditor eventEditor)
			throws LogFilterException {

		return filter(progress, steps, log, summary, eventEditor, new XTraceEditor() {

			public XTrace editTrace(XTrace trace) {
				if (trace.isEmpty()) {
					return null;
				} else {
					return trace;
				}
			}
		});
	}

}
