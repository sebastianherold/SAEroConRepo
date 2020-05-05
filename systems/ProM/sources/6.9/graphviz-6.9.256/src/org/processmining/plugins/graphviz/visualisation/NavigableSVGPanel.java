package org.processmining.plugins.graphviz.visualisation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.processmining.plugins.graphviz.visualisation.export.ExportDialog;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.export.ExporterEMF;
import org.processmining.plugins.graphviz.visualisation.export.ExporterEPS;
import org.processmining.plugins.graphviz.visualisation.export.ExporterPDF;
import org.processmining.plugins.graphviz.visualisation.export.ExporterPNG;
import org.processmining.plugins.graphviz.visualisation.export.ExporterSVG;
import org.processmining.plugins.graphviz.visualisation.listeners.ImageTransformationChangedListener;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;

/**
 * A JPanel that displays an SVG image. Animation support is present, but not
 * complete, i.e. you'll need a subclass to perform animation. Controls are
 * included to ease implementation of subclasses.
 * 
 * @author sleemans
 *
 */
public class NavigableSVGPanel extends JPanel implements Printable {

	private static final long serialVersionUID = -3285916948952045282L;

	//state variables and constants
	protected SVGDiagram image;
	protected AffineTransform image2user = new AffineTransform();
	private AffineTransform user2image = new AffineTransform();
	private Point lastMousePosition;
	private Dimension lastPanelDimension = null;
	private boolean resetViewLater = false;

	protected boolean isDraggingImage = false;
	private final static double zoomIncrement = 1.8;

	//animation controls
	protected boolean isDraggingAnimation = false;
	protected boolean isDraggingTimeScale = false;
	private boolean wasPlayingBeforeDragging = false;
	protected Rectangle animationControls;
	protected Rectangle controlsPlayPause = new Rectangle();
	protected Rectangle controlsTimeScale = new Rectangle();
	private Rectangle controlsProgressLine = new Rectangle();
	private boolean animationControlsShowing = false;

	//navigation variables and constants
	private double navigationScale = 1.0;
	public final static double navigationImageWidthInPartOfPanel = 0.1;
	public final static Color navigationImageBorderColor = Color.black;
	public final static float dash1[] = { 10.0f };
	public final static BasicStroke navigationImageOutlineStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	//listeners
	private ImageTransformationChangedListener imageTransformationChangedListener = null;

	//helper controls variables and constants
	private Arc2D helperControlsArc = null;
	private boolean helperControlsShowing = false;
	public static final int helperControlsWidth = 350;
	public static final Font helperControlsFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
	private static final Font helperControlsButtonFont = new Font("TimesRoman", Font.PLAIN, 20);
	private static final String helperControlsButtonString = "?";

	//help-popup
	protected List<String> helperControlsShortcuts = new ArrayList<>(
			Arrays.asList("up/down", "left/right", "ctrl =", "ctrl -", "ctrl 0", "ctrl i", "ctrl p"));
	protected List<String> helperControlsExplanations = new ArrayList<>(Arrays.asList("pan up/down", "pan left/right",
			"zoom in", "zoom out", "reset zoom & pan", "save image", "print"));

	private Action zoomInAction = new AbstractAction() {
		private static final long serialVersionUID = 3863042569537144601L;

		public void actionPerformed(ActionEvent e) {
			//zoom in on the center of the panel
			try {
				zoomIn(new Point(getWidth() / 2, getHeight() / 2));
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action zoomOutAction = new AbstractAction() {
		private static final long serialVersionUID = 7842478506942554961L;

		public void actionPerformed(ActionEvent e) {
			//zoom in of the center of the panel
			try {
				zoomOut(new Point(getWidth() / 2, getHeight() / 2));
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action resetViewAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			//reset the view
			try {
				resetView();
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action walkAction = new AbstractAction() {
		private static final long serialVersionUID = 1114226211978622533L;

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if (command.equals("DOWN")) {
				image2user.translate(0, -10);
			} else if (command.equals("UP")) {
				image2user.translate(0, 10);
			} else if (command.equals("LEFT")) {
				image2user.translate(10, 0);
			} else if (command.equals("RIGHT")) {
				image2user.translate(-10, 0);
			}
			try {
				user2image = image2user.createInverse();
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
		}
	};

	private Action printAction = new AbstractAction() {
		private static final long serialVersionUID = -7112953904402077397L;

		public void actionPerformed(ActionEvent e) {
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPrintable(NavigableSVGPanel.this);
			boolean ok = job.printDialog();
			if (ok) {
				try {
					job.print();
				} catch (PrinterException ex) {
					/* The job did not successfully complete */
				}
			}
		}
	};

	public NavigableSVGPanel(final SVGDiagram newImage) {
		setOpaque(false);
		setDoubleBuffered(true);
		setFocusable(true);
		setImage(newImage, false);
		setupListeners();
	}

	public void setupListeners() {
		//set up resize listener
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				try {
					if (image2user.isIdentity() || resetViewLater) {
						lastPanelDimension = new Dimension(getWidth(), getHeight());
						resetView();
					} else if (lastPanelDimension != null) {
						//on resizing, keep the center in center, and scale proportionally to the width.
						double zoom = lastPanelDimension.getWidth() / getWidth();
						user2image.translate(lastPanelDimension.getWidth() / 2.0, lastPanelDimension.getHeight() / 2.0);
						user2image.scale(zoom, zoom);
						lastPanelDimension = new Dimension(getWidth(), getHeight());
						user2image.translate(-lastPanelDimension.getWidth() / 2.0,
								-lastPanelDimension.getHeight() / 2.0);
						image2user = user2image.createInverse();
					} else {
						lastPanelDimension = new Dimension(getWidth(), getHeight());
					}
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
				updateTransformation();
				repaint();
			}
		});

		//set up mouse listener
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				processMouseRelease(e);
			}

			public void mousePressed(MouseEvent e) {
				processMousePress(e);
			}

			public void mouseExited(MouseEvent e) {
				processMouseExit(e);
			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseClicked(MouseEvent e) {
				if (isFocusable()) {
					requestFocusInWindow();
				}
				processMouseClick(e);
			}
		});

		//set up drag listener
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				processMouseDrag(e);
			}

			public void mouseMoved(MouseEvent e) {
				processMouseMove(e);
			}
		});

		//set up scroll listener
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				Point p = e.getPoint();
				boolean zoomIn = (e.getWheelRotation() < 0);

				if (isInNavigation(p)) {
					//zoom navigation
					if (zoomIn) {
						zoomNavigation(1.2);
					} else {
						zoomNavigation(0.8);
					}
				} else {
					//zoom image
					try {
						if (zoomIn) {
							zoomIn(p);
						} else {
							zoomOut(p);
						}
						updateTransformation();
					} catch (NoninvertibleTransformException ex) {
						ex.printStackTrace();
					}
				}
				repaint();
			}
		});

		//listen to ctrl + to zoom in
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_MASK),
				"zoomIn"); // + key in English keyboards
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK),
				"zoomIn"); // + key in non-English keyboards
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, InputEvent.CTRL_MASK),
				"zoomIn"); // + key on the numpad
		getActionMap().put("zoomIn", zoomInAction);

		//listen to ctrl - to zoom out
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK),
				"zoomOut"); // - key
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, InputEvent.CTRL_MASK),
				"zoomOut"); // - key on the numpad
		getActionMap().put("zoomOut", zoomOutAction);

		//listen to arrow keys to walk around
		registerKeyboardAction(walkAction, "DOWN", KeyStroke.getKeyStroke("DOWN"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "UP", KeyStroke.getKeyStroke("UP"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "LEFT", KeyStroke.getKeyStroke("LEFT"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		registerKeyboardAction(walkAction, "RIGHT", KeyStroke.getKeyStroke("RIGHT"), JComponent.WHEN_IN_FOCUSED_WINDOW);

		//listen to ctrl 0 to reset view
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK),
				"viewReset"); // - key
		getActionMap().put("viewReset", resetViewAction);

		//listen to ctrl i to save image
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK),
					"saveAs"); // - key
			getActionMap().put("saveAs", new AbstractAction() {
				private static final long serialVersionUID = -4780600363000017631L;

				public void actionPerformed(ActionEvent arg0) {
					exportView();
				}
			});
		}

		//listen to ctrl p to print
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "print"); // - key
		getActionMap().put("print", printAction);
	}

	/**
	 * Paints the panel and its image at the current zoom level, location, and
	 * interpolation method dependent on the image scale.
	 * </p>
	 * 
	 * @param g
	 *            the <code>Graphics</code> context for painting
	 */
	protected void paintComponent(Graphics g) {
		if (!isPaintingForPrint()) {
			super.paintComponent(g); // Paints the background
		}

		Graphics2D g2 = (Graphics2D) g;

		if (image == null) {
			return;
		}

		//set anti-aliasing if desired
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		//set clipping mask to save a few cpu/gpu cycles
		if (!isPaintingForPrint()) {
			g2.setClip(0, 0, getWidth(), getHeight());
		} else {
			g2.setClip((int) Math.round(image.getViewRect().getX()), (int) Math.round(image.getViewRect().getY()),
					(int) Math.round(image.getViewRect().getWidth()),
					(int) Math.round(image.getViewRect().getHeight()));
		}

		//draw image
		if (!isPaintingForPrint()) {
			g2.transform(image2user);
		}
		paintImage(g2);
		if (!isPaintingForPrint()) {
			g2.transform(user2image);
		}

		//draw animation
		drawAnimation(g2);

		//Draw navigation image
		if (!isPaintingForPrint() && !isImageCompletelyInPanel()) {
			int width = (int) Math.round(getNavigationWidth());
			int height = (int) Math.round(getNavigationHeight());
			drawSVG(g2, image, 0, 0, width, height);
			g2.drawRect(0, 0, width, height);
			drawNavigationOutline(g2);
		}

		//draw helper controls
		if (!isPaintingForPrint()) {
			drawHelperControls(g2);
		}

		//draw animation controls
		if (!isPaintingForPrint()) {
			drawAnimationControls((Graphics2D) g);
		}
	}

	protected void paintImage(Graphics2D g) {
		try {
			image.render(g);
		} catch (SVGException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Draw an svg image at the given coordinates and of the given size.
	 * 
	 * @param g
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public static void drawSVG(Graphics2D g, SVGDiagram image, int x, int y, int width, int height) {

		double scaleX = width / image.getWidth();
		double scaleY = height / image.getHeight();

		g.translate(x, y);
		g.scale(scaleX, scaleY);

		try {
			image.render(g);
		} catch (SVGException e) {
			e.printStackTrace();
		}

		g.scale(1 / scaleX, 1 / scaleY);
		g.translate(-x, -y);
	}

	/**
	 * Draw the animation.
	 * 
	 * @param g
	 */
	protected void drawAnimation(Graphics2D g) {

	}

	/**
	 * Paints an outline over the navigation to denote what part of the main
	 * image is displayed.
	 * 
	 * @param g
	 * @param t
	 */
	private void drawNavigationOutline(Graphics2D g) {

		//get edges of panel in image coordinates
		Point2D.Double nw = new Point.Double(0, 0);
		user2image.transform(nw, nw);
		Point2D.Double se = new Point.Double(getWidth(), getHeight());
		user2image.transform(se, se);

		//transform to navigation coordinates
		Point2D nwNav = transformImageToNavigation(nw);
		Point2D seNav = transformImageToNavigation(se);

		int x = (int) Math.round(nwNav.getX());
		int y = (int) Math.round(nwNav.getY());
		int width = (int) Math.round(seNav.getX() - nwNav.getX());
		int height = (int) Math.round(seNav.getY() - nwNav.getY());

		if (x + width > getNavigationWidth()) {
			width = (int) Math.round(getNavigationWidth() - x);
		}
		if (y + height > getNavigationHeight()) {
			height = (int) Math.round(getNavigationHeight() - y);
		}
		Stroke backupStroke = g.getStroke();
		g.setColor(navigationImageBorderColor);
		g.setStroke(navigationImageOutlineStroke);
		g.drawRect(x, y, width, height);
		g.setStroke(backupStroke);

	}

	/**
	 * Draws the little help-question-mark in the right bottom corner and the
	 * help text that appears when hovering over it.
	 * 
	 * @param g
	 */
	private void drawHelperControls(Graphics g) {
		Color backupColour = g.getColor();
		Font backupFont = g.getFont();

		FontMetrics fm = getFontMetrics(helperControlsButtonFont);
		int width = fm.stringWidth(helperControlsButtonString);

		//draw the background arc
		if (helperControlsShowing) {
			g.setColor(new Color(0, 0, 0, 180));
		} else {
			g.setColor(new Color(0, 0, 0, 20));
		}
		helperControlsArc = new Arc2D.Float(Arc2D.PIE);
		helperControlsArc.setFrame(getWidth() - 25, getHeight() - 25, 50, 50);
		helperControlsArc.setAngleStart(90);
		helperControlsArc.setAngleExtent(90);
		g.fillArc(getWidth() - 25, getHeight() - 25, 50, 50, 90, 90);

		//draw the helper panel
		if (helperControlsShowing) {
			int x = getWidth() - (25 + helperControlsWidth);
			int y = getHeight() - (helperControlsShortcuts.size() * 20 - 10);

			//background
			g.setColor(new Color(0, 0, 0, 180));
			g.fillRoundRect(x - 15, y - 20, helperControlsWidth, helperControlsShortcuts.size() * 20 + 20, 10, 10);

			//text
			g.setColor(new Color(255, 255, 255, 220));
			g.setFont(helperControlsFont);
			for (int i = 0; i < helperControlsShortcuts.size(); i++) {
				g.drawString(String.format("%-12s", helperControlsShortcuts.get(i)) + " "
						+ helperControlsExplanations.get(i), x, y);
				y += 20;
			}
		}

		//draw the question mark
		if (helperControlsShowing) {
			g.setColor(new Color(255, 255, 255, 128));
		} else {
			g.setColor(new Color(0, 0, 0, 128));
		}
		g.setFont(helperControlsButtonFont);
		g.drawString(helperControlsButtonString, getWidth() - width - 3, getHeight() - 3);

		//revert colour and font
		g.setColor(backupColour);
		g.setFont(backupFont);
	}

	private void drawAnimationControls(Graphics2D g) {
		Color backupColour = g.getColor();

		int alpha = 20;
		if (!isAnimationEnabled()) {
			alpha = 0;
		} else if (animationControlsShowing) {
			alpha = 180;
		}

		g.setColor(new Color(0, 0, 0, alpha));
		int width = getWidth() - 100;
		int height = 50;
		int x = (getWidth() - width) / 2;
		int y = getHeight() - 2 * height;
		g.fillRoundRect(x, y, width, height, 10, 10);
		animationControls = new Rectangle(x, y, width, height);

		//play button
		g.setColor(new Color(255, 255, 255, alpha));
		controlsPlayPause.setBounds(x + 10, y + 10, 30, height);
		if (!isAnimationPlaying()) {
			//play button
			Polygon triangle = new Polygon();
			triangle.addPoint(x + 10, y + 10);
			triangle.addPoint(x + 10, y + height - 10);
			triangle.addPoint(x + 10 + 25, y + (height / 2));
			g.fillPolygon(triangle);
		} else {
			//pause button
			g.fillRoundRect(x + 10, y + 10, 10, height - 20, 5, 5);
			g.fillRoundRect(x + 25, y + 10, 10, height - 20, 5, 5);
		}

		//time scale control
		{
			int rightX = x + 50 + 30;
			int leftX = x + 50;
			int topY = y + (height / 3);
			int bottomY = y + (height / 3) * 2;

			if (animationControlsShowing) {
				//dynamic part
				{
					int x2 = (int) (leftX + getTimeScale() * (rightX - leftX));
					int y2 = (int) (bottomY + getTimeScale() * (topY - bottomY));

					//line
					g.drawLine(x2, bottomY + 5, x2, y2 - 5);

					//fill
					Polygon triangle = new Polygon();
					triangle.addPoint(x2, y2);
					triangle.addPoint(leftX, bottomY);
					triangle.addPoint(x2, bottomY);
					g.fillPolygon(triangle);
				}

				//outline
				{
					Polygon triangle = new Polygon();
					triangle.addPoint(rightX, topY);
					triangle.addPoint(leftX, bottomY);
					triangle.addPoint(rightX, bottomY);
					g.drawPolygon(triangle);
				}
			}

			controlsTimeScale.setBounds(leftX, y + 5, rightX - leftX, height - 10);
		}

		//progress line
		{
			int startLineX = x + 100;
			int endLineX = x + width - 20;
			int lineY = y + height / 2;

			if (animationControlsShowing) {
				g.drawLine(startLineX, lineY, endLineX, lineY);
				double progress = (getAnimationTime() - getAnimationMinimumTime())
						/ (getAnimationMaximumTime() - getAnimationMinimumTime());

				//draw the little oval that denotes where we are
				double ovalX = (startLineX + (endLineX - startLineX) * progress) - 5;
				double ovalY = lineY - 5;
				g.translate(ovalX, ovalY);
				g.fillOval(0, 0, 10, 10);
				g.translate(-ovalX, -ovalY);
			}

			getControlsProgressLine().setBounds(startLineX, y, endLineX - startLineX, height);
		}

		g.setColor(backupColour);
	}

	/**
	 * 
	 * @return The width of the navigation part in user coordinates.
	 */
	private double getNavigationWidth() {
		return getWidth() * navigationImageWidthInPartOfPanel * navigationScale;
	}

	/**
	 * 
	 * @return The height of the navigation part in user coordinates.
	 */
	private double getNavigationHeight() {
		return (getNavigationWidth() / image.getWidth()) * image.getHeight();
	}

	/**
	 * Transforms the given point in navigation coordinates to image coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformNavigationToImage(Point2D p) {
		double x = p.getX() * image.getWidth() / getNavigationWidth();
		double y = p.getY() * image.getHeight() / getNavigationHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Transforms the given point in image coordinates to navigation coordinates
	 * 
	 * @param p
	 * @return
	 */
	public Point2D transformImageToNavigation(Point2D p) {
		double x = p.getX() * getNavigationWidth() / image.getWidth();
		double y = p.getY() * getNavigationHeight() / image.getHeight();

		return new Point2D.Double(x, y);
	}

	/**
	 * Returns whether a point (in user coordinates) is in the image and not in
	 * the navigation image.
	 * 
	 * @param pointInUserCoordinates
	 * @return
	 */
	public boolean isInImage(Point pointInUserCoordinates) {
		if (isInNavigation(pointInUserCoordinates)) {
			return false;
		}
		return getImageBoundingBoxInUserCoordinates().contains(pointInUserCoordinates);
	}

	/**
	 * Returns whether a point (in user coordinates) is in the navigation image.
	 * 
	 * @param pointInUserCoordinates
	 * @return
	 */
	public boolean isInNavigation(Point pointInUserCoordinates) {
		return (pointInUserCoordinates.x < getNavigationWidth() && pointInUserCoordinates.y < getNavigationHeight());
	}

	public boolean isInHelperControls(Point pointInUserCoordinates) {
		return helperControlsArc != null && helperControlsArc.contains(pointInUserCoordinates);
	}

	/**
	 * 
	 * @return The currently registered shortcuts, which are displayed when
	 *         hovering over the question mark in the lower right corner.
	 */
	public List<String> getHelperControlsShortcuts() {
		return helperControlsShortcuts;
	}

	/**
	 * Sets the shortcuts, displayed when hovering over the question mark in the
	 * lower right corner.
	 * 
	 * @param helperControlsShortcuts
	 */
	public void setHelperControlsShortcuts(List<String> helperControlsShortcuts) {
		this.helperControlsShortcuts = helperControlsShortcuts;
	}

	/**
	 * @return The currently registered shortcut explanations, which are
	 *         displayed when hovering over the question mark in the lower right
	 *         corner.
	 */
	public List<String> getHelperControlsExplanations() {
		return helperControlsExplanations;
	}

	/**
	 * Sets the shortcut explanations, displayed when hovering over the question
	 * mark in the lower right corner.
	 * 
	 * @param helperControlsExplanations
	 */
	public void setHelperControlsExplanations(List<String> helperControlsExplanations) {
		this.helperControlsExplanations = helperControlsExplanations;
	}

	/**
	 * 
	 * @return The currently displaying svg image.
	 */
	public SVGDiagram getImage() {
		return image;
	}

	/**
	 * 
	 * @return The bounding box of the image in user coordinates, truncated
	 *         (approximately) to the visible area.
	 */
	public Rectangle getVisibleImageBoundingBoxInUserCoordinates() {
		//transform point (0,0) to user coordinates
		Point2D.Double nw = new Point.Double(0, 0);
		user2image.transform(nw, nw);
		double x1 = Math.min(Math.max(0, nw.getX()), getWidth());
		double y1 = Math.min(Math.max(0, nw.getY()), getHeight());

		//transform the other corner to user coordinates
		Point2D se = new Point2D.Double(image.getWidth(), image.getHeight());
		user2image.transform(se, se);
		double x2 = Math.min((Math.max(0, se.getX())), getWidth());
		double y2 = Math.min((Math.max(0, se.getY())), getHeight());

		return new Rectangle((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.abs(x2 - x1),
				(int) Math.abs(y2 - y1));
	}

	/**
	 * 
	 * @return the bounding box of the complete image in user coordinates
	 */
	public Rectangle getImageBoundingBoxInUserCoordinates() {
		//transform point (0,0) to user coordinates
		Point2D.Double nw = new Point.Double(0, 0);
		image2user.transform(nw, nw);
		double x1 = nw.getX();
		double y1 = nw.getY();

		//transform the other corner to user coordinates
		Point2D se = new Point2D.Double(image.getWidth(), image.getHeight());
		image2user.transform(se, se);
		double x2 = se.getX();
		double y2 = se.getY();

		return new Rectangle((int) Math.min(x1, x2), (int) Math.min(y1, y2), (int) Math.abs(x2 - x1),
				(int) Math.abs(y2 - y1));
	}

	/**
	 * Returns whether the image is completely visible in the panel
	 * 
	 * @param panState
	 * @param diagram
	 * @param panel
	 * @return
	 */
	public boolean isImageCompletelyInPanel() {
		//check the northwest corner
		Point2D.Double nw = new Point.Double(0, 0);
		image2user.transform(nw, nw);
		if (nw.getX() < 0 || nw.getY() < 0) {
			return false;
		}

		//check the southeast corner
		Point2D.Double se = new Point2D.Double(image.getWidth(), image.getHeight());
		image2user.transform(se, se);
		if (se.getX() > getWidth() || se.getY() > getHeight()) {
			return false;
		}

		return true;
	}

	/**
	 * <p>
	 * Sets an image for display in the panel.
	 * </p>
	 * 
	 * @param image
	 *            an image to be set in the panel
	 */
	public void setImage(SVGDiagram image, boolean resetView) {
		if (image == null) {
			System.out.println("invalid dot given");
			throw new NullPointerException("invalid dot given");
		}
		this.image = image;
		image.setDeviceViewport(new java.awt.Rectangle(0, 0, (int) image.getWidth(), (int) image.getHeight()));

		if (resetView) {
			try {
				resetView();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}
			updateTransformation();
		}

		repaint();
	}

	/**
	 * Scale and center the image just in the viewport.
	 * 
	 * @throws NoninvertibleTransformException
	 */
	public void resetView() throws NoninvertibleTransformException {
		if (getWidth() <= 0 || getHeight() <= 0) {
			resetViewLater = true;
			return;
		}
		resetViewLater = false;

		double scaleX = getWidth() / (double) image.getWidth();
		double scaleY = getHeight() / (double) image.getHeight();
		double scale = Math.min(scaleX, scaleY);

		double width = scale * image.getWidth();
		double height = scale * image.getHeight();

		double x = (getWidth() - width) / 2.0;
		double y = (getHeight() - height) / 2.0;

		image2user.setToIdentity();
		image2user.translate(x, y);
		image2user.scale(scale, scale);
		user2image = image2user.createInverse();

		//reset navigation image
		navigationScale = 1.0;
	}

	/**
	 * The user clicked within the navigation image and this part of the image
	 * is displayed in the panel. The clicked point of the image is centered in
	 * the panel.
	 * 
	 * @param pointInNavigationCoordinates
	 * @throws NoninvertibleTransformException
	 */
	public void centerImageAround(Point pointInNavigationCoordinates) throws NoninvertibleTransformException {
		//transform to image coordinates
		Point2D pImage = transformNavigationToImage(pointInNavigationCoordinates);

		//transform to user coordinates
		image2user.transform(pImage, pImage);

		//compute difference
		double dx = (getWidth() / 2.0 - pImage.getX()) / image2user.getScaleX();
		double dy = (getHeight() / 2.0 - pImage.getY()) / image2user.getScaleY();

		//and translate
		image2user.translate(dx, dy);
		user2image = image2user.createInverse();
	}

	/**
	 * Zoom in keeping the given point at its place
	 * 
	 * @param aroundInUserCoordinates
	 * @throws NoninvertibleTransformException
	 */
	private void zoomIn(Point2D aroundInUserCoordinates) throws NoninvertibleTransformException {
		user2image.translate(aroundInUserCoordinates.getX(), aroundInUserCoordinates.getY());
		user2image.scale(1 / zoomIncrement, 1 / zoomIncrement);
		user2image.translate(-aroundInUserCoordinates.getX(), -aroundInUserCoordinates.getY());
		image2user = user2image.createInverse();
	}

	/**
	 * Zoom out keeping the given point at its place
	 * 
	 * @param aroundInUserCoordinates
	 * @throws NoninvertibleTransformException
	 */
	private void zoomOut(Point2D aroundInUserCoordinates) throws NoninvertibleTransformException {
		user2image.translate(aroundInUserCoordinates.getX(), aroundInUserCoordinates.getY());
		user2image.scale(zoomIncrement, zoomIncrement);
		user2image.translate(-aroundInUserCoordinates.getX(), -aroundInUserCoordinates.getY());
		image2user = user2image.createInverse();
	}

	/**
	 * Process a mouse press.
	 * 
	 * @param e
	 * @return whether the press was handled and did something.
	 */
	protected boolean processMousePress(MouseEvent e) {
		Point point = e.getPoint();
		lastMousePosition = point;

		//process press in helper text
		if (SwingUtilities.isLeftMouseButton(e) && isInHelperControls(point)) {
			return true;
		}

		//process press in animation controls
		if (SwingUtilities.isLeftMouseButton(e) && isAnimationEnabled() && animationControls != null
				&& animationControls.contains(point)) {
			if (getControlsProgressLine().contains(point)) {
				//clicked in progress line area
				wasPlayingBeforeDragging = isAnimationPlaying();
			} else if (controlsPlayPause.contains(point)) {
				//clicked on play/pause button
				pauseResume();

				//repaint to make sure the button is changed
				repaint();
			} else if (controlsTimeScale.contains(point)) {
				//clicked in time scale
			}
			return true;
		}

		//process press in navigation
		if (SwingUtilities.isLeftMouseButton(e) && isInNavigation(point)) {
			//pressed in navigation
			try {
				centerImageAround(point);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			updateTransformation();
			repaint();
			return true;
		}

		//process press anywhere else (=> on the image)
		if (SwingUtilities.isLeftMouseButton(e)) {
			return true;
		}

		return false;
	}

	/**
	 * Process a mouse release
	 * 
	 * @param e
	 * @return whether the hover was handled and did something.
	 */
	protected boolean processMouseRelease(MouseEvent e) {
		if (isDraggingAnimation) {
			double progress = Math.max(0,
					Math.min(1, (e.getX() - getControlsProgressLine().x) / (getControlsProgressLine().width * 1.0)));
			seek(getAnimationMinimumTime() + progress * (getAnimationMaximumTime() - getAnimationMinimumTime()));
			if (wasPlayingBeforeDragging) {
				resume();
			} else {
				renderOneFrame();
				repaint();
			}
		}

		if (isDraggingImage) {
			renderOneFrame();
		}

		isDraggingImage = false;
		isDraggingAnimation = false;
		isDraggingTimeScale = false;

		return processMouseMove(e);
	}

	/**
	 * Process a mouse drag;
	 * 
	 * @param e
	 * @return whether the drag was handled and did something.
	 */
	protected boolean processMouseDrag(MouseEvent e) {
		if (!isDraggingImage && !isDraggingAnimation && !isDraggingTimeScale) {
			if (SwingUtilities.isLeftMouseButton(e) && isAnimationEnabled() && animationControls != null
					&& animationControls.contains(lastMousePosition)
					&& getControlsProgressLine().contains(lastMousePosition)) {
				isDraggingAnimation = true;
			} else if (SwingUtilities.isLeftMouseButton(e) && isAnimationEnabled() && animationControls != null
					&& animationControls.contains(lastMousePosition) && controlsTimeScale.contains(lastMousePosition)) {
				isDraggingTimeScale = true;
			} else if (SwingUtilities.isLeftMouseButton(e) && contains(e.getPoint())
					&& !animationControls.contains(lastMousePosition)) {
				isDraggingImage = true;
			}
		}

		if (isDraggingImage) {
			Point point = e.getPoint();
			if (lastMousePosition != null) {
				double dx = (point.x - lastMousePosition.x) / image2user.getScaleX();
				double dy = (point.y - lastMousePosition.y) / image2user.getScaleY();

				//pan with the difference in user coordinates between the last known mouse position and this one
				image2user.translate(dx, dy);
				try {
					user2image = image2user.createInverse();
				} catch (NoninvertibleTransformException e1) {
					e1.printStackTrace();
				}
			}
			lastMousePosition = point;
			updateTransformation();
			repaint();
			return true;
		} else if (isDraggingAnimation) {
			pause();

			double progress = Math.min(1,
					Math.max(0, (e.getX() - getControlsProgressLine().x) / (getControlsProgressLine().width * 1.0)));
			seek(getAnimationMinimumTime() + progress * (getAnimationMaximumTime() - getAnimationMinimumTime()));
			renderOneFrame();
			lastMousePosition = e.getPoint();
			return true;
		} else if (isDraggingTimeScale) {
			double progress = Math.min(1,
					Math.max(0, (e.getX() - controlsTimeScale.x) / (controlsTimeScale.width * 1.0)));
			setTimeScale(progress);
			lastMousePosition = e.getPoint();
			repaint();
			return true;
		}

		return false;
	}

	/**
	 * Process a mouse move. Captured = true implies that the hover was already
	 * processed (and we should hide everything related to hovering).
	 * 
	 * @param e
	 * @return whether the move is hovering something.
	 */
	protected boolean processMouseMove(MouseEvent e) {
		lastMousePosition = e.getPoint();
		if (!isDraggingImage && helperControlsArc != null && !helperControlsShowing
				&& isInHelperControls(e.getPoint())) {
			//we have to show the helper controls
			helperControlsShowing = true;
			animationControlsShowing = false;
			repaint();
			return true;
		} else if (!isDraggingImage && animationControls != null && !animationControlsShowing
				&& isInAnimationControls(e.getPoint())) {
			//we have to show the animation controls
			animationControlsShowing = true;
			helperControlsShowing = false;
			repaint();
			return true;
		} else {
			//hide things
			if (animationControlsShowing && !isInAnimationControls(e.getPoint())) {
				animationControlsShowing = false;
				repaint();
			}
			if (helperControlsShowing && !isInHelperControls(e.getPoint())) {
				helperControlsShowing = false;
				repaint();
			}
			return helperControlsShowing || animationControlsShowing || isInNavigation(e.getPoint());
		}
	}

	/**
	 * Process a mouse click
	 * 
	 * @param e
	 * @return whether the click was handled and did something.
	 */
	protected boolean processMouseClick(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e) && isAnimationEnabled() && animationControls != null
				&& animationControls.contains(lastMousePosition)
				&& getControlsProgressLine().contains(lastMousePosition)) {
			double progress = Math.max(0,
					Math.min(1, (e.getX() - getControlsProgressLine().x) / (getControlsProgressLine().width * 1.0)));
			seek(getAnimationMinimumTime() + progress * (getAnimationMaximumTime() - getAnimationMinimumTime()));
			if (wasPlayingBeforeDragging) {
				resume();
			} else {
				renderOneFrame();
				repaint();
			}
		}

		if (controlsTimeScale.contains(e.getPoint())) {
			double progress = Math.min(1,
					Math.max(0, (e.getX() - controlsTimeScale.x) / (controlsTimeScale.width * 1.0)));
			setTimeScale(progress);
			repaint();
		}

		return isInHelperControls(e.getPoint()) || isInAnimationControls(e.getPoint()) || isInNavigation(e.getPoint());
	}

	/**
	 * Process a mouse exit
	 * 
	 * @param e
	 * @return whether the exit was handled and did something.
	 */
	protected boolean processMouseExit(MouseEvent e) {
		if (helperControlsShowing) {
			helperControlsShowing = false;
			repaint();
		}
		if (animationControlsShowing) {
			animationControlsShowing = false;
			repaint();
		}
		if (isDraggingAnimation) {
			isDraggingAnimation = false;
			if (wasPlayingBeforeDragging) {
				resume();
			} else {
				renderOneFrame();
				repaint();
			}
		}
		if (isDraggingImage) {
			isDraggingImage = false;
		}
		if (isDraggingTimeScale) {
			isDraggingTimeScale = false;
		}
		return false;
	}

	/**
	 * Zoom the navigation
	 * 
	 * @param zoomFactor
	 */
	private void zoomNavigation(double zoomFactor) {
		navigationScale *= zoomFactor;
	}

	public void setImageTransformationChangedListener(ImageTransformationChangedListener listener) {
		this.imageTransformationChangedListener = listener;
	}

	private void updateTransformation() {
		if (imageTransformationChangedListener != null) {
			imageTransformationChangedListener.imageTransformationChanged(image2user, user2image);
		}
	}

	public Point2D transformUser2Image(Point2D p) {
		return user2image.transform(p, null);
	}

	public Point2D transformImage2User(Point2D p) {
		return image2user.transform(p, null);
	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return whether the animation is rendered and controls are displayed.
	 */
	public boolean isAnimationEnabled() {
		return false;
	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return whether the animation is currently playing (not paused)
	 */
	public boolean isAnimationPlaying() {
		return false;
	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return the time scale set, which is a 0 <= number <= 1.
	 */
	public double getTimeScale() {
		return 0.5;
	}

	/**
	 * Request the time scale to be set. Needs to be overridden by a subclass.
	 * 
	 * @param time
	 */
	public void setTimeScale(double time) {

	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return the current animation time.
	 */
	public double getAnimationTime() {
		return 0;
	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return the minimum animation time.
	 */
	public double getAnimationMinimumTime() {
		return 0;
	}

	/**
	 * Needs to be overridden by a subclass.
	 * 
	 * @return the maximum animation time.
	 */
	public double getAnimationMaximumTime() {
		return 0;
	}

	/**
	 * 
	 * @param p
	 * @return whether a point lies in the animation controls.
	 */
	protected boolean isInAnimationControls(Point p) {
		return isAnimationEnabled() && animationControls != null && animationControls.contains(p);
	}

	/**
	 * Request the next animation frame to be rendered at the given time. Needs
	 * to be overridden by a subclass.
	 * 
	 * @param time
	 */
	public void seek(double time) {

	}

	/**
	 * Request the animation to pause. Needs to be overridden by a subclass.
	 */
	public void pause() {

	}

	/**
	 * Resume the animation from pausing. Needs to be overridden by a subclass.
	 */
	public void resume() {

	}

	/**
	 * Request the animation to pause or resume. Needs to be overridden by a
	 * subclass.
	 * 
	 */
	public void pauseResume() {

	}

	/**
	 * Request one frame of the animation to be rendered. Needs to be overridden
	 * by a subclass.
	 * 
	 */
	public void renderOneFrame() {

	}

	public Rectangle getControlsProgressLine() {
		return controlsProgressLine;
	}

	public boolean isAnimationControlsShowing() {
		return animationControlsShowing;
	}

	/**
	 * 
	 * @return The list of exporters. Subclasses can override this to a relevant
	 *         list. Called everytime the user attempts to export the image.
	 */
	public List<Exporter> getExporters() {
		List<Exporter> exporters = new ArrayList<>();
		exporters.add(new ExporterPDF());
		exporters.add(new ExporterPNG());
		exporters.add(new ExporterSVG());
		exporters.add(new ExporterEPS());
		exporters.add(new ExporterEMF());
		return exporters;
	}

	/**
	 * Prompts the user for a file name and saves the view under that file name.
	 */
	public void exportView() {
		new ExportDialog(this, getExporters());
	}

	public int print(Graphics g, PageFormat pageFormat, int page) throws PrinterException {
		if (page > 0) {
			return NO_SUCH_PAGE;
		}

		Graphics2D g2d = (Graphics2D) g;

		double scaleX = pageFormat.getImageableWidth() / image.getWidth();
		double scaleY = pageFormat.getImageableHeight() / image.getHeight();

		if (scaleX < scaleY) {
			g2d.translate(pageFormat.getImageableWidth() / 2, pageFormat.getImageableHeight() / 2);
			g2d.rotate(Math.toRadians(-90));
			g2d.translate(pageFormat.getImageableHeight() / -2, pageFormat.getImageableWidth() / -2);
			g2d.translate(-pageFormat.getImageableX(), pageFormat.getImageableY());

			double scale = pageFormat.getImageableHeight() / image.getWidth();
			g2d.scale(scale, scale);
		} else {
			g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g2d.scale(scaleY, scaleY);
		}

		// Now we perform our rendering
		print(g2d);

		// tell the caller that this page is part
		// of the printed document
		return PAGE_EXISTS;
	}
}
