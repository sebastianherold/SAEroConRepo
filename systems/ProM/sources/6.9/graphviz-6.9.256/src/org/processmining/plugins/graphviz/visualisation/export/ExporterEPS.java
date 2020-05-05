package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Dimension;
import java.io.File;
import java.util.Properties;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ps.EPSGraphics2D;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExporterEPS extends Exporter {

	protected String getExtension() {
		return "eps";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {

		double width = panel.getImage().getViewRect().getWidth();
		double height = panel.getImage().getViewRect().getHeight();

		Dimension dimension = new Dimension((int) Math.ceil(width), (int) Math.ceil(height));
		VectorGraphics g = new EPSGraphics2D(file, dimension);
		Properties p = new Properties(EPSGraphics2D.getDefaultProperties());
		p.setProperty(EPSGraphics2D.PAGE_SIZE, EPSGraphics2D.CUSTOM_PAGE_SIZE);
		p.setProperty(EPSGraphics2D.PAGE_MARGINS, "0, 0, 0, 0");
		p.put(EPSGraphics2D.CUSTOM_PAGE_SIZE, dimension.width + ", " + dimension.height);
		g.setProperties(p);
		g.startExport();
		panel.print(g);
		g.endExport();
	}

}