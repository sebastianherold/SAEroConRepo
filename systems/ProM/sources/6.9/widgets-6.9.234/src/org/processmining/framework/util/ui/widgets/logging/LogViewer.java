package org.processmining.framework.util.ui.widgets.logging;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextArea;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * 
 * @author mwesterg
 * 
 */
@Plugin(name = "Show Log", returnLabels = { "Log Viewer" }, returnTypes = { JComponent.class }, parameterLabels = { "Loggable Object" }, userAccessible = false)
@Visualizer
public class LogViewer {
	private static class LogHandler extends Handler {
		private final transient LogView view;

		public LogHandler(final LogView view) {
			this.view = view;
		}

		@Override
		public void close() throws SecurityException {
		}

		@Override
		public void flush() {
		}

		@Override
		public void publish(final LogRecord record) {
			if (view != null) {
				view.add(record);
			}
		}
	}

	private static class LogView extends ProMPropertiesPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JCheckBox details;
		@SuppressWarnings("rawtypes")
		private final JComboBox/* <Level> */level;
		private final ProMTextArea log;

		public LogView(final Logger logger) {
			super("Log (" + logger.getName() + ")");

			level = addComboBox("Logging level", new Level[] { Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO,
					Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL });
			level.setSelectedItem(Level.ALL);
			level.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					logger.setLevel((Level) level.getSelectedItem());
				}
			});

			details = addCheckBox("Show details", true);

			final JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setOpaque(false);
			add(panel);

			log = new ProMTextArea();
			log.setEditable(false);
			log.setTabSize(4);
			panel.add(log);

			final JPanel buttons = new JPanel();
			buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			buttons.setOpaque(false);
			panel.add(buttons, BorderLayout.SOUTH);

			final JButton clearButton = SlickerFactory.instance().createButton("Clear");
			buttons.add(clearButton);

			clearButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					log.setText("");
				}
			});

			final JButton saveButton = SlickerFactory.instance().createButton("Save");
			buttons.add(saveButton);

			saveButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String txt = log.getText();
					final JFileChooser chooser = new JFileChooser();
					chooser.setFileFilter(new FileFilter() {
						@Override
						public boolean accept(final File f) {
							return f.getName().toLowerCase().endsWith(".txt");
						}

						@Override
						public String getDescription() {
							return "Text files";
						}
					});
					chooser.setSelectedFile(new File("Log for " + logger.getName() + ".txt"));
					if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
						FileWriter fileWriter;
						try {
							fileWriter = new FileWriter(chooser.getSelectedFile());
							final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
							bufferedWriter.append(txt);
							bufferedWriter.close();
						} catch (final IOException _) {
						}
					}
				}
			});

		}

		public void add(final LogRecord record) {
			if (((Level) level.getSelectedItem()).intValue() <= record.getLevel().intValue()) {
				final StringBuilder message = new StringBuilder();
				boolean indent = false;
				if (details.isSelected()) {
					indent = true;
					message.append(LogViewer.format.format(new Date(record.getMillis())));
					message.append('\t');
					message.append(record.getLevel());
					message.append('\t');
					boolean addedClass = false;
					if (record.getSourceClassName() != null && !"".equals(record.getSourceClassName())) {
						message.append(record.getSourceClassName());
						addedClass = true;
					}
					if (record.getSourceMethodName() != null && !"".equals(record.getSourceMethodName())) {
						if (addedClass) {
							message.append('.');
						}
						message.append(record.getSourceMethodName());
					}
					message.append("\n\t");
				}
				message.append(record.getMessage());
				Throwable t = record.getThrown();
				while (t != null) {
					message.append(' ');
					message.append(t.getClass());
					message.append(": ");
					message.append(t.getMessage());
					for (final StackTraceElement ste : record.getThrown().getStackTrace()) {
						message.append('\n');
						if (indent) {
							message.append('\t');
						}
						message.append('\t');
						message.append(ste);
					}
					if (t.getCause() != t && t.getCause() != null) {
						t = t.getCause();
						message.append('\n');
						if (indent) {
							message.append('\t');
						}
						message.append("Caused by");
					} else {
						t = null;
					}
				}
				message.append('\n');
				log.append(message.toString());
				log.scrollToEnd();
			}
		}
	};

	private static final DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	/**
	 * @param context
	 * @param service
	 * @return
	 */
	@PluginVariant(requiredParameterLabels = { 0 })
	public static JComponent visualize(final PluginContext context, final Loggable service) {
		final Logger logger = service.getLogger();
		logger.setLevel(Level.ALL);
		final LogView view = new LogView(logger);
		logger.addHandler(new LogHandler(view));
		return view;
	}

}
