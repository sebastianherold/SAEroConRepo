package org.processmining.framework.util.ui.widgets.logging;

import java.util.logging.Logger;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMPropertiesPanel;
import org.processmining.framework.util.ui.widgets.ProMTextField;

/**
 * @author michael
 * 
 */
@Plugin(name = "Create Fresh Log", parameterLabels = {}, returnLabels = { "Log" }, returnTypes = { Loggable.class })
public class CreateLogPlugin {
	/**
	 * @param context
	 * @return
	 */
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M. Westergaard", email = "m.westergaard@tue.nl")
	@PluginVariant(requiredParameterLabels = {})
	public static Loggable createLog(final UIPluginContext context) {
		final ProMPropertiesPanel panel = new ProMPropertiesPanel(null);
		final ProMTextField name = panel.addTextField("Log Name");
		if (context.showConfiguration("New Log", panel) == InteractionResult.CONTINUE) {
			return new Loggable() {
				transient Logger log = Logger.getLogger(name.getText());

				@Override
				public Logger getLogger() {
					return log;
				}
			};
		}
		context.getFutureResult(0).cancel(true);
		return null;
	}
}
