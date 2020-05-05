package org.processmining.log.csvimport.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JLabel;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.log.csv.CSVFile;
import org.processmining.log.csv.config.CSVConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVAttributeConversionMode;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVEmptyCellHandlingMode;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

public class ExpertConfigUI extends CSVConfigurationPanel {

	private static final long serialVersionUID = 7749368962812585099L;

	private static final int COLUMN_WIDTH = 360;

	private static final class XFactoryUI {

		private final XFactory factory;

		public XFactoryUI(XFactory factory) {
			super();
			this.factory = factory;
		}

		public XFactory getFactory() {
			return factory;
		}

		@Override
		public String toString() {
			return factory.getName();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((factory == null) ? 0 : factory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof XFactoryUI))
				return false;
			XFactoryUI other = (XFactoryUI) obj;
			if (factory == null) {
				if (other.factory != null)
					return false;
			} else if (!factory.equals(other.factory))
				return false;
			return true;
		}

	}

	private final ProMComboBox<XFactoryUI> xFactoryChoice;
	private final ProMComboBox<CSVEmptyCellHandlingMode> emptyCellHandlingModeCbx;
	private final ProMComboBox<CSVErrorHandlingMode> errorHandlingModeCbx;
	private final ProMComboBox<CSVAttributeConversionMode> attributeConversionModeCbx;

	public ExpertConfigUI(final CSVFile csv, final CSVConfig importConfig, final CSVConversionConfig conversionConfig) {
		super();
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		setMaximumSize(new Dimension(COLUMN_WIDTH * 2, Short.MAX_VALUE));

		JLabel conversionOptionsLabel = SlickerFactory.instance().createLabel(
				"Additional Conversion Options (Defaults are a good guess)");
		conversionOptionsLabel.setFont(conversionOptionsLabel.getFont().deriveFont(Font.BOLD, 20));

		xFactoryChoice = new ProMComboBox<>(Iterables.transform(getAvailableXFactories(),
				new Function<XFactory, XFactoryUI>() {

					public XFactoryUI apply(XFactory factory) {
						return new XFactoryUI(factory);
					}

				}));
		xFactoryChoice.setSelectedItem(new XFactoryUI(conversionConfig.getFactory()));
		JLabel xFactoryLabel = createLabel(
				"XFactory",
				"XFactory implementation that is used to create the log. Some implementations might be more memory efficient, consider changing this in case you import a huge log.");

		xFactoryChoice.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				conversionConfig.setFactory(((XFactoryUI) xFactoryChoice.getSelectedItem()).getFactory());
			}
		});

		errorHandlingModeCbx = new ProMComboBox<>(CSVErrorHandlingMode.values());
		errorHandlingModeCbx.setSelectedItem(conversionConfig.getErrorHandlingMode());
		JLabel errorHandlingModeLabel = createLabel("Error Handling",
				"Stop conversion upon malformed input or try to import as much as possible?");
		errorHandlingModeCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				conversionConfig.setErrorHandlingMode((CSVErrorHandlingMode) errorHandlingModeCbx.getSelectedItem());
			}
		});

		emptyCellHandlingModeCbx = new ProMComboBox<>(CSVEmptyCellHandlingMode.values());
		emptyCellHandlingModeCbx.setSelectedItem(conversionConfig.getEmptyCellHandlingMode());
		JLabel emptyCellHandlingModeLabel = createLabel(
				"Sparse / Dense Log",
				"Exclude (sparse) or include (dense) empty cells in the conversion. This affects how empty cells in the CSV are handled. "
						+ "Some plug-ins require the log to be dense, i.e., all attributes are defined for each event. "
						+ "In other cases it might be more efficient or even required to only add attributes to events if the attributes actually contain data.");
		emptyCellHandlingModeCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				conversionConfig.setEmptyCellHandlingMode((CSVEmptyCellHandlingMode) emptyCellHandlingModeCbx
						.getSelectedItem());
			}
		});

		attributeConversionModeCbx = new ProMComboBox<>(CSVAttributeConversionMode.values());
		attributeConversionModeCbx
				.setSelectedItem(conversionConfig.isShouldAddStartEventAttributes() ? CSVAttributeConversionMode.ADD_TO_BOTH
						: CSVAttributeConversionMode.ADD_TO_COMPLETE);
		JLabel attributeConversionModeLabel = createLabel(
				"Attribute Conversion Mode",
				"Add attributes either to both start and complete events, or only to the complete event. "
				+ "This is only relevant if your log contains information on the 'start' and 'completion' of an activity.");
		attributeConversionModeCbx.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				conversionConfig.setShouldAddStartEventAttributes(attributeConversionModeCbx.getSelectedItem() == CSVAttributeConversionMode.ADD_TO_BOTH);
			}
		});

		SequentialGroup verticalGroup = layout.createSequentialGroup();
		verticalGroup.addGroup(layout
				.createParallelGroup()
				.addGroup(
						layout.createSequentialGroup().addComponent(errorHandlingModeLabel)
								.addComponent(errorHandlingModeCbx))
				.addGroup(
						layout.createSequentialGroup().addComponent(xFactoryLabel)
								.addComponent(xFactoryChoice)));
		verticalGroup.addGroup(layout
				.createParallelGroup()
				.addGroup(
						layout.createSequentialGroup().addComponent(emptyCellHandlingModeLabel)
							.addComponent(emptyCellHandlingModeCbx))
				.addGroup(layout.createSequentialGroup().addComponent(attributeConversionModeLabel).addComponent(attributeConversionModeCbx)));

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		horizontalGroup.addGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
								.addComponent(errorHandlingModeLabel, Alignment.LEADING, COLUMN_WIDTH, COLUMN_WIDTH,
										COLUMN_WIDTH)
								.addComponent(errorHandlingModeCbx, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(xFactoryLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
								.addComponent(xFactoryChoice, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)));
		horizontalGroup.addGroup(layout
				.createSequentialGroup()
				.addGroup(
						layout.createParallelGroup()
								.addComponent(emptyCellHandlingModeLabel, Alignment.LEADING, COLUMN_WIDTH, COLUMN_WIDTH,
										COLUMN_WIDTH)
								.addComponent(emptyCellHandlingModeCbx, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH))
				.addGroup(
						layout.createParallelGroup()
								.addComponent(attributeConversionModeLabel, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)
								.addComponent(attributeConversionModeCbx, COLUMN_WIDTH, COLUMN_WIDTH, COLUMN_WIDTH)));

		layout.linkSize(errorHandlingModeLabel, xFactoryLabel, emptyCellHandlingModeLabel, attributeConversionModeLabel);

		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
	}

	private Set<XFactory> getAvailableXFactories() {
		//Try to register XESLite Factories
		tryRegisterFactory("org.xeslite.lite.factory.XFactoryLiteImpl");
		tryRegisterFactory("org.xeslite.external.XFactoryExternalStore$MapDBDiskImpl");
		tryRegisterFactory("org.xeslite.external.XFactoryExternalStore$MapDBDiskWithoutCacheImpl");
		tryRegisterFactory("org.xeslite.external.XFactoryExternalStore$MapDBDiskSequentialAccessImpl");
		tryRegisterFactory("org.xeslite.external.XFactoryExternalStore$InMemoryStoreImpl");
		return XFactoryRegistry.instance().getAvailable();
	}

	/**
	 * Tries to load the class and call the 'register' method.
	 * 
	 * @param className
	 */
	private void tryRegisterFactory(String className) {
		try {
			getClass().getClassLoader().loadClass(className).getDeclaredMethod("register").invoke(null);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
		}
	}

}
