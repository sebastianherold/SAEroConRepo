package org.processmining.plugins.log.logfilters;

import org.deckfour.xes.model.XTrace;

/**
 * Interface used for easy filtering of XLog object. Used by LogFilter.
 * 
 * @author bfvdonge
 * 
 */
public interface XTraceCondition {

	/**
	 * When filtering, this method is called for each XTrace in the log. The
	 * trace should not be edited (use the XTraceEditor for that). Instead, this
	 * method should test whether the trace should be kept by a given filter or
	 * not.
	 * 
	 * @param trace
	 *            The trace that is currently being considered by the calling
	 *            filter. Note that it can be assumed that
	 *            trace.isEmpty()==false
	 * @return true if the trace should be kept, false if the given trace should
	 *         be removed.
	 */
	public boolean keepTrace(XTrace trace);
}
