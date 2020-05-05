package org.processmining.contexts.uitopia.packagemanager;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.deckfour.uitopia.ui.util.ImageLoader;

public class PMPackageListCellRenderer extends JComponent implements ListCellRenderer {

	private static final long serialVersionUID = 413965516084559714L;

	private static final Color COLOR_PASSIVE_TOP = new Color(160, 160, 160);
	private static final Color COLOR_PASSIVE_BOTTOM = new Color(150, 150, 150);
	private static final Color COLOR_PASSIVE_TEXT = new Color(30, 30, 30);
	private static final Color COLOR_ACTIVE_TOP_G = new Color(80, 160, 80);
	private static final Color COLOR_ACTIVE_BOTTOM_G = new Color(40, 120, 40);
	private static final Color COLOR_ACTIVE_TOP_Y = new Color(160, 160, 80);
	private static final Color COLOR_ACTIVE_BOTTOM_Y = new Color(120, 120, 40);
	private static final Color COLOR_ACTIVE_TOP_R = new Color(160, 80, 80);
	private static final Color COLOR_ACTIVE_BOTTOM_R = new Color(120, 40, 40);
	private static final Color COLOR_ACTIVE_TEXT = new Color(0, 0, 0);
	private static final Color COLOR_DEAD_TOP = new Color(80, 80, 80);
	private static final Color COLOR_DEAD_BOTTOM = new Color(40, 40, 40);
	private static final Color COLOR_DEAD_TEXT = new Color(160, 160, 160);
	private static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 0);

	private static final Image favoriteIcon = ImageLoader.load("favorite_30x30.png");

	private static final int HEIGHT = 60;

	private PMPackage pack;
	private boolean selected;

	private boolean paintArrow = true;

	public PMPackageListCellRenderer() {
		this(true);
	}

	public PMPackageListCellRenderer(boolean paintArrow) {
		this.paintArrow = paintArrow;
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder());
		setMinimumSize(new Dimension(100, HEIGHT));
		setMaximumSize(new Dimension(1000, HEIGHT));
		setPreferredSize(new Dimension(250, HEIGHT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {
		int height = getHeight();
		int width = getWidth();
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// paint background
		if (selected) {
			switch (pack.getStatus()) {
				case TOUNINSTALL :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_ACTIVE_TOP_G, 0, height, COLOR_ACTIVE_BOTTOM_G));
					break;
				case TOUPDATE :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_ACTIVE_TOP_Y, 0, height, COLOR_ACTIVE_BOTTOM_Y));
					break;
				case TOINSTALL :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_ACTIVE_TOP_R, 0, height, COLOR_ACTIVE_BOTTOM_R));
					break;
				case DEAD :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_DEAD_TOP, 0, height, COLOR_DEAD_BOTTOM));
					break;
			}
		} else {
			switch (pack.getStatus()) {
				case DEAD :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_DEAD_TOP, 0, height, COLOR_DEAD_BOTTOM));
					break;
				default :
					g2d.setPaint(new GradientPaint(0, 0, COLOR_PASSIVE_TOP, 0, height, COLOR_PASSIVE_BOTTOM));
					break;
			}
		}
		g2d.fillRect(0, 0, width, height);
		// paint icon
		//		Image icon = pack.getPreview(10, 10);
		//		int iconX = 10;
		//		int iconY = (height - icon.getHeight(null)) / 2;
		//		g2d.drawImage(icon, iconX, iconY, null);
		// paint text
		if (selected) {
			switch (pack.getStatus()) {
				case DEAD :
					g2d.setPaint(new GradientPaint(width - 20, 0, COLOR_DEAD_TEXT, width, 0, COLOR_TRANSPARENT));
					break;
				default :
					g2d.setPaint(new GradientPaint(width - 20, 0, COLOR_ACTIVE_TEXT, width, 0, COLOR_TRANSPARENT));
					break;
			}
		} else {
			switch (pack.getStatus()) {
				case DEAD :
					g2d.setPaint(new GradientPaint(width - 20, 0, COLOR_DEAD_TEXT, width, 0, COLOR_TRANSPARENT));
					break;
				default :
					g2d.setPaint(new GradientPaint(width - 20, 0, COLOR_PASSIVE_TEXT, width, 0, COLOR_TRANSPARENT));
					break;
			}
		}
		g2d.setFont(g2d.getFont().deriveFont(13f));
		int textX = 20; // + icon.getWidth(null);
		int textY = HEIGHT / 2;
		g2d.drawString(pack.getPackageName(), textX, textY - 6);
		g2d.setFont(g2d.getFont().deriveFont(10f));
		g2d.drawString(pack.getAuthorName(), textX, textY + 10);
		g2d.drawString(pack.getVersion(), textX, textY + 22);
		if (pack.isFavorite()) {
			int fwidth = favoriteIcon.getWidth(null);
			int fheight = favoriteIcon.getHeight(null);
			if (!selected) {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
			} else {
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
			}
			int posX = width - fwidth - 20;
			if (paintArrow) {
				posX -= 20;
			}
			g2d.drawImage(favoriteIcon, posX, (height - fheight) / 2, null);
		}
		if (selected && paintArrow) {
			g2d.setColor(new Color(10, 10, 10, 200));
			int yMid = height / 2;
			int x[] = { width - 30, width - 10, width - 30, width - 28 };
			int y[] = { yMid - 10, yMid, yMid + 10, yMid };
			g2d.fillPolygon(x, y, 4);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing
	 * .JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		pack = (PMPackage) value;
		selected = isSelected;
		return this;
	}
}
