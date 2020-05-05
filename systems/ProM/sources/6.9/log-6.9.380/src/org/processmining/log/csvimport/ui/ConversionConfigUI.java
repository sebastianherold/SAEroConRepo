package org.processmining.log.csvimport.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMListSortableWithComboBox;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.ICSVReader;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVMapping;
import org.processmining.log.csvimport.ui.preview.CSVPreviewFrame;

import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.collect.Lists;

/**
 * UI for the configuration of the actual conversion
 * 
 * @author F. Mannhardt
 *
 */
public final class ConversionConfigUI extends CSVConfigurationPanel implements AutoCloseable {

	private static final int COLUMN_WIDTH = 360;

	private final class ChangeListenerImpl implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			updateSettings();
		}

		public void updateSettings() {
			try {
				conversionConfig.setCaseColumns(caseComboBox.getElements());
				conversionConfig.setEventNameColumns(eventComboBox.getElements());
				conversionConfig.setStartTimeColumn(startTimeColumnCbx.getSelectedItem().toString());
				conversionConfig.setCompletionTimeColumn(completionTimeColumnCbx.getSelectedItem().toString());

				if (conversionConfig.getStartTimeColumn().isEmpty()) {
					startTimeFormat.setText("");
					startTimeFormat.setEnabled(false);
				} else {
					startTimeFormat.setEnabled(true);
					if (hasManipulatedStartTime) {
						CSVMapping mapping = conversionConfig.getConversionMap()
								.get(conversionConfig.getStartTimeColumn());
						mapping.setPattern(startTimeFormat.getText());
					} else {
						// use originally guess format 
						startTimeFormat.setText(conversionConfig.getConversionMap()
								.get(conversionConfig.getStartTimeColumn()).getPattern());
					}
				}

				if (conversionConfig.getCompletionTimeColumn().isEmpty()) {
					completionTimeFormat.setText("");
					completionTimeFormat.setEnabled(false);
				} else {
					completionTimeFormat.setEnabled(true);
					if (hasManipulatedCompletionTime) {
						CSVMapping mapping = conversionConfig.getConversionMap()
								.get(conversionConfig.getCompletionTimeColumn());
						mapping.setPattern(completionTimeFormat.getText());
					} else {
						// use originally guess format 
						completionTimeFormat.setText(conversionConfig.getConversionMap()
								.get(conversionConfig.getCompletionTimeColumn()).getPattern());
					}
				}
			} catch (RuntimeException e) {
				ProMUIHelper.showErrorMessage(previewFrame, e.getMessage() != null ? e.getMessage() : e.toString(), "Error updating configuration", e);
			}

			previewFrame.refresh();
		}

	}

	private final class LoadCSVRecordsWorker extends SwingWorker<Void, String[]> {
		protected Void doInBackground() throws Exception {
			String[] oldLine = null;
			String[] nextLine;
			int i = 0;
			while ((nextLine = reader.readNext()) != null && i < maxLoad) {
				if (oldLine != null) {
					if (oldLine.length != nextLine.length) {
						throw new IllegalArgumentException(
								"CSV file has inconsistent number of columns, please check CSV config.");
					}
				}
				publish(nextLine);
				oldLine = nextLine;
				i++;
			}
			return null;
		}

		protected void process(List<String[]> chunks) {
			previewFrame.addRows(chunks);
			previewFrame.setTitle(String.format("CSV Preview (%s rows - scroll down to load more)",
					previewFrame.getPreviewTable().getModel().getRowCount()));
		}
	}

	private static final long serialVersionUID = 2L;

	private static final int ACTION_DELAY = 500;

	private final CSVConversionConfig conversionConfig;

	private final String[] headers;
	private final String[] headersInclEmpty;

	private final ProMListSortableWithComboBox<String> caseComboBox;
	private final ProMListSortableWithComboBox<String> eventComboBox;
	private final ProMComboBox<String> completionTimeColumnCbx;
	private final ProMComboBox<String> startTimeColumnCbx;

	private final ChangeListenerImpl changeListener;
	private final Timer updateTimer;

	private final ICSVReader reader;
	private final CSVPreviewFrame previewFrame;
	private int maxLoad = 1000;

	private ProMTextField completionTimeFormat;
	private ProMTextField startTimeFormat;

	private boolean hasManipulatedStartTime = false;
	private boolean hasManipulatedCompletionTime = false;

	public ConversionConfigUI(final CSVFile csv, final CSVConfig importConfig, CSVConversionConfig conversionConfig)
			throws IOException {
		this.conversionConfig = conversionConfig;

		reader = csv.createReader(importConfig);
		headers = reader.readNext();
		headersInclEmpty = Lists.asList("", headers).toArray(new String[headers.length + 1]);
		changeListener = new ChangeListenerImpl();
		updateTimer = new Timer(ACTION_DELAY, new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeListener.updateSettings();
			}
		});
		updateTimer.setRepeats(false);

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setMaximumSize(new Dimension(COLUMN_WIDTH * 2, Short.MAX_VALUE));
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		JLabel standardAttributesLabel = SlickerFactory.instance()
				.createLabel("<HTML><H2>Mapping to Standard XES Attributes</H2></HTML>");
		JButton showPreviewButton = new SlickerButton("Show Expert Configuration");
		showPreviewButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				togglePreviewFrame();
			}
		});

		caseComboBox = new ProMListSortableWithComboBox<>(new DefaultComboBoxModel<>(headers));
		JLabel caseLabel = createLabel("Case Column (Optional)",
				"Groups events into traces, and is mapped to 'concept:name' of the trace. Select one or more columns, re-order by drag & drop.");
		for (String caseColumn : conversionConfig.getCaseColumns()) {
			caseComboBox.addElement(caseColumn);
		}
		caseComboBox.getSelectedItemsText().setText("Selected case columns:");
		caseComboBox.getListModel().addListDataListener(new ListDataListener() {

			public void intervalRemoved(ListDataEvent e) {
				changeListener.updateSettings();
			}

			public void intervalAdded(ListDataEvent e) {
				changeListener.updateSettings();
			}

			public void contentsChanged(ListDataEvent e) {
				changeListener.updateSettings();
			}
		});

		eventComboBox = new ProMListSortableWithComboBox<>(new DefaultComboBoxModel<>(headers));
		JLabel eventLabel = createLabel("Event Column (Optional)",
				"Mapped to 'concept:name' of the event. Select one or more columns, re-order by drag & drop.");
		for (String eventColumn : conversionConfig.getEventNameColumns()) {
			eventComboBox.addElement(eventColumn);
		}
		eventComboBox.getSelectedItemsText().setText("Selected event columns:");
		eventComboBox.getListModel().addListDataListener(new ListDataListener() {

			public void intervalRemoved(ListDataEvent e) {
				changeListener.updateSettings();
			}

			public void intervalAdded(ListDataEvent e) {
				changeListener.updateSettings();
			}

			public void contentsChanged(ListDataEvent e) {
				changeListener.updateSettings();
			}
		});

		completionTimeColumnCbx = new ProMComboBox<>(headersInclEmpty);
		completionTimeColumnCbx.setToolTipText(
				"Mapped to 'time:timestamp' of the main event that is created for each row in the CSV file.");
		JLabel completionTimeLabel = createLabel("Completion Time (Optional)", "Mapped to 'time:timestamp'");
		if (conversionConfig.getCompletionTimeColumn() != null) {
			completionTimeColumnCbx.setSelectedItem(conversionConfig.getCompletionTimeColumn());
		} else {
			completionTimeColumnCbx.setSelectedItem("");
		}
		completionTimeColumnCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				hasManipulatedCompletionTime = false;
				changeListener.updateSettings();
			}
		});

		completionTimeFormat = new ProMTextField("",
				"Could not auto-detect the used date format. Please provide a SimpleDateFormat pattern!");
		if (conversionConfig.getCompletionTimeColumn() != null
				&& !conversionConfig.getCompletionTimeColumn().isEmpty()) {
			completionTimeFormat.setText(
					conversionConfig.getConversionMap().get(conversionConfig.getCompletionTimeColumn()).getPattern());
		}
		completionTimeFormat.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				hasManipulatedCompletionTime = true;
				update();
			}

			private void update() {
				try {
					new SimpleDateFormat(completionTimeFormat.getText());
					updateTimer.restart();
					completionTimeFormat.getTextField().setForeground(Color.WHITE);
				} catch (IllegalArgumentException e) {
					completionTimeFormat.getTextField().setForeground(Color.RED);
				}
			}

		});

		startTimeColumnCbx = new ProMComboBox<>(headersInclEmpty);
		startTimeColumnCbx.setToolTipText(
				"<HTML>Mapped to 'time:timestamp' of an extra 'start' event that is created for each row in the CSV file. "
						+ "<BR/>In case your lifecycle events such as 'start' are already separate row in the CSV file, please leave this empty and use the 'Expert Mode' to configure an appropriate mapping.</HTML>");
		JLabel startTimeLabel = createLabel("Start Time (Optional)",
				"Mapped to 'time:timestamp' of a separate start event");
		if (conversionConfig.getStartTimeColumn() != null) {
			startTimeColumnCbx.setSelectedItem(conversionConfig.getStartTimeColumn());
		} else {
			startTimeColumnCbx.setSelectedItem("");
		}
		startTimeColumnCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				hasManipulatedStartTime = false;
				changeListener.updateSettings();
			}
		});

		startTimeFormat = new ProMTextField("",
				"Could not auto-detect the used date format. Please provide a SimpleDateFormat pattern!");
		if (conversionConfig.getStartTimeColumn() != null && !conversionConfig.getStartTimeColumn().isEmpty()) {
			startTimeFormat.setText(
					conversionConfig.getConversionMap().get(conversionConfig.getStartTimeColumn()).getPattern());
		}

		startTimeFormat.addKeyListener(new KeyAdapter() {

			public void keyPressed(KeyEvent e) {
				hasManipulatedStartTime = true;
				update();
			}

			private void update() {
				try {
					new SimpleDateFormat(startTimeFormat.getText());
					updateTimer.restart();
					startTimeFormat.getTextField().setForeground(Color.WHITE);
				} catch (IllegalArgumentException e) {
					startTimeFormat.getTextField().setForeground(Color.RED);
				}
			}

		});

		SequentialGroup verticalGroup = layout.createSequentialGroup();
		verticalGroup.addGroup(layout.createParallelGroup(Alignment.CENTER).addComponent(standardAttributesLabel)
				.addComponent(showPreviewButton));
		verticalGroup.addGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(caseLabel).addComponent(caseComboBox))
				.addGroup(layout.createSequentialGroup().addComponent(eventLabel).addComponent(eventComboBox)));
		verticalGroup.addGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(startTimeLabel).addComponent(startTimeColumnCbx)
						.addComponent(startTimeFormat))
				.addGroup(layout.createSequentialGroup().addComponent(completionTimeLabel)
						.addComponent(completionTimeColumnCbx).addComponent(completionTimeFormat)));

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		horizontalGroup.addGroup(
				layout.createSequentialGroup().addComponent(standardAttributesLabel).addComponent(showPreviewButton));
		horizontalGroup.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(caseLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
						.addComponent(caseComboBox, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH))
				.addGroup(
						layout.createParallelGroup().addComponent(eventLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
								.addComponent(eventComboBox, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)));
		horizontalGroup.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(startTimeLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
						.addComponent(startTimeColumnCbx, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
						.addComponent(startTimeFormat, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH))
				.addGroup(layout.createParallelGroup()
						.addComponent(completionTimeLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
						.addComponent(completionTimeColumnCbx, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
						.addComponent(completionTimeFormat, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)));

		layout.linkSize(eventLabel, caseLabel);
		layout.linkSize(completionTimeLabel, startTimeLabel);

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);

		previewFrame = new CSVPreviewFrame(headers, conversionConfig);
		previewFrame.setTitle("Expert Configuration & Preview - Scroll down to load more rows");
		previewFrame.getMainScrollPane().getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			public void adjustmentValueChanged(AdjustmentEvent e) {
				int maximum = e.getAdjustable().getMaximum();
				int current = e.getValue();
				if (Math.abs(maximum - current) < 1000 && !e.getValueIsAdjusting()) {
					new LoadCSVRecordsWorker().execute();
				}
			}
		});

		changeListener.updateSettings();
	}

	private void togglePreviewFrame() {
		if (!previewFrame.isVisible()) {
			previewFrame.showFrame(this);
			try {
				// Update Content
				new LoadCSVRecordsWorker().execute();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error parsing CSV " + e.getMessage(), "CSV Parsing Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} else {
			previewFrame.setVisible(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#addNotify()
	 */
	@Override
	public void addNotify() {
		super.addNotify();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#removeNotify()
	 */
	@Override
	public void removeNotify() {
		super.removeNotify();
		changeListener.updateSettings();
		previewFrame.save();
		previewFrame.setVisible(false);
	}

	public CSVConversionConfig getConversionConfig() {
		return conversionConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.AutoCloseable#close()
	 */
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.toString());
		}
	}

}
