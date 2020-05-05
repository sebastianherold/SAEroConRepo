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
import org.processmining.log.algorithms.LogCentralityAlgorithm;
import org.processmining.log.connections.LogCentralityConnection;
import org.processmining.log.dialogs.LogCentralityDialog;
import org.processmining.log.help.LogCentralityHelp;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityParameters;

@Plugin(name = "Create Happifiable Log", categories = { PluginCategory.Enhancement }, parameterLabels = { "Event Log", "Parameters" }, returnLabels = { "Happifiable Log" }, returnTypes = { LogCentrality.class }, help = LogCentralityHelp.TEXT)
public class LogCentralityPlugin extends LogCentralityAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack="Log")
	@PluginVariant(variantLabel = "Create Happifiable Log, UI", requiredParameterLabels = { 0 })
	public LogCentrality runUI(UIPluginContext context, XLog log) {
		LogCentralityParameters parameters = new LogCentralityParameters(log);
		LogCentrality centrality = new LogCentrality(log);
		LogCentralityDialog dialog = new LogCentralityDialog(context, log, centrality, parameters);
		InteractionResult result = context.showWizard("Configure Creation of Happifiable Log", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnections(context, log, centrality, parameters);
	}
	
	/**
	 * @deprecated Use runUI() instead.
	 */
	@Deprecated
	public LogCentrality runDialog(UIPluginContext context, XLog log) {
		return runUI(context, log);
	}
	
	@PluginVariant(variantLabel = "Create Happifiable Log, Parameters", requiredParameterLabels = { 0, 1 })
	public LogCentrality run(PluginContext context, XLog log, LogCentralityParameters parameters) {
		LogCentrality centrality = new LogCentrality(log);
		return runConnections(context, log, centrality, parameters);
	}
	
	/**
	 * @deprecated Use runUI() instead.
	 */
	@Deprecated
	public LogCentrality runParameters(PluginContext context, XLog log, LogCentralityParameters parameters) {
		return run(context, log, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack="Log")
	@PluginVariant(variantLabel = "Create Happifiable Log, Default", requiredParameterLabels = { 0 })
	public LogCentrality runDefault(PluginContext context, XLog log) {
		LogCentralityParameters parameters = new LogCentralityParameters(log);
		LogCentrality centrality = new LogCentrality(log);
		return runConnections(context, log, centrality, parameters);
	}
	
	private LogCentrality runConnections(PluginContext context, XLog log, LogCentrality centrality, LogCentralityParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<LogCentralityConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						LogCentralityConnection.class, context, log);
				for (LogCentralityConnection connection : connections) {
					if (connection.getObjectWithRole(LogCentralityConnection.LOG)
							.equals(log) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(LogCentralityConnection.LOGCENTRALITY);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		LogCentrality logCentrality = apply(context, log, centrality, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new LogCentralityConnection(log, logCentrality, parameters));
		}
		return logCentrality;
	}
}
