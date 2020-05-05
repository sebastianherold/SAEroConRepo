package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Dimension;
import java.io.File;
import java.util.Properties;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExporterSVG extends Exporter {

	protected String getExtension() {
		return "svg";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {	
		double width = panel.getImage().getViewRect().getWidth();
		double height = panel.getImage().getViewRect().getHeight();
		
		Dimension dimension = new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
		VectorGraphics g = new SVGGraphics2D(file, dimension);
		Properties p = new Properties(SVGGraphics2D.getDefaultProperties());
		g.setProperties(p);
		g.startExport();
		panel.print(g);
		g.endExport();
	}

}
