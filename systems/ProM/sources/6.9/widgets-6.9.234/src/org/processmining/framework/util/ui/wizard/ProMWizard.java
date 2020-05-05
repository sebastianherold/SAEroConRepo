package org.processmining.framework.util.ui.wizard;

/**
 * @author michael
 * 
 * @param <SettingsModel>
 * @param <WizardModel>
 */
public interface ProMWizard<SettingsModel, WizardModel> {

	/**
	 * @param model
	 * @return
	 */
	public ProMWizardStep<SettingsModel> getFirst(WizardModel model);

	/**
	 * @param wizardModel
	 * @return
	 */
	public SettingsModel getModel(WizardModel wizardModel);

	/**
	 * @param model
	 * @param current
	 * @return
	 */
	public ProMWizardStep<SettingsModel> getNext(WizardModel model, ProMWizardStep<SettingsModel> current);

	/**
	 * @param model
	 * @param currentWizardModel
	 * @return
	 */
	public WizardModel getWizardModel(SettingsModel model, WizardModel currentWizardModel);

	//we do not have canFinish, as the ProM Wizard interface only 
	//shows the finish button if it is the last step
	//public boolean canFinish(WizardModel model);
	/**
	 * @param model
	 * @return
	 */
	public boolean isFinished(WizardModel model);

	/**
	 * @param model
	 * @return
	 */
	public boolean isLastStep(WizardModel model);

}
