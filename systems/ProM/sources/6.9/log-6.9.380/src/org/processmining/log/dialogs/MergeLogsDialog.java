package org.processmining.log.dialogs;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.log.parameters.MergeLogsParameters;
import org.processmining.log.utils.MergeLogsUtils;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class MergeLogsDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5486690145975904628L;

	public MergeLogsDialog(final MergeLogsParameters parameters, final XLog log) {
		double size[][] = { { 100, 700 }, { 30, TableLayoutConstants.FILL, 30, 30, 30, 30, 30, 30, 30 } };
		setLayout(new TableLayout(size));
		int row = 0;
		DateFormat df;
		final Collection<String> ids = new HashSet<String>();
		for (XTrace trace : log) {
			String id = XConceptExtension.instance().extractName(trace);
			if (id != null) {
				ids.add(id);
			}
		}
		
		final ProMTextField idField = new ProMTextField(parameters.getTraceId());
		idField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				String value = idField.getText().trim();
				if (value.isEmpty() || !ids.contains(value)) {
					value = null;
				} 
				parameters.setTraceId(value);
				idField.visualizeStatus(idField.getText().trim().isEmpty() || value != null);
			}
		});
		add(new JLabel("Choose Trace ID"), "0, " + row);
		add(idField, "1, " + row);
		row++;

		DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.addElement(parameters.getDateFormat());
		listModel.addElement("MM/dd/yyyy HH:mm:ss");
		listModel.addElement("MM/dd/yyyy hh:mm");
		listModel.addElement("MM/dd/yyyy hh:mm:ss");
		listModel.addElement("MM/dd/yyyy");
		listModel.addElement("dd/MM/yyyy HH:mm");
		listModel.addElement("dd/MM/yyyy HH:mm:ss");
		listModel.addElement("dd/MM/yyyy hh:mm");
		listModel.addElement("dd/MM/yyyy hh:mm:ss");
		listModel.addElement("dd/MM/yyyy");
		listModel.addElement("dd-MMM-yyyy HH:mm");
		listModel.addElement("dd-MMM-yyyy HH:mm:ss");
		listModel.addElement("dd-MMM-yyyy hh:mm");
		listModel.addElement("dd-MMM-yyyy hh:mm:ss");
		listModel.addElement("dd-MMM-yyyy");
		listModel.addElement("MM/dd/yy HH:mm");
		listModel.addElement("MM/dd/yy HH:mm:ss");
		listModel.addElement("MM/dd/yy hh:mm");
		listModel.addElement("MM/dd/yy hh:mm:ss");
		listModel.addElement("MM/dd/yy");
		listModel.addElement("dd/MM/yy HH:mm");
		listModel.addElement("dd/MM/yy HH:mm:ss");
		listModel.addElement("dd/MM/yy hh:mm");
		listModel.addElement("dd/MM/yy hh:mm:ss");
		listModel.addElement("dd/MM/yy");
		listModel.addElement("dd-MMM-yy HH:mm");
		listModel.addElement("dd-MMM-yy HH:mm:ss");
		listModel.addElement("dd-MMM-yy hh:mm");
		listModel.addElement("dd-MMM-yy hh:mm:ss");
		listModel.addElement("dd-MMM-yy");
		final ProMList<String> list = new ProMList<String>("Select date format", listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		final String defaultConfiguration = parameters.getDateFormat();
		list.setSelection(defaultConfiguration);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				List<String> selected = list.getSelectedValuesList();
				if (selected.size() == 1) {
					parameters.setDateFormat(selected.get(0));
				} else {
					/*
					 * Nothing selected. Revert to selection of default classifier.
					 */
					list.setSelection(defaultConfiguration);
					parameters.setDateFormat(defaultConfiguration);
				}
			}
		});
		list.setPreferredSize(new Dimension(10, 10));
		add(list, "0, " + row + ", 1, " + row);
		row++;

		String fromDate = "";
		df = new SimpleDateFormat(parameters.getDateFormat());
		if (parameters.getFromDate() != null) {
			fromDate = df.format(parameters.getFromDate());
		}
		final ProMTextField fromField = new ProMTextField(fromDate);
		fromField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				Date date = MergeLogsUtils.getDate(parameters, fromField.getText().trim());
				parameters.setFromDate(date);
				fromField.visualizeStatus(fromField.getText().trim().isEmpty() || date != null); 
			}
			
		});
		add(new JLabel("From Date"), "0, " + row);
		add(fromField, "1, " + row);
		row++;

		String toDate = "";
		df = new SimpleDateFormat(parameters.getDateFormat());
		if (parameters.getToDate() != null) {
			toDate = df.format(parameters.getToDate());
		}
		final ProMTextField toField = new ProMTextField(toDate);
		toField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				Date date = MergeLogsUtils.getDate(parameters, toField.getText().trim());
				parameters.setToDate(date);
				toField.visualizeStatus(toField.getText().trim().isEmpty() || date != null); 
			}
			
		});
		add(new JLabel("To Date"), "0, " + row);
		add(toField, "1, " + row);
		row++;

		String specificDate = "";
		df = new SimpleDateFormat(parameters.getDateFormat());
		if (parameters.getSpecificDate() != null) {
			specificDate = df.format(parameters.getSpecificDate());
		}
		final ProMTextField specificField = new ProMTextField(specificDate);
		specificField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				Date date = MergeLogsUtils.getDate(parameters, specificField.getText().trim());
				parameters.setSpecificDate(date);
				specificField.visualizeStatus(specificField.getText().trim().isEmpty() || date != null); 
			}
			
		});
		add(new JLabel("Specific Date"), "0, " + row);
		add(specificField, "1, " + row);
		row++;

		final ProMTextField requiredField = new ProMTextField(parameters.getRequiredWords());
		requiredField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				String value = requiredField.getText().trim();
				if (value.isEmpty()) {
					value = null;
				}
				parameters.setRequiredWords(requiredField.getText());
			}
			
		});
		add(new JLabel("Required Words"), "0, " + row);
		add(requiredField, "1, " + row);
		row++;

		final ProMTextField forbiddenField = new ProMTextField(parameters.getForbiddenWords());
		forbiddenField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				String value = forbiddenField.getText().trim();
				if (value.isEmpty()) {
					value = null;
				}
				parameters.setForbiddenWords(value);
			}
			
		});
		add(new JLabel("Forbidden Words"), "0, " + row);
		add(forbiddenField, "1, " + row);
		row++;


		final NiceSlider clusterSlider = SlickerFactory.instance().createNiceIntegerSlider("Set related", 0, 100,
				parameters.getRelated(), Orientation.HORIZONTAL);
		clusterSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setRelated(clusterSlider.getSlider().getValue());
			}
		});
		add(clusterSlider, "0, "+ row + ", 1, " + row);
		row++;


		final NiceSlider wordSlider = SlickerFactory.instance().createNiceIntegerSlider("Set matching words", 0, 100,
				parameters.getMinMatches(), Orientation.HORIZONTAL);
		clusterSlider.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				parameters.setMinMatches(wordSlider.getSlider().getValue());
			}
		});
		add(wordSlider, "0, "+ row + ", 1, " + row);
		row++;

		
	}
	
}
