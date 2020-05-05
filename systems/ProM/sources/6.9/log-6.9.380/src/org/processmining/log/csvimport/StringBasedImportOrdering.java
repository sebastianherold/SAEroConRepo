package org.processmining.log.csvimport;

import com.google.common.collect.Ordering;

final class StringBasedImportOrdering extends Ordering<String[]> {

	private final int[] sortingIndices;

	public StringBasedImportOrdering(int[] sortingIndicies) {
		this.sortingIndices = sortingIndicies;
	}

	public int compare(String[] o1, String[] o2) {
		if (o1.length != o2.length) {
			throw new IllegalArgumentException(
					"Can only compare lines in a CSV file with the same number of columns!");
		}
		// First compare on all the case columns
		for (int i = 0; i < sortingIndices.length; i++) {
			int index = sortingIndices[i];
			// We treat empty and NULL cells as the same as there is no concept of a NULL cell in CSV 
			String s1 = o1[index] == null ? "" : o1[index];
			String s2 = o2[index] == null ? "" : o2[index];
			int comp = s1.compareTo(s2);
			if (comp != 0) {
				// Case ID is different on current index
				return comp;
			}
		}
		// Keep ordering -> using a stable sort algorithm
		return 0;
	}

}