package org.processmining.models.jgraph.factory;

import java.util.ArrayList;
import java.util.List;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.Edge;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.Port;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;
import org.processmining.framework.util.Cast;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.elements.ProMGraphPort;
import org.processmining.models.jgraph.views.JGraphEdgeView;
import org.processmining.models.jgraph.views.JGraphPortView;
import org.processmining.models.jgraph.views.JGraphShapeView;

public class ProMCellViewFactory extends DefaultCellViewFactory {

	private static final long serialVersionUID = -2424217390990685801L;
	private final boolean isPIP;
	private final ViewSpecificAttributeMap viewSpecificAttributes;

	public ProMCellViewFactory(boolean isPIP, ViewSpecificAttributeMap viewSpecificAttributes) {
		this.isPIP = isPIP;
		this.viewSpecificAttributes = viewSpecificAttributes;
	}

	@Override
	protected VertexView createVertexView(Object v) {
		ProMGraphCell cell = (ProMGraphCell) v;
		JGraphShapeView view = new JGraphShapeView(cell, isPIP, viewSpecificAttributes);
		cell.setView(view);
		return view;
	}

	@Override
	@Deprecated
	protected EdgeView createEdgeView(Edge e) {
		return createEdgeView((Object) e);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected EdgeView createEdgeView(Object e) {
		ProMGraphEdge cell = Cast.<ProMGraphEdge>cast(e);

		List list = new ArrayList(cell.getInternalPoints());
		list.add(0, cell.getSource().getView());
		list.add(cell.getTarget().getView());
		GraphConstants.setPoints(cell.getAttributes(), list);

		JGraphEdgeView view = new JGraphEdgeView(cell, isPIP, viewSpecificAttributes);
		cell.setView(view);
		return view;
	}

	@Override
	@Deprecated
	protected PortView createPortView(Port e) {
		return createPortView((Object) e);
	}

	@Override
	protected PortView createPortView(Object e) {
		ProMGraphPort cell = Cast.<ProMGraphPort>cast(e);
		JGraphPortView view = new JGraphPortView(cell, isPIP, viewSpecificAttributes);
		cell.setView(view);
		cell.updateViewsFromMap();
		return view;
	}

}
