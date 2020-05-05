package org.processmining.framework.util.ui.wizard;

import javax.swing.JComponent;

/**
 * @author michael
 * 
 * @param <SettingsModel>
 */
public interface ProMWizardStep<SettingsModel> {

	/**
	 * @param model
	 * @param component
	 * @return
	 */
	public SettingsModel apply(SettingsModel model, JComponent component);

	/**
	 * @param model
	 * @param component
	 * @return
	 */
	public boolean canApply(SettingsModel model, JComponent component);

	/**
	 * @param model
	 * @return
	 */
	public JComponent getComponent(SettingsModel model);

	/**
	 * @return
	 */
	public String getTitle();
}
