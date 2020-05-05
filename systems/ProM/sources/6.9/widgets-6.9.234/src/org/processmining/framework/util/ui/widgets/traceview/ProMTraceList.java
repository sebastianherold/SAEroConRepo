/*
 * Copyright (c) 2014 F. Mannhardt (f.mannhardt@tue.nl)
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionListener;

import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.MultiPageDocument;
import org.freehep.graphicsio.PageConstants;
import org.freehep.graphicsio.VectorGraphicsIO;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.ps.EPSGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.processmining.framework.util.ui.widgets.ProMScrollPane;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.DefaultEvent;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.DefaultTrace;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Event;
import org.processmining.framework.util.ui.widgets.traceview.ProMTraceView.Trace;
import org.processmining.framework.util.ui.widgets.traceview.model.FilteredListModel;
import org.processmining.framework.util.ui.widgets.traceview.model.FilteredListModelImpl;
import org.processmining.framework.util.ui.widgets.traceview.model.FilteredListModelImpl.ListModelFilter;
import org.processmining.framework.util.ui.widgets.traceview.model.MutableListModel;
import org.processmining.framework.util.ui.widgets.traceview.model.SortableListModel;
import org.processmining.framework.util.ui.widgets.traceview.model.SortableListModelImpl;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * A {@link JPanel} that displays a list of arbitrary objects that are
 * visualized as "wedges" using {@link ProMTraceView}. It uses a {@link JList}
 * under the hood, which can be accessed directly using {@link #getList()}.
 * <p>
 * To use this component you need to create a {@link TraceBuilder} that takes
 * any of your objects and returns a {@link Trace} of {@link Event}s. This
 * conversion happens on-the-fly and only for the objects that are currently
 * shown to the user. Therefore, the performance overhead should be small.
 * <p>
 * There are default implementations {@link DefaultTrace} and
 * {@link DefaultEvent} available for convenience. If your class already
 * implements {@link Trace}, then use the provided {@link NoOpTraceBuilder}.
 * <p>
 * It is possible to sort and filter the list by using an appropriate
 * {@link ListModel}. Such as {@link SortableListModelImpl} or
 * {@link FilteredListModelImpl}.
 * <p>
 * It is also possible to listen for click, selection and mouse movement events
 * on an element of the list by using
 * {@link #addTraceClickListener(ClickListener)}.
 * {@link #addTraceSelectionListener(ListSelectionListener)}, as well as
 * {@link #addTraceMoveListener(MoveListener)}.
 * 
 * @author F. Mannhardt
 * 
 * @param <T>
 *            the class of objects that are to be visualized
 */
public class ProMTraceList<T> extends JPanel {

	public interface ClickListener<T> extends EventListener {

		/**
		 * Event that the user has double clicked on a trace.
		 * 
		 * @param trace
		 *            the object that has been clicked on
		 * @param traceIndex
		 *            the index of the trace in the list
		 * @param eventIndex
		 *            the index of the event that has been clicked on or -1 in
		 *            case not event was below the mouse pointer
		 * @param e
		 *            the original {@link MouseEvent}
		 */
		public void traceMouseDoubleClicked(T trace, int traceIndex, int eventIndex, MouseEvent e);

		/**
		 * Event that the user has clicked on a trace.
		 * 
		 * @param trace
		 *            the object that has been clicked on
		 * @param traceIndex
		 *            the index of the trace in the list
		 * @param eventIndex
		 *            the index of the event that has been clicked on or -1 in
		 *            case not event was below the mouse pointer
		 * @param e
		 *            the original {@link MouseEvent}
		 */
		public void traceMouseClicked(T trace, int traceIndex, int eventIndex, MouseEvent e);

	}

	public interface MoveListener<T> extends EventListener {

		/**
		 * Event that the user has moved the mouse over a trace.
		 * 
		 * @param trace
		 *            the object that has been moved over
		 * @param traceIndex
		 *            the index of the trace in the list
		 * @param eventIndex
		 *            the index of the event or -1 in case no event was below
		 *            the mouse pointer
		 * @param e
		 *            the original {@link MouseEvent}
		 */
		public void traceMouseMoved(T trace, int traceIndex, int eventIndex, MouseEvent e);

	}

	/**
	 * Builds {@link Trace} objects for object of class T.
	 * 
	 * @author F. Mannhardt
	 * 
	 * @param <T>
	 *            class of object that is converted to a {@link Trace}
	 */
	public interface TraceBuilder<T> {

		/**
		 * @param element
		 * @return a {@link Trace}
		 */
		Trace<? extends Event> build(T element);

	}

	/**
	 * Can be used to override the default coloring of an {@link Event} based on
	 * some other information. If only one of the colors needs to be overridden,
	 * please use {@link DefaultWedgeBuilder}.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public interface WedgeBuilder {

		Color buildNameColor(Trace<? extends Event> trace);

		Color buildInfoColor(Trace<? extends Event> trace);

		Color buildWedgeColor(Trace<? extends Event> trace, Event event);

		Integer assignWedgeGap(Trace<? extends Event> trace, Event event);

		Stroke buildBorderStroke(Trace<? extends Event> trace, Event event);

		Color buildBorderColor(Trace<? extends Event> trace, Event event);

		Color buildLabelColor(Trace<? extends Event> trace, Event event);

		Color buildTopLabelColor(Trace<? extends Event> trace, Event event);

		Color buildBottomLabelColor(Trace<? extends Event> trace, Event event);

		Color buildBottom2LabelColor(Trace<? extends Event> trace, Event event);

	}

	/**
	 * {@link WedgeBuilder} that simply returns the original color of an
	 * {@link Event}. Override single methods to change the color.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class DefaultWedgeBuilder implements WedgeBuilder {

		public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
			return event.getWedgeColor();
		}

		public Color buildLabelColor(Trace<? extends Event> trace, Event event) {
			return event.getLabelColor();
		}

		public Color buildTopLabelColor(Trace<? extends Event> trace, Event event) {
			return event.getTopLabelColor();
		}

		public Color buildBottomLabelColor(Trace<? extends Event> trace, Event event) {
			return event.getBottomLabelColor();
		}

		public Color buildBottom2LabelColor(Trace<? extends Event> trace, Event event) {
			return event.getBottomLabel2Color();
		}

		public Color buildBorderColor(Trace<? extends Event> trace, Event event) {
			return event.getBorderColor();
		}

		public Integer assignWedgeGap(Trace<? extends Event> trace, Event event) {
			return null;
		}

		public Stroke buildBorderStroke(Trace<? extends Event> trace, Event e) {
			return null;
		}

		public Color buildNameColor(Trace<? extends Event> trace) {
			return trace.getNameColor();
		}

		public Color buildInfoColor(Trace<? extends Event> trace) {
			return trace.getInfoColor();
		}

	}

	/**
	 * {@link WedgeBuilder} that delegates all methods to another
	 * {@link WedgeBuilder}.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class DelegateWedgeBuilder extends DefaultWedgeBuilder {

		private final WedgeBuilder wedgeBuilder;

		public DelegateWedgeBuilder(WedgeBuilder wedgeBuilder) {
			this.wedgeBuilder = wedgeBuilder;
		}

		public Color buildWedgeColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildWedgeColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildWedgeColor(trace, event);
		}

		public Color buildLabelColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildLabelColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildLabelColor(trace, event);
		}

		public Color buildTopLabelColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildTopLabelColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildTopLabelColor(trace, event);
		}

		public Color buildBottomLabelColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildBottomLabelColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildBottomLabelColor(trace, event);
		}

		public Color buildBottom2LabelColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildBottom2LabelColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildBottom2LabelColor(trace, event);
		}

		public Color buildBorderColor(Trace<? extends Event> trace, Event event) {
			Color color = wedgeBuilder.buildBorderColor(trace, event);
			if (color != null) {
				return color;
			}
			return super.buildBorderColor(trace, event);
		}

		public Integer assignWedgeGap(Trace<? extends Event> trace, Event event) {
			Integer wedgeGap = wedgeBuilder.assignWedgeGap(trace, event);
			if (wedgeGap != null) {
				return wedgeGap;
			}
			return super.assignWedgeGap(trace, event);
		}

		public Stroke buildBorderStroke(Trace<? extends Event> trace, Event e) {
			Stroke wedgeStroke = wedgeBuilder.buildBorderStroke(trace, e);
			if (wedgeStroke != null) {
				return wedgeStroke;
			}
			return super.buildBorderStroke(trace, e);
		}

		public Color buildNameColor(Trace<? extends Event> trace) {
			Color color = wedgeBuilder.buildNameColor(trace);
			if (color != null) {
				return color;
			}
			return super.buildNameColor(trace);
		}

		public Color buildInfoColor(Trace<? extends Event> trace) {
			Color color = wedgeBuilder.buildInfoColor(trace);
			if (color != null) {
				return color;
			}
			return super.buildInfoColor(trace);
		}

	}

	/**
	 * TraceBuilder that just returns a class, which already implements
	 * {@link Trace}.
	 * 
	 * @author F. Mannhardt
	 * 
	 */
	public static class NoOpTraceBuilder<T extends Event> implements TraceBuilder<Trace<T>> {

		public Trace<? extends Event> build(Trace<T> e) {
			return e;
		}

	}

	private static final Font DEFAULT_FONT = new Font(null, Font.PLAIN, 10);

	private final static class ProMTraceListMouseAdapter<T> extends MouseAdapter {

		private final Set<ClickListener<T>> clickListener;
		private final Set<MoveListener<T>> moveListener;
		private final JList<T> list;
		private ProMTraceViewCellRenderer<T> cellRenderer;

		private ProMTraceListMouseAdapter(JList<T> list, ProMTraceViewCellRenderer<T> cellRenderer,
				Set<ClickListener<T>> clickListener, Set<MoveListener<T>> moveListener) {
			super();
			this.list = list;
			this.cellRenderer = cellRenderer;
			this.clickListener = clickListener;
			this.moveListener = moveListener;
		}

		public void mouseClicked(MouseEvent e) {

			if (!clickListener.isEmpty()) {
				final int traceIndex = getTraceIndex(e);
				if (traceIndex != -1) {
					final T trace = list.getModel().getElementAt(traceIndex);
					final int eventIndex = getEventIndex(traceIndex, trace, e);

					for (ClickListener<T> listener : clickListener) {
						if (e.getClickCount() == 2) {
							listener.traceMouseDoubleClicked(trace, traceIndex, eventIndex, e);
						} else if (e.getClickCount() == 1) {
							listener.traceMouseClicked(trace, traceIndex, eventIndex, e);
						}
					}
				}
			}
		}

		public void mouseMoved(MouseEvent e) {

			if (!moveListener.isEmpty()) {
				final int traceIndex = getTraceIndex(e);
				if (traceIndex != -1) {
					final T trace = list.getModel().getElementAt(traceIndex);
					final int eventIndex = getEventIndex(traceIndex, trace, e);

					for (MoveListener<T> listener : moveListener) {
						listener.traceMouseMoved(trace, traceIndex, eventIndex, e);
					}
				}
			}
		}

		private int getEventIndex(int traceIndex, T trace, MouseEvent e) {
			return cellRenderer.translateToEventIndex(e.getPoint(), trace, list.isSelectedIndex(traceIndex));
		}

		private int getTraceIndex(MouseEvent e) {
			Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
			if (r != null && r.contains(e.getPoint())) {
				return list.locationToIndex(e.getPoint());
			}
			return -1;
		}

	}

	/**
	 * The {@link ListCellRenderer} used to paint the objects.
	 * 
	 * @author F. Mannhardt
	 * 
	 * @param <T>
	 */
	private final static class ProMTraceViewCellRenderer<E> extends ProMTraceView implements ListCellRenderer<E> {

		private static final long serialVersionUID = -2495069999724478333L;

		private TraceBuilder<E> traceBuilder;
		private E currentValue;
		private int fixedWidthLimit = DEFAULT_FIXED_WIDTH_TRACE_COUNT;

		private final ListModel<E> listModel;

		public ProMTraceViewCellRenderer(ListModel<E> listModel, TraceBuilder<E> traceBuilder, Font defaultFont,
				boolean hasLabels) {
			super(defaultFont, hasLabels);
			this.listModel = listModel;
			setTraceBuilder(traceBuilder);
			setOpaque(false);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			setForeground(list.getForeground());
			return this;
		}

		public int translateToEventIndex(Point point, E value, boolean isSelected) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			return translateToEventIndex(point);
		}

		public int translateToDetailedEventIndex(Point point, E value, boolean isSelected) {
			if (value != currentValue) {
				setTrace(getTraceBuilder().build(value));
				updatePreferredSize();
			}
			currentValue = value;
			setSelected(isSelected);
			return translateToDetailedEventIndex(point);
		}

		public TraceBuilder<E> getTraceBuilder() {
			return traceBuilder;
		}

		public void setTraceBuilder(TraceBuilder<E> traceBuilder) {
			this.currentValue = null;
			this.traceBuilder = traceBuilder;
		}

		public int getFixedWidthLimit() {
			return fixedWidthLimit;
		}

		public void setFixedWidthLimit(int fixedWidthLimit) {
			this.currentValue = null;
			this.fixedWidthLimit = fixedWidthLimit;
		}

		@Override
		public int getFixedWedgeWidth() {
			// listModel might be have been initialized yet
			if (listModel != null && listModel.getSize() > getFixedWidthLimit()) {
				// Override the wedge width, either with max width or the preset fixed width
				return super.getFixedWedgeWidth() == -1 ? getMaxWedgeWidth() : super.getFixedWedgeWidth();
			} else {
				// Return the normal setting
				return super.getFixedWedgeWidth();
			}
		}

		@Override
		public int getFixedInfoWidth() {
			// listModel might be have been initialized yet
			if (listModel != null && listModel.getSize() > getFixedWidthLimit()) {
				// Override the info width, either with max width or the preset fixed width
				return super.getFixedInfoWidth() == -1 ? getMaxInfoWidth() : super.getFixedInfoWidth();
			} else {
				// Return the normal setting
				return super.getFixedInfoWidth();
			}
		}

		@Override
		public String getToolTipText(MouseEvent e) {
			int event = translateToDetailedEventIndex(e.getPoint(), currentValue, isSelected());
			if (event != -1) {
				Trace<?> tr = traceBuilder.build(currentValue);
				if (event == -2) {
					return "<html>" + n(tr.getName()) + "<br>" + n(tr.getInfo()) + "</html>";
				} else if (event >= 0) {
					for (Iterator<? extends Event> it = tr.iterator(); it.hasNext();) {
						Event ev = it.next();
						if (event == 0) {
							return "<html>" + n(ev.getTopLabel()) + "<br>" + n(ev.getLabel()) + "<br>"
									+ n(ev.getBottomLabel()) + "<br>" + n(ev.getBottomLabel2()) + "</html>";
						}
						event--;
					}
				}
			}
			return null;
		}

		private String n(Object x) {
			return x == null ? "" : x.toString();
		}
	}

	private static final long serialVersionUID = -8729322696443726936L;

	public static final int DEFAULT_FIXED_WIDTH_TRACE_COUNT = 50000;

	private final ListModel<T> listModel;
	private final JList<T> jList;
	private final ProMTraceViewCellRenderer<T> cellRenderer;
	private final boolean hasLabels;

	private final JScrollPane scrollPane;

	private transient CopyOnWriteArraySet<ClickListener<T>> clickListener = new CopyOnWriteArraySet<ClickListener<T>>();
	private transient CopyOnWriteArraySet<MoveListener<T>> moveListener = new CopyOnWriteArraySet<MoveListener<T>>();

	private JProgressBar progressBar;

	private JPanel toolbar;
	private JButton selectAll;
	private JButton deselectAll;

	/**
	 * Creates an empty {@link ProMTraceList}
	 * 
	 * @param traceBuilder
	 */
	public ProMTraceList(TraceBuilder<T> traceBuilder) {
		this(Collections.<T>emptyList(), traceBuilder);
	}

	/**
	 * Creates a filterable and sortable {@link ProMTraceList} from the supplied
	 * {@link Collection} of objects
	 * 
	 * @param traces
	 * @param traceBuilder
	 */
	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder) {
		this(traces, traceBuilder, DEFAULT_FONT);
	}

	/**
	 * Creates a filterable and sortable {@link ProMTraceList} from the supplied
	 * {@link Collection} of objects
	 * 
	 * @param traces
	 * @param traceBuilder
	 * @param labelFont
	 */
	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder, Font labelFont) {
		this(traces, traceBuilder, null, labelFont);
	}

	/**
	 * Creates a filterable and sortable {@link ProMTraceList} from the supplied
	 * {@link Collection} of objects
	 * 
	 * @param traces
	 * @param traceBuilder
	 * @param labelFont
	 * @param hasLabels
	 */
	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder, Font labelFont, boolean hasLabels) {
		this(traces, traceBuilder, null, labelFont, hasLabels);
	}

	/**
	 * Creates a filterable and sortable {@link ProMTraceList} from the supplied
	 * {@link Collection} of objects
	 * 
	 * @param traces
	 * @param traceBuilder
	 * @param order
	 */
	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder, Comparator<T> order) {
		this(traces, traceBuilder, order, DEFAULT_FONT);
	}

	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder, Comparator<T> order, Font labelFont) {
		this(traces, traceBuilder, order, labelFont, true);
	}

	/**
	 * Creates a filterable and sortable {@link ProMTraceList} from the supplied
	 * {@link Collection} of objects
	 * 
	 * @param traces
	 * @param traceBuilder
	 * @param order
	 * @param labelFont
	 * @param hasLabels
	 */
	public ProMTraceList(Collection<T> traces, TraceBuilder<T> traceBuilder, Comparator<T> order, Font labelFont,
			boolean hasLabels) {
		this(new FilteredListModelImpl<>(new SortableListModelImpl<>(traces)), traceBuilder, labelFont, hasLabels);
		if (order != null) {
			sort(order);
		}
	}

	/**
	 * Creates a {@link ProMTraceList} from the supplied {@link ListModel}, if
	 * you want your list to be sortable and filterable your {@link ListModel}
	 * needs to implement {@link FilteredListModel} and
	 * {@link SortableListModel}.
	 * 
	 * @param traceModel
	 * @param traceBuilder
	 */
	public ProMTraceList(ListModel<T> traceModel, TraceBuilder<T> traceBuilder) {
		this(traceModel, traceBuilder, DEFAULT_FONT);
	}

	public ProMTraceList(ListModel<T> traceModel, TraceBuilder<T> traceBuilder, Font labelFont) {
		this(traceModel, traceBuilder, labelFont, true);
	}

	/**
	 * Creates a {@link ProMTraceList} from the supplied {@link ListModel}, if
	 * you want your list to be sortable and filterable your {@link ListModel}
	 * needs to implement {@link FilteredListModel} and
	 * {@link SortableListModel}.
	 * 
	 * @param traces
	 * @param traceBuilder
	 * @param labelFont
	 *            to be used
	 * @param whether
	 *            there should be space for labels
	 */
	public ProMTraceList(ListModel<T> listModel, TraceBuilder<T> traceBuilder, Font labelFont, boolean hasLabels) {
		this.listModel = listModel;
		this.hasLabels = hasLabels;
		this.cellRenderer = new ProMTraceViewCellRenderer<T>(listModel, traceBuilder, labelFont, hasLabels);
		setFont(labelFont);

		jList = new JList<T>(listModel);
		jList.setFixedCellHeight((int) cellRenderer.getPreferredSize().getHeight());
		jList.setOpaque(false);
		jList.setForeground(null);
		jList.setBackground(null);
		jList.setCellRenderer(cellRenderer);

		ProMTraceListMouseAdapter<T> mouseAdapter = new ProMTraceListMouseAdapter<T>(jList, cellRenderer, clickListener,
				moveListener);
		jList.addMouseListener(mouseAdapter);
		jList.addMouseMotionListener(mouseAdapter);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		setForeground(null);
		setBackground(null);

		toolbar = new JPanel();
		toolbar.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
		toolbar.setOpaque(false);
		toolbar.setForeground(null);
		toolbar.setBackground(null);
		toolbar.setVisible(false);
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		selectAll = SlickerFactory.instance().createButton("Select all");
		selectAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jList.setSelectionInterval(0, jList.getModel().getSize() - 1);
			}
		});
		toolbar.add(selectAll);
		toolbar.add(Box.createHorizontalStrut(5));
		deselectAll = SlickerFactory.instance().createButton("Deselect all");
		deselectAll.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				jList.clearSelection();
			}
		});
		toolbar.add(deselectAll);
		toolbar.add(Box.createGlue());

		scrollPane = new ProMScrollPane(jList);
		scrollPane.setOpaque(false);
		scrollPane.setForeground(null);
		scrollPane.setBackground(null);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getViewport().setForeground(null);
		scrollPane.getViewport().setBackground(null);

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setString("Loading ...");
		progressBar.setStringPainted(true);

		add(toolbar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void addTraceSelectionListener(ListSelectionListener l) {
		jList.addListSelectionListener(l);
	}

	public void removeTraceSelectionListener(ListSelectionListener l) {
		jList.removeListSelectionListener(l);
	}

	public void addTraceClickListener(ClickListener<T> l) {
		clickListener.add(l);
	}

	public void removeTraceClickListener(ClickListener<T> l) {
		clickListener.remove(l);
	}

	public void addTraceMoveListener(MoveListener<T> l) {
		moveListener.add(l);
	}

	public void removeTraceMoveListener(MoveListener<T> l) {
		moveListener.remove(l);
	}

	/**
	 * @return the underlying {@link JList}
	 */
	public JList<T> getList() {
		return jList;
	}

	/**
	 * @return the underlying {@link JScrollPane}
	 */
	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	/**
	 * @return the underlying {@link ListModel}
	 */
	public ListModel<T> getListModel() {
		return listModel;
	}

	/****************************************************************************
	 * Methods for special ListModels, mainly here for legacy reasons. Better to
	 * use ListModel directly!
	 ****************************************************************************/

	/**
	 * Adds the element to this view. Please note your {@link ListModel} needs
	 * to implement {@link MutableListModel} otherwise an
	 * {@link UnsupportedOperationException} will be thrown.
	 * 
	 * @param element
	 * @throws UnsupportedOperationException
	 *             in case the ListModel is not mutable
	 */
	public void add(T element) {
		if (listModel instanceof FilteredListModel) {
			@SuppressWarnings("unchecked")
			FilteredListModel<T> filteredListModel = (FilteredListModel<T>) listModel;
			((SortableListModel<T>) filteredListModel.getUnfilteredListModel()).add(element);
		} else if (listModel instanceof MutableListModel) {
			((MutableListModel<T>) listModel).add(element);
		} else {
			throw new UnsupportedOperationException(
					"ListModel does not implement MutableListModel, please add data directly to the ListModel you supplied!");
		}
	}

	/**
	 * Adds the elements to this view. Please note your {@link ListModel} needs
	 * to implement {@link MutableListModel} otherwise an
	 * {@link UnsupportedOperationException} will be thrown.
	 * 
	 * @param elements
	 * @throws UnsupportedOperationException
	 *             in case the ListModel is not mutable
	 */
	public void addAll(Iterable<T> elements) {
		if (listModel instanceof FilteredListModel) {
			@SuppressWarnings("unchecked")
			FilteredListModel<T> filteredListModel = (FilteredListModel<T>) listModel;
			((MutableListModel<T>) filteredListModel.getUnfilteredListModel()).addAll(elements);
		} else if (listModel instanceof MutableListModel) {
			((MutableListModel<T>) listModel).addAll(elements);
		} else {
			throw new UnsupportedOperationException(
					"ListModel does not implement MutableListModel, please add data directly to the ListModel you supplied!");
		}
	}

	/**
	 * Clears the underlying data structure {@link #getListModel()}.
	 * 
	 * @throws UnsupportedOperationException
	 *             in case the ListModel is not mutable
	 */
	public void clear() {
		if (listModel instanceof FilteredListModel) {
			@SuppressWarnings("unchecked")
			FilteredListModel<T> filteredListModel = (FilteredListModel<T>) listModel;
			((MutableListModel<T>) filteredListModel.getUnfilteredListModel()).clear();
		} else if (listModel instanceof MutableListModel) {
			((MutableListModel<T>) listModel).clear();
		} else {
			throw new UnsupportedOperationException(
					"ListModel does not implement MutableListModel, please clear your data directly in the ListModel you supplied!");
		}
	}

	public void sort(Comparator<T> sortOrder) {
		if (listModel instanceof FilteredListModel) {
			@SuppressWarnings("unchecked")
			FilteredListModel<T> filteredListModel = (FilteredListModel<T>) listModel;
			((SortableListModel<T>) filteredListModel.getUnfilteredListModel()).sort(sortOrder);
		} else if (listModel instanceof SortableListModel) {
			((SortableListModel<T>) listModel).sort(sortOrder);
		} else {
			throw new UnsupportedOperationException(
					"ListModel does not implement SortableListModel, please sort your data directly in the ListModel you supplied!");
		}
	}

	@SuppressWarnings("unchecked")
	public void filter(ListModelFilter<T> filter) {
		if (listModel instanceof FilteredListModel) {
			((FilteredListModel<T>) listModel).filter(filter);
		} else {
			throw new UnsupportedOperationException(
					"ListModel does not implement FilteredListModel, please filter your data directly in the ListModel you supplied!");
		}
	}

	/*********************************************************/

	/**
	 * Set the factor by which the color of the unselected traces is modified.
	 * For example, 1.0 will result in no change and 0.5 will darken the color.
	 * 
	 * @param attenuationFactor
	 *            non-negative factor
	 */
	public void setAttenuationFactor(float attenuationFactor) {
		cellRenderer.setAttenuationFactor(attenuationFactor);
	}

	public float getAttenuationFactor() {
		return cellRenderer.getAttenuationFactor();
	}

	public WedgeBuilder getWedgeBuilder() {
		return cellRenderer.getWedgeBuilder();
	}

	/**
	 * Use a {@link WedgeBuilder} to dynamically override the color of an
	 * {@link Event}. Use {@link DefaultWedgeBuilder}, if you only want to
	 * override a certain color.
	 * 
	 * @param wedgeBuilder
	 *            that overrides the default color of an event
	 */
	public void setWedgeBuilder(WedgeBuilder wedgeBuilder) {
		if (wedgeBuilder == null) {
			cellRenderer.setWedgeBuilder(new DefaultWedgeBuilder());
		} else {
			cellRenderer.setWedgeBuilder(new DelegateWedgeBuilder(wedgeBuilder));
		}
	}

	public int getMaxWedgeWidth() {
		return cellRenderer.getMaxWedgeWidth();
	}

	/**
	 * Set the maximum width (in pixel) of a single "event wedge".
	 * 
	 * @param maxWidth
	 */
	public void setMaxWedgeWidth(int maxWidth) {
		cellRenderer.setMaxWedgeWidth(maxWidth);
	}

	public int getMaxInfoWidth() {
		return cellRenderer.getMaxInfoWidth();
	}

	/**
	 * Set the maximum width (in pixel) of the trace info area.
	 * 
	 * @param maxWidth
	 */
	public void setMaxInfoWidth(int maxWidth) {
		cellRenderer.setMaxInfoWidth(maxWidth);
	}

	/**
	 * Set the fixed width (in pixel) of a single "event wedge". Set to -1 to
	 * enable dynamic sizing (slower) according to the length of labels.
	 * 
	 * @param fixedWidth
	 */
	public void setFixedWedgeWidth(int fixedWidth) {
		cellRenderer.setFixedWedgeWidth(fixedWidth);
	}

	/**
	 * Set the fixed width (in pixel) of the trace info area. Set to -1 to
	 * enable dynamic sizing (slower) according to the length of labels.
	 * 
	 * @param fixedWidth
	 */
	public void setFixedInfoWidth(int fixedWidth) {
		cellRenderer.setFixedInfoWidth(fixedWidth);
	}

	public int getFixedWedgeLimit() {
		return cellRenderer.getFixedWidthLimit();
	}

	/**
	 * Changes the built-in limit of events per trace that is used to determine
	 * when to switch to fixed size wedge width for performance reasons.
	 * 
	 * @param fixedWedgeLimit
	 */
	public void setFixedWedgeLimit(int fixedWedgeLimit) {
		cellRenderer.setFixedWidthLimit(fixedWedgeLimit);
	}

	public int getCollapsedLabelLength() {
		return cellRenderer.getCollapsedLabelLength();
	}

	/**
	 * Set the length of the label shown in a collapsed event, in an unselected
	 * trace.
	 * 
	 * @param collapsedLabelLength
	 */
	public void setCollapsedLabelLength(int collapsedLabelLength) {
		cellRenderer.setCollapsedLabelLength(collapsedLabelLength);
	}

	public Stroke getWedgeStroke() {
		return cellRenderer.getWedgeStroke();
	}

	/**
	 * @param wedgeStroke
	 *            the default {@link Stroke} that the wedges border is painted
	 *            with
	 */
	public void setWedgeStroke(Stroke wedgeStroke) {
		cellRenderer.setWedgeStroke(wedgeStroke);
	}

	public int getWedgeGap() {
		return cellRenderer.getWedgeGap();
	}

	/**
	 * @param wedgeGap
	 *            sets the default gap between two event wedges
	 */
	public void setWedgeGap(int wedgeGap) {
		cellRenderer.setWedgeGap(wedgeGap);
	}

	public TraceBuilder<T> getTraceBuilder() {
		return cellRenderer.getTraceBuilder();
	}

	/**
	 * @param traceBuilder
	 *            that is used to create traces out of the input objects
	 */
	public void setTraceBuilder(TraceBuilder<T> traceBuilder) {
		cellRenderer.setTraceBuilder(traceBuilder);
	}

	public void beforeUpdate() {
		setEnabled(false);
		remove(scrollPane);
		add(progressBar, BorderLayout.CENTER);
		validate();
	}

	public void afterUpdate() {
		remove(progressBar);
		add(scrollPane, BorderLayout.CENTER);
		validate();
		setEnabled(true);
	}

	public void showToolbar() {
		toolbar.setVisible(true);
	}

	public void hideToolbar() {
		toolbar.setVisible(false);
	}

	public JPanel getToolbar() {
		return toolbar;
	}

	public void setToolbar(JPanel toolbar) {
		this.toolbar = toolbar;
	}

	/********* Printing/Saving methods ********/

	private interface GraphicsExporterFactory {

		Graphics2D newGraphicsIO(Dimension pageDimension) throws FileNotFoundException, IOException;

	}

	/**
	 * Saves the list content as PDF
	 * 
	 * @param pdfTitle
	 * @param file
	 * @param graphicsExporter
	 * @throws IOException
	 */
	public static <T> void saveAsPDF(ProMTraceList<T> traceList, final String pdfTitle, final File file)
			throws IOException {
		saveUsing(new GraphicsExporterFactory() {

			public VectorGraphicsIO newGraphicsIO(Dimension pageDimension) throws FileNotFoundException {
				PDFGraphics2D vectorGraphicsIO = new PDFGraphics2D(file, pageDimension);
				vectorGraphicsIO.setMultiPage(true);
				Properties p = new Properties();
				p.setProperty(PDFGraphics2D.PAGE_SIZE, PageConstants.A4);
				p.setProperty(PDFGraphics2D.ORIENTATION, PageConstants.LANDSCAPE);
				p.setProperty(PDFGraphics2D.TITLE, pdfTitle != null ? pdfTitle : "Trace Variants");
				p.setProperty(PDFGraphics2D.COMPRESS, "true");
				p.setProperty(PDFGraphics2D.FIT_TO_PAGE, "true");
				p.setProperty(PDFGraphics2D.TEXT_AS_SHAPES, "false");
				vectorGraphicsIO.setProperties(p);
				return vectorGraphicsIO;
			}
		}, traceList);
	}

	/**
	 * Saves the list content as EMF
	 * 
	 * @param file
	 * @param graphicsExporter
	 * @throws IOException
	 */
	public static <T> void saveAsEMF(ProMTraceList<T> traceList, final File file) throws IOException {
		saveUsing(new GraphicsExporterFactory() {

			public VectorGraphicsIO newGraphicsIO(Dimension pageDimension) throws FileNotFoundException {
				return new EMFGraphics2D(file, pageDimension);
			}
		}, traceList);
	}

	/**
	 * Saves the list content as PS
	 * 
	 * @param file
	 * @param graphicsExporter
	 * @throws IOException
	 */
	public static <T> void saveAsEPS(ProMTraceList<T> traceList, final File file) throws IOException {
		saveUsing(new GraphicsExporterFactory() {

			public VectorGraphicsIO newGraphicsIO(Dimension pageDimension) throws FileNotFoundException {
				EPSGraphics2D g = new EPSGraphics2D(file, pageDimension);
				Properties p = new Properties(EPSGraphics2D.getDefaultProperties());
				p.setProperty(EPSGraphics2D.PAGE_SIZE, EPSGraphics2D.CUSTOM_PAGE_SIZE);
				p.setProperty(EPSGraphics2D.PAGE_MARGINS, "0, 0, 0, 0");
				p.setProperty(EPSGraphics2D.TEXT_AS_SHAPES, "false");
				p.put(EPSGraphics2D.CUSTOM_PAGE_SIZE, pageDimension.width + ", " + pageDimension.height);
				g.setProperties(p);
				return g;
			}
		}, traceList);
	}

	public static <T> void saveAsSVG(ProMTraceList<T> traceList, final File file) throws IOException {
		saveUsing(new GraphicsExporterFactory() {

			public VectorGraphicsIO newGraphicsIO(Dimension pageDimension) throws IOException {
				SVGGraphics2D g = new SVGGraphics2D(file, pageDimension);
				Properties properties = new Properties(SVGGraphics2D.getDefaultProperties());
				properties.setProperty(SVGGraphics2D.TEXT_AS_SHAPES, "false");
				g.setProperties(properties);
				return g;
			}
		}, traceList);
	}

	/**
	 * Saves the list content as PDF
	 * 
	 * @param pdfTitle
	 * @param file
	 * @throws IOException
	 */
	public static <T> void saveUsing(GraphicsExporterFactory graphicsExporter, ProMTraceList<T> traceList)
			throws IOException {

		List<T> currentElements = new ArrayList<>();
		for (int i = 0; i < traceList.getListModel().getSize(); i++) {
			currentElements.add(traceList.getListModel().getElementAt(i));
		}

		ProMTraceList<T> listForPrinting = new ProMTraceList<>(currentElements, traceList.getTraceBuilder(),
				traceList.getFont(), traceList.hasLabels());
		listForPrinting.setBackground(traceList.getBackground());
		listForPrinting.setForeground(traceList.getForeground());
		listForPrinting.setAttenuationFactor(traceList.getAttenuationFactor());
		listForPrinting.setWedgeBuilder(traceList.getWedgeBuilder());
		listForPrinting.setWedgeGap(traceList.getWedgeGap());
		listForPrinting.setWedgeStroke(traceList.getWedgeStroke());
		listForPrinting.setOpaque(true);

		JFrame printFrame = new JFrame();

		try {

			JList<T> list = listForPrinting.getList();

			printFrame.add(list);
			printFrame.pack();

			Dimension pageDimension = new Dimension(list.getWidth(), list.getHeight());

			Graphics2D graphicsIO = graphicsExporter.newGraphicsIO(pageDimension);

			if (graphicsIO instanceof VectorGraphicsIO) {
				((VectorGraphics) graphicsIO).startExport();
			}

			if (graphicsIO instanceof MultiPageDocument) {

				int tracesPerPage = 10;
				int pageCount = Math.max(1, currentElements.size() / tracesPerPage);
				int currentStartIndex = 0;

				for (int i = 0; i < pageCount; i++) {

					List<T> pageTraces = currentElements.subList(currentStartIndex,
							Math.min(currentStartIndex + tracesPerPage, currentElements.size()));

					listForPrinting.clear();
					listForPrinting.addAll(pageTraces);
					printFrame.pack();

					pageDimension = new Dimension(listForPrinting.getList().getWidth(), list.getHeight());

					((MultiPageDocument) graphicsIO).openPage(pageDimension, "Page " + i);
					list.print(graphicsIO);
					((MultiPageDocument) graphicsIO).closePage();

					currentStartIndex = currentStartIndex + tracesPerPage;
				}
			} else {
				list.print(graphicsIO);
			}

			if (graphicsIO instanceof VectorGraphicsIO) {
				((VectorGraphics) graphicsIO).endExport();
			}

		} finally {
			printFrame.dispose();
		}
	}

	public JButton getSelectAllButton() {
		return selectAll;
	}

	public JButton getDeselectAllButton() {
		return deselectAll;
	}

	public boolean hasLabels() {
		return hasLabels;
	}

}