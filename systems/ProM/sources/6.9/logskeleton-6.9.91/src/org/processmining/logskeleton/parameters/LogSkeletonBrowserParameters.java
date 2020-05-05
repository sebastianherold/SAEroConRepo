package org.processmining.logskeleton.parameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogSkeletonBrowserParameters {

	private Set<String> activities;
	private List<LogSkeletonBrowser> visualizers;
	private boolean useHyperArcs;
	private boolean useFalseConstraints;
	private boolean useEdgeColors;
	private boolean useEquivalenceClass;
	private boolean useNeighbors;
	private boolean useHeadTailLabels;
	private int precedenceThreshold;
	private int responseThreshold;
	private int notCoExistenceThreshold;

	public LogSkeletonBrowserParameters() {
		activities = new HashSet<String>();
		visualizers = new ArrayList<LogSkeletonBrowser>();
		/* 
		 * By default, do not use hyper arcs as finding the hyper arcs may take considerable time. 
		 */
		setUseHyperArcs(false);
		setUseFalseConstraints(true);
		setUseEdgeColors(true);
		setUseEquivalenceClass(true);
		setUseNeighbors(true);
		setUseHeadTailLabels(true);
		setPrecedenceThreshold(100);
		setResponseThreshold(100);
		setNotCoExistenceThreshold(100);
	}
	
	public Set<String> getActivities() {
		return activities;
	}

	public List<LogSkeletonBrowser> getVisualizers() {
		return visualizers;
	}

	public boolean isUseHyperArcs() {
		return useHyperArcs;
	}

	public void setUseHyperArcs(boolean useHyperArcs) {
		this.useHyperArcs = useHyperArcs;
	}

	public boolean isUseFalseConstraints() {
		return useFalseConstraints;
	}

	public void setUseFalseConstraints(boolean useFalseConstraints) {
		this.useFalseConstraints = useFalseConstraints;
	}

	public boolean isUseNeighbors() {
		return useNeighbors;
	}

	public void setUseNeighbors(boolean useNeighbors) {
		this.useNeighbors = useNeighbors;
	}

	public boolean isUseEdgeColors() {
		return useEdgeColors;
	}

	public void setUseEdgeColors(boolean useEdgeColors) {
		this.useEdgeColors = useEdgeColors;
	}

	public boolean isUseEquivalenceClass() {
		return useEquivalenceClass;
	}

	public void setUseEquivalenceClass(boolean useEquivalenceClass) {
		this.useEquivalenceClass = useEquivalenceClass;
	}

	public int getPrecedenceThreshold() {
		return precedenceThreshold;
	}

	public void setPrecedenceThreshold(int threshold) {
		this.precedenceThreshold = threshold;
	}

	public int getResponseThreshold() {
		return responseThreshold;
	}

	public void setResponseThreshold(int threshold) {
		this.responseThreshold = threshold;
	}

	public int getNotCoExistenceThreshold() {
		return notCoExistenceThreshold;
	}

	public void setNotCoExistenceThreshold(int threshold) {
		this.notCoExistenceThreshold = threshold;
	}

	public boolean isUseHeadTailLabels() {
		return useHeadTailLabels;
	}

	public void setUseHeadTailLabels(boolean useHeadTailLabels) {
		this.useHeadTailLabels = useHeadTailLabels;
	}
}
