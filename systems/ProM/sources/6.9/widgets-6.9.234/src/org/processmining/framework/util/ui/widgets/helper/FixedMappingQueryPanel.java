package org.processmining.framework.util.ui.widgets.helper;

import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMTable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

class FixedMappingQueryPanel<S,T> extends JPanel {

	private static final String NO_MAPPING = "NONE";

	private static final long serialVersionUID = 4946819373227598703L;

	private ProMTable mappingTable;
	private ImmutableList<S> sourceList;
	private ImmutableList<T> targetList;

	@SuppressWarnings("serial")
	public FixedMappingQueryPanel(String text, Iterable<S> sources, Iterable<T> targets, Map<S, T> defaultValues) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(null);

		sourceList = ImmutableList.copyOf(sources);
		targetList = ImmutableList.copyOf(targets);

		DefaultTableModel tableModel = new DefaultTableModel(sourceList.size(), 2) {

			public boolean isCellEditable(int row, int column) {
				if (column == 0) {
					return false;
				}
				return super.isCellEditable(row, column);
			}

		};

		mappingTable = new ProMTable(tableModel);
		mappingTable.setPreferredSize(new Dimension(600, 400));
		mappingTable.setMaximumSize(null);
		mappingTable.setMinimumSize(null);
		mappingTable.getTableHeader().getColumnModel().getColumn(0).setHeaderValue("Source");
		mappingTable.getTableHeader().getColumnModel().getColumn(0).setPreferredWidth(200);
		mappingTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue("Target");
		mappingTable.getTableHeader().getColumnModel().getColumn(1).setPreferredWidth(200);

		mappingTable.getColumnModel().getColumn(1)
				.setCellEditor(new DefaultCellEditor(new JComboBox<T>(new Vector<>(targetList))) {

					protected void fireEditingStopped() {
						this.cancelCellEditing();
						super.fireEditingStopped();
					}

					protected void fireEditingCanceled() {
						super.fireEditingCanceled();
					}

				});

		int i = 0;
		for (S source : sourceList) {
			mappingTable.getTable().getModel().setValueAt(source, i++, 0);
		}

		for (i = 0; i < sourceList.size(); i++) {
			T defaultValue = defaultValues.get(sourceList.get(i));
			if (defaultValue != null) {
				mappingTable.getTable().getModel().setValueAt(defaultValue, i, 1);
			} else {
				mappingTable.getTable().getModel().setValueAt(null, i, 1);
			}
		}

		add(mappingTable);
	}

	public InteractionResult getUserChoice(UIPluginContext context, String query) {
		return context.showConfiguration(query, this);
	}
	
	public InteractionResult getUserChoice(Component view, String query) {
		String[] options = new String[] { "Confirm", "Cancel" };
		int result = JOptionPane.showOptionDialog(view, this, query, JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		return result == 0 ? InteractionResult.CONTINUE : InteractionResult.CANCEL;
	}

	public Map<S, T> getResult() {
		Map<S, T> result = new HashMap<>();

		int i = 0;
		UnmodifiableIterator<S> iterator = sourceList.iterator();
		while (iterator.hasNext()) {
			S source = iterator.next();
			@SuppressWarnings("unchecked")
			T target = (T) mappingTable.getTable().getModel().getValueAt(i++, 1);
			if (!target.equals(null)) {
				result.put(source, target);
			}
		}

		return result;
	}

}