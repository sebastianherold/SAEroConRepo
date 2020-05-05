package org.processmining.framework.util.ui.wizard;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author michael
 * 
 * @param <SettingsModel>
 * @param <Key>
 */
public abstract class MapWizard<SettingsModel, Key> implements
		ProMWizard<SettingsModel, MapWizard.MapModel<SettingsModel, Key>> {

	/**
	 * @author michael
	 * 
	 * @param <SettingsModel>
	 * @param <Key>
	 */
	public static class MapModel<SettingsModel, Key> {

		private final Key current;
		private final Key previous;
		private final SettingsModel settings;

		/**
		 * @param current
		 * @param model
		 * @param previous
		 */
		public MapModel(final Key current, final SettingsModel model, final Key previous) {
			super();

			this.current = current;
			this.settings = model;
			this.previous = previous;
		}

		/**
		 * @return the current
		 */
		public Key getCurrent() {
			return current;
		}

		/**
		 * @return
		 */
		public SettingsModel getModel() {
			return this.settings;
		}

		/**
		 * @return
		 */
		public Key getPrevious() {
			return previous;
		}
	}

	protected Map<Key, ProMWizardStep<SettingsModel>> steps;

	/**
	 * @param steps
	 */
	public MapWizard(final Map<Key, ProMWizardStep<SettingsModel>> steps) {
		this.steps = new HashMap<Key, ProMWizardStep<SettingsModel>>(steps);
	}

	protected MapWizard() {
		this.steps = new HashMap<Key, ProMWizardStep<SettingsModel>>();
	}

	/**
	 * @param currentWizardModel
	 * @return
	 */
	public abstract Collection<Key> getFinalKeys(MapModel<SettingsModel, Key> currentWizardModel);

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getFirst(java.lang.Object)
	 */
	@Override
	public ProMWizardStep<SettingsModel> getFirst(final MapModel<SettingsModel, Key> model) {
		return getNext(model, null);
	}

	/**
	 * @param settings
	 * @return
	 */
	public abstract Key getInitialKey(SettingsModel settings);

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getModel(java.lang.Object)
	 */
	@Override
	public SettingsModel getModel(final MapModel<SettingsModel, Key> wizardModel) {
		return wizardModel.getModel();
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getNext(java.lang.Object,
	 *      org.processmining.framework.util.ui.wizard.ProMWizardStep)
	 */
	@Override
	public ProMWizardStep<SettingsModel> getNext(final MapModel<SettingsModel, Key> model,
			final ProMWizardStep<SettingsModel> current) {
		if (steps.containsKey(model.getCurrent())) {
			return steps.get(model.getCurrent());
		}
		return null;
	}

	/**
	 * @param currentWizardModel
	 * @return
	 */
	public abstract Key getNextKey(MapModel<SettingsModel, Key> currentWizardModel);

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#getWizardModel(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	public MapModel<SettingsModel, Key> getWizardModel(final SettingsModel model,
			final MapModel<SettingsModel, Key> currentWizardModel) {
		if (currentWizardModel == null) {
			return new MapModel<SettingsModel, Key>(getInitialKey(model), model, null);
		} else {
			return new MapModel<SettingsModel, Key>(getNextKey(currentWizardModel), model,
					currentWizardModel.getCurrent());
		}
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#isFinished(java.lang.Object)
	 */
	@Override
	public boolean isFinished(final MapModel<SettingsModel, Key> model) {
		return model.getPrevious() != null && getFinalKeys(model).contains(model.getPrevious());
	}

	/**
	 * @see org.processmining.framework.util.ui.wizard.ProMWizard#isLastStep(java.lang.Object)
	 */
	@Override
	public boolean isLastStep(final MapModel<SettingsModel, Key> model) {
		return model.getCurrent() != null && getFinalKeys(model).contains(model.getCurrent());
	}

}
