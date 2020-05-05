package org.processmining.plugins.graphviz.dot.plugins;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;

public abstract class DotImageExportPlugin {

	protected void export(Dot dot, File file, Exporter exporter) throws IOException {
		DotPanel panel = new DotPanel(dot);
		panel.setSize(new Dimension((int) panel.getSVG().getWidth(), (int) panel.getSVG().getHeight()));
		try {
			exporter.export(panel, file);
		} catch (Exception e) {
			if (e instanceof IOException) {
				throw (IOException) e;
			} else {
				throw new IOException("Cannot export Dot.", e);
			}
		}
	}

}
