package org.processmining.filtering.xflog.implementations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.XVisitor;
import org.processmining.filtering.filter.interfaces.Filter;
import org.processmining.filtering.xflog.interfaces.XFLog;

public class XFLogImpl extends ShuffleInsertionList<XTrace> implements XFLog {

	/**
	 * Filter related variables
	 */
	protected XLog source;
	protected XAttributeMap attributes;
	protected Set<XExtension> extensions = new HashSet<>();
	protected List<XEventClassifier> classifiers = new ArrayList<>();
	protected List<XAttribute> globalTraceAttributes = new ArrayList<>();
	protected List<XAttribute> globalEventAttributes = new ArrayList<>();
	protected XLogInfo cachedInfo = null;
	protected XEventClassifier cachedClassifier = null;

	Filter<XTrace> traceFilter;
	Filter<XAttributeMap> logAttributeFilter;

	public XFLogImpl(XLog source, Filter<XTrace> traceFilter, Filter<XAttributeMap> logAttributeFilter) {
		super(source);
		init(source, traceFilter, logAttributeFilter);
	}

	public XFLogImpl(XLog source, int[] tracePositions, Filter<XTrace> traceFilter,
			Filter<XAttributeMap> logAttributeFilter) {
		super(source, tracePositions);
		init(source, traceFilter, logAttributeFilter);
	}

	public XFLogImpl(XLog source, List<XTrace> modifiedOrder, Filter<XTrace> traceFilter,
			Filter<XAttributeMap> logAttributeFilter) {
		super(source, modifiedOrder);
		init(source, traceFilter, logAttributeFilter);
	}

	protected void init(XLog source, Filter<XTrace> traceFilter, Filter<XAttributeMap> logAttributeFilter) {
		this.source = source;
		attributes = source.getAttributes();
		this.traceFilter = traceFilter;
		this.logAttributeFilter = logAttributeFilter;
	}

	public Object clone() {
		XFLogImpl clone = null;
		try {
			clone = (XFLogImpl) super.clone();
			clone.attributes = (XAttributeMap) attributes.clone();
			clone.extensions = new HashSet<>(extensions);
			clone.classifiers = new ArrayList<>(classifiers);
			clone.globalTraceAttributes = new ArrayList<>(globalTraceAttributes);
			clone.globalEventAttributes = new ArrayList<>(globalEventAttributes);
			clone.cachedClassifier = null;
			clone.cachedInfo = null;
			clone.source = (XLog) this.source.clone();
			clone.traceFilter = traceFilter;
			clone.logAttributeFilter = logAttributeFilter;

			clone.setSource((XLog) source.clone());
			clone.clear();
			for (XTrace trace : this) {
				clone.add((XTrace) trace.clone());
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return clone;
	}

	public XAttributeMap getAttributes() {
		return logAttributeFilter.apply(attributes);
	}

	public void setAttributes(XAttributeMap attributes) {
		this.attributes = attributes;

	}

	public Set<XExtension> getExtensions() {
		return extensions;
	}

	public boolean hasAttributes() {
		return !attributes.isEmpty();
	}

	public XLog getSource() {
		return source;
	}

	public void setSource(XLog log) {
		source = log;
	}

	public XTrace get(int index) {
		return traceFilter.apply(super.get(index));
	}

	@Override
	public void setTraceFilter(Filter<XTrace> filter) {
		traceFilter = filter;
	}

	@Override
	public void setLogAttributeFilter(Filter<XAttributeMap> filter) {
		logAttributeFilter = filter;
	}

	public boolean accept(XVisitor visitor) {
		boolean accept = false;
		if (visitor.precondition()) {
			accept = true;
			visitor.init(this);
			visitor.visitLogPre(this);

			// visit extensions
			for (XExtension extension : extensions) {
				extension.accept(visitor, this);
			}

			// visit classifiers
			for (XEventClassifier classifier : classifiers) {
				classifier.accept(visitor, this);
			}

			// visit attributes
			for (XAttribute attribute : attributes.values()) {
				attribute.accept(visitor, this);
			}

			for (XTrace trace : this) {
				trace.accept(visitor, this);
			}

			visitor.visitLogPost(this);
		}
		return accept;
	}

	//TODO: Check what the impact of filtering is on these functions!
	public List<XEventClassifier> getClassifiers() {
		return classifiers;
	}

	public List<XAttribute> getGlobalEventAttributes() {
		return globalEventAttributes;
	}

	public List<XAttribute> getGlobalTraceAttributes() {
		return globalTraceAttributes;
	}

	public XLogInfo getInfo(XEventClassifier classifier) {
		return classifier.equals(cachedClassifier) ? cachedInfo : null;
	}

	public void setInfo(XEventClassifier classifier, XLogInfo info) {
		cachedClassifier = classifier;
		cachedInfo = info;
	}

}
