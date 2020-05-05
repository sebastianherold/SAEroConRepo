package org.processmining.models.jgraph;

import java.util.Collection;

import javax.swing.JPopupMenu;

import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphElement;

public interface ContextMenuCreator {

	JPopupMenu createMenuFor(DirectedGraph<?, ?> graph, Collection<DirectedGraphElement> selectedElements);

}
