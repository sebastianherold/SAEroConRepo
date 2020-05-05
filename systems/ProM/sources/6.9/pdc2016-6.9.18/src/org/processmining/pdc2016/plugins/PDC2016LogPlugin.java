package org.processmining.pdc2016.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.pdc2016.algorithms.PDC2016LogAlgorithm;
import org.processmining.pdc2016.dialogs.PDC2016Dialog;
import org.processmining.pdc2016.parameters.PDC2016Parameters;

@Plugin(name = "Create PDC 2016 Log", parameterLabels = {}, returnLabels = { "PDC 2016 Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2016 Plug-in")
public class PDC2016LogPlugin extends PDC2016LogAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { })
	public XLog run(UIPluginContext context) throws Exception {
		PDC2016Parameters parameters = new PDC2016Parameters();
		PDC2016Dialog dialog = new PDC2016Dialog(parameters);
		InteractionResult result = context.showWizard("Select log to create", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, parameters);
	}
}

