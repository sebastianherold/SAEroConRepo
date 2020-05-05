package org.processmining.plugins.loginsertstart;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.processmining.contexts.uitopia.UIPluginContext;

import com.fluxicon.slickerbox.components.HeaderBar;

/**
 * Insert Start First Panel class
 * 
 * @author jnakatumba
 * 
 */

public class AddStSecPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel;
	private JPanel panel;
	private JLabel jLabel;
	private ButtonGroup jButtonGroup;
	private JRadioButton resourcePer;
	private JRadioButton casePer;
	private JRadioButton averageResCase;
	private String displayChoice;

	public AddStSecPanel(UIPluginContext context) {

		setLayout(new BorderLayout());

		addPanel();

	}

	private void addPanel() {

		HeaderBar header = new HeaderBar("Start Insertion Plugin Settings");
		header.setHeight(40);

		jLabel = new JLabel("Show Outliers Based on:");

		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		TitledBorder title;
		title = BorderFactory.createTitledBorder(raisedetched);
		title.setTitleJustification(TitledBorder.CENTER);

		jButtonGroup = new ButtonGroup();
		resourcePer = new JRadioButton();
		casePer = new JRadioButton();
		averageResCase = new JRadioButton();

		jButtonGroup.add(resourcePer);
		resourcePer.setSelected(true);
		resourcePer.setText("Resource perspective");

		jButtonGroup.add(casePer);
		casePer.setSelected(true);
		casePer.setText("Case perspective");

		jButtonGroup.add(averageResCase);
		averageResCase.setSelected(true);
		averageResCase.setText("Maximum of Case and Resource Perspectives");

		jPanel = new JPanel(new BorderLayout());
		panel = new JPanel();

		jPanel.add(header, BorderLayout.NORTH);

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		jLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(jLabel);

		resourcePer.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(resourcePer);

		casePer.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(casePer);

		averageResCase.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(averageResCase);

		panel.setBorder(title);

		jPanel.add(panel, BorderLayout.CENTER);

	}

	public JPanel getPanel() {
		return jPanel;
	}

	public String getDisplayChoice() {
		if (resourcePer.isSelected() == true) {

			displayChoice = "Resource perspective";

		} else if (casePer.isSelected() == true) {
			displayChoice = "Case perspective";

		} else if (averageResCase.isSelected() == true) {
			displayChoice = "Resource/Case perspective";

		}
		return displayChoice;
	}

}
