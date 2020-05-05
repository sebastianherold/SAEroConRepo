package org.processmining.framework.util.ui.scalableview;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.framework.util.Cleanable;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.ui.scalableview.ScalableComponent.UpdateListener;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * Panel for visualizing components.
 * 
 * @author hverbeek
 * 
 */
public class ScalableViewPanel extends JLayeredPane implements Cleanable, ChangeListener, MouseMotionListener,
		UpdateListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7826982030435169712L;

	/**
	 * The maximal zoom factor for the primary view on the transition system.
	 */
	public static final int MAX_ZOOM = 250;

	/**
	 * The access to scalable methods of primary view
	 */
	protected final ScalableComponent scalable;

	/**
	 * The primary view
	 */
	private JComponent component;

	/**
	 * The scroll pane containing the primary view on the transition system.
	 */
	protected JScrollPane scroll;

	private ViewInteractionPanel visiblePanel = null;
	private Map<ViewInteractionPanel, Pair<JPanel, JPanel>> panels = new HashMap<ViewInteractionPanel, Pair<JPanel, JPanel>>();
	private Map<ViewInteractionPanel, Integer> locations = new HashMap<ViewInteractionPanel, Integer>();

	private JButton[] buttons = new JButton[4];

	private int north = 0, south = 0, east = 0, west = 0;

	/**
	 * The bounds for the primary view on the transition system.
	 */
	private Rectangle normalBounds;

	protected SlickerFactory factory;

	protected SlickerDecorator decorator;

	/**
	 * Create a panel for visualizing the given view.
	 * 
	 * @param scalableComponent
	 *            The given graph
	 */
	public ScalableViewPanel(final ScalableComponent scalableComponent) {
		/*
		 * We will not use a layout manager, instead we will set the bounds of
		 * every panel.
		 */
		setLayout(null);
		/*
		 * Register the given view as the primary view, and get the transition
		 * system from the model.
		 */
		this.scalable = scalableComponent;
		component = scalableComponent.getComponent();
		/*
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();

		/*
		 * Create the scroll panel containing the primary view, and register the
		 * created adjustment and mouse listener.
		 */
		scroll = new JScrollPane(getComponent());
		/*
		 * Adjust Look+Feel of scrollbar to Slicker.
		 */
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		/*
		 * Create a dashed border for the primary view.
		 */
		scroll.setBorder(new DashedBorder(Color.LIGHT_GRAY));

		/*
		 * Add primary view to the layered pane. The special panels are added to
		 * the drag layer, which keeps them on top even when the underlying
		 * primary view gets updated.
		 */
		add(scroll, JLayeredPane.DEFAULT_LAYER);

		this.addMouseMotionListener(this);
		getComponent().addMouseMotionListener(this);
		scalable.addUpdateListener(this);

		/*
		 * Register a component listener to handle resize events, as the bounds
		 * of many panels depend on the size of this panel.
		 */
		this.addComponentListener(new java.awt.event.ComponentListener() {
			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
			}

			public void componentShown(ComponentEvent e) {
			}

			public void componentResized(ComponentEvent e) {
				resize();
			}
		});

		this.scroll.addComponentListener(new ComponentListener() {

			public void componentShown(ComponentEvent e) {

			}

			public void componentResized(ComponentEvent e) {
				scroll.removeComponentListener(this);
				scalable.setScale(1);
				double rx = (scroll.getWidth() - scroll.getVerticalScrollBar().getWidth())
						/ scalable.getComponent().getPreferredSize().getWidth();
				double ry = (scroll.getHeight() - scroll.getHorizontalScrollBar().getHeight())
						/ scalable.getComponent().getPreferredSize().getHeight();
				scalable.setScale(Math.min(rx, ry));
			}

			public void componentMoved(ComponentEvent e) {

			}

			public void componentHidden(ComponentEvent e) {

			}
		});

		/*
		 * Wrap up.
		 */
		initialize();
		validate();
		repaint();
	}

	public final static int TAB_HEIGHT = 30;
	public final static int TAB_WIDTH = 120;

	/**
	 * Adds the interaction panel at the given location. Location is one of
	 * SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.EAST, or
	 * SwingConstants.WEST
	 * 
	 * @param panel
	 * @param location
	 */
	public synchronized void addViewInteractionPanel(ViewInteractionPanel panel, int location) {
		panel.setScalableComponent(scalable);
		panel.setParent(this);

		JPanel panelOn = factory.createRoundedPanel(15, Color.LIGHT_GRAY);
		JPanel panelOff = factory.createRoundedPanel(15, Color.DARK_GRAY);
		panelOn.setLayout(null);
		panelOff.setLayout(null);

		panelOn.add(panel.getComponent());
		panelOn.setVisible(false);
		panelOn.setEnabled(false);
		panelOff.setVisible(true);
		panelOff.setEnabled(true);
		JLabel panelTitle = factory.createLabel(panel.getPanelName());
		panelTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		panelTitle.setVerticalTextPosition(SwingConstants.CENTER);
		panelTitle.setForeground(Color.WHITE);
		panelTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
		panelOff.add(panelTitle);

		panels.put(panel, new Pair<JPanel, JPanel>(panelOn, panelOff));
		locations.put(panel, location);

		switch (location) {
			case SwingConstants.NORTH : {
				panelTitle.setBounds(5, 10, TAB_WIDTH - 15, TAB_HEIGHT);
				panelOn.setLocation(TAB_HEIGHT + 10 + north * TAB_WIDTH, -10);
				panel.getComponent().setLocation(10, 20);
				panelOff.setBounds(TAB_HEIGHT + 10 + north * TAB_WIDTH, -10, TAB_WIDTH - 5, TAB_HEIGHT + 10);
				north++;
				break;
			}
			case SwingConstants.EAST : {
				panelTitle.setBounds(0, 5, TAB_HEIGHT, TAB_WIDTH - 15);
				panelTitle.setUI(new VerticalLabelUI(true));
				panelOn.setLocation(getWidth() - TAB_HEIGHT, TAB_HEIGHT + 10 + east * TAB_WIDTH);
				panelOff.setBounds(getWidth() - TAB_HEIGHT, TAB_HEIGHT + 10 + east * TAB_WIDTH, TAB_HEIGHT + 10,
						TAB_WIDTH - 5);
				panel.getComponent().setLocation(10, 10);
				east++;
				break;
			}
			case SwingConstants.WEST : {
				panelTitle.setBounds(10, 5, TAB_HEIGHT, TAB_WIDTH - 15);
				panelTitle.setUI(new VerticalLabelUI(true));
				panelOn.setLocation(-10, TAB_HEIGHT + 10 + west * TAB_WIDTH);
				panelOff.setBounds(-10, TAB_HEIGHT + 10 + west * TAB_WIDTH, TAB_HEIGHT + 10, TAB_WIDTH - 5);
				panel.getComponent().setLocation(20, 10);
				west++;
				break;
			}
			default : {
				//SOUTH
				panelTitle.setBounds(5, 0, TAB_WIDTH - 15, TAB_HEIGHT);
				panelOn.setLocation(TAB_HEIGHT + 10 + south * TAB_WIDTH, getHeight() - TAB_HEIGHT);
				panelOff.setBounds(TAB_HEIGHT + 10 + south * TAB_WIDTH, getHeight() - TAB_HEIGHT, TAB_WIDTH - 5,
						TAB_HEIGHT + 10);
				panel.getComponent().setLocation(10, 10);
				south++;

			}
		}
		setSize(panel, panelOff, panelOn);
		setLocation(panel, panelOff, panelOn);

		add(panelOn, JLayeredPane.PALETTE_LAYER);
		add(panelOff, JLayeredPane.PALETTE_LAYER);
		panel.updated();
	}

	/**
	 * Remove a previously added interaction panel from the ScalableViewPanel.
	 * 
	 * This can be used to remove an interaction panel from the
	 * ScalableViewPanel. If the interaction panel does not exist, nothing will
	 * be removed.
	 * 
	 * @param panel
	 *            The panel that should be removed.
	 */
	public synchronized void removeViewInteractionPanel(ViewInteractionPanel panel) {

		//Remove the panelOn and panelOff panels from the pane.
		Pair<JPanel, JPanel> pair = panels.remove(panel);
		if (pair != null) {
			remove(pair.getFirst());
			remove(pair.getSecond());
		}

		//Modify the position counters to account for the removed interaction panels.
		Integer location = locations.remove(panel);
		if (location != null) {

			switch (location) {
				case SwingConstants.NORTH :
					north--;
					break;
				case SwingConstants.EAST :
					east--;
					break;
				case SwingConstants.SOUTH :
					south--;
					break;
				case SwingConstants.WEST :
					west--;
					break;
				default :
					System.err.println("Unknown interaction panel location. No position counters have been updated.");
					break;
			}
		}

		//Repaint to get rid of the old panel tab pictures.
		repaint();
	}

	/**
	 * List all registered interaction panels and their locations.
	 * 
	 * @return Map of interaction panels and their locations.
	 */
	public Map<ViewInteractionPanel, Integer> getViewInteractionPanels() {
		return new HashMap<ViewInteractionPanel, Integer>(locations);
	}

	private boolean isChild(Component c, final Component parent) {
		if (c == parent) {
			return true;
		} else if (c.getParent() == null) {
			return false;
		} else {
			return (c.getParent() == parent) || isChild(c.getParent(), parent);
		}
	}

	public synchronized void mouseMoved(MouseEvent e) {
		Point p = e.getPoint();
		if (e.getComponent() == getComponent()) {
			Point p2 = scroll.getViewport().getViewPosition();
			p.setLocation(p.getX() - p2.getX() + TAB_HEIGHT, p.getY() - p2.getY() + TAB_HEIGHT);
		}
		Component c = findComponentAt(p.x, p.y);
		if (c == null) {
			return;
		}
		if (c == this || isChild(c, getComponent())) {
			turnPanelOff();
		} else {
			// walk through the off panels
			for (Entry<ViewInteractionPanel, Pair<JPanel, JPanel>> entry : panels.entrySet()) {
				JPanel panelOn = entry.getValue().getFirst();
				JPanel panelOff = entry.getValue().getSecond();
				ViewInteractionPanel panel = entry.getKey();
				if (panelOff.getBounds().contains(p)) {
					if (panelOff == c || isParentPanel(c, panelOff)) {

						setSize(entry.getKey(), panelOff, panelOn);
						setLocation(entry.getKey(), panelOff, panelOn);
						turnPanelOff();
						panel.willChangeVisibility(true);
						panelOn.setVisible(true);
						panelOn.setEnabled(true);
						panelOff.setVisible(false);
						panelOff.setEnabled(false);
						visiblePanel = entry.getKey();
					}
				}
			}
		}
	}

	private boolean isParentPanel(Component topmost, JPanel panel) {

		Container c = topmost.getParent();
		while (c != null) {

			if (c == panel) {
				return true;
			}

			c = c.getParent();
		}

		return false;
	}

	private void turnPanelOff() {
		if (visiblePanel != null) {
			JPanel panelOn = panels.get(visiblePanel).getFirst();
			JPanel panelOff = panels.get(visiblePanel).getSecond();
			visiblePanel.willChangeVisibility(false);
			panelOn.setVisible(false);
			panelOn.setEnabled(false);
			panelOff.setVisible(true);
			panelOff.setEnabled(true);
			visiblePanel = null;
		}

	}

	public void mouseDragged(MouseEvent e) {
		// ignore!

	}

	/**
	 * Adds a button to one of the positions on the screen indicated by the
	 * location parameter. Should be SwingConstants.NORTH_EAST,
	 * SwingConstants.SOUTH_EAST SwingConstants.NORTH_WEST,
	 * SwingConstants.SOUTH_WEST
	 * 
	 * @param label
	 * @param listener
	 * @param location
	 */
	public void addButton(JLabel label, ActionListener listener, int location) {
		JButton button = factory.createButton("");
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.CENTER);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		label.setForeground(Color.WHITE);
		label.setBorder(BorderFactory.createEmptyBorder());
		label.setOpaque(false);

		button.setLayout(null);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.add(label);
		button.setToolTipText("Reposition the graph to the origin");
		button.addActionListener(listener);

		label.setBounds(0, 0, TAB_HEIGHT - 5, TAB_HEIGHT - 5);

		switch (location) {
			case SwingConstants.NORTH_WEST :
				button.setBounds(0, 0, TAB_HEIGHT, TAB_HEIGHT);
				buttons[0] = button;
				break;
			case SwingConstants.NORTH_EAST :
				button.setBounds(0, getWidth() - TAB_HEIGHT, TAB_HEIGHT, TAB_HEIGHT);
				buttons[1] = button;
				break;
			case SwingConstants.SOUTH_EAST :
				button.setBounds(getHeight() - TAB_HEIGHT, getWidth() - TAB_HEIGHT, TAB_HEIGHT, TAB_HEIGHT);
				buttons[2] = button;
				break;
			default :
				// SOUTH_WEST
				button.setBounds(getHeight() - TAB_HEIGHT, 0, TAB_HEIGHT, TAB_HEIGHT);
				buttons[3] = button;
		}

		this.add(button, JLayeredPane.PALETTE_LAYER);

	}

	protected void initialize() {
		// empty implementation. Can be overridden by subclasses
	}

	public void setSize(ViewInteractionPanel panel, JPanel panelOff, JPanel panelOn) {
		double w = panel.getWidthInView();
		double h = panel.getHeightInView();
		if (w > 1) {
			// fixed width
			w += 20;
		} else {
			// relative width
			w *= scroll.getWidth();
		}
		if (h > 1) {
			// fixed height
			h += 20;
		} else {
			// relative height
			h *= scroll.getHeight();
		}
		w = Math.min(w, scroll.getWidth() - 2 * TAB_HEIGHT);
		h = Math.min(h, scroll.getHeight() - 2 * TAB_HEIGHT);

		panel.getComponent().setSize((int) w, (int) h);
		panelOn.invalidate();
	}

	public void setLocation(ViewInteractionPanel panel, JPanel panelOff, JPanel panelOn) {
		int x = panelOff.getLocation().x;
		int y = panelOff.getLocation().y;
		switch (locations.get(panel)) {
			case SwingConstants.SOUTH : {
				y = panelOff.getLocation().y - panel.getComponent().getHeight() + 10;
			}
				//$FALL-THROUGH$
			case SwingConstants.NORTH : {
				panelOn.setSize(panel.getComponent().getWidth() + 20, panel.getComponent().getHeight() + TAB_HEIGHT);
				break;
			}
			case SwingConstants.EAST : {
				x = panelOff.getLocation().x - panel.getComponent().getWidth() + 10;
			}
				//$FALL-THROUGH$
			default : {
				panelOn.setSize(panel.getComponent().getWidth() + TAB_HEIGHT, panel.getComponent().getHeight() + 20);
			}
		}
		if (x + panelOn.getWidth() > getWidth()) {
			x = Math.max(TAB_HEIGHT, getWidth() - panelOn.getWidth());
		}
		if (y + panelOn.getHeight() > getHeight()) {
			y = Math.max(TAB_HEIGHT, getHeight() - panelOn.getHeight());
		}
		panelOn.setLocation(x, y);
		panelOn.invalidate();
	}

	/**
	 * Resizes the panels base don the current size of the layered pane.
	 */
	private void resize() {
		/*
		 * Get the size of the layered pane.
		 */

		for (Entry<ViewInteractionPanel, Pair<JPanel, JPanel>> entry : panels.entrySet()) {
			JPanel panelOn = entry.getValue().getFirst();
			JPanel panelOff = entry.getValue().getSecond();
			ViewInteractionPanel panel = entry.getKey();

			if (locations.get(panel) == SwingConstants.EAST) {
				//east
				panelOn.setLocation(getWidth() - TAB_HEIGHT, panelOn.getLocation().y);
				panelOff.setBounds(getWidth() - TAB_HEIGHT, panelOff.getLocation().y, TAB_HEIGHT + 10, TAB_WIDTH - 5);
			} else if (locations.get(panel) == SwingConstants.SOUTH) {
				// south
				panelOn.setLocation(panelOn.getLocation().x, getHeight() - TAB_HEIGHT);
				panelOff.setBounds(panelOff.getLocation().x, getHeight() - TAB_HEIGHT, TAB_WIDTH - 5, TAB_HEIGHT + 10);
			}
			setSize(panel, panelOff, panelOn);
			setLocation(panel, panelOff, panelOn);
		}

		for (int i = 1; i < 3; i++) {
			if (buttons[i] == null) {
				continue;
			}
			buttons[i].setLocation(i > 1 ? getHeight() - TAB_HEIGHT : 0, i < 3 ? getWidth() - TAB_HEIGHT : 0);
		}

		normalBounds = new Rectangle(TAB_HEIGHT, TAB_HEIGHT, getWidth() - 2 * TAB_HEIGHT, getHeight() - 2 * TAB_HEIGHT);
		scroll.setBounds(normalBounds);

		updated();
		//		invalidate();

	}

	public JScrollBar getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	public JScrollBar getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	/**
	 * Returns the zoom factor of the primary view.
	 * 
	 * @return The zoom factor of the primary view.
	 */
	public double getScale() {
		return scalable.getScale();
	}

	/**
	 * Sets the zoom factor of the primary view to the given factor.
	 * 
	 * @param d
	 *            The given factor.
	 */
	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	/**
	 * Clean up.
	 */
	public void cleanUp() {
		/*
		 * Clean up both views.
		 */
		if (getComponent() instanceof Cleanable) {
			((Cleanable) getComponent()).cleanUp();
		}
		scalable.removeUpdateListener(this);
		getComponent().removeMouseMotionListener(this);
	}

	/**
	 * Deals with change events.
	 */
	public void stateChanged(ChangeEvent e) {
		Object source = e.getSource();
		if (source instanceof JSlider) {
			/*
			 * Slider has been changed. Determine and set new zoom factor.
			 */
			scalable.setScale(((JSlider) source).getValue() / 100.0);
			getComponent().repaint();
			/*
			 * Update secondary view accordingly.
			 */
		}
	}

	/**
	 * Deals with garbage collection.
	 */
	@Override
	public void finalize() throws Throwable {
		try {
			/*
			 * We can now clean up.
			 */
			cleanUp();
		} finally {
			super.finalize();
		}
	}

	public void updated() {
		JComponent newComponent = scalable.getComponent();
		if (newComponent != getComponent()) {
			scroll.setViewportView(newComponent);
			if (getComponent() instanceof Cleanable) {
				((Cleanable) getComponent()).cleanUp();
			}
			getComponent().removeMouseMotionListener(this);

			component = newComponent;
			getComponent().addMouseMotionListener(this);
			invalidate();
		}
		for (ViewInteractionPanel panel : panels.keySet()) {
			// HV: Do not call setScalableComponent now, as it changes the originalAttributeMap of the scalable.
			//			panel.setScalableComponent(scalable);
			panel.updated();
		}
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public void scaleToFit() {
		scalable.setScale(1);
		double rx = scroll.getViewport().getExtentSize().getWidth()
				/ scalable.getComponent().getPreferredSize().getWidth();
		double ry = scroll.getViewport().getExtentSize().getHeight()
				/ scalable.getComponent().getPreferredSize().getHeight();
		scalable.setScale(Math.min(rx, ry));
	}

	public JComponent getComponent() {
		return component;
	}

}
