package org.processmining.log.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityParameters;

public class LogCentralityDialog extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8634000584911454942L;

	public LogCentralityDialog(final UIPluginContext context, final XLog log, final LogCentrality centrality, final LogCentralityParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		DefaultListModel<XEventClassifier> listModel = new DefaultListModel<XEventClassifier>();
		for (XEventClassifier classifier: log.getClassifiers()) {
			listModel.addElement(classifier);
		}
		final ProMList<XEventClassifier> list = new ProMList<XEventClassifier>("Select Happy Classifier", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final XEventClassifier defaultClassifier = parameters.getClassifier();
		list.setSelection(defaultClassifier);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<XEventClassifier> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					parameters.setClassifier(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultClassifier);
					parameters.setClassifier(defaultClassifier);
				}
				centrality.setClassifier(context, parameters);
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");	
	}
}
