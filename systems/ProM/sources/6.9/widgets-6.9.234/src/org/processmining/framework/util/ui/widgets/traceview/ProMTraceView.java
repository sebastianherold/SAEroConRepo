/*
 * Copyright (c) 2014 F. Mannhardt (f.mannhardt@tue.nl) Original Copyright for
 * the 'wedge' drawing code Copyright (c) 2007 Christian W. Guenther
 * (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package org.processmining.framework.util.ui.widgets.traceview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseListener;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.DefaultWedgeBuilder;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.WedgeBuilder;

import com.google.common.collect.ForwardingList;

/**
 * Displays a {@link Trace} of {@link Event}s as a sequence of wedges. This
 * implementation is similar to the class TraceView in the LogDialog package,
 * but it neither allows nor relies on {@link MouseListener}, so it may be used
 * as a rubber stamp in a {@link ListCellRenderer} of {@link JList}.
 * 
 * @author F. Mannhardt
 * 
 */
public class ProMTraceView extends JComponent {

	/**
	 * A Trace is just a sequence of events.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static interface Trace<T extends Event> extends Iterable<T> {

		/**
		 * @return name of the trace that is displayed in front of it
		 */
		String getName();

		/**
		 * @return the text color of the name label
		 */
		Color getNameColor();

		/**
		 * @return extra information about the trace that is displayed below the
		 *         name
		 */
		String getInfo();

		/**
		 * @return the text color of the info label
		 */
		Color getInfoColor();

	}

	/**
	 * An Event with labels and colors. It is safe for methods to return NULL.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static interface Event {

		/**
		 * @return the color of the wedge
		 */
		Color getWedgeColor();

		/**
		 * @return the color of the border
		 */
		Color getBorderColor();

		/**
		 * @return the label of this event
		 */
		String getLabel();

		/**
		 * @return the text color of the label
		 */
		Color getLabelColor();

		/**
		 * @return label that is displayed on top of the event
		 */
		String getTopLabel();

		/**
		 * @return the text color of the top label
		 */
		Color getTopLabelColor();

		/**
		 * @return label that is displayed in the first row below of the event
		 */
		String getBottomLabel();

		/**
		 * @return the text color of the bottom label
		 */
		Color getBottomLabelColor();

		/**
		 * @return label that is displayed in the second row below of the event
		 */
		String getBottomLabel2();

		/**
		 * @return the text color of the second bottom label
		 */
		Color getBottomLabel2Color();

	}

	public static interface SplittedEvent extends Event {

		String getUpperLabel();

		Color getUpperColor();

		String getLowerLabel();

		Color getLowerColor();

	}

	public static interface ExtendedEvent extends Event {

		Color getExtendedColor();

		String getExtendedLabel();

	}

	/**
	 * An abstract implementation of the {@link Trace} interface. This class
	 * provides an empty list, please override the {@link #delegate()} method to
	 * provide your own list.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public abstract static class AbstractTrace<T extends Event> extends ForwardingList<T> implements Trace<T> {

		public String getName() {
			return EMPTY_LABEL;
		}

		public String getInfo() {
			return EMPTY_LABEL;
		}

		public Color getNameColor() {
			return null;
		}

		public Color getInfoColor() {
			return null;
		}

		protected List<T> delegate() {
			return Collections.emptyList();
		}

	}

	/**
	 * An abstract implementation of {@link Event} that returns an empty event.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public abstract static class AbstractEvent implements Event {

		public String getLabel() {
			return EMPTY_LABEL;
		}

		public Color getWedgeColor() {
			return DEFAULT_COLOR;
		}

		public String getTopLabel() {
			return EMPTY_LABEL;
		}

		public String getBottomLabel() {
			return EMPTY_LABEL;
		}

		public String getBottomLabel2() {
			return EMPTY_LABEL;
		}

		public Color getLabelColor() {
			return null;
		}

		public Color getTopLabelColor() {
			return null;
		}

		public Color getBottomLabelColor() {
			return null;
		}

		public Color getBottomLabel2Color() {
			return null;
		}

		public Color getBorderColor() {
			return DEFAULT_BORDER_COLOR;
		}

	}

	/**
	 * A default implementation of the {@link Trace} interface.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class DefaultTrace<T extends Event> extends AbstractTrace<T> {

		private List<T> list;

		private String name;
		private String info;

		public DefaultTrace() {
			this(EMPTY_LABEL, EMPTY_LABEL);
		}

		public DefaultTrace(String name, String info) {
			this(name, info, 16);
		}

		public DefaultTrace(String name, String info, int initialCapacity) {
			super();
			this.list = new ArrayList<T>(initialCapacity);
			this.name = name;
			this.info = info;
		}

		protected List<T> delegate() {
			return list;
		}

		public String getName() {
			return name;
		}

		public String getInfo() {
			return info;
		}

	}

	/**
	 * A default implementation of the {@link Event} interface.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class DefaultEvent extends AbstractEvent {

		private final Color color;
		private final String label;
		private final String topLabel;
		private final String bottomLabel;
		private final String bottomLabel2;

		public DefaultEvent() {
			this(DEFAULT_COLOR, EMPTY_LABEL, EMPTY_LABEL, EMPTY_LABEL, EMPTY_LABEL);
		}

		public DefaultEvent(String label) {
			this(DEFAULT_COLOR, label);
		}

		public DefaultEvent(Color color, String label) {
			this(color, label, EMPTY_LABEL, EMPTY_LABEL, EMPTY_LABEL);
		}

		public DefaultEvent(Color color, String label, String topLabel, String bottomLabel, String bottomLabel2) {
			super();
			this.color = color;
			this.label = label;
			this.topLabel = topLabel;
			this.bottomLabel = bottomLabel;
			this.bottomLabel2 = bottomLabel2;
		}

		public String getLabel() {
			return label;
		}

		public Color getWedgeColor() {
			return color;
		}

		public String getTopLabel() {
			return topLabel;
		}

		public String getBottomLabel() {
			return bottomLabel;
		}

		public String getBottomLabel2() {
			return bottomLabel2;
		}

		public Color getBorderColor() {
			return null;
		}

	}

	/**
	 * Abstract base class for {@link SplittedEvent}.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static abstract class AbstractSplittedEvent extends AbstractEvent implements SplittedEvent {

		public String getUpperLabel() {
			return getLabel();
		}

		public Color getUpperColor() {
			return getWedgeColor();
		}

		public String getLowerLabel() {
			return getLabel();
		}

		public Color getLowerColor() {
			return getWedgeColor();
		}

	}

	/**
	 * Default implementation of {@link SplittedEvent}.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class DefaultSplittedEvent extends DefaultEvent implements SplittedEvent {

		private String upperLabel;
		private String lowerLabel;
		private Color upperColor;
		private Color lowerColor;

		public DefaultSplittedEvent(Color upperColor, String upperLabel, Color lowerColor, String lowerLabel) {
			super();
			this.upperColor = upperColor;
			this.upperLabel = upperLabel;
			this.lowerColor = lowerColor;
			this.lowerLabel = lowerLabel;
		}

		public DefaultSplittedEvent(Color upperColor, String upperLabel, Color lowerColor, String lowerLabel,
				String topLabel, String bottomLabel, String bottomLabel2) {
			super(null, null, topLabel, bottomLabel, bottomLabel2);
			this.upperColor = upperColor;
			this.upperLabel = upperLabel;
			this.lowerColor = lowerColor;
			this.lowerLabel = lowerLabel;
		}

		@Override
		public String getUpperLabel() {
			return upperLabel;
		}

		@Override
		public Color getUpperColor() {
			return upperColor;
		}

		@Override
		public String getLowerLabel() {
			return lowerLabel;
		}

		@Override
		public Color getLowerColor() {
			return lowerColor;
		}

	}

	public static abstract class AbstractExtendedEvent extends AbstractEvent implements ExtendedEvent {

		public Color getExtendedColor() {
			return null;
		}

		public String getExtendedLabel() {
			return null;
		}

	}

	private static final long serialVersionUID = -2403943214348555300L;

	public static final String EMPTY_LABEL = new String();

	private static final Color DEFAULT_COLOR = Color.LIGHT_GRAY;
	private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;

	private static final int WEDGE_HEIGHT = 40;
	private static final int MINIMUM_WEDGE_WIDTH = 15;

	private static final int ELEMENT_TRI_OFFSET = 6;
	private static final int ELEMENT_X_OFFSET = 5;

	private static final int LABEL_Y_OFFSET = 1;
	private static final int LABEL_X_OFFSET = 9;

	private static final int TOP_LABEL_Y_OFFSET = 0;
	private static final int TOP_LABEL_X_OFFSET = 5;

	private static final int BOTTOM_LABEL_Y_OFFSET = 0;
	private static final int BOTTOM_LABEL_X_OFFSET = 5;

	private static final int NAME_INFO_X_OFFSET = 2;

	private static final String ABBREVIATION_SUFFIX = "...";

	private final int elementHeight;
	private final int halfElementHeight;
	private final Font defaultFont;

	private Dimension cachedPreferredSize;

	private int maxInfoWidth = 85;
	private int fixedInfoWidth = -1;

	private int maxWedgeWidth = 130;
	private int fixedWedgeWidth = -1;

	private int collapsedLabelLength = 3;
	private int wedgeGap = 0;

	private float attenuationFactor = 0.9f;
	private Stroke wedgeStroke = new BasicStroke();

	private Trace<? extends Event> trace;
	private boolean isSelected;

	private WedgeBuilder wedgeBuilder;

	private FontMetrics fontMetric;

	/**
	 * Creates a new instance of {@link ProMTraceView} that paints a
	 * {@link Trace} consisting of {@link Event}'s. You need to call the method
	 * {@link #setTrace(Trace, boolean)} to actually display something. That is
	 * because this class is used together with {@link JList} and, therefore,
	 * the displayed {@link Trace} needs to be changeable.
	 */
	public ProMTraceView() {
		this(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
	}

	public ProMTraceView(Font font) {
		this(font, true);
	}

	/**
	 * Creates a new instance of {@link ProMTraceView} that paints a
	 * {@link Trace} consisting of {@link Event}'s. You need to call the method
	 * {@link #setTrace(Trace, boolean)} to actually display something. That is
	 * because this class is used together with {@link JList} and, therefore,
	 * the displayed {@link Trace} needs to be changeable.
	 * 
	 * @param colorBuilder
	 * 
	 * @param font
	 *            the font to be used
	 * @param hasLabels
	 *            whether the wedge has labels
	 */
	public ProMTraceView(Font font, boolean hasLabels) {
		this.setWedgeBuilder(new DefaultWedgeBuilder());
		this.isSelected = false;
		this.defaultFont = font;
		this.trace = new DefaultTrace<Event>();
		this.fontMetric = getFontMetrics(getDefaultFont());
		if (hasLabels) {
			this.elementHeight = 40 + fontMetric.getHeight() * 3 + 5;
		} else {
			this.elementHeight = 40 + 25;
		}
		this.halfElementHeight = elementHeight / 2;
		updatePreferredSize();
		setDoubleBuffered(true);
		setOpaque(false);
	}

	public boolean isOpaque() {
		return false;
	}

	/**
	 * Updates the currently displayed trace
	 * 
	 * @param trace
	 */
	public void setTrace(Trace<? extends Event> trace) {
		this.trace = trace;
	}

	/**
	 * Updates the selection status of the currently displayed trace
	 * 
	 * @param isSelected
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}
	
	public boolean isSelected() {
		return isSelected;
	}

	public Trace<? extends Event> getTrace() {
		return trace;
	}

	public void updatePreferredSize() {
		this.cachedPreferredSize = calcPreferredSize();
	}

	public void updatePreferredSize(Dimension size) {
		this.cachedPreferredSize = size;
	}

	protected void paintChildren(Graphics g) {
		// No op
	}

	protected void paintBorder(Graphics g) {
		//No op
	}

	protected void printComponent(Graphics g) {
		boolean wasSelected = isSelected;
		try {
			isSelected = true;
			super.printComponent(g);
		} finally {
			isSelected = wasSelected;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics g) {

		Iterator<? extends Event> iterator = trace.iterator();

		if (iterator.hasNext()) {
			Graphics2D g2d = (Graphics2D) g;
			g2d.setFont(getDefaultFont());
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

			FontMetrics fontMetrics = g2d.getFontMetrics();
			int fontHeight = fontMetrics.getHeight();
			int nameInfoWidth = getNameInfoWidth(fontMetrics);

			Dimension preferredSize = getPreferredSize();
			Rectangle visibleRegion = g.getClipBounds();
			Rectangle completeRegion = new Rectangle(preferredSize.width, preferredSize.height);

			int wedgeYOffset = fontHeight + TOP_LABEL_Y_OFFSET;
			int y = completeRegion.y + wedgeYOffset + 2;
			int x = completeRegion.x;

			// draw name / info			
			x += NAME_INFO_X_OFFSET;

			if (isInVisibleRegion(visibleRegion, x, nameInfoWidth + 2 * NAME_INFO_X_OFFSET)) {
				if (!isEmpty(trace.getInfo()) && !isEmpty(trace.getName())) {
					drawTraceLabel(g2d, x, y + fontHeight, wedgeBuilder.buildNameColor(trace), trace.getName());
					drawTraceLabel(g2d, x, y + 2 * fontHeight, wedgeBuilder.buildInfoColor(trace), trace.getInfo());
				} else if (!isEmpty(trace.getName())) {
					drawTraceLabel(g2d, x, (int) (y + 1.5 * fontHeight), wedgeBuilder.buildNameColor(trace),
							trace.getName());
				}
			}

			// draw events			
			x += NAME_INFO_X_OFFSET;
			x += nameInfoWidth;
			x += ELEMENT_X_OFFSET;

			boolean hasPainted = false;

			while (iterator.hasNext()) {
				Event e = iterator.next();

				// position & wedge width calculations
				int currentEventWidth;
				int wedgeGap = getWedgeGap(trace, e);
				if (isSelected) {
					currentEventWidth = getExpandedEventWidth(e, fontMetrics);
				} else {
					currentEventWidth = getCollapsedEventWidth(e, fontMetrics);
				}

				// check if we should paint
				if (isInVisibleRegion(visibleRegion, x, currentEventWidth + wedgeGap)) {

					// color & stroke
					Color color = nullSafeColor(wedgeBuilder.buildWedgeColor(trace, e));
					Color strokeColor = nullSafeColor(wedgeBuilder.buildBorderColor(trace, e));
					Stroke stroke = getWedgeStroke(trace, e);

					// actual painting
					if (isSelected) {
						drawEventWedge(g2d, e, color, strokeColor, stroke, x, wedgeYOffset, currentEventWidth,
								WEDGE_HEIGHT, isSelected);

						if (e instanceof SplittedEvent) {
							SplittedEvent splittedEvent = (SplittedEvent) e;
							if (!isEmpty(splittedEvent.getUpperLabel())) {
								drawFullLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color,
										splittedEvent.getUpperLabel(), x, wedgeYOffset, currentEventWidth,
										halfElementHeight);
							}
							if (!isEmpty(splittedEvent.getLowerLabel())) {
								drawFullLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color,
										splittedEvent.getLowerLabel(), x,
										wedgeYOffset + 2 * fontMetrics.getAscent() - 2, currentEventWidth,
										halfElementHeight);
							}
						} else {
							if (!isEmpty(e.getLabel())) {
								drawFullLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color, e.getLabel(), x,
										wedgeYOffset, currentEventWidth, elementHeight);
							}
						}

						if (e instanceof ExtendedEvent) {
							ExtendedEvent extendedEvent = (ExtendedEvent) e;
							drawExtendedInfo(g2d, extendedEvent.getExtendedColor(), extendedEvent.getExtendedLabel(), x,
									TOP_LABEL_Y_OFFSET, currentEventWidth);
						}

						if (!isEmpty(e.getTopLabel()) && !(e instanceof ExtendedEvent)) {
							drawExtraLabel(g2d, wedgeBuilder.buildTopLabelColor(trace, e), e.getTopLabel(),
									x + TOP_LABEL_X_OFFSET, TOP_LABEL_Y_OFFSET, currentEventWidth);
						}
						if (!isEmpty(e.getBottomLabel())) {
							drawExtraLabel(g2d, wedgeBuilder.buildBottomLabelColor(trace, e), e.getBottomLabel(),
									x + BOTTOM_LABEL_X_OFFSET, BOTTOM_LABEL_Y_OFFSET + WEDGE_HEIGHT + wedgeYOffset,
									currentEventWidth);
						}
						if (!isEmpty(e.getBottomLabel2())) {
							drawExtraLabel(g2d, wedgeBuilder.buildBottom2LabelColor(trace, e), e.getBottomLabel2(),
									x + BOTTOM_LABEL_X_OFFSET,
									BOTTOM_LABEL_Y_OFFSET + WEDGE_HEIGHT + wedgeYOffset + fontHeight,
									currentEventWidth);
						}
					} else {
						drawEventWedge(g2d, e, color, strokeColor, stroke, x, wedgeYOffset, currentEventWidth,
								WEDGE_HEIGHT, isSelected);

						if (e instanceof SplittedEvent) {
							SplittedEvent splittedEvent = (SplittedEvent) e;
							if (!isEmpty(splittedEvent.getUpperLabel())) {
								drawCollapsedLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color,
										splittedEvent.getUpperLabel(), x, wedgeYOffset + fontMetrics.getAscent());
							}
							if (!isEmpty(splittedEvent.getLowerLabel())) {
								drawCollapsedLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color,
										splittedEvent.getLowerLabel(), x,
										wedgeYOffset + 3 * fontMetrics.getAscent() - 2);
							}
						} else {
							if (!isEmpty(e.getLabel())) {
								drawCollapsedLabel(g2d, wedgeBuilder.buildLabelColor(trace, e), color, e.getLabel(), x,
										wedgeYOffset + (WEDGE_HEIGHT / 2) + (int) (fontMetrics.getAscent() / 1.5f)
												- fontMetrics.getLeading() - fontMetrics.getDescent());
							}
						}

						if (e instanceof ExtendedEvent) {
							ExtendedEvent extendedEvent = (ExtendedEvent) e;
							drawCollapseExtendedInfo(g2d, extendedEvent.getExtendedColor(),
									extendedEvent.getExtendedLabel(), x, TOP_LABEL_Y_OFFSET, currentEventWidth);
						}

					}

					hasPainted = true;

				} else {
					if (hasPainted) {
						break;
					}
				}

				x += currentEventWidth + wedgeGap;
			}
		}
	}

	private static boolean isInVisibleRegion(Rectangle visibleRegion, int x, int elementWidth) {
		int partlyVisibleHorizon = elementWidth;
		int startX = visibleRegion.x;
		int endX = visibleRegion.x + visibleRegion.width;
		return x + partlyVisibleHorizon > startX && x - partlyVisibleHorizon < endX;
	}

	protected final int translateToEventIndex(Point point) {
		Iterator<? extends Event> iterator = trace.iterator();
		FontMetrics fontMetrics = getFontMetrics(getDefaultFont());
		int offsetX = NAME_INFO_X_OFFSET + getNameInfoWidth(fontMetrics) + ELEMENT_X_OFFSET + ELEMENT_TRI_OFFSET;
		int currentX = offsetX;
		int lastX = offsetX;
		int currentEventIndex = 0;
		while (iterator.hasNext() && point.getX() > currentX) {
			Event e = iterator.next();
			if (isSelected) {
				currentX += getExpandedEventWidth(e, fontMetrics);
			} else {
				currentX += getCollapsedEventWidth(e, fontMetrics);
			}
			if (point.getX() > lastX && point.getX() < currentX) {
				return currentEventIndex;
			}
			currentX += getWedgeGap(trace, e);
			lastX = currentX;
			currentEventIndex++;
		}
		return -1;
	}

	/**
	 * 
	 * @param point
	 * @return positive: event index; -1: out of trace; -2: name of trace
	 */
	protected final int translateToDetailedEventIndex(Point point) {
		Iterator<? extends Event> iterator = trace.iterator();
		FontMetrics fontMetrics = getFontMetrics(getDefaultFont());
		int offsetX = NAME_INFO_X_OFFSET + getNameInfoWidth(fontMetrics) + ELEMENT_X_OFFSET + ELEMENT_TRI_OFFSET;
		int currentX = offsetX;
		int lastX = offsetX;
		int currentEventIndex = 0;
		if (point.getX() <= currentX) {
			return -2;
		}
		while (iterator.hasNext() && point.getX() > currentX) {
			Event e = iterator.next();
			if (isSelected) {
				currentX += getExpandedEventWidth(e, fontMetrics);
			} else {
				currentX += getCollapsedEventWidth(e, fontMetrics);
			}
			if (point.getX() > lastX && point.getX() < currentX) {
				return currentEventIndex;
			}
			currentX += getWedgeGap(trace, e);
			lastX = currentX;
			currentEventIndex++;
		}
		return -1;
	}

	private Stroke getWedgeStroke(Trace<? extends Event> trace, Event e) {
		Stroke stroke = wedgeBuilder.buildBorderStroke(trace, e);
		if (stroke != null) {
			return stroke;
		}
		return wedgeStroke;
	}

	private int getWedgeGap(Trace<? extends Event> trace, Event e) {
		Integer gap = wedgeBuilder.assignWedgeGap(trace, e);
		if (gap != null) {
			return gap;
		}
		return this.wedgeGap;
	}

	private static boolean isEmpty(String s) {
		return s == null || s == EMPTY_LABEL || s.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMaximumSize()
	 */
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE, elementHeight);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return cachedPreferredSize;
	}

	private final Dimension calcPreferredSize() {
		FontMetrics fontMetrics = fontMetric;
		int nameWidth = NAME_INFO_X_OFFSET + getNameInfoWidth(fontMetrics);
		int traceWidth = ELEMENT_X_OFFSET + ELEMENT_TRI_OFFSET + getSelectedTraceWidth(fontMetrics);
		return new Dimension(nameWidth + traceWidth + 5, elementHeight); // do not know where the 5 comes from
	}

	private int getNameInfoWidth(FontMetrics metrics) {
		if (getFixedInfoWidth() != -1) {
			return getFixedInfoWidth();
		} else {
			return Math.min(getMaxInfoWidth(),
					Math.max(nullSafeWidth(metrics, trace.getName()), nullSafeWidth(metrics, trace.getInfo())));
		}
	}

	private final int getSelectedTraceWidth(FontMetrics metrics) {
		if (getFixedWedgeWidth() != -1) {
			if (trace instanceof Collection) {
				return (getFixedWedgeWidth() + wedgeGap) * ((Collection<?>) trace).size();
			} else {
				Iterator<? extends Event> iterator = trace.iterator();
				if (iterator.hasNext()) {
					int width = 0;
					for (; iterator.hasNext();) {
						iterator.next();
						width += getFixedWedgeWidth();
						width += wedgeGap;
					}
					return width;
				} else {
					return MINIMUM_WEDGE_WIDTH;
				}
			}
		} else {
			Iterator<? extends Event> iterator = trace.iterator();
			if (iterator.hasNext()) {
				int width = 0;
				for (; iterator.hasNext();) {
					Event e = iterator.next();
					width += getExpandedEventWidth(e, metrics);
					width += getWedgeGap(trace, e);
				}
				return width;
			} else {
				return MINIMUM_WEDGE_WIDTH;
			}
		}
	}

	private final int getExpandedEventWidth(final Event e, FontMetrics metrics) {
		if (getFixedWedgeWidth() != -1) {
			return Math.max(getFixedWedgeWidth(), MINIMUM_WEDGE_WIDTH);
		} else {
			int width = nullSafeWidth(metrics, e.getLabel()) + LABEL_X_OFFSET;
			width = Math.max(width, nullSafeWidth(metrics, e.getTopLabel()) + BOTTOM_LABEL_X_OFFSET);
			width = Math.max(width, nullSafeWidth(metrics, e.getBottomLabel()) + BOTTOM_LABEL_X_OFFSET);
			width = Math.max(width, nullSafeWidth(metrics, e.getBottomLabel2()) + BOTTOM_LABEL_X_OFFSET);

			if (e instanceof SplittedEvent) {
				width = Math.max(width, nullSafeWidth(metrics, ((SplittedEvent) e).getUpperLabel()) + LABEL_X_OFFSET);
				width = Math.max(width, nullSafeWidth(metrics, ((SplittedEvent) e).getLowerLabel()) + LABEL_X_OFFSET);
			}

			return Math.max(Math.min(maxWedgeWidth, width), MINIMUM_WEDGE_WIDTH);
		}
	}

	private int getCollapsedEventWidth(Event e, FontMetrics metrics) {
		int width = Math.max(MINIMUM_WEDGE_WIDTH, nullSafeWidth(metrics, shortenLabel(e.getLabel()) + LABEL_X_OFFSET));
		if (e instanceof SplittedEvent) {
			width = Math.max(width,
					nullSafeWidth(metrics, shortenLabel(((SplittedEvent) e).getUpperLabel())) + LABEL_X_OFFSET);
			width = Math.max(width,
					nullSafeWidth(metrics, shortenLabel(((SplittedEvent) e).getLowerLabel())) + LABEL_X_OFFSET);
		}
		return width;
	}

	private void drawCollapseExtendedInfo(Graphics2D g2d, Color extendedColor, String extendedLabel, int x, int y,
			int width) {
		if (extendedColor != null) {
			g2d.setColor(extendedColor);
			g2d.fillRect(x, y, width, g2d.getFontMetrics().getHeight() - 4);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(x, y, width, g2d.getFontMetrics().getHeight() - 4);
			g2d.setColor(determineFontColor(getForeground(), extendedColor));
		}

		if (extendedLabel != null) {
			g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize() - 2.0f));
			try {
				FontMetrics fontMetrics = g2d.getFontMetrics();
				g2d.setColor(determineFontColor(getForeground(), extendedColor));
				if (fontMetrics.stringWidth(extendedLabel) <= width) {
					g2d.drawString(extendedLabel, x, y + fontMetrics.getAscent());
				} else {
					char[] charArray = extendedLabel.toCharArray();
					int cutoffPoint = extendedLabel.length();
					for (int i = extendedLabel.length(); i > 0; i--) {
						if (fontMetrics.charsWidth(charArray, 0, i) <= width) {
							cutoffPoint = i;
							break;
						}
					}
					g2d.drawString(extendedLabel.substring(0, Math.max(0, cutoffPoint - 3)).concat(ABBREVIATION_SUFFIX),
							x, y + fontMetrics.getAscent());
				}
			} finally {
				g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize() + 2.0f));
			}
		}
	}

	private void drawExtendedInfo(Graphics2D g2d, Color extendedColor, String extendedLabel, int x, int y, int width) {
		if (extendedColor != null) {
			g2d.setColor(extendedColor);
			g2d.fillRect(x, y, width, g2d.getFontMetrics().getHeight() - 4);
			g2d.setColor(Color.BLACK);
			g2d.drawRect(x, y, width, g2d.getFontMetrics().getHeight() - 4);
			g2d.setColor(determineFontColor(getForeground(), extendedColor));
		}

		if (extendedLabel != null) {
			g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize() - 2.0f));
			try {
				FontMetrics fontMetrics = g2d.getFontMetrics();
				if (fontMetrics.stringWidth(extendedLabel) <= width) {
					g2d.drawString(extendedLabel, x, y + fontMetrics.getAscent());
				} else {
					char[] charArray = extendedLabel.toCharArray();
					int cutoffPoint = extendedLabel.length();
					for (int i = extendedLabel.length(); i > 0; i--) {
						if (fontMetrics.charsWidth(charArray, 0, i) <= width) {
							cutoffPoint = i;
							break;
						}
					}
					g2d.drawString(extendedLabel.substring(0, Math.max(0, cutoffPoint - 3)).concat(ABBREVIATION_SUFFIX),
							x, y + fontMetrics.getAscent());
				}
			} finally {
				g2d.setFont(g2d.getFont().deriveFont(g2d.getFont().getSize() + 2.0f));
			}
		}
	}

	private final void drawEventWedge(final Graphics2D g, Event e, final Color color, final Color strokeColor,
			Stroke wedgeStroke, int x, int y, int width, int height, boolean isSelected) {

		int midPointBX = x + ELEMENT_TRI_OFFSET;
		int midPointAX = x + width + ELEMENT_TRI_OFFSET;
		int midPointY = y + (height / 2);
		int endPointX = x + width;
		int endPointY = y + height;

		if (e instanceof SplittedEvent) {
			SplittedEvent splittedEvent = (SplittedEvent) e;

			int[] xUpper = new int[] { x, endPointX, midPointAX, midPointBX };
			int[] yUpper = new int[] { y, y, midPointY, midPointY };

			g.setColor(isSelected ? splittedEvent.getUpperColor() : attenuateColor(splittedEvent.getUpperColor()));
			g.fillPolygon(xUpper, yUpper, 4);

			int[] xLower = new int[] { midPointBX, x, endPointX, midPointAX };
			int[] yLower = new int[] { midPointY, endPointY, endPointY, midPointY };

			g.setColor(isSelected ? splittedEvent.getLowerColor() : attenuateColor(splittedEvent.getLowerColor()));
			g.fillPolygon(xLower, yLower, 4);

		} else {

			int[] xCoords = new int[] { x, endPointX, midPointAX, endPointX, x, midPointBX };
			int[] yCoords = new int[] { y, y, midPointY, endPointY, endPointY, midPointY };

			g.setColor(isSelected ? color : attenuateColor(color));
			g.fillPolygon(xCoords, yCoords, 6);
		}

		g.setColor(strokeColor);

		Stroke oldStroke = g.getStroke();
		try {
			g.setStroke(wedgeStroke);

			// Start Wedge
			g.drawPolyline(new int[] { x, midPointBX, x }, new int[] { y, midPointY, endPointY }, 3);
			// End Wedge
			g.drawPolyline(new int[] { endPointX, midPointAX, endPointX }, new int[] { y, midPointY, endPointY }, 3);

			// Upper line
			g.drawLine(x, y, endPointX, y);
			// Lower line
			g.drawLine(x, endPointY, endPointX, endPointY);
		} finally {
			g.setStroke(oldStroke);
		}
	}

	private final void drawTraceLabel(Graphics2D g2d, int x, int y, Color color, String label) {
		g2d.setColor(color == null ? getForeground() : color);

		FontMetrics fontMetrics = g2d.getFontMetrics();
		int wedgeWidth = getNameInfoWidth(fontMetrics);
		if (wedgeWidth != -1) {
			if (fontMetrics.stringWidth(label) <= wedgeWidth) {
				g2d.drawString(label, x, y);
			} else {
				char[] charArray = label.toCharArray();
				int cutoffPoint = label.length();
				for (int i = label.length(); i > 0; i--) {
					if (fontMetrics.charsWidth(charArray, 0, i) <= wedgeWidth) {
						cutoffPoint = i;
						break;
					}
				}
				g2d.drawString(label.substring(0, Math.max(0, cutoffPoint - 3)).concat(ABBREVIATION_SUFFIX), x, y);
			}
		} else {
			g2d.drawString(label, x, y);
		}

	}

	private final void drawCollapsedLabel(final Graphics2D g, Color textColor, Color bgColor, final String label, int x,
			int y) {
		String shortenLabel = shortenLabel(label);
		if (shortenLabel != null) {
			g.setColor(determineFontColor(textColor, bgColor));
			g.drawString(shortenLabel, x + LABEL_X_OFFSET, y + LABEL_Y_OFFSET);
		}
	}

	private String shortenLabel(final String label) {
		if (label != null) {
			return label.substring(0, Math.min(collapsedLabelLength, label.length()));
		} else {
			return null;
		}
	}

	private final void drawFullLabel(final Graphics2D g, final Color textColor, final Color bgColor, final String label,
			int x, int y, int width, int maxHeight) {

		g.setColor(determineFontColor(textColor, bgColor));
		int wrappingWidth = width - LABEL_X_OFFSET;

		FontMetrics fontMetrics = g.getFontMetrics();
		if (fontMetrics.stringWidth(label) <= wrappingWidth) {
			// Shortcut the expensive text wrapping
			if (maxHeight == elementHeight) {
				// somehow get it centered (try and error calculation)
				g.drawString(label, x + LABEL_X_OFFSET, y + (WEDGE_HEIGHT / 2) + (fontMetrics.getAscent() / 2));
			} else {
				g.drawString(label, x + LABEL_X_OFFSET, y + LABEL_Y_OFFSET + fontMetrics.getAscent());
			}
		} else {
			AttributedString attributedString = new AttributedString(label);
			attributedString.addAttribute(TextAttribute.FONT, g.getFont());
			LineBreakMeasurer breakMeasure = new LineBreakMeasurer(attributedString.getIterator(),
					g.getFontRenderContext());

			int labelX = x + LABEL_X_OFFSET;
			int labelY = y + LABEL_Y_OFFSET;

			while (breakMeasure.getPosition() < label.length()) {

				int positionBefore = breakMeasure.getPosition();
				int positionAfter = breakMeasure.nextOffset(wrappingWidth);

				TextLayout layout = breakMeasure.nextLayout(wrappingWidth);
				float textHeight = layout.getDescent() + layout.getLeading() + layout.getAscent();

				if ((labelY + (4 * textHeight)) > maxHeight) {
					// Last Row				
					String shortenedLastRow = label
							.substring(positionBefore, Math.max(positionBefore, positionAfter - 3))
							.concat(ABBREVIATION_SUFFIX);
					AttributedString attrLastRow = new AttributedString(shortenedLastRow);
					attrLastRow.addAttribute(TextAttribute.FONT, g.getFont());
					TextLayout lastRowLayout = new TextLayout(attrLastRow.getIterator(), g.getFontRenderContext());
					float dx = lastRowLayout.isLeftToRight() ? 0 : (wrappingWidth - lastRowLayout.getAdvance());
					lastRowLayout.draw(g, labelX + dx, labelY + lastRowLayout.getAscent());
					break;
				}

				labelY += layout.getAscent();
				float dx = layout.isLeftToRight() ? 0 : (wrappingWidth - layout.getAdvance());
				layout.draw(g, labelX + dx, labelY);
				labelY += layout.getDescent() + layout.getLeading();
			}
		}

	}

	private void drawExtraLabel(Graphics2D g, Color color, String label, int x, int y, int width) {
		g.setColor(color != null ? color : getForeground());

		FontMetrics fontMetrics = g.getFontMetrics();
		if (fontMetrics.stringWidth(label) <= width) {
			g.drawString(label, x, y + fontMetrics.getAscent());
		} else {
			char[] charArray = label.toCharArray();
			int cutoffPoint = label.length();
			for (int i = label.length(); i > 0; i--) {
				if (fontMetrics.charsWidth(charArray, 0, i) <= width) {
					cutoffPoint = i;
					break;
				}
			}
			g.drawString(label.substring(0, Math.max(0, cutoffPoint - 3)).concat(ABBREVIATION_SUFFIX), x,
					y + fontMetrics.getAscent());
		}
	}

	private final Color attenuateColor(final Color c) {
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hsb[2] = hsb[2] * getAttenuationFactor();
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
	}

	private static final Color determineFontColor(final Color textColor, final Color bgColor) {
		if (textColor == null) {
			double val = Math.sqrt(.299 * Math.pow(bgColor.getRed(), 2) + .587 * Math.pow(bgColor.getGreen(), 2)
					+ .114 * Math.pow(bgColor.getBlue(), 2));
			return (val < 130) ? Color.WHITE : Color.BLACK;
		} else {
			return textColor;
		}
	}

	private static int nullSafeWidth(FontMetrics fontMetrics, String s) {
		return s != null ? fontMetrics.stringWidth(s) : 0;
	}

	private Color nullSafeColor(Color color) {
		return color == null ? Color.BLACK : color;
	}

	public float getAttenuationFactor() {
		return attenuationFactor;
	}

	public void setAttenuationFactor(float attenuationFactor) {
		this.attenuationFactor = attenuationFactor;
	}

	public Font getDefaultFont() {
		return defaultFont;
	}

	public WedgeBuilder getWedgeBuilder() {
		return wedgeBuilder;
	}

	public void setWedgeBuilder(WedgeBuilder colorBuilder) {
		this.wedgeBuilder = colorBuilder;
	}

	public int getMaxWedgeWidth() {
		return maxWedgeWidth;
	}

	public void setMaxWedgeWidth(int maxWedgeWidth) {
		this.maxWedgeWidth = maxWedgeWidth;
	}

	public int getCollapsedLabelLength() {
		return collapsedLabelLength;
	}

	public void setCollapsedLabelLength(int collapsedLabelLength) {
		this.collapsedLabelLength = collapsedLabelLength;
	}

	public Stroke getWedgeStroke() {
		return wedgeStroke;
	}

	public void setWedgeStroke(Stroke wedgeStroke) {
		this.wedgeStroke = wedgeStroke;
	}

	public int getWedgeGap() {
		return wedgeGap;
	}

	public void setWedgeGap(int wedgeGap) {
		this.wedgeGap = wedgeGap;
	}

	public boolean isFixedWedgeWidth() {
		return getFixedWedgeWidth() != -1;
	}

	public void setFixedWedgeWidth(int fixedWedgeWidth) {
		this.fixedWedgeWidth = fixedWedgeWidth;
	}

	public int getFixedWedgeWidth() {
		return fixedWedgeWidth;
	}

	public int getMaxInfoWidth() {
		return maxInfoWidth;
	}

	public void setMaxInfoWidth(int maxInfoWidth) {
		this.maxInfoWidth = maxInfoWidth;
	}

	public boolean isFixedInfoWidth() {
		return getFixedInfoWidth() != -1;
	}

	public int getFixedInfoWidth() {
		return fixedInfoWidth;
	}

	public void setFixedInfoWidth(int fixedInfoWidth) {
		this.fixedInfoWidth = fixedInfoWidth;
	}

}