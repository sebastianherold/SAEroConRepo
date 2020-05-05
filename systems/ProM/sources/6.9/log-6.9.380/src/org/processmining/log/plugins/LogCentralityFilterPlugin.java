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
import org.processmining.log.algorithms.LogCentralityFilterAlgorithm;
import org.processmining.log.connections.LogCentralityFilterConnection;
import org.processmining.log.dialogs.LogCentralityFilterDialog;
import org.processmining.log.help.LogCentralityFilterHelp;
import org.processmining.log.models.LogCentrality;
import org.processmining.log.parameters.LogCentralityFilterParameters;

@Plugin(name = "Happify Log", categories = { PluginCategory.Filtering }, parameterLabels = { "Happifiable Log", "Parameters" }, returnLabels = { "Happified Log" }, returnTypes = { XLog.class }, help = LogCentralityFilterHelp.TEXT)
public class LogCentralityFilterPlugin extends LogCentralityFilterAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack="Log")
	@PluginVariant(variantLabel = "Happify Log, UI", requiredParameterLabels = { 0 })
	public XLog runDialog(UIPluginContext context, LogCentrality centrality) {
		LogCentralityFilterParameters parameters = new LogCentralityFilterParameters(centrality);
		LogCentralityFilterDialog dialog = new LogCentralityFilterDialog(context, centrality, parameters);
		InteractionResult result = context.showWizard("Configure Happification of Log", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnections(context, centrality, parameters);
	}
	
	@PluginVariant(variantLabel = "Happify Log, Parameters", requiredParameterLabels = { 0, 1 })
	public XLog runParameters(PluginContext context, LogCentrality centrality, LogCentralityFilterParameters parameters) {
		return runConnections(context, centrality, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl", pack="Log")
	@PluginVariant(variantLabel = "Happify Log, Default", requiredParameterLabels = { 0 })
	public XLog runDefault(PluginContext context, LogCentrality centrality) {
		LogCentralityFilterParameters parameters = new LogCentralityFilterParameters(centrality);
		return runConnections(context, centrality, parameters);
	}
	
	private XLog runConnections(PluginContext context, LogCentrality centrality, LogCentralityFilterParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<LogCentralityFilterConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						LogCentralityFilterConnection.class, context, centrality);
				for (LogCentralityFilterConnection connection : connections) {
					if (connection.getObjectWithRole(LogCentralityFilterConnection.LOGCENTRALITY)
							.equals(centrality) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(LogCentralityFilterConnection.LOG);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		XLog filteredLog = apply(context, centrality, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new LogCentralityFilterConnection(filteredLog, centrality, parameters));
		}
		return filteredLog;
	}
}
