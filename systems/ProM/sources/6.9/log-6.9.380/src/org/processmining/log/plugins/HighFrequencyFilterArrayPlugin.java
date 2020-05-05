package org.processmining.log.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.dialogs.HighFrequencyFilterDialog;
import org.processmining.log.help.HighFrequencyFilterArrayHelp;
import org.processmining.log.models.EventLogArray;
import org.processmining.log.models.impl.EventLogArrayFactory;
import org.processmining.log.parameters.HighFrequencyFilterParameters;

@Plugin(name = "Filter In High-Frequency Traces (Multiple Logs)", categories = { PluginCategory.Filtering }, parameterLabels = { "Event Logs" }, returnLabels = { "Filtered Logs" }, returnTypes = { EventLogArray.class }, userAccessible = true, help = HighFrequencyFilterArrayHelp.TEXT)
public class HighFrequencyFilterArrayPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org", pack="Log")
	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Multiple Logs), UI", requiredParameterLabels = { 0 })
	public EventLogArray runUI(UIPluginContext context, EventLogArray logs) {
		if (logs.getSize() > 0) {
			XLog log = logs.getLog(0);
			HighFrequencyFilterParameters parameters = new HighFrequencyFilterParameters(log);
			HighFrequencyFilterDialog dialog = new HighFrequencyFilterDialog(log, parameters);
			InteractionResult result = context.showWizard("Configure High-Frequency Filter", true, true, dialog);
			if (result != InteractionResult.FINISHED) {
				return null;
			}
			EventLogArray filteredLogs = EventLogArrayFactory.createEventLogArray();
			filteredLogs.init();
			for (int i = 0; i < logs.getSize(); i++) {
				parameters.displayMessage("[HighFrequencyFilterArrayPlugin] Filtering log " + i + " of " + logs.getSize());
				filteredLogs.addLog((new HighFrequencyFilterPlugin()).run(context, logs.getLog(i), parameters));
			}
			return filteredLogs;
		}
		return null;
	}

	/**
	 * @deprecated Use runUI instead.
	 */
	@Deprecated
	public EventLogArray publicUIArray(UIPluginContext context, EventLogArray logs) {
		return runUI(context, logs);
	}

	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Multiple Logs), Parameters", requiredParameterLabels = { 0 })
	public EventLogArray run(PluginContext context, EventLogArray logs,
			HighFrequencyFilterParameters parameters) {
		if (logs.getSize() > 0) {
			EventLogArray filteredLogs = EventLogArrayFactory.createEventLogArray();
			filteredLogs.init();
			for (int i = 0; i < logs.getSize(); i++) {
				parameters.displayMessage("[HighFrequencyFilterArrayPlugin] Filtering log " + i + " of " + logs.getSize());
				filteredLogs.addLog((new HighFrequencyFilterPlugin()).run(context, logs.getLog(i), parameters));
			}
			return filteredLogs;
		}
		return null;
	}

	/**
	 * @deprecated Use run instead.
	 */
	@Deprecated
	public EventLogArray publicParameters(PluginContext context, EventLogArray logs,
			HighFrequencyFilterParameters parameters) {
		return run(context, logs, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org", pack="Log")
	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Multiple Logs), Default", requiredParameterLabels = { 0 })
	public EventLogArray runDefault(PluginContext context, EventLogArray logs) {
		if (logs.getSize() > 0) {
			HighFrequencyFilterParameters parameters = new HighFrequencyFilterParameters(logs.getLog(0));
			EventLogArray filteredLogs = EventLogArrayFactory.createEventLogArray();
			filteredLogs.init();
			for (int i = 0; i < logs.getSize(); i++) {
				parameters.displayMessage("[HighFrequencyFilterArrayPlugin] Filtering log " + i + " of " + logs.getSize());
				filteredLogs.addLog((new HighFrequencyFilterPlugin()).run(context, logs.getLog(i), parameters));
			}
			return filteredLogs;
		}
		return null;
	}

	/**
	 * @deprecated Use runDefault instead.
	 */
	@Deprecated
	public EventLogArray publicDefault(PluginContext context, EventLogArray logs) {
		return runDefault(context, logs);
	}
}
