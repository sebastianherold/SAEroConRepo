package org.processmining.log.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.algorithms.MergeLogsAlgorithm;
import org.processmining.log.dialogs.MergeLogsDialog;
import org.processmining.log.help.MergeLogsHelp;
import org.processmining.log.parameters.MergeLogsParameters;

@Plugin(name = "Merge logs", categories = { PluginCategory.Filtering }, parameterLabels = { "Main Log", "Sub log", "Parameters" }, 
returnLabels = { "Merged logs" }, returnTypes = { XLog.class }, help = MergeLogsHelp.TEXT)
public class MergeLogsPlugin extends MergeLogsAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Decomposed Discovery, UI", requiredParameterLabels = { 0, 1 })
	public XLog runUI(UIPluginContext context, XLog mainLog, XLog subLog) {
		MergeLogsParameters parameters = new MergeLogsParameters();
		MergeLogsDialog dialog = new MergeLogsDialog(parameters, mainLog);
		InteractionResult result = context.showWizard("Configure merge", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}
		return run(context, mainLog, subLog, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Merge logs, Default", requiredParameterLabels = { 0, 1 })
	public XLog run(PluginContext context, XLog mainLog, XLog subLog) {
		MergeLogsParameters parameters = new MergeLogsParameters();

		XLog log = apply(context, mainLog, subLog, parameters);

		return log;
	}

	@PluginVariant(variantLabel = "Merge logs, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public XLog run(PluginContext context, XLog mainLog, XLog subLog,
			MergeLogsParameters parameters) {

		XLog log = apply(context, mainLog, subLog, parameters);

		return log;
	}
}
