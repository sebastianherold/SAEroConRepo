package org.processmining.plugins.graphviz.visualisation.listeners;

import java.util.Set;

public interface SelectionChangedListener<E> {
	
	/**
	 * Called when the selection changes.
	 * @param selectedElements The new set of selected elements.
	 */
	public void selectionChanged(Set<E> selectedElements);
}
