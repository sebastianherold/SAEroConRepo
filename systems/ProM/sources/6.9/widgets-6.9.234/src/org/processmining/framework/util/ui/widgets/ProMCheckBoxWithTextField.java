package org.processmining.framework.util.ui.widgets;

import java.awt.BorderLayout;

/**
 * @author michael
 * 
 */
public class ProMCheckBoxWithTextField extends ProMCheckBoxWithPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3331182375174654223L;
	private final ProMTextField textField;

	/**
	 * 
	 */
	public ProMCheckBoxWithTextField() {
		this(true, true, "");
	}

	/**
	 * @param checked
	 * @param hideIfNotSelected
	 * @param text
	 */
	public ProMCheckBoxWithTextField(final boolean checked, final boolean hideIfNotSelected, final String text) {
		super(checked, hideIfNotSelected);

		textField = new ProMTextField(text);
		getPanel().setLayout(new BorderLayout());
		getPanel().add(textField, BorderLayout.CENTER);

	}

	/**
	 * @param checked
	 * @param text
	 */
	public ProMCheckBoxWithTextField(final boolean checked, final String text) {
		this(checked, true, text);
	}

	/**
	 * @param text
	 */
	public ProMCheckBoxWithTextField(final String text) {
		this(true, true, text);
	}

	/**
	 * @return
	 */
	public String getText() {
		return textField.getText();
	}

	/**
	 * @param text
	 */
	public void setText(final String text) {
		textField.setText(text);
	}

}
