package org.processmining.models.jgraph.views;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexView;
import org.processmining.framework.util.Cleanable;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.renderers.ProMGroupShapeRenderer;
import org.processmining.models.jgraph.renderers.ProMShapeRenderer;
import org.processmining.models.shapes.Rectangle;
import org.processmining.models.shapes.Shape;

public class JGraphShapeView extends VertexView implements Cleanable {

	public final static Shape RECTANGLE = new Rectangle();

	private static final long serialVersionUID = 660416141359812229L;
	private static ProMGroupShapeRenderer renderer;
	private DirectedGraphNode node;
	private Shape shape;

	private final boolean isPIP;

	private final ViewSpecificAttributeMap viewSpecificAttributes;

	@SuppressWarnings("unchecked")
	public JGraphShapeView(ProMGraphCell cell, boolean isPIP, ViewSpecificAttributeMap viewSpecificAttributes) {
		super(cell);
		this.isPIP = isPIP;
		this.viewSpecificAttributes = viewSpecificAttributes;
		node = cell.getNode();
		shape = node.getAttributeMap().get(AttributeMap.SHAPE, RECTANGLE);

		GraphConstants.setMoveable(getAttributes(), node.getAttributeMap().get(AttributeMap.MOVEABLE, true));

		groupBounds = null;
		childViews = new ArrayList();
		for (Object child : cell.getChildren()) {
			if (child instanceof ProMGraphCell) {
				ProMGraphCell c2 = (ProMGraphCell) child;
				if (c2.getView() != null) {
					childViews.add(c2.getView());
				}
			}
		}
		setCachedBounds(GraphConstants.getBounds(cell.getAttributes()));

	}

	@Override
	public ProMShapeRenderer getRenderer() {
		if (renderer == null) {
			renderer = node.getAttributeMap().get(AttributeMap.RENDERER, null);
			if (renderer == null) {
				renderer = new ProMGroupShapeRenderer();
			}
		}
		return renderer;
	}

	@Override
	public Point2D getPerimeterPoint(EdgeView edge, Point2D source, Point2D p) {
		if (p == null) {
			p = getCenterPoint(this);
		}
		return shape.getPerimeterPoint(this.getBounds(), source, p);
	}

	public DirectedGraphNode getNode() {
		return node;
	}

	public ViewSpecificAttributeMap getViewSpecificAttributeMap() {
		return viewSpecificAttributes;
	}

	public void cleanUp() {
		viewSpecificAttributes.clearViewSpecific(node);
		setCell(null);
		node = null;
		shape = null;
		if (renderer != null) {
			getRenderer().cleanUp();
			renderer = null;
		}
	}

	public boolean isPIP() {
		return isPIP;
	}

	@Override
	public Rectangle2D getBounds() {
		Dimension d = (Dimension) viewSpecificAttributes.get(node, AttributeMap.SIZE);
		Rectangle2D b = super.getBounds();
		if (d != null && (d.getWidth() != b.getWidth() || d.getHeight() != b.getHeight())) {
			return new Rectangle2D.Double(b.getX(), b.getY(), d.getWidth(), d.getHeight());
		} else {
			return b;
		}
	}

	/**
	 * Sets the bounds of this <code>view</code>. Calls translateView and
	 * scaleView.
	 * 
	 * @param bounds
	 *            the new bounds for this cell view
	 */
	@Override
	public void setBounds(Rectangle2D bounds) {
		super.setBounds(bounds);
		viewSpecificAttributes.putViewSpecific(node, AttributeMap.SIZE, new Dimension((int) bounds.getWidth(),
				(int) bounds.getHeight()));
	}

}
