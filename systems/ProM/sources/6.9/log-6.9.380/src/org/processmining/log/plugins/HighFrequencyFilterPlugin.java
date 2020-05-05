package org.processmining.log.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.algorithms.HighFrequencyFilterAlgorithm;
import org.processmining.log.connections.HighFrequencyFilterConnection;
import org.processmining.log.dialogs.HighFrequencyFilterDialog;
import org.processmining.log.help.HighFrequencyFilterHelp;
import org.processmining.log.parameters.HighFrequencyFilterParameters;

@Plugin(name = "Filter In High-Frequency Traces (Single Log)", categories = { PluginCategory.Filtering }, parameterLabels = { "Event Log" }, returnLabels = { "Filtered Log" }, returnTypes = { XLog.class }, userAccessible = true, help = HighFrequencyFilterHelp.TEXT)
public class HighFrequencyFilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org", pack="Log")
	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Single Log), UI", requiredParameterLabels = { 0 })
	public XLog runUI(UIPluginContext context, XLog log) {
		HighFrequencyFilterParameters parameters = new HighFrequencyFilterParameters(log);
		HighFrequencyFilterDialog dialog = new HighFrequencyFilterDialog(log, parameters);
		InteractionResult result = context.showWizard("Configure High-Frequency Filter", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use runUI() instead.
	 */
	@Deprecated
	public XLog publicUI(UIPluginContext context, XLog log) {
		return runUI(context, log);
	}

	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Single Log), Parameters", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log, HighFrequencyFilterParameters parameters) {
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use run() instead.
	 */
	@Deprecated
	public XLog publicParameters(PluginContext context, XLog log, HighFrequencyFilterParameters parameters) {
		return run(context, log, parameters);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org", pack="Log")
	@PluginVariant(variantLabel = "Filter In High-Frequency Traces (Single Log), Default", requiredParameterLabels = { 0 })
	public XLog runDefault(PluginContext context, XLog log) {
		HighFrequencyFilterParameters parameters = new HighFrequencyFilterParameters(log);
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use runDefault() instead.
	 */
	@Deprecated
	public XLog publicDefault(PluginContext context, XLog log) {
		return runDefault(context, log);
	}

	private XLog runConnections(PluginContext context, XLog log, HighFrequencyFilterParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<HighFrequencyFilterConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(HighFrequencyFilterConnection.class,
						context, log);
				for (HighFrequencyFilterConnection connection : connections) {
					if (connection.getObjectWithRole(HighFrequencyFilterConnection.LOG).equals(log)
							&& connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(HighFrequencyFilterConnection.FILTEREDLOG);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		XLog filteredLog = (new HighFrequencyFilterAlgorithm()).apply(context, log, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new HighFrequencyFilterConnection(log, filteredLog, parameters));
		}
		return filteredLog;
	}
}
