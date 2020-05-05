package org.processmining.filtering.filter.factories;

import java.util.List;

import org.processmining.filtering.filter.implementations.FilterStackImpl;
import org.processmining.filtering.filter.implementations.MirrorFilterImpl;
import org.processmining.filtering.filter.interfaces.Filter;
import org.processmining.filtering.filter.interfaces.FilterStack;

public class FilterFactory {

	public static <T> Filter<T> mirrorFilter() {
		return new MirrorFilterImpl<T>();
	}

	public static <T> FilterStack<T> filterStack(List<Filter<T>> filters) {
		return new FilterStackImpl<T>(filters);
	}

}
