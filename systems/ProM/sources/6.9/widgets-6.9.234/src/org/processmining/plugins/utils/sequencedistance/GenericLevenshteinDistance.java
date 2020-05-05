package org.processmining.plugins.utils.sequencedistance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic Levenshtein Distance computation that also returns an "aligned"
 * version of the two input Lists. Adapted from this post at <a href=
 * "http://stackoverflow.com/questions/15042879/java-characters-alignment-algorithm/15043478#15043478"
 * >Stack Overflow</a>.
 * <p>
 * Please note that this class is not thread-safe.
 * 
 * @author F. Mannhardt
 * 
 * @param <T>
 *            type of the objects
 */
public final class GenericLevenshteinDistance<T> extends AbstractSequenceDistance<T> {

	private final class ResultEntryImpl implements DistanceResult.Entry<T> {

		private final T a;
		private final T b;

		public ResultEntryImpl(T a, T b) {
			this.a = a;
			this.b = b;
		}

		public T getA() {
			return a;
		}

		public T getB() {
			return b;
		}

	}

	private final int maxDistance;
	
	private int[][] matrix;
	private int[] array1;
	private int[] array2;
	
	public GenericLevenshteinDistance() {
		this(Integer.MAX_VALUE);
	}

	public GenericLevenshteinDistance(int maxDistance) {
		this(maxDistance, 30);
	}
	
	public GenericLevenshteinDistance(int maxDistance, int initialCapacity) {
		super();
		this.maxDistance = maxDistance;
		this.matrix = new int[initialCapacity][initialCapacity];
		this.array1 = new int[initialCapacity];
		this.array2 = new int[initialCapacity];
	}
	

	public final DistanceResult<T> computeAlignment(final List<T> a, final List<T> b,
			final SequenceDistance.Equivalence<T> eq) {
		
		if (a == b) {
			return new DistanceResult<T>() {

				public int getDistance() {
					return 0;
				}

				public List<DistanceResult.Entry<T>> getAlignment() {
					ArrayList<DistanceResult.Entry<T>> alignment = new ArrayList<DistanceResult.Entry<T>>(a.size());
					for (int i = 0; i < a.size(); i++) {
						alignment.add(new ResultEntryImpl(a.get(i), b.get(i)));
					}
					return alignment;
				}

			};
		}

		final int sizeA = a.size();
		final int sizeB = b.size();

		final int[][] matrix = getMatrix(sizeA + 1, sizeB + 1);

		for (int i = 0; i <= sizeA; i++) {
			matrix[i][0] = i;
		}
		for (int i = 0; i <= sizeB; i++) {
			matrix[0][i] = i;
		}

		for (int i = 1; i <= sizeA; i++) {
			for (int j = 1; j <= sizeB; j++) {
				if (eq.equals(a.get(i - 1), b.get(j - 1))) {
					matrix[i][j] = matrix[i - 1][j - 1];
				} else {
					matrix[i][j] = Math.min(matrix[i - 1][j], matrix[i][j - 1]) + 1;
				}
			}
		}

		final int distance = matrix[sizeA][sizeB];

		final List<DistanceResult.Entry<T>> result = new ArrayList<DistanceResult.Entry<T>>(Math.max(sizeA, sizeB));
		for (int i = sizeA, j = sizeB; i > 0 || j > 0;) {
			if (i > 0 && matrix[i][j] == matrix[i - 1][j] + 1) {
				result.add(new ResultEntryImpl(a.get(--i), null));
			} else if (j > 0 && matrix[i][j] == matrix[i][j - 1] + 1) {
				result.add(new ResultEntryImpl(null, b.get(--j)));
			} else if (i > 0 && j > 0 && matrix[i][j] == matrix[i - 1][j - 1]) {
				result.add(new ResultEntryImpl(a.get(--i), b.get(--j)));
			}
		}
		Collections.reverse(result);

		return new DistanceResult<T>() {

			public int getDistance() {
				return distance;
			}

			public List<DistanceResult.Entry<T>> getAlignment() {
				return result;
			}

		};
	}

	private final int[][] getMatrix(final int sizeA, final int sizeB) {
		if (matrix.length < sizeA || matrix[0].length < sizeB) {
			int newSize = Math.max(sizeA, sizeB);
			matrix = new int[newSize][newSize];
		}
		return matrix;
	}

	public final int computeDistance(List<T> a, List<T> b, SequenceDistance.Equivalence<T> eq) {
		
		if (a == b) {
			return 0;
		}
		
		int aSize = a.size() + 1;
		int bSize = b.size() + 1;

		int[] array1 = getArray1(aSize);
		int[] array2 = getArray2(aSize);

		for (int i = 0; i < aSize; i++) {
			array1[i] = i;
		}

		for (int j = 1; j < bSize; j++) {
			array2[0] = j;
			for (int i = 1; i < aSize; i++) {
				int substitution = array1[i - 1] + (eq.equals(a.get(i - 1), b.get(j - 1)) ? 0 : 1);
				int insertion = array1[i] + 1;
				int deletion = array2[i - 1] + 1;
				array2[i] = Math.min(Math.min(insertion, deletion), substitution);
			}
			int[] temp = array1;
			array1 = array2;
			array2 = temp;
		}
		return array1[aSize - 1];
	}

	private int[] getArray1(int size) {
		if (array1.length < size) {
			array1 = new int[Math.max(size, array1.length * 2)];
		}
		return array1;
	}

	private int[] getArray2(int size) {
		if (array2.length < size) {
			array2 = new int[Math.max(size, array2.length * 2)];
		}
		return array2;
	}

}
