package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Collection;

import javax.swing.JPanel;

/**
 * @author michael
 * 
 */
public class ProMComboBoxWithTextField extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3750834515241351840L;
	private final ProMComboBox<?> comboBox;
	private final ProMTextField textField;

	/**
	 * @param items
	 */
	public ProMComboBoxWithTextField(final Collection<Object> items) {
		this(items.toArray());
	}

	/**
	 * @param items
	 * @param text
	 */
	public ProMComboBoxWithTextField(final Collection<Object> items, final String text) {
		this(items.toArray(), text);
	}

	/**
	 * @param items
	 */
	public ProMComboBoxWithTextField(final Object[] items) {
		this(items, "");
	}

	/**
	 * @param items
	 * @param text
	 */
	public ProMComboBoxWithTextField(final Object[] items, final String text) {

		comboBox = new ProMComboBox<Object>(items);
		textField = new ProMTextField(text);

		comboBox.setMinimumSize(new Dimension(50, 30));
		comboBox.setPreferredSize(new Dimension(150, 30));

		textField.setMinimumSize(new Dimension(50, 30));
		textField.setPreferredSize(new Dimension(250, 30));

		setLayout(new BorderLayout());
		setOpaque(false);

		this.add(comboBox, BorderLayout.WEST);
		this.add(textField, BorderLayout.EAST);
	}

	/**
	 * @return
	 */
	public ProMComboBox<?> getComboBox() {
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
	 * @return
	 */
	public String getText() {
		return textField.getText();
	}

	/**
	 * @return
	 */
	public ProMTextField getTextField() {
		return textField;
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

	/**
	 * @param text
	 */
	public void setText(final String text) {
		textField.setText(text);
	}

}
