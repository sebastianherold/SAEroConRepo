package org.processmining.filtering.xflog.implementations;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract shuffle insertion list is a list that allows a programmer to
 * hide/add/duplicate elements from the list. It does so by maintaining an array
 * of indices referring either to: 1. the original list 2. a set of "added"
 * elements.
 * 
 * @author S.J. van Zelst
 * 
 * @param <T>
 *            generic type of elements within the list.
 */
public class ShuffleInsertionList<T> extends AbstractList<T> implements Cloneable {

	protected List<T> source;
	protected int[] positions;

	@SuppressWarnings("unchecked")
	protected T[] newElements = (T[]) new Object[0];

	public ShuffleInsertionList(List<T> source) {
		this.source = source;
		positions = new int[source.size()];
		for (int i = 0; i < source.size(); i++) {
			positions[i] = i;
		}
	}

	public ShuffleInsertionList(List<T> source, int[] elementPositions) {
		this.source = source;
		this.positions = elementPositions;
	}

	//TODO: Test addition of (artificial) events
	public ShuffleInsertionList(List<T> source, List<T> modifiedOrder) {
		this.source = source;
		Map<T, Integer> newElementMap = new HashMap<>();
		positions = new int[modifiedOrder.size()];
		for (int i = 0; i < modifiedOrder.size(); i++) {
			T t = modifiedOrder.get(i);
			if (source.contains(t)) {
				positions[i] = source.indexOf(t);
			} else {
				if (!(newElementMap.keySet().contains(t))) {
					newElements = Arrays.copyOf(newElements, newElements.length + 1);
					newElements[newElements.length - 1] = t;
					newElementMap.put(t, newElements.length * -1);
				}
				positions[i] = newElementMap.get(t);
			}
		}
	}

	public int size() {
		return positions.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		ShuffleInsertionList<T> clone = null;
		try {
			clone = (ShuffleInsertionList<T>) super.clone();
			clone.source = new ArrayList<T>(source);
			clone.positions = positions.clone();
			clone.newElements = newElements.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	public T get(int index) {
		T result;
		if (positions[index] >= 0) {
			result = source.get(positions[index]);
		} else {
			result = newElements[(positions[index] * -1) - 1];
		}
		return result;
	}

}
