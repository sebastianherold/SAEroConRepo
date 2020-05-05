package org.processmining.framework.util.ui.widgets;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/**
 * @author michael
 * 
 */
public class ProMComboBoxUI extends BasicComboBoxUI {
	/**
	 * @param c
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static ComponentUI createUI(final JComponent c) {
		return new ProMComboBoxUI((JComboBox/* <?> */) c);
	}

	@SuppressWarnings("rawtypes")
	private final JComboBox/* <?> */component;

	/**
	 * @param c
	 */
	@SuppressWarnings("rawtypes")
	public ProMComboBoxUI(final JComboBox/* <?> */c) {
		component = c;
	}

	/**
	 * @see javax.swing.plaf.basic.BasicComboBoxUI#configureArrowButton()
	 */
	@Override
	public void configureArrowButton() {
		super.configureArrowButton();
		arrowButton.setBorder(BorderFactory.createEmptyBorder());
	}

	/**
	 * @see javax.swing.plaf.basic.BasicComboBoxUI#paintCurrentValueBackground(java.awt.Graphics,
	 *      java.awt.Rectangle, boolean)
	 */
	@Override
	public void paintCurrentValueBackground(final Graphics g, final Rectangle bounds, final boolean hasFocus) {
	}

	@Override
	protected void configureEditor() {
		super.configureEditor();
		if (editor instanceof JComponent) {
			((JComponent) editor).setBorder(BorderFactory.createEmptyBorder());
			((JComponent) editor).setBackground(WidgetColors.COLOR_LIST_BG);
			((JComponent) editor).setForeground(WidgetColors.COLOR_LIST_FG);
		}
	}

	@Override
	protected JButton createArrowButton() {
		final JButton button = new BasicArrowButton(SwingConstants.SOUTH, WidgetColors.COLOR_ENCLOSURE_BG,
				WidgetColors.COLOR_ENCLOSURE_BG, WidgetColors.COLOR_LIST_FG, WidgetColors.COLOR_ENCLOSURE_BG);
		button.setName("ComboBox.arrowButton");
		return button;
	}

	@Override
	protected ComboPopup createPopup() {
		final BasicComboPopup result = new ProMComboBoxPopup(component);
		return result;
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		comboBox.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
	}

	@Override
	protected Rectangle rectangleForCurrentValue() {
		final int width = comboBox.getWidth();
		final int height = comboBox.getHeight();
		final Insets insets = getInsets();
		int buttonSize = height - (insets.top + insets.bottom);
		if (arrowButton != null) {
			buttonSize = arrowButton.getWidth();
		}
		return new Rectangle(insets.left + 3, insets.top - 1, width
				- (insets.left + insets.right + buttonSize + 3 + 3 + 10), height - (insets.top + insets.bottom) + 1);
	}

}
