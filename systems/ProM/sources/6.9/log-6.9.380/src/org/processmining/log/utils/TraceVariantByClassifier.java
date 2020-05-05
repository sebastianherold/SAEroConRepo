package org.processmining.log.utils;

import java.util.Iterator;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

public final class TraceVariantByClassifier implements TraceVariant<XEventClass> {

	private final XTrace trace;
	private final XEventClasses eventClasses;

	public TraceVariantByClassifier(XTrace trace, XEventClasses eventClasses) {
		this.trace = trace;
		this.eventClasses = eventClasses;
	}

	public ImmutableList<XEventClass> getEvents() {
		return ImmutableList.copyOf(Lists.transform(trace, new Function<XEvent, XEventClass>() {

			public XEventClass apply(XEvent e) {
				return eventClasses.getClassOf(e);
			}
		}));
	}

	public int hashCode() {
		int hashCode = 1;
		for (XEvent e : trace) {
			XEventClass eventClass = eventClasses.getClassOf(e);
			hashCode = 31 * hashCode + (e == null ? 0 : Ints.hashCode(eventClass.getIndex()));
		}
		return hashCode;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		TraceVariantByClassifier other = (TraceVariantByClassifier) obj;
		if (trace == null) {
			if (other.trace != null)
				return false;
		} else if (!isEqualVariant(trace, other.trace))
			return false;
		return true;
	}

	private boolean isEqualVariant(XTrace t1, XTrace t2) {
		if (t1 == t2) {
			return true;
		}

		Iterator<XEvent> it1 = t1.iterator();
		Iterator<XEvent> it2 = t2.iterator();

		while (it1.hasNext() && it2.hasNext()) {
			XEventClass cl1 = eventClasses.getClassOf(it1.next());
			XEventClass cl2 = eventClasses.getClassOf(it2.next());
			if (cl1.getIndex() != cl2.getIndex()) {
				return false;
			}
		}
		return !(it1.hasNext() || it2.hasNext());
	}

}