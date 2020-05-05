package org.processmining.models.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.Pair;
import org.processmining.framework.util.collection.MultiSet;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.models.graphbased.directed.DirectedGraphElement;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ListSelectionPanel extends AbstractSelectionPanel<JList> {

	private static final long serialVersionUID = -196246314805797890L;
	static final Pair<Collection<? extends DirectedGraphElement>, String> NONE = new Pair<Collection<? extends DirectedGraphElement>, String>(
			Collections.<DirectedGraphElement>emptySet(), "None");
	private String name;

	public ListSelectionPanel(String name, String title, boolean interactive) {
		super(title, new JList());
		this.name = name;

		ListCellRenderer renderer = new ViewListCellRenderer(interactive);
		this.component.setCellRenderer(renderer);
		this.component.setSelectionModel(new ToggleSelectionModel());

		//component.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if (interactive) {
			component.addListSelectionListener(new ListSelectionListener() {

				@SuppressWarnings("unchecked")
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting()) {
						return;
					}
					ListSelectionModel lsm = component.getSelectionModel();
					int selectedIndex = lsm.getMinSelectionIndex();
					if (selectedIndex < 0) {
						unselectAll(true);
						component.clearSelection();
						return;
					}
					Pair<Collection<? extends DirectedGraphElement>, String> pair = (Pair<Collection<? extends DirectedGraphElement>, String>) component
							.getModel().getElementAt(selectedIndex);
					Collection<? extends DirectedGraphElement> selected = pair.getFirst();

					Map<DirectedGraphElement, String> labelled = new HashMap<DirectedGraphElement, String>(selected
							.size());
					// For multisets, label with occurance count
					if (selected instanceof MultiSet) {
						for (DirectedGraphElement elt : ((MultiSet<DirectedGraphElement>) selected).baseSet()) {
							labelled.put(elt, "" + ((MultiSet<DirectedGraphElement>) selected).occurrences(elt));
						}
					} else {
						for (DirectedGraphElement elt : selected) {
							labelled.put(elt, elt.getLabel());
						}
					}
					selectElements(labelled);

				}

			});
		}

		component.setModel(new DefaultListModel());
		component.setEnabled(false);
		((DefaultListModel) component.getModel()).addElement(NONE);

	}

	public void addElementCollection(Collection<? extends DirectedGraphElement> collection) {
		if (collection instanceof HTMLToString) {
			addElementCollection(collection, ((HTMLToString) collection).toHTMLString(true));
		} else {
			addElementCollection(collection, collection.toString());
		}
	}

	public void addElementCollection(Collection<? extends DirectedGraphElement> collection, String label) {
		Pair<Collection<? extends DirectedGraphElement>, String> pair = new Pair<Collection<? extends DirectedGraphElement>, String>(
				collection, label);
		if (!component.isEnabled()) {
			((DefaultListModel) component.getModel()).removeAllElements();
			component.setEnabled(true);
		}
		if (!((DefaultListModel) component.getModel()).contains(pair)) {
			((DefaultListModel) component.getModel()).addElement(pair);
		}
	}

	public void updated() {
		// Ignore
	}

	public String getPanelName() {
		return name;
	}

	public void setParent(ScalableViewPanel viewPanel) {
		// ignore
	}

}

/**
 * This DefaultSelectionModel subclass enables SINGLE_SELECTION mode and
 * overrides setSelectionInterval so that the first selection update in a
 * gesture (like mouse press, drag, release) toggles the current selection
 * state. A "gesture" starts when the first update to the selection model
 * occurs, and the gesture ends when the isAdjusting ListSelectionModel property
 * is set to false.
 */
class ToggleSelectionModel extends DefaultListSelectionModel {
	private static final long serialVersionUID = -8595521046515667658L;
	boolean gestureStarted = false;

	// HV: Allow only a single selection, as only one will be shown.
	public ToggleSelectionModel() {
		super ();
		this.setSelectionMode(SINGLE_SELECTION);
	}
	
	public void setSelectionInterval(int index0, int index1) {
		if (isSelectedIndex(index0) && !gestureStarted) {
			super.removeSelectionInterval(index0, index1);
		} else {
			super.setSelectionInterval(index0, index1);
		}
		gestureStarted = true;
	}

	public void setValueIsAdjusting(boolean isAdjusting) {
		if (isAdjusting == false) {
			gestureStarted = false;
		}
	}
}

class ViewListCellRenderer implements ListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -808355468668630456L;
	private static final CompoundBorder BORDER = BorderFactory.createCompoundBorder(BorderFactory
			.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(2, 5, 2, 5));;
	private static final CompoundBorder SELBORDER = BorderFactory.createCompoundBorder(BorderFactory
			.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(2, 5, 2, 5));

	private final static JLabel LABEL = SlickerFactory.instance().createLabel("test");
	private static final Border EMPTYBORDER = BorderFactory.createEmptyBorder(2, 5, 2, 5);
	private final boolean allowsClick;

	public ViewListCellRenderer(boolean allowsClick) {
		this.allowsClick = allowsClick;
		LABEL.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));

		LABEL.setBorder(EMPTYBORDER);
		LABEL.setOpaque(false);
		SlickerDecorator.instance().decorate(LABEL);
	}

	@SuppressWarnings("unchecked")
	public Component getListCellRendererComponent(JList component, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		Pair<Collection<? extends DirectedGraphElement>, String> pair = (Pair<Collection<? extends DirectedGraphElement>, String>) value;
		if (pair == ListSelectionPanel.NONE) {
			LABEL.setText("None");
			LABEL.setForeground(Color.DARK_GRAY);
			LABEL.setBorder(EMPTYBORDER);
			return LABEL;
		}
		String label = pair.getSecond().trim();
		LABEL.setText(fitLabelToWidth(LABEL, label, component.getWidth()));
		LABEL.setToolTipText(label);
		if (allowsClick) {
			LABEL.setForeground(isSelected ? Color.WHITE : Color.BLACK);
			LABEL.setBorder(isSelected ? SELBORDER : BORDER);
		} else {
			LABEL.setBorder(EMPTYBORDER);
		}

		return LABEL;

	}

	public static String fitLabelToWidth(JComponent c, String label, int maxWidth) {
		FontMetrics metrics = c.getFontMetrics(c.getFont());
		Graphics g = c.getGraphics();
		boolean abbreviated = false;
		int width = Integer.MAX_VALUE;
		while (true) {
			if (label.length() < 2) {
				break;
			}
			String test = label;
			if (abbreviated) {
				test += "...";
			}
			Rectangle2D stringBounds = metrics.getStringBounds(test, g);
			width = (int) stringBounds.getWidth();
			if (width > maxWidth) {
				label = label.substring(0, label.length() - 1);
				if (!abbreviated) {
					abbreviated = true;
				}
			} else {
				break;
			}
		}
		if (abbreviated) {
			label += "...";
		}
		return label;
	}
}
