package org.processmining.logskeleton.pdc2017.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.logskeleton.algorithms.LogPreprocessorAlgorithm;
import org.processmining.logskeleton.pdc2017.parameters.PDC2017TestParameters;
import org.processmining.pdc2017.algorithms.PDC2017Set;

public class PDC2017TestDialog extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 378756044807568628L;
	
	public PDC2017TestDialog(final PDC2017TestParameters parameters) {
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<PDC2017Set> setListModel = new DefaultListModel<PDC2017Set>();
		int[] selectedIndices = new int[parameters.getSets().size()];
		int i = 0;
		int j = 0;
		List<PDC2017Set> sortedSets = new ArrayList<PDC2017Set>();
		sortedSets.addAll(parameters.getAllSets());
		Collections.sort(sortedSets, new Comparator<PDC2017Set>() {
			public int compare(PDC2017Set set1, PDC2017Set set2) {
				return set1.toString().compareTo(set2.toString());
			}
		});
		for (PDC2017Set set : sortedSets) {
			setListModel.addElement(set);
			if (parameters.getSets().contains(set)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<PDC2017Set> setList = new ProMList<PDC2017Set>("Select log set(s)", setListModel);
		setList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setList.setSelectedIndices(selectedIndices);
		setList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<PDC2017Set> selectedSets = setList.getSelectedValuesList();
				parameters.setSets(new HashSet<PDC2017Set>(selectedSets));
			}
		});
		setList.setPreferredSize(new Dimension(100, 100));
		add(setList, "0, 0");

		DefaultListModel<Integer> nrListModel = new DefaultListModel<Integer>();
		selectedIndices = new int[parameters.getAllNrs().size()];
		i = 0;
		j = 0;
		for (int nr : parameters.getAllNrs()) {
			nrListModel.addElement(nr);
			if (parameters.getNrs().contains(nr)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<Integer> nrList = new ProMList<Integer>("Select log number(s)", nrListModel);
		nrList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		nrList.setSelectedIndices(selectedIndices);
		nrList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<Integer> selectedNrs = nrList.getSelectedValuesList();
				parameters.setNrs(new HashSet<Integer>(selectedNrs));
			}
		});
		nrList.setPreferredSize(new Dimension(100, 100));
		add(nrList, "1, 0");
		
		DefaultListModel<LogPreprocessorAlgorithm> preprocessors = new DefaultListModel<LogPreprocessorAlgorithm>();
		selectedIndices = new int[1];
		i = 0;
		j = 0;
		List<LogPreprocessorAlgorithm> sortedPreprocessors = new ArrayList<LogPreprocessorAlgorithm>();
		sortedPreprocessors.addAll(parameters.getAllPreprocessors());
		Collections.sort(sortedPreprocessors, new Comparator<LogPreprocessorAlgorithm>() {
			public int compare(LogPreprocessorAlgorithm prep1, LogPreprocessorAlgorithm prep2) {
				return prep1.toString().compareTo(prep2.toString());
			}
		});
		for (LogPreprocessorAlgorithm preprocessor : sortedPreprocessors) {
			preprocessors.addElement(preprocessor);
			if (parameters.getPreprocessor().equals(preprocessor)) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<LogPreprocessorAlgorithm> preprocessorList = new ProMList<LogPreprocessorAlgorithm>("Select preprocessor", preprocessors);
		preprocessorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		preprocessorList.setSelectedIndices(selectedIndices);
		preprocessorList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<LogPreprocessorAlgorithm> selectedPreprocessors = preprocessorList.getSelectedValuesList();
				if (selectedPreprocessors.size() == 1) {
					parameters.setPreprocessor(selectedPreprocessors.iterator().next());
				}
			}
		});
		preprocessorList.setPreferredSize(new Dimension(100, 100));
		add(preprocessorList, "2, 0");

	}
}
