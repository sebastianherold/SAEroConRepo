package org.processmining.framework.util.ui.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import csplugins.id.mapping.ui.CheckComboBox;

/**
 * @author michael
 * 
 */
public class ProMCheckComboBox extends CheckComboBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param <T>
	 * @param values
	 * @return
	 */
	public static <T> Object[] toArray(final Iterable<T> values) {
		final ArrayList<T> valueList = new ArrayList<T>();
		for (final T value : values) {
			valueList.add(value);
		}
		return valueList.toArray();
	}

	private final BorderPanel borderPanel;
	private final BorderPanel buttonPanel;

	/**
	 * @param <T>
	 * @param values
	 */
	public <T> ProMCheckComboBox(final Iterable<T> values) {
		this(ProMCheckComboBox.toArray(values));
	}

	/**
	 * @param values
	 */
	public ProMCheckComboBox(final Object[] values) {
		super(values);

		borderPanel = new BorderPanel(15, 3);
		borderPanel.setOpaque(true);
		borderPanel.setBackground(WidgetColors.COLOR_LIST_BG);
		borderPanel.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		buttonPanel = new BorderPanel(15, 3);
		buttonPanel.setOpaque(true);
		buttonPanel.setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		buttonPanel.setForeground(WidgetColors.COLOR_ENCLOSURE_BG);
		setOpaque(false);
		setBackground(WidgetColors.COLOR_LIST_BG);
		setForeground(WidgetColors.COLOR_LIST_FG);
		setMinimumSize(new Dimension(200, 30));
		setMaximumSize(new Dimension(1000, 30));
		setPreferredSize(new Dimension(1000, 30));

		//setUI(new ProMComboBoxUI(this));
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		final Rectangle bounds = getBounds();
		buttonPanel.setBounds(bounds);
		buttonPanel.paintComponent(g);
		final Dimension d = new Dimension();
		d.setSize(bounds.getWidth() - bounds.getHeight(), bounds.getHeight());
		bounds.setSize(d);
		borderPanel.setBounds(bounds);
		borderPanel.paintComponent(g);
		super.paintComponent(g);
	}
}
