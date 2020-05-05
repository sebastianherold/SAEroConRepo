package org.processmining.plugins.graphviz.dot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Dot extends DotCluster {

	public enum GraphDirection {
		topDown("TD"), leftRight("LR"), bottomTop("BT"), rightLeft("RL");

		private final String name;

		private GraphDirection(String s) {
			name = s;
		}

		public String getName() {
			return name;
		}
	}

	private String stringValue = null;

	public Dot() {
		setOption("rankdir", "TD");
		setOption("compound", "true");
	}

	public String toString() {
		if (stringValue != null) {
			return stringValue;
		}

		StringBuilder result = new StringBuilder();
		result.append("digraph G {\n");

		appendOptions(result);

		contentToString(result);

		result.append("}");

		return result.toString();
	}

	public Dot(InputStream input) throws IOException {
		this.stringValue = IOUtils.toString(input, "UTF-8");
	}

	public void exportToFile(File file) throws IOException {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(toString());
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public GraphDirection getDirection() {
		String value = getOption("rankdir");
		for (GraphDirection dir : GraphDirection.values()) {
			if (dir.getName().equals(value)) {
				return dir;
			}
		}
		return GraphDirection.topDown;
	}

	public void setDirection(GraphDirection direction) {
		setOption("rankdir", direction.getName());
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public boolean isKeepOrderingOfChildren() {
		return "out".equals(getGraphOption("ordering"));
	}

	public void setKeepOrderingOfChildren(boolean keepOrderingOfChildren) {
		if (keepOrderingOfChildren) {
			setGraphOption("ordering", "out");
		} else {
			setGraphOption("ordering", ""); // graphviz default
		}
	}

}