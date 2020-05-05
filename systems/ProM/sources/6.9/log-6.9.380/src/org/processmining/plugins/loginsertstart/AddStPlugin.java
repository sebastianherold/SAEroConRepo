package org.processmining.plugins.loginsertstart;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;

/**
 * Add missing start events to the log
 * 
 * @author jnakatumba
 * 
 */
@Plugin(name = "Add Missing Events", categories = { PluginCategory.Filtering }, parameterLabels = { "Log" }, returnLabels = { "Log (With Start Events)" }, returnTypes = { AddStVisualizer.class }, userAccessible = true)
public class AddStPlugin {
	/**
	 * This class adds missing start events to the event log
	 * 
	 * @param context
	 *            The pluginContext in which this plugin is executed.
	 * 
	 * @param log
	 *            The log that needs to be filtered.
	 * @return the changed log.
	 */
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "J. Nakatumba", email = "jnakatum@tue.nl")
	@PluginVariant(variantLabel = "Default Settings", requiredParameterLabels = { 0 })
	public static AddStVisualizer filter(final UIPluginContext context, final XLog log) {
		AddSt miner = new AddSt(context, log);
		AddStVisualizer output = miner.mine();

		String name = XConceptExtension.instance().extractName(log);
		context.getFutureResult(0).setLabel(name + "(With Start Events)");

		return output;

	}

}
