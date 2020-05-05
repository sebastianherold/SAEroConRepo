package org.processmining.pdc2017.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
import org.processmining.pdc2017.algorithms.PDC2017Set;
import org.processmining.pdc2017.parameters.PDC2017Parameters;

public class PDC2017Dialog extends JComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7895709177834365821L;
	
	public PDC2017Dialog(final PDC2017Parameters parameters) {
		
		double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
				{ TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));
		setOpaque(false);

		DefaultListModel<PDC2017Set> setListModel = new DefaultListModel<PDC2017Set>();
		int[] selectedIndices = new int[1];
		int i = 0;
		int j = 0;
		List<PDC2017Set> sortedSets = new ArrayList<PDC2017Set>();
		sortedSets.addAll(Arrays.asList(PDC2017Set.values()));
		Collections.sort(sortedSets, new Comparator<PDC2017Set>() {
			public int compare(PDC2017Set set1, PDC2017Set set2) {
				return set1.toString().compareTo(set2.toString());
			}
		});
		for (PDC2017Set set : sortedSets) {
			setListModel.addElement(set);
			if (parameters.getSet() == set) {
				selectedIndices[j++] = i;
			}
			i++;
		}
		final ProMList<PDC2017Set> setList = new ProMList<PDC2017Set>("Select log collection", setListModel);
		setList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setList.setSelectedIndices(selectedIndices);
		setList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<PDC2017Set> selectedSet = setList.getSelectedValuesList();
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
