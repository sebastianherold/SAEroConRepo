package org.processmining.plugins.graphviz.visualisation.listeners;

public interface GraphChangedListener {
	
	public enum GraphChangedReason {
		graphDirectionChanged, nodeSeparationChanged 
	}
	
	public void graphChanged(GraphChangedReason reason, Object newState);
}
