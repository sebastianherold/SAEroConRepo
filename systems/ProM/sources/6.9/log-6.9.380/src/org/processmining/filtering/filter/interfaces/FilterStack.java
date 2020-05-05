package org.processmining.filtering.filter.interfaces;

import java.util.List;

/**
 * A Filter stack is (as the name suggests) a "stack of filters". It is in
 * essence a list of filters, all of the type f: T -> T. If we have a list of
 * filters L = <f0, f1, ..., fn> and we hand over object t of type T, the result
 * will be fn(fn-1(...f1(f0(t)))).
 * 
 * @param <T>
 *            generic type on which this filter is applied.
 */
public interface FilterStack<T> extends Filter<T>, List<Filter<T>> {
	
	public FilterStack<T> clone();

}