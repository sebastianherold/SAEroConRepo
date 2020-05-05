package org.processmining.framework.util.ui.widgets;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * @author michael
 * @param <E>
 * 
 */
@SuppressWarnings("rawtypes")
public class ProMComboBox<E> extends JComboBox/* <E> */{

	private static final long serialVersionUID = 1L;

	/**
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
	
	@SuppressWarnings("unchecked")
	public ProMComboBox(final ComboBoxModel<E> model) {
		super(model);
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

		setUI(new ProMComboBoxUI(this));
	}
	
	
	public ProMComboBox(final ComboBoxModel<E> model, boolean noSize) {
		this(model);
		resetSizes();
	}

	public ProMComboBox<E> resetSizes() {
		setPreferredSize(null);
		setMaximumSize(null);
		setMinimumSize(null);
		return this;
	}

	/**
	 * @param values
	 */
	public ProMComboBox(final Iterable<E> values) {
		this(ProMComboBox.toArray(values));
	}

	/**
	 * @param values
	 */
	@SuppressWarnings({ "unchecked" })
	public ProMComboBox(final Object[] values) {
		this(new DefaultComboBoxModel<E>((E[]) values));
	}

	/**
	 * @param values
	 */
	@SuppressWarnings("unchecked")
	public void addAllItems(final Iterable<E> values) {
		for (final E value : values) {
			addItem(value);
		}
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
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
