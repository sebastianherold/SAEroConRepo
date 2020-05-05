package org.processmining.models.jgraph;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.event.MouseInputAdapter;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.Expandable;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.renderers.ProMGroupShapeRenderer;
import org.processmining.models.jgraph.views.JGraphShapeView;

/**
 * Mananges the folding and unfolding of groups
 */
public class JGraphFoldingManager extends MouseInputAdapter {

	private final GraphLayoutConnection layoutConnection;

	public JGraphFoldingManager(GraphLayoutConnection layoutConnection) {
		super();
		this.layoutConnection = layoutConnection;

	}

	/**
	 * Called when the mouse button is released to see if a collapse or expand
	 * request has been made
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.getSource() instanceof JGraph) {
			final JGraph graph = (JGraph) e.getSource();
			Pair<Expandable, CellView> pair = getGroupByFoldingHandle(graph, e.getPoint());
			if (pair != null) {
				if (pair.getSecond().isLeaf()) {
					layoutConnection.expand(pair.getFirst());
					layoutConnection.updated();
				} else {
					layoutConnection.collapse(pair.getFirst());
					layoutConnection.updated();
				}
			}
			e.consume();
		}
	}

	/**
	 * Called when the mouse button is released to see if a collapse or expand
	 * request has been made
	 */
	public static Pair<Expandable, CellView> getGroupByFoldingHandle(JGraph graph, Point2D pt) {
		CellView[] views = graph.getGraphLayoutCache().getCellViews();
		for (int i = 0; i < views.length; i++) {
			Point2D containerPoint = graph.fromScreen((Point2D) pt.clone());
			if (views[i].getBounds().contains(containerPoint.getX(), containerPoint.getY())) {
				Rectangle2D rectBounds = views[i].getBounds();
				containerPoint.setLocation(containerPoint.getX() - rectBounds.getX(), containerPoint.getY()
						- rectBounds.getY());
				Component renderer = views[i].getRendererComponent(graph, false, false, false);
				if (renderer instanceof ProMGroupShapeRenderer) {
					DirectedGraphNode node = ((JGraphShapeView) views[i]).getNode();
					boolean isGroup = (node instanceof Expandable);
					if (isGroup) {
						ProMGroupShapeRenderer group = (ProMGroupShapeRenderer) renderer;
						if (group.inHitRegion(containerPoint)) {
							return new Pair<Expandable, CellView>((Expandable) node, views[i]);
						}
					}
				}
			}
		}
		return null;
	}

}
