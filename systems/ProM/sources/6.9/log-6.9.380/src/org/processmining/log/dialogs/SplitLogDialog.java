package org.processmining.log.dialogs;

import java.awt.Dimension;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.parameters.SplitLogParameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class SplitLogDialog extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5028679044691450395L;

	public SplitLogDialog(final SplitLogParameters parameters, XLog log) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		Set<String> keys = new TreeSet<String>();
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (XAttribute attribute : event.getAttributes().values()) {
					if (attribute instanceof XAttributeLiteral) {
						keys.add(attribute.getKey());
					}
				}
			}
		}
		for (String key : keys) {
			listModel.addElement(key);
		}
		final ProMList<String> list = new ProMList<String>("Select attribute key", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String defaultKey = parameters.getKey();
		list.setSelection(defaultKey);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					parameters.setKey(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default key.
					 */
					list.setSelection(defaultKey);
					parameters.setKey(defaultKey);
				}
			}
		});
		list.setPreferredSize(new Dimension(10, 10));
		add(list, "0, 0");		
	}
}
