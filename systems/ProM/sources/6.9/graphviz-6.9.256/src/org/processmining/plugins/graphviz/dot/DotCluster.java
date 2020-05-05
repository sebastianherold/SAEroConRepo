package org.processmining.plugins.graphviz.dot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DotCluster extends DotNode {

	private final List<DotNode> nodes;
	private final List<DotEdge> edges;
	private final List<DotCluster> clusters;

	private final Map<String, String> graphOptionMap;
	private final Map<String, String> nodeOptionMap;
	private final Map<String, String> edgeOptionMap;

	protected DotCluster() {
		super("", null);
		nodes = new ArrayList<DotNode>();
		edges = new ArrayList<DotEdge>();
		clusters = new ArrayList<DotCluster>();
		graphOptionMap = new HashMap<>();
		nodeOptionMap = new HashMap<>();
		edgeOptionMap = new HashMap<>();
	}

	/**
	 * 
	 * @return An unmodifiable list of all nodes in the graph (not in
	 *         sub-graphs).
	 */
	public List<DotNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * 
	 * @return An unmodifiable list of all nodes in this graph and all it's
	 *         sub-graphs.
	 */
	public List<DotNode> getNodesRecursive() {
		List<DotNode> result = new LinkedList<DotNode>();
		result.addAll(nodes);
		result.addAll(clusters);

		for (DotCluster cluster : clusters) {
			result.addAll(cluster.getNodesRecursive());
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * Add a new dot node to the graph with the given label.
	 * 
	 * @param label
	 * @return the new dot node.
	 */
	public DotNode addNode(String label) {
		return addNode(label, null);
	}

	/**
	 * Add a new dot node to the graph.
	 * 
	 * @param label
	 * @param options
	 *            A map of node options, which will not be copied.
	 * @return the new dot node.
	 */
	public DotNode addNode(String label, Map<String, String> options) {
		DotNode result = new DotNode(label, options);
		addNode(result);
		return result;
	}

	/**
	 * Add an existing dot node to the graph.
	 * 
	 * @param node
	 */
	public void addNode(DotNode node) {
		nodes.add(node);
	}

	/**
	 * Insert a new dot node to the graph.
	 * 
	 * @param index
	 *            The index at which the node is to be inserted.
	 * @param label
	 * @return the new dot node.
	 */
	public DotNode insertNode(int index, String label) {
		return insertNode(index, label, null);
	}

	/**
	 * Insert a new dot node to the graph.
	 * 
	 * @param index
	 *            The index at which the node is to be inserted.
	 * @param label
	 * @param options
	 *            A map of node options, which will not be copied.
	 * @return the new dot node.
	 */
	public DotNode insertNode(int index, String label, Map<String, String> options) {
		DotNode result = new DotNode(label, options);
		insertNode(index, result);
		return result;
	}

	/**
	 * Insert an existing node into the graph.
	 * 
	 * @param index
	 *            The index at which the node is to be inserted.
	 * @param node
	 */
	public void insertNode(int index, DotNode node) {
		nodes.add(index, node);
	}

	/**
	 * Remove all equivalent dot nodes from the graph.
	 * 
	 * @param node
	 */
	public void removeNode(DotNode node) {
		Iterator<DotNode> it = nodes.iterator();
		while (it.hasNext()) {
			if (node.equals(it.next())) {
				it.remove();
			}
		}
	}

	/**
	 * Set a default node option. It will be a default for all nodes in this
	 * graph.
	 * 
	 * @param option
	 * @param value
	 */
	public void setNodeOption(String option, String value) {
		nodeOptionMap.put(option, value);
	}

	/**
	 * Get a default node option.
	 * 
	 * @param option
	 * @return The value of that option, or null if it is not set.
	 */
	public String getNodeOption(String option) {
		if (nodeOptionMap.containsKey(option)) {
			return nodeOptionMap.get(option);
		}
		return null;
	}

	/**
	 * 
	 * @return An unmodifiable list of all default node options for this graph.
	 */
	public Set<String> getNodeOptionKeySet() {
		return Collections.unmodifiableSet(nodeOptionMap.keySet());
	}

	/**
	 * 
	 * @return An unmodifiable list of all edges in this graph (not in
	 *         sub-graphs).
	 */
	public List<DotEdge> getEdges() {
		return Collections.unmodifiableList(edges);
	}

	/**
	 * 
	 * @return An unmodifiable list of all edges in this graph and in all its
	 *         sub-graphs.
	 */
	public List<DotEdge> getEdgesRecursive() {
		List<DotEdge> result = new LinkedList<DotEdge>();
		result.addAll(edges);

		for (DotCluster cluster : clusters) {
			result.addAll(cluster.getEdgesRecursive());
		}

		return Collections.unmodifiableList(result);
	}

	/**
	 * Add an edge that connects source to target, with a default label.
	 * 
	 * @param source
	 * @param target
	 * @return
	 */
	public DotEdge addEdge(DotNode source, DotNode target) {
		return addEdge(source, target, "");
	}

	/**
	 * Add an edge that connects source to target, having a label, without
	 * further options set.
	 * 
	 * @param source
	 * @param target
	 * @param label
	 * @return
	 */
	public DotEdge addEdge(DotNode source, DotNode target, String label) {
		return addEdge(source, target, label, null);
	}

	/**
	 * Add an edge that connects source to target, having a label and using an
	 * options map. This options map is not copied.
	 * 
	 * @param source
	 * @param target
	 * @param label
	 * @param optionsMap
	 * @return
	 */
	public DotEdge addEdge(DotNode source, DotNode target, String label, Map<String, String> optionsMap) {
		DotEdge result = new DotEdge(source, target, label, optionsMap);
		addEdge(result);
		return result;
	}

	/**
	 * Add a previously defined dot edge.
	 * 
	 * @param edge
	 */
	public void addEdge(DotEdge edge) {
		edges.add(edge);
	}

	/**
	 * Remove all equivalent edges from the graph.
	 * 
	 * @param edge
	 */
	public void removeEdge(DotEdge edge) {
		Iterator<DotEdge> it = edges.iterator();
		while (it.hasNext()) {
			if (edge == it.next()) {
				it.remove();
			}
		}
	}

	/**
	 * 
	 * @param source
	 * @param target
	 * @return The first edge from source to target in the graph.
	 */
	public DotEdge getFirstEdge(DotNode source, DotNode target) {
		for (DotEdge edge : edges) {
			if (edge.getSource() == source && edge.getTarget() == target) {
				return edge;
			}
		}
		return null;
	}

	/**
	 * Sets a default edge option. This will be the default for all edges in
	 * this graph.
	 * 
	 * @param option
	 * @param value
	 */
	public void setEdgeOption(String option, String value) {
		edgeOptionMap.put(option, value);
	}

	/**
	 * 
	 * @param option
	 * @return The value of the option, or null if it does not exist.
	 */
	public String getEdgeOption(String option) {
		if (edgeOptionMap.containsKey(option)) {
			return edgeOptionMap.get(option);
		}
		return null;
	}

	/**
	 * 
	 * @return An unmodifiable set of all default edge options set for this
	 *         graph.
	 */
	public Set<String> getEdgeOptions() {
		return Collections.unmodifiableSet(edgeOptionMap.keySet());
	}

//	@Deprecated
//	public Set<String> getEdgeOptionKeySet() {
//		return getEdgeOptions();
//	}

	/**
	 * 
	 * @return An unmodifiable list of clusters in this graph (not in
	 *         sub-graphs).
	 */
	public List<DotCluster> getClusters() {
		return Collections.unmodifiableList(clusters);
	}

	/**
	 * Add a new cluster to the graph.
	 * 
	 * @return
	 */
	public DotCluster addCluster() {
		DotCluster cluster = new DotCluster();
		clusters.add(cluster);
		return cluster;
	}

	/**
	 * Removes all equivalent clusters from the graph (not from sub-graphs).
	 * 
	 * @param cluster
	 */
	public void removeCluster(DotCluster cluster) {
		Iterator<DotCluster> it = clusters.iterator();
		while (it.hasNext()) {
			if (cluster.equals(it.next())) {
				it.remove();
			}
		}
	}

	/**
	 * Sets a default option for sub-graphs of this graph.
	 * 
	 * @param option
	 * @param value
	 */
	public void setGraphOption(String option, String value) {
		graphOptionMap.put(option, value);
	}

	/**
	 * Gets a default option for sub-graphs of this graph.
	 * 
	 * @param option
	 * @return the value of the option, or null if it does not exist.
	 */
	public String getGraphOption(String option) {
		if (graphOptionMap.containsKey(option)) {
			return graphOptionMap.get(option);
		}
		return null;
	}

	/**
	 * 
	 * @return An unmodifiable set of graph options that are set on this graph.
	 */
	public Set<String> getGraphOptions() {
		return Collections.unmodifiableSet(graphOptionMap.keySet());
	}

//	@Deprecated
//	public Set<String> getGraphOptionKeySet() {
//		return Collections.unmodifiableSet(graphOptionMap.keySet());
//	}

	/**
	 * Get a string representation of this graph in the Dot-language.
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("subgraph \"" + getId() + "\"{\n");

		result.append("id=\"" + getId() + "\";");
		result.append("label=" + labelToString() + ";");

		appendOptions(result);

		contentToString(result);

		result.append("}");

		return result.toString();
	}

	protected void appendOptions(StringBuilder result) {
		for (String key : getOptionKeySet()) {
			/*
			 * HV, May 11, 2017
			 * Graph label may contain HTML code. Deal with that.
			 */
			if (key.equals("label")) {
				String label = getOption(key);
				if (label.startsWith("<") && label.endsWith(">")) {
					// Label contains HTML code, copy as-is
					result.append(key + "=" + label + ";\n");
					continue;
				}
			} 
			result.append(key + "=\"" + getOption(key) + "\";\n");
		}
		appendSpecialOptions(result, "graph", graphOptionMap);
		appendSpecialOptions(result, "node", nodeOptionMap);
		appendSpecialOptions(result, "edge", edgeOptionMap);
	}

	protected void appendSpecialOptions(StringBuilder result, String type, Map<String, String> optionMap) {
		Iterator<String> graphOptionIter = optionMap.keySet().iterator();
		if (graphOptionIter.hasNext()) {
			result.append(type);
			result.append("[");
			while (graphOptionIter.hasNext()) {
				String key = graphOptionIter.next();
				result.append(key + "=" + escapeString(optionMap.get(key)));
				if (graphOptionIter.hasNext()) {
					result.append(',');
				}
			}
			result.append("];\n");
		}
	}

	protected void contentToString(StringBuilder result) {
		for (DotNode node : nodes) {
			result.append(node);
			result.append("\n");
		}

		for (DotEdge edge : edges) {
			result.append(edge);
			result.append("\n");
		}

		for (DotCluster cluster : clusters) {
			result.append(cluster);
			result.append("\n");
		}
	}

	@Override
	public String getId() {
		return "cluster_" + super.getId();
	}

}
