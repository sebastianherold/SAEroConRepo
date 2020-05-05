package org.processmining.plugins.graphviz.visualisation;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.processmining.plugins.graphviz.colourMaps.ColourMap;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.Dot2Image;
import org.processmining.plugins.graphviz.dot.Dot2Image.Type;
import org.processmining.plugins.graphviz.dot.DotEdge;
import org.processmining.plugins.graphviz.dot.DotElement;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.graphviz.visualisation.export.ExporterDot;
import org.processmining.plugins.graphviz.visualisation.listeners.DotElementSelectionListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.GraphChangedListener.GraphChangedReason;
import org.processmining.plugins.graphviz.visualisation.listeners.MouseInElementsChangedListener;
import org.processmining.plugins.graphviz.visualisation.listeners.SelectionChangedListener;

import com.kitfox.svg.Group;
import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.xml.StyleAttribute;

public class DotPanel extends NavigableSVGPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6201301504669783161L;

	private Action changeGraphDirection = new AbstractAction() {
		private static final long serialVersionUID = -38576326322179480L;

		public void actionPerformed(ActionEvent e) {
			GraphDirection newDirection;
			switch (dot.getDirection()) {
				case bottomTop :
					newDirection = GraphDirection.leftRight;
					break;
				case leftRight :
					newDirection = GraphDirection.rightLeft;
					break;
				case rightLeft :
					newDirection = GraphDirection.topDown;
					break;
				case topDown :
					newDirection = GraphDirection.bottomTop;
					break;
				default :
					newDirection = GraphDirection.bottomTop;
					break;
			}
			userSettings.setDirection(newDirection);
			changeDot(dot, true);
			graphChanged(GraphChangedReason.graphDirectionChanged, newDirection);
		}
	};

	private Action increaseAspectRatio = new AbstractAction() {
		private static final long serialVersionUID = 2323619233153604381L;

		public void actionPerformed(ActionEvent e) {
			userSettings.nodeSeparation = Math.min(userSettings.nodeSeparation + .05, 4);
			changeDot(dot, true);
			graphChanged(GraphChangedReason.nodeSeparationChanged, userSettings.nodeSeparation);
		}
	};

	private Action decreaseAspectRatio = new AbstractAction() {
		private static final long serialVersionUID = 1136135860512175161L;

		public void actionPerformed(ActionEvent e) {
			userSettings.nodeSeparation = Math.max(userSettings.nodeSeparation - 0.05, 0.02);
			changeDot(dot, true);
			graphChanged(GraphChangedReason.nodeSeparationChanged, userSettings.nodeSeparation);
		}
	};

	private Dot dot;
	private final DotPanelUserSettings userSettings;
	private HashMap<String, DotElement> id2element;
	private Set<DotElement> selectedElements;
	private Set<DotElement> mouseInElements;
	private final CopyOnWriteArrayList<SelectionChangedListener<DotElement>> selectionChangedListeners = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<MouseInElementsChangedListener<DotElement>> mouseInElementsChangedListeners = new CopyOnWriteArrayList<>();
	private final CopyOnWriteArrayList<GraphChangedListener> graphChangedListeners = new CopyOnWriteArrayList<>();

	public DotPanel(Dot dot) {
		super(dot2svg(dot));
		this.dot = dot;
		prepareNodeSelection(dot);
		mouseInElements = new HashSet<>();
		userSettings = new DotPanelUserSettings(dot);

		//listen to ctrl+d for a change in graph layouting direction
		helperControlsShortcuts.add("ctrl d");
		helperControlsExplanations.add("change graph direction");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK),
				"changeGraphDirection");
		getActionMap().put("changeGraphDirection", changeGraphDirection);

		//listen to ctrl+q for increasing aspect ratio
		helperControlsShortcuts.add("ctrl q");
		helperControlsExplanations.add("increase graph node distance");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK),
				"increaseAspectRatio");
		getActionMap().put("increaseAspectRatio", increaseAspectRatio);

		//listen to ctrl+w for decreasing aspect ratio
		helperControlsShortcuts.add("ctrl w");
		helperControlsExplanations.add("decrease graph node distance");
		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK),
				"decreaseAspectRatio");
		getActionMap().put("decreaseAspectRatio", decreaseAspectRatio);

		//add mouse listeners
		final DotPanel this2 = this;
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				e.setSource(this2);
				for (DotElement element : getElementsAtPoint(e.getPoint())) {
					element.mousePressed(e);
				}
			}
		});
	}

	@Override
	protected boolean processMouseClick(MouseEvent e) {
		boolean superChanged = super.processMouseClick(e);

		//pass clicks to elements and process selection changes, but not if the click was already catched.
		boolean selectionChange = false;
		Point point = e.getPoint();
		if (!superChanged) {
			e.setSource(this);
			for (DotElement element : getElementsAtPoint(point)) {
				element.mouseClicked(e);

				if (SwingUtilities.isLeftMouseButton(e)) {
					selectionChange = selectionChange || processSelection(element, e);
				}
			}

			if (SwingUtilities.isLeftMouseButton(e) && !selectionChange && !e.isControlDown()) {
				//the user did not click on anything clickable. Remove the selection.
				selectionChange = removeSelection();
			}

			if (selectionChange) {
				selectionChanged();
			}
		}

		return superChanged || selectionChange;
	};

	@Override
	protected boolean processMouseRelease(MouseEvent e) {
		boolean superChanged = super.processMouseRelease(e);

		//call mouseReleased on all elements under the mouse
		for (DotElement element : getElementsAtPoint(e.getPoint())) {
			element.mouseReleased(e);
		}

		return superChanged;
	}

	@Override
	protected boolean processMouseDrag(MouseEvent e) {
		boolean changed = super.processMouseDrag(e);

		//if we start dragging, we exit all elements
		changed |= exitAllElements(e);

		return changed;
	}

	@Override
	protected boolean processMouseMove(MouseEvent e) {
		boolean captured = super.processMouseMove(e);

		boolean changed = false;

		if (captured) {
			//something above us captured the mouse hover, so exit all dot elements
			changed = exitAllElements(e);
		} else {
			//process the mouseEnter and Exit of the dot elements
			Set<DotElement> newElements = getElementsAtPoint(e.getPoint());
			//exit
			for (DotElement element : mouseInElements) {
				if (!newElements.contains(element)) {
					element.mouseExited(e);
					changed = true;
				}
			}
			//enter
			for (DotElement element : newElements) {
				if (!mouseInElements.contains(element)) {
					element.mouseEntered(e);
					changed = true;
				}
			}
			mouseInElements = newElements;

			if (changed) {
				mouseInElementsChanged();
			}
		}

		return changed || captured;
	}

	@Override
	protected boolean processMouseExit(MouseEvent e) {
		boolean changed = super.processMouseExit(e);

		//exit the dot elements, as we are leaving the screen
		changed |= exitAllElements(e);

		return changed;
	}

	@Override
	public List<Exporter> getExporters() {
		List<Exporter> exporters = super.getExporters();
		exporters.add(new ExporterDot());
		return exporters;
	}

	/**
	 * Deselect all nodes; return whether the selection changed
	 * 
	 * @return
	 */
	private boolean removeSelection() {
		for (DotElement element : selectedElements) {
			for (DotElementSelectionListener listener : element.getSelectionListeners()) {
				listener.deselected(element, image);
			}
		}
		boolean result = !selectedElements.isEmpty();
		selectedElements.clear();
		return result;
	}

	private boolean processSelection(DotElement element, MouseEvent e) {
		if (element.isSelectable()) {
			if (e.isControlDown()) {
				//only change this element
				if (selectedElements.contains(element)) {
					//deselect element
					selectedElements.remove(element);
					for (DotElementSelectionListener listener : element.getSelectionListeners()) {
						listener.deselected(element, image);
					}
				} else {
					//select element
					selectedElements.add(element);
					for (DotElementSelectionListener listener : element.getSelectionListeners()) {
						listener.selected(element, image);
					}
				}
			} else {
				if (selectedElements.contains(element)) {
					//clicked on selected element without keypress
					if (selectedElements.size() > 1) {
						//deselect all other selected elements
						Iterator<DotElement> it = selectedElements.iterator();
						while (it.hasNext()) {
							DotElement selectedElement = it.next();
							if (selectedElement != element) {
								for (DotElementSelectionListener listener : selectedElement.getSelectionListeners()) {
									listener.deselected(selectedElement, image);
								}
								it.remove();
							}
						}
					} else {
						//only this element was selected, deselect it
						selectedElements.remove(element);
						for (DotElementSelectionListener a : element.getSelectionListeners()) {
							a.deselected(element, image);
						}
					}
				} else {
					//clicked on not selected element without keypress
					//deselect all selected elements
					Iterator<DotElement> it = selectedElements.iterator();
					while (it.hasNext()) {
						DotElement selectedElement = it.next();
						if (selectedElement != element) {
							for (DotElementSelectionListener listener : selectedElement.getSelectionListeners()) {
								listener.deselected(selectedElement, image);
							}
							it.remove();
						}
					}
					//select this element
					selectedElements.add(element);
					for (DotElementSelectionListener listener : element.getSelectionListeners()) {
						listener.selected(element, image);
					}
				}
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private Set<DotElement> getElementsAtPoint(Point pointUserCoordinates) {
		HashSet<DotElement> result = new HashSet<DotElement>();
		if (isInImage(pointUserCoordinates)) {
			Point2D pointImageCoordinates = transformUser2Image(pointUserCoordinates);
			try {
				//get the elements at the clicked position
				List<List<RenderableElement>> elements = image.pick(pointImageCoordinates, false, null);

				StyleAttribute classAttribute = new StyleAttribute("class");
				StyleAttribute idAttribute = new StyleAttribute("id");
				for (List<RenderableElement> path : elements) {
					for (RenderableElement element : path) {
						//RenderableElement element = path.iterator().next();
						if (element instanceof Group) {
							Group group = (Group) element;

							//get the class
							group.getPres(classAttribute);

							//get the id
							group.getPres(idAttribute);
							String id = idAttribute.getStringValue();

							if (classAttribute.getStringValue().equals("node")
									|| classAttribute.getStringValue().equals("edge")) {
								//we have found a node or edge
								DotElement dotElement = id2element.get(id);
								if (dotElement != null) {
									result.add(dotElement);
								}
							}
						}
					}
				}

			} catch (SVGException e1) {
				e1.printStackTrace();
			}
		}
		return result;
	}

	private boolean exitAllElements(MouseEvent e) {
		for (DotElement element : mouseInElements) {
			element.mouseExited(e);
		}
		boolean changed = !mouseInElements.isEmpty();
		mouseInElements.clear();
		if (changed) {
			mouseInElementsChanged();
		}
		return changed;
	}

	/**
	 * Sets a new image
	 * 
	 * @param dot
	 *            ; set dot to this
	 * @param resetView
	 *            ; whether reset the view to centered+fitting
	 */
	public void changeDot(Dot dot, boolean resetView) {
		userSettings.applyToDot(dot);
		SVGDiagram diagram = dot2svg(dot);
		changeDot(dot, diagram, resetView);
	}

	/**
	 * Sets a new precomputed image. Assumptions are made about the dot & the
	 * diagram, so do not provide arbitrary ones.
	 * 
	 * @param dot
	 *            ; set dot to this
	 * @param diagram
	 *            ; use this SVG image
	 * @param resetView
	 *            ; whether reset the view to centered+fitting
	 */
	public void changeDot(Dot dot, SVGDiagram diagram, boolean resetView) {
		prepareNodeSelection(dot);
		this.dot = dot;
		setImage(diagram, resetView);
	}

	private void prepareNodeSelection(Dot dot) {
		selectedElements = new HashSet<DotElement>();

		id2element = new HashMap<String, DotElement>();
		for (DotNode dotNode : dot.getNodesRecursive()) {
			id2element.put(dotNode.getId(), dotNode);
		}
		for (DotEdge dotEdge : dot.getEdgesRecursive()) {
			id2element.put(dotEdge.getId(), dotEdge);
		}
	}

	/*
	 * convert Dot into svg
	 */
	public static SVGDiagram dot2svg(Dot dot) {
		SVGUniverse universe = new SVGUniverse();

		InputStream stream = Dot2Image.dot2imageInputStream(dot, Type.svg);
		URI uri;
		try {
			uri = universe.loadSVG(stream, "hoi");
		} catch (IOException e) {
			return null;
		}

		SVGDiagram diagram = universe.getDiagram(uri);

		if (diagram == null) {
			throw new RuntimeException("the dot-structure given is not valid\n" + dot.toString());
		}
		return diagram;
	}

	/*
	 * select a dotElement
	 */
	public void select(DotElement element) {
		selectedElements.add(element);
		for (DotElementSelectionListener listener : element.getSelectionListeners()) {
			listener.selected(element, image);
		}
		selectionChanged();
	}

	/**
	 * 
	 * @param image
	 * @param element
	 * @return the svg element of a DotElement
	 */
	public static Group getSVGElementOf(SVGDiagram image, DotElement element) {
		SVGElement svgElement = image.getElement(element.getId());
		if (svgElement instanceof Group) {
			return (Group) svgElement;
		}
		return null;
	}

	/**
	 * Set a css-property of a DotElement; returns the old value or null.
	 * 
	 * @param image
	 * @param element
	 * @param attribute
	 * @param value
	 * @return
	 */
	public static String setCSSAttributeOf(SVGDiagram image, DotElement element, String attribute, String value) {
		Group group = getSVGElementOf(image, element);
		return setCSSAttributeOf(group, attribute, value);
	}

	public static String getAttributeOf(SVGElement element, String attribute) {
		try {
			if (element.hasAttribute(attribute, AnimationElement.AT_CSS)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getStyle(sty);
				return sty.getStringValue();
			}
			if (element.hasAttribute(attribute, AnimationElement.AT_XML)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getPres(sty);
				return sty.getStringValue();
			}
		} catch (SVGElementException e) {
			e.printStackTrace();
		} catch (SVGException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String setCSSAttributeOf(SVGElement element, String attribute, Color colour) {
		return setCSSAttributeOf(element, attribute, ColourMap.toHexString(colour));
	}

	/**
	 * Set a css-property of an SVG element; returns the old value or null
	 * providing null as value removes the attribute
	 * 
	 * @param element
	 * @param attribute
	 * @param value
	 * @return
	 */
	public static String setCSSAttributeOf(SVGElement element, String attribute, String value) {
		try {
			if (element.hasAttribute(attribute, AnimationElement.AT_CSS)) {
				StyleAttribute sty = new StyleAttribute(attribute);
				element.getStyle(sty);
				String oldValue = sty.getStringValue();
				if (value != null) {
					element.setAttribute(attribute, AnimationElement.AT_CSS, value);
				} else {
					element.removeAttribute(attribute, AnimationElement.AT_CSS);
				}
				return oldValue;
			} else {
				if (value != null) {
					element.addAttribute(attribute, AnimationElement.AT_CSS, value);
				}
			}
		} catch (SVGElementException e) {
			e.printStackTrace();
		} catch (SVGException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<DotElement> getSelectedElements() {
		return Collections.unmodifiableSet(selectedElements);
	}

	public Set<DotElement> getMouseInElements() {
		return Collections.unmodifiableSet(mouseInElements);
	}

	public List<DotEdge> getEdges() {
		return dot.getEdgesRecursive();
	}

	public List<DotNode> getNodes() {
		return dot.getNodesRecursive();
	}

	public SVGDiagram getSVG() {
		return image;
	}

	public Dot getDot() {
		return dot;
	}

	public DotPanelUserSettings getUserSettings() {
		return userSettings;
	}

	//listeners
	private void graphChanged(GraphChangedReason reason, Object newState) {
		for (GraphChangedListener listener : graphChangedListeners) {
			listener.graphChanged(reason, newState);
		}
	}

	private void selectionChanged() {
		for (SelectionChangedListener<DotElement> listener : selectionChangedListeners) {
			listener.selectionChanged(Collections.unmodifiableSet(selectedElements));
		}
	}

	private void mouseInElementsChanged() {
		for (MouseInElementsChangedListener<DotElement> listener : mouseInElementsChangedListeners) {
			listener.mouseInElementsChanged(Collections.unmodifiableSet(mouseInElements));
		}
	}

	public void addSelectionChangedListener(SelectionChangedListener<DotElement> listener) {
		selectionChangedListeners.add(listener);
	}

	public void addGraphChangedListener(GraphChangedListener listener) {
		graphChangedListeners.add(listener);
	}

	public void addMouseInElementsChangedListener(MouseInElementsChangedListener<DotElement> listener) {
		mouseInElementsChangedListeners.add(listener);
	}

	/**
	 * Public method to change the graph direction. No listeners are called;
	 * that's your responsibility.
	 * 
	 * @param topdown
	 */
	public void setDirection(GraphDirection direction) {
		userSettings.setDirection(direction);
		changeDot(dot, true);
	}
}
