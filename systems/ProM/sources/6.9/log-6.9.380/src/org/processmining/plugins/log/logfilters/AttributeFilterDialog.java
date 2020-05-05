package org.processmining.plugins.log.logfilters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.framework.util.ui.widgets.BorderPanel;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class AttributeFilterDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5477222861834208877L;
	private Map<String, ProMList<String>> lists;
	private Map<String, JCheckBox> mustHaves;
	private ProMTextField textField;
	private JCheckBox removeEmptyTraces;
	AttributeFilterParameters parameters;
	
	public AttributeFilterDialog(PluginContext context, AttributeFilterParameters parameters, String namePostfix) {
		this.parameters = parameters;
		Map<String, List<String>> values = new HashMap<String, List<String>>();
		for (String key : parameters.getFilter().keySet()) {
			values.put(key, new ArrayList<String>());
			values.get(key).addAll(parameters.getFilter().get(key));
			Collections.sort(values.get(key), new AlphanumComparator());
			context.getProgress().inc();
		}

		double size[][] = { { 80, TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30, 30 } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		lists = new HashMap<String, ProMList<String>>();
		mustHaves = new HashMap<String, JCheckBox>();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		List<String> sortedKeys = new ArrayList<String>();
		sortedKeys.addAll(values.keySet());
		Collections.sort(sortedKeys, new AlphanumComparator());
		for (String key : sortedKeys) {
			DefaultListModel<String>listModel = new DefaultListModel<String>();
			int[] selected = new int[values.get(key).size()];
			int i = 0;
			for (String value: values.get(key)) {
				listModel.addElement(value);
				selected[i] = i;
				i++;
			}
			context.getProgress().inc();
			ProMList<String> list = new ProMList<String>("Select values", listModel);
			lists.put(key, list);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			list.setSelectedIndices(selected);
			list.setPreferredSize(new Dimension(100, 100));
			context.getProgress().inc();
			
			JCheckBox checkBox = SlickerFactory.instance().createCheckBox("Remove if no value provided", false);
			checkBox.setSelected(parameters.getMustHaves().contains(key));
			mustHaves.put(key, checkBox);
			
			JPanel panel = new BorderPanel(5, 2);
			double panelSize[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL, 30 } };
			panel.setLayout(new TableLayout(panelSize));
			panel.add(lists.get(key), "0, 0");
			panel.add(mustHaves.get(key), "0, 1");
			
			tabbedPane.add(key, panel);
		}
		this.add(tabbedPane, "0, 0, 1, 0");
		
		textField = new ProMTextField();
		textField.setText(parameters.getName() + namePostfix);
		add(textField, "1, 1");
		textField.setPreferredSize(new Dimension(100, 25));
		add(new JLabel("Log name:"), "0, 1");

		removeEmptyTraces = SlickerFactory.instance().createCheckBox("Remove trace if all events were removed", parameters.isRemoveEmptyTraces()); 
		add(removeEmptyTraces, "0, 2, 1, 2");
	}
	
	public void applyFilter() {
		Set<String> mustHaves = new HashSet<String>();
		for (String key : lists.keySet()) {
			parameters.getFilter().get(key).clear();
			parameters.getFilter().get(key).addAll(lists.get(key).getSelectedValuesList());
			if (this.mustHaves.get(key).isSelected()) {
				mustHaves.add(key);
			}
		}
		parameters.setMustHave(mustHaves);
		parameters.setName(textField.getText());
		parameters.setRemoveEmptyTraces(removeEmptyTraces.isSelected());
	}
}
