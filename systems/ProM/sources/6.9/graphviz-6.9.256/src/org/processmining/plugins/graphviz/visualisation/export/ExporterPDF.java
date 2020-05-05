package org.processmining.plugins.graphviz.visualisation.export;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Due to bugs in direct pdf export, first export to eps and then convert to
 * pdf.
 * 
 * @author sander
 *
 */

public class ExporterPDF extends Exporter {

	protected String getExtension() {
		return "pdf";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		float width = (float) panel.getImage().getViewRect().getWidth();
		float height = (float) panel.getImage().getViewRect().getHeight();

		Document document = new Document(new Rectangle(width, height), 0, 0, 0, 0);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
		document.open();
		PdfContentByte canvas = writer.getDirectContent();
		
		Graphics2D g = new PdfGraphics2D(canvas, width, height);
		panel.print(g);
		g.dispose();
		
		document.close();
	}

}
