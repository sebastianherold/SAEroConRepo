package org.processmining.pdc2019.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.pdc2019.algorithms.PDC2019LogAlgorithm;
import org.processmining.pdc2019.dialogs.PDC2019Dialog;
import org.processmining.pdc2019.parameters.PDC2019Parameters;

@Plugin(name = "Create PDC 2019 Log", parameterLabels = {}, returnLabels = { "PDC 2019 Log" }, returnTypes = { XLog.class }, userAccessible = true, help = "PDC 2017 Plug-in")
public class PDC2019LogPlugin extends PDC2019LogAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { })
	public XLog run(UIPluginContext context) throws Exception {
		PDC2019Parameters parameters = new PDC2019Parameters();
		PDC2019Dialog dialog = new PDC2019Dialog(parameters);
		InteractionResult result = context.showWizard("Select log to create", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return apply(context, parameters);
	}
}

