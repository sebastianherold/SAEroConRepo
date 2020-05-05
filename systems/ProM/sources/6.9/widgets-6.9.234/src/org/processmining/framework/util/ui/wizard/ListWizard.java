package org.processmining.framework.util.ui.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author michael
 * 
 * @param <SettingsModel>
 */
public class ListWizard<SettingsModel> implements ProMWizard<SettingsModel, ListWizard.ListModel<SettingsModel>> {

	static class ListModel<SettingsModel> {
		private final SettingsModel model;

		private int step = 0;

		public ListModel(final int step, final SettingsModel model) {
			super();
			this.step = step;
			this.model = model;
		}

		public SettingsModel getModel() {
			return model;
		}

		public int getStep() {
			return step;
		}

	}

	private final List<ProMWizardStep<SettingsModel>> steps;

	/**
	 * @param steps
	 */
	public ListWizard(final List<ProMWizardStep<SettingsModel>> steps) {
		this.steps = new ArrayList<ProMWizardStep<SettingsModel>>(steps);
	}

	/**
	 * @param steps
	 */
	public ListWizard(final ProMWizardStep<SettingsModel>... steps) {
		this.steps = new ArrayList<ProMWizardStep<SettingsModel>>(Arrays.asList(steps));
	}

	/**
	 * @param step
	 */
	public ListWizard(final ProMWizardStep<SettingsModel> step) {
		this.steps = new ArrayList<ProMWizardStep<SettingsModel>>();
		steps.add(step);
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getFirst(java.lang.Object)
	 */
	@Override
	public ProMWizardStep<SettingsModel> getFirst(final ListModel<SettingsModel> model) {
		return getNext(model, null);
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getModel(java.lang.Object)
	 */
	@Override
	public SettingsModel getModel(final ListModel<SettingsModel> wizardModel) {
		return wizardModel.getModel();
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getNext(java.lang.Object,
	 *      org.processmining.framework.util.ui.wizard.ProMWizardStep)
	 */
	@Override
	public ProMWizardStep<SettingsModel> getNext(final ListModel<SettingsModel> model,
			final ProMWizardStep<SettingsModel> current) {

		if (steps.size() > model.getStep()) {
			return steps.get(model.getStep());
		}

		return null;
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getWizardModel(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public ListModel<SettingsModel> getWizardModel(final SettingsModel model,
			final ListModel<SettingsModel> currentWizardModel) {
		if (currentWizardModel == null) {
			return new ListModel<SettingsModel>(0, model);
		} else {
			return new ListModel<SettingsModel>(currentWizardModel.getStep() + 1, model);
		}
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#isFinished(java.lang.Object)
	 */
	@Override
	public boolean isFinished(final ListModel<SettingsModel> model) {

		return model.getStep() >= steps.size();

	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#isLastStep(java.lang.Object)
	 */
	@Override
	public boolean isLastStep(final ListModel<SettingsModel> model) {
		return model.getStep() >= steps.size() - 1;
	}

}
