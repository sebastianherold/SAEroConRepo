package org.processmining.filtering.xflog.interfaces;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.filtering.filter.interfaces.Filter;

/**
 * An XFTrace is a Filtered XTrace. It behaves like a trace and is based on some
 * data type which adheres to the XTrace interface.
 * 
 * @author M.L. van Eck
 * @author S.J. van Zelst
 */
public interface XFTrace extends XTrace {

	/**
	 * What trace is this filtered trace based upon?
	 * 
	 * @return pointer to current source.
	 */
	public XTrace getSource();

	/**
	 * Set the source of this filtered trace. Setting the source can invoke a
	 * re-evaluation of the internal apply(s).
	 * 
	 * @param trace
	 *            pointer to new source.
	 */
	public void setSource(XTrace trace);

	/**
	 * Set the trace's event attribute filter.
	 * 
	 * @param filter
	 *            on attribute map.
	 */
	public void setEventFilter(Filter<XEvent> filter);

	/**
	 * Set the trace's attribute filter
	 * 
	 * @param filter
	 *            on trace attribute-map
	 */
	public void setTraceAttributeFilter(Filter<XAttributeMap> filter);
}
