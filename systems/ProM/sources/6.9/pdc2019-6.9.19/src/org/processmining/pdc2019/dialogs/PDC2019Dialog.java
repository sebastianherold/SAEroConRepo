package org.processmining.pdc2019.dialogs;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.pdc2019.algorithms.PDC2019Set;
import org.processmining.pdc2019.parameters.PDC2019Parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class PDC2019Dialog extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7895709177834365821L;
	
	public PDC2019Dialog(final PDC2019Parameters parameters) {
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<PDC2019Set> setListModel = new DefaultListModel<PDC2019Set>();
		int[] selectedIndices = new int[1];
		int i = 0;
		int j = 0;
		List<PDC2019Set> sortedSets = new ArrayList<PDC2019Set>();
		sortedSets.addAll(Arrays.asList(PDC2019Set.values()));
		Collections.sort(sortedSets, new Comparator<PDC2019Set>() {
			public int compare(PDC2019Set set1, PDC2019Set set2) {
				return set1.toString().compareTo(set2.toString());
			}
		});
		for (PDC2019Set set : sortedSets) {
			setListModel.addElement(set);
			if (parameters.getSet() == set) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<PDC2019Set> setList = new ProMList<PDC2019Set>("Select log collection", setListModel);
		setList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setList.setSelectedIndices(selectedIndices);
		setList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<PDC2019Set> selectedSet = setList.getSelectedValuesList();
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
