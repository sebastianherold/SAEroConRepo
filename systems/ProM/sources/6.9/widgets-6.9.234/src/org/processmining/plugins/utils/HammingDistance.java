package org.processmining.plugins.utils;

import java.util.Comparator;

/**
 * @author michael
 * 
 */
public class HammingDistance {
	/**
	 * @author michael
	 * 
	 * @param <T>
	 */
	public static interface Namer<T> {
		/**
		 * @param object
		 * @return
		 */
		String toString(T object);
	}

	/**
	 * @param <T>
	 * @param name
	 * @param values
	 * @return
	 */
	public static <T> T getBestMatch(final String name, final Iterable<T> values) {
		return HammingDistance.getBestMatch(name, values, new Namer<T>() {
			@Override
			public String toString(final T object) {
				if (object == null) {
					return "<null>";
				}
				return object.toString();
			}
		});
	}

	/**
	 * @param <T>
	 * @param name
	 * @param values
	 * @param namer
	 * @return
	 */
	public static <T> T getBestMatch(final String name, final Iterable<T> values, final Namer<T> namer) {
		return HammingDistance.getBestMatch(name, values, namer, new Comparator<T>() {
			@Override
			public int compare(final T o1, final T o2) {
				return 0;
			}
		});
	}

	/**
	 * @param <T>
	 * @param name
	 * @param values
	 * @param namer
	 * @param comparator
	 * @return
	 */
	public static <T> T getBestMatch(final String name, final Iterable<T> values, final Namer<T> namer,
			final Comparator<T> comparator) {
		int match = -1;
		T result = null;
		for (final T value : values) {
			final String newName = namer.toString(value);
			final int newMatch = HammingDistance.hammingDistance(name, newName, false);
			if (newMatch > match) {
				match = newMatch;
				result = value;
			}
			if (newMatch == match) {
				final int thisStrictMatch = HammingDistance.hammingDistance(name, newName);
				final int oldStrictMatch = HammingDistance.hammingDistance(name, namer.toString(result));
				if (thisStrictMatch > oldStrictMatch) {
					match = newMatch;
					result = value;
				}
				if (thisStrictMatch == oldStrictMatch) {
					if (comparator.compare(result, value) < 0) {
						match = newMatch;
						result = value;
					}
				}
			}
		}
		return result;
	}

	/**
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static int hammingDistance(final String s1, final String s2) {
		return HammingDistance.hammingDistance(s1, s2, true);
	}

	/**
	 * @param s1
	 * @param s2
	 * @param caseSensitive
	 * @return
	 */
	public static int hammingDistance(String s1, String s2, final boolean caseSensitive) {
		if (s2.length() > s1.length()) {
			final String tmp = s1;
			s1 = s2;
			s2 = tmp;
		}
		final int[][] matrix = new int[2][s2.length() + 1];
		for (int i = 1; i < s1.length() + 1; i++) {
			for (int j = 1; j < s2.length() + 1; j++) {
				int result = Math.max(matrix[(i - 1) % 2][j], matrix[i % 2][j - 1]);
				if (s1.charAt(i - 1) == s2.charAt(j - 1) || !caseSensitive
						&& Character.toLowerCase(s1.charAt(i - 1)) == Character.toLowerCase(s2.charAt(j - 1))) {
					result = Math.max(result, matrix[(i - 1) % 2][j - 1] + 1);
				}
				matrix[i % 2][j] = result;
			}
		}
		return matrix[s1.length() % 2][s2.length()];
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		HammingDistance.test("Michael", "mikael");
		HammingDistance.test("Britney", "britney");
		HammingDistance.test("Lady Gaga", "ladygaga");
	}

	/**
	 * @param s1
	 * @param s2
	 */
	public static void test(final String s1, final String s2) {
		System.out.print("\"" + s1 + "\"");
		System.out.print(" -- ");
		System.out.print("\"" + s2 + "\"");
		System.out.print(": ");
		System.out.println(HammingDistance.hammingDistance(s1, s2));
		System.out.print("\"" + s1 + "\"");
		System.out.print(" -- ");
		System.out.print("\"" + s2 + "\"");
		System.out.print(": ");
		System.out.println(HammingDistance.hammingDistance(s1, s2, false));
	}
}
