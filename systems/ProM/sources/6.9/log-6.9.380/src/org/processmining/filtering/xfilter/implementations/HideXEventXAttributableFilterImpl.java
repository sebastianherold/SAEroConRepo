package org.processmining.filtering.xfilter.implementations;

import java.util.Arrays;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.filtering.filter.factories.FilterFactory;
import org.processmining.filtering.filter.interfaces.Filter;
import org.processmining.filtering.xfilter.interfaces.XAttributableFilter;
import org.processmining.filtering.xfilter.interfaces.XFilter;
import org.processmining.filtering.xflog.implementations.XFTraceImpl;

public class HideXEventXAttributableFilterImpl implements XFilter<XTrace> {
	
	protected XAttributableFilter<XEvent> eventFilter;
	
	public HideXEventXAttributableFilterImpl(XAttributableFilter<XEvent> eventFilter) {
		this.eventFilter = eventFilter;
	}	
	
	public XTrace apply(XTrace t) {
		int[] keep = new int[0];		
		for (int i = 0; i < t.size(); i++) {
			if (eventFilter.apply(t.get(i)) == null) {
				keep = Arrays.copyOf(keep, keep.length + 1);
				keep[keep.length - 1] = i;
			}
		}
		Filter<XEvent> eventFilter = FilterFactory.mirrorFilter();
		Filter<XAttributeMap> traceAttributeFilter = FilterFactory.mirrorFilter();
		return new XFTraceImpl(t, keep, eventFilter, traceAttributeFilter);
	}
	

	public XFilter<XTrace> clone() {
		HideXEventXAttributableFilterImpl clone = null;
		try {
			clone = (HideXEventXAttributableFilterImpl) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	
	
}
