package org.processmining.models.jgraph.visualization;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jgraph.graph.BasicMarqueeHandler;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;

public class ProMJGraphPanel extends ScalableViewPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8937461038820086748L;

	public ProMJGraphPanel(final ProMJGraph graph) {
		super(graph);

		JLabel label = new JLabel("<html>&#8629;</html>");

		addButton(label, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				graph.repositionToOrigin();
				updated();
			}
		}, SwingConstants.NORTH_WEST);
	}

	@Override
	protected void initialize() {
		getGraph().setTolerance(4);

		getGraph().setMarqueeHandler(new BasicMarqueeHandler() {
			private boolean test(MouseEvent e) {
				return SwingUtilities.isRightMouseButton(e) && ((e.getModifiers() & InputEvent.ALT_MASK) == 0);

			}

			public boolean isForceMarqueeEvent(MouseEvent event) {
				if (test(event)) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (test(e)) {
					e.consume();
				} else {
					super.mouseReleased(e);
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (test(e)) {
					synchronized (getGraph().getProMGraph()) {
						// Check for selection.
						// If the cell that is being clicked is part of the
						// selection,
						// we use the current selection.
						// otherwise, we use a new selection
						Object cell = getGraph().getFirstCellForLocation(e.getX(), e.getY());

						Collection<DirectedGraphElement> sel;
						if (cell == null) {
							// Nothing selected
							getGraph().clearSelection();
							sel = new ArrayList<DirectedGraphElement>(0);
						} else if (getGraph().getSelectionModel().isCellSelected(cell)) {
							// the current selection contains cell
							// use that selection
							sel = getSelectedElements();
						} else {
							// the current selection does not contain cell.
							// reset the selection to [cell]
							sel = new ArrayList<DirectedGraphElement>(1);
							sel.add(getElementForLocation(e.getX(), e.getY()));
							getGraph().setSelectionCell(cell);
						}

					}
				} else {
					super.mousePressed(e);
				}
			}

		});
		super.initialize();
	}

	public ProMJGraph getGraph() {
		return (ProMJGraph) getComponent();
	}

	public Collection<DirectedGraphElement> getSelectedElements() {
		List<DirectedGraphElement> elements = new ArrayList<DirectedGraphElement>();
		for (Object o : getGraph().getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				elements.add(((ProMGraphCell) o).getNode());
			} else if (o instanceof ProMGraphEdge) {
				elements.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return elements;
	}

	public Collection<DirectedGraphNode> getSelectedNodes() {
		List<DirectedGraphNode> nodes = new ArrayList<DirectedGraphNode>();
		for (Object o : getGraph().getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				nodes.add(((ProMGraphCell) o).getNode());
			}
		}
		return nodes;
	}

	public Collection<DirectedGraphEdge<?, ?>> getSelectedEdges() {
		List<DirectedGraphEdge<?, ?>> edges = new ArrayList<DirectedGraphEdge<?, ?>>();
		for (Object o : getGraph().getSelectionCells()) {
			if (o instanceof ProMGraphEdge) {
				edges.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return edges;
	}

	public DirectedGraphElement getElementForLocation(double x, double y) {
		Object cell = getGraph().getFirstCellForLocation(x, y);
		if (cell instanceof ProMGraphCell) {
			return ((ProMGraphCell) cell).getNode();
		}
		if (cell instanceof ProMGraphEdge) {
			return ((ProMGraphEdge) cell).getEdge();
		}
		return null;
	}

}
