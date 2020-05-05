package org.processmining.plugins.log.logfilters;

import org.deckfour.xes.model.XEvent;

/**
 * Interface used for easy filtering of XLog object. Used by LogFilter.
 * 
 * @author bfvdonge
 * 
 */
public interface XEventCondition {

	/**
	 * When filtering, this method is called for each XEvent in the log. The
	 * event should not be edited (use the XEventEditor for that). Instead, this
	 * method should test whether the event should be kept by a given filter or
	 * not.
	 * 
	 * @param event
	 *            The event that is currently being considered by the calling
	 *            filter.
	 * @return true if the event should be kept, false if the given event should
	 *         be removed.
	 */
	public boolean keepEvent(XEvent event);
}
