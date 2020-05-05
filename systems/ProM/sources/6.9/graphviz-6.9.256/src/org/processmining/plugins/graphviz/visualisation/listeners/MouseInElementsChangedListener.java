package org.processmining.plugins.graphviz.visualisation.listeners;

import java.util.Set;

public interface MouseInElementsChangedListener<E> {
	/**
	 * Called when the selection changes.
	 * @param selectedElements The new set of selected elements.
	 */
	public void mouseInElementsChanged(Set<E> mouseInElements);
}
