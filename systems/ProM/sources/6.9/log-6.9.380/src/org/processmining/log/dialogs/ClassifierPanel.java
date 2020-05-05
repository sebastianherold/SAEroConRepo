package org.processmining.log.dialogs;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.parameters.UpdateParameter;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class ClassifierPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7242932924333294111L;

	public ClassifierPanel(List<XEventClassifier> classifiers, final ClassifierParameter classifierParameter) {
		this (classifiers, classifierParameter, null);
	}
	
	public ClassifierPanel(List<XEventClassifier> classifiers, final ClassifierParameter classifierParameter, final UpdateParameter updateParameter) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		DefaultListModel<XEventClassifier> listModel = new DefaultListModel<XEventClassifier>();
		for (XEventClassifier classifier: classifiers) {
			listModel.addElement(classifier);
		}
		final ProMList<XEventClassifier> list = new ProMList<XEventClassifier>("Select classifier", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final XEventClassifier defaultClassifier = classifierParameter.getClassifier();
		list.setSelection(defaultClassifier);
		classifierParameter.setClassifier(wrap(defaultClassifier));
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<XEventClassifier> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					classifierParameter.setClassifier(wrap(selected.get(0)));
					if (updateParameter != null) {
						updateParameter.update();
					}
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultClassifier);
					classifierParameter.setClassifier(wrap(defaultClassifier));
					if (updateParameter != null) {
						updateParameter.update();
					}
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");

	}
	
	private XEventClassifier wrap(XEventClassifier classifier) {
//		if (classifier instanceof StartEndClassifier) {
			return classifier;
//		}
//		return new StartEndClassifier(classifier);
	}

}
