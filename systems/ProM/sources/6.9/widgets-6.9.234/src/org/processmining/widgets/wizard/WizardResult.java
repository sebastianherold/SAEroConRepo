package org.processmining.widgets.wizard;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;

public class WizardResult<P> {

	private final P parameters;

	private final InteractionResult interactionResult;

	public WizardResult(P parameters, InteractionResult interactionResult) {
		this.parameters = parameters;
		this.interactionResult = interactionResult;
	}

	public P getParameters() {
		return parameters;
	}

	public InteractionResult getInteractionResult() {
		return interactionResult;
	}
	
	
}
