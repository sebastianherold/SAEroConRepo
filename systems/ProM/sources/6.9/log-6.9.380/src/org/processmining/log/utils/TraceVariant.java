package org.processmining.log.utils;

import java.util.List;

/**
 * Trace variant which should override {@link #equals(Object)} and
 * {@link #hashCode()} to provide an equivalence relation for traces. A standard
 * implementation based on the classification of events is provided in
 * {@link TraceVariantByClassifier}.
 * 
 * @author F. Mannhardt
 *
 * @param <E>
 *            what constitutes an event
 */
public interface TraceVariant<E> {

	/**
	 * @return the list of events as viewed by this variant
	 */
	List<E> getEvents();

}