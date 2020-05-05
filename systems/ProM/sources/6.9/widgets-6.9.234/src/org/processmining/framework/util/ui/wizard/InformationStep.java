package org.processmining.framework.util.ui.wizard;

import javax.swing.JComponent;

/**
 * @author michael
 * 
 * @param <M>
 */
public abstract class InformationStep<M> implements ProMWizardStep<M> {

	private final String title;

	/**
	 * @param title
	 */
	public InformationStep(final String title) {
		this.title = title;
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizardStep#apply(java.lang.Object,
	 *      javax.swing.JComponent)
	 */
	@Override
	public M apply(final M model, final JComponent component) {
		return model;
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizardStep#canApply(java.lang.Object,
	 *      javax.swing.JComponent)
	 */
	@Override
	public boolean canApply(final M model, final JComponent component) {
		return true;
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizardStep#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

}
