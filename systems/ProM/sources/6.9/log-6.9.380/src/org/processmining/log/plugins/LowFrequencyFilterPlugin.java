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
import org.processmining.log.algorithms.LowFrequencyFilterAlgorithm;
import org.processmining.log.connections.LowFrequencyFilterConnection;
import org.processmining.log.dialogs.LowFrequencyFilterDialog;
import org.processmining.log.help.LowFrequencyFilterHelp;
import org.processmining.log.parameters.LowFrequencyFilterParameters;

@Plugin(name = "Filter Out Low-Frequency Traces (Single Log)", categories = { PluginCategory.Filtering }, parameterLabels = {"Event Log"}, returnLabels = { "Filtered Log" }, returnTypes = {XLog.class }, userAccessible = true, help = LowFrequencyFilterHelp.TEXT)
public class LowFrequencyFilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Filter Out Low-Frequency Traces (Single Log), UI", requiredParameterLabels = { 0 })
	public XLog runUI(UIPluginContext context, XLog log) {
		LowFrequencyFilterParameters parameters = new LowFrequencyFilterParameters(log);
		LowFrequencyFilterDialog dialog = new LowFrequencyFilterDialog(log, parameters);
		InteractionResult result = context.showWizard("Configure Low-Frequency Filter", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnections(context, log, parameters);
	}
	
	/**
	 * @deprecated Use runUI instead.
	 */
	@Deprecated
	public XLog publicUI(UIPluginContext context, XLog log) {
		return runUI(context, log);
	}
	
	@PluginVariant(variantLabel = "Filter Out Low-Frequency Traces (Single Log), Parameters", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log, LowFrequencyFilterParameters parameters) {
		return runConnections(context, log, parameters);
	}
	
	/**
	 * @deprecated Use run instead.
	 */
	@Deprecated
	public XLog publicParameters(PluginContext context, XLog log, LowFrequencyFilterParameters parameters) {
		return run(context, log, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Filter Out Low-Frequency Traces (Single Log), Default", requiredParameterLabels = { 0 })
	public XLog runDefault(PluginContext context, XLog log) {
		LowFrequencyFilterParameters parameters = new LowFrequencyFilterParameters(log);
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use run instead.
	 */
	@Deprecated
	public XLog publicDefault(PluginContext context, XLog log) {
		return runDefault(context, log);
	}
	
	private XLog runConnections(PluginContext context, XLog log, LowFrequencyFilterParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<LowFrequencyFilterConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						LowFrequencyFilterConnection.class, context, log);
				for (LowFrequencyFilterConnection connection : connections) {
					if (connection.getObjectWithRole(LowFrequencyFilterConnection.LOG)
							.equals(log) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(LowFrequencyFilterConnection.FILTEREDLOG);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		XLog filteredLog = (new LowFrequencyFilterAlgorithm()).apply(context, log, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new LowFrequencyFilterConnection(log, filteredLog, parameters));
		}
		return filteredLog;
	}
}
