package org.processmining.plugins.log.logfilters;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Filter Log on Trace Attribute Values", level= PluginLevel.PeerReviewed, categories = { PluginCategory.Filtering }, parameterLabels = { "Log", "Parameters" }, returnLabels = { "Log" }, returnTypes = { XLog.class })
public class TraceAttributeFilterPlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Filter Log on Trace Attribute Values, UI", requiredParameterLabels = { 0 })
	public XLog filterDialog(UIPluginContext context, XLog log) {
		context.getProgress().setMaximum(3 * log.size());
		TraceAttributeFilterParameters parameters = new TraceAttributeFilterParameters(context, log);
		AttributeFilterDialog dialog = new AttributeFilterDialog(context, parameters, " (filtered on trace attributes)");
		InteractionResult result = context.showWizard("Configure filter (values)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			context.log("Canceled by user.");
			context.getFutureResult(0).cancel(true);
			return null;
		}
		dialog.applyFilter();
		return filterPrivate(context, log, parameters);
	}

	@PluginVariant(variantLabel = "Filter Log on Trace Attribute Values, Parameters", requiredParameterLabels = { 0 })
	public XLog filterParameters(PluginContext context, XLog log, AttributeFilterParameters parameters) {
		return filterPrivate(context, log, parameters);
	}

	private XLog filterPrivate(PluginContext context, XLog log, AttributeFilterParameters parameters) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog filteredLog = factory.createLog((XAttributeMap) log.getAttributes().clone());
		filteredLog.getClassifiers().addAll(log.getClassifiers());
		filteredLog.getExtensions().addAll(log.getExtensions());
		filteredLog.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		filteredLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		for (XTrace trace : log) {
			boolean add = true;
			if (trace.getAttributes().keySet().containsAll(parameters.getMustHaves())) {
				for (String key : trace.getAttributes().keySet()) {
					String value = trace.getAttributes().get(key).toString();
					if (!parameters.getFilter().get(key).contains(value)) {
						add = false;
						continue;
					}
				}
				if (add) {
					filteredLog.add(trace);
				}
			}
			context.getProgress().inc();
		}
		XConceptExtension.instance().assignName(filteredLog, parameters.getName());
		context.getFutureResult(0).setLabel(parameters.getName());
		return filteredLog;
	}
}
