package org.processmining.framework.util.ui.widgets.traceview.masterdetail;

import java.util.Collection;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList;

public interface MasterView<M, D> {
	
	JComponent getMasterComponent();
	
	ProMTraceList<M> getMasterList();
	
	Collection<D> getDetailElements(M element);

}
