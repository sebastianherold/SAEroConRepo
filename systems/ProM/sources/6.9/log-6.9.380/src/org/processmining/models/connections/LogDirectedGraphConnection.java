package org.processmining.models.connections;

import java.util.Collection;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.annotations.ConnectionDoesntExistMessage;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;

@ConnectionDoesntExistMessage(message = "No mapping is known between the given XEventClasses and the nodes of the given graph.")
public class LogDirectedGraphConnection extends
		AbstractLogModelConnection<DirectedGraphNode, DirectedGraphEdge<DirectedGraphNode, DirectedGraphNode>> {

	public LogDirectedGraphConnection(XLog log, XEventClasses classes,
			DirectedGraph<DirectedGraphNode, DirectedGraphEdge<DirectedGraphNode, DirectedGraphNode>> graph,
			Collection<Pair<DirectedGraphNode, XEventClass>> relations) {
		super(log, classes, graph, graph.getNodes(), relations);
	}

	public LogDirectedGraphConnection(XLog log, XEventClasses classes,
			DirectedGraph<DirectedGraphNode, DirectedGraphEdge<DirectedGraphNode, DirectedGraphNode>> graph,
			Map<DirectedGraphNode, XEventClass> relations) {
		super(log, classes, graph, graph.getNodes(), relations);
	}

}
