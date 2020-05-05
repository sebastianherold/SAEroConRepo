package org.processmining.framework.util.ui.scalableview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class DashedBorder extends javax.swing.border.LineBorder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1357931293759243135L;

	public DashedBorder(Color color) {
		super(color);
	}

	public void paintBorder(java.awt.Component comp, Graphics g, int x1,
			int x2, int y1, int y2) {

		Stroke old = ((Graphics2D) g).getStroke();
		BasicStroke bs = new BasicStroke(5.0f, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_MITER, 10.0f, new float[] { 15.0f, 30.0f },
				2.0f);
		((Graphics2D) g).setStroke(bs);
		super.paintBorder(comp, g, x1, x2, y1, y2);
		((Graphics2D) g).setStroke(old);
	}
}