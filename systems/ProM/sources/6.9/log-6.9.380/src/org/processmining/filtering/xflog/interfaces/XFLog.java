package org.processmining.filtering.xflog.interfaces;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.filtering.filter.interfaces.Filter;

/**
 * An XFLog is a "Filtered" XLog. It behaves like an XLog, though using some
 * source log, it can apply various filtering capabilities. An XFLog can apply a
 * "trace filter" on each trace and a "log attribute filter" on it's own
 * attributes.
 * 
 * @author M.L. van Eck
 * @author S.J. van Zelst
 */
public interface XFLog extends XLog {

	/**
	 * What log is this filtered log based upon?
	 * 
	 * @return pointer to current source.
	 */
	public XLog getSource();

	/**
	 * Set the source of this filtered log. Setting the source can invoke a
	 * re-evaluation of the internal apply(s).
	 * 
	 * @param log
	 *            pointer to new source.
	 */
	public void setSource(XLog log);

	/**
	 * Set the "trace filter" of this log. The trace filter will be applied on
	 * each trace upon trace request (lazy evaluation).
	 * 
	 * @param filter
	 *            to apply on traces in the log.
	 */
	public void setTraceFilter(Filter<XTrace> filter);

	/**
	 * Set the "log attribute filter" of this log. The filter will be applied on
	 * the log's attribute upon attribute request (lazy evaluation).
	 * 
	 * @param filter
	 *            to apply on log's attributes.
	 */
	public void setLogAttributeFilter(Filter<XAttributeMap> filter);
}
