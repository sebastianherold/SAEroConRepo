package org.processmining.models.jgraph;

import java.util.Collection;
import java.util.Map;

import javax.swing.SwingConstants;

import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionID;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ExportInteractionPanel;
import org.processmining.framework.util.ui.scalableview.interaction.PIPInteractionPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ZoomInteractionPanel;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

public class ProMJGraphVisualizer {

	protected ProMJGraphVisualizer() {
	};

	private static ProMJGraphVisualizer instance = null;

	public static ProMJGraphVisualizer instance() {
		if (instance == null) {
			instance = new ProMJGraphVisualizer();
		}
		return instance;
	}

	protected GraphLayoutConnection findConnection(PluginContext context, DirectedGraph<?, ?> graph) {
		return findConnection(context.getConnectionManager(), graph);
	}

	protected GraphLayoutConnection findConnection(ConnectionManager manager, DirectedGraph<?, ?> graph) {
		Collection<ConnectionID> cids = manager.getConnectionIDs();
		for (ConnectionID id : cids) {
			Connection c;
			try {
				c = manager.getConnection(id);
			} catch (ConnectionCannotBeObtained e) {
				continue;
			}
			if (c != null && !c.isRemoved() && c instanceof GraphLayoutConnection
					&& c.getObjectWithRole(GraphLayoutConnection.GRAPH) == graph) {
				return (GraphLayoutConnection) c;
			}
		}
		return null;
	}

	public ProMJGraphPanel visualizeGraphWithoutRememberingLayout(DirectedGraph<?, ?> graph) {
		return visualizeGraph(new GraphLayoutConnection(graph), null, graph, new ViewSpecificAttributeMap());
	}

	public ProMJGraphPanel visualizeGraphWithoutRememberingLayout(DirectedGraph<?, ?> graph,
			ViewSpecificAttributeMap map) {
		return visualizeGraph(new GraphLayoutConnection(graph), null, graph, map);
	}

	public ProMJGraphPanel visualizeGraph(PluginContext context, DirectedGraph<?, ?> graph) {
		return visualizeGraph(findConnection(context, graph), context, graph, new ViewSpecificAttributeMap());
	}

	public ProMJGraphPanel visualizeGraph(PluginContext context, DirectedGraph<?, ?> graph, ViewSpecificAttributeMap map) {
		return visualizeGraph(findConnection(context, graph), context, graph, map);
	}

	private ProMJGraphPanel visualizeGraph(GraphLayoutConnection layoutConnection, PluginContext context,
			DirectedGraph<?, ?> graph, ViewSpecificAttributeMap map) {
		boolean newConnection = false;
		if (layoutConnection == null) {
			layoutConnection = createLayoutConnection(graph);
			newConnection = true;
		}

		if (!layoutConnection.isLayedOut()) {
			// shown for the first time.
			layoutConnection.expandAll();
		}
		//		graph.signalViews();

		ProMGraphModel model = new ProMGraphModel(graph);
		ProMJGraph jgraph;
		/*
		 * Make sure that only a single ProMJGraph is created at every time.
		 * The underlying JGrpah code cannot handle creating multiple creations at the same time.
		 */
		synchronized (instance) {
			jgraph = new ProMJGraph(model, map, layoutConnection);
		}

		JGraphLayout layout = getLayout(map.get(graph, AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

		if (!layoutConnection.isLayedOut()) {

			JGraphFacade facade = new JGraphFacade(jgraph);

			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			if (layout instanceof JGraphHierarchicalLayout) {
				facade.run((JGraphHierarchicalLayout) layout, true);
			} else {
				facade.run(layout, true);
			}

			Map<?, ?> nested = facade.createNestedMap(true, true);

			jgraph.getGraphLayoutCache().edit(nested);
//			jgraph.repositionToOrigin();
			layoutConnection.setLayedOut(true);

		}

		jgraph.setUpdateLayout(layout);

		ProMJGraphPanel panel = new ProMJGraphPanel(jgraph);

		panel.addViewInteractionPanel(new PIPInteractionPanel(panel), SwingConstants.NORTH);
		panel.addViewInteractionPanel(new ZoomInteractionPanel(panel, ScalableViewPanel.MAX_ZOOM), SwingConstants.WEST);
		panel.addViewInteractionPanel(new ExportInteractionPanel(panel), SwingConstants.SOUTH);

		layoutConnection.updated();

		if (newConnection) {
			context.getConnectionManager().addConnection(layoutConnection);
		}

		return panel;

	}

	private GraphLayoutConnection createLayoutConnection(DirectedGraph<?, ?> graph) {
		GraphLayoutConnection c = new GraphLayoutConnection(graph);
		return c;
	}

	protected JGraphLayout getLayout(int orientation) {
		JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
		layout.setDeterministic(true);
		layout.setCompactLayout(true);
		layout.setFineTuning(true);
		layout.setParallelEdgeSpacing(15);
		layout.setFixRoots(false);
		
	
		layout.setOrientation(orientation);

		return layout;
	}

}
