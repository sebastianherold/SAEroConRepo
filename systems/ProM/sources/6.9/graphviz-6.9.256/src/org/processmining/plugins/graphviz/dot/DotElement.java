package org.processmining.plugins.graphviz.dot;

import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;

import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;

public interface DotElement extends MouseListener {

	public String getLabel();

	public void setLabel(String label);

	public void setOption(String key, String value);

	/**
	 * 
	 * @param key
	 * @return the value of the option if it was set, otherwise null.
	 */
	public String getOption(String key);

	/**
	 * 
	 * @return the set of options that is set (keys)
	 */
	public Set<String> getOptionKeySet();

	public String getId();

	//mouse listeners, gui stuff

	/**
	 * Please note that in the current implementation, only a click on a drawn
	 * pixel of an element is registered as a click (due to Bezier curves for
	 * edges and irregular shapes for nodes). Thus, only if a user clicks on the
	 * border or label of a node, the click is registered. The same holds for
	 * mouseEnter and mouseExit events: these are based on drawn pixels.
	 * 
	 * To avoid this problem, give each node a fill using
	 * node.setOption("fillcolor", "#FFFFFF") and node.setOption("style",
	 * "filled").
	 * 
	 * @param l
	 */
	public void addMouseListener(MouseListener l);

	//selection listeners

	/**
	 * Sets whether this node can be selected.
	 * 
	 * Please note that in the current implementation, only a click on a drawn
	 * pixel of an element is registered as a click (due to Bezier curves for
	 * edges and irregular shapes for nodes). Thus, only if a user clicks on the
	 * border or label of a node, the click is registered.
	 * 
	 * To avoid this problem, give each node a fill using
	 * node.setOption("fillcolor", "#FFFFFF") and node.setOption("style",
	 * "filled").
	 * 
	 * @param selectable
	 */
	public void setSelectable(boolean selectable);

	/**
	 * Please note that in the current implementation, only a click on a drawn
	 * pixel of an element is registered as a click (due to Bezier curves for
	 * edges and irregular shapes for nodes). Thus, only if a user clicks on the
	 * border or label of a node, the click is registered.
	 * 
	 * To avoid this problem, give each node a fill using
	 * node.setOption("fillcolor", "#FFFFFF") and node.setOption("style",
	 * "filled").
	 * 
	 * @return whether the element is selectable.
	 */
	public boolean isSelectable();

	/**
	 * Add a selection/deselection listener. Side-effect: enables selection of
	 * the element. Thread-safe.
	 * 
	 * Please note that in the current implementation, only a click on a drawn
	 * pixel of an element is registered as a click (due to Bezier curves for
	 * edges and irregular shapes for nodes). Thus, only if a user clicks on the
	 * border or label of a node, the click is registered.
	 * 
	 * To avoid this problem, give each node a fill using
	 * node.setOption("fillcolor", "#FFFFFF") and node.setOption("style",
	 * "filled").
	 * 
	 * @param listener
	 */
	public void addSelectionListener(DotElementSelectionListener listener);

	public List<DotElementSelectionListener> getSelectionListeners();
}
