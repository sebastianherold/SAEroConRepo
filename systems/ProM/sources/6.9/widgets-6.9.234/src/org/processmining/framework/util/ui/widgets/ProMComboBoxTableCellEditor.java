package org.processmining.framework.util.ui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

/**
 * @author michael
 * 
 * @param <T>
 */
public class ProMComboBoxTableCellEditor<T> extends AbstractCellEditor implements TableCellEditor {
	/**
		 * 
		 */
	private static final long serialVersionUID = 0;
	private T configured;
	protected ProMComboBox<T> comboBox;

	/**
	 * @param values
	 */
	public ProMComboBoxTableCellEditor(final Collection<T> values) {
		comboBox = new ProMComboBox<T>(values);
		comboBox.addActionListener(new ActionListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void actionPerformed(final ActionEvent e) {
				setValue((T) comboBox.getSelectedItem());
			}
		});
	}

	/**
	 * @param values
	 */
	public ProMComboBoxTableCellEditor(final T... values) {
		this(Arrays.asList(values));
	}

	/**
	 * @see javax.swing.CellEditor#getCellEditorValue()
	 */
	@Override
	public Object getCellEditorValue() {
		return configured;
	}

	/**
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
	 *      java.lang.Object, boolean, int, int)
	 */
	@Override
	public ProMComboBox<T> getTableCellEditorComponent(final JTable table, final Object value,
			final boolean isSelected, final int row, final int column) {
		comboBox.setSelectedItem(value);
		return comboBox;
	}

	protected void setValue(final T value) {
		configured = value;
	}
}
