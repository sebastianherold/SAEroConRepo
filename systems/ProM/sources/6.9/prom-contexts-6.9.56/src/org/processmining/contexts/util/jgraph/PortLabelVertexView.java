package org.processmining.contexts.util.jgraph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

/**
 * 
 */
public class PortLabelVertexView extends VertexView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4669647479882266935L;

	/**
	 * the renderer for this view
	 */
	protected static WrapperPortLabelRenderer renderer = new WrapperPortLabelRenderer();

	protected CellView[] ports;

	/**
	 * Creates new instance of <code>InstanceView</code>.
	 */
	public PortLabelVertexView() {
		super();
	}

	/**
	 * Creates new instance of <code>InstanceView</code> for the specified graph
	 * cell.
	 * 
	 * @param arg0
	 *            a graph cell to create view for
	 */
	public PortLabelVertexView(Object arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jgraph.graph.AbstractCellView#getRenderer()
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	public static class WrapperPortLabelRenderer extends JPanel implements CellViewRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5603848567918969169L;

		private transient static JLabel label = new JLabel();

		private transient static PortLabelVertexRenderer portLabelRenderer = new PortLabelVertexRenderer();

		/**
		 * Cache the current graph for drawing
		 */
		protected transient JGraph graph = null;

		transient protected Color gradientColor = null;

		/** Cached hasFocus and selected value. */
		transient protected boolean hasFocus, selected, preview;

		/**
		 * Constructs a renderer that may be used to render vertices.
		 */
		public WrapperPortLabelRenderer() {
			super(new BorderLayout());
			this.add(portLabelRenderer, BorderLayout.CENTER);
			this.add(label, BorderLayout.SOUTH);
			label.setOpaque(false);
			setOpaque(false);

		}

		public Component getRendererComponent(JGraph graph, CellView view, boolean sel, boolean focus, boolean preview) {
			portLabelRenderer.getRendererComponent(graph, view, sel, focus, preview);
			label.setText(view.getCell().toString());
			this.graph = graph;
			selected = sel;
			this.preview = preview;
			hasFocus = focus;
			installAttributes(view);
			return this;
		}

		public void paint(Graphics g) {
			if ((gradientColor != null) && !preview) {
				setOpaque(false);
				Graphics2D g2d = (Graphics2D) g;
				g2d.setPaint(new GradientPaint(0, 0, getBackground(), getWidth(), getHeight(), gradientColor, true));
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}
			if (selected) {
				paintSelectionBorder(g);
			}
			super.paint(g);
		}

		protected void paintSelectionBorder(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			Stroke previousStroke = g2.getStroke();
			float[] dash = new float[] { 5f, 5f };
			g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
			g.setColor(Color.RED);
			g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
			g2.setStroke(previousStroke);
		}

		public Point2D getPerimeterPoint(VertexView view, Point2D source, Point2D p) {
			return portLabelRenderer.getPerimeterPoint(view, source, p);
		}

		protected void installAttributes(CellView view) {
			Map<?, ?> attributes = view.getAllAttributes();
			label.setVerticalAlignment(GraphConstants.getVerticalAlignment(attributes));
			label.setHorizontalAlignment(GraphConstants.getHorizontalAlignment(attributes));
			label.setVerticalTextPosition(GraphConstants.getVerticalTextPosition(attributes));
			label.setHorizontalTextPosition(GraphConstants.getHorizontalTextPosition(attributes));
			label.setFont(GraphConstants.getFont(attributes));
			gradientColor = GraphConstants.getGradientColor(attributes);
		}
	}

	/**
	 * The renderer class for instance view.
	 */
	public static class PortLabelVertexRenderer extends VertexRenderer {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2599058809422643000L;

		protected CellView[] ports = null;

		protected FontMetrics fontMetrics = null;

		public static transient int PORTLABELSPACING = 5;

		public static transient int MINIMUMHORIZONTALSPACING = 15;

		public static transient int MINIMUMVERTICALSPACING = 2;

		/**
		 * The vertical distance added to the label height to make the color
		 * backdrop extend beyond the label text
		 */
		public static transient int COLORHEIGHTBUFFER = 4;

		/**
		 * The maximum width of a label, any label more than this value in width
		 */
		public static transient int MAXLABELWIDTH = 100;

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.Component#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			selected = false;
			super.paint(g);
			g.setColor(getForeground());
			paintPortLabels(g);
		}

		public Component getRendererComponent(JGraph graph, CellView view, boolean sel, boolean focus, boolean preview) {
			// Do not display the label, the wrapper does this
			setText(null);
			// Finds the ports and install them into the renderer
			Graphics2D g = (Graphics2D) graph.getGraphics();
			if (g != null) {
				fontMetrics = g.getFontMetrics();
				Object cell = view.getCell();
				GraphModel model = graph.getModel();
				int childCount = model.getChildCount(cell);
				List<CellView> result = new ArrayList<CellView>(childCount);
				for (int i = 0; i < childCount; i++) {
					Object child = model.getChild(cell, i);
					if (model.isPort(child)) {
						CellView portView = graph.getGraphLayoutCache().getMapping(child, false);
						if (portView != null) {
							result.add(portView);
						}
					}
				}

				ports = new CellView[result.size()];
				result.toArray(ports);
			}
			Component c = super.getRendererComponent(graph, view, sel, focus, preview);
			// Do not display the label, the wrapper does this
			setText(null);
			return c;
		}

		/**
		 * Install the attributes of specified cell in this renderer instance.
		 * This means, retrieve every published key from the cells hashtable and
		 * set global variables or superclass properties accordingly.
		 * 
		 * @param view
		 *            the cell view to retrieve the attribute values from.
		 */
		protected void installAttributes(CellView view) {
			Map<?, ?> map = view.getAllAttributes();
			setIcon(GraphConstants.getIcon(map));
			setOpaque(GraphConstants.isOpaque(map));
			setBorder(GraphConstants.getBorder(map));
			setVerticalAlignment(GraphConstants.getVerticalAlignment(map));
			setHorizontalAlignment(GraphConstants.getHorizontalAlignment(map));
			bordercolor = GraphConstants.getBorderColor(map);
			borderWidth = Math.max(1, Math.round(GraphConstants.getLineWidth(map)));
			setOpaque(false);
			setFont(GraphConstants.getFont(map));
		}

		/**
		 * Draws a <code>String</code>. Its horizontal position <code>x</code>
		 * is given and its vertical position is centered on given
		 * <code>y</code>.
		 * 
		 * @param g
		 *            a <code>Graphics2D</code> to draw with
		 * @param label
		 *            a <code>String</code> to draw
		 * @param x
		 *            an offset to left edge of the bounding box of vertex
		 * @param y
		 *            an offset to center of the string
		 * @param background
		 *            the background color, if any, behind the text
		 * @param isLeftLabel
		 *            whether or not this label is on the left-hand side of the
		 *            vertex
		 */
		public static void drawPortLabel(Graphics g, String labelValue, double x, double y, Color background,
				boolean isLeftLabel) {
			FontMetrics metrics = g.getFontMetrics();
			StringTokenizer st = new StringTokenizer(labelValue, "\n");
			List<String> labels = new ArrayList<String>(st.countTokens());
			while (st.hasMoreTokens()) {
				labels.add(st.nextToken());
			}
			double height = metrics.getStringBounds(labelValue, g).getHeight();
			double i = -.5;
			for (String label : labels) {

				int sw = metrics.stringWidth(label);
				// If string is longer than allowed, concat it, replacing excess
				// with "..."
				int tries = 0;
				if (sw > MAXLABELWIDTH) {
					int dotLength = metrics.stringWidth("...");
					while ((sw > MAXLABELWIDTH) && (tries < 5)) {
						int characters = label.length();
						double stringRatio = (double) (MAXLABELWIDTH - dotLength) / (double) sw;
						int newStringLength = (int) (characters * stringRatio);
						// Shorten string with each try
						newStringLength -= tries;
						if (newStringLength < characters) {
							String newLabel = label.substring(0, newStringLength);
							newLabel += "...";
							int newStringWidth = metrics.stringWidth(newLabel);
							if (newStringWidth < MAXLABELWIDTH) {
								label = newLabel;
								sw = newStringWidth;
							}
						}
						tries++;
					}
				}
				int sh = metrics.getHeight();
				int offsetX = 0;
				if (isLeftLabel) {
					offsetX = PORTLABELSPACING;
				} else {
					offsetX = -sw - PORTLABELSPACING;
				}
				g.drawString(label, (int) x + offsetX, (int) ((y + sh / 2) + height * i));
				i += 1.0;
			}
		}

		/**
		 * Paints port labels the view.
		 * 
		 * @param g
		 *            a <code>Graphics2D</code> to draw with
		 */
		public void paintPortLabels(Graphics g) {
			if ((ports != null) && (ports.length > 0)) {
				CellView parentView = ports[0].getParentView();
				Rectangle2D bounds = GraphConstants.getBounds(parentView.getAllAttributes());
				// Get the bounds of the vertex and deduct twice the cell label
				// height plus the vertical buffer distance from it.
				double height = bounds.getHeight();
				Color oldBackground = getBackground();
				for (int i = 0; i < ports.length; i++) {
					String labelValue = ports[i].getCell().toString();
					if (labelValue == null) {
						labelValue = new String();
					}
					Point2D portOffset = GraphConstants.getOffset(ports[i].getAllAttributes());
					Color portBackground = GraphConstants.getBackground(ports[i].getAllAttributes());
					double x = 0;
					double y = 0;
					if (portOffset != null) {
						if (bounds != null) {
							// By x position should be 0 or the width of the
							// vertex
							x = portOffset.getX() * bounds.getWidth() / GraphConstants.PERMILLE;
							// The y position is the proportion of the vertex
							// height available for port label. Remember a bit
							// is reserved either end for the vertex label.
							y = portOffset.getY() * height / GraphConstants.PERMILLE;
							if (portOffset.getX() == 0) {
								drawPortLabel(g, labelValue, x, y, portBackground, true);
							} else if (portOffset.getX() == GraphConstants.PERMILLE) {
								drawPortLabel(g, labelValue, x, y, portBackground, false);
							}
						}
					}
				}
				setBackground(oldBackground);
			}
		}

		public Dimension getPreferredSize() {
			Dimension vertexPreferredSize = super.getPreferredSize();
			if ((ports != null) && (fontMetrics != null)) {
				int heightPerLabel = fontMetrics.getHeight() + MINIMUMVERTICALSPACING;
				double left = 0, right = 0;
				double maxLeft = 0, maxRight = 0;
				for (int i = 0; i < ports.length; i++) {
					String labelValue = ports[i].getCell().toString();
					if (labelValue == null) {
						labelValue = "";
					}
					StringTokenizer st = new StringTokenizer(labelValue, "\n");
					List<String> labels = new ArrayList<String>(st.countTokens());
					while (st.hasMoreTokens()) {
						labels.add(st.nextToken());
					}
					Point2D portOffset = GraphConstants.getOffset(ports[i].getAllAttributes());
					if (portOffset != null) {
						for (String label : labels) {
							int sw = fontMetrics.stringWidth(label);
							// Limit the size of labels to MAXLABELWIDTH
							if (sw > MAXLABELWIDTH) {
								sw = MAXLABELWIDTH;
							}
							if (portOffset.getX() == 0) {
								left += heightPerLabel;
								maxLeft = Math.max(maxLeft, sw);
							} else if (portOffset.getX() == GraphConstants.PERMILLE) {
								right += heightPerLabel;
								maxRight = Math.max(maxRight, sw);
							}
						}
					}
				}
				int maxX = 1 + Math.max((int) vertexPreferredSize.getWidth(),
						(2 * MAXLABELWIDTH + MINIMUMHORIZONTALSPACING));//(maxLeft + maxRight + MINIMUMHORIZONTALSPACING));
				return new Dimension(maxX, (int) Math.max(left, right));
			}
			return vertexPreferredSize;
		}
	}
}