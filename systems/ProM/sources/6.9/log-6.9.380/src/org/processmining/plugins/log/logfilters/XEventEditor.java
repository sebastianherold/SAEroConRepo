package org.processmining.plugins.log.logfilters;

import org.deckfour.xes.model.XEvent;

/**
 * Interface used for easy filtering of XLog object. Used by LogFilter.
 * 
 * @author bfvdonge
 * 
 */
public interface XEventEditor {

	/**
	 * When filtering, this method is called for each XEvent in the log. The
	 * event can be edited, or a new one can be returned. If null is returned,
	 * the calling filter will remove the event from the log. If a new XEvent
	 * object is returned, the called filter will replace the old event with the
	 * new event.
	 * 
	 * @param event
	 *            The event that is currently being considered by the calling
	 *            filter.
	 * @return The edited event. If null is returned, then the event is removed.
	 *         If a new XEvent object is returned, the event is replaced.
	 * 
	 */
	public XEvent editEvent(XEvent event);

}
