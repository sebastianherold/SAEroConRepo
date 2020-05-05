package org.processmining.filtering.filter.implementations;

import java.util.ArrayList;
import java.util.List;

import org.processmining.filtering.filter.interfaces.Filter;
import org.processmining.filtering.filter.interfaces.FilterStack;

/**
 * @see FilterStack
 * 
 * @author S.J. van Zelst
 * 
 * @param <T>
 *            generic type on which this filter stack should be applied.
 */
public class FilterStackImpl<T> extends ArrayList<Filter<T>> implements FilterStack<T> {

	private static final long serialVersionUID = 6042187895669094422L;

	public FilterStackImpl(List<Filter<T>> filters) {
		this.addAll(filters);
	}

	@Override
	public T apply(T t) {
		T result = t;
		for (Filter<T> filter : this) {
			result = filter.apply(result);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public FilterStack<T> clone() {
		FilterStackImpl<T> clone = null;
		clone = (FilterStackImpl<T>) super.clone();
		return clone;
	}
	

}
