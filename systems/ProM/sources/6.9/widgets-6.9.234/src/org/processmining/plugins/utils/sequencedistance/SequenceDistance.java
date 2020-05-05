package org.processmining.plugins.utils.sequencedistance;

import java.util.List;

public interface SequenceDistance<T> {

	public interface Equivalence<T> {

		boolean equals(T a, T b);

	}

	/**
	 * Holds the result of a sequence distance computation.
	 * 
	 * @author F. Mannhardt
	 * 
	 * @param <T>
	 */
	public interface DistanceResult<T> {

		/**
		 * A single step in the alignment of A and B. Either A or B can be
		 * <code>null</code>, if the distance function did not find a
		 * corresponding "move".
		 * 
		 * @author F. Mannhardt
		 * 
		 * @param <T>
		 */
		public interface Entry<T> {
			T getA();
			T getB();
		}

		/**
		 * Optional alignment of the two sequences. This may be
		 * <code>null</code>.
		 * 
		 * @return a list of {@link Entry} that represents the "alignment" of A
		 *         and B according to the distance function
		 */
		List<Entry<T>> getAlignment();

		/**
		 * @return the distance between A and B
		 */
		int getDistance();
	}

	/**
	 * Compute the distance and a possible alignment between two sequences. 
	 * 
	 * @param a
	 *            first sequence
	 * @param b
	 *            second sequence
	 * @param eq
	 *            equivalence relation on <code>T</code>
	 * @return a distance and optional alignment
	 */
	DistanceResult<T> computeAlignment(T[] a, T[] b, Equivalence<T> eq);

	/**
	 * Compute the distance and a possible alignment between two sequences. 
	 * 
	 * @param a
	 *            first sequence
	 * @param b
	 *            second sequence
	 * @param eq
	 *            equivalence relation on <code>T</code>
	 * @return a distance and optional alignment
	 */
	DistanceResult<T> computeAlignment(List<T> a, List<T> b, Equivalence<T> eq);

	/**
	 * Computes only the distance between two sequences.
	 * 
	 * @param a
	 *            first sequence
	 * @param b
	 *            second sequence
	 * @param eq
	 *            equivalence relation on <code>T</code>
	 * @return the distance
	 */
	int computeDistance(T[] a, T[] b, Equivalence<T> eq);
	
	/**
	 * Computes only the distance between two sequences.
	 * 
	 * @param a
	 *            first sequence
	 * @param b
	 *            second sequence
	 * @param eq
	 *            equivalence relation on <code>T</code>
	 * @return the distance
	 */
	int computeDistance(List<T> a, List<T> b, Equivalence<T> eq);

}