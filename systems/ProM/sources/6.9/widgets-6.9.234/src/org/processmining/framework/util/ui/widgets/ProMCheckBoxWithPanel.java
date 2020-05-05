package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author michael
 * 
 */
public class ProMCheckBoxWithPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1605514658715195558L;
	private final JCheckBox checkBox;
	private final JPanel panel;

	/**
	 * 
	 */
	public ProMCheckBoxWithPanel() {
		this(true, true);
	}

	/**
	 * @param checked
	 */
	public ProMCheckBoxWithPanel(final boolean checked) {
		this(checked, true);
	}

	/**
	 * @param checked
	 * @param hideIfNotChecked
	 */
	public ProMCheckBoxWithPanel(final boolean checked, final boolean hideIfNotChecked) {

		panel = new JPanel();
		checkBox = SlickerFactory.instance().createCheckBox("", checked);

		if (hideIfNotChecked) {
			checkBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					toggleVisibility();
				}
			});
		}

		setLayout(new BorderLayout());
		setOpaque(false);

		checkBox.setPreferredSize(new Dimension(30, 30));
		checkBox.setMinimumSize(checkBox.getPreferredSize());
		checkBox.setMaximumSize(checkBox.getPreferredSize());

		//panel.setMinimumSize(new Dimension(600,30));
		panel.setPreferredSize(new Dimension(530, 30));

		this.add(checkBox, BorderLayout.WEST);
		this.add(panel, BorderLayout.EAST);
	}

	/**
	 * @return
	 */
	public JCheckBox getCheckBox() {
		return checkBox;
	}

	/**
	 * @return
	 */
	public boolean isSelected() {
		return checkBox.isSelected();
	}

	/**
	 * @param checked
	 */
	public void setSelected(final boolean checked) {
		checkBox.setSelected(checked);
	}

	private void toggleVisibility() {
		panel.setVisible(!panel.isVisible());
	}

	protected JPanel getPanel() {
		return panel;
	}
}
