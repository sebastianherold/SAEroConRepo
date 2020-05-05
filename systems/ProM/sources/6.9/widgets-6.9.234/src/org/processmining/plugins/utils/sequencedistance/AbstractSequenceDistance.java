package org.processmining.plugins.utils.sequencedistance;

import java.util.Arrays;

public abstract class AbstractSequenceDistance<T> implements SequenceDistance<T> {

	public AbstractSequenceDistance() {
		super();
	}

	public DistanceResult<T> computeAlignment(T[] a, T[] b, SequenceDistance.Equivalence<T> eq) {
		return computeAlignment(Arrays.asList(a), Arrays.asList(b), eq);
	}

	public int computeDistance(T[] a, T[] b, SequenceDistance.Equivalence<T> eq) {
		return computeDistance(Arrays.asList(a), Arrays.asList(b), eq);
	}

}