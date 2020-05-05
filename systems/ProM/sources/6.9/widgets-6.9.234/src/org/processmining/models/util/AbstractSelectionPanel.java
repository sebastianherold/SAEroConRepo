package org.processmining.models.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.AttributeMapOwner;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.jgraph.ProMJGraph;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public abstract class AbstractSelectionPanel<C extends JComponent> extends JPanel implements ViewInteractionPanel {

	private static final long serialVersionUID = 8436979211504873546L;
	protected ProMJGraph graph;
	protected SlickerFactory factory = SlickerFactory.instance();
	protected SlickerDecorator decorator = SlickerDecorator.instance();

	protected final String title;
	protected ViewSpecificAttributeMap originalSpecificMap;
	protected ViewSpecificAttributeMap viewSpecificMap;
	private Map<DirectedGraphElement, String> selectedNodes = new HashMap<DirectedGraphElement, String>();
	protected final JScrollPane scroll;
	protected final C component;

	public AbstractSelectionPanel(String title, C component) {
		super(new BorderLayout());
		this.title = title;
		this.scroll = new JScrollPane(component);
		this.component = component;
		this.component.setOpaque(false);

		// add the title
		JLabel panelTitle = factory.createLabel(title);
		panelTitle.setHorizontalTextPosition(SwingConstants.CENTER);
		panelTitle.setVerticalTextPosition(SwingConstants.CENTER);
		panelTitle.setForeground(Color.WHITE);
		panelTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 16));
		panelTitle.setOpaque(false);
		this.add(panelTitle, BorderLayout.NORTH);
		this.setOpaque(false);

		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		scroll.getViewport().setOpaque(false);
		scroll.setOpaque(false);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.getVerticalScrollBar().setOpaque(false);

		this.add(scroll, BorderLayout.CENTER);
	}

	public JComponent getComponent() {
		return this;
	}

	public void setScalableComponent(ScalableComponent scalable) {
		if (scalable instanceof ProMJGraph) {
			this.graph = (ProMJGraph) scalable;
			this.viewSpecificMap = graph.getViewSpecificAttributes();

			originalSpecificMap = new ViewSpecificAttributeMap();
			for (AttributeMapOwner o : viewSpecificMap.keySet()) {
				for (String key : viewSpecificMap.keySet(o)) {
					originalSpecificMap.putViewSpecific(o, key, viewSpecificMap.get(o, key));
				}
			}
		} else {
			throw new IllegalArgumentException("Scalable needs to be instance of ProMJGraph");
		}
	}

	public double getHeightInView() {
		return 140;
	}

	public double getWidthInView() {
		return 250;
	}

	protected synchronized void unselectAll(boolean signal) {
		if (viewSpecificMap == null) {
			return;
		}
		ArrayList<AttributeMapOwner> toUpdate = new ArrayList<AttributeMapOwner>(viewSpecificMap.keySet());
		for (AttributeMapOwner p : toUpdate) {
			// copy the original keys back into viewSpecific
			viewSpecificMap.clearViewSpecific(p);
			for (String key : originalSpecificMap.keySet(p)) {
				viewSpecificMap.putViewSpecific(p, key, originalSpecificMap.get(p, key));
			}
		}
		selectedNodes.clear();

		if (signal) {
			// HV: Convert toUpdate to a set of AttributeMapOwners, as otherwise the update 
			//     gets an array containing the set as only element, which it does not handle properly.
			graph.update(new HashSet<AttributeMapOwner>(toUpdate));
		}
	}

	public void willChangeVisibility(boolean to) {
		if (to) {
			// HV: Also show if the selection has become empty.
			//			if (!selectedNodes.keySet().isEmpty()) {
			selectElements(new HashMap<DirectedGraphElement, String>(selectedNodes));
			//			}
		}
	}

	protected synchronized <T extends DirectedGraphElement> void selectElements(Map<T, String> labelledSelection) {
		unselectAll(false);
		selectedNodes.putAll(labelledSelection);

		for (DirectedGraphElement p : labelledSelection.keySet()) {
			String newLabel = labelledSelection.get(p);
			String tooltip = p.getAttributeMap().get(AttributeMap.LABEL,
					viewSpecificMap.get(p, AttributeMap.LABEL, newLabel));

			// set the attributes
			viewSpecificMap.putViewSpecific(p, AttributeMap.FILLCOLOR, Color.ORANGE);
			viewSpecificMap.putViewSpecific(p, AttributeMap.LABEL, newLabel);
			viewSpecificMap.putViewSpecific(p, AttributeMap.TOOLTIP, tooltip);
			viewSpecificMap.putViewSpecific(p, AttributeMap.SHOWLABEL, !newLabel.isEmpty());
		}

		// refresh the figure

		graph.update(labelledSelection.keySet());
	}

}
