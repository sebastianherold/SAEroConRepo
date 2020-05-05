package org.processmining.contexts.util.jgraph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.tree.MutableTreeNode;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphConstants;

public class PortLabelCell extends DefaultGraphCell {

	public PortLabelCell(Object o, Point2D topLeft) {
		super(o);
		if (topLeft == null) {
			topLeft = new Point2D.Double(20, 20);
		}
		GraphConstants.setBounds(getAttributes(), new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), 10, 10));

	}

	public PortLabelCell(Object o, AttributeMap map, MutableTreeNode[] nodes) {
		super(o, map, nodes);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3154130726739617209L;

}