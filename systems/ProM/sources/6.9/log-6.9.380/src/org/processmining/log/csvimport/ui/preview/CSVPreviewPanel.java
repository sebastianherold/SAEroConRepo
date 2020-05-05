package org.processmining.log.csvimport.ui.preview;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.processmining.framework.util.ui.widgets.ProMScrollPane;
import org.processmining.framework.util.ui.widgets.ProMTableWithoutPanel;

public final class CSVPreviewPanel extends JPanel {

	private static final long serialVersionUID = 3007836604602201962L;

	private final class BatchUpdateDefaultTableModel extends DefaultTableModel {

		private static final long serialVersionUID = -3423057642229809442L;

		private BatchUpdateDefaultTableModel(Object[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		@SuppressWarnings("unchecked")
		public void addRows(List<String[]> rowData) {
			int firstRow = dataVector.size();
			for (String[] row : rowData) {
				dataVector.add(convertToVector(row));
			}
			int lastRow = dataVector.size() - 1;
			fireTableRowsInserted(firstRow, lastRow);
		}
	}

	private final BatchUpdateDefaultTableModel previewTableModel;
	private final JTable previewTable;
	private final JScrollPane mainScrollPane;

	private JTable datatypeTable;

	public CSVPreviewPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
		previewTableModel = new BatchUpdateDefaultTableModel(null, 0);
		previewTable = new ProMTableWithoutPanel(previewTableModel);
		previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		Enumeration<TableColumn> columns = previewTable.getColumnModel().getColumns();
		
		while (columns.hasMoreElements()) {
			final TableColumn column = columns.nextElement();
			column.setPreferredWidth(130);
			column.setCellEditor(new DefaultCellEditor(new JTextField()) {

				private static final long serialVersionUID = -2741181064062738008L;

				protected void fireEditingStopped() {
					this.cancelCellEditing();
					super.fireEditingStopped();
				}

				protected void fireEditingCanceled() {
					super.fireEditingCanceled();
				}

			});
		}
		previewTable.getColumnModel().setColumnSelectionAllowed(false);
		previewTable.getTableHeader().setReorderingAllowed(false);

		mainScrollPane = new ProMScrollPane(previewTable);
		
		add(mainScrollPane);
	}

	public void addRow(String[] data) {
		previewTableModel.addRow(data);
	}

	public void addRows(List<String[]> rows) {
		previewTableModel.addRows(rows);
	}

	public void setHeader(String[] header) {
		if (header == null) {
			previewTable.setTableHeader(null);
		} else {			
			Vector<String> columnIds = new Vector<String>();
			for (String obj: header) {
				if (obj == null) {
					columnIds.add("");
				} else {
					columnIds.add(obj);	
				}				
			}
			previewTableModel.setColumnIdentifiers(columnIds);
		}
	}

	public void clear() {
		previewTableModel.getDataVector().clear();
		previewTable.repaint();
	}

	public void refresh() {
		if (datatypeTable != null) {
			datatypeTable.repaint();
		}
	}

	public JTable getPreviewTable() {
		return previewTable;
	}

	public JScrollPane getMainScrollPane() {
		return mainScrollPane;
	}

}
