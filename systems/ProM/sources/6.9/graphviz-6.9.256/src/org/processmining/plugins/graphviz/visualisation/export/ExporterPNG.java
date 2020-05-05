package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

public class ExporterPNG extends Exporter {

	protected String getExtension() {
		return "png";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		BufferedImage bi = new BufferedImage(Math.round(panel.getImage().getWidth()), Math.round(panel.getImage().getHeight()),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		panel.print(g);
		ImageIO.write(bi, "PNG", file);
	}

}
