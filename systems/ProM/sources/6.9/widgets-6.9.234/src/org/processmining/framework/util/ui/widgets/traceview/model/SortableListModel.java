package org.processmining.framework.util.ui.widgets.traceview.model;

import java.util.Comparator;

public interface SortableListModel<T> extends MutableListModel<T> {

	void sort(Comparator<T> comparator);
	
}
