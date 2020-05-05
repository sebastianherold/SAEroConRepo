package org.processmining.plugins.log.logfilters;

import org.deckfour.xes.model.XTrace;

/**
 * Interface used for easy filtering of XLog object. Used by LogFilter.
 * 
 * @author bfvdonge
 * 
 */
public interface XTraceEditor {

	/**
	 * When filtering, this method is called for each XTrace in the log. The
	 * trace can be edited, or a new one can be returned. If null is returned,
	 * or an empty trace is returned, the calling filter will remove the trace
	 * from the log. No new XTrace objects should be returned.
	 * 
	 * @param trace
	 *            The trace that is currently being considered by the calling
	 *            filter. Note that it can be assumed that
	 *            trace.isEmpty()==false
	 * @return The edited trace. If null, or an empty trace is returned, then
	 *         the trace is removed. No new trace objects should be returned.
	 */
	public XTrace editTrace(XTrace trace);

}
