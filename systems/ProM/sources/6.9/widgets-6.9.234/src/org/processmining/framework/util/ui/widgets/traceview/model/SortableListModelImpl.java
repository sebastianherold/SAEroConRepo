package org.processmining.framework.util.ui.widgets.traceview.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;
import javax.swing.ListModel;

/**
 * Provides a sorted {@link ListModel} based on an {@link ArrayList} for use
 * with a {@link JList}.
 * 
 * @author F. Mannhardt
 *
 * @param <T>
 */
public final class SortableListModelImpl<T> extends AbstractListModel<T> implements SortableListModel<T> {

	private static final long serialVersionUID = -8658365206622116891L;

	private final List<T> sortedList;

	public SortableListModelImpl() {
		this(Collections.<T>emptyList());
	}

	public SortableListModelImpl(Collection<T> traces) {
		super();
		this.sortedList = new ArrayList<T>(traces);
	}

	public int getSize() {
		return sortedList.size();
	}

	public T getElementAt(int index) {
		return sortedList.get(index);
	}

	public boolean add(T element) {
		if (sortedList.add(element)) {
			fireIntervalAdded(this, sortedList.size(), sortedList.size());
			return true;
		} else {
			return false;
		}
	}

	public boolean addAll(Iterable<T> elements) {
		final int sizeBefore = sortedList.size();
		boolean hasAdded = false;
		if (elements instanceof Collection) {
			hasAdded = sortedList.addAll((Collection<T>) elements);
		} else {
			for (T item : elements) {
				// Side-effect is adding the element 
				hasAdded = sortedList.add(item) || hasAdded;
			}
		}
		if (hasAdded) {
			fireIntervalAdded(this, sizeBefore + 1, sortedList.size());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.util.ui.widgets.traceview.SortableListModel#
	 * sort(java.util.Comparator)
	 */
	public void sort(Comparator<T> comparator) {
		Collections.sort(sortedList, comparator);
		fireContentsChanged(this, 0, sortedList.size());
	}

	public void clear() {
		sortedList.clear();
		fireIntervalRemoved(this, 0, sortedList.size());
	}

}