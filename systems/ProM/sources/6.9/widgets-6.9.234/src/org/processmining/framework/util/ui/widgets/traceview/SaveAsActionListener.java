package org.processmining.framework.util.ui.widgets.traceview;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;

public class SaveAsActionListener<T> implements ActionListener {

	static final Preferences PREFS = Preferences.userRoot().node("org.processmining.widgets.traceview");
	static final String LAST_USED_FOLDER = "lastUsedFolder";

	private final ProMTraceList<T> listView;
	private final Comparator<T> sortOrder;

	public SaveAsActionListener(ProMTraceList<T> listView, Comparator<T> sortOrder) {
		this.listView = listView;
		this.sortOrder = sortOrder;
	}

	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = new JFileChooser(PREFS.get(LAST_USED_FOLDER, new File(".").getAbsolutePath()));
		FileNameExtensionFilter pdfFilter = new FileNameExtensionFilter("PDF", "pdf");
		FileNameExtensionFilter emfFilter = new FileNameExtensionFilter("EMF", "emf");
		FileNameExtensionFilter epsFilter = new FileNameExtensionFilter("EPS", "eps");
		FileNameExtensionFilter svgFilter = new FileNameExtensionFilter("SVG", "svg");
		chooser.addChoosableFileFilter(pdfFilter);
		chooser.addChoosableFileFilter(emfFilter);
		chooser.addChoosableFileFilter(epsFilter);
		chooser.addChoosableFileFilter(svgFilter);
		chooser.setFileFilter(pdfFilter);
		chooser.setAcceptAllFileFilterUsed(false);
		int returnVal = chooser.showSaveDialog(listView);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			PREFS.put(LAST_USED_FOLDER, file.getParent());

			List<T> traces = new ArrayList<>();
			List<T> selection = listView.getList().getSelectedValuesList();
			for (T o : selection) {
				traces.add(o);
			}

			if (selection.size() > 100) {
				int result = JOptionPane.showConfirmDialog(listView,
						String.format(
								"You selected %s traces to be saved, this could take a long time and freeze the UI. Do you want to continue?",
								selection.size()));
				if (result != JOptionPane.OK_OPTION) {
					return;
				}
			}
			
			String font = JOptionPane.showInputDialog(listView, "If you want to use a specific font, please specify its name (Experimental Feature)", Font.SANS_SERIF);
	
			ProMTraceList<T> listForPrinting = new ProMTraceList<>(traces, listView.getTraceBuilder(), sortOrder, new Font(font, Font.PLAIN, 10), listView.hasLabels());

			listForPrinting.setOpaque(true);
			listForPrinting.setBackground(Color.WHITE);
			listForPrinting.setForeground(Color.BLACK);

			if (chooser.getFileFilter() == pdfFilter) {
				if (!file.getAbsolutePath().endsWith(".pdf")) {
					file = new File(file.getAbsolutePath() + ".pdf");
				}
				try {
					ProMTraceList.saveAsPDF(listForPrinting, "", file);
				} catch (IOException e1) {
					ProMUIHelper.showErrorMessage(listView, e1.getMessage(), "Error saving");
				}
			} else if (chooser.getFileFilter() == emfFilter) {
				if (!file.getAbsolutePath().endsWith(".emf")) {
					file = new File(file.getAbsolutePath() + ".emf");
				}
				try {
					ProMTraceList.saveAsEMF(listForPrinting, file);
				} catch (IOException e1) {
					ProMUIHelper.showErrorMessage(listView, e1.getMessage(), "Error saving");
				}
			} else if (chooser.getFileFilter() == epsFilter) {
				if (!file.getAbsolutePath().endsWith(".eps")) {
					file = new File(file.getAbsolutePath() + ".eps");
				}
				try {
					ProMTraceList.saveAsEPS(listForPrinting, file);
				} catch (IOException e1) {
					ProMUIHelper.showErrorMessage(listView, e1.getMessage(), "Error saving");
				}
			} else if (chooser.getFileFilter() == svgFilter) {
				if (!file.getAbsolutePath().endsWith(".svg")) {
					file = new File(file.getAbsolutePath() + ".svg");
				}
				try {
					ProMTraceList.saveAsSVG(listForPrinting, file);
				} catch (IOException e1) {
					ProMUIHelper.showErrorMessage(listView, e1.getMessage(), "Error saving");
				}
			}
		}

	}

}
