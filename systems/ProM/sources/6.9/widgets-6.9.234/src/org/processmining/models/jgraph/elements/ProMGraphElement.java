package org.processmining.models.jgraph.elements;

import java.util.Map;

import org.jgraph.graph.CellView;

public interface ProMGraphElement {

	CellView getView();

	void updateViewsFromMap();

	Map getAttributes();
}
