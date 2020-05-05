package org.processmining.log.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.algorithms.SplitLogAlgorithm;
import org.processmining.log.dialogs.SplitLogDialog;
import org.processmining.log.help.SplitLogHelp;
import org.processmining.log.parameters.SplitLogParameters;

@Plugin(name = "Split traces", categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Parameters" }, 
returnLabels = { "Log containing splitted traces" }, returnTypes = { XLog.class }, help = SplitLogHelp.TEXT)
public class SplitLogPlugin extends SplitLogAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Split traces, UI", requiredParameterLabels = { 0 })
	public XLog runUI(UIPluginContext context, XLog log) {
		SplitLogParameters parameters = new SplitLogParameters(log);
		SplitLogDialog dialog = new SplitLogDialog(parameters, log);
		InteractionResult result = context.showWizard("Configure split", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return run(context, log, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Merge logs, Default", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log) {
		SplitLogParameters parameters = new SplitLogParameters(log);

		XLog splittedLog = apply(context, log, parameters);

		return splittedLog;
	}

	@PluginVariant(variantLabel = "Merge logs, Parameters", requiredParameterLabels = { 0, 1 })
	public XLog run(PluginContext context, XLog log,
			SplitLogParameters parameters) {

		XLog splittedLog = apply(context, log, parameters);

		return splittedLog;
	}
}
