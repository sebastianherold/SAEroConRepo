package org.processmining.framework.util.ui.widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Enumeration;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Adapted from {@link ProMTable}, but without a surrounding {@link JPanel}.
 * 
 * @author F. Mannhardt
 *
 */
public class ProMTableWithoutPanel extends JTable {

	private static final long serialVersionUID = 9039927753456671146L;
	
	public ProMTableWithoutPanel(TableModel model, TableColumnModel columnModel) {
		super(model, columnModel);
		setup();
	}

	public ProMTableWithoutPanel(TableModel model) {
		super(model);
		setup();
	}

	@SuppressWarnings("serial")
	private void setup() {
		setBackground(WidgetColors.COLOR_LIST_BG);
		setForeground(WidgetColors.COLOR_LIST_FG);
		setSelectionBackground(WidgetColors.COLOR_LIST_SELECTION_BG);
		setSelectionForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		setGridColor(WidgetColors.COLOR_ENCLOSURE_BG);
		setFont(getFont().deriveFont(Font.BOLD));
		
		getTableHeader().setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		getTableHeader().setOpaque(true);
		getTableHeader().setForeground(Color.WHITE);
		getTableHeader().setFont(getTableHeader().getFont().deriveFont(13f).deriveFont(Font.BOLD));
		getTableHeader().setReorderingAllowed(false);
		DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				if (!isSelected) {
					c.setBackground(table.getBackground());
					c.setForeground(table.getForeground());
				}
				return c;
			}

		};
		
		Enumeration<TableColumn> columns = getColumnModel().getColumns();
		while (columns.hasMoreElements()) {
			final TableColumn column = columns.nextElement();
			column.setPreferredWidth(200);
			column.setCellRenderer(cellRenderer);
			column.setCellEditor(new DefaultCellEditor(new JTextField()) {

				protected void fireEditingStopped() {
					this.cancelCellEditing();
					super.fireEditingStopped();
				}

				protected void fireEditingCanceled() {
					super.fireEditingCanceled();
				}

			});
		}
		
		setColumnSelectionAllowed(false);		
		setShowHorizontalLines(false);

		setMinimumSize(null);
		setMaximumSize(null);
		setPreferredSize(null);
		setBackground(null);
		setForeground(null);
		setOpaque(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

}
