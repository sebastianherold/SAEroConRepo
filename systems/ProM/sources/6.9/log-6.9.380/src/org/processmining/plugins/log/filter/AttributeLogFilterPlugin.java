package org.processmining.plugins.log.filter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "Filter Log by Attributes",
	parameterLabels = { "a log", "filter settings"}, //
	returnLabels = { "filtered log" },
	returnTypes = { XLog.class }, 
	userAccessible = true,
	help = "Filter traces and individual events from the log based on the presence or absence of attributes with particular values.",
	mostSignificantResult = 1)
public class AttributeLogFilterPlugin {
	
	// take log and net as input and guess initial marking
	@UITopiaVariant(
			affiliation="TU/e",
			author="D. Fahland",
			email="d.fahland@tue.nl",
			website = "http://www.processmining.org/",
			pack="Uma")
	@PluginVariant(variantLabel = "Filter Log by Attributes", requiredParameterLabels = { 0 })
	public XLog filterLog(UIPluginContext context, XLog log) {
		
		AttributeLogFilter filter = new AttributeLogFilter(log);
		AttributeLogFilter_UI ui = new AttributeLogFilter_UI(filter);
		if (ui.setParameters(context, filter) != InteractionResult.CANCEL)
			return filterLog(context, log, filter);
		else
			return cancel(context, "Canceled by user.");
				
	}
	
	@PluginVariant(variantLabel = "Filter Log by Attributes", requiredParameterLabels = { 0, 1 })
	public XLog filterLog(PluginContext context, XLog log, AttributeLogFilter filter) {

		XFactory f = XFactoryRegistry.instance().currentDefault();
		
		// create new log, copy original attributes
		XLog filtered = f.createLog(log.getAttributes());
		
		// HV: Copy log metadata.
		filtered.getExtensions().addAll(log.getExtensions());
		filtered.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		filtered.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		filtered.getClassifiers().addAll(log.getClassifiers());
		
		for (XTrace t : log) {
			if (filter.keepTrace(t)) {
				filtered.add(t);
			}
		}
		
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		String timeString = dateFormat.format(date);
		
		String logName = XConceptExtension.instance().extractName(log);
		if (logName == null) logName = "log";
		logName = logName+" (filtered @ "+timeString+")";
		context.getFutureResult(0).setLabel(logName);
		
		return filtered;
	}

	protected static XLog cancel(PluginContext context, String message) {
		System.out.println("[AttributeFilter]: "+message);
		context.log(message);
		context.getFutureResult(0).cancel(true);
		return null;
	}
}
