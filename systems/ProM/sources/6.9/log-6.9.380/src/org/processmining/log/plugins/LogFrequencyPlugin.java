package org.processmining.log.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.log.algorithms.LogFrequencyAlgorithm;
import org.processmining.log.connections.LogFrequencyConnection;
import org.processmining.log.dialogs.LogFrequencyDialog;
import org.processmining.log.help.LogFrequencyHelp;
import org.processmining.log.models.LogFrequency;
import org.processmining.log.parameters.LogFrequencyParameters;

@Plugin(name = "Create Frequency Distribution", parameterLabels = { "Event Log" }, returnLabels = { "Log Frequency Distribution" }, returnTypes = { LogFrequency.class }, userAccessible = true, help = LogFrequencyHelp.TEXT)
public class LogFrequencyPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Create Frequency Distribution, UI", requiredParameterLabels = { 0 })
	public LogFrequency runUI(UIPluginContext context, XLog log) {
		LogFrequencyParameters parameters = new LogFrequencyParameters(log);
		LogFrequencyDialog dialog = new LogFrequencyDialog(log, parameters);
		InteractionResult result = context.showWizard("Configure Frequency Distribution (classifier)", true, true,
				dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use runUI() instead.
	 */
	@Deprecated
	public LogFrequency publicUI(UIPluginContext context, XLog log) {
		return runUI(context, log);
	}
	
	@PluginVariant(variantLabel = "Create Frequency Distribution, Parameters", requiredParameterLabels = { 0 })
	public LogFrequency run(PluginContext context, XLog log, LogFrequencyParameters parameters) {
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use run() instead.
	 */
	@Deprecated
	public LogFrequency publicParameters(PluginContext context, XLog log, LogFrequencyParameters parameters) {
		return run(context, log, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Eric Verbeek", email = "h.m.w.verbeek@tue.nl", website = "www.processmining.org")
	@PluginVariant(variantLabel = "Create Frequency Distribution, Default", requiredParameterLabels = { 0 })
	public LogFrequency runDefault(PluginContext context, XLog log) {
		LogFrequencyParameters parameters = new LogFrequencyParameters(log);
		return runConnections(context, log, parameters);
	}

	/**
	 * @deprecated Use runDefault() instead.
	 */
	@Deprecated
	public LogFrequency publicDefault(PluginContext context, XLog log) {
		return runDefault(context, log);
	}

	private LogFrequency runConnections(PluginContext context, XLog log, LogFrequencyParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<LogFrequencyConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(
						LogFrequencyConnection.class, context, log);
				for (LogFrequencyConnection connection : connections) {
					if (connection.getObjectWithRole(LogFrequencyConnection.LOG)
							.equals(log) && connection.getParameters().equals(parameters)) {
						return connection
								.getObjectWithRole(LogFrequencyConnection.LOGFREQUENCY);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}
		LogFrequency logFrequency = (new LogFrequencyAlgorithm()).apply(context, log, parameters);
		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(
					new LogFrequencyConnection(log, logFrequency, parameters));
		}
		return logFrequency;
	}
}
