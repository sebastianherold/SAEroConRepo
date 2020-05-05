package org.processmining.framework.util.ui.widgets.traceview.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Provides a filtered {@link ListModel} based on a supplied
 * {@link ListModelFilter}.
 * 
 * @author F.Mannhardt
 *
 * @param <E>
 */
public final class FilteredListModelImpl<T extends ListModel<E>, E> extends AbstractListModel<E>
		implements FilteredListModel<E> {

	private static final long serialVersionUID = -5708650645208821099L;

	public static interface ListModelFilter<E> {
		boolean accept(E e);
	}

	private ListModelFilter<E> currentFilter;
	private final T originalListModel;
	private final List<Integer> visibleElements = new ArrayList<Integer>();

	public FilteredListModelImpl(T source) {
		originalListModel = source;
		getUnfilteredListModel().addListDataListener(new ListDataListener() {
			public void intervalRemoved(ListDataEvent e) {
				boolean changed = doFilter();
				if (!changed) {
					fireIntervalRemoved(e.getSource(), e.getIndex0(), e.getIndex1());
				}
			}

			public void intervalAdded(ListDataEvent e) {
				boolean changed = doFilter();
				if (!changed) {
					fireIntervalAdded(e.getSource(), e.getIndex0(), e.getIndex1());
				}
			}

			public void contentsChanged(ListDataEvent e) {
				boolean changed = doFilter();
				if (!changed) {
					fireContentsChanged(e.getSource(), e.getIndex0(), e.getIndex1());
				}
			}
		});
	}

	public T getUnfilteredListModel() {
		return originalListModel;
	}

	public void filter(ListModelFilter<E> f) {
		currentFilter = f;
		doFilter();
	}

	protected boolean doFilter() {
		ListModelFilter<E> f = currentFilter;
		if (f != null) {
			visibleElements.clear();
			int count = getUnfilteredListModel().getSize();
			for (int i = 0; i < count; i++) {
				if (f.accept(getUnfilteredListModel().getElementAt(i))) {
					visibleElements.add(i);
				}
			}
			fireContentsChanged(this, 0, getSize() - 1);
			return true;
		}
		return false;
	}

	@Override
	public int getSize() {
		return (currentFilter != null) ? visibleElements.size() : getUnfilteredListModel().getSize();
	}

	@Override
	public E getElementAt(int index) {
		return (currentFilter != null) ? getUnfilteredListModel().getElementAt(visibleElements.get(index))
				: getUnfilteredListModel().getElementAt(index);
	}

}