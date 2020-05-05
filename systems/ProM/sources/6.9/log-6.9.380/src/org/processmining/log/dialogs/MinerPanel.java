package org.processmining.log.dialogs;

import java.awt.Dimension;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.parameters.MinerParameter;
import org.processmining.log.parameters.UpdateParameter;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class MinerPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7656913719419272750L;

	public MinerPanel(List<String> miners, final MinerParameter minerParameter) {
		this(miners, minerParameter, null);
	}
	
	public MinerPanel(List<String> miners, final MinerParameter minerParameter, final UpdateParameter updateParameter) {
		double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
		setLayout(new TableLayout(size));

		setOpaque(false);
		
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		for (String miner: miners) {
			listModel.addElement(miner);
		}
		final ProMList<String> list = new ProMList<String>("Select miner", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String defaultMiner = minerParameter.getMiner();
		list.setSelection(defaultMiner);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					minerParameter.setMiner(selected.get(0));
					if (updateParameter != null) {
						updateParameter.update();
					}
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultMiner);
					minerParameter.setMiner(defaultMiner);
					if (updateParameter != null) {
						updateParameter.update();
					}
				}
			}
		});
		list.setPreferredSize(new Dimension(100, 100));
		add(list, "0, 0");

	}

}
