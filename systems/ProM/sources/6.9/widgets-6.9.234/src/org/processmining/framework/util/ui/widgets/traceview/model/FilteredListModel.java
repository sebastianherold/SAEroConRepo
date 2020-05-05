package org.processmining.framework.util.ui.widgets.traceview.model;

import javax.swing.ListModel;

import org.processmining.framework.util.ui.widgets.traceview.model.FilteredListModelImpl.ListModelFilter;

public interface FilteredListModel<E> {

	void filter(ListModelFilter<E> filter);

	ListModel<E> getUnfilteredListModel();

}
