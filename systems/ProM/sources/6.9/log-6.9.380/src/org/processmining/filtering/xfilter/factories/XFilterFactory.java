package org.processmining.filtering.xfilter.factories;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.filtering.xfilter.implementations.HideXEventXAttributableFilterImpl;
import org.processmining.filtering.xfilter.implementations.XAttributableContainsKeyWithValueXFilterImpl;
import org.processmining.filtering.xfilter.interfaces.XAttributableFilter;
import org.processmining.filtering.xfilter.interfaces.XFilter;

public class XFilterFactory {

	/**
	 * {@link XAttributableContainsKeyWithValueXFilterImpl}
	 * @param key of pair
	 * @param value of pair
	 * @return filter based on pair <key,value>
	 */
	public static <T extends XAttributable> XAttributableFilter<T> containsKeyValuePairFilter(String key, String value) {
		return new XAttributableContainsKeyWithValueXFilterImpl<>(key, value);
	}
	
	/**
	 * {@link XAttributableContainsKeyWithValueXFilterImpl}
	 * @param key of pair
	 * @param value of pair
	 * @return filter based on pair <key,value>
	 */
	public static <T extends XAttributable> XAttributableFilter<T> containsKeyValuePairFilter(String key, boolean value) {
		return new XAttributableContainsKeyWithValueXFilterImpl<>(key, value);
	}
	
	public static XFilter<XTrace> hideXEvent(XAttributableFilter<XEvent> eventFilter) {
		return new HideXEventXAttributableFilterImpl(eventFilter);
	}
}
