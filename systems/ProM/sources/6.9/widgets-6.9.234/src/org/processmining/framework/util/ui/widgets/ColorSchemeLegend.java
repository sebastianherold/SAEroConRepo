package org.processmining.framework.util.ui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.MessageFormat;

import javax.swing.JComponent;

/**
 * Legend for a gradient-based color scheme.
 * 
 * @author F. Mannhardt
 *
 */
public class ColorSchemeLegend extends JComponent {

	private static final long serialVersionUID = 1L;

	private final Color[] colorPalette;
	protected final float bucketSize;

	private int textDistance = 1;
	private int tickHeight = 3;

	public ColorSchemeLegend(Color[] colorPalette) {
		super();
		this.colorPalette = colorPalette;
		this.bucketSize = 1.0f / colorPalette.length;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension size = getSize();
		Graphics2D g2d = (Graphics2D) g;
		g2d.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));

		double barWidth = size.getWidth() / colorPalette.length;
		double barHeight = getBarHeight(g2d, size);

		for (int i = 0; i < colorPalette.length; i++) {
			Color color = colorPalette[i];
			g2d.setColor(color);
			double currentX = i * barWidth;
			g2d.fill(new Rectangle2D.Double(currentX, 0, barWidth, barHeight));
			g2d.setColor(getForeground());
			if (i != 0) {
				g2d.setColor(Color.BLACK);
				g2d.draw(new Line2D.Double(currentX, 0, currentX, barHeight + getTickHeight()));
				drawBarDesc(g2d, barHeight, i, currentX);
			}
			drawBucketDesc(g2d, barWidth, barHeight, i);
		}
		g2d.setColor(Color.BLACK);
		drawBucketDesc(g2d, barWidth, barHeight, colorPalette.length);
		g2d.draw(new Rectangle2D.Double(0, 0, size.getWidth() - 1, getBarHeight(g2d, size)));
	}

	private void drawBarDesc(Graphics2D g2d, double barHeight, int i, double currentX) {
		String barDesc = getBarDesc(i);
		int descWidth = g2d.getFontMetrics().stringWidth(barDesc);
		g2d.drawString(barDesc, (int) Math.ceil(currentX - (descWidth / 2.0)),
				(int) Math.ceil(barHeight + getTextBaseline(g2d)));
	}

	private void drawBucketDesc(Graphics2D g2d, double barWidth, double barHeight, int i) {
		String bucketDesc = getBucketDesc(i);
		if (bucketDesc != null) {
			int descWidth = g2d.getFontMetrics().stringWidth(bucketDesc);
			g2d.drawString(bucketDesc, (int) Math.ceil((i * barWidth) - (descWidth / 2.0) - (barWidth / 2.0)),
					(int) Math.ceil(barHeight + getTextBaseline(g2d)));
		}
	}

	protected String getBarDesc(int bucket) {
		float bucketStart = bucket * bucketSize;
		return MessageFormat.format("{0,number,#.#}", bucketStart);
	}

	protected String getBucketDesc(int bucket) {
		return null;
	}

	private double getBarHeight(Graphics2D g2d, Dimension size) {
		return size.getHeight() - getTextBaseline(g2d);
	}

	private int getTextBaseline(Graphics2D g2d) {
		return g2d.getFontMetrics().getAscent() + textDistance;
	}

	public int getTextDistance() {
		return textDistance;
	}

	public void setTextDistance(int textDistance) {
		this.textDistance = textDistance;
	}

	public int getTickHeight() {
		return tickHeight;
	}

	public void setTickHeight(int tickHeight) {
		this.tickHeight = tickHeight;
	}
}