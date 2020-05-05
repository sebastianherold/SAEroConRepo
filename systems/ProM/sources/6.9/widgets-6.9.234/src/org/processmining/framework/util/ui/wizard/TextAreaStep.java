package org.processmining.framework.util.ui.wizard;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.widgets.ProMTextArea;

class TextAreaStep extends ProMTextArea implements ProMWizardStep<String> {

	private static final long serialVersionUID = 7227063553927410431L;

	private final String title;

	public TextAreaStep() {
		this("");
	}

	public TextAreaStep(final String title) {
		this("", "");
	}

	public TextAreaStep(final String title, final String text) {
		super();

		this.title = title;

		setText(text);

	}

	@Override
	public String apply(final String model, final JComponent component) {
		if (component instanceof TextAreaStep) {
			return ((TextAreaStep) component).getText();
		} else {
			return "";
		}
	}

	@Override
	public boolean canApply(final String model, final JComponent component) {
		return true;
	}

	@Override
	public JComponent getComponent(final String model) {
		return new TextAreaStep(title, model);
	}

	@Override
	public String getTitle() {
		return title;
	}
}