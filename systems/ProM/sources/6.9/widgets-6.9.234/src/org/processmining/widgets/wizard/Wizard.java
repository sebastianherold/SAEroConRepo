package org.processmining.widgets.wizard;

import org.deckfour.uitopia.api.event.TaskListener;
import org.processmining.contexts.uitopia.UIPluginContext;

public class Wizard {

	public static <P> WizardResult<P> show(UIPluginContext context, Dialog<P> dialog) {
		return route(context, dialog, embed(context, dialog).getInteractionResult());
	}

	private static <P> WizardResult<P> embed(UIPluginContext context, Dialog<P> dialog) {
		return new WizardResult<>(dialog.getParameters(), context.showWizard(dialog.getTitle(),
				!dialog.hasPreviousDialog(), !dialog.hasNextDialog(), dialog.visualize()));
	}

	private static <P> WizardResult<P> route(UIPluginContext context, Dialog<P> dialog,
			TaskListener.InteractionResult interactionResult) {
		Dialog<P> next;
		switch (interactionResult) {
			case NEXT :
				next = dialog.getNext();
				return route(context, next, embed(context, next).getInteractionResult());
			case PREV :
				next = dialog.getPrevious();
				if (next == null)
					next = dialog;
				return route(context, next, embed(context, next).getInteractionResult());
			case FINISHED :
				next = dialog.getNext();
				if (next == null) {
					dialog.updateParametersOnGetNext();
					return new WizardResult<>(dialog.getParameters(), interactionResult);
				}
				return route(context, next, embed(context, next).getInteractionResult());
			default :
				return new WizardResult<>(dialog.getParameters(), interactionResult);
		}
	}

}
