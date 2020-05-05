package org.processmining.models.connections;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;

public abstract class AbstractLogModelConnection<N extends DirectedGraphNode, E extends DirectedGraphEdge<? extends N, ? extends N>>
		extends AbstractStrongReferencingConnection {

	public final static String LOG = "Log";
	public final static String MODEL = "Model";
	public final static String CLASSES = "XEventClasses";
	protected final Map<N, Set<XEventClass>> node2activity = new WeakHashMap<N, Set<XEventClass>>();
	private int minNodePerActivity = Integer.MAX_VALUE;
	private int maxNodePerActivity = Integer.MIN_VALUE;
	private int minActivityPerNode = Integer.MAX_VALUE;
	private int maxActivityPerNode = Integer.MIN_VALUE;
	private final Collection<WeakReference<N>> mappableNodes;
	
	private AbstractLogModelConnection(XLog log, XEventClasses classes, DirectedGraph<N, E> graph,
			Collection<? extends N> mappableNodes) {
		super(graph.getLabel() + " generated from " + XConceptExtension.instance().extractName(log));
		this.mappableNodes = new HashSet<WeakReference<N>>();
		for (N n : mappableNodes) {
			this.mappableNodes.add(new WeakReference<N>(n));
		}
		put(LOG, log);
		put(MODEL, graph);
		putStrong(CLASSES, classes);
	}

	protected AbstractLogModelConnection(XLog log, XEventClasses classes, DirectedGraph<N, E> graph,
			Collection<? extends N> mappableNodes,
			Collection<? extends Pair<? extends N, ? extends XEventClass>> relations) {
		this(log, classes, graph, mappableNodes);
		for (Pair<? extends N, ? extends XEventClass> pair : relations) {
			if (!node2activity.containsKey(pair.getFirst())) {
				node2activity.put(pair.getFirst(), new HashSet<XEventClass>());
			}
			node2activity.get(pair.getFirst()).add(pair.getSecond());
		}
		initialize(classes, graph);
	}

	protected AbstractLogModelConnection(XLog log, XEventClasses classes, DirectedGraph<N, E> graph,
			Collection<? extends N> mappableNodes, Map<? extends N, ? extends XEventClass> relations) {
		this(log, classes, graph, mappableNodes);
		for (Map.Entry<? extends N, ? extends XEventClass> pair : relations.entrySet()) {
			if (!node2activity.containsKey(pair.getKey())) {
				node2activity.put(pair.getKey(), new HashSet<XEventClass>());
			}
			node2activity.get(pair.getKey()).add(pair.getValue());

		}
		initialize(classes, graph);
	}

	private void initialize(XEventClasses classes, DirectedGraph<N, E> graph) {
		for (XEventClass clazz : classes.getClasses()) {
			Set<N> nodes = getNodesFor(clazz);
			minNodePerActivity = Math.min(minNodePerActivity, nodes.size());
			maxNodePerActivity = Math.max(maxNodePerActivity, nodes.size());
		}
		for (WeakReference<N> node : mappableNodes) {
			Set<XEventClass> events = getActivitiesFor(node.get());
			minActivityPerNode = Math.min(minActivityPerNode, events.size());
			maxActivityPerNode = Math.max(maxActivityPerNode, events.size());
		}
	}

	public Set<XEventClass> getActivitiesFor(N node) {
		Set<XEventClass> s = node2activity.get(node);
		return s == null ? Collections.<XEventClass>emptySet() : s;
	}

	public Set<N> getNodesFor(XEventClass clazz) {
		Set<N> s = new HashSet<N>();
		for (Map.Entry<N, Set<XEventClass>> entry : node2activity.entrySet()) {
			if (entry.getValue().contains(clazz)) {
				s.add(entry.getKey());
			}
		}
		return s;
	}

	public boolean isInjectionFromNodeToActivity() {
		return maxNodePerActivity <= 1;
	}

	public boolean isInjectionFromActivityToNode() {
		return maxActivityPerNode <= 1;
	}

	public boolean isSurjectionFromNodeToActivity() {
		return minNodePerActivity >= 1;
	}

	public boolean isSurjectionFromActivityToNode() {
		return minActivityPerNode >= 1;
	}

	public boolean isBijectionFromNodeToActivity() {
		return isInjectionFromNodeToActivity() && isSurjectionFromNodeToActivity();
	}

	public boolean isBijectionFromActivityToNode() {
		return isInjectionFromActivityToNode() && isSurjectionFromActivityToNode();
	}

	public Collection<N> getMappableNodes() {
		HashSet<N> set = new HashSet<N>();
		for (WeakReference<N> n : mappableNodes) {
			set.add(n.get());
		}
		return Collections.unmodifiableCollection(set);
	}

	public XEventClasses getEventClasses() {
		return (XEventClasses) get(CLASSES);
	}
}
