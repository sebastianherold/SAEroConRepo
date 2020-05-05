package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;

/**
 * @author michael
 * @param <T>
 * 
 */
public class ProMCheckBoxWithComboBox<T> extends ProMCheckBoxWithPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5157157777410895997L;

	private ProMComboBox<T> comboBox;

	/**
	 * @param checked
	 * @param hideIfNotChecked
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final boolean checked, final boolean hideIfNotChecked, final Iterable<T> items) {
		this(checked, hideIfNotChecked, ProMComboBox.toArray(items));
	}

	/**
	 * @param checked
	 * @param hideIfNotChecked
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final boolean checked, final boolean hideIfNotChecked, final Object[]... items) {
		super(checked, hideIfNotChecked);

		comboBox = new ProMComboBox<T>(items);
		getPanel().setLayout(new BorderLayout());
		getPanel().add(comboBox, BorderLayout.CENTER);

	}

	/**
	 * @param checked
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final boolean checked, final Iterable<T> items) {
		this(checked, true, items);
	}

	/**
	 * @param checked
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final boolean checked, final T[]... items) {
		this(checked, true, items);
	}

	/**
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final Iterable<T> items) {
		this(true, true, items);
	}

	/**
	 * @param items
	 */
	public ProMCheckBoxWithComboBox(final T[]... items) {
		this(true, true, items);
	}

	/**
	 * @return
	 */
	public ProMComboBox<T> getComboBox() {
		return comboBox;
	}

	/**
	 * @return
	 */
	public int getSelectedIndex() {
		return comboBox.getSelectedIndex();
	}

	/**
	 * @return
	 */
	public Object getSelectedItem() {
		return comboBox.getSelectedItem();
	}

	/**
	 * @param index
	 */
	public void setSelectedIndex(final int index) {
		comboBox.setSelectedIndex(index);
	}

	/**
	 * @param item
	 */
	public void setSelectedItem(final Object item) {
		comboBox.setSelectedItem(item);
	}
}
