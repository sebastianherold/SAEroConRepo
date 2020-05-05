package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.fluxicon.slickerbox.components.RoundedPanel;

/**
 * Table with SlickerBox L&F
 * 
 * @author mwesterg
 * 
 */
public class ProMTable extends RoundedPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTable table;

	/**
	 * @param model
	 */
	public ProMTable(final TableModel model) {
		this(model, null);
	}

	/**
	 * @param model
	 * @param columnModel
	 */
	public ProMTable(final TableModel model, final TableColumnModel columnModel) {
		super(10, 5, 0);

		table = createTable(model, columnModel);
		table.setBackground(WidgetColors.COLOR_LIST_BG);
		table.setForeground(WidgetColors.COLOR_LIST_FG);
		table.setSelectionBackground(WidgetColors.COLOR_LIST_SELECTION_BG);
		table.setSelectionForeground(WidgetColors.COLOR_LIST_SELECTION_FG);

		final ProMScrollPane scroller = new ProMScrollPane(table);

		table.getTableHeader().setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		table.getTableHeader().setOpaque(false);
		table.getTableHeader().setForeground(WidgetColors.COLOR_LIST_SELECTION_FG);
		table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
		table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(13f).deriveFont(Font.BOLD));
		table.getTableHeader().setAlignmentX(Component.CENTER_ALIGNMENT);
		table.setShowHorizontalLines(false);
		table.setGridColor(WidgetColors.COLOR_ENCLOSURE_BG);
		table.setFont(table.getFont().deriveFont(Font.BOLD));

		scroller.getViewport().setBackground(WidgetColors.COLOR_LIST_BG);
		table.getTableHeader().setDefaultRenderer(new HeaderRenderer());

		setBackground(WidgetColors.COLOR_ENCLOSURE_BG);
		setLayout(new BorderLayout());
		setMinimumSize(new Dimension(200, 100));
		setMaximumSize(new Dimension(1000, 1000));
		setPreferredSize(new Dimension(1000, 200));
		add(Box.createHorizontalStrut(5), BorderLayout.WEST);
		add(scroller, BorderLayout.CENTER);
		add(Box.createHorizontalStrut(5), BorderLayout.EAST);

		table.setColumnSelectionAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
	}

	/**
	 * @see java.awt.Component#addMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void addMouseListener(final MouseListener l) {
		table.addMouseListener(l);
	}

	/**
	 * @param from
	 * @param to
	 */
	public void addRowSelectionInterval(final int from, final int to) {
		table.addRowSelectionInterval(from, to);
	}

	/**
	 * 
	 */
	public void clearSelection() {
		table.clearSelection();
	}

	/**
	 * @param point
	 * @return
	 */
	public int columnAtPoint(final Point point) {
		return table.columnAtPoint(point);
	}

	/**
	 * @param row
	 * @param col
	 * @return
	 */
	public TableCellEditor getCellEditor(final int row, final int col) {
		return table.getCellEditor(row, col);
	}

	/**
	 * @return
	 */
	public TableColumnModel getColumnModel() {
		return table.getColumnModel();
	}

	/**
	 * @return
	 */
	public boolean getColumnSelectionAllowed() {
		return table.getColumnSelectionAllowed();
	}

	/**
	 * @return
	 */
	public boolean getRowSelectionAllowed() {
		return table.getRowSelectionAllowed();
	}

	/**
	 * @return
	 */
	public RowSorter<? extends TableModel> getRowSorter() {
		return table.getRowSorter();
	}

	/**
	 * @return
	 */
	public int getSelectedColumn() {
		return table.getSelectedColumn();
	}

	/**
	 * @return
	 */
	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	/**
	 * @return
	 */
	public ListSelectionModel getSelectionModel() {
		return table.getSelectionModel();
	}

	/**
	 * @return
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * @return
	 */
	public JTableHeader getTableHeader() {
		return table.getTableHeader();
	}

	/**
	 * @param row
	 * @param column
	 * @return
	 */
	public Object getValueAt(final int row, final int column) {
		return table.getValueAt(row, column);
	}

	/**
	 * @see java.awt.Component#removeMouseListener(java.awt.event.MouseListener)
	 */
	@Override
	public void removeMouseListener(final MouseListener l) {
		table.removeMouseListener(l);
	}

	/**
	 * @param point
	 * @return
	 */
	public int rowAtPoint(final Point point) {
		return table.rowAtPoint(point);
	}

	/**
	 * @param create
	 */
	public void setAutoCreateRowSorter(final boolean create) {
		table.setAutoCreateRowSorter(create);
	}

	/**
	 * @param mode
	 */
	public void setAutoResizeMode(final int mode) {
		table.setAutoResizeMode(mode);
	}

	/**
	 * @param allowed
	 */
	public void setColumnSelectionAllowed(final boolean allowed) {
		table.setColumnSelectionAllowed(allowed);
	}

	/**
	 * @param column
	 * @param width
	 */
	public void setPreferredWidth(final int column, final int width) {
		table.getColumnModel().getColumn(column).setPreferredWidth(width);
	}

	/**
	 * @param allowed
	 */
	public void setRowSelectionAllowed(final boolean allowed) {
		table.setRowSelectionAllowed(allowed);
	}

	/**
	 * @param column
	 * @param comparator
	 */
	public void setRowSorter(final int column, final Comparator<?> comparator) {
		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
		sorter.setSortsOnUpdates(true);
		sorter.setComparator(column, null);
		setRowSorter(sorter);
		sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(column, SortOrder.ASCENDING)));
	}

	/**
	 * @param sorter
	 */
	public void setRowSorter(final RowSorter<? extends TableModel> sorter) {
		table.setRowSorter(sorter);
	}

	/**
	 * @param sorter
	 */
	public void setRowSorter(final TableRowSorter<? extends TableModel> sorter) {
		table.setRowSorter(sorter);
		sorter.sort();
	}

	/**
	 * @param mode
	 */
	public void setSelectionMode(final int mode) {
		table.setSelectionMode(mode);
	}

	protected JTable createTable(final TableModel model, final TableColumnModel columnModel) {
		return new JTable(model, columnModel);
	}

}