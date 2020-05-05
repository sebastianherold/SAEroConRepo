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
import org.processmining.log.algorithms.LowOccurrencesFilterAlgorithm;
import org.processmining.log.connections.LowOccurrencesFilterConnection;
import org.processmining.log.dialogs.LowOccurrencesFilterDialog;
import org.processmining.log.help.LowOccurrencesFilterHelp;
import org.processmining.log.parameters.LowOccurrencesFilterParameters;

@Plugin(name = "Filter Out Low-Occurrence Traces (Single Log)", categories = { PluginCategory.Filtering }, parameterLabels = { "Event Log" }, returnLabels = { "Filtered Log" }, returnTypes = { XLog.class }, userAccessible = true, help = LowOccurrencesFilterHelp.TEXT)
public class LowOccurrencesFilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Filter Out Low-Occurrence Traces (Single Log), UI", requiredParameterLabels = { 0 })
	public XLog runUI(UIPluginContext context, XLog log) {
		LowOccurrencesFilterParameters parameters = new LowOccurrencesFilterParameters(log);
		LowOccurrencesFilterDialog dialog = new LowOccurrencesFilterDialog(log, parameters);
		InteractionResult result = context.showWizard("Configure Low-Occurrence Filter", true, true, dialog);
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
	
	@PluginVariant(variantLabel = "Filter Out Low-Occurrence Traces (Single Log), Parameters", requiredParameterLabels = { 0 })
	public XLog run(PluginContext context, XLog log, LowOccurrencesFilterParameters parameters) {
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use run instead.
	 */
	@Deprecated
	public XLog publicParameters(UIPluginContext context, XLog log, LowOccurrencesFilterParameters parameters) {
		return run(context, log, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Filter Out Low-Occurrence Traces (Single Log), Default", requiredParameterLabels = { 0 })
	public XLog runDefault(PluginContext context, XLog log) {
		LowOccurrencesFilterParameters parameters = new LowOccurrencesFilterParameters(log);
		return runConnections(context, log, parameters);
	}
	
	/**
	 * @deprecated Use runDefault instead.
	 */
	@Deprecated
	public XLog publicDefault(UIPluginContext context, XLog log) {
		return runDefault(context, log);
	}
	
	private XLog runConnections(PluginContext context, XLog log, LowOccurrencesFilterParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<LowOccurrencesFilterConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						LowOccurrencesFilterConnection.class, context, log);
				for (LowOccurrencesFilterConnection connection : connections) {
					if (connection.getObjectWithRole(LowOccurrencesFilterConnection.LOG)
							.equals(log) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(LowOccurrencesFilterConnection.FILTEREDLOG);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		XLog filteredLog = (new LowOccurrencesFilterAlgorithm()).apply(context, log, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new LowOccurrencesFilterConnection(log, filteredLog, parameters));
		}
		return filteredLog;
	}

}
