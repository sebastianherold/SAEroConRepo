package org.processmining.framework.util.ui.scalableview.interaction;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;

public class PIPInteractionPanel extends JPanel implements MouseListener, MouseMotionListener, ViewInteractionPanel {

	public static final int PIPSIZE = 250;
	public static final Stroke DEFAULTSTROKE = new BasicStroke(2);

	private static final long serialVersionUID = 5563202305263696868L;

	// new FlowLayout(FlowLayout.LEADING, 0, 0);
	private Rectangle2D rect = new Rectangle2D.Double(0, 0, 0, 0);
	private Stroke stroke = DEFAULTSTROKE;
	private Color color = Color.BLUE;
	private JViewport parentScroll;
	private final ScalableViewPanel panel;

	private JComponent component;

	private ScalableComponent scalable;

	public PIPInteractionPanel(ScalableViewPanel panel) {

		super(new BorderLayout());

		this.setBorder(BorderFactory.createEmptyBorder());
		this.setOpaque(true);

		this.addMouseMotionListener(this);
		this.addMouseListener(this);

		this.parentScroll = panel.getViewport();
		this.panel = panel;

	}

	public synchronized void initializeImage() {
		Dimension size = component.getPreferredSize();
		if (size.getWidth() > 0 && size.getHeight() > 0) {
			if (this.getComponentCount() > 0) {
				this.remove(0);
			}

			double rx = (double) getWidth() / (double) size.width;
			double ry = (double) getHeight() / (double) size.height;
			double r = Math.min(rx, ry);

			BufferedImage image = (BufferedImage) component.createImage(getWidth(), getHeight());
			Graphics2D g2ds = image.createGraphics();

			g2ds.setColor(component.getBackground());
			g2ds.fillRect(0, 0, getWidth(), getHeight());

			double oldScale = scalable.getScale();
			scalable.setScale(r * oldScale);
			component.paint(g2ds);
			scalable.setScale(oldScale);
			g2ds.dispose();

			drawMain(rect.getX(), rect.getY());
			setRect();

			JLabel label = new JLabel(new ImageIcon(image), SwingConstants.LEFT);
			label.setBorder(BorderFactory.createEmptyBorder());
			this.add(label, BorderLayout.NORTH);
			this.invalidate();
			this.repaint();
			repaintNeeded = false;
		}

	}

	public double getVisWidth() {
		return component.getSize().getWidth();
	}

	public double getVisHeight() {
		return component.getSize().getHeight();
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);
		if (rect != null) {
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(color);
			g2d.setStroke(stroke);
			g2d.draw(rect);
		}
	}

	public Rectangle2D getRect() {
		return rect;
	}

	public void setRect() {
		Rectangle2D rect2 = parentScroll.getViewRect();
		double s = getScale();
		double x = s * Math.max(0, rect2.getX());
		double y = s * Math.max(0, rect2.getY());
		double w = s * Math.min(rect2.getWidth(), getVisWidth());
		double h = s * Math.min(rect2.getHeight(), getVisHeight());
		this.rect = new Rectangle2D.Double(x, y, w, h);
	}

	public synchronized void mouseDragged(MouseEvent evt) {
		if (SwingUtilities.isLeftMouseButton(evt)) {
			// a is the point in the graph where I dragged to
			if (pressPoint == null) {
				// I didn't start dragging inside rectangle.
				return;
			}
			double offsetX = pressPoint.getX() - rect.getX();
			double offsetY = pressPoint.getY() - rect.getY();
			pressPoint = evt.getPoint();
			double x = pressPoint.getX() - offsetX;
			double maxX = getWidth() - rect.getWidth();
			if (x > maxX) {
				x = maxX;
			}
			if (x < 0) {
				x = 0;
			}
			double maxY = getHeight() - rect.getHeight();
			double y = pressPoint.getY() - offsetY;
			if (y > maxY) {
				y = maxY;
			}
			if (y < 0) {
				y = 0;
			}
			drawMain(x, y);

			rect = new Rectangle2D.Double(x, y, rect.getWidth(), rect.getHeight());
		} else if (SwingUtilities.isRightMouseButton(evt)) {
			double endX = Math.max(0, Math.min(evt.getPoint().getX(), getWidth()));
			double endY = Math.max(0, Math.min(evt.getPoint().getY(), getHeight()));

			if (startDragPoint != null) {

				double startX = startDragPoint.getX();
				double startY = startDragPoint.getY();

				double r = ((double) parentScroll.getHeight() / (double) parentScroll.getWidth());
				double w = Math.abs(endX - startX);
				double h = w * r;

				if ((endX >= startX) && (endY >= startY)) {
					if (startY + h > getHeight()) {
						h = getHeight() - startY;
						w = h / r;
					}
					rect = new Rectangle2D.Double(startX, startY, w, h);
				} else if ((endX >= startX) && (endY < startY)) {
					if (startY - h < 0) {
						h = startY;
						w = h / r;
					}
					rect = new Rectangle2D.Double(startX, startY - h, w, h);
				} else if ((endX < startX) && (endY >= startY)) {
					if (startY + h > getHeight()) {
						h = getHeight() - startY;
						w = h / r;
					}
					rect = new Rectangle2D.Double(startX - w, startY, w, h);
				} else {
					if (startY - h < 0) {
						h = startY;
						w = h / r;
					}
					rect = new Rectangle2D.Double(startX - w, startY - h, w, h);
				}
			}
		}
		repaint();
	}

	private Point2D pressPoint = null;
	private Point startDragPoint;
	private Rectangle2D lastRect;
	private boolean repaintNeeded = true;

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
	}

	public synchronized void mousePressed(MouseEvent e) {
		// store the point where I clicked the mouse
		if (rect != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				pressPoint = null;
				Point2D a = e.getPoint();
				if (rect.contains(a)) {
					pressPoint = a;
				}
				stroke = DEFAULTSTROKE;
				color = Color.BLUE;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				pressPoint = null;
				startDragPoint = null;
				Rectangle2D visRect = new Rectangle2D.Double(0, 0, getWidth(), getHeight());
				if (visRect.contains(e.getPoint())) {
					startDragPoint = e.getPoint();
					lastRect = rect;
					rect = null;
					stroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
							new float[] { 5.0f }, 0.0f);
					color = Color.BLUE;
					repaint();
				}
			}
		}
	}

	public synchronized void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			stroke = DEFAULTSTROKE;
			color = Color.BLUE;
			if (rect == null || lastRect == null) {
				return;
			}

			double f = getScale();

			double w = Math.max(parentScroll.getExtentSize().getWidth() * f, lastRect.getWidth()) / lastRect.getWidth();
			double h = Math.max(parentScroll.getExtentSize().getHeight() * f, lastRect.getHeight())
					/ lastRect.getHeight();

			double scaleFactor = rect.getWidth() / lastRect.getWidth() / w;
			scaleFactor = Math.max(scaleFactor, rect.getHeight() / lastRect.getHeight() / h);
			double x = rect.getX();
			double y = rect.getY();

			panel.setScale(panel.getScale() / scaleFactor);

			drawMain(x, y);
			setRect();

		}
	}

	public void drawMain(double x, double y) {
		// The point I have now should be translated back
		// to a point in the main graph.

		double f = getScale();
		int xPos = (int) Math.ceil(x / f);
		int yPos = (int) Math.ceil(y / f);

		if (!panel.getVerticalScrollBar().isShowing()) {
			xPos = xPos - panel.getVerticalScrollBar().getWidth();
			xPos = Math.max(xPos, 0);
		}
		if (!panel.getHorizontalScrollBar().isShowing()) {
			yPos = yPos - panel.getHorizontalScrollBar().getHeight();
			yPos = Math.max(yPos, 0);
		}

		parentScroll.setViewPosition(new Point(xPos, yPos));

	}

	public double getScale() {
		Dimension size = component.getPreferredSize();

		double rx = (double) getWidth() / (double) size.width;
		double ry = (double) getHeight() / (double) size.height;
		double r = Math.min(rx, ry);
		return r;
	}

	public void setScalableComponent(ScalableComponent scalable) {
		this.scalable = scalable;
		this.component = scalable.getComponent();
	}

	public void setParent(ScalableViewPanel parent) {

	}

	public JComponent getComponent() {
		return this;
	}

	public int getPosition() {
		return SwingConstants.NORTH;
	}

	public String getPanelName() {
		return "PIP";
	}

	public void updated() {
		repaintNeeded = true;
	}

	public double getHeightInView() {
		Dimension size = component.getPreferredSize();
		double ratio = (size.getWidth() / size.getHeight());
		if (ratio > 1) {
			// wider than heigh, so height depends on width
			return PIPSIZE / ratio;
		}
		return PIPSIZE;
	}

	public double getWidthInView() {
		Dimension size = component.getPreferredSize();
		double ratio = (size.getWidth() / size.getHeight());
		if (ratio < 1) {
			// heigher than wide, so width depends on height
			return PIPSIZE / ratio;
		}
		return PIPSIZE;
	}

	public void willChangeVisibility(boolean to) {
		if (repaintNeeded) {
			initializeImage();
		}
		setRect();
	}

	public void setSize(int width, int height) {
		Dimension oldSize = getSize();
		super.setSize(width, height);
		if (!getSize().equals(oldSize)) {
			repaintNeeded = true;
		}
	}

}
