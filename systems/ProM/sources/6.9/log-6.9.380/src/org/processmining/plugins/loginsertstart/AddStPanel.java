package org.processmining.plugins.loginsertstart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.processmining.contexts.uitopia.UIPluginContext;

import com.fluxicon.slickerbox.components.HeaderBar;

/**
 * Insert Start Panel Class
 * 
 * 
 * @author jnakatumba
 * 
 */

public class AddStPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private String choice;
	private final ChartPanel chartPanel;
	private JPanel jPanel;
	private final double maxValue;
	private JSpinner jSpinner;
	private JLabel jLabel;
	protected JSplitPane splitPane;
	private JPanel jPanel1;
	private final Color colorBg;
	private String outlierValue;
	private ButtonGroup jButtonGroup;
	private JRadioButton removeOut;
	private JRadioButton noRemove;
	private JRadioButton estimate;

	AddStPanel(UIPluginContext context, ChartPanel chartPanel, Double maxValue) {

		this.maxValue = maxValue;
		this.chartPanel = chartPanel;
		colorBg = new Color(120, 120, 120);

		setLayout(new BorderLayout());

		addPanel();
		context.showConfiguration("Insert Missing Events", jPanel1);

	}

	private void addPanel() {

		HeaderBar header = new HeaderBar("Start Insertion Plugin ");
		header.setHeight(40);

		jLabel = new JLabel("Upper Bound(minutes)");
		jPanel1 = new JPanel();

		jSpinner = new JSpinner();

		jButtonGroup = new ButtonGroup();
		removeOut = new JRadioButton();
		noRemove = new JRadioButton();
		estimate = new JRadioButton();

		jButtonGroup.add(removeOut);
		removeOut.setSelected(true);
		removeOut.setText("Remove Outliers");

		jButtonGroup.add(noRemove);
		noRemove.setSelected(true);
		noRemove.setText("Do not Insert Start Events");

		jButtonGroup.add(estimate);
		estimate.setSelected(true);
		estimate.setText("Estimate range of Service Time");

		int min = 0;
		int step = 5;
		int initValue = 50;
		SpinnerModel model = new SpinnerNumberModel(initValue, min, maxValue, step);
		jSpinner = new JSpinner(model);
		jSpinner.setPreferredSize(new Dimension(200, 20));
		jSpinner.setMaximumSize(new Dimension(200, 20));

		jSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				outlierValue = String.valueOf(jSpinner.getValue());

			}
		});

		//jTextField = new JTextField();
		jPanel = new JPanel();
		jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

		/**
		 * jTextField.addActionListener(new ActionListener() {
		 * 
		 * public void actionPerformed(ActionEvent e) { textField =
		 * jTextField.getText();
		 * 
		 * }
		 * 
		 * });
		 */

		removeOut.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(removeOut);

		jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(jLabel);

		jSpinner.setMaximumSize(new Dimension(150, 30));
		jSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(jSpinner);

		estimate.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(estimate);

		//jTextField.setMaximumSize(new Dimension(150, 30));
		//jTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
		//jPanel.add(jTextField);

		noRemove.setAlignmentX(Component.LEFT_ALIGNMENT);
		jPanel.add(noRemove);

		jPanel1 = new JPanel();
		jPanel1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jPanel1.setLayout(new BorderLayout());

		splitPane = new JSplitPane();
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		splitPane.setRightComponent(chartPanel);
		splitPane.setLeftComponent(jPanel);
		splitPane.setBackground(colorBg);

		splitPane.setDividerSize(2);
		jPanel1.add(header, BorderLayout.NORTH);
		jPanel1.add(splitPane);

	}

	public String getChoice() {
		if (removeOut.isSelected() == true) {
			choice = outlierValue;

		} else if (noRemove.isSelected() == true) {
			choice = null;

		} else if (estimate.isSelected() == true) {
			choice = "Estimate Values";

		}

		return choice;

	}

}
