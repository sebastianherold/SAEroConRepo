package org.processmining.framework.util.ui.widgets.traceview.masterdetail;

import java.util.Comparator;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;

public interface DetailView<T> {
	
	JComponent getDetailComponent();
	
	ProMTraceList<T> getDetailList();

	Comparator<T> getSortOrder();

}
