package org.processmining.filtering.xflog.implementations;

import java.util.List;
import java.util.Set;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.deckfour.xes.util.XAttributeUtils;
import org.processmining.filtering.filter.interfaces.Filter;
import org.processmining.filtering.xflog.interfaces.XFTrace;

public class XFTraceImpl extends ShuffleInsertionList<XEvent> implements XFTrace {

	protected XAttributeMap attributes;
	protected XTrace source;

	protected Filter<XEvent> eventAttributeFilter;
	protected Filter<XAttributeMap> traceAttributeFilter;

	public XFTraceImpl(XTrace source, Filter<XEvent> eventFilter, Filter<XAttributeMap> traceAttributeFilter) {
		super(source);
		init(source, eventFilter, traceAttributeFilter);
	}

	public XFTraceImpl(XTrace source, int[] eventPositions, Filter<XEvent> eventAttributeFilter,
			Filter<XAttributeMap> traceAttributeFilter) {
		super(source, eventPositions);
		init(source, eventAttributeFilter, traceAttributeFilter);
	}

	public XFTraceImpl(XTrace source, List<XEvent> modifiedOrder, Filter<XEvent> eventAttributeFilter,
			Filter<XAttributeMap> traceAttributeFilter) {
		super(source, modifiedOrder);
		init(source, eventAttributeFilter, traceAttributeFilter);
	}

	protected void init(XTrace source, Filter<XEvent> eventAttributeFilter, Filter<XAttributeMap> traceAttributeFilter) {
		this.source = source;
		attributes = source.getAttributes();
		this.eventAttributeFilter = eventAttributeFilter;
		this.traceAttributeFilter = traceAttributeFilter;
	}

	public XEvent get(int index) {
		return eventAttributeFilter.apply(super.get(index));
	}

	@Override
	public Object clone() {
		XFTrace clone = null;
		clone = (XFTraceImpl) super.clone();
		clone.setSource((XTrace) source.clone());
		clone.setAttributes((XAttributeMap) attributes.clone());
		clone.setEventFilter(eventAttributeFilter);
		clone.setTraceAttributeFilter(traceAttributeFilter);
		clone.clear();
		for (XEvent event : this) {
			clone.add((XEvent) event.clone());
		}
		return clone;
	}

	@Override
	public void accept(XVisitor visitor, XLog log) {
		// first call;
		visitor.visitTracePre(this, log);

		// visit attributes
		for (XAttribute attribute : attributes.values()) {
			attribute.accept(visitor, this);
		}

		// visit events
		for (XEvent event : this) {
			event.accept(visitor, this);
		}

		// final call
		visitor.visitTracePost(this, log);
	}

	@Override
	public XAttributeMap getAttributes() {
		return traceAttributeFilter.apply(attributes);
	}

	@Override
	public void setAttributes(XAttributeMap attributes) {
		this.attributes = attributes;

	}

	@Override
	public Set<XExtension> getExtensions() {
		return XAttributeUtils.extractExtensions(getAttributes());
	}

	@Override
	public boolean hasAttributes() {
		return !(attributes.isEmpty());
	}

	@Override
	public XTrace getSource() {
		return source;
	}

	@Override
	public void setSource(XTrace trace) {
		source = trace;
		attributes = trace.getAttributes();
	}

	@Override
	public void setEventFilter(Filter<XEvent> filter) {
		eventAttributeFilter = filter;
	}

	@Override
	public void setTraceAttributeFilter(Filter<XAttributeMap> filter) {
		traceAttributeFilter = filter;
	}

	/**
	 * The interface describes an "ordering based on time". This does no longer
	 * comply with the "current" view on Logs.
	 */
	public int insertOrdered(XEvent event) {
		throw new UnsupportedOperationException();
	}

}
