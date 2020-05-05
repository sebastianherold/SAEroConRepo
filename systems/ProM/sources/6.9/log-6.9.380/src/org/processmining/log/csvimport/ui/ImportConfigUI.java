package org.processmining.log.csvimport.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.ICSVReader;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csv.config.CSVQuoteCharacter;
import org.processmining.log.csv.config.CSVSeperator;
import org.processmining.log.csvimport.ui.preview.CSVPreviewPanel;

/**
 * UI for the import configuration (charset, separator, ..)
 * 
 * @author F. Mannhardt
 *
 */
public final class ImportConfigUI extends CSVConfigurationPanel {

	private static final long serialVersionUID = 2L;

	private static final int MAX_PREVIEW = 1000;
	private static final int COLUMN_WIDTH = 240;

	private final CSVFile csv;
	private final CSVConfig importConfig;

	private final ProMComboBox<String> charsetCbx;
	private final ProMComboBox<CSVSeperator> separatorField;
	private final ProMComboBox<CSVQuoteCharacter> quoteField;

	private final CSVPreviewPanel previewPanel;

	private SwingWorker<Void, String[]> worker;

	public ImportConfigUI(final CSVFile csv, final CSVConfig importConfig) {
		super();
		this.importConfig = importConfig;
		this.csv = csv;
		this.previewPanel = new CSVPreviewPanel();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);		

		JLabel header = new JLabel("<HTML><H2>CSV Parser: Settings</H2></HTML>");
		header.setAlignmentX(CENTER_ALIGNMENT);
		
		add(header);

		add(Box.createVerticalStrut(10));

		JPanel topPanel = new JPanel();
		
		GroupLayout layout = new GroupLayout(topPanel);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		topPanel.setOpaque(false);
		topPanel.setLayout(layout);
		
		JPanel charsetPanel = new JPanel();
		charsetPanel.setOpaque(false);
		charsetPanel.setLayout(new BoxLayout(charsetPanel, BoxLayout.Y_AXIS));
		charsetCbx = new ProMComboBox<>(Charset.availableCharsets().keySet());
		charsetCbx.setSelectedItem(importConfig.getCharset());
		charsetCbx.setPreferredSize(null);
		charsetCbx.setMinimumSize(null);
		JLabel charsetLabel = createLabel("Charset", 
				"Configure the character encoding that is used by the CSV file");
		charsetLabel.setAlignmentX(LEFT_ALIGNMENT);
		charsetCbx.setAlignmentX(LEFT_ALIGNMENT);
		charsetPanel.add(charsetLabel);
		charsetPanel.add(charsetCbx);
		charsetCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				importConfig.setCharset(charsetCbx.getSelectedItem().toString());
				refreshPreview();
			}
		});

		JPanel separatorPanel = new JPanel();
		separatorPanel.setOpaque(false);
		separatorPanel.setLayout(new BoxLayout(separatorPanel, BoxLayout.Y_AXIS));
		separatorField = new ProMComboBox<>(CSVSeperator.values());
		separatorField.setPreferredSize(null);
		separatorField.setMinimumSize(null);
		separatorField.setSelectedItem(importConfig.getSeparator());
		JLabel seperationLabel = createLabel("Separator Character", 
				"Configure the character that is used by the CSV file to separate two fields");
		seperationLabel.setAlignmentX(LEFT_ALIGNMENT);
		separatorField.setAlignmentX(LEFT_ALIGNMENT);
		separatorPanel.add(seperationLabel);
		separatorPanel.add(separatorField);
		separatorField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				importConfig.setSeparator(((CSVSeperator) separatorField.getSelectedItem()));
				refreshPreview();
			}
		});

		JPanel quotePanel = new JPanel();
		quotePanel.setOpaque(false);
		quotePanel.setLayout(new BoxLayout(quotePanel, BoxLayout.Y_AXIS));
		quoteField = new ProMComboBox<>(CSVQuoteCharacter.values());
		quoteField.setPreferredSize(null);
		quoteField.setMinimumSize(null);
		JLabel quoteLabel = createLabel("Quote Character", 
				"Configure the character that is used by the CSV file that is used to quote values if they contain the separator character or a newline");
		quoteLabel.setAlignmentX(LEFT_ALIGNMENT);
		quoteField.setAlignmentX(LEFT_ALIGNMENT);
		quotePanel.add(quoteLabel);
		quotePanel.add(quoteField);
		quoteField.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				importConfig.setQuoteChar((CSVQuoteCharacter) quoteField.getSelectedItem());
				refreshPreview();
			}
		});
		quoteField.setSelectedItem(importConfig.getQuoteChar());

		ParallelGroup verticalGroup = layout.createParallelGroup()
				.addComponent(charsetPanel, Alignment.TRAILING)
				.addComponent(separatorPanel, Alignment.TRAILING)
				.addComponent(quotePanel, Alignment.TRAILING);

		SequentialGroup horizontalGroup = layout.createSequentialGroup()
				.addComponent(charsetPanel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
				.addComponent(separatorPanel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH )
				.addComponent(quotePanel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH);

		layout.linkSize(SwingConstants.HORIZONTAL, separatorPanel, charsetPanel, quotePanel);
		
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
		
		add(topPanel);
		previewPanel.setMaximumSize(new Dimension(725, 350));
		add(previewPanel);
	}

	private void refreshPreview() {

		if (worker != null) {
			worker.cancel(true);
		}

		previewPanel.clear();

		// Update Header
		try {
			previewPanel.setHeader(csv.readHeader(importConfig));
		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			ProMUIHelper.showWarningMessage(this, "Error parsing CSV " + e.getMessage(), "CSV Parsing Error");
			return;
		}

		worker = new SwingWorker<Void, String[]>() {

			protected Void doInBackground() throws Exception {

				try (ICSVReader reader = csv.createReader(importConfig)) {
					// Skip header
					reader.readNext();
					String[] nextLine;
					int i = 0;
					while ((nextLine = reader.readNext()) != null && i < MAX_PREVIEW) {
						publish(nextLine);
						i++;
					}
				}

				return null;
			}

			protected void process(List<String[]> chunks) {
				for (String[] row : chunks) {
					previewPanel.addRow(row);
				}
			}

		};

		try {
			worker.execute();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error parsing CSV " + e.getMessage(), "CSV Parsing Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public CSVConfig getImportConfig() {
		return importConfig;
	}

}