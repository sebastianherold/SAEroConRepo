package org.processmining.filtering.filter.interfaces;

/**
 * A filter is in essence a "function" applied on some input. A filter could be
 * defined as a function f : T -> T. A filter is a cloneable object.
 * 
 * @param <T>
 *            generic type on which this filter is applied.
 */
public interface Filter<T> extends Cloneable {
	
	/**
	 * Apply this filter on some input.
	 * 
	 * @param t
	 *            input object of type T
	 * @return return object of type T
	 */
	public T apply(T t);

	/**
	 * Create this filter's clone.
	 * 
	 * @return clone of filter.
	 */
	public Filter<T> clone();
}
