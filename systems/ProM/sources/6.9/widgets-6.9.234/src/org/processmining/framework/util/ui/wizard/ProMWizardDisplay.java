package org.processmining.framework.util.ui.wizard;

import java.util.Stack;

import javax.swing.JComponent;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;

/**
 * @author michael
 * 
 */
public class ProMWizardDisplay {

	/**
	 * @param context
	 * @param wizard
	 * @param model
	 * @return
	 */
	public static <SettingsModel, WizardModel> SettingsModel show(final UIPluginContext context,
			final ProMWizard<SettingsModel, WizardModel> wizard, final SettingsModel model) {

		WizardModel wizardModel = wizard.getWizardModel(model, null);

		ProMWizardStep<SettingsModel> current = wizard.getFirst(wizardModel);
		JComponent configuration = current.getComponent(wizard.getModel(wizardModel));

		final Stack<Pair<ProMWizardStep<SettingsModel>, WizardModel>> steps = new Stack<Pair<ProMWizardStep<SettingsModel>, WizardModel>>();

		int stepCount = 1;
		do {
			String title;
			if (current.getTitle() == null) {
				title = "Step " + stepCount;
			} else {
				title = current.getTitle();
			}

			final InteractionResult result = context.showWizard(title, steps.isEmpty(), wizard.isLastStep(wizardModel),
					configuration);

			switch (result) {
				case FINISHED :
				case NEXT :
					if (current.canApply(wizard.getModel(wizardModel), configuration)) {
						steps.push(new Pair<ProMWizardStep<SettingsModel>, WizardModel>(current, wizardModel));

						wizardModel = wizard.getWizardModel(current.apply(wizard.getModel(wizardModel), configuration),
								wizardModel);
						current = wizard.getNext(wizardModel, current);
						if (current != null) {
							configuration = current.getComponent(wizard.getModel(wizardModel));
							stepCount++;
						}
					}
					// if canApply is false, the current remains the same!
					break;
				case PREV :
					if (current.canApply(wizard.getModel(wizardModel), configuration)) {
						wizardModel = wizard.getWizardModel(current.apply(wizard.getModel(wizardModel), configuration),
								wizardModel);
					}
					final Pair<ProMWizardStep<SettingsModel>, WizardModel> prevstep = steps.pop();
					current = prevstep.getFirst();
					wizardModel = prevstep.getSecond();

					configuration = current.getComponent(wizard.getModel(wizardModel));
					stepCount--;
					break;
				case CANCEL :
					return null;
				case CONTINUE :
					//never used.
			}

		} while (!wizard.isFinished(wizardModel));

		//ProMWizardStep<SettingsModel> lastStupidStep = wizard.getLast(wizardModel, current); 
		//context.showWizard(lastStupidStep.getTitle(), true, true, lastStupidStep.getComponent(wizard.getModel(wizardModel)));

		return wizard.getModel(wizardModel);
	}

}
