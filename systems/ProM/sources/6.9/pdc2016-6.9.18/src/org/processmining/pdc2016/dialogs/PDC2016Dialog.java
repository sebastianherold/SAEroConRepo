package org.processmining.pdc2016.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.pdc2016.algorithms.PDC2016Set;
import org.processmining.pdc2016.parameters.PDC2016Parameters;

public class PDC2016Dialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6814149869848660395L;

	public PDC2016Dialog(final PDC2016Parameters parameters) {
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<PDC2016Set> setListModel = new DefaultListModel<PDC2016Set>();
		int[] selectedIndices = new int[1];
		int i = 0;
		int j = 0;
		List<PDC2016Set> sortedSets = new ArrayList<PDC2016Set>();
		sortedSets.addAll(Arrays.asList(PDC2016Set.values()));
		Collections.sort(sortedSets, new Comparator<PDC2016Set>() {
			public int compare(PDC2016Set set1, PDC2016Set set2) {
				return set1.toString().compareTo(set2.toString());
			}
		});
		for (PDC2016Set set : sortedSets) {
			setListModel.addElement(set);
			if (parameters.getSet() == set) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<PDC2016Set> setList = new ProMList<PDC2016Set>("Select log collection", setListModel);
		setList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setList.setSelectedIndices(selectedIndices);
		setList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<PDC2016Set> selectedSet = setList.getSelectedValuesList();
				parameters.setSet(selectedSet.iterator().next());
			}
		});
		setList.setPreferredSize(new Dimension(100, 100));
		add(setList, "0, 0");

		DefaultListModel<Integer> nrListModel = new DefaultListModel<Integer>();
		selectedIndices = new int[10];
		i = 0;
		j = 0;
		for (int nr = 1; nr < 11; nr++) {
			nrListModel.addElement(nr);
			if (parameters.getNr() == nr) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<Integer> nrList = new ProMList<Integer>("Select log number", nrListModel);
		nrList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nrList.setSelectedIndices(selectedIndices);
		nrList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<Integer> selectedNr = nrList.getSelectedValuesList();
				parameters.setNr(selectedNr.iterator().next());
			}
		});
		nrList.setPreferredSize(new Dimension(100, 100));
		add(nrList, "1, 0");
	}
}
