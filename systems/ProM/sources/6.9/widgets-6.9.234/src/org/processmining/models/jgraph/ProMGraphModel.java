package org.processmining.models.jgraph;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.jgraph.event.GraphModelEvent;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.ParentMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;

public class ProMGraphModel extends DefaultGraphModel implements ModelOwner {

	private static final long serialVersionUID = 9097862538097193482L;
	private final DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> graph;

	public ProMGraphModel(
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> graph) {
		this.graph = graph;
	}

	public DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> getGraph() {
		return graph;
	}

	public String toString() {
		return graph.toString();
	}

	public ProMGraphModel getModel() {
		return this;
	}

	/**
	 * Invoke this method after you've changed how the cells are to be
	 * represented in the graph.
	 */
	public void cellsChanged(final Object[] cells, final Rectangle2D dirtyRegion) {
		if (cells != null) {
			fireGraphChanged(this, new GraphModelEvent.GraphModelChange() {

				public Object[] getInserted() {
					return null;
				}

				public Object[] getRemoved() {
					return null;
				}

				public Map<?, ?> getPreviousAttributes() {
					return null;
				}

				public ConnectionSet getConnectionSet() {
					return null;
				}

				public ConnectionSet getPreviousConnectionSet() {
					return null;
				}

				public ParentMap getParentMap() {
					return null;
				}

				public ParentMap getPreviousParentMap() {
					return null;
				}

				public void putViews(GraphLayoutCache view, CellView[] cellViews) {
				}

				public CellView[] getViews(GraphLayoutCache view) {
					return null;
				}

				public Object getSource() {
					return this;
				}

				public Object[] getChanged() {
					return cells;
				}

				public Map<?, ?> getAttributes() {
					return null;
				}

				public Object[] getContext() {
					return null;
				}

				public Rectangle2D getDirtyRegion() {
					return dirtyRegion;
				}

				public void setDirtyRegion(Rectangle2D dirty) {
				}

			});
		}
	}

}
