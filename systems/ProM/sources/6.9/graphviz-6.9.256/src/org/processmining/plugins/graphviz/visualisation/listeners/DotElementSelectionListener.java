package org.processmining.plugins.graphviz.visualisation.listeners;

import org.processmining.plugins.graphviz.dot.DotElement;

import com.kitfox.svg.SVGDiagram;

public interface DotElementSelectionListener {
	public void selected(DotElement element, SVGDiagram image);
	public void deselected(DotElement element, SVGDiagram image);
}
