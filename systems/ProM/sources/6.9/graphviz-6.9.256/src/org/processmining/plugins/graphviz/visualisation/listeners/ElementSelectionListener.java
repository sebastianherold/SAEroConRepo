package org.processmining.plugins.graphviz.visualisation.listeners;

@Deprecated
public interface ElementSelectionListener<E> {
	public void selected(E element);
	public void deselected(E element);
}
