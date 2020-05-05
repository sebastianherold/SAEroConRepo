package org.processmining.framework.util.ui.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Collection;

import csplugins.id.mapping.ui.CheckComboBox;

/**
 * @author Massimiliano de Leoni
 * 
 */
public class ProMComboCheckBox extends CheckComboBox {

	private static final long serialVersionUID = 1L;

	private BorderPanel borderPanel;
	private BorderPanel buttonPanel;

	public ProMComboCheckBox(@SuppressWarnings("rawtypes") Collection objs) {
		super(objs);
	}

	public ProMComboCheckBox(Object[] attributes, boolean selected) {
		super(attributes, selected);
		init();
	}

	public ProMComboCheckBox(String[] attributes, boolean selected) {
		super(attributes, selected);
		init();
	}

	private final void init() {
		borderPanel = new BorderPanel(15, 3);
		borderPanel.setOpaque(true);
		borderPanel.setBackground(WidgetColors.COLOR_LIST_BG);
		borderPanel.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		buttonPanel = new BorderPanel(15, 3);
		buttonPanel.setOpaque(true);
		buttonPanel.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		buttonPanel.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		setOpaque(false);
		setBackgroundNotSelected(WidgetColors.COLOR_LIST_BG);
		setForegroundNotSelected(WidgetColors.COLOR_LIST_FG);
		setBackgroundSelected(WidgetColors.COLOR_LIST_SELECTION_BG);
		setForegroundSelected(WidgetColors.COLOR_LIST_SELECTION_FG);
		setMinimumSize(new Dimension(200, 30));
		setMaximumSize(new Dimension(1000, 30));
		setPreferredSize(new Dimension(1000, 30));

		setUI(new ProMComboBoxUI(this));
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (!Boolean.TRUE.equals(getClientProperty("JComboBox.isTableCellEditor"))) {
			final Rectangle bounds = getBounds();
			buttonPanel.setBounds(bounds);
			buttonPanel.paintComponent(g);
			final Dimension d = new Dimension();
			d.setSize(bounds.getWidth() - bounds.getHeight(), bounds.getHeight());
			bounds.setSize(d);
			borderPanel.setBounds(bounds);
			borderPanel.paintComponent(g);
		}
		super.paintComponent(g);
	}
}
