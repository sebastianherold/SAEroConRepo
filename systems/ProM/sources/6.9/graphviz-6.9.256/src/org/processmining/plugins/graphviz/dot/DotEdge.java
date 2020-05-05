package org.processmining.plugins.graphviz.dot;

import java.util.Map;
import java.util.Map.Entry;

public class DotEdge extends AbstractDotElement {

	private DotNode source;
	private DotNode target;

	public DotEdge(DotNode source, DotNode target) {
		this.setSource(source);
		this.setTarget(target);
	}

	public DotEdge(DotNode source, DotNode target, String label, Map<String, String> optionsMap) {
		this.setSource(source);
		this.setTarget(target);
		this.setLabel(label);
		if (optionsMap != null) {
			for (Entry<String, String> e : optionsMap.entrySet()) {
				setOption(e.getKey(), e.getValue());
			}
		}
	}

	public DotNode getTarget() {
		return target;
	}

	public void setTarget(DotNode target) {
		this.target = target;
	}

	public DotNode getSource() {
		return source;
	}

	public void setSource(DotNode source) {
		this.source = source;
	}

	public String toString() {
		/**
		 * Dot does not support edges from/to clusters. I such edges are added,
		 * use an arbitrary node in the cluster as the target.
		 */
		DotNode localSource = source;
		DotNode localTarget = target;
		{
			if (localSource instanceof DotCluster && !((DotCluster) localSource).getNodes().isEmpty()) {
				localSource = ((DotCluster) localSource).getNodes().get(0);
			}
			if (localTarget instanceof DotCluster && !((DotCluster) localTarget).getNodes().isEmpty()) {
				localTarget = ((DotCluster) localTarget).getNodes().get(0);
			}
		}

		String result = "\"" + localSource.getId() + "\" -> \"" + localTarget.getId() + "\" [label=" + labelToString()
				+ " id=\"" + getId() + "\"";

		for (String key : getOptionKeySet()) {
			result += "," + key + "=" + escapeString(getOption(key));
		}
		
		/**
		 * If the edges goes to/from a cluster, we need to set the lhead/ltail.
		 */
		if (localSource != source) {
			result += ",ltail=" + escapeString(source.getId());
		}
		if (localTarget != target) {
			result += ",lhead=" + escapeString(target.getId());
		}

		return result + "];";
	}
}
