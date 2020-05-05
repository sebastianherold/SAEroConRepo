package org.processmining.framework.util.ui.widgets.traceview.model;

import javax.swing.ListModel;

public interface MutableListModel<T> extends ListModel<T> {

	boolean add(T element);
	
	boolean addAll(Iterable<T> elements);

	void clear();
	
}
