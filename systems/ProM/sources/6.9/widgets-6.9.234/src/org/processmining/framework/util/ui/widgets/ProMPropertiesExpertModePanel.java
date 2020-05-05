package org.processmining.framework.util.ui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * A {@link ProMPropertiesPanel} with a switch that shows/hides a set of
 * 'expert' properties. Use {@link #addExpertProperty(JComponent)} to add these
 * 'expert' properties.
 * 
 * @author F. Mannhardt
 * 
 */
public class ProMPropertiesExpertModePanel extends ProMPropertiesPanel {

	private static final long serialVersionUID = -8333719879782883891L;

	private static String DEFAULT_EXPERT_MODE_TEXT = "Show Expert Options";
	private static final String EXPERT_MODE_HEADER = "------------ EXPERT CONFIGURATION OPTIONS ------------";

	private final List<JComponent> expertProperties;
	private JCheckBox expertSwitch;

	private boolean showExpertSwitch;
	private String expertPropertiesText;

	public ProMPropertiesExpertModePanel(final String title) {
		this(title, true, DEFAULT_EXPERT_MODE_TEXT);
	}

	public ProMPropertiesExpertModePanel(final String title, final boolean showExpertSwitch,
			final String expertSwitchText) {
		super(title);
		this.showExpertSwitch = showExpertSwitch;
		this.expertPropertiesText = expertSwitchText;
		this.expertProperties = new ArrayList<JComponent>();
	}

	public JCheckBox addCheckBox(final String name, final boolean value, final boolean isExpert) {
		final JCheckBox checkBox = SlickerFactory.instance().createCheckBox(null, value);
		return addProperty(name, checkBox, isExpert);
	}

	public ProMTextField addTextField(final String name, final String value, final boolean isExpertProperty) {
		return addProperty(name, new ProMTextField(value), isExpertProperty);
	}

	public <E> ProMComboBox<E> addComboBox(final String name, final Iterable<E> values, final boolean isExpertProperty) {
		return addProperty(name, new ProMComboBox<E>(values), isExpertProperty);
	}

	public <E> ProMComboBox<E> addComboBox(final String name, final E[] values, final boolean isExpertProperty) {
		return addProperty(name, new ProMComboBox<E>(values), isExpertProperty);
	}

	public <T extends JComponent> T addProperty(final String name, final T component, final boolean isExpertProperty) {
		if (isExpertProperty && showExpertSwitch && expertSwitch == null) {
			addExpertSwitch();
		}
		T c = super.addProperty(name, component);
		if (isExpertProperty) {
			addExpertProperty(c);
		}
		return c;
	}

	public void showExpertProperties() {
		for (JComponent c : getExpertProperties()) {
			c.setVisible(true);
		}
	}

	public void hideExpertProperties() {
		for (JComponent c : getExpertProperties()) {
			c.getParent().setVisible(false);
		}
	}

	public void toggleExpertPanel() {
		for (JComponent c : getExpertProperties()) {
			c.getParent().setVisible(!c.getParent().isVisible());
		}
	}

	/**
	 * @return {@link List} of all properties that are only visible in expert
	 *         mode
	 */
	public List<JComponent> getExpertProperties() {
		return Collections.unmodifiableList(expertProperties);
	}

	public void addExpertProperty(JComponent c) {
		expertProperties.add(c);
		// Hide component initially
		c.getParent().setVisible(false);
	}

	private void addExpertSwitch() {
		expertSwitch = addCheckBox(expertPropertiesText, false);
		expertSwitch.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				toggleExpertPanel();
			}
		});
		addProperty(null, SlickerFactory.instance().createLabel(EXPERT_MODE_HEADER), true);
	}

}
