package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Dimension;
import java.io.File;
import java.util.Properties;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExporterEMF extends Exporter {

	protected String getExtension() {
		return "emf";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {

		double width = panel.getImage().getViewRect().getWidth();
		double height = panel.getImage().getViewRect().getHeight();

		VectorGraphics g = new EMFGraphics2D(file, new Dimension((int) Math.ceil(width), (int) Math.ceil(height)));
		Properties p = new Properties();
		g.setProperties(p);
		g.startExport();
		panel.print(g);
		g.endExport();
	}

}