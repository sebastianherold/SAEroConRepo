package org.processmining.plugins.graphviz.visualisation.export;

import java.io.File;
import java.io.PrintWriter;

import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExporterDot extends Exporter {

	@Override
	public String getDescription() {
		return "dot (graph structure)";
	};
	
	protected String getExtension() {
		return "dot";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		if (panel instanceof DotPanel) {
			PrintWriter writer = new PrintWriter(file);
			writer.print(((DotPanel) panel).getDot());
			writer.close();
		}
	}

}