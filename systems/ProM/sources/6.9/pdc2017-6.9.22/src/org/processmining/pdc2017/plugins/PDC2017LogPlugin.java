package org.processmining.pdc2017.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.pdc2017.algorithms.PDC2017LogAlgorithm;
import org.processmining.pdc2017.dialogs.PDC2017Dialog;
import org.processmining.pdc2017.parameters.PDC2017Parameters;

@Plugin(name = "Create PDC 2017 Log", parameterLabels = {}, returnLabels = { "PDC 2017 Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2017LogPlugin extends PDC2017LogAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { })
	public XLog run(UIPluginContext context) throws Exception {
		PDC2017Parameters parameters = new PDC2017Parameters();
		PDC2017Dialog dialog = new PDC2017Dialog(parameters);
		InteractionResult result = context.showWizard("Select log to create", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, parameters);
	}
}

